package edu.rit.entityg.prefuse.view;

import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.util.force.ForceSimulator;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

/**
 * Customized force directed layout. Suited to allow greater customization of spring effects.
 * @date May 6, 2011
 * @author Eric Kisner
 */
public class CustomizedForceDirectedLayout extends ForceDirectedLayout {

    /**
     * Default constructor.
     * @param group The data group name this layout should belong to.
     * @param fsim The backing force simulator. The Force Simulator should be setup prior to creating this layout.
     * @param enforceBounds Set this to true if the layout should enforce the bounds of the graph window (the graph's
     *                      edges and nodes will not go beyond those bounds).
     */
    public CustomizedForceDirectedLayout( String group, ForceSimulator fsim, boolean enforceBounds ) {
        super( group, fsim, enforceBounds, false );
    }

    @Override
    protected float getSpringLength( EdgeItem e ) {
        return 100;
    }

    @Override
    protected float getMassValue( VisualItem n ) {
        return 1.0f;
    }

    @Override
    protected float getSpringCoefficient( EdgeItem e ) {
        return -1;
    }
}
