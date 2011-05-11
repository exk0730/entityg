package edu.rit.entityg.exceptions;

/**
 *
 * @author Eric Kisner
 */
public class BadSetupException extends Exception {

    /**
     * Creates a new instance of <code>BadSetupException</code> without detail message.
     */
    public BadSetupException() {
        super( "Data loading of EntityG failed. Make sure your specifications are correctly formatted." );
    }

    /**
     * Constructs an instance of <code>BadSetupException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public BadSetupException( String msg ) {
        super( msg );
    }
}
