package au.com.thoughtpatterns.core.bo.ibatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import au.com.thoughtpatterns.core.util.AusDollar;

import com.ibatis.sqlmap.engine.type.BaseTypeHandler;
import com.ibatis.sqlmap.engine.type.TypeHandler;


public class AusDollarTypeHandler extends BaseTypeHandler implements
        TypeHandler {

    public void setParameter(PreparedStatement ps, int i, Object parameter,
            String jdbcType) throws SQLException {
        AusDollar d = (AusDollar) parameter;
        ps.setInt(i, ( d != null ? d.getCents() : null));
    }

    public Object getResult(ResultSet rs, String columnName)
            throws SQLException {
        Integer cents = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        } else {
            return AusDollar.new_cents(cents);
        }
    }

    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
        Integer cents = rs.getInt(columnIndex);
        if (rs.wasNull()) {
            return null;
        } else {
            return AusDollar.new_cents(cents);
        }
    }

    public Object getResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        Integer cents = cs.getInt(columnIndex);
        if (cs.wasNull()) {
            return null;
        } else {
            return AusDollar.new_cents(cents);
        }
    }

    public Object valueOf(String s) {
        try {
            int cents = (int) Double.parseDouble(s) * 100;
            return AusDollar.new_cents(cents);
        } catch (Exception ex) {
            return null;
        }
    }

}
