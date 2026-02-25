package au.com.thoughtpatterns.core.bo.ibatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import au.com.thoughtpatterns.core.lookup.Lookup;
import au.com.thoughtpatterns.core.util.SystemException;

import com.ibatis.sqlmap.engine.type.BaseTypeHandler;
import com.ibatis.sqlmap.engine.type.TypeHandler;


public class LookupTypeHandler extends BaseTypeHandler implements
        TypeHandler {

    public void setParameter(PreparedStatement ps, int i, Object parameter,
            String jdbcType) throws SQLException {

        Lookup l = (Lookup) parameter;
        String code = ( l != null ? l.getCode() : null );
        ps.setString(i, code);
    }

    public Object getResult(ResultSet rs, String columnName)
            throws SQLException {
        throw new SystemException("Operation not implemented");
    }

    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
        throw new SystemException("Operation not implemented");
    }

    public Object getResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        throw new SystemException("Operation not implemented");
    }

    public Object valueOf(String s) {
        return s;
    }

}
