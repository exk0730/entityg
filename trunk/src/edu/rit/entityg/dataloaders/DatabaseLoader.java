package edu.rit.entityg.dataloaders;

import edu.rit.entityg.database.DatabaseConnection;
import edu.rit.entityg.treeimpl.GenericTreeNode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import prefuse.data.Node;

/**
 * Exposes methods to load data from a database and format data from that database into a prefuse-readable format.
 * This means that there are a few things that need to be known prior to loading any data into EntityG:
 * <ul>
 * <li>What type of data the node represents. Using a database data source as an example, the data a node represents
 * is based on a column name. If the data you are trying to display is located within a database table in a column
 * named "FirstName", then the {@link GenericTreeNode}'s data header should be "FirstName".
 * </li><li>The method to retrieve the information to be loaded into {@link Node}s. For a database data source, the way
 * to load data into {@link Node}s, is to perform SQL queries on the database. Based on the information returned
 * by the SQL queries, we can load data into {@link GenericTreeNode}s, and then build a {@link Node} around to
 * correspond to that.
 * </li><li>The maximum number of nodes, or results, that a single query should produce. Since a query can produce
 * multiple rows of results, we need to limit how many we're going to load into the prefuse graph. We do not want
 * the graph to become cluttered or unreadable due to too many nodes. However, for the initial data load, we do not
 * need to limit the amount nodes that can be produced.
 * </li><li>The relationships between nodes. For example, if the absolute parent is a person's name, and its children
 * describe that person (such as their address, their phone number, etc), if you click on a child node, it should
 * display other person's names who share that commonality (for example, two people who share the same city). And if
 * you click on a person's name node, that will expand into <i>that</i> person's information. And so on.
 * </li>
 * </ul>
 * @date May 6, 2011
 * @author Eric Kisner
 */
public class DatabaseLoader {

    /**
     * A patter basically describes the relationship between a parent and a child. When creating a node group,
     * there should be a parent node, followed by its children, which describe the parent node (or display some
     * information about the parent). In the case of a database pattern, the key should be the parent-header, and
     * the value should be a list of all column headers which describe the parent. <p/>As an example, "FirstName" could
     * be the parent node within a node group, and "City", "State", "Age" could all be columns in the same table
     * (or possibly different tables) that describe the "person" node. So the mapping for this 'pattern' would look
     * like: <code>["FirstName" -> {"City", "State", "Age"}]</code>
     */
    private HashMap<String, List<String>> patterns;
    /**
     * The <code>baseQuery</code> for the database loader is the query which will provide all information needed
     * to load data into the prefuse nodes. This query will usually look something like:
     * <br/><code>SELECT * FROM <i>tableName</i> WHERE</code>
     * <br/>with the ending to the where-clause being specified by what node is clicked on (or initial load
     * specifications). It can be a complicated SQL query, or a simple one, but the query should contain the table
     * name, and possibly the specific columns you want to be looking at (although, these columns can be specified
     * by other means). If the baseQuery is null at the time of this class performing any functions, the program
     * will fail to execute and exit gracefully.
     */
    private String baseQuery = null;
    /**
     * The column name of the absolute root's data.
     */
    private String baseColumnName = null;
    private DatabaseConnection conn;

    /**
     * Default constructor.
     * @param conn A connection to the database of our choice.
     */
    public DatabaseLoader( DatabaseConnection conn ) {
        this.conn = conn;
        this.patterns = new HashMap<String, List<String>>();
    }

    /**
     * Set the base query for loading data into prefuse nodes.
     */
    public void setBaseQuery( String baseQuery ) {
        this.baseQuery = baseQuery;
    }

    /**
     * Get the <code>baseQuery</code> of this database loader.
     */
    public String getBaseQuery() {
        return baseQuery;
    }

    /**
     * Set the base column name of the absolute root's data.
     */
    public void setBaseColumnName( String baseColumnName ) {
        this.baseColumnName = baseColumnName;
    }

    /**
     * Get the <code>baseColumnName</code> of this database loader.
     */
    public String getBaseColumnName() {
        return baseColumnName;
    }

    /**
     * Add a new parent header, along with a list of its children headers.
     * @param parentHeader The parent's column header.
     * @param childrenHeaders A list of the parent's children's column headers.
     */
    public void addPattern( String parentHeader, List<String> childrenHeaders ) {
        patterns.put( parentHeader, childrenHeaders );
    }

    /**
     * Add a new parent header with only a single child header.
     * @param parentHeader The parent's column header.
     * @param childHeader The only child's column header.
     */
    public void addSinglePattern( String parentHeader, String childHeader ) {
        patterns.put( parentHeader, Collections.singletonList( childHeader ) );
    }

    /**
     * Set a pre-made mapping of patterns.
     */
    public void setPatterns( HashMap<String, List<String>> patterns ) {
        this.patterns = new HashMap<String, List<String>>( patterns );
    }

    /**
     * Get the patterns that are currently contained by this data loader.
     */
    public HashMap<String, List<String>> getPatterns() {
        return this.patterns;
    }

    /**
     * The absolute parent should only return a parent node with its children. Therefore, the parent node is the main
     * descriptor of the entire graph, while its children give information about the parent node.
     * @param query
     */
    public GenericTreeNode<String> loadAbsoluteParent( String parentNodeLabel, String parentHeader ) {
        if( baseQuery == null ) {
            //TODO: do something - bad bad bad bad
        }
        List<String> childrenHeaders = patterns.get( parentHeader );
        try {
            ResultSet rs = conn.executeQuery( baseQuery + parentHeader + " = '" + parentNodeLabel + "'" );
            ArrayList<String> results = conn.getSingleRowFromColumnHeaders( rs, childrenHeaders );
            if( results.isEmpty() ) {
                //TODO: means that the query was invalid / didn't return any information
            } else if( childrenHeaders.size() > results.size() ) {
                //TODO: means that the query returned null values which are not included in the results -
                //we don't want "null" as a label for ANY node, even if the user wants that data in the graph
            }
            GenericTreeNode<String> rootParent = new GenericTreeNode<String>( parentNodeLabel, parentHeader );
            //Add the children data to the root parent
            for( int i = 0; i < results.size(); i++ ) {
                rootParent.addChild( new GenericTreeNode<String>( results.get( i ), childrenHeaders.get( i ) ) );
            }
            return rootParent;
        } catch( SQLException sqle ) {
            //TODO: something meaningful
            sqle.printStackTrace();
        }
        //TODO: an empty node, instead of null might be better here.
        return null;
    }

    public GenericTreeNode<String> createAbsoluteParent( String label, String dataColumn ) {
        return new GenericTreeNode<String>( label, dataColumn );
    }

    /**
     * Loads all children nodes of a 'describing node.'
     * ---------If the user clicks a contact name, get all information about that contact.
     * @param parent
     * @param label
     * @param dataColumn
     * @return
     */
    public GenericTreeNode<String> x( GenericTreeNode<String> parent,
                                      String label,
                                      String dataColumn ) {
        if( baseQuery == null ) {
            //TODO: do something - bad bad bad bad
        }
        List<String> childrenHeaders = patterns.get( dataColumn );
        try {
            ResultSet rs = conn.executeQuery( baseQuery + dataColumn + " = '" + label + "'" );
            ArrayList<String> results = conn.getSingleRowFromColumnHeaders( rs, childrenHeaders );
            if( results.isEmpty() ) {
                //TODO: means that the query was invalid / didn't return any information
            } else if( childrenHeaders.size() > results.size() ) {
                //TODO: means that the query returned null values which are not included in the results -
                //we don't want "null" as a label for ANY node, even if the user wants that data in the graph
            }
            //Add the children data to the root parent
            for( int i = 0; i < results.size(); i++ ) {
                parent.addChild( new GenericTreeNode<String>( results.get( i ), childrenHeaders.get( i ) ) );
            }
            return parent;
        } catch( SQLException sqle ) {
            //TODO: something meaningful
            sqle.printStackTrace();
        }
        //TODO: an empty node, instead of null might be better here.
        return null;
    }

    /**
     * Loads all children nodes of an 'information node.'
     * ---------If the user clicks "Mr", get all contact names with "Mr"
     * @param parent
     * @param label
     * @param dataColumn
     * @param maxNodes
     * @return
     */
    public GenericTreeNode<String> y( GenericTreeNode<String> parent,
                                      String label,
                                      String dataColumn,
                                      int maxNodes ) {
        if( baseQuery == null ) {
            //TODO: BAD
        }
        try {
            ResultSet rs = conn.executeQuery( baseQuery + dataColumn + " = '" + label + "'" );
            ArrayList<ArrayList<String>> data = conn.getData( rs, baseColumnName );
            if( data.isEmpty() ) {
                return parent;
            }
            for( int i = 0; i < data.size(); i++ ) {
                if( i >= maxNodes )
                    break;
                //Get the only piece of information in our current row.
                String result = data.get( i ).get( 0 );
                parent.addChild( new GenericTreeNode<String>( result, baseColumnName ) );
            }
        } catch( SQLException sqle ) {
            //TODO: something meaningful
            sqle.printStackTrace();
        }
        return parent;
    }

    /**
     *
     * @param parent 
     * @return
     */
    public GenericTreeNode<String> x( GenericTreeNode<String> parent,
                                      String parentNodeLabel,
                                      String parentHeader,
                                      int maximumNodes ) {
        if( baseQuery == null ) {
            //TODO: BAD
        }
        try {
            //If the user wants information about a "ContactName" node
            if( parentHeader.equals( baseColumnName ) ) {
                parent = loadAbsoluteParent( parentNodeLabel, parentHeader );
                return parent;
            }
            ResultSet rs = conn.executeQuery( baseQuery + parentHeader + " = '" + parentNodeLabel + "'" );
            ArrayList<ArrayList<String>> data = conn.getData( rs, baseColumnName );
            if( data.isEmpty() ) {
                return parent;
            }
            for( int i = 0; i < data.size(); i++ ) {
                if( i >= maximumNodes )
                    break;
                //Get the only piece of information in our current row.
                String result = data.get( i ).get( 0 );
                parent.addChild( new GenericTreeNode<String>( result, baseColumnName ) );
            }
        } catch( SQLException sqle ) {
            //TODO: something meaningful
            sqle.printStackTrace();
        }
        return parent;
    }
}
