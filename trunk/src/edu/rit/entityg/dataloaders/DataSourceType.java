package edu.rit.entityg.dataloaders;

import edu.rit.entityg.prefuse.view.EntityG;

/**
 * Enum that tells {@link EntityG} what data source we are loading from.
 * @author Eric Kisner
 */
public enum DataSourceType {

    /**
     * Load data from a database.
     */
    DATABASE,
    /**
     * Load data from a XML file.
     */
    XML,
    /**
     * Load data from a CSV file.
     */
    CSV
}
