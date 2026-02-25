package au.com.thoughtpatterns.core.sql;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import au.com.thoughtpatterns.core.sql.LocalConnectionManager.LocalConnection;

/**
 * Tests for the LocalConnectionManager, using mock objects for the ConnectionPool
 * and the ConnectionTracker.
 *
 */
public class LocalConnectionManager_Test {

    private LocalConnectionManager manager;
    private MockConnectionPool pool;
    private MockConnectionTracker tracker;
    
    @Test
    public void testOpenClose() throws Exception {
        Connection conn = manager.getConnection();
        
        // need to actually invoke a method to trigger instantiation of underlying connection
        conn.toString();
        
        Assert.assertEquals(1, pool.outstanding);

        conn.close();       
        Assert.assertEquals(0, pool.outstanding);
    }

    @Test
    public void testOpenOpenCloseClose() throws Exception {
        Connection conn = manager.getConnection();
        
        // need to actually invoke a method to trigger instantiation of underlying connection
        conn.toString();

        Assert.assertEquals(1, pool.outstanding);

        Connection conn2 = manager.getConnection();
        Assert.assertEquals(1, pool.outstanding);

        conn.close();       
        Assert.assertEquals(1, pool.outstanding);

        conn2.close();       
        Assert.assertEquals(0, pool.outstanding);
    }
    
    @Test
    public void testOpenStartClose() throws Exception {
        Connection conn = manager.getConnection();
        
        // need to actually invoke a method to trigger instantiation of underlying connection
        conn.toString();

        Assert.assertEquals(1, pool.outstanding);

        manager.startTransaction();
        conn.close();

        // Connection should not be back in the pool - the transaction is 
        // still pending
        Assert.assertEquals(1, pool.outstanding);
    }
    
    @Test 
    public void testStartOpenCloseEnd() throws Exception {
        Assert.assertEquals(0, pool.outstanding);

        manager.startTransaction();
        Assert.assertEquals(0, pool.outstanding); // 0 because of lazy instantiation of connections
        
        Connection conn = manager.getConnection();
        conn.toString(); // trigger instantiation
        Assert.assertEquals(1, pool.outstanding);

        conn.close();
        Assert.assertEquals(1, pool.outstanding);

        manager.endTransaction();
        Assert.assertEquals(0, pool.outstanding);
    }
    
    @Test 
    public void testStartOpenCloseStartEndEnd() throws Exception {

        Assert.assertEquals(0, pool.outstanding);

        manager.startTransaction();
        Assert.assertEquals(0, pool.outstanding); // 0 because of lazy instantiation of connections
        
        Connection conn = manager.getConnection();
        conn.toString(); // trigger instantiation
        Assert.assertEquals(1, pool.outstanding);

        conn.close();
        Assert.assertEquals(1, pool.outstanding);

        manager.startTransaction();
        Assert.assertEquals(1, pool.outstanding);

        manager.endTransaction();
        Assert.assertEquals(1, pool.outstanding);

        manager.endTransaction();
        Assert.assertEquals(0, pool.outstanding);

    }

    // ---------------------------------
    // Fixtures
    
    @Before
    public void setUp() {
        manager = new LocalConnectionManager();
        pool = new MockConnectionPool();
        tracker = new MockConnectionTracker();       
        
        manager.setConnectionPool(pool);
        manager.setConnectionTracker(tracker);
    }
    
    // ------------------------
    // Helper classes
    
    public class MockConnectionPool implements ConnectionPool {

        public int outstanding;
        
        public Connection getConnection(String label) {
            
            outstanding ++;
            
            ClassLoader loader = getClass().getClassLoader();
            Class[] interfaces = new Class[] { Connection.class };
            InvocationHandler h = new MockConnectionHandler();
            Connection conn = (Connection) Proxy.newProxyInstance(loader, interfaces, h);
            return conn;
        }

        public void returnConnection(Connection connection) {
            outstanding --;
        }

        @Override
        public int getOutstandingConnections() {
            return outstanding;
        }
        
    }
    
    public class MockConnectionHandler implements InvocationHandler {

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
        }
        
    }
    
    public class MockConnectionTracker implements ConnectionTracker<LocalConnection> {

        LocalConnection current;
        
        public LocalConnection get() {
            return current;
        }

        public void remove() {
            current = null;
        }

        public void set(LocalConnection transaction) {
            current = transaction;
        }
        
    }
    
}
