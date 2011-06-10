package edu.rit.entityg;

import edu.rit.entityg.csv.CSVConnection;
import edu.rit.entityg.dataloaders.CSVLoader;
import edu.rit.entityg.exceptions.BadSetupException;
import edu.rit.entityg.treeimpl.GenericTreeNode;
import edu.rit.entityg.utils.ExceptionUtils;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.SwingUtilities;
import prefuse.data.Node;
import prefuse.visual.VisualItem;

import static edu.rit.entityg.csv.CSVConnection.DELIM;

/**
 * {@link CSVEntityG} is a class that implements any methods that are required when loading data from a CSV file.
 * This includes: setting up the first node and its children, and setting any CSV fields - like information node
 * columns, and the center node column.
 * @date May 25, 2011
 * @author Eric Kisner
 */
public class CSVEntityG extends AbstractEntityG {

    private String fileName;
    private String centerColumnName;
    private int centerColumnNumber;
    private int[] infoColumnNumbers;
    private HashMap<Integer, String> columnToNameMapping;
    /**
     * The data loader for EntityG.
     */
    private CSVLoader loader;

    public CSVEntityG() {
        super();
    }

    /**
     * Set the path to the CSV file.
     * @param fileName The CSV file's path.
     */
    public void set_file_name( String fileName ) {
        this.fileName = fileName;
    }
    
    /**
     * Sets the center node column name.
     * @param columnName The name of the column in the CSV file that contains center node data.
     */
    public void set_center_node_column_name( String columnName ) {
        this.centerColumnName = columnName;
    }


    /**
     * Set the column number that contains center node data.
     * @param columnNumber An integer as a String that represents the column index.
     */
    public void set_center_node_column_number( String columnNumber ) {
        try {
            this.centerColumnNumber = Integer.parseInt( columnNumber ) - 1;
        } catch( NumberFormatException nfe ) {
            ExceptionUtils.handleMessage( columnNumber + " is not a number." );
            this.centerColumnNumber = -1;
        }
    }

    /**
     * Sets the column names to each column in the CSV file.
     * @param columnNames A list of Strings, with a {@link CSVConnection#DELIM} separating each header. Also, it is
     *                    important that these column names are in the same order as the columns in the CSV file. All
     *                    columns must have headers / must be represented somehow.
     */
    public void set_column_to_name_mapping( String columnNames ) {
        columnToNameMapping = new HashMap<Integer, String>();
        String[] temp = columnNames.split( DELIM );
        for( int i = 0; i < temp.length; i++ ) {
            columnToNameMapping.put( i, temp[i] );
        }
    }

    /**
     * Sets the list of column numbers which contain all information node data.
     * @param columnNumbers A list of integers, represented as a String with {@link CSVConnection#DELIM} separating
     *                      each number.
     */
    public void set_information_node_column_numbers( String columnNumbers ) {
        String[] temp = columnNumbers.split( DELIM );
        this.infoColumnNumbers = new int[temp.length];
        for( int i = 0; i < temp.length; i++ ) {
            try {
                infoColumnNumbers[i] = Integer.parseInt( temp[i] ) - 1;
            } catch( NumberFormatException nfe ) {
                ExceptionUtils.handleMessage( temp[i] + " is not a number." );
                this.infoColumnNumbers = new int[0];
                break;
            }
        }
    }

    public void connectToDataSource() {
        CSVConnection.setProperties( fileName );
        loader = new CSVLoader( CSVConnection.instance() );
        super.registerLoader( loader );
    }

    public GenericTreeNode<String> setupAbsoluteParent() {
        loader.setCenterNodeColumnNumber( centerColumnNumber );
        loader.setColumnToNameMapping( columnToNameMapping );
        loader.setInformationNodeColumnNumbers( infoColumnNumbers );
        try {
            GenericTreeNode<String> ret = loader.loadAbsoluteParent( centerColumnName );
            return ret;
        } catch( BadSetupException bse ) {
            ExceptionUtils.handleException( bse );
            throw new RuntimeException( bse );
        }
    }

    public void customItemClicked( VisualItem item, MouseEvent e ) {
        if( !SwingUtilities.isLeftMouseButton( e ) ) return;
        if( e.getClickCount() == 2 ) {//DoubleClick
            //The backing Tuple of this visual item is actually a Node object (from g.addNode)
            Node source = (Node) item.getSourceTuple();
            //Get the related TreeNode of this Node
            GenericTreeNode<String> treeNode = displayNodeToDataNodeMap.get( source );
            /**
             * If the Tree node has children, and they are visible nodes on the graph, we want to set those
             * nodes to be invisible. Else, if the tree node has children and they are invisible, we want
             * to set those nodes to be visible.
             */
            if( treeNode.hasChildren() ) {
                if( hasVisibleChildren( item ) ) {
                    setVisibilityOfAllChildren( item, false );
                } else {
                    setVisibilityOfAllChildren( item, true );
                }
            } else {
                //If they click on a "center node"
                try {
                    if( treeNode.isCenterNode() ) {
                        treeNode = loader.loadInformationNodes( treeNode, treeNode.getData() );
                    } else {    //Else they clicked on an information node
                        treeNode = loader.loadCenterNodes( treeNode, defaultMaxNodes, treeNode.getData(),
                                                           treeNode.getDataHeader() );
                    }
                } catch( BadSetupException bse ) {
                    ExceptionUtils.handleException( bse );
                }
                //Retrieve all children of this TreeNode and render it on the graph.
                if( treeNode.hasChildren() ) {
                    renderNewNodes( source, treeNode );
                }
            }
        }
    }
}
