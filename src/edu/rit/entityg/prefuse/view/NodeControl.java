package edu.rit.entityg.prefuse.view;

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

    /**
     * Default constructor.
     * @param displayNodeToDataNodeMap The mapping of a {@link Node} to a {@link GenericTreeNode}.
     * @param g The Graph that displays each Node.
     * @param v The Visualization that contains the Graph. This is needed so we can run the
     *          {@link EntityG#DRAW} action again, if we add any new {@link Node}s to the graph.
     */
    public NodeControl( HashMap<Node, GenericTreeNode<String>> displayNodeToDataNodeMap,
                        Graph g,
                        Visualization v ) {
        this.displayNodeToDataNodeMap = displayNodeToDataNodeMap;
        this.g = g;
        this.v = v;

    }

    @Override
    public void itemClicked( VisualItem item, MouseEvent e ) {
        if( !SwingUtilities.isLeftMouseButton( e ) ) return;
        if( e.getClickCount() == 2 ) {//DoubleClick
            //The backing Tuple of this visual item is actually a Node object (from g.addNode)
            Node backingNode = (Node) item.getSourceTuple();
            //Get the related TreeNode of this Node
            GenericTreeNode<String> treeNode = displayNodeToDataNodeMap.get( backingNode );
            if( treeNode.hasChildren() ) {
                removeAllChildren( item );
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
     */
    private void removeAllChildren( VisualItem item ) {
        NodeItem ni = (NodeItem) item;
        for( int i = 0; i < ni.getChildCount(); i++ ) {
            NodeItem child = (NodeItem) ni.getChild( i );
            EdgeItem ei = (EdgeItem) child.getParentEdge();
            child.setVisible( !child.isVisible() );
            ei.setVisible( !ei.isVisible() );
            removeAllChildren( child );
        }
    }

    /**
     * Renders the newly-added {@link Node}s and their {@link Edge}s.
     */
    private void renderNewNodes() {
        v.run( EntityG.DRAW );
    }
}