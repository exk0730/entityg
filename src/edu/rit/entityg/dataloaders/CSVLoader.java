package edu.rit.entityg.dataloaders;

import edu.rit.entityg.exceptions.BadSetupException;
import edu.rit.entityg.treeimpl.GenericTreeNode;

/**
 * @date Jun 4, 2011
 * @author Eric Kisner
 */
public class CSVLoader implements DataSourceLoader {

    public GenericTreeNode<String> loadAbsoluteParent( Object data ) throws BadSetupException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public GenericTreeNode<String> loadInformationNodes( GenericTreeNode<String> parent, Object... data ) throws BadSetupException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public GenericTreeNode<String> loadCenterNodes( GenericTreeNode<String> parent, int maxNodes, Object... data ) throws BadSetupException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    //The remaining methods are used for a DatabaseLoader and should not be implemented in this class.
    public void setBaseQuery( String baseQuery ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void setCenterNodeColumnName( String centerNodeColumnName ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void setInformationNodeColumNames( String[] columnNames ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

}
