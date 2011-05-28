package edu.rit.entityg.configure;

import edu.rit.entityg.utils.ExceptionUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import static edu.rit.entityg.configure.EntityGOptions.*;

/**
 * Runs the EntityG program after validating command-line arguments. This class should be used only when running
 * EntityG as a standalone application. In the case that EntityG will be run as a standalone application, any
 * options set with this class will override any options set in {@link EntityGIniFile}.
 * <br/>
 * <br/>
 * <b>Note:</b> See {@link EntityGIniFile} for a list of possible options that can be specified
 * in the command line.
 * @date May 10, 2011
 * @author Eric Kisner
 */
public class EntityGCommandLine {

    /**
     * The {@link Options} object that represents what options can be specified in the command line.
     */
    private static final Options options = new Options();
    /**
     * The {@link CommandLineParser} that will parse arguments sent to a main method.
     */
    private static final CommandLineParser parser = new GnuParser();
    private CommandLine line;

    static {
        createOptions();
    }

    /**
     * Default constructor. Requires command line arguments, else we aren't parsing anything.
     * @param args The command-line arguments passed into the main method.
     */
    public EntityGCommandLine( String[] args ) {
        this.line = parseArgs( args );
    }

    /**
     * Parses a list of arguments sent to the main method.
     * @param args The arguments passed to a main method.
     * @return The {@link CommandLine} object that results from parsing <code>args</code>.
     */
    private CommandLine parseArgs( String[] args ) {
        CommandLine cl = null;
        try {
            cl = parser.parse( options, args );
        } catch( ParseException pe ) {
            ExceptionUtils.handleException( pe );
            usage();
            //Since we are specifically loading EntityG as a standalone program (with arguments from the command line),
            //we can just exit gracefully here. There's nothing we can do if we can't correctly parse the command line
            //arguments, we have to rely on the user to fix their mistakes.
            System.exit( -1 );
        }
        return cl;
    }

    /**
     * Checks if the parsed command line arguments includes a specific option.
     * @param optionName The name of the option we want to check this {@link CommandLine} for.
     * @return True if the arguments passed to this method contained <code>optionName</code>, else false.
     */
    public boolean optionExists( String optionName ) {
        return line.hasOption( optionName );
    }

    /**
     * Returns the value of the option with name <code>optionName</code>.
     * @param optionName The name of the option we want to retrieve a value for.
     * @return Value of the option if set, and has an argument, else null.
     */
    public String getOptionValue( String optionName ) {
        return line.getOptionValue( optionName );
    }

    /**
     * Uses the Commons CLI {@link HelpFormatter} to print a usage statement.
     */
    public static void usage() {
        HelpFormatter help = new HelpFormatter();
        help.printHelp( "EntityG.java", options );
        System.exit( 0 );
    }

    /**
     * Create the {@link Option}s that can exist as arguments to EntityG.
     */
    @SuppressWarnings( "static-access" )
    private static void createOptions() {
        Option maxNodesOption = OptionBuilder.withArgName( "max nodes" ).
                hasArg().
                withDescription( "Max number of children nodes that should be display for a given parent." ).
                create( DEFAULT_MAX_NODES );

        Option useToolTipOption = new Option( USE_TOOL_TIP,
                                              "Use a Tool Tip to display what each node's data represents." );

        Option useConfigFileOption = OptionBuilder.withArgName( "filepath" ).
                hasArg().
                withDescription( "The file path to the configuration file. The file should be a .ini file." ).
                create( CONFIG_FILE );

        Option dataSourceOption = OptionBuilder.withArgName( "type" ).
                hasArg().
                withDescription( "Type of data source to load from. Choose one: [database | xml | csv]" ).
                create( DATASOURCE_TYPE );

        Option hostOption = OptionBuilder.withArgName( HOST ).
                hasArg().
                withDescription( "Host URL for database if <type> = database." ).
                create( HOST );

        Option portOption = OptionBuilder.withArgName( PORT ).
                hasArg().
                withDescription( "Port number for the database if <type> = database." ).
                create( PORT );

        Option usernameOption = OptionBuilder.withArgName( USER ).
                hasArg().
                withDescription( "Username for database if <type> = database." ).
                create( USER );

        Option passwordOption = OptionBuilder.withArgName( PASSWORD ).
                hasArg().
                withDescription( "Password for database if <type> = database." ).
                create( PASSWORD );

        Option databaseNameOption = OptionBuilder.withArgName( DATABASE_NAME ).
                hasArg().
                withDescription( "Schema name for database if <type> = database." ).
                create( DATABASE_NAME );

        Option baseQueryOption = OptionBuilder.withArgName( "query" ).
                hasArg().
                withDescription( "Base query to use for loading data from a database into EntityG." ).
                create( BASE_QUERY );

        Option baseColumnOption = OptionBuilder.withArgName( "column" ).
                hasArg().
                withDescription( "Column in the database that represents the starting node of EntityG." ).
                create( BASE_COLUMN_NAME );

        Option firstNodeDataOption = OptionBuilder.withArgName( "data" ).
                hasArg().
                withDescription( "Data that the starting node of EntityG should contain." ).
                create( FIRST_NODE_ENTRY );

        Option childrenOption = OptionBuilder.withArgName( "children" ).
                hasArg().
                withDescription( "Column names in the database that represent information about the starting node"
                                 + " of EntityG. Column names should be separated by commas." ).
                create( CHILDREN_COLUMNS );

        options.addOption( useConfigFileOption );
        options.addOption( maxNodesOption );
        options.addOption( useToolTipOption );
        options.addOption( dataSourceOption );
        options.addOption( hostOption );
        options.addOption( portOption );
        options.addOption( usernameOption );
        options.addOption( passwordOption );
        options.addOption( databaseNameOption );
        options.addOption( baseQueryOption );
        options.addOption( baseColumnOption );
        options.addOption( firstNodeDataOption );
        options.addOption( childrenOption );
    }
}
