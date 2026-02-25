package au.com.thoughtpatterns.core.bo.ibatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import au.com.thoughtpatterns.core.util.BusinessDate;

import com.ibatis.sqlmap.engine.type.BaseTypeHandler;
import com.ibatis.sqlmap.engine.type.SimpleDateFormatter;
import com.ibatis.sqlmap.engine.type.TypeHandler;

/**
 * BusinessDate implementation of TypeHandler
 */
public class BusinessDateTypeHandler extends BaseTypeHandler implements
        TypeHandler {

    private static final String DATE_FORMAT = "yyyy/MM/dd";

    public void setParameter(PreparedStatement ps, int i, Object parameter,
            String jdbcType) throws SQLException {
        ps.setDate(i, ((BusinessDate)parameter).toSqlDate());
    }

    public Object getResult(ResultSet rs, String columnName)
            throws SQLException {
        java.sql.Date sqlDate = rs.getDate(columnName);
        if (rs.wasNull()) {
            return null;
        } else {
            return BusinessDate.newSqlDate(sqlDate);
        }
    }

    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
        java.sql.Date sqlDate = rs.getDate(columnIndex);
        if (rs.wasNull()) {
            return null;
        } else {
            return BusinessDate.newSqlDate(sqlDate);
        }
    }

    public Object getResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        java.sql.Date sqlDate = cs.getDate(columnIndex);
        if (cs.wasNull()) {
            return null;
        } else {
            return BusinessDate.newSqlDate(sqlDate);
        }
    }

    public Object valueOf(String s) {
        return SimpleDateFormatter.format(DATE_FORMAT, s);
    }

}
