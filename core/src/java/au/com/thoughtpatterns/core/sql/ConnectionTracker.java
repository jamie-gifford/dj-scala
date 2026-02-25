package au.com.thoughtpatterns.core.sql;

/**
 * An interface used by the ConnectionManager to track the current contextual
 * transaction.
 */
public interface ConnectionTracker<T> {

    public void set(T transaction);
    public T get();
    public void remove();
    
}
