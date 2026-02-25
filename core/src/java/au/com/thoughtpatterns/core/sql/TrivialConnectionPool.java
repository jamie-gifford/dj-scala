package au.com.thoughtpatterns.core.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import au.com.thoughtpatterns.core.bo.SystemSqlException;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.Parameters;
import au.com.thoughtpatterns.core.util.SystemException;

/**
 * A "non pooling" connection pool that only works on one datasource.
 */
public class TrivialConnectionPool implements ConnectionPool {

    private static Logger log = Logger.get(TrivialConnectionPool.class);
    
    private int out = 0;
    private int total = 0;
    
    public Connection getConnection(String label) {
        try {
        Parameters params = Parameters.instance();
        String driver = params.get("persistence.local.driver");
        String url = params.get("persistence.local.url");
        String user = params.get("persistence.local.user");
        String passwd = params.get("persistence.local.passwd");
        
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            throw new SystemException(ex);
        }
    
        Connection connection = DriverManager.getConnection(url, user, passwd);
        
        out ++;
        total ++;

        log.debug("getConnection (total " + total + ", out " + out + "): " + url);

        return connection;
        
        } catch (SQLException ex) {
            throw new SystemSqlException(ex);
        }
    }

    public void returnConnection(Connection connection) {
        try {
            connection.close();
            out --;
            log.debug("returnConnection (total " + total + ", out " + out + ")");
        } catch (SQLException ex) {
            throw new SystemSqlException(ex);
        }
    }

    @Override
    public int getOutstandingConnections() {
        return out;
    }
    
    
}
