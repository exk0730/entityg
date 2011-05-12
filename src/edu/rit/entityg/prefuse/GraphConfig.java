package edu.rit.entityg.prefuse;

/**
 *
 * @author Eric Kisner
 */
public enum GraphConfig {

    /**
     * Data group name for the graph
     */
    GRAPH( "graph" ),
    /**
     * Data group name for nodes
     */
    NODES( "graph.nodes" ),
    /**
     * Data group name for edges
     */
    EDGES( "graph.edges" ),
    /**
     * Data group name for node-labels
     */
    LABEL( "data" ),
    /**
     * Name of the action for "drawing"
     */
    DRAW( "draw" ),
    /**
     * Name of the action for "animating"
     */
    ANIMATE( "animate" );
    private String label;

    private GraphConfig( String label ) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
