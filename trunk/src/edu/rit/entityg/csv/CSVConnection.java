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
 * @date Jun 4, 2011
 * @author Eric Kisner
 */
public class CSVConnection {

    public static final String DELIM = ",";
    private static CSVConnection instance;
    private static final Properties setup = new Properties();
    private BufferedReader reader;
    private File file;
    private String line;
    private List<String> columnNames;

    public static void setProperties( String filePath ) {
        if( filePath.isEmpty() || filePath == null || filePath.equalsIgnoreCase( "null" ) ) {
            throw new IllegalArgumentException( "The file path to set up a CSV connection is invalid." );
        } else {
            setup.setProperty( "filePath", filePath );
        }
    }

    public static CSVConnection instance() {
        if( instance == null ) {
            instance = new CSVConnection( setup );
        }
        return instance;
    }

    private CSVConnection( Properties props ) {
        if( props.containsKey( "filePath" ) ) {
            this.file = new File( props.getProperty( "filePath" ) );
        } else {
            throw new RuntimeException( "The path to the CSV file is invalid." );
        }

        validateFile( file );
        connect( file );
    }

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

    private void connect( File file ) {
        try {
            reader = new BufferedReader( new FileReader( file ) );
            columnNames = Arrays.asList( reader.readLine().split( DELIM ) );
        } catch( IOException ioe ) {
            ExceptionUtils.handleException( ioe );
        }
    }

    private boolean hasNext() throws IOException {
        line = reader.readLine();
        return !(line == null);
    }

    public void close() throws IOException {
        reader.close();
    }

    public TableRow getLine() throws IOException {
        if( hasNext() ) {
            return new ATableRow( line.split( DELIM ), columnNames );
        }
        return null;
    }
}
