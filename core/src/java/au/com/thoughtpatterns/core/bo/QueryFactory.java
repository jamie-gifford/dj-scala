package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import au.com.thoughtpatterns.core.bo.Query.Row;
import au.com.thoughtpatterns.core.lookup.Lookup;
import au.com.thoughtpatterns.core.sql.Connections;
import au.com.thoughtpatterns.core.util.BusinessDate;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.ProfilePoint;
import au.com.thoughtpatterns.core.util.SystemException;
import au.com.thoughtpatterns.core.util.Util;

/**
 * Create a Query object.
 * @see Query
 */
public class QueryFactory {

    public static Query create(String sql) {
        return new QueryImpl(sql);
    }
    
    /**
     * Create a string with n question marks separated by commas. As a special case, if n == 0, return "-1". 
     * This is intended for "id in ( ?, ?, ... )" style SQL queries
     * @param size
     * @return question-mark string
     */
    public static String qmarks(int n) {
        if (n == 0) {
            return "-1";
        }
        List<String> qs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            qs.add("?");
        }
        return Util.join(",", qs);
    }

    public static Object toJdbcType(Object in) {

        if (in instanceof PersistentObject) {
            PersistentObject po = (PersistentObject) in;
            BOKeyImpl key = (BOKeyImpl) po.getBOKey();
            Long id = key.getId();
            return id;
        }
        if (in instanceof BOKey) {
            BOKeyImpl key = (BOKeyImpl) in;
            Long id = key.getId();
            return id;
        }
        if (in instanceof Lookup) {
            String code = ((Lookup)in).getCode();
            return code;
        }
        if (in instanceof BusinessDate) {
            BusinessDate bd = (BusinessDate) in;
            in = bd.toSqlDate();
        }
        if (in instanceof Date && !(in instanceof java.sql.Date)) {
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(((Date)in).getTime());
            in = sqlDate;
        }
        
        return in;
    }
    

}

// TODO worry about SQL type mapping.
class QueryImpl implements Query, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.get(QueryImpl.class);
    private static final int SQL_TRUNCATE_LENGTH = 80;
    
    private String sql;

    private List<Object> parameters = new ArrayList<Object>();

    private int firstRow;

    private int maxRows;

    QueryImpl(String aSql) {
        sql = aSql;
    }

    public int setNextValue(Object value) {
        value = convertToJdbcType(value);
        parameters.add(value);
        return parameters.size();
    }

    public void setIndexedValue(int index, Object value) {
        value = convertToJdbcType(value);
        while (parameters.size() < index) {
            parameters.add(null);
        }
        parameters.set(index - 1, value);
    }

    private Object convertToJdbcType(Object in) {
        return QueryFactory.toJdbcType(in);
    }
    
    public void setFirstRow(int aFirstRow) {
        firstRow = aFirstRow;
    }

    public void setMaxRows(int aMaxRows) {
        maxRows = aMaxRows;
    }

    // COULDDO make boundary-aware
    public List<Row> execute() {

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        List<Row> rows = new ArrayList<Row>();

        log.debug("Executing sql " + sql);
        
        int length = sql.length();
        if (length > SQL_TRUNCATE_LENGTH) {
            length = SQL_TRUNCATE_LENGTH;
        }
        String subsql = sql.substring(0, length);
        
        ProfilePoint point = new ProfilePoint("sql", subsql);
        
        try {
            point.start();
            
            connection = Connections.getConnection();

            stmt = connection.prepareStatement(sql);
            
            for (int i = 0; i < parameters.size(); i++) {
                Object value = parameters.get(i);
                stmt.setObject(i + 1, value);
            }
            
            boolean resultType = stmt.execute();
            
            if (!resultType) {
                // Must have been an update, so return a single
                // row with a single int value, the number of updated rows
                int updated = stmt.getUpdateCount();
                QueryRow row = new QueryRow();
                row.addValue(updated);
                rows.add(row);
                return rows;
            }
            
            rs = stmt.getResultSet(); 
            
            ResultSetMetaData meta = stmt.getMetaData();
            int cols = meta.getColumnCount();
            
            // Spin past first rows
            boolean moreRows = true;
            for (int i = 0; i < firstRow && moreRows; i++) {
                moreRows = rs.next();
            }
            
            if (! moreRows) {
                // Return empty rows
                return rows;
            }
            
            if (maxRows == 0) {
                maxRows = Integer.MAX_VALUE;
            }
            
            for (int i = 0; i < maxRows && moreRows; i++) {
             
                if (rs.next()) {
                    
                    QueryRow row = new QueryRow();
                    for (int j = 0; j < cols; j++) {
                        Object value = rs.getObject(j + 1);
                        
                        // TODO worry about SQL type mapping.
                        row.addValue(value);
                    }
                    rows.add(row);
                    
                } else {
                    moreRows = false;
                }
                
            }

            return rows;
            
        } catch (SQLException ex) {
            throw new SystemException(ex);
        } finally {
            tidy(connection, stmt, rs);
            point.stop();
        }
    }

    public Object executeSingle() {
        List<Row> rows = execute();
        if (rows.size() == 0) {
            return null;
        }
        return rows.get(0).getValue(0);
    }

    public Integer executeSingleInt() {
        List<Row> rows = execute();
        if (rows.size() == 0) {
            return null;
        }
        return rows.get(0).getInt(0); 
    }

    public Long executeSingleLong() {
        List<Row> rows = execute();
        if (rows.size() == 0) {
            return null;
        }
        return rows.get(0).getLong(0); 
    }

    private void tidy(Connection c, Statement s, ResultSet rs) {
        try {
            try {
                if (rs != null) {
                    rs.close();
                }
            } finally {
                try {
                    if (s != null) {
                        s.close();
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        } catch (SQLException ex) {
            throw new SystemException(ex);
        }
    }

}

class QueryRow implements Row {

    private static final long serialVersionUID = 1L;
    private List<Object> list = new ArrayList<Object>();
    
    public Object getValue(int index) {
        return list.get(index);
    }
    
    public void addValue(Object value) {
        list.add(value);
    }

    public BusinessDate getBusinessDate(int index) {
        // JDBC Drivers will return java.util.Date, not BusinessDate
        Date date = (Date) getValue(index);
        BusinessDate b = BusinessDate.newDate(date);
        return b;
    }

    public Long getLong(int index) {
        Number l = getNumber(index);
        if (l == null) {
            return null;
        }
        return l.longValue();
    }

    public Integer getInt(int index) {
        Number l = getNumber(index);
        if (l == null) {
            return null;
        }
        return l.intValue();
    }

    public Number getNumber(int index) {
        return (Number) getValue(index);
    }
    
    public String getString(int index) {
        return (String) getValue(index);
    }

    public Date getDate(int index) {
        Date date = (Date) getValue(index);
        return date;
    }
    
    public Boolean getBoolean(int index) {
        Object o = getValue(index);
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        if (o instanceof Number) {
            return ((Number)o).intValue() != 0;
        }
        return false;
    }
    
}