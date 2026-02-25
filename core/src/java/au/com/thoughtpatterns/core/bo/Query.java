package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import au.com.thoughtpatterns.core.util.BusinessDate;

/**
 * Interface for invoking an SQL query. Parameters may be provided to the query
 * either via the sequential {@link #setNextValue} method or the random-access
 * {@link #setIndexedValue} method. It's not a good idea to mix both types of
 * method calls.
 * 
 * This is effectively a stripped down and disconnected version of the 
 * JDBC prepared statement and result set. It is easier to use than JDBC 
 * because you don't have to worry about closing statements, results sets, etc,
 * and it will work across client-server boundaries. The downside is that it is 
 * much less powerful.
 * 
 * <p />
 * A Query can be obtained from the {@link QueryFactory}.
 */
public interface Query extends Serializable {

    /**
     * Set the next value for the query.
     * 
     * @param value the next value
     * @return the index of the parameter that was set (1 = first parameter)
     */
    int setNextValue(Object value);

    /**
     * Set the value at the given index position.
     */
    void setIndexedValue(int index, Object value);

    /**
     * Set the index of the first row to return when executing (0 = first row)
     */
    void setFirstRow(int firstRow);
    
    /**
     * Set the maximum number of rows to return (0 = unbounded)
     */
    void setMaxRows(int maxRows);
    
    /**
     * Execute the query.
     * @return a List of Row objects, one per returned row.
     */
    List<Row> execute();

    /**
     * Execute the special case where the return result is a single row
     * with a single column. This is a convenience method to make it easier to 
     * get at the return result
     */
    Object executeSingle();
    
    /**
     * Special case of executeSingle where return value is an Integer
     */
    Integer executeSingleInt();

    /**
     * Special case of executeSingle where return value is a Long
     */
    Long executeSingleLong();

    public static interface Row extends Serializable {
        Object getValue(int index);
        
        // Convenience methods
        String getString(int index);
        BusinessDate getBusinessDate(int index);
        Long getLong(int index);
        Integer getInt(int index);
        Date getDate(int index);
        Boolean getBoolean(int index);
    }
}
