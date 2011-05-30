package edu.rit.entityg.configure;

import edu.rit.entityg.AbstractEntityG;
import edu.rit.entityg.DatabaseEntityG;
import edu.rit.entityg.dataloaders.DataSourceType;
import edu.rit.entityg.exceptions.InvalidIniException;
import edu.rit.entityg.utils.ExceptionUtils;
import java.lang.reflect.Method;

import static edu.rit.entityg.configure.EntityGOptions.*;

/**
 * This class is used for configuring an {@link AbstractEntityG} based on command-line arguments and/or a configuration
 * file.
 *
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
 * <tr><td>config_file</td><td>String</td><td>no</td><td><code>entityg.ini</code></td>
 * <td>Path to the configuration (.ini) file.</td></tr>
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
 * <td>Initial node data we want to display information for.</td></tr>
 * <tr><td>children_columns</td><td>Delimited list of Strings</td><td>yes</td><td><code>none</code></td>
 * <td>All column names of the information that will be returned from <code>base_query</code>. The information nodes
 * will consist of the data of these columns. This option must be specified as a String with each column delimited
 * by a comma (<code>','</code>).</td></tr>
 * </table>
 *
 * <p/>
 * <h4>CSV source</h4>
 * <table border="1"> <tr><th>name</th><th>value type</th><th>required</th><th>default</th><th>description</th></tr>
 * <p/>
 * <tr><td>file</td><td>String</td><td>yes</td><td><code>none</code></td><td>Path to the CSV file.</td></tr>
 * </table>
 * @see EntityGCommandLine
 * @see EntityGIniFile
 * @date May 24, 2011
 * @author Eric Kisner
 */
public class EntityGConfiguration {

    private EntityGIniFile entitygINI;
    private EntityGCommandLine entitygCL;
    private AbstractEntityG entityG;
    private boolean emptyArgs;

    /**
     * Default constructor. Should be used when we want to run EntityG within an application, and/or with the default
     * configuration file.
     * @throws InvalidIniException An exception can be thrown for the following reasons:
     * <ul>
     * <li>If entityg.ini file is empty, and the command-line arguments are empty;</li>
     * <li>If <code>iniFile</code> is not a valid file, or does not end with <code>.ini</code>;</li>
     * <li>If there was an error trying to read lines from <code>iniFile</code>;</li>
     * or
     * <li>If any line in <code>iniFile</code> is not of the format <code>name=value</code>.
     * </ul>
     */
    public EntityGConfiguration() throws InvalidIniException {
        configAll( new String[]{}, null );
    }

    /**
     * Standalone EntityG constructor. Should be used when we have arguments to pass into EntityG. This constructor
     * will also check to see if a non-default configuration file has been specified within the arguments.
     * @param args The command-line arguments passed into a main method.
     * @throws InvalidIniException An exception can be thrown for the following reasons:
     * <ul>
     * <li>If entityg.ini file is empty, and the command-line arguments are empty;</li>
     * <li>If <code>iniFile</code> is not a valid file, or does not end with <code>.ini</code>;</li>
     * <li>If there was an error trying to read lines from <code>iniFile</code>;</li>
     * or
     * <li>If any line in <code>iniFile</code> is not of the format <code>name=value</code>.
     * </ul>
     */
    public EntityGConfiguration( String[] args ) throws InvalidIniException {
        configAll( args, null );
    }

    /**
     * Constructor that should be used when we are specifying a different configuration file. This constructor relies
     * on the configuration file to set up EntityG.
     * @param iniFile The <code>entityg.ini</code> file's location.
     * @throws InvalidIniException An exception can be thrown for the following reasons:
     * <ul>
     * <li>If entityg.ini file is empty, and the command-line arguments are empty;</li>
     * <li>If <code>iniFile</code> is not a valid file, or does not end with <code>.ini</code>;</li>
     * <li>If there was an error trying to read lines from <code>iniFile</code>;</li>
     * or
     * <li>If any line in <code>iniFile</code> is not of the format <code>name=value</code>.
     * </ul>
     */
    public EntityGConfiguration( String iniFile ) throws InvalidIniException {
        configAll( new String[]{}, iniFile );
    }

    /**
     * Calling this method will start an {@link AbstractEntityG} graph.
     */
    public void startEntityG() {
        entityG.start();
    }

    /**
     * Sets up any required configuration objects based on the command-line arguments or a configuration file.
     * @param args The command-line arguments passed into a main method.
     * @param filePath The path to the configuration file.
     * @throws InvalidIniException An exception can be thrown for the following reasons:
     * <ul>
     * <li>If entityg.ini file is empty, and the command-line arguments are empty;</li>
     * <li>If <code>iniFile</code> is not a valid file, or does not end with <code>.ini</code>;</li>
     * <li>If there was an error trying to read lines from <code>iniFile</code>;</li>
     * or
     * <li>If any line in <code>iniFile</code> is not of the format <code>name=value</code>.
     * </ul>
     */
    private void configAll( String[] args, String filePath ) throws InvalidIniException {
        emptyArgs = !(args.length > 0);

        if( !emptyArgs ) {
            entitygCL = new EntityGCommandLine( args );
            if( entitygCL.optionExists( CONFIG_FILE ) ) {
                entitygINI = new EntityGIniFile( entitygCL.getOptionValue( CONFIG_FILE ), emptyArgs );
            }
        }

        //If an option for a config file wasn't set in the command line arguments, we still need to create an object
        //to handle any configuration file options.
        if( entitygINI == null ) {
            if( filePath != null ) {
                entitygINI = new EntityGIniFile( filePath, emptyArgs );
            } else {
                entitygINI = new EntityGIniFile( emptyArgs );
            }
        }

        String dst = getValue( DATASOURCE_TYPE );

        if( dst.equalsIgnoreCase( "database" ) ) {
            entityG = new DatabaseEntityG();
            runMethodsForDataSource( DataSourceType.DATABASE );
            entityG.connectToDatabase();
        } else if( dst.equalsIgnoreCase( "csv" ) ) {
            throw new UnsupportedOperationException( "CSV is not implemented yet." );
        } else if( dst.equalsIgnoreCase( "xml" ) ) {
            throw new UnsupportedOperationException( "XML is not implemented yet." );
        } else {
            throw new IllegalStateException( "'" + dst + "' is not a supported data source type." );
        }
    }

    /**
     * Runs any setter methods based on the configuration objects. This method will first retrieve any options that can
     * be set based on <code>dst</code>, then run any setter methods based on the names of those options using
     * reflection.
     * @param dst The {@link DataSourceType} that we are setting fields for.
     */
    private void runMethodsForDataSource( DataSourceType dst ) {
        try {
            for( String option : getOptions( dst ) ) {
                Method m = getMethodFromOption( option );
                m.invoke( entityG, getValue( option ) );
            }
        } catch( IllegalArgumentException iae ) {
            ExceptionUtils.handleException( iae );
        } catch( Exception e ) {
            ExceptionUtils.handleException( e );
        }

        String defMaxNodeStr = getValue( DEFAULT_MAX_NODES );
        if( defMaxNodeStr != null ) {
            try {
                entityG.set_default_max_nodes( Integer.parseInt( defMaxNodeStr ) );
            } catch( Exception e ) {
                ExceptionUtils.handleMessage( "The option for " + DEFAULT_MAX_NODES + " was not an integer value. "
                                              + "Using the default value." );
            }
        }

        /**
         * We need to determine USE_TOOL_TIP's option because the command line only requires that the flag is set, while
         * the .ini file requires that the option is set to "true".
         */
        if( !emptyArgs ) {
            if( entitygCL.optionExists( USE_TOOL_TIP ) ) {
                entityG.set_use_tool_tip( true );
            }
        } else {
            if( entitygINI.optionExists( USE_TOOL_TIP ) ) {
                entityG.set_use_tool_tip( Boolean.parseBoolean( entitygINI.getOptionValue( USE_TOOL_TIP ) ) );
            }
        }
    }

    /**
     * Retrieves a {@link Method} from <code>optionName</code>. These methods should be setters that start with
     * <code>set_</code> and then end with <code>optionName</code>. The methods will be retrieved from the class
     * {@link AbstractEntityG}.
     * @param optionName The name of the option we are running a setter method for.
     * @return The {@link Method} whose name is equal to <code>set_[optionName]</code> or null, if <code>optionName
     *         </code> could not be found.
     */
    private Method getMethodFromOption( String optionName ) {
        final String prefix = "set_";
        for( Method m : AbstractEntityG.class.getMethods() ) {
            if( (prefix + optionName).equalsIgnoreCase( m.getName() ) ) {
                return m;
            }
        }
        return null;
    }

    /**
     * Returns the value of an option which has been specified in either the command-line arguments, or within the
     * configuration file.
     * @param optionName The option that we want to get a value for.
     * @return The value of <code>optionName</code> if it was found in the command-line arguments or configuration file,
     *         or an empty string if none could be found.
     */
    private String getValue( String optionName ) {
        /**
         * In order to properly retrieve a value from the command-line arguments or configuration file, we need to first
         * check that the arguments are not empty. If the args aren't empty, and they contain <code>optionName</code>,
         * then return that value, else, try looking in the config file for the option. If the args are empty, only
         * look in the config file for the value of <code>optionName</code>.
         */
        if( !emptyArgs ) {
            if( entitygCL.optionExists( optionName ) ) {
                return entitygCL.getOptionValue( optionName );
            }
        }

        if( entitygINI.optionExists( optionName ) ) {
            return entitygINI.getOptionValue( optionName );
        }

        return "";
    }
}
