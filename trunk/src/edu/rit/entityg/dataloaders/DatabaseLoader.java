package edu.rit.entityg.dataloaders;

import edu.rit.entityg.database.DatabaseConnection;
import edu.rit.entityg.exceptions.BadSetupException;
import edu.rit.entityg.treeimpl.GenericTreeNode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Implements {@link AbstractDataSourceLoader} for a {@link DataSourceType#DATABASE} data source. The following
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
public class DatabaseLoader implements AbstractDataSourceLoader {

    private String baseQuery;
    private String centerNodeColumnName;
    private String[] columnNames;
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

    public void setBaseQuery( String baseQuery ) {
        this.baseQuery = baseQuery;
    }

    public void setCenterNodeColumnName( String centerNodeColumnName ) {
        this.centerNodeColumnName = centerNodeColumnName;
    }

    public void setInformationNodeColumNames( String[] columnNames ) {
        this.columnNames = columnNames;
    }

    public GenericTreeNode<String> loadAbsoluteParent( Object data ) throws BadSetupException,
                                                                            IllegalArgumentException {
        if( baseQuery == null || centerNodeColumnName == null ) {
            throw new IllegalArgumentException( "You must set a base query and base column header before loading "
                                                + "any data." );
        }

        try {
            ResultSet rs = conn.executeQuery( baseQuery + centerNodeColumnName + " = '" + (String) data + "'" );
            ArrayList<String> results = conn.getSingleRowFromColumnHeaders( rs, Arrays.asList( columnNames ) );
            if( results.isEmpty() ) {
                throw new BadSetupException( "The information provided to setup the graph was invalid. "
                                             + "You must provide specifications that allow EntityG to load data. "
                                             + "You have provided specifications that do not give any data to "
                                             + "EntityG." );
            } else if( columnNames.length > results.size() ) {
                throw new BadSetupException( "There are null values in your database which you want displayed on the "
                                             + "first level. Please make sure the first node level has no "
                                             + "null values." );
            }
            GenericTreeNode<String> rootParent = new GenericTreeNode<String>( true, (String) data, centerNodeColumnName );
            //Add the children data to the root parent
            for( int i = 0; i < results.size(); i++ ) {
                rootParent.addChild( new GenericTreeNode<String>( false, results.get( i ), columnNames[i] ) );
            }
            return rootParent;
        } catch( SQLException sqle ) {
            throw new BadSetupException( sqle.getMessage() );
        }
    }

    public GenericTreeNode<String> loadInformationNodes( GenericTreeNode<String> parent,
                                                         Object... obj ) throws BadSetupException {
        String data = (String) obj[0];
        String columnHeader = (String) obj[1];
        try {
            ResultSet rs = conn.executeQuery( baseQuery + columnHeader + " = '" + data + "'" );
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
        try {
            ResultSet rs = conn.executeQuery( baseQuery + columnHeader + " = '" + data + "'" );
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
}
