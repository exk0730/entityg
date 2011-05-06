package edu.rit.entityg.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * ExceptionUtils is a utilities class that provides various static methods to deal with Throwables (such as
 * printing them out, or retrieving the stack trace).
 * @author Eric Kisner
 */
public class ExceptionUtils {

    /**
     * Returns the Exception's stack trace as a String.
     * @param t Throwable to retrieve its stack trace.
     * @return The stacktrace as a String.
     */
    private static String stackTraceAsString( Throwable t ) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream( baos );
        t.printStackTrace( ps );
        return baos.toString();
    }

    /**
     * Simple method to log an exception.
     * @param t The exception that was thrown.
     */
    public static void handleException( Throwable t ) {
        handleException( t, null );
    }

    /**
     * Generic method used to handle a message sent to the user.
     * @param parent The Frame the user is currently at.
     * @param t The exception that was thrown.
     * @param message The message we want to display to the user.
     * @param c The class where this occurred.
     */
    public static void handleException( JFrame parent, Throwable t, String message, Class c ) {
        handleException( t, c );
        JOptionPane.showMessageDialog( parent, message );
    }

    /**
     * Generic method used to handle an exception that the user should not see. This method will simply log
     * the error.
     * @param t The exception that was thrown.
     * @param c The class where this occurred.
     */
    public static void handleException( Throwable t, Class c ) {
        String toLog = "\nException: " + t.getMessage();
        toLog += "\n***Stack Trace***\n";
        toLog += stackTraceAsString( t );
        if( c == null ) {
            Logging.log( toLog );
        } else {
            Logging.log( toLog, c );
        }
    }
}
