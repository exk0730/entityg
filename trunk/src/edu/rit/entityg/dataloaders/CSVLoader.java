package edu.rit.entityg.dataloaders;

import edu.rit.entityg.csv.CSVConnection;
import edu.rit.entityg.csv.TableRow;
import edu.rit.entityg.exceptions.BadSetupException;
import edu.rit.entityg.treeimpl.GenericTreeNode;
import edu.rit.entityg.utils.ExceptionUtils;
import java.io.IOException;
import java.util.HashMap;

/**
 * Implements {@link DataSourceLoader} for a {@link DataSourceType#CSV} data source. The following
 * represents what information is needed prior to loading data into EntityG from a CSV file:
 * <ul>
 * <li>The file name that we're loading data from.</li>
 * <li>The column number that we want center node's to take data from.</li>
 * <br/>
 * <li>The column names of each column.</li>
 * <br/>
 * <li>The information nodes column numbers.</li>
 * </ul>
 * @date Jun 4, 2011
 * @author Eric Kisner
 */
public class CSVLoader implements DataSourceLoader {

    private CSVConnection conn;
    private HashMap<Integer, String> columnToColumnNameMapping;
    private int centerNodeColumnNumber;
    private int[] informationNodeColumnNumbers;

    /**
     * Default constructor.
     * @param conn A connection to the CSV file.
     */
    public CSVLoader( CSVConnection conn ) {
        this.conn = conn;
        this.columnToColumnNameMapping = new HashMap<Integer, String>();
    }

    public void close() throws IOException {
        conn.close();
    }

    /**
     * Sets the number of the center node column.
     * @param columnNumber The number that represents a column in the CSV file, which relates to center nodes.
     */
    public void setCenterNodeColumnNumber( int columnNumber ) {
        this.centerNodeColumnNumber = columnNumber;
    }

    /**
     * Sets the list of information node column numbers.
     * @param columnNumbers An array of integers that represent CSV column numbers, which relate to information nodes.
     */
    public void setInformationNodeColumnNumbers( int[] columnNumbers ) {
        this.informationNodeColumnNumbers = columnNumbers;
    }

    /**
     * Sets the mapping of column numbers to headers.
     * @param columnToNameMapping A {@link HashMap} with a key of an integer, representing a column number in the CSV
     *                            file, and a value of a String, representing the name of that column.
     */
    public void setColumnToNameMapping( HashMap<Integer, String> columnToNameMapping ) {
        this.columnToColumnNameMapping = columnToNameMapping;
    }

    /**
     * @param data The column name we want to pull data from to populate a center node.
     */
    public GenericTreeNode<String> loadAbsoluteParent( Object data ) throws BadSetupException {
        if( centerNodeColumnNumber == -1
            || informationNodeColumnNumbers.length == 0
            || columnToColumnNameMapping.isEmpty() ) {
            throw new BadSetupException( "You must provide a valid center node column number, information node column "
                                         + "numbers, and/or valid column names to run a CSV EntityG instance." );
        }

        String s = (String) data;
        if( !columnToColumnNameMapping.containsKey( centerNodeColumnNumber ) ) {
            throw new BadSetupException( "The column number '" + centerNodeColumnNumber + "' does not exist in the "
                                         + "CSV file." );
        } else if( !columnToColumnNameMapping.get( centerNodeColumnNumber ).equals( s ) ) {
            throw new BadSetupException( "The column number '" + centerNodeColumnNumber + "' does not match the label '"
                                         + s + "'. Please provide the correct column number which matches '"
                                         + s + "'." );
        }

        try {
            //Get the first CSV row of actual data (not the header row)
            TableRow firstLine = conn.getLine();
            //Get the data in the center node column (specified by a column number)
            String centerData = firstLine.getColumn( centerNodeColumnNumber );
            GenericTreeNode<String> rootParent = new GenericTreeNode<String>( true, centerData, s );

            for( int i = 0; i < informationNodeColumnNumbers.length; i++ ) {
                int infoNum = informationNodeColumnNumbers[i];
                rootParent.addChild( new GenericTreeNode<String>( false, firstLine.getColumn( infoNum ),
                                                                  columnToColumnNameMapping.get( infoNum ) ) );
            }
            return rootParent;
        } catch( IOException ioe ) {
            throw new BadSetupException( "There was an error trying to receieve data from the csv file." );
        }
    }

    /**
     * @param data The data that is contained in <code>parent</code>. <code>parent</code> will be a center node.
     */
    public GenericTreeNode<String> loadInformationNodes( GenericTreeNode<String> parent, Object... data )
            throws BadSetupException {
        String centerNodeData = (String) data[0];

        try {
            TableRow row = null;
            while( (row = conn.getLine()) != null ) {
                if( row.getColumn( centerNodeColumnNumber ).equals( centerNodeData ) ) {
                    break;
                } else {
                    row = null;
                }
            }

            if( row != null ) {
                for( int i = 0; i < informationNodeColumnNumbers.length; i++ ) {
                    int infoNum = informationNodeColumnNumbers[i];
                    parent.addChild( new GenericTreeNode<String>( false, row.getColumn( infoNum ),
                                                                  columnToColumnNameMapping.get( infoNum ) ) );
                }
            } else {
                ExceptionUtils.handleMessage( "No information for center node: " + parent.toString() );
            }
            conn.reset();
            return parent;
        } catch( IOException ioe ) {
            throw new BadSetupException( "There was a problem retrieving information from the csv file." );
        }
    }

    /**
     * @param data Must be two strings: The data of the information node that the user clicked on, and that piece of
     *             data's column name.
     */
    public GenericTreeNode<String> loadCenterNodes( GenericTreeNode<String> parent, int maxNodes, Object... data )
            throws BadSetupException {
        String informationNodeData = (String) data[0];
        String informationColumnHeader = (String) data[1];

        int column = -1;
        for( int i = 0; i < informationNodeColumnNumbers.length; i++ ) {
            if( columnToColumnNameMapping.get( informationNodeColumnNumbers[i] ).equals( informationColumnHeader ) ) {
                column = informationNodeColumnNumbers[i];
            }
        }

        try {
            TableRow row = null;
            while( (row = conn.getLine()) != null ) {
                if( row.getColumn( column ).equals( informationNodeData ) ) {
                    parent.addChild(
                            new GenericTreeNode<String>( true, row.getColumn( centerNodeColumnNumber ),
                                                         columnToColumnNameMapping.get( centerNodeColumnNumber ) ) );
                }
            }
            conn.reset();
            return parent;
        } catch( IOException ioe ) {
            throw new BadSetupException( "There was a problem retrieving information from the csv file." );
        }
    }
}
