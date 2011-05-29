package edu.rit.entityg.configure;

import edu.rit.entityg.dataloaders.DataSourceType;
import edu.rit.entityg.exceptions.InvalidIniException;
import edu.rit.entityg.utils.ExceptionUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * {@link EntityGIniFile} is a class specifically used for parsing the <code>entityg.ini</code> file.
 * EntityG can be run as a standalone application, as well as using it within another Java application. This class
 * should be used in both instances when reading from the configuration file which can set up EntityG.
 * <p/>What follows is the possible name-value pairs that can exist in the .ini file or command-line arguments.
 * <br/>The first table lists the default name-value pairs for EntityG.
 * <br/>The second table lists name-value pairs that can be specified for a {@link DataSourceType#DATABASE} source type.
 * <br/>The third table lists name-value pairs that can be specified for a {@link DataSourceType#CSV} source type.
 * <br/>The fourth table lists name-value pairs that can be specified for a {@link DataSourceType#XML} source type.
 * <p/>
 * <h4>Defaults</h4>
 * <table border="1"> <tr><th>name</th><th>value type</th><th>required</th><th>default</th><th>description</th></tr>
 * <p/>
 * <tr><td>datasource_type</td><td>String</td><td>yes</td><td><code>none</code></td>
 * <td>The type of datasource we are loading data from. Choose one of the following: [database | csv | xml]</td></tr>
 * <tr><td>default_max_nodes</td><td>integer greater than 1</td><td>no</td><td>7</td>
 * <td>Integer value to set how many child nodes to display when a user clicks a node.</td></tr>
 * <tr><td>use_tool_tip</td><td><code>true</code> or <code>false</code></td><td>no</td><td>false</td>
 * <td>Flag that specifies if the user wants to see a tool tip when hovering over a node</td></tr>
 * </table>
 *
 * <p/>
 * <h4>Database source</h4>
 * <table border="1"> <tr><th>name</th><th>value type</th><th>required</th><th>default</th><th>description</th></tr>
 * <p/>
 * <tr><td>host</td><td>String</td><td>no</td><td>localhost</td><td>Host of the database.</td></tr>
 * <tr><td>port</td><td>integer</td><td>no</td><td>3306</td><td>Port of the database.</td></tr>
 * <tr><td>user</td><td>String</td><td>no</td><td>root</td><td>Username to get full access to the database.</td></tr>
 * <tr><td>password</td><td>String</td><td>no</td><td><code>none</code></td>
 * <td>Password for <code>user</code> to access the database.</td></tr>
 * <tr><td>database_name</td><td>String</td><td>yes</td><td><code>none</code></td>
 * <td>Schema name we are connecting to on <code>database</code>.</td></tr>
 * <tr><td>base_query</td><td>String</td><td>yes</td><td><code>none</code></td>
 * <td>Query that will retrieve information from the database to load into EntityG.</td></tr>
 * <tr><td>base_column_name</td><td>String</td><td>yes</td><td><code>none</code></td>
 * <td>Name of the column that we want center nodes to consist of.</td></tr>
 * <tr><td>first_node_entry</td><td>String</td><td>yes</td><td><code>none</code></td>
 * <td>Initial node we want to display information for.</td></tr>
 * <tr><td>children_columns</td><td>Delimited list of Strings</td><td>yes</td><td><code>none</code></td>
 * <td>All column names of the information that will be returned from <code>base_query</code>. The information nodes
 * will consist of the data of these columns.</td></tr>
 * </table>
 *
 * <p/>
 * <h4>CSV source</h4>
 * <table border="1"> <tr><th>name</th><th>value type</th><th>required</th><th>default</th><th>description</th></tr>
 * <p/>
 * <tr><td>file</td><td>String</td><td>yes</td><td><code>none</code></td><td>Path to the CSV file.</td></tr>
 * </table>
 * @date May 19, 2011
 * @author Eric Kisner
 */
public class EntityGIniFile {

    /**
     * The default configuration file.
     */
    private static final String ENTITY_G_INI = "entityg.ini";
    /**
     * The delimiter between names and their values.
     */
    private static final String NAME_VALUE_DELIM = "=";
    /**
     * The configuration {@link File} object.
     */
    private File iniFile;
    /**
     * A mapping of name/value pairs.
     */
    private HashMap<String, String> iniPairs = null;

    /**
     * Default constructor. Uses the default <code>entityg.ini</code> file to configure EntityG.
     * @param emptyArgs Flag that says whether or not EntityG can be configured from command-line arguments. If
     *                  <code>emptyArgs</code> is false, then EntityG can look in the arguments for values. If
     *                  <code>emptyArgs</code> is true, then EntityG must rely on the <code>entityg.ini</code> file
     *                  for all options.
     */
    public EntityGIniFile( boolean emptyArgs ) throws InvalidIniException {
        this( ENTITY_G_INI, emptyArgs );
    }

    /**
     * Convenience constructor. Allows the caller to specify the .ini file instead of using the default.
     * @param iniFile The path to the .ini file.
     * @param emptyArgs Flag that says whether or not EntityG can be configured from command-line arguments. If
     *                  <code>emptyArgs</code> is false, then EntityG can look in the arguments for values. If
     *                  <code>emptyArgs</code> is true, then EntityG must rely on the <code>entityg.ini</code> file
     *                  for all options.
     * @throws InvalidIniException An exception can be thrown for the following reasons:
     * <ul>
     * <li>If entityg.ini file is empty, and the command-line arguments are empty;</li>
     * <li>If <code>iniFile</code> is not a valid file, or does not end with <code>.ini</code>;</li>
     * <li>If there was an error trying to read lines from <code>iniFile</code>;</li>
     * or
     * <li>If any line in <code>iniFile</code> is not of the format <code>name=value</code>.
     * </ul>
     */
    public EntityGIniFile( String iniFile, boolean emptyArgs ) throws InvalidIniException {
        this.iniFile = validatePath( iniFile );
        this.iniPairs = loadPairsFromFile( this.iniFile );

        if( iniPairs.isEmpty() && emptyArgs ) {
            throw new InvalidIniException( iniFile + " does not contain any options." );
        }
    }

    /**
     * Validates that <code>filePath</code> is a file, and that it is a .ini file.
     * @param filePath The path to the .ini file.
     * @return The {@link File} object of the pathname.
     */
    private File validatePath( String filePath ) throws InvalidIniException {
        try {
            File file = new File( filePath );
            if( !file.exists() ) {
                throw new InvalidIniException( filePath + " is not a valid file." );
            } else if( !file.getCanonicalPath().endsWith( ".ini" ) ) {
                throw new InvalidIniException( filePath + " is not a valid .ini file." );
            }
            return file;
        } catch( IOException ioe ) {
            throw new IllegalStateException( "There was a problem retrieving information about " + filePath );
        }
    }

    /**
     * Loads all data from a .ini file into a mapping of name/value pairs.
     * @param file The .ini {@link File}.
     * @return A mapping of name/value pairs as a result from <code>name=value</code> in the .ini file.
     */
    private HashMap<String, String> loadPairsFromFile( File file ) throws InvalidIniException {
        HashMap<String, String> ret = new HashMap<String, String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader( new FileReader( file ) );
            String line = "";
            while( (line = reader.readLine()) != null ) {
                validateLine( line );
                String[] split = line.split( "=" );
                ret.put( split[0], split[1] );
            }
        } catch( FileNotFoundException fnfe ) {
            //This should have already been checked, so we shouldn't have to worry about this.
        } catch( IOException ioe ) {
            //Try removing the lock on the file.
            if( reader != null ) {
                try {
                    reader.close();
                } catch( IOException ignore ) {
                    /**If we can't close the file, ignore this*/
                }
            }
            ExceptionUtils.handleException( ioe );
            throw new InvalidIniException( "There was an error trying to read lines from '" + file + "'." );
        }
        return ret;
    }

    /**
     * Validates a line in the <code>entityg.ini</code> file to have the following characteristics:
     * <br/>-It must contain a {@link EntityGIniFile#NAME_VALUE_DELIM} delimiter which separates the name
     * and value.
     * <br/>-It must only have one {@link EntityGIniFile#NAME_VALUE_DELIM} delimiter.
     * @param line A line from the <code>entityg.ini</code> file. This line should only contain a name/value pair
     *             with a {@link EntityGIniFile#NAME_VALUE_DELIM} delimiter in between.
     */
    private void validateLine( String line ) throws InvalidIniException {
        final String errorMessage = "'" + line + "' has an invalid format.";
        if( !line.contains( NAME_VALUE_DELIM ) )
            throw new InvalidIniException( errorMessage + " A .ini file requires " + NAME_VALUE_DELIM
                                           + " to separate names and values." );
        String[] split = line.split( NAME_VALUE_DELIM );
        if( split.length != 2 )
            throw new InvalidIniException( errorMessage + " A line in this .ini file should only consist of "
                                           + "name/value pairs." );
    }

    /**
     * Checks if the parsed .ini file includes a specific option.
     * @param optionName The name of the option we want to check this file for.
     * @return True if the .ini file passed to this method contained <code>optionName</code>, else false.
     */
    public boolean optionExists( String optionName ) {
        return iniPairs.containsKey( optionName );
    }

    /**
     * Returns the value of the option with <code>optionName</code>.
     * @param optionName The name of the option we want to retrieve a value for.
     * @return Value of the option if set, and has an argument, else null.
     */
    public String getOptionValue( String optionName ) {
        return iniPairs.get( optionName );
    }
}
