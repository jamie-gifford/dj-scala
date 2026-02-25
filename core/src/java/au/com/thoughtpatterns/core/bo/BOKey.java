package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;

/**
 * A wrapper for the primary key of a business object.
 * A BOKey may be versioned or unversioned.
 */
public interface BOKey<T extends IEntity> extends Serializable {

    /**
     * Get the underlying (persistent) id of the key
     */
    public Long getId();
    
    /**
     * Forget any versioning information contained in the key. Returns self.
     */
    public BOKey<T> unversion();
    
    /**
     * Add versioning information. Returns self
     */
    public BOKey<T> version(Long actitityId);
    
    public Long getVersion(); 
    
    /**
     * Return true if this key represents a persisted object (ie, the 
     * primary key has been assigned)
     */
    public boolean isPersistent();
    
    /**
     * Create a copy of the key
     */
    public BOKey<T> copy();
    
    /**
     * Adapt the key to a given entity class. This method is not usually used by clients. 
     */
    // public BOKey<T> adapt(Class<T> entityClass);
    public BOKey<? extends IEntity> adapt(Class<? extends IEntity> entityClass);
}
