package edu.rit.entityg;

import edu.rit.entityg.treeimpl.GenericTreeNode;

/**
 * {@link CSVEntityG} is a class that implements any methods that are required when loading data from a CSV file.
 * This includes: setting up the first node and its children, and setting any CSV fields - like information node
 * columns, and the center node column.
 * @date May 25, 2011
 * @author Eric Kisner
 */
public class CSVEntityG extends AbstractEntityG {

    public GenericTreeNode<String> setupAbsoluteParent() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void set_children_columns( String childrenColumnNames ) {
        throw new UnsupportedOperationException( "Invalid operation." );
    }

    public void set_host( String optionValue ) {
        throw new UnsupportedOperationException( "Invalid operation." );
    }

    public void set_port( String optionValue ) {
        throw new UnsupportedOperationException( "Invalid operation." );
    }

    public void set_user( String optionValue ) {
        throw new UnsupportedOperationException( "Invalid operation." );
    }

    public void set_password( String optionValue ) {
        throw new UnsupportedOperationException( "Invalid operation." );
    }

    public void set_database_name( String optionValue ) {
        throw new UnsupportedOperationException( "Invalid operation." );
    }

    public void set_base_query( String optionValue ) {
        throw new UnsupportedOperationException( "Invalid operation." );
    }

    public void set_base_column_name( String optionValue ) {
        throw new UnsupportedOperationException( "Invalid operation." );
    }

    public void set_first_node_entry( String optionValue ) {
        throw new UnsupportedOperationException( "Invalid operation." );
    }

    public void connectToDatabase() {
        throw new UnsupportedOperationException( "Invalid operation." );
    }
}
