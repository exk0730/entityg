package edu.rit.entityg.dataloaders;

import edu.rit.entityg.database.DatabaseConnection;

/**
 * Exposes methods to load data from a database and format data from that database into a prefuse-readable format.
 * This means that there are a few things that need to be known prior to loading any data into EntityG:
 *
 * @date May 6, 2011
 * @author Eric Kisner
 */
public class DatabaseLoader {
    
    private DatabaseConnection conn;

    /**
     * Default constructor.
     * @param conn A connection to the database of our choice.
     */
    public DatabaseLoader( DatabaseConnection conn ) {
        this.conn = conn;
    }
}
