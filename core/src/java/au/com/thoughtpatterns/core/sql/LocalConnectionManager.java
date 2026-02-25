package au.com.thoughtpatterns.core.sql;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import au.com.thoughtpatterns.core.bo.SystemSqlException;
import au.com.thoughtpatterns.core.util.AppContext;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.SystemException;

public class LocalConnectionManager implements ConnectionManager {

    private static final Logger log = Logger.get(LocalConnectionManager.class);
    
    private ConnectionPool pool;
    private ConnectionTracker<LocalConnection> established; 

    // ----------------------------------------
    // Configuration methods
    
    public void setConnectionPool(ConnectionPool aPool) {
        pool = aPool;
    }
    
    public void setConnectionTracker(ConnectionTracker<LocalConnection> aTracker) {
        established = aTracker;
    }
    
    // ----------------------------------------
    
    public Connection getConnection() {
        // log.debug("getConnection");
        LocalConnection local = getLocalConnection();
        Connection conn = local.createWrapper();
        return conn;
    }

    public void startTransaction() {
        log.debug("startTransaction");
        LocalConnection local = getLocalConnection();
        local.startTransaction();
    }

    public void setRollbackOnly() {
        log.debug("setRollbackOnly");
        LocalConnection local = getLocalConnection();
        local.setRollbackOnly();        
    }

    public void endTransaction() {
        log.debug("endTransaction");
        LocalConnection local = getLocalConnection();
        local.endTransaction();
    }
    
    @Override
    public int getTransactionDepth() {
        LocalConnection local = established.get();
        return local != null ? local.getTransactionDepth() : 0;
    }
    

    // ---------------------------------------
    

    protected LocalConnection getLocalConnection() {
        LocalConnection local = established.get();
        
        if (local != null) {
            
            log.debug("Reusing existing connection " + local);
            
        } else {
            local = new LocalConnection(); 

            established.set(local);            

            log.debug("Established new connection + " + local);
        }
        return local;
    }
    
    private void finished(LocalConnection local) {
        // Return the localConnection's connection to the pool
        // and remove the reference
        
        Connection wrapped = local.getWrapped0();
        
        if (wrapped != null) {
            pool.returnConnection(wrapped);
        }

        established.remove();
    }
    
    public class LocalConnection {
        
        private Connection wrappedX;
        
        /**
         * The number of non-closed wrapper connections we've handed out that
         * point to the wrapped connection.
         */
        private int references;
        
        /**
         * The number of outstanding transactions.
         */
        private int transactions;
        
        /**
         * Keep track of whether we are rollback only
         */
        private boolean rollbackOnly;
        
        LocalConnection() {
        }
        
        private void createConnection() throws SQLException {
            if (wrappedX == null) {
                wrappedX = pool.getConnection(null);
                if (transactions > 0) {
                    wrappedX.setAutoCommit(false);
                }
            }
        }
        
        Connection getWrapped() throws SQLException {
            createConnection();
            return wrappedX;
        }

        Connection getWrapped0() {
            return wrappedX;
        }

        Connection createWrapper() {
            
            final Class[] INTERFACES = new Class[] { Connection.class };
            final ClassLoader loader = getClass().getClassLoader();
            final ConnectionWrapper handler = new ConnectionWrapper(this);
            
            Connection wrapper = (Connection) Proxy.newProxyInstance(loader, INTERFACES, handler);
            
            // Increment connection reference count
            
            references ++;
            
            return wrapper;
        }
        
        void close() {
            references --;
            
            tidy();
        }

        public void startTransaction() {
            if (transactions == 0 && wrappedX != null) {
                try {
                    wrappedX.setAutoCommit(false);
                } catch (SQLException ex) {
                    throw new SystemSqlException(ex);
                }
            }
            transactions ++;
        }

        public void setRollbackOnly() {
            rollbackOnly = true;
        }
        
        public boolean getRollbackOnly() {
            return rollbackOnly;
        }

        public void endTransaction() {
            transactions --;
            if (transactions == 0 && wrappedX != null) {
                try {
                    if (rollbackOnly) {
                        wrappedX.rollback();
                    } else {
                        wrappedX.commit();
                    }
                    rollbackOnly = false;
                    
                } catch (SQLException ex) {
                    throw new SystemSqlException(ex);
                }
            }
         
            tidy();
        }

        public int getTransactionDepth() {
            return transactions;
        }
        
        private void tidy() {
            if (references == 0 && transactions == 0) {
                // We can discard this LocalConnection
                
                finished(this);
            }
        }
    }
    
    /**
     * Wraps a connection and intercepts the close method
     */
    private static class ConnectionWrapper implements InvocationHandler {

        private LocalConnection local;
        private boolean closed;
        
        ConnectionWrapper(LocalConnection aLocal) {
            local = aLocal;
        }
        
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("close".equals(method.getName())) {
                // Don't really close it, intercept
                if (closed) {
                    throw new SystemException("Close called on a connection that was already closed");
                }
                
                local.close();
                closed = true;
                return null;
                
            } else {
                // Delegate to underlying wrapper
                Connection wrapped = local.getWrapped();
                return method.invoke(wrapped, args);
            }
        }
    }
    
    /**
     * The "normal" ConnectionTracker.
     */
    public static class AppContextConnectionTracker implements ConnectionTracker<LocalConnection> {

        private AppContext<LocalConnection> locals = new AppContext<LocalConnection>();
        
        public LocalConnection get() {
            return locals.get();
        }

        public void remove() {
            locals.remove();
        }

        public void set(LocalConnection transaction) {
            locals.set(transaction);
        }
        
    }
}
