/**
 * Database connection. This class provides methods to:
 *  - connect to a database
 *  - close the database
 *  - execute queries that return result sets
 *  - execute queries that don't return result sets
 *  - create prepared statements
 *  - get data from a ResultSet
 */
package edu.rit.entityg.database;

import edu.rit.entityg.utils.Logging;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Connects to a MySQL database.
 * @author Eric Kisner
 */
public class DatabaseConnection {

    /**
     * Default static configuration variables
     */
    private static final String HOST = "localhost";
    private static final String PORT = "3307";
    private static final String DATABASE = "vardb";
    private static final String UID = "root";
    private static final String PASS = "";
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String URL = "jdbc:mysql://";
    private String host = HOST;
    private String port = PORT;
    private String database = DATABASE;
    private String uid = UID;
    private String password = PASS;
    /**
     * Connection object
     */
    private Connection connect = null;
    /**
     * Singleton Design Pattern DatabaseConnection instance.
     */
    private static DatabaseConnection instance = null;
    /**
     * Default properties for connecting to the VAR App database.
     */
    public final static Properties setup = new Properties();

    static {
        setup.setProperty( "url", URL + HOST + ":" + PORT + "/" + DATABASE + "?zeroDateTimeBehavior=convertToNull" );
        setup.setProperty( "uid", UID );
        setup.setProperty( "password", PASS );
    }

    public static void setProperties( String host, String port, String database, String uid, String password ) {
        setup.clear();
        setup.setProperty( "port", port );
        setup.setProperty( "host", host );
        setup.setProperty( "database", database );
        setup.setProperty( "uid", uid );
        setup.setProperty( "password", password );
    }

    /**
     * Singleton-Design Pattern. We do not want to establish a connection to the database each time we transition
     * between different views, we also do not want to have to pass a database connection object throughout the
     * presentation layer.
     * @return The static initialized DatabaseConnection instance.
     */
    public static DatabaseConnection instance() {
        if( instance == null ) {
            instance = new DatabaseConnection( setup );
        }
        return instance;
    }

    /**
     * Constructor to specify database location
     */
    private DatabaseConnection( Properties props ) {
        if( props.containsKey( "host" ) ) {
            host = props.getProperty( "host" );
        }
        if( props.containsKey( "port" ) ) {
            port = props.getProperty( "port" );
        }
        if( props.containsKey( "database" ) ) {
            database = props.getProperty( "database" );
        }
        if( props.containsKey( "uid" ) ) {
            uid = props.getProperty( "uid" );
        }
        if( props.containsKey( "password" ) ) {
            password = props.getProperty( "password" );
        }

        String url = URL + host + ":" + port + "/" + database + "?zeroDateTimeBehavior=convertToNull";
        connect( DRIVER, url, uid, password );
    }

    /**
     * Connect to the database
     * @param driver Driver to use for connecting to the DBMS
     * @param pass Password to access the database
     * @param uid User Id to access the database
     * @param url URL to the database
     * @throws java.sql.SQLException
     */
    private void connect( String driver, String url, String uid, String pass ) {
        try {
            Class.forName( driver );
            connect = DriverManager.getConnection( url, uid, pass );
        } catch( ClassNotFoundException cnfe ) {
            Logging.log( "Class " + driver + " has not been found. Terminating the program.", this.getClass() );
            System.exit( 1 );
        } catch( SQLException sqle ) {
            Logging.log( "The application has encountered an error when trying to establish a connection to the "
                         + "database. Please check the log files for more information.", this.getClass() );
            System.exit( 1 );
        }
    }

    /**
     * Close the connection
     * @throws java.sql.SQLException
     */
    public void close() throws SQLException {
        if( !connect.isClosed() && instance != null ) {
            connect.close();
            instance = null;
        }
    }

    /**
     * Returns current open-status of the database connection.
     * @return True if the database connection is open, else false.
     * @throws SQLException
     */
    public boolean isOpen() throws SQLException {
        return !connect.isClosed();
    }

    /**
     * Starts or stops a transaction by setting the DBMS' autoCommit value flag.
     * If the flag is set to false, a transaction will be started. If the flag is set to true,
     * the method will commit for you and then stop the transaction.
     * @param on Flag to set the autoCommit value. If false, a transaction will be started. If true,
     *            a commit will be performed, followed by autoCommit being turned back on.
     */
    public void setAutoCommit( boolean on ) throws SQLException {
        if( on ) {
            connect.commit();
        }
        connect.setAutoCommit( on );
    }

    /**
     * @see Connection#rollback()
     * @throws SQLException
     */
    public void rollback() throws SQLException {
        connect.rollback();
    }

    /**
     * Execute a query that doesn't return a result
     * @param query The string query
     * @throws java.sql.SQLException
     */
    public void executeNonQuery( String query ) throws SQLException {
        Statement st = connect.createStatement();
        st.execute( query );
    }

    /**
     * Execute a query that returns success or failure (update, insert, delete).
     * @param query The string query
     * @return The result of running the update query.
     * @throws SQLException
     */
    public int executeUpdateQuery( String query ) throws SQLException {
        Statement st = connect.createStatement();
        return st.executeUpdate( query );
    }

    /**
     * Execute a query that returns a result
     * @param query the query
     * @return the ResultSet
     * @throws java.sql.SQLException
     */
    public ResultSet executeQuery( String query ) throws SQLException {
        Statement st = connect.createStatement();
        ResultSet rs = st.executeQuery( query );
        return rs;
    }

    /**
     * Create a new prepared statement that allows auto-generated keys to be returned.
     * @param query
     * @return
     * @throws java.sql.SQLException
     */
    public PreparedStatement newPreparedStatement( String query ) throws SQLException {
        return connect.prepareStatement( query, Statement.RETURN_GENERATED_KEYS );
    }

    /**
     * Returns a new prepared statement using a sql statement and an array of parameters.
     * @param stmnt The sql statement to use for the prepared statement.
     * @param objs The parameters to inject into the prepared statement.
     * @return A prepared statement.
     * @throws SQLException
     */
    public PreparedStatement buildStmnt( String stmnt, Object... objs ) throws SQLException {
        PreparedStatement ret = newPreparedStatement( stmnt );
        if( objs.length < 1 || objs == null ) {
            return ret;
        }
        int rowIndx = 0;
        do {
            ret.setObject( (rowIndx + 1), objs[rowIndx] );
            rowIndx++;
        } while( rowIndx < objs.length );
        return ret;
    }

    /**
     * Generic method to return all data of a ResultSet within a collection. Column headers can be included.
     * @param rs The ResultSet to return all data from.
     * @param columns Flag to indicate if we want the column headers returned with the data.
     * @return A collection of Rows of data (implemented as a collection of strings), or an empty collection.
     * @throws SQLException
     */
    public ArrayList<ArrayList<String>> getData( ResultSet rs, boolean columns ) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
        ArrayList<String> temp = new ArrayList<String>();
        if( columns ) {
            for( int i = 1; i <= rsmd.getColumnCount(); i++ ) {
                temp.add( rsmd.getColumnName( i ) );
            }
            ret.add( temp );
            temp = new ArrayList<String>();
        }
        while( rs.next() ) {
            for( int i = 1; i <= rsmd.getColumnCount(); i++ ) {
                temp.add( rs.getString( i ) );
            }
            ret.add( temp );
            temp = new ArrayList<String>();
        }
        return ret;
    }

    public ArrayList<ArrayList<String>> getData( ResultSet rs ) throws SQLException {
        return getData( rs, false );
    }

    /**
     * Gets a List of rows for a specific column in the provided result set.
     * For example: we can specify "Name" as our <code>column</code> arg, and we will get a list of rows
     * which will each only contain the "Name" result. Therefore, the size of each "row" is 1, if the
     * column is found.
     * @param rs The ResultSet we are looking through.
     * @param column The column data to return.
     * @return A list of rows, represented by a list of strings.
     * @throws SQLException
     */
    public ArrayList<ArrayList<String>> getData( ResultSet rs, String column ) throws SQLException {
        ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
        ArrayList<String> tempRow = new ArrayList<String>();
        while( rs.next() ) {
            tempRow.add( rs.getString( column ) );
            ret.add( tempRow );
            tempRow = new ArrayList<String>();
        }
        return ret;
    }

    /**
     * Gets a single row's data. This method should only be used when the ResultSet is known to only return one row.
     * Prior to using this method, the ResultSet cursor should already be on the first row.
     * @return The result set's data as a collection of strings.
     * @throws SQLException
     */
    public ArrayList<String> getSingleRow( ResultSet rs ) throws SQLException {
        ArrayList<String> ret = new ArrayList<String>();
        if( !rs.next() ) {
            return ret;
        }
        for( int i = 1; i <= rs.getMetaData().getColumnCount(); i++ ) {
            String toAdd = rs.getString( i );
            if( rs.wasNull() || toAdd.equalsIgnoreCase( "null" ) ) {
                continue;
            }
            ret.add( toAdd );
        }
        return ret;
    }

    public ArrayList<String> getSingleRowFromColumnHeaders( ResultSet rs, List<String> columnHeaders )
            throws SQLException {
        ArrayList<String> ret = new ArrayList<String>();
        if( !rs.next() ) {
            return ret;
        }
        for( String columnHeader : columnHeaders ) {
            String toAdd = rs.getString( columnHeader );
            if( rs.wasNull() || toAdd.equalsIgnoreCase( "null" ) ) {
                continue;
            }
            ret.add( toAdd );
        }
        return ret;
    }
}
