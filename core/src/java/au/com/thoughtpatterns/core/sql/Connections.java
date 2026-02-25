package au.com.thoughtpatterns.core.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import au.com.thoughtpatterns.core.util.Factory;

/**
 * Facade for obtaining database connections and managing transactions.
 * This is for convenience.
 */
public class Connections {

    /**
     * Get a database connection to the default data source.
     * @return a Connection to the default data source, enlisted in the 
     * current transaction.
     */
    public static Connection getConnection() {
        ConnectionManager manager = (ConnectionManager) Factory.create(ConnectionManager.class);
        return manager.getConnection();
    }
    
    /**
     * Start a transaction context. This increments the "transaction depth".
     */
    public static void startTransaction() {
        ConnectionManager manager = (ConnectionManager) Factory.create(ConnectionManager.class);
        manager.startTransaction();
    }
    
    /**
     * Mark the current transaction for rollback.
     */
    public static void setRollbackOnly() {
        ConnectionManager manager = (ConnectionManager) Factory.create(ConnectionManager.class);
        manager.setRollbackOnly();
    }
    
    /**
     * Finish the transaction. If the total transaction depth decreases to zero 
     * after this, actually commit or rollback the transaction.
     */
    public static void endTransaction() {
        ConnectionManager manager = (ConnectionManager) Factory.create(ConnectionManager.class);
        manager.endTransaction();        
    }
    
    /**
     * Get the current depth of the transaction (incremented each time startTransaction
     * is called and decremented each time endTransaction is called)s
     * @return
     */
    public static int getTransactionDepth() {
        ConnectionManager manager = (ConnectionManager) Factory.create(ConnectionManager.class);
        return manager.getTransactionDepth();
    }
    
    
    /**
     * A convenience method for closing a set of database resources safely.
     * The given resources are closed using finally blocks so that even if 
     * some resources throw exceptions on closing, the other resources are still
     * closed.
     * 
     * @param conn if not null, it will be closed.
     * @param st if not null, it will be closed.
     * @param rs if not null, it will be closed.
     * @throws SQLException
     */
    public static void tidy(Connection conn, Statement st, ResultSet rs) throws SQLException {
        try {
            if (rs != null) {
                rs.close();
            }
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        }
    }

}
