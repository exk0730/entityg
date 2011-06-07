package edu.rit.entityg.csv;

import java.util.Arrays;
import java.util.List;

/**
 * Implementation for a {@link TableRow}
 * @author Eric Kisner
 */
public class ATableRow implements TableRow {

    private final String[] values;
    private final List<String> columnNames;

    public ATableRow( String[] values, List<String> columnNames ) {
        this.values = values;
        this.columnNames = columnNames;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public String getColumn( int index ) {
        return values[index];
    }

    public int getColumnIndex( String columnName ) {
        for( int i = 0; i < columnNames.size(); i++ ) {
            if( columnNames.get( i ).contains( columnName ) ) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        String ret = "";
        ret += "\nColumn titles\t: " + columnNames;
        ret += "\nRow data\t: " + Arrays.asList( values );
        return ret;
    }
}
