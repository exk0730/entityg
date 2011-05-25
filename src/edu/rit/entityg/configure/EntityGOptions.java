package edu.rit.entityg.configure;

import edu.rit.entityg.DatabaseEntityG;

/**
 * Static variables accessible for any EntityG configuration class.
 * @date May 19, 2011
 * @author Eric Kisner
 */
public class EntityGOptions {

    /**
     * ----------------------------------------------------------------------------------------------------------
     * Database configuration static labels.
     * ----------------------------------------------------------------------------------------------------------
     */
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String DATABASE_NAME = "database_name";
    public static final String BASE_QUERY = "base_query";
    public static final String BASE_COLUMN_NAME = "base_column_name";
    public static final String FIRST_NODE_ENTRY = "first_node_entry";
    public static final String CHILDREN_COLUMNS = "children_columns";
    /**
     * ----------------------------------------------------------------------------------------------------------
     * EntityG general configuration labels.
     * ----------------------------------------------------------------------------------------------------------
     */
    public static final String DEFAULT_MAX_NODES = "default_max_nodes";
    public static final String USE_TOOL_TIP = "use_tool_tip";
    public static final String CENTER_NODE_LABEL = "center_node_label";
    public static final String DATASOURCE_TYPE = "datasource_type";
    public static final String CONFIG_FILE = "config_file";

    /**
     * Returns an array of options that can be used in {@link DatabaseEntityG}.
     * @return Array of options that can be set for a database.
     */
    public static String[] getDatabaseOptions() {
        return new String[]{ HOST,
                             PORT,
                             USER,
                             PASSWORD,
                             DATABASE_NAME,
                             BASE_QUERY,
                             BASE_COLUMN_NAME,
                             FIRST_NODE_ENTRY,
                             CHILDREN_COLUMNS
                };
    }
}
