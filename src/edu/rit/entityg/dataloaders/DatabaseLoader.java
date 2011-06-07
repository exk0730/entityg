package edu.rit.entityg.dataloaders;

import edu.rit.entityg.database.DatabaseConnection;
import edu.rit.entityg.exceptions.BadSetupException;
import edu.rit.entityg.treeimpl.GenericTreeNode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Implements {@link DataSourceLoader} for a {@link DataSourceType#DATABASE} data source. The following
 * represents what information is needed prior to loading data into EntityG from a database:
 * <ul>
 * <li>A single SQL query. This query should allow {@link DatabaseLoader} to retrieve any needed information for both
 * center and information nodes and load this information into a Tree structure using {@link GenericTreeNode}s.</li>
 * <br/>
 * <li>The center node column name.</li>
 * <br/>
 * <li>The information nodes column names.</li>
 * </ul>
 * @date May 6, 2011
 * @author Eric Kisner
 */
public class DatabaseLoader implements DataSourceLoader {

    private String baseQuery = null;
    private String centerNodeColumnName = null;
    private String[] columnNames = null;
    /**
     * The database connection.
     */
    private DatabaseConnection conn;

    /**
     * Default constructor.
     * @param conn A connection to the database of our choice.
     */
    public DatabaseLoader( DatabaseConnection conn ) {
        this.conn = conn;
    }

    public void close() throws SQLException {
        conn.close();
    }

    /**
     * Set the base query for loading data into prefuse nodes.
     * <p/>
     * The <code>baseQuery</code> for the database loader is the query which will provide all information needed
     * to load data into the prefuse nodes. This query will usually look something like:
     * <br/><code>SELECT * FROM <i>tableName</i> WHERE</code>
     * <br/>with the ending to the where-clause being specified by what node is clicked on (or initial load
     * specifications). It can be a complicated SQL query, or a simple one, but the query should contain the table
     * name, and possibly the specific columns you want to be looking at (although, these columns can be specified
     * through {@link DatabaseLoader#setInformationNodeColumNames(java.lang.String[])}). If the baseQuery is null at
     * the time of this class performing any functions, the program will fail to execute and exit gracefully.
     */
    public void setBaseQuery( String baseQuery ) {
        this.baseQuery = baseQuery.trim() + " ";
    }

    /**
     * Set the column name of any center node's data.
     * <p/>
     * For example, if we want to display center nodes of type "First Name," the base column name would be "First Name."
     */
    public void setCenterNodeColumnName( String centerNodeColumnName ) {
        this.centerNodeColumnName = centerNodeColumnName;
    }

    /**
     * Sets all information node column names.
     * <p/>
     * For example, if we want to display information nodes "City", "State", and "Zip", those would be our information
     * node column names.
     */
    public void setInformationNodeColumNames( String[] columnNames ) {
        this.columnNames = columnNames;
    }

    public GenericTreeNode<String> loadAbsoluteParent( Object data ) throws BadSetupException {
        if( (baseQuery == null || baseQuery.isEmpty())
            || (centerNodeColumnName == null || centerNodeColumnName.isEmpty())
            || (columnNames == null || columnNames.length == 0) ) {
            throw new BadSetupException( "You must set a base query, base column header, and children column names "
                                         + "before loading any data." );
        }

        String sql = baseQuery + centerNodeColumnName + " = '" + (String) data + "'";
        try {
            ResultSet rs = conn.executeQuery( sql );
            ArrayList<String> results = conn.getSingleRowFromColumnHeaders( rs, Arrays.asList( columnNames ) );
            if( results.isEmpty() ) {
                throw new BadSetupException( longErrorMessage() );
            } else if( columnNames.length > results.size() ) {
                throw new BadSetupException( "There are null values in your database which you want displayed on the "
                                             + "first level. Please make sure the first node level has no "
                                             + "null values." );
            }
            GenericTreeNode<String> rootParent = new GenericTreeNode<String>( true, (String) data,
                                                                              centerNodeColumnName );
            //Add the children data to the root parent
            for( int i = 0; i < results.size(); i++ ) {
                rootParent.addChild( new GenericTreeNode<String>( false, results.get( i ), columnNames[i] ) );
            }
            return rootParent;
        } catch( SQLException sqle ) {
            throw new BadSetupException( "There was an error trying to create a SQL Query from " + sql + " with "
                                         + Arrays.toString( columnNames ) + " as columns to retreive data from." );
        }
    }

    public GenericTreeNode<String> loadInformationNodes( GenericTreeNode<String> parent,
                                                         Object... obj ) throws BadSetupException {
        String data = (String) obj[0];
        String columnHeader = (String) obj[1];
        String sql = baseQuery + columnHeader + " = '" + data + "'";
        try {
            ResultSet rs = conn.executeQuery( sql );
            ArrayList<String> results = conn.getSingleRowFromColumnHeaders( rs, Arrays.asList( columnNames ) );
            if( columnNames.length > results.size() ) {
                throw new BadSetupException( "There are null values in your database which you want displayed. "
                                             + "Ignoring this data." );
            }
            if( !results.isEmpty() ) {
                for( int i = 0; i < results.size(); i++ ) {
                    //Add the children data to the root parent
                    parent.addChild( new GenericTreeNode<String>( false, results.get( i ), columnNames[i] ) );
                }
            }
            return parent;
        } catch( SQLException sqle ) {
            throw new BadSetupException( sqle.getMessage() );
        }
    }

    public GenericTreeNode<String> loadCenterNodes( GenericTreeNode<String> parent, int maxNodes,
                                                    Object... obj ) throws BadSetupException {
        String data = (String) obj[0];
        String columnHeader = (String) obj[1];
        String sql = baseQuery + columnHeader + " = '" + data + "'";
        try {
            ResultSet rs = conn.executeQuery( sql );
            ArrayList<ArrayList<String>> results = conn.getData( rs, centerNodeColumnName );
            if( results.isEmpty() ) {
                return parent;
            }
            for( int i = 0; i < results.size(); i++ ) {
                if( i >= maxNodes )
                    break;
                //Get the only piece of information in our current row.
                String result = results.get( i ).get( 0 );
                parent.addChild( new GenericTreeNode<String>( true, result, centerNodeColumnName ) );
            }
        } catch( SQLException sqle ) {
            throw new BadSetupException( sqle.getMessage() );
        }
        return parent;
    }

    /**
     * Returns a verbose error message to be used in {@link DatabaseLoader#loadAbsoluteParent(java.lang.Object)}.
     */
    private String longErrorMessage() {
        return "The information provided to setup the graph was invalid. You must provide specifications that allow "
               + "EntityG to load data. The specifications that may be invalid are: '" + baseQuery + "', '"
               + centerNodeColumnName + "', or '" + Arrays.toString( columnNames ) + "'.\nPlease correct these before "
               + "continuing.";
    }
}
