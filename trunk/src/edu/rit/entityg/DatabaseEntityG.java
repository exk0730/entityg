package edu.rit.entityg;

import edu.rit.entityg.database.DatabaseConnection;
import edu.rit.entityg.dataloaders.DatabaseLoader;
import edu.rit.entityg.exceptions.BadSetupException;
import edu.rit.entityg.treeimpl.GenericTreeNode;
import prefuse.Visualization;

/**
 * {@link DatabaseEntityG} is a class that implements any methods that are required when loading data from a database.
 * This includes: setting up the first node and its children, setting any database fields - such as columns or queries,
 * and actually making a connection to a database.
 * @date May 20, 2011
 * @author Eric Kisner
 */
public class DatabaseEntityG extends AbstractEntityG {

    private String host;
    private String port;
    private String uid;
    private String password = "";
    private String databaseName;
    private String baseQuery;
    private String baseColumnName;
    private String firstNodeData;
    private String[] childrenColumnNames;

    /**
     * Default constructor. Simply calls {@link AbstractEntityG#AbstractEntityG()} to setup the {@link Visualization}.
     */
    public DatabaseEntityG() {
        super();
    }

    public void set_host( String host ) {
        this.host = host;
    }

    public void set_port( String port ) {
        this.port = port;
    }

    public void set_user( String uid ) {
        this.uid = uid;
    }

    public void set_password( String password ) {
        this.password = password;
    }

    public void set_database_name( String schemaName ) {
        this.databaseName = schemaName;
    }

    public void set_base_query( String baseQuery ) {
        this.baseQuery = baseQuery;
    }

    public void set_base_column_name( String baseColumnName ) {
        this.baseColumnName = baseColumnName;
    }

    public void set_first_node_entry( String firstNodeData ) {
        this.firstNodeData = firstNodeData;
    }

    public void set_children_columns( String childrenColumnNames ) {
        this.childrenColumnNames = childrenColumnNames.split( "," );
    }

    public void connectToDatabase() {
        DatabaseConnection.setProperties( host, port, databaseName, uid, password );
        loader = new DatabaseLoader( DatabaseConnection.instance() );
    }

    public GenericTreeNode<String> setupAbsoluteParent() {
        loader.setBaseQuery( baseQuery );
        loader.setCenterNodeColumnName( baseColumnName );
        loader.setInformationNodeColumNames( childrenColumnNames );
        try {
            GenericTreeNode<String> ret = loader.loadAbsoluteParent( firstNodeData );
            return ret;
        } catch( BadSetupException bse ) {
            throw new IllegalArgumentException( bse );
        }
    }
}
