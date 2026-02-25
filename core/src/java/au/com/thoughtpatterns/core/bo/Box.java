package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import au.com.thoughtpatterns.core.util.BusinessDate;

/**
 * Business object lifecyce interface. A Box represents a (long running or short
 * running) transactional view of business data.
 */
public interface Box extends Serializable {

    /**
     * Create an (unversioned) BOKey for a given primary key value.
     * @return BOKey or null if id is null
     */
    <T extends IEntity> BOKey<T> createKey(Class<T> clas, Long id);

    /**
     * Add a PersistentObject to the database. This method can be called on
     * PersistentObjects that are already attached to the Box, in which case it
     * does nothing.
     */
    void add(IEntity object);

    /**
     * Load a PersistentObject by key. May return null if there is no
     * corresponding BusinessObject.
     * 
     * May throw a VersionException if there is an optimistic lock exception.
     * 
     * The Box caches PersistentObject instances and will return the same Java
     * object if the load method is called twice with the same key.
     */
     <T extends IEntity> T load(BOKey<T> key);
     
    /**
     * Preload a series of PersistentObjects by key. The objects will be cached
     * and available via the load method.
     * 
     * May throw a VersionException if there is an optimistic lock exception.
     * 
     * @return a list of loaded objects, in same order as keys.
     * Note that if a key does not resolve to an object, there will be no
     * corresponding object in the returned list (so the two lists can have
     * different lengths). 
     */
    <T extends IEntity> List<T> preload(List<BOKey<T>> keys);

    /**
     * Preload a series of PersistentObjects defined by a query. The objects
     * will be cached and available via the load method.
     * 
     * The query must return rows with a single result, a long value.
     * It will return the loaded objects
     */
    <T extends IEntity> List<T> preload(Query query, Class<T> clas);

    /**
     * Delete a PersistentObject.
     * 
     * May throw a VersionException if there is an optimistic lock exception.
     */
    void delete(IEntity object);

    /**
     * Force a synchronisation to the backing store, if appropriate.
     */
    void flush();

    /**
     * Add a BoxListener to this Box
     */
    void addBoxListener(BoxListener l);

    /**
     * Remove a BoxListener from this Box
     */
    void removeBoxListener(BoxListener l);

    /**
     * Called by a OneToMany relationship when it wants to join the Box
     */
    void join(OneToMany<? extends IEntity> r);

    /**
     * Get the associated IssueBox
     */
    IssueBox getIssueBox();

    /**
     * Get the "business transaction date". Usually this will be the same as the 
     * system date. It will be used for business date calculations. 
     */
    BusinessDate getToday();
    
    /**
     * Set the "transaction time". Normally this is for use by the framework only
     * (or test harnesses). It is not necessary to set the transaction time on
     * a box, since the default value is the system time. 
     */
    public void setToday(BusinessDate now);
    
    /**
     * Get the "transaction system time". Usually it will be the same as system time. 
     */
    Date getNowTimestamp();
    
    void setNowTimestamp(Date now);
    
    /**
     * Return true if this box is using the optimistic lock feature, false otherwise.
     * This can be set by calling {@link #setOptimisticLock}.
     * @return
     */
    public boolean getOptimisticLock();

    /**
     * Configure whether the box uses the optimistic lock feature on reads.
     * @param optimisticLock
     */
    public void setOptimisticLock(boolean optimisticLock);
    
    /**
     * Configure whether the box uses the optimistic lock on writes. 
     */
    public void setOptimisticLockOnWrites(boolean lock);
    
    /**
     * Return true if this box is using the optimistic lock feature on writes, false otherwise.
     */
    public boolean getOptimisticLockOnWrites();
    
    /**
     * Fetch the "read only" status of this box. 
     * A box that is in "read only" mode will throw a ReadOnlyBoxException if
     * you try to modify elements under its control
     * @return whether the box is read only or not.
     */
    public boolean getReadOnly();
    
    public void setReadOnly(boolean isReadOnly);
    
    /**
     * Create a new, unique key suitable for the given entity class. 
     * This method is not normally used. It may be necessary for synchronisingn additions between
     * an offline node and a server node, for instance.
     */
    public <T extends IEntity> BOKey<T> createNewKey(Class<T> entityClass);
    
    /**
     * "Forgets" an attached object. Any box-related listeners will be removed
     * from the object, and the box will forget that it ever loaded the object.
     * 
     * This operation is not intended to be used by clients. It is for use
     * within the framework.
     */
    public void detach(IEntity object);
    
    /**
     * Resolves a persistent object (possibly from another box)
     */
    public <T extends PersistentObject> T resolve(T obj);

    
}
