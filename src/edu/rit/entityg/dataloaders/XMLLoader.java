package edu.rit.entityg.dataloaders;

import edu.rit.entityg.exceptions.BadSetupException;
import edu.rit.entityg.treeimpl.GenericTreeNode;

/**
 * @date Jun 7, 2011
 * @author Eric Kisner
 */
public class XMLLoader implements DataSourceLoader {

    public void close() throws Exception {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public GenericTreeNode<String> loadAbsoluteParent( Object data ) throws BadSetupException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public GenericTreeNode<String> loadInformationNodes( GenericTreeNode<String> parent, Object... data ) throws BadSetupException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public GenericTreeNode<String> loadCenterNodes( GenericTreeNode<String> parent, int maxNodes, Object... data ) throws BadSetupException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

}
