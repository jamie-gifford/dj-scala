package au.com.thoughtpatterns.core.sql;

import java.sql.Connection;


public interface ConnectionManager {

    public Connection getConnection();
    
    /**
     * Start a transaction context. This increments the "transaction depth".
     */
    public void startTransaction();
    
    /**
     * Mark the current transaction for rollback.
     */
    public void setRollbackOnly();
    
    /**
     * Finish the transaction. If the total transaction depth decreases to zero 
     * after this, actually commit or rollback the transaction.
     */
    public void endTransaction();
    
    /**
     * Get the current depth of the transaction (incremented each time startTransaction
     * is called and decremented each time endTransaction is called)s
     * @return
     */
    public int getTransactionDepth();

}
