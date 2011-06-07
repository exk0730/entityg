package edu.rit.entityg;

import edu.rit.entityg.dataloaders.XMLLoader;
import edu.rit.entityg.treeimpl.GenericTreeNode;
import java.awt.event.MouseEvent;
import prefuse.visual.VisualItem;

/**
 * {@link XMLEntityG} is a class that implements any methods that are required when loading data from a XML file.
 * This includes: setting up the first node and its children, and setting any XML fields - like information node tags,
 * and the center node tag.
 * @date May 25, 2011
 * @author Eric Kisner
 */
public class XMLEntityG extends AbstractEntityG {
    /**
     * The data loader for EntityG.
     */
    private XMLLoader loader;

    public GenericTreeNode<String> setupAbsoluteParent() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
    
    public void customItemClicked( VisualItem item, MouseEvent e ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void connectToDataSource() {
        loader = new XMLLoader();
        super.registerLoader( loader );
    }
}
