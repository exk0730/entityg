package edu.rit.entityg.csv;

import edu.rit.entityg.utils.ExceptionUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * CSV connection. This class provides methods to read data from a CSV file.
 * @date Jun 4, 2011
 * @author Eric Kisner
 */
public class CSVConnection {

    /**
     * A <code>comma</code> which is the delimiter for CSV files.
     */
    public static final String DELIM = ",";
    private static final Properties setup = new Properties();
    /**
     * The {@link CSVConnection} instance object for our Singleton design pattern.
     */
    private static CSVConnection instance;
    /**
     * The {@link BufferedReader} object we are using to read lines from <code>file</code>.
     */
    private BufferedReader reader;
    /**
     * The actual CSV file.
     */
    private File file;
    /**
     * The current line that <code>reader</code> is on.
     */
    private String line;
    /**
     * The list of column names that are contained in <code>file</code>.
     */
    private List<String> columnNames;

    /**
     * Set the properties used to connect to a CSV file.
     * @param filePath The path to the CSV file.
     */
    public static void setProperties( String filePath ) {
        if( filePath.isEmpty() || filePath == null || filePath.equalsIgnoreCase( "null" ) ) {
            throw new IllegalArgumentException( "The file path to set up a CSV connection is invalid." );
        } else {
            setup.setProperty( "filePath", filePath );
        }
    }

    /**
     * Singleton design pattern. Call this method to retrieve an instance of a {@link CSVConnection} object.
     * @return The single {@link CSVConnection} instance that is created.
     */
    public static CSVConnection instance() {
        if( instance == null ) {
            instance = new CSVConnection( setup );
        }
        return instance;
    }

    /**
     * Private constructor. Use {@link CSVConnection#instance()} instead.
     * @param props The {@link Properties} that will set up the CSV connection. In this case, <code>props</code> only
     *              need contain a key <code>filePath</code>, which tells {@link CSVConnection} where the CSV file is.
     */
    private CSVConnection( Properties props ) {
        if( props.containsKey( "filePath" ) ) {
            this.file = new File( props.getProperty( "filePath" ) );
        } else {
            throw new RuntimeException( "The path to the CSV file is invalid." );
        }

        validateFile( file );
        connect( file );
    }

    /**
     * Validates that <code>file</code> is: 1) a file that exists; 2) a CSV file; and 3) can be read.
     * @param file The {@link File} object that was created from a file path.
     */
    private void validateFile( File file ) {
        try {
            file.canRead();
            if( !file.exists() || !file.isFile() || !file.getCanonicalPath().endsWith( ".csv" ) ) {
                throw new RuntimeException( file + " is not a valid file." );
            }
        } catch( SecurityException se ) {
            throw new RuntimeException( se );
        } catch( IOException ioe ) {
            throw new RuntimeException( ioe );
        }
    }

    /**
     * Instantiates a {@link BufferedReader} object for <code>file</code>. Also reads the first line of the CSV file,
     * in order to retrieve the column names.
     * <p/><b>Note:</b> it is assumed that the first line IS column names. In the future, we may want to allow a user
     * to specify an option for a file that does not have its first line as column names.
     * @param file The {@link File} object that we are "connecting" to.
     */
    private void connect( File file ) {
        try {
            reader = new BufferedReader( new FileReader( file ) );
            columnNames = Arrays.asList( reader.readLine().split( DELIM ) );
        } catch( IOException ioe ) {
            ExceptionUtils.handleException( ioe );
        }
    }

    /**
     * Tries to read the next line in the file.
     * @return True if there is a line, else false.
     */
    private boolean hasNext() throws IOException {
        line = reader.readLine();
        return !(line == null);
    }

    /**
     * Resets our connection to the CSV file by resetting the {@link BufferedReader}, and re-reading the first line.
     */
    public void reset() throws IOException {
        reader = new BufferedReader( new FileReader( file ) );
        columnNames = Arrays.asList( reader.readLine().split( DELIM ) );
    }

    /**
     * Closes the {@link BufferedReader} of this connection.
     */
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Get the line that the {@link BufferedReader} is currently on as an implementation of {@link TableRow}.
     * @return A {@link TableRow} object after splitting the current line in the file, else null if there were no lines
     *         left in the file to retrieve.
     */
    public TableRow getLine() throws IOException {
        if( hasNext() ) {
            return new ATableRow( line.split( DELIM ), columnNames );
        }
        return null;
    }
}
