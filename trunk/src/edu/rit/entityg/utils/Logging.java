package edu.rit.entityg.utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * The Logging class will print all error messages to a log file.
 */
public class Logging {

    private static final Logger logger;

    static {
        logger = Logger.getLogger( "EntityG.Logger" );
        try {
            final File logFile = new File( "entityg.log" );
            if( !logFile.exists() ) {
                logFile.createNewFile();
            }
            FileHandler fh = new FileHandler( "entityg.log", true ); //set filehandler with append true
            fh.setFormatter( new Formatter() {

                @Override
                public String format( LogRecord record ) {
                    StringBuilder buf = new StringBuilder();
                    buf.append( "\n" );
                    buf.append( new Date() );
                    buf.append( " " );
                    buf.append( formatMessage( record ) );
                    buf.append( "\n" );
                    return buf.toString();
                }
            } );
            logger.addHandler( fh );
        } catch( IOException ignore ) {
            System.err.println( "There was a problem creating or writing to the log file."
                                + " No errors will be logged for the duration of the program." );
        }
    }

    private Logging() { }

    /**
     * Logs a message to the log file.
     * @param message The message to write to the file.
     * @param c The class from where the message came from.
     */
    public static void log( String message, Class c ) {
        if( c == null ) {
            log( message );
        } else {
            String toLog = "[" + c.getCanonicalName() + "]\t" + message + "\n";
            logger.warning( toLog );
        }
    }

    /**
     * Logs a message to the log file without the classname.
     * @param message
     */
    public static void log( String message ) {
        logger.warning( message );
    }

    /**
     * Closes all file handlers for this logger.
     */
    public static void close() {
        for( Handler handler : logger.getHandlers() ) {
            handler.close();
        }
    }
}
