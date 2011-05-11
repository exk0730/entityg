package edu.rit.entityg.utils;

import edu.rit.entityg.dataloaders.DataSourceType;
import edu.rit.entityg.prefuse.view.EntityG;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import static edu.rit.entityg.dataloaders.DataSourceType.*;

/**
 * Runs the EntityG program after validating command-line arguments and setting up an EntityG object.
 * @date May 10, 2011
 * @author Eric Kisner
 */
public class EntityGRunModule {

    private static final String DATASOURCE = "datasource";
    private static final String HOST = "host";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String SCHEMA_NAME = "schemaname";
    private static final String BASE_QUERY = "basequery";
    private static final String BASE_COLUMN = "basecolumn";
    private static final String NODE_DATA = "nodedata";
    private static final String CHILDREN_COLUMNS = "childrencolumns";
    private static final String DEFAULT_MAX_NODES = "maxnodes";
    private static final Options options = new Options();
    private EntityG entityG;
    public DataSourceType dsType = null;

    static {
        createOptions();
    }

    public EntityGRunModule() {
        this.entityG = new EntityG();
    }

    /**
     * Validate arguments passed into EntityG.
     * @param args The list of arguments passed into {@link EntityGRunModule#main(String[])}.
     * @return A {@link CommandLine} that after parsing the arguments.
     */
    public CommandLine validateArgs( String[] args ) {
        CommandLineParser parser = new GnuParser();
        CommandLine line = null;
        try {
            line = parser.parse( options, args );
            if( !line.hasOption( DATASOURCE ) ) usage();

            String dataSourceType = line.getOptionValue( DATASOURCE );
            if( dataSourceType.equalsIgnoreCase( "database" ) ) {
                checkRequiredOptions( line,
                                      HOST,
                                      USER,
                                      SCHEMA_NAME,
                                      BASE_QUERY,
                                      BASE_COLUMN,
                                      NODE_DATA,
                                      CHILDREN_COLUMNS );
                dsType = DATABASE;

            } else if( dataSourceType.equalsIgnoreCase( "xml" ) ) {
                dsType = XML;
                throw new UnsupportedOperationException( "Loading data from an XML file is currently not supported." );

            } else if( dataSourceType.equalsIgnoreCase( "csv" ) ) {
                dsType = CSV;
                throw new UnsupportedOperationException( "Loading data from a CSV file is currently not supported." );

            } else {
                usage();
            }
        } catch( ParseException pe ) {
            ExceptionUtils.handleException( pe );
            System.exit( -1 );
        }
        return line;
    }

    /**
     * Sets up the {@link EntityG} object that is going to be doing all of the display work.
     * @param line The {@link CommandLine} that parsed any options we want to pass into {@link EntityG}.
     * @param dsType The {@link DataSourceType} that we are loading data from.
     */
    public void setupEntityG( CommandLine line, DataSourceType dsType ) {
        switch( dsType ) {
            case DATABASE:
                entityG.setHost( line.getOptionValue( HOST ) );
                entityG.setUid( line.getOptionValue( USER ) );
                if( line.hasOption( PASSWORD ) ) {
                    entityG.setPassword( line.getOptionValue( PASSWORD ) );
                }
                entityG.setSchemaName( line.getOptionValue( SCHEMA_NAME ) );
                entityG.setBaseQuery( line.getOptionValue( BASE_QUERY ) );
                entityG.setBaseColumnName( line.getOptionValue( BASE_COLUMN ) );
                entityG.setFirstNodeData( line.getOptionValue( NODE_DATA ) );
                entityG.setChildrenColumnNames( line.getOptionValue( CHILDREN_COLUMNS ).split( "," ) );
                entityG.connectToDatabase();
                break;

            case XML:
                break;

            case CSV:
                break;

            default:
                break;
        }

        if( line.hasOption( DEFAULT_MAX_NODES ) ) {
            IllegalArgumentException e = new IllegalArgumentException( line.getOptionValue( DEFAULT_MAX_NODES )
                                                                       + " is not an integer greater than 1." );
            try {
                int i = Integer.parseInt( line.getOptionValue( DEFAULT_MAX_NODES ) );
                if( i < 1 ) {
                    throw e;
                }
                entityG.setDefaultMaxNodes( i );
            } catch( NumberFormatException nfe ) {
                throw e;
            }
        }
    }

    /**
     * Starts the visualization of {@link EntityG}.
     */
    public void startEntityG() {
        entityG.start();
    }

    /**
     * A private method to check if a parsed {@link CommandLine} contains required options.
     * @param line The {@link CommandLine} that parsed the list of arguments sent to the main method.
     * @param optNames The list of required arguments that should exist in <code>line</code>.
     */
    private void checkRequiredOptions( CommandLine line, String... optNames ) {
        for( String opt : optNames ) {
            if( !line.hasOption( opt ) ) {
                usage();
            }
        }
    }

    /**
     * Uses the Commons CLI {@link HelpFormatter} to print a usage statement.
     */
    private static void usage() {
        HelpFormatter help = new HelpFormatter();
        help.printHelp( "EntityG.java", options );
        System.exit( 0 );
    }

    /**
     * Create the {@link Option}s that can exist as arguments to EntityG.
     */
    // <editor-fold defaultstate="collapsed" desc="Create argument options">
    @SuppressWarnings( "static-access" )
    private static void createOptions() {
        Option dataSourceOption = OptionBuilder.withArgName( "type" ).
                hasArg().
                withDescription( "Type of data source to load from. Choose one: [database | xml | csv]" ).
                create( DATASOURCE );

        Option hostOption = OptionBuilder.withArgName( HOST ).
                hasArg().
                withDescription( "Host URL for database if <type> = database." ).
                create( HOST );

        Option usernameOption = OptionBuilder.withArgName( USER ).
                hasArg().
                withDescription( "Username for database if <type> = database." ).
                create( USER );

        Option passwordOption = OptionBuilder.withArgName( PASSWORD ).
                hasArg().
                withDescription( "Password for database if <type> = database." ).
                create( PASSWORD );

        Option databaseNameOption = OptionBuilder.withArgName( SCHEMA_NAME ).
                hasArg().
                withDescription( "Schema name for database if <type> = database." ).
                create( SCHEMA_NAME );

        Option baseQueryOption = OptionBuilder.withArgName( "query" ).
                hasArg().
                withDescription( "Base query to use for loading data from a database into EntityG." ).
                create( BASE_QUERY );

        Option baseColumnOption = OptionBuilder.withArgName( "column" ).
                hasArg().
                withDescription( "Column in the database that represents the starting node of EntityG." ).
                create( BASE_COLUMN );

        Option firstNodeDataOption = OptionBuilder.withArgName( "data" ).
                hasArg().
                withDescription( "Data that the starting node of EntityG should contain." ).
                create( NODE_DATA );

        Option childrenOption = OptionBuilder.withArgName( "children" ).
                hasArg().
                withDescription( "Column names in the database that represent information about the starting node"
                                 + " of EntityG. Column names should be separated by commas." ).
                create( CHILDREN_COLUMNS );

        Option maxNodesOption = OptionBuilder.withArgName( "number" ).
                hasArg().
                withDescription( "Max number of children nodes that should be display for a given parent." ).
                create( DEFAULT_MAX_NODES );

        options.addOption( dataSourceOption );
        options.addOption( hostOption );
        options.addOption( usernameOption );
        options.addOption( passwordOption );
        options.addOption( databaseNameOption );
        options.addOption( baseQueryOption );
        options.addOption( baseColumnOption );
        options.addOption( firstNodeDataOption );
        options.addOption( childrenOption );
        options.addOption( maxNodesOption );
    } //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Main">
    public static void main( String[] args ) {
        EntityGRunModule main = new EntityGRunModule();
        CommandLine line = main.validateArgs( args );
        main.setupEntityG( line, main.dsType );
        main.startEntityG();
    }//</editor-fold>
}
