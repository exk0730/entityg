package edu.rit.entityg.exceptions;

/**
 * Exception subclass that is thrown when the entityg configuration file has invalid formatting, or specifies invalid
 * options.
 * @author Eric Kisner
 */
public class InvalidIniException extends Exception {

    /**
     * Creates a new instance of <code>InvalidIniException</code> without detail message.
     */
    public InvalidIniException() {
    }


    /**
     * Constructs an instance of <code>InvalidIniException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InvalidIniException(String msg) {
        super(msg);
    }
}
