package edu.rit.entityg;

import edu.rit.entityg.csv.CSVConnection;
import edu.rit.entityg.dataloaders.CSVLoader;
import edu.rit.entityg.treeimpl.GenericTreeNode;
import java.awt.event.MouseEvent;
import java.util.HashMap;
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

    public GenericTreeNode<String> setupAbsoluteParent() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void customItemClicked( VisualItem item, MouseEvent e ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void connectToDataSource() {
        CSVConnection.setProperties( fileName );
        loader = new CSVLoader( CSVConnection.instance() );
        super.registerLoader( loader );
    }

    public void set_file_name( String fileName ) {
        this.fileName = fileName;
    }

    public void set_center_node_column_number( String columnNumber ) {
        this.centerColumnNumber = Integer.parseInt( columnNumber );
    }

    public void set_information_node_column_numbers( String columnNumbers ) {
        String[] temp = columnNumbers.split( DELIM );
        this.infoColumnNumbers = new int[temp.length];
        for( int i = 0; i < temp.length; i++ ) {
            infoColumnNumbers[i] = Integer.parseInt( temp[i] );
        }
    }

    public void set_column_to_name_mapping( String columnNames ) {
        columnToNameMapping = new HashMap<Integer, String>();
        String[] temp = columnNames.split( DELIM );
        for( int i = 0; i < temp.length; i++ ) {
            columnToNameMapping.put( i, temp[i] );
        }
    }
}
