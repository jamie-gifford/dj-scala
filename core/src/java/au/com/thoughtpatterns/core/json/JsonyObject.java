package au.com.thoughtpatterns.core.json;

import java.util.Set;


public interface JsonyObject extends Jsony {

    // -------------------------
    // Standard interface
    
    Jsony get(String property);
    
    void set(String property, Jsony val);
    
    void set(String property, String val);
    void set(String property, Integer val);
    void set(String property, Long val);
    void set(String property, Double val);
    void set(String property, Boolean val);
    
    void delete(String property);

    Set<String> getPropertyNames();
    
    // ------------------------
    // Auto-casting interface
    
    <T> T getCast(String property, Class<T> cls);
    
    /**
     * Returns null if cast is not possible, instead of throwing exception.
     */
    <T> T getCast0(String property, Class<T> cls);
    
    
}
