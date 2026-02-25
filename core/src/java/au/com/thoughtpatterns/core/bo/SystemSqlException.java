package au.com.thoughtpatterns.core.bo;

import java.sql.SQLException;

import au.com.thoughtpatterns.core.util.SystemException;

/**
 * Wraps an SQL exception as an unchecked exception
 */
public class SystemSqlException extends SystemException {

    private static final long serialVersionUID = 1L;

    public SystemSqlException(SQLException ex) {
        super(ex);
    }
    
}
