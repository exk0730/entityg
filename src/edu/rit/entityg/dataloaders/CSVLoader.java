package edu.rit.entityg.dataloaders;

import edu.rit.entityg.csv.CSVConnection;
import edu.rit.entityg.exceptions.BadSetupException;
import edu.rit.entityg.treeimpl.GenericTreeNode;
import java.io.IOException;
import java.util.HashMap;

/**
 * @date Jun 4, 2011
 * @author Eric Kisner
 */
public class CSVLoader implements DataSourceLoader {

    private CSVConnection conn;
    private HashMap<Integer, String> columnToColumnNameMapping;
    private int centerNodeColumnNumber;
    private int [] informationNodeColumnNumbers;

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

    public GenericTreeNode<String> loadAbsoluteParent( Object data ) throws BadSetupException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public GenericTreeNode<String> loadInformationNodes( GenericTreeNode<String> parent, Object... data )
            throws BadSetupException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public GenericTreeNode<String> loadCenterNodes( GenericTreeNode<String> parent, int maxNodes, Object... data )
            throws BadSetupException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void setCenterNodeColumnNumber( int columnNumber ) {
        this.centerNodeColumnNumber = columnNumber;
    }

    public void setInformationNodeColumnNumbers( int[] columnNumbers ) {
        this.informationNodeColumnNumbers = columnNumbers;
    }

    public void setColumnToNameMapping( HashMap<Integer, String> columnToNameMapping ) {
        this.columnToColumnNameMapping = columnToNameMapping;
    }
}
