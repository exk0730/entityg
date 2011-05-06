package edu.rit.entityg.treeimpl;

import java.util.*;

/**
 * @date Apr 14, 2011
 * @author Eric Kisner
 * http://vivin.net/2010/01/30/generic-n-ary-tree-in-java/
 */
public class GenericTree<T> {

    private GenericTreeNode<T> root;
    private HashMap<Integer, List<GenericTreeNode<T>>> treeLevelToNodeList;
    private int curLevel = 0;

    public GenericTree() {
        super();
        treeLevelToNodeList = new LinkedHashMap<Integer, List<GenericTreeNode<T>>>();
    }

    public GenericTree( GenericTreeNode<T> root ) {
        this();
        setRoot( root );
    }

    public GenericTreeNode<T> getRoot() {
        return this.root;
    }

    public final void setRoot( GenericTreeNode<T> root ) {
        /**
         * You cannot change the root of the tree after it has already been set.
         */
        if( this.root == null ) {
            this.root = root;
            treeLevelToNodeList.put( curLevel, Collections.singletonList( root ) );
            curLevel++;
        }
    }

    public int getNumberOfNodes() {
        int numberOfNodes = 0;

        if( root != null ) {
            numberOfNodes = auxiliaryGetNumberOfNodes( root ) + 1; //1 for the root!
        }

        return numberOfNodes;
    }

    private int auxiliaryGetNumberOfNodes( GenericTreeNode<T> node ) {
        int numberOfNodes = node.getNumberOfChildren();

        for( GenericTreeNode<T> child : node.getChildren() ) {
            numberOfNodes += auxiliaryGetNumberOfNodes( child );
        }

        return numberOfNodes;
    }

    public boolean exists( GenericTreeNode<T> nodeToFind ) {
        return (find( nodeToFind ) != null);
    }

    public GenericTreeNode<T> find( GenericTreeNode<T> nodeToFind ) {
        GenericTreeNode<T> returnNode = null;

        if( root != null ) {
            returnNode = auxiliaryFind( root, nodeToFind );
        }

        return returnNode;
    }

    private GenericTreeNode<T> auxiliaryFind( GenericTreeNode<T> currentNode, GenericTreeNode<T> nodeToFind ) {
        GenericTreeNode<T> returnNode = null;
        int i = 0;

        if( currentNode.equals( nodeToFind ) ) {
            returnNode = currentNode;
        } else if( currentNode.hasChildren() ) {
            i = 0;
            while( returnNode == null && i < currentNode.getNumberOfChildren() ) {
                returnNode = auxiliaryFind( currentNode.getChildAt( i ), nodeToFind );
                i++;
            }
        }

        return returnNode;
    }

    public boolean isEmpty() {
        return (root == null);
    }

    public List<GenericTreeNode<T>> build( GenericTreeNode<T> node ) {
        List<GenericTreeNode<T>> traversalResult = new ArrayList<GenericTreeNode<T>>();
        buildPreOrder( node, traversalResult );
        return traversalResult;
    }

    private void buildPreOrder( GenericTreeNode<T> node, List<GenericTreeNode<T>> traversalResult ) {
        traversalResult.add( node );

        for( GenericTreeNode<T> child : node.getChildren() ) {
            buildPreOrder( child, traversalResult );
        }
    }

    public Map<GenericTreeNode<T>, Integer> buildWithDepth( GenericTreeNode<T> node ) {
        Map<GenericTreeNode<T>, Integer> traversalResult = new LinkedHashMap<GenericTreeNode<T>, Integer>();
        buildPreOrderWithDepth( node, traversalResult, 0 );
        return traversalResult;
    }

    private void buildPreOrderWithDepth( GenericTreeNode<T> node, Map<GenericTreeNode<T>, Integer> traversalResult,
                                         int depth ) {
        traversalResult.put( node, depth );

        for( GenericTreeNode<T> child : node.getChildren() ) {
            buildPreOrderWithDepth( child, traversalResult, depth + 1 );
        }
    }

    @Override
    public String toString() {
        /**
         * Assume pre-order traversal by default
         */
        String stringRepresentation = "";
        if( root != null ) {
            stringRepresentation = build( root ).toString();

        }
        return stringRepresentation;
    }

    public String toStringWithDepth() {
        /**
         * Assume pre-order traversal by default
         */
        String stringRepresentation = "";
        if( root != null ) {
            stringRepresentation = buildWithDepth( root ).toString();
        }
        return stringRepresentation;
    }
}
