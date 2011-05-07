package edu.rit.entityg.treeimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @date Apr 14, 2011
 * @author Eric Kisner
 */
public class GenericTreeNode<T> {

    private T displayData;
    private T dataHeader;
    /**
     * A node may have hidden data which should not be displayed to the user.
     * For example: if dealing with Primary Key IDs from a database, you can store these as hidden data
     * in the node. This is kept in a map to allow the hidden data to be keyed.
     */
    private Map<T, T> hiddenData;
    private List<GenericTreeNode<T>> children;

    /**
     * Default constructor. Allows for manual data setting.
     */
    public GenericTreeNode() {
        children = new ArrayList<GenericTreeNode<T>>();
        hiddenData = new HashMap<T, T>();
    }

    /**
     * Constructor to specify what data this node should display, and what that data represents.
     * @param displayData Data which this node should display.
     * @param dataHeader The header for the data. For example, if the displayData is from a database,
     *                   the header should exactly mimic the column-header that we are pulling the data from.
     */
    public GenericTreeNode( T displayData, T dataHeader ) {
        this();
        setData( displayData );
        setDataHeader( dataHeader );
    }

    /**
     * Constructor for a node which has display data and hidden data.
     * @param displayData Data which this node should display.
     * @param hiddenData All data which this node should keep hidden.
     */
    public GenericTreeNode( T displayData, T dataHeader, Map<T, T> hiddenData ) {
        this( displayData, dataHeader );
        setHiddenData( hiddenData );
    }

    /**
     * Returns this node's children nodes.
     * @return A list of this node's children nodes.
     */
    public List<GenericTreeNode<T>> getChildren() {
        return this.children;
    }

    /**
     * Returns the number of children nodes this node has.
     */
    public int getNumberOfChildren() {
        return getChildren().size();
    }

    /**
     * Tests for this node's children.
     * @return True if this node has children, else false.
     */
    public boolean hasChildren() {
        return (getNumberOfChildren() > 0);
    }

    /**
     * Sets this node's children.
     * @param children A list of tree nodes that are children of this node.
     */
    public void setChildren( List<GenericTreeNode<T>> children ) {
        this.children = children;
    }

    /**
     * Add a new child node to this node.
     */
    public void addChild( GenericTreeNode<T> child ) {
        children.add( child );
    }

    /**
     * Add a new child node at a specified index.
     */
    public void addChildAt( int index, GenericTreeNode<T> child ) throws IndexOutOfBoundsException {
        children.add( index, child );
    }

    /**
     * Remove all children from this node.
     */
    public void removeChildren() {
        this.children = new ArrayList<GenericTreeNode<T>>();
    }

    /**
     * Remove a child node at the specified index.
     */
    public void removeChildAt( int index ) throws IndexOutOfBoundsException {
        children.remove( index );
    }

    /**
     * Returns a child at a specified index.
     */
    public GenericTreeNode<T> getChildAt( int index ) throws IndexOutOfBoundsException {
        return children.get( index );
    }

    /**
     * Returns the data this node holds.
     */
    public T getData() {
        return this.displayData;
    }

    /**
     * Set this node's data.
     */
    public final void setData( T data ) {
        this.displayData = data;
    }

    /**
     * Returns the column header for the display data.
     */
    public T getDataHeader() {
        return this.dataHeader;
    }

    /**
     * Set this node's data header.
     */
    public final void setDataHeader( T dataHeader ) {
        this.dataHeader = dataHeader;
    }

    /**
     * Adds a pre-populated map to this node's hidden data map.
     */
    public void addHiddenData( Map<T, T> hiddenData ) {
        this.hiddenData.putAll( hiddenData );
    }

    /**
     * Adds a new piece of hidden data to this node.
     */
    public final void addHiddenData( T key, T data ) {
        this.hiddenData.put( key, data );
    }

    /**
     * Checks if this node has hidden data.
     * @return True if this node has hidden data, else false.
     */
    public boolean hasHiddenData() {
        return this.hiddenData != null;
    }

    /**
     * Returns the hidden data of this node.
     */
    public Map<T, T> getHiddenData() {
        return this.hiddenData;
    }

    /**
     * Store this node's hidden data.
     */
    public final void setHiddenData( Map<T, T> hiddenData ) {
        this.hiddenData = hiddenData;
    }

    /**
     * Returns the string representation of this node.
     * Use {@link GenericTreeNode#toStringVerbose()} for the string representation of this
     * node and its children.
     * @return A string containing this node's display data and hidden data.
     */
    @Override
    public String toString() {
        String ret = "";
        ret += (getData() == null) ? "" : getData().toString();
        ret += " (HIDDEN): ";
        ret += (getHiddenData() == null) ? "" : getHiddenData().toString();
        return ret;
    }

    @Override
    public boolean equals( Object obj ) {
        if( obj == null ) return false;
        if( getClass() != obj.getClass() ) return false;
        final GenericTreeNode<T> other = (GenericTreeNode<T>) obj;
        if( this.displayData != other.displayData
            && (this.displayData == null || !this.displayData.equals( other.displayData )) )
            return false;
        if( this.dataHeader != other.dataHeader
            && (this.dataHeader == null || !this.dataHeader.equals( other.dataHeader )) )
            return false;
        if( this.hiddenData != other.hiddenData
            && (this.hiddenData == null || !this.hiddenData.equals( other.hiddenData )) )
            return false;
        if( this.children != other.children
            && (this.children == null || !this.children.equals( other.children )) )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.displayData != null ? this.displayData.hashCode() : 0);
        hash = 53 * hash + (this.dataHeader != null ? this.dataHeader.hashCode() : 0);
        hash = 53 * hash + (this.hiddenData != null ? this.hiddenData.hashCode() : 0);
        hash = 53 * hash + (this.children != null ? this.children.hashCode() : 0);
        return hash;
    }

    /**
     * Verbose toString. Gets the toString of all this node's children.
     */
    public String toStringVerbose() {
        String stringRepresentation = this.toString() + ":[";

        for( GenericTreeNode<T> node : getChildren() ) {
            stringRepresentation += node.toString() + ", ";
        }

        //Pattern.DOTALL causes ^ and $ to match. Otherwise it won't. It's retarded.
        Pattern pattern = Pattern.compile( ", $", Pattern.DOTALL );
        Matcher matcher = pattern.matcher( stringRepresentation );

        stringRepresentation = matcher.replaceFirst( "" );
        stringRepresentation += "]";

        return stringRepresentation;
    }
}
