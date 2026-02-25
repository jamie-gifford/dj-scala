package au.com.thoughtpatterns.core.util;

import java.util.WeakHashMap;

import au.com.thoughtpatterns.core.security.CredentialsManager;

/**
 * A generalisation of ThreadLocal or singleton patterns.
 * 
 * In applications which use a single thread to service a particular client,
 * this class acts like a ThreadLocal. In applications which have a more complex
 * thread model (like Swing applications), this class provides a "single client" view.
 *
 * Use this class instead of ThreadLocal.
 */
public class AppContext<T> {

    private static final Logger log = Logger.get(AppContext.class);
    
    /**
     * Map from threads to keys into the AppContext map
     */
    private static ThreadLocal<Object> threadLocal = new ThreadLocal<Object>();
    
    /** 
     * Map context keys (by default, threads) to context objects
     */
    private WeakHashMap<Object, T> appContexts = new WeakHashMap<Object, T>();
    
    protected T initialValue() {
        return null;
    }
    
    public T get() {
        Object key = getKey();
        T t = appContexts.get(key);
        if (t == null) {
            t = initialValue();
            appContexts.put(key, t);
        }
        return t;
    }
    
    public void set(T t) {
        Object key = getKey();
        appContexts.put(key, t);
    }
    
    public void remove() {
        Object key = getKey();
        appContexts.remove(key);   
    }
    
    private static Object getKey() {
        Object key = threadLocal.get();
        if (key == null) {
            // Default key is the thread itself
            key = Thread.currentThread();
            threadLocal.set(key);
        }
        return key;
    }

    /**
     * Change the AppContext to a context identified by the given key.
     * This is intended for use in tests, where it may be necessary to
     * simulate what would normally be a multi-context situation. 
     * It is not supposed to be used 
     * in production environments.
     * 
     * @param newKey
     * @return the previous key
     */
    public static Object changeContext(Object newKey) {
        Object oldKey = getKey();
        threadLocal.set(newKey);
        
        log.info("Changed context from " + oldKey + " to " + newKey);
        CredentialsManager.getInstance().configure();
        
        return oldKey;
    }
    
    public static Object getCurrentKey() {
        return getKey();
    }

}
