package edu.rit.entityg;

import edu.rit.entityg.treeimpl.GenericTreeNode;
import java.awt.event.MouseEvent;
import prefuse.visual.VisualItem;

/**
 * {@link CSVEntityG} is a class that implements any methods that are required when loading data from a CSV file.
 * This includes: setting up the first node and its children, and setting any CSV fields - like information node
 * columns, and the center node column.
 * @date May 25, 2011
 * @author Eric Kisner
 */
public class CSVEntityG extends AbstractEntityG {

    private String fileName;

    public CSVEntityG() {
        super();
    }

    public GenericTreeNode<String> setupAbsoluteParent() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void customItemClicked( VisualItem item, MouseEvent e ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void connectToDataSource() {

    }

    public void set_file_name( String fileName ) {
        this.fileName = fileName;
    }
}
