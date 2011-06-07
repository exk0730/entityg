package edu.rit.entityg.csv;

import java.util.List;

/**
 * TableRow defines an interface for holding information of a line of a character-delimited file.
 * @author Eric Kisner
 */
public interface TableRow {

    /**
     * Returns the first row in the file.
     * <p/>
     * It is assumed that the first row contains column names and no data.
     */
    public List<String> getColumnNames();

    /**
     * Returns the data in the current row at column <code>index</code>
     * @param index Column number
     * @return The data that is in column <code>index</code> as a String value.
     */
    public String getColumn( int index );

    /**
     * Looks for the specified column name in the file's header row.
     *
     * @param columnName The name of the column we want to find.
     *
     * @return int - <code>-1</code> if the column name is not found in the header, or the position of
     *         the column name in the file.
     */
    public int getColumnIndex( String columnName );
}
