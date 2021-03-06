package edu.rit.entityg.dataloaders;

import edu.rit.entityg.exceptions.BadSetupException;
import edu.rit.entityg.treeimpl.GenericTreeNode;
import java.util.HashMap;

/**
 * This is the interface for all data source loaders to implement.
 * @author Eric Kisner
 */
public interface DataSourceLoader {

    /**
     * Closes the connection to the data source.
     */
    public void close() throws Exception;

    /**
     * Sets up the base node (and its children) of EntityG. This should load all information from the data source into
     * a tree structure, with one node at the top, and its immediate children branching from it. If this method requires
     * any piece of information to be known other than <code>data</code>, they should be set through other methods. For
     * example, {@link DatabaseLoader#loadAbsoluteParent(java.lang.Object)} requires a <code>baseQuery</code>, so that
     * can be set using {@link DatabaseLoader#setBaseQuery(java.lang.String)}.
     * @param data The piece of information that is required to retrieve data from a data source. For example, for a
     *             {@link DataSourceType#DATABASE} data source, <code>data</code> will be the value that the first node
     *             should contain.
     * @return A {@link GenericTreeNode} that contains one parent node and variable children. The parent node's data
     *         should consist of <code>data</code>, while the children nodes should contain data that provides
     *         information about the parent.
     * @throws BadSetupException
     */
    public GenericTreeNode<String> loadAbsoluteParent( Object data ) throws BadSetupException;

    /**
     * Loads information nodes. Information nodes are nodes which describe (or provide information for) a center node.
     * @param parent The {@link GenericTreeNode} center node parent that we should load information for.
     * @param data A list of data that will allow a data source loader to retrieve the correct information for
     *             <code>parent</code>. This list can be any {@link Object}, however, be sure that <code>data</code>
     *             gives you exactly what you need to retrieve specific information from the data source.
     * @return <code>parent</code> with its information nodes as children.
     * @throws BadSetupException
     */
    public GenericTreeNode<String> loadInformationNodes( GenericTreeNode<String> parent,
                                                         Object... data ) throws BadSetupException;

    /**
     * Loads center nodes. Center nodes are nodes which we want to find more information about. This method is used
     * when an information node is clicked, since we want to find all center nodes which share <code>parent's</code>
     * data.
     * @param parent A {@link GenericTreeNode} that is an information node.
     * @param maxNodes The max number of center nodes that we should be returning as children to <code>parent</code>.
     *                 Since it's possible to have a very large number of nodes which share <code>parent's</code> data,
     *                 we should provide a way to limit this scope.
     * @param data A list of data that will allow a data source loader to retrieve the correct center nodes for
     *             <code>parent</code>. This list can be any {@link Object}, however, be sure that <code>data</code>
     *             gives you exactly what you need to retrieve all center node information which share <code>parent's
     *             </code> data.
     * @return <code>parent</code> with center nodes as its children.
     * @throws BadSetupException
     */
    public GenericTreeNode<String> loadCenterNodes( GenericTreeNode<String> parent, int maxNodes,
                                                    Object... data ) throws BadSetupException;
}
