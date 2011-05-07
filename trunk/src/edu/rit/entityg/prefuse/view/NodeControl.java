package edu.rit.entityg.prefuse.view;

import edu.rit.entityg.dataloaders.DatabaseLoader;
import edu.rit.entityg.treeimpl.GenericTreeNode;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.SwingUtilities;
import prefuse.Visualization;
import prefuse.controls.ControlAdapter;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * Customized ControlAdapter specifically used when the user clicks on a {@link Node}.
 * @date May 6, 2011
 * @author Eric Kisner
 */
public class NodeControl extends ControlAdapter {

    private HashMap<Node, GenericTreeNode<String>> displayNodeToDataNodeMap;
    private Graph g;
    private Visualization v;
    private DatabaseLoader loader;

    /**
     * Default constructor.
     * @param displayNodeToDataNodeMap The mapping of a {@link Node} to a {@link GenericTreeNode}.
     * @param g The Graph that displays each Node.
     * @param v The Visualization that contains the Graph. This is needed so we can run the
     *          {@link EntityG#DRAW} action again, if we add any new {@link Node}s to the graph.
     */
    public NodeControl( HashMap<Node, GenericTreeNode<String>> displayNodeToDataNodeMap,
                        Graph g, Visualization v, DatabaseLoader loader ) {
        this.displayNodeToDataNodeMap = displayNodeToDataNodeMap;
        this.g = g;
        this.v = v;
        this.loader = loader;

    }

    @Override
    public void itemClicked( VisualItem item, MouseEvent e ) {
        if( !SwingUtilities.isLeftMouseButton( e ) ) return;
        if( e.getClickCount() == 2 ) {//DoubleClick
            //The backing Tuple of this visual item is actually a Node object (from g.addNode)
            Node source = (Node) item.getSourceTuple();
            //Get the related TreeNode of this Node
            GenericTreeNode<String> treeNode = displayNodeToDataNodeMap.get( source );
            /**
             * If the Tree node has children, and they are visible nodes on the graph, we want to set those
             * nodes to be invisible. Else, if the tree node has children and they are invisible, we want
             * to set those nodes to be visible.
             */
            if( treeNode.hasChildren() && hasVisibleChildren( item ) ) {
                setVisibilityOfAllChildren( item, false );
            } else if( treeNode.hasChildren() && !hasVisibleChildren( item ) ) {
                setVisibilityOfAllChildren( item, true );
            } else {
//                treeNode = loader.tryNewLoad( treeNode );
//                if( treeNode.hasChildren() ) {
//                    renderGraph( backingNode, treeNode );
//                }
            }
        }
    }

    /**
     * Recursively removes all children from the Graph from the Node that was clicked on.
     * @param item The Node that was clicked on (as a VisualItem).
     * @param visibility Flag to say if we want to hide all children, or display them. If <code>hide</code> is true,
     *                   the method will set all children nodes and edges of <code>item</code> to invisible. If
     *                   <code>hide</code> is false, the method will set all children nodes and edges to visible.
     */
    private void setVisibilityOfAllChildren( VisualItem item, boolean visibility ) {
        NodeItem ni = (NodeItem) item;
        for( int i = 0; i < ni.getChildCount(); i++ ) {
            NodeItem child = (NodeItem) ni.getChild( i );
            EdgeItem ei = (EdgeItem) child.getParentEdge();
            child.setVisible( visibility );
            ei.setVisible( visibility );
            setVisibilityOfAllChildren( child, visibility );
        }
    }

    /**
     * Tests the first child of this {@link VisualItem} for its visibility. We only need to check the first child,
     * because the visibility status for each child after should be the same as the first child.
     * @param item The {@link VisualItem} we are testing for children visibility.
     * @return True if the children of <code>item</code> are visible, else false.
     */
    private boolean hasVisibleChildren( VisualItem item ) {
        NodeItem ni = (NodeItem) item;
        return ((NodeItem) ni.getChild( 0 )).isVisible();
    }

    /**
     * Renders the newly-added {@link Node}s and their {@link Edge}s.
     */
    private void renderNewNodes() {
        v.run( EntityG.DRAW );
    }
}
