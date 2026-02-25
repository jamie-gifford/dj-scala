package au.com.thoughtpatterns.core.sql;

import java.sql.Connection;

/**
 * A JDBC connection pool
 */
public interface ConnectionPool {

    /**
     * Get a connection. May block if the pool is empty.
     * @param label a symbol that indices which datasource to get the connection for
     * @return a Connection
     */
    public Connection getConnection(String label);
    
    /**
     * Put a connection back into the pool
     * @param connection
     */
    public void returnConnection(Connection connection);
    
    /**
     * Count number of "out" connections
     * @return number of out connections
     */
    public int getOutstandingConnections();
    
}
