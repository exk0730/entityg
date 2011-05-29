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
     * @throws InvalidIniException
     */
    public EntityGConfiguration() throws InvalidIniException {
        configAll( new String[]{}, null );
    }

    /**
     * Standalone EntityG constructor. Should be used when we have arguments to pass into EntityG. This constructor
     * will also check to see if a non-default configuration file has been specified within the arguments.
     * @param args The command-line arguments passed into a main method.
     * @throws InvalidIniException
     */
    public EntityGConfiguration( String[] args ) throws InvalidIniException {
        configAll( args, null );
    }

    /**
     * Constructor that should be used when we are specifying a different configuration file. This constructor relies
     * on the configuration file to set up EntityG.
     * @param iniFile The <code>entityg.ini</code> file's location.
     * @throws InvalidIniException
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
        if( !defMaxNodeStr.isEmpty() ) {
            entityG.set_default_max_nodes( Integer.parseInt( defMaxNodeStr ) );
        }

        String useToolTipStr = getValue( USE_TOOL_TIP );
        if( useToolTipStr != null ) {
            entityG.set_use_tool_tip( Boolean.parseBoolean( useToolTipStr ) );
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
            } else if( entitygINI.optionExists( optionName ) ) {
                return entitygINI.getOptionValue( optionName );
            }
        } else {
            if( entitygINI.optionExists( optionName ) ) {
                return entitygINI.getOptionValue( optionName );
            }
        }
        return "";
    }
}
