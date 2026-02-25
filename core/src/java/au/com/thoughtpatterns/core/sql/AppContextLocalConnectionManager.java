package au.com.thoughtpatterns.core.sql;

/**
 * A ConnectionManager that is wired to use AppContext to track connections
 * and a simple connection pool.
 * 
 * This class is hard to test - it's easier to test the superclass because
 * the pool and tracker can be mocked out. 
 */
public class AppContextLocalConnectionManager extends LocalConnectionManager {

    private static final ConnectionPool pool = new TrivialConnectionPool();
    private static final AppContextConnectionTracker tracker = new AppContextConnectionTracker();
        
    public AppContextLocalConnectionManager() {
        setConnectionPool(pool);
        setConnectionTracker(tracker);
    }
    
}
