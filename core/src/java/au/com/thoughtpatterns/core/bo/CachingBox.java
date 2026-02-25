package au.com.thoughtpatterns.core.bo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.thoughtpatterns.core.bo.Query.Row;
import au.com.thoughtpatterns.core.util.BusinessDate;

/**
 * Implements the caching and notification mechanisms needed for a Box
 * implementation.
 */
public abstract class CachingBox implements Box {

    private static final long serialVersionUID = 1L;

    private Map<BOKey<?>, IEntity> cache = new HashMap<BOKey<?>, IEntity>();

    private Set<IEntity> dirty = new HashSet<IEntity>();

    private Set<IEntity> added = new HashSet<IEntity>();

    private List<IEntity> deleted = new ArrayList<IEntity>();
    private Set<BOKey<?>> deletedKeys = new HashSet<BOKey<?>>();

    private long tempIdCounter = 1;

    private BusinessObjectListener listener = new Listener();

    private List<BoxListener> boxListeners = new ArrayList<BoxListener>();

    private List<BusinessObjectListener> boListeners = new ArrayList<BusinessObjectListener>();

    private IssueBox issueBox = new DefaultIssueBox();

    private BusinessDate today = BusinessDate.newSystemToday();

    private Date now = new Date();
    
    /**
     * Controls whether the box is in "read only" mode or not.
     */
    private boolean readOnly;
    
    /**
     * Controls whether the optimistic lock is enabled or not.
     */
    private boolean optimisticLock = true;
    
    /**
     * Controls whether the optimistic lock is used on writes or not.
     */
    private boolean opLockOnWrite = true;
    
    /**
     * The primary key of the current "activity".
     */
    private Long activityKey;

    public <T extends IEntity> BOKey<T> createKey(Class<T> clas, Long id) {
        return BOKeyFactory.createKey(clas, id);
    }

    public CachingBox() {
        super();
    }

    public void add(IEntity object) {

        if (getReadOnly()) {
            throw new ReadOnlyBoxException();
        }
        
        BOKey<?> key = object.getBOKey();
        if (key != null && cache.containsKey(key)) {
            // Already present - do nothing
            return;
        }

        if (key == null) {
            key = createNewKey(object);
            object.setBOKey(key);
        }

        added.add(object);
        object.setBox(this);

        // Flush straight away, so that primary keys are allocated if we
        // are using a Box that allocates keys when flushing
        flush();
        cache.put(key, object);

        object.addListener(listener);

        fireAdded(object);
    }

    public <T extends IEntity> T load(BOKey<T> key) {
        if (key == null) {
            return null;
        }
        
        if (deletedKeys.contains(key)) {
            return null;
        }
        
        IEntity raw = cache.get(key);
        T object = (T) raw; // Generics cast
        if (object != null) {
            return object;
        }

        // Do a raw load
        object = loadInt(key);

        if (object == null) {
            return object;
        }

        discover(key, object);

        return object;
    }
    
    protected boolean isLoaded(BOKey<? extends IEntity> key) {
        if (deletedKeys.contains(key)) {
            return true;
        }
        
        IEntity object = cache.get(key);
        if (object != null) {
            return true;
        }
        return false;
    }

    protected <T extends IEntity> T discover(BOKey<T> key, T object) {
        // We may already have the object, in which case we return the already loaded version
        if (isLoaded(key)) {
            return load(key);
        }
        
        checkVersion(key, object);

        Long loadedVersion = object.getActivityId();
        object.setLoadedActivityId(loadedVersion);

        // Add to cache, wire with listeners, etc. Use copy of key since keys are mutable
        BOKey<?> copy = key.copy();
        cache.put(copy, object);
        object.setBox(this);
        object.addListener(listener);

        fireLoaded(object);
        
        return object;
    }

    protected <T extends IEntity> void checkVersion(BOKey<T> key, T object) {
        
        // Check optimistic lock
        BOKeyImpl<T> keyImpl = (BOKeyImpl<T>) key;
        Long version = keyImpl.getActivityId();
        Long loadedVersion = object.getActivityId();
        if (version != null && !version.equals(loadedVersion) && optimisticLock) {
            throw new VersionException();
        }

    }
    
    protected abstract <T extends IEntity> T loadInt(BOKey<T> key);
    
    public <T extends IEntity> List<T> preload(List<BOKey<T>> keys) {
        // COULDDO improve this cheap algorithm!
        List<T> loaded = new ArrayList<T>();
        for (BOKey<? extends IEntity> key : keys) {
            Object x = load(key);
            if (x != null) {
                loaded.add((T)x);
            }
        }
        return loaded;
    }
    
    public <T extends IEntity> List<T> preload(Query query, Class<T> clas) {
        List<Row> rows = query.execute();
        List<BOKey<T>> keys = new ArrayList<BOKey<T>>();
        
        for (Row row: rows) {
            Long id = (Long) row.getValue(0);
            if (id != null) {
                BOKey<T> key = createKey(clas, id);
                keys.add(key);
            }
        }
        
        preload(keys);
        
        List<T> loaded = new ArrayList<T>();
        for (BOKey<T> key: keys) {
            T obj = load(key);
            if (obj != null) {
                loaded.add(obj);
            }
        }
        return loaded;
    }

    public void delete(IEntity object) {
        if (object == null) {
            return;
        }
        
        if (getReadOnly()) {
            throw new ReadOnlyBoxException();
        }
        
        cache.remove(object.getBOKey());
        if (deleted.contains(object)) {
            return;
        }
        if (added.contains(object)) {
            added.remove(object);
        } else {
            dirty.remove(object);
            deleted.add(object);
            deletedKeys.add(object.getBOKey());
        }
        // Stop listening
        object.removeListener(listener);
        fireDeleted(object);
    }

    public abstract void flush();

    /**
     * "Forgets" an attached object. Any box-related listeners will be removed
     * from the object, and the box will forget that it ever loaded the object.
     * 
     * This operation is not intended to be used by clients. It is for use
     * within the framework.
     */
    public void detach(IEntity object) {
        if (object == null) {
            return;
        }
        object.removeListener(listener);
        dirty.remove(object);
        added.remove(object);
        deleted.remove(object);
        deletedKeys.remove(object.getBOKey());
        cache.remove(object.getBOKey());
        object.setBox(null);
    }

    public <T extends IEntity> BOKey<T> createNewKey(T object) {
        Class<T> clas = object.getEntityClass();
        return createNewKey(clas);
    }

    public <T extends IEntity> BOKey<T> createNewKey(Class<T> entityClass) {
        BOKeyImpl<T> key = new BOKeyImpl<T>(entityClass, null);

        long tempKey = ++tempIdCounter;
        key.setTemporaryId(tempKey);
        return key;
    }
    
    private class Listener implements BusinessObjectListener {

        private static final long serialVersionUID = 1L;

        public void changed(BusinessObject object) {
            // We only listen to PersistentObjects
            if (getReadOnly()) {
                throw new ReadOnlyBoxException();
            }
            
            IEntity node = (IEntity) object;
            if (!added.contains(node) && !deleted.contains(node)) {
                dirty.add(node);
            }

            propagateChanged(object);
        }

    }

    private void propagateChanged(BusinessObject changed) {
        for (BusinessObjectListener l : boListeners) {
            l.changed(changed);
        }
    }

    public void addBoxListener(BoxListener l) {
        if (!boxListeners.contains(l)) {
            boxListeners.add(l);
        }
    }

    public void removeBoxListener(BoxListener l) {
        boxListeners.remove(l);
    }

    public void addBoListener(BusinessObjectListener l) {
        if (!boListeners.contains(l)) {
            boListeners.add(l);
        }
    }

    public void removeBoListener(BusinessObjectListener l) {
        boListeners.remove(l);
    }

    static interface Fire {

        void fire(BoxListener l);
    }

    protected void fire(Fire fire) {
        
        // Copy needed to avoid concurrent modification exception
        List<BoxListener> copy = new ArrayList<BoxListener>(boxListeners);
        
        for (BoxListener l : copy) {
            fire.fire(l);
        }
    }

    protected void fireAdded(final IEntity object) {
        fire(new Fire() {

            public void fire(BoxListener l) {
                l.added(object);
            }
        });
    }

    protected void fireDeleted(final IEntity object) {
        fire(new Fire() {

            public void fire(BoxListener l) {
                l.deleted(object);
            }
        });
    }

    protected void fireLoaded(final IEntity object) {
        fire(new Fire() {

            public void fire(BoxListener l) {
                l.loaded(object);
            }
        });
    }

    protected Collection<IEntity> getDirty() {
        return dirty;
    }

    protected Collection<IEntity> getAdded() {
        return added;
    }

    protected Collection<IEntity> getDeleted() {
        return deleted;
    }

    protected Collection<BOKey<? extends IEntity>> getDeletedKeys() {
        return deletedKeys;
    }

    public void join(OneToMany<? extends IEntity> r) {
        // Provide the relationship with all changed events since the last
        // flush.

        for (IEntity changed : dirty) {
            r.changed(changed);
        }

        // And now become a box listener
        addBoxListener(r);
        addBoListener(r);
    }

    public void setActivityId(Long id) {
        activityKey = id;
    }

    public Long getActivityId() {
        return activityKey;
    }

    public IssueBox getIssueBox() {
        return issueBox;
    }

    public BusinessDate getToday() {
        return today;
    }
    
    public void setToday(BusinessDate aDate) {
        today = aDate;
    }
    
    public Date getNowTimestamp() {
        return now;
    }
    
    public void setNowTimestamp(Date aNow) {
        now = aNow;
    }
    
    /**
     * Check that fields in the persistent object adhere to size constraints.
     * Add issues to the issue box for each constraint violation, and return 
     * whether the object is okay
     * @param aObj
     * @return true if no size constraints are violated
     */
    public boolean checkSizeConstraints(IManagedObject aObj) {
        return MetadataUtil.checkSizeConstraints(aObj, getIssueBox());
    }
    
    public boolean getOptimisticLock() {
        return optimisticLock;
    }

    
    public void setOptimisticLock(boolean optimisticLock) {
        this.optimisticLock = optimisticLock;
    }
    
    @Override
    public boolean getOptimisticLockOnWrites() {
        return opLockOnWrite;
    }

    @Override
    public void setOptimisticLockOnWrites(boolean lock) {
        opLockOnWrite = lock;
    }

    public boolean getReadOnly() {
        return readOnly;
    }
    
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public <T extends PersistentObject> T resolve(T obj) {
        if (obj == null) {
            return null;
        }
        BOKey<T> key = (BOKey<T>) obj.getBOKey();
        T x = load(key);
        return x;
    }    
    
}
