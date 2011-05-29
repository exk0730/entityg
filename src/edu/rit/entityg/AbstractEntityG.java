package edu.rit.entityg;

import edu.rit.entityg.dataloaders.DataSourceLoader;
import edu.rit.entityg.prefuse.view.CustomizedForceDirectedLayout;
import edu.rit.entityg.treeimpl.GenericTreeNode;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.ToolTipManager;
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
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
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

import static edu.rit.entityg.prefuse.GraphConfig.*;

/**
 * Main display class for EntityG. This is an abstract class since there are some methods which must be implemented
 * for specific data sources.
 * @date May 6, 2011
 * @author Eric Kisner
 */
public abstract class AbstractEntityG extends Display {

    /**
     * The {@link Graph} object we are displaying.
     */
    protected Graph graph;
    /**
     * A mapping of a {@link prefuse.data.Node} object to the {@link GenericTreeNode} that contains its 'real' data.
     */
    protected HashMap<Node, GenericTreeNode<String>> displayNodeToDataNodeMap;
    /**
     * The data loader for EntityG. This is an abstract class so we can later specify exactly what data source
     * we are loading data from.
     */
    protected DataSourceLoader loader;
    /**
     * Default flag to say whether tool tips should be displayed when hovering over {@link Node}s.
     */
    protected boolean useToolTip = false;
    /**
     * Default max number of nodes to display when loading new center nodes.
     */
    protected int defaultMaxNodes = 7;

    /**
     * Default constructor. Initializes the visualization.
     */
    public AbstractEntityG() {
        super( new Visualization() );
        displayNodeToDataNodeMap = new HashMap<Node, GenericTreeNode<String>>();
    }

    /**
     * Sets up all components to EntityG, and starts the visualization.
     */
    public void start() {
        initializeGraph();
        m_vis.addGraph( GRAPH.getLabel(), graph );
        setupLabelRenderer();
        setupColorActions();
        setupMainAnimationLayout();
        setupWindow();
        m_vis.run( DRAW.getLabel() );

        JFrame frame = new JFrame( "EntityG - A Visualization for Data" );
        frame.getContentPane().add( this );
        frame.pack();
        frame.setVisible( true );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    }

    /**
     * Checks if a {@link GenericTreeNode} is already represented as a {@link Node} on the graph.
     * @param node The {@link GenericTreeNode} we want to test for existence.
     * @return The {@link Node} that exists in the {@link Graph} which represents <code>node</code>, or null
     *         if a {@link Node} does not exist.
     */
    private Node getVisualNodeFromTreeNode( GenericTreeNode<String> node ) {
        for( Node n : getNodes() ) {
            if( n.getString( LABEL.getLabel() ).equalsIgnoreCase( node.getData() ) ) {
                return n;
            }
        }
        return null;
    }

    /**
     * Returns a list of nodes that are currently displayed (visible or not) on this graph as a list of
     * {@link Node}s.
     */
    private List<Node> getNodes() {
        Node[] nodes = new Node[graph.getNodeCount()];
        for( int i = 0; i < graph.getNodeCount(); i++ ) {
            nodes[i] = graph.getNode( i );
        }
        return Arrays.asList( nodes );
    }

    /**
     * Initialize Nodes and Edges for this graph.
     */
    private void initializeGraph() {
        graph = new Graph();
        //Add a new column to the graph. This tells the Graph that our nodes will display data as Strings, and
        //tells the graph the data group name of each label - in this case, "data". Technically, this is an
        //arbitrary label.
        graph.addColumn( LABEL.getLabel(), String.class );

        GenericTreeNode<String> absoluteParent = setupAbsoluteParent();
        //Add the parent node and its children to the graph
        Node root = graph.addNode();
        root.setString( LABEL.getLabel(), absoluteParent.getData() );
        displayNodeToDataNodeMap.put( root, absoluteParent );
        renderNewNodes( root, absoluteParent );
    }

    /**
     * Set the Label Renderer of nodes for this visualization.
     */
    private void setupLabelRenderer() {
        DefaultRendererFactory drf = new DefaultRendererFactory();
        drf.setDefaultRenderer( new LabelRenderer( LABEL.getLabel() ) );
        m_vis.setRendererFactory( drf );
    }

    /**
     * Set the color actions for painting nodes and edges for this visualization.
     */
    private void setupColorActions() {
        //Set the text color to red for a node
        ColorAction nText = new ColorAction( NODES.getLabel(), VisualItem.TEXTCOLOR );
        nText.setDefaultColor( ColorLib.rgb( 100, 0, 0 ) );
        //Set the outline for a node to black
        ColorAction nStroke = new ColorAction( NODES.getLabel(), VisualItem.STROKECOLOR );
        nStroke.setDefaultColor( ColorLib.gray( 0 ) );
        //Set the fill color for a node to white
        ColorAction nFill = new ColorAction( NODES.getLabel(), VisualItem.FILLCOLOR );
        nFill.setDefaultColor( ColorLib.gray( 255 ) );
        //Set the edges of the graph to black
        ColorAction nEdges = new ColorAction( EDGES.getLabel(), VisualItem.STROKECOLOR );
        nEdges.setDefaultColor( ColorLib.gray( 0 ) );

        //Add the ColorActions to an ActionList
        ActionList draw = new ActionList();
        draw.add( nText );
        draw.add( nStroke );
        draw.add( nFill );
        draw.add( nEdges );
        //Add the 'draw' action to the visualization
        m_vis.putAction( DRAW.getLabel(), draw );
    }

    /**
     * Sets the graph to use a ForceDirectedLayout as its main Animate layout.
     */
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
        ForceDirectedLayout fdl = new CustomizedForceDirectedLayout( GRAPH.getLabel(), fsim, false );

        //Create a new action list for animating the nodes/edges.
        //These animations should run for the entirety of the program.
        ActionList animate = new ActionList( Activity.INFINITY );
        animate.add( fdl );
        animate.add( new RepaintAction() );
        m_vis.putAction( ANIMATE.getLabel(), animate );

        //Schedule the DRAW action to run before the ANIMATE action. The ANIMATE action will wait for
        //the DRAW action to run.
        m_vis.runAfter( DRAW.getLabel(), ANIMATE.getLabel() );
    }

    /**
     * Setup the window that this graph is displayed in.
     */
    private void setupWindow() {
        setSize( 500, 500 );
        pan( 250, 250 );
        setHighQuality( true );
        addControlListener( new ZoomControl() );
        addControlListener( new PanControl() );
        addControlListener( new DragControl() );
        addControlListener( new NodeControl() );
    }

    /**
     * Renders newly-added {@link Node}s and their {@link Edge}s.
     * <p/><b>Note:</b> Currently, the graph will <i>not</i> render nodes which already exist in the graph. This
     * means that there are no duplicates of a node on the graph. Instead, there will be an {@link Edge} created
     * between <code>nodeParent</code> and the pre-existing {@link Node}.
     * @param nodeParent The {@link Node} that was clicked on.
     * @param treeParent The {@link GenericTreeNode} that contains the data of the children of
     *                   <code>nodeParent</code>.
     */
    protected void renderNewNodes( Node nodeParent, GenericTreeNode<String> treeParent ) {
        for( GenericTreeNode<String> child : treeParent.getChildren() ) {
            Node n = getVisualNodeFromTreeNode( child );
            /**
             * If a node representing <code>child</code> already exists, then we should just add an edge from that node
             * to <code>nodeParent</code>. However, if an {@link Edge} exists between <code>child</code> and
             * <code>nodeParent</code>, we don't want to create a duplicate {@link Edge}.
             */
            if( n != null ) {
                if( !checkForExistingEdge( nodeParent, n ) ) {
                    graph.addEdge( nodeParent, n );
                }
                continue;
            }
            Node newNode = graph.addNode();
            newNode.setString( LABEL.getLabel(), child.getData() );
            displayNodeToDataNodeMap.put( newNode, child );
            graph.addEdge( nodeParent, newNode );
        }
        m_vis.run( DRAW.getLabel() );
    }

    /**
     * Checks for an existing edge between <code>source</code> and <code>target</code>.
     * @param source The source {@link Node}.
     * @param target The target {@link Node}.
     * @return True if there exists an {@link Edge} between <code>source</code> and <code>target</code>.
     */
    private boolean checkForExistingEdge( Node source, Node target ) {
        if( graph.getEdge( source, target ) != null || graph.getEdge( target, source ) != null ) {
            return true;
        }
        return false;
    }

    /**
     * Customized {@link ControlAdapter} specifically used when the user clicks on a {@link Node}.
     * {@link NodeControl} must be defined in this class because it needs access to the {@link Graph} object of
     * EntityG, as well as the backing {@link Visualization}.
     */
    private class NodeControl extends ControlAdapter {

        private VisualItem hovered;

        /**
         * Clear the tool tip text.
         */
        @Override
        public void itemExited( VisualItem item, MouseEvent e ) {
            if( !useToolTip ) return;
            if( hovered != null ) {
                setToolTipText( null );
                hovered = null;
            }
        }

        /**
         * Set the tool tip text for a hovered {@link VisualItem} that is an instance of a {@link Node}.
         */
        @Override
        public void itemEntered( VisualItem item, MouseEvent e ) {
            if( !useToolTip ) return;
            if( item.getSourceTuple() instanceof Node ) {
                hovered = item;
                Node source = (Node) item.getSourceTuple();
                GenericTreeNode<String> treeNode = displayNodeToDataNodeMap.get( source );
                setToolTipText( treeNode.getDataHeader() );
                ToolTipManager.sharedInstance().mouseMoved( e );
            }
        }

        @Override
        public void itemClicked( VisualItem item, MouseEvent e ) {
            customItemClicked( item, e );
        }
    } //end NodeControl adapter

    /**
     * Recursively removes all children from the Graph from the Node that was clicked on.
     * @param item The Node that was clicked on (as a VisualItem).
     * @param visibility Flag to say if we want to hide all children, or display them. If <code>hide</code> is true,
     *                   the method will set all children nodes and edges of <code>item</code> to invisible. If
     *                   <code>hide</code> is false, the method will set all children nodes and edges to visible.
     */
    protected void setVisibilityOfAllChildren( VisualItem item, boolean visibility ) {
        NodeItem ni = (NodeItem) item;
        for( int i = 0; i < ni.getChildCount(); i++ ) {
            NodeItem child = (NodeItem) ni.getChild( i );
            for( Iterator<EdgeItem> edgesIter = child.edges(); edgesIter.hasNext(); ) {
                EdgeItem ei = (EdgeItem) edgesIter.next();
                ei.setVisible( visibility );
            }
            child.setVisible( visibility );
            setVisibilityOfAllChildren( child, visibility );
        }
    }

    /**
     * Tests the first child of this {@link VisualItem} for its visibility. We only need to check the first child,
     * because the visibility status for each child after should be the same as the first child.
     * @param item The {@link VisualItem} we are testing for children visibility.
     * @return True if the children of <code>item</code> are visible, else false.
     */
    protected boolean hasVisibleChildren( VisualItem item ) {
        NodeItem ni = (NodeItem) item;
        if( !ni.children().hasNext() ) {
            return false;
        }
        return ((NodeItem) ni.getChild( 0 )).isVisible();
    }

    /**
     * Sets a new max node default.
     * @param defaultMaxNodes The new value for {@link AbstractEntityG#defaultMaxNodes}.
     */
    public void set_default_max_nodes( int defaultMaxNodes ) {
        this.defaultMaxNodes = defaultMaxNodes;
    }

    /**
     * Sets the {@link AbstractEntityG#useToolTip} flag.
     * @param useToolTip The new value for {@link AbstractEntityG#useToolTip}.
     */
    public void set_use_tool_tip( boolean useToolTip ) {
        this.useToolTip = useToolTip;
    }

    /**
     * Sets up the first node group for this graph. This is where all customization for how the graph should first
     * appear should go. Each subclass that extends {@link AbstractEntityG} should implement this function in order
     * to customize how the first node and its children is set up.
     *
     * @return A {@link GenericTreeNode} that contains a root node for this graph, and its immediate children.
     *         This parent node will be rendered on the graph.
     */
    public abstract GenericTreeNode<String> setupAbsoluteParent();

    /**
     * User-click handling method. This should be implemented in each subclass of {@link AbstractEntityG} because each
     * data sources require different information to be sent to an implementation of {@link DataSourceLoader}.
     * @param item The {@link VisualItem} that was clicked.
     * @param e The {@link MouseEvent} of the click.
     */
    public abstract void customItemClicked( VisualItem item, MouseEvent e );

    /**
     * =============================================================================================================
     * The next few methods represent any required methods that should be overriden in {@link DatabaseEntityG}.
     * These methods are used by the EntityG run classes to set up any required parameters for EntityG.
     * =============================================================================================================
     */
    /**
     * Sets the children columns we are retrieving data from.
     * @param childrenColumnNames A String with children columns, each delimited by <code>','</code>.
     */
    public abstract void set_children_columns( String childrenColumnNames );

    /**
     * Sets the host of the database we want to connect to.
     * @param optionValue The host name.
     */
    public abstract void set_host( String optionValue );

    /**
     * Sets the port of the database we want to connect to.
     * @param optionValue The port.
     */
    public abstract void set_port( String optionValue );

    /**
     * Sets the user who can access the database we want to connect to.
     * @param optionValue The username.
     */
    public abstract void set_user( String optionValue );

    /**
     * Sets the password for the user.
     * @param optionValue The password.
     */
    public abstract void set_password( String optionValue );

    /**
     * Sets the schema name we are pulling data from.
     * @param optionValue The schema name.
     */
    public abstract void set_database_name( String optionValue );

    /**
     * Sets the base query which will allow {@link AbstractEntityG} to populate information nodes with result sets of
     * this query.
     * @param optionValue The base query.
     */
    public abstract void set_base_query( String optionValue );

    /**
     * Sets the base column name for center nodes.
     * @param optionValue The base column name.
     */
    public abstract void set_base_column_name( String optionValue );

    /**
     * Sets the first center node's data. This will be the starting point to the graph.
     * @param optionValue The data for the first node.
     */
    public abstract void set_first_node_entry( String optionValue );

    /**
     * Try to connect to the database using the options.
     */
    public abstract void connectToDatabase();
}
