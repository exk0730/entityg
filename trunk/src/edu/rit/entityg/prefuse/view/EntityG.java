package edu.rit.entityg.prefuse.view;

import edu.rit.entityg.database.DatabaseConnection;
import edu.rit.entityg.dataloaders.DatabaseLoader;
import edu.rit.entityg.treeimpl.GenericTree;
import edu.rit.entityg.treeimpl.GenericTreeNode;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.RungeKuttaIntegrator;
import prefuse.util.force.SpringForce;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * Main entry point to EntityG. Creates the Display used for the entity-association graph.
 * @date May 6, 2011
 * @author Eric Kisner
 */
public class EntityG extends Display {

    // <editor-fold defaultstate="collapsed" desc="Main">
    public static void main( String[] args ) {
        EntityG entityg = new EntityG();
        JFrame frame = new JFrame( "EntityG - A Visualization for Data" );
        frame.getContentPane().add( entityg );
        frame.pack();
        frame.setVisible( true );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    }//</editor-fold>
    /**
     * Data group name for the graph
     */
    private static final String GRAPH = "graph";
    /**
     * Data group name for nodes
     */
    private static final String NODES = "graph.nodes";
    /**
     * Data group name for edges
     */
    private static final String EDGES = "graph.edges";
    /**
     * Data group name for node-labels
     */
    public static final String LABEL = "data";
    /**
     * Name of the action for "drawing"
     */
    public static final String DRAW = "draw";
    /**
     * Name of the action for "animating"
     */
    private static final String ANIMATE = "animate";
    /**
     * The Graph object we are displaying.
     */
    private Graph graph;
    /**
     * A mapping of a {@link prefuse.data.Node} object to the {@link GenericTreeNode} that contains its 'real' data.
     */
    private HashMap<Node, GenericTreeNode<String>> displayNodeToDataNodeMap;
    /**
     * Tree that contains a root node, and all of its children.
     */
    private GenericTree<String> tree = new GenericTree<String>();
    /**
     * Default max number of nodes to display per each node-group.
     */
    private static final int DEFAULT_MAX_NODES = 5;
    /**
     * The data loader for EntityG. In the future, there can be different data loaders (such as, from XML or
     * a CSV file).
     */
    private DatabaseLoader loader = new DatabaseLoader( DatabaseConnection.instance() );

    /**
     * Default constructor. Starts everything.
     */
    public EntityG() {
        super( new Visualization() );
        displayNodeToDataNodeMap = new HashMap<Node, GenericTreeNode<String>>();
        initializeGraph();
        m_vis.addGraph( GRAPH, graph );

        setupLabelRenderer();
        setupColorActions();
        setupMainAnimationLayout();
        setupWindow();

        //Set things running.
        m_vis.run( DRAW );
    }

    /**
     * Checks if a {@link GenericTreeNode} is already represented as a {@link Node} on the graph.
     * @param node The {@link GenericTreeNode} we want to test for existence.
     * @return True if <code>node</code> already exists on the graph, else false.
     */
    private boolean existsInGraph( GenericTreeNode<String> node ) {
        for( Node n : getNodes() ) {
            if( n.getString( LABEL ).equalsIgnoreCase( node.getData() ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of nodes that are currently displayed (visible or not) on this graph as a list of
     * {@link Node}s.
     */
    public List<Node> getNodes() {
        Node[] nodes = new Node[graph.getNodeCount()];
        for( int i = 0; i < graph.getNodeCount(); i++ ) {
            nodes[i] = graph.getNode( i );
        }
        return Arrays.asList( nodes );
    }

    /**
     * Returns a list of nodes that are currently displayed (visible or not) on this graph as a list of
     * {@link VisualItem}s.
     */
    public List<VisualItem> getNodesAsVisualItems() {
        TupleSet nodes = m_vis.getGroup( NODES );
        VisualItem[] items = new VisualItem[nodes.getTupleCount()];
        int tupleCounter = 0;
        for( Iterator tupleIter = nodes.tuples(); tupleIter.hasNext(); ) {
            items[tupleCounter] = m_vis.getVisualItem( NODES, (Tuple) tupleIter.next() );
            tupleCounter++;
        }
        return Arrays.asList( items );
    }

    /**
     * Initialize Nodes and Edges for this graph.
     * @return A Graph object that contains the initial nodes and edges that will be displayed.
     */
    private void initializeGraph() {
        graph = new Graph();
        //Add a new column to the graph. This tells the Graph that our nodes will display data as Strings, and
        //tells the graph the data group name of each label - in this case, "data". Technically, this is an
        //arbitrary label.
        graph.addColumn( LABEL, String.class );

        GenericTreeNode<String> absoluteParent = setupAbsoluteParent();
        tree.setRoot( absoluteParent );
        //Add the parent node and its children to the graph
        Node root = graph.addNode();
        root.setString( LABEL, absoluteParent.getData() );
        displayNodeToDataNodeMap.put( root, absoluteParent );
        for( GenericTreeNode<String> aChild : absoluteParent.getChildren() ) {
            Node nodeChild = graph.addNode();
            nodeChild.setString( LABEL, aChild.getData() );
            displayNodeToDataNodeMap.put( nodeChild, aChild );
            graph.addEdge( root, nodeChild );
        }
    }

    /**
     * Sets up the first node group for this graph. This is where all customization for how the graph should first
     * appear should go.
     * @return A {@link GenericTreeNode} that contains a root node for this graph, and its immediate children.
     *         This parent node will be rendered on the graph.
     */
    // <editor-fold defaultstate="collapsed" desc="Setup absolute parent">
    private GenericTreeNode<String> setupAbsoluteParent() {
        //TODO: move this somewhere else - maybe have it be an argument to the program
        loader.setBaseQuery( "SELECT * FROM customers WHERE " );
        loader.setBaseColumnName( "ContactName" );
        //Load data from VARDB
        String[] children = { "Title", "CompanyName", "Addr1", "City", "State", "Zip" };
        //Set a pattern
        loader.addPattern( "ContactName", Arrays.asList( children ) );
        //Get the first node group as an instance of a parent node
        return loader.loadAbsoluteParent( "Kevin Battle", "ContactName" );
    }//</editor-fold>

    /**
     * Set the Label Renderer of nodes for this visualization.
     */
    // <editor-fold defaultstate="collapsed" desc="Setup label renderer">
    private void setupLabelRenderer() {
        DefaultRendererFactory drf = new DefaultRendererFactory();
        drf.setDefaultRenderer( new LabelRenderer( LABEL ) );
        m_vis.setRendererFactory( drf );
    }// </editor-fold>

    /**
     * Set the color actions for painting nodes and edges for this visualization.
     */
    // <editor-fold defaultstate="collapsed" desc="Setup color actions">
    private void setupColorActions() {
        //Set the text color to red for a node
        ColorAction nText = new ColorAction( NODES, VisualItem.TEXTCOLOR );
        nText.setDefaultColor( ColorLib.rgb( 100, 0, 0 ) );
        //Set the outline for a node to black
        ColorAction nStroke = new ColorAction( NODES, VisualItem.STROKECOLOR );
        nStroke.setDefaultColor( ColorLib.gray( 0 ) );
        //Set the fill color for a node to white
        ColorAction nFill = new ColorAction( NODES, VisualItem.FILLCOLOR );
        nFill.setDefaultColor( ColorLib.gray( 255 ) );
        //Set the edges of the graph to black
        ColorAction nEdges = new ColorAction( EDGES, VisualItem.STROKECOLOR );
        nEdges.setDefaultColor( ColorLib.gray( 0 ) );

        //Add the ColorActions to an ActionList
        ActionList draw = new ActionList();
        draw.add( nText );
        draw.add( nStroke );
        draw.add( nFill );
        draw.add( nEdges );
        //Add the 'draw' action to the visualization
        m_vis.putAction( DRAW, draw );
    }// </editor-fold>

    /**
     * Sets the graph to use a ForceDirectedLayout as its main Animate layout.
     */
    // <editor-fold defaultstate="collapsed" desc="Setup animation layout">
    private void setupMainAnimationLayout() {
        //Set the algorithm the ForceSimulator should use
        ForceSimulator fsim = new ForceSimulator( new RungeKuttaIntegrator() );
        //Set coefficients for a ForceSimulator
        float gravConstant = -1f;
        float minDistance = -3f;
        float theta = 0.9f;
        float drag = 0.03f;
        float springCoeff = 1E-4f;
        float defaultLength = 150f;
        //Add all forces to the simulator
        fsim.addForce( new NBodyForce( gravConstant, minDistance, theta ) );
        fsim.addForce( new DragForce( drag ) );
        fsim.addForce( new SpringForce( springCoeff, defaultLength ) );

        //Create a customized force directed layout from the force simulator
        ForceDirectedLayout fdl = new CustomizedForceDirectedLayout( GRAPH, fsim, false );

        //Create a new action list for animating the nodes/edges.
        //These animations should run for the entirety of the program.
        ActionList animate = new ActionList( Activity.INFINITY );
        animate.add( fdl );
        animate.add( new RepaintAction() );
        m_vis.putAction( ANIMATE, animate );

        //Schedule the DRAW action to run before the ANIMATE action. The ANIMATE action will wait for
        //the DRAW action to run.
        m_vis.runAfter( DRAW, ANIMATE );
    }// </editor-fold>

    /**
     * Setup the window that this graph is displayed in.
     */
    // <editor-fold defaultstate="collapsed" desc="Setup graph window">
    private void setupWindow() {
        setSize( 500, 500 );
        pan( 250, 250 );
        setHighQuality( true );
        addControlListener( new ZoomControl() );
        addControlListener( new PanControl() );
        addControlListener( new DragControl() );
        addControlListener( new NodeControl() );
    }// </editor-fold>

    /**
     * Customized ControlAdapter specifically used when the user clicks on a {@link Node}.
     * {@link NodeControl} must be defined in this class because it needs access to the {@link Graph} object of
     * EntityG, as well as the backing {@link Visualization}.
     */
    // <editor-fold defaultstate="collapsed" desc="Custom node control private class">
    private class NodeControl extends ControlAdapter {

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
                if( treeNode.hasChildren() ) {
                    if( hasVisibleChildren( item ) ) {
                        setVisibilityOfAllChildren( item, false );
                    } else {
                        setVisibilityOfAllChildren( item, true );
                    }
                } else {
                    //If they click on "ContactName"
                    if( treeNode.getDataHeader().equalsIgnoreCase( loader.getBaseColumnName() ) ) {
                        treeNode = loader.x( treeNode, treeNode.getData(), treeNode.getDataHeader() );
                    } else {    //Else they clicked on "Mr"/"Rochester"/etc
                        treeNode = loader.y( treeNode, treeNode.getData(),
                                             treeNode.getDataHeader(), DEFAULT_MAX_NODES );
                    }
                    //Retrieve all children of this TreeNode and render it on the graph.
                    if( treeNode.hasChildren() ) {
                        renderNewNodes( source, treeNode );
                    }
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
        private void renderNewNodes( Node nodeParent, GenericTreeNode<String> treeParent ) {
            for( GenericTreeNode<String> child : treeParent.getChildren() ) {
                if( existsInGraph( child ) ) {
                    continue;
                }
                Node newNode = graph.addNode();
                newNode.setString( LABEL, child.getData() );
                displayNodeToDataNodeMap.put( newNode, child );
                graph.addEdge( newNode, nodeParent );
            }
            m_vis.run( DRAW );
        }
    }// </editor-fold>
}
