package edu.rit.entityg.dataloaders;

import edu.rit.entityg.database.DatabaseConnection;
import edu.rit.entityg.exceptions.BadSetupException;
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
     * A pattern basically describes the relationship between a parent and a child. When creating a node group,
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
     * starting point of the entire graph, while its children give information about the parent node.
     * @param data
     * @param columnHeader
     */
    public GenericTreeNode<String> loadAbsoluteParent( String data ) throws BadSetupException,
                                                                            IllegalArgumentException {
        if( baseQuery == null || baseColumnName == null ) {
            throw new IllegalArgumentException( "You must set a base query and base column header before loading "
                                                + "any data." );
        }
        List<String> childrenHeaders = patterns.get( baseColumnName );
        try {
            ResultSet rs = conn.executeQuery( baseQuery + baseColumnName + " = '" + data + "'" );
            ArrayList<String> results = conn.getSingleRowFromColumnHeaders( rs, childrenHeaders );
            if( results.isEmpty() ) {
                throw new BadSetupException( "The information provided to setup the graph was invalid. "
                                             + "You must provide specifications that allow EntityG to load data. "
                                             + "You have provided specifications that do not give any data to "
                                             + "EntityG." );
            } else if( childrenHeaders.size() > results.size() ) {
                throw new BadSetupException( "There are null values in your database which you want displayed. "
                                             + "Please make sure any columns with null values are ignored." );
            }
            GenericTreeNode<String> rootParent = new GenericTreeNode<String>( data, baseColumnName );
            //Add the children data to the root parent
            for( int i = 0; i < results.size(); i++ ) {
                rootParent.addChild( new GenericTreeNode<String>( results.get( i ), childrenHeaders.get( i ) ) );
            }
            return rootParent;
        } catch( SQLException sqle ) {
            throw new BadSetupException( sqle.getMessage() );
        }
    }

    /**
     * Loads information nodes. Information nodes are nodes which describe a center node. Basically, they provide
     * information about a single node. They can be considered the children of a single node.
     * @param parent The {@link GenericTreeNode} parent that we should load information for.
     * @param data The display data of <code>parent</code>.
     * @param columnHeader The data header of <code>parent</code>
     * @return <code>parent</code> with its information nodes as children.
     */
    public GenericTreeNode<String> loadInformationNodes( GenericTreeNode<String> parent,
                                                         String data,
                                                         String columnHeader ) throws BadSetupException {
        List<String> childrenHeaders = patterns.get( columnHeader );
        try {
            ResultSet rs = conn.executeQuery( baseQuery + columnHeader + " = '" + data + "'" );
            ArrayList<String> results = conn.getSingleRowFromColumnHeaders( rs, childrenHeaders );
            if( childrenHeaders.size() > results.size() ) {
                throw new BadSetupException( "There are null values in your database which you want displayed. "
                                             + "Please make sure any columns with null values are ignored." );
            }
            if( !results.isEmpty() ) {
                for( int i = 0; i < results.size(); i++ ) {
                    //Add the children data to the root parent
                    parent.addChild( new GenericTreeNode<String>( results.get( i ), childrenHeaders.get( i ) ) );
                }
            }
            return parent;
        } catch( SQLException sqle ) {
            throw new BadSetupException( sqle.getMessage() );
        }
    }

    /**
     * Loads all center nodes. If an information node is clicked, then we should display all "center nodes" which
     * have the information data in common.
     * @param parent The {@link GenericTreeNode} that is an information node.
     * @param data The display data of <code>parent</code>.
     * @param columnHeader The data header of <code>parent</code>
     * @param maxNodes The maximum number of center nodes we should load as children of <code>parent</code>
     * @return <code>parent</code> with center nodes that commonly share <code>parent's</code> data as its children.
     */
    public GenericTreeNode<String> loadCenterNodes( GenericTreeNode<String> parent,
                                                    String data,
                                                    String columnHeader,
                                                    int maxNodes ) throws BadSetupException {
        try {
            ResultSet rs = conn.executeQuery( baseQuery + columnHeader + " = '" + data + "'" );
            ArrayList<ArrayList<String>> results = conn.getData( rs, baseColumnName );
            if( results.isEmpty() ) {
                return parent;
            }
            for( int i = 0; i < results.size(); i++ ) {
                if( i >= maxNodes )
                    break;
                //Get the only piece of information in our current row.
                String result = results.get( i ).get( 0 );
                parent.addChild( new GenericTreeNode<String>( result, baseColumnName ) );
            }
        } catch( SQLException sqle ) {
            throw new BadSetupException( sqle.getMessage() );
        }
        return parent;
    }
}
