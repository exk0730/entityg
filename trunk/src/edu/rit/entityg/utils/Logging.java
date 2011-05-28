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
 * The Logging class will print all error messages to a log file. The methods in this class should only be called from
 * {@link ExceptionUtils}, therefore, any validating should be done in that class.
 */
public class Logging {

    private static final Logger logger;
    public static final String LINE_SEP = System.getProperty( "line.separator" );

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
                    return LINE_SEP + new Date() + "   "
                           + "[" + record.getLevel() + "] : "
                           + record.getMessage() + LINE_SEP;
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
        String toLog = "[" + c.getCanonicalName() + "]\t" + message + LINE_SEP;
        logger.info( toLog );
        flush();
    }

    /**
     * Logs a message to the log file without the classname.
     */
    public static void log( String message ) {
        logger.info( message );
        flush();
    }

    /**
     * Closes all file handlers for this logger.
     */
    public static void close() {
        for( Handler handler : logger.getHandlers() ) {
            handler.close();
        }
    }

    /**
     * Flushes all file handlers for this logger.
     */
    private static void flush() {
        for( Handler handler : logger.getHandlers() ) {
            handler.flush();
        }
    }
}
