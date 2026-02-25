package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import au.com.thoughtpatterns.core.bo.Query.Row;

/**
 * Represents a One-to-Many relationship. The relationship is normally from another persistent 
 * object (the "owner"), but it may be a "static" One-to-Many relationship, in which case there 
 * is no owner PersistentObject. Instead, the relationship is owned directly by a box.
 */
public abstract class OneToMany<T extends IEntity> 
    implements BusinessObjectListener, BoxListener, Serializable {

    private static final long serialVersionUID = 1L;

    protected List<T> contents;
    
    private ClassHolder<T> classHolder;

    // One of the two must be nonnull
    private IEntity owner;
    private Box ownerBox;
    
    public OneToMany(IEntity aOwner, Class<T> targetClass) {
        owner = aOwner;
        classHolder = ClassHolder.get(targetClass);
    }

    public OneToMany(Box aOwner, Class<T> targetClass) {
        ownerBox = aOwner;
        classHolder = ClassHolder.get(targetClass);
    }

    
    // ----------------------------------------
    // Abstract methods
    
    /**
     * Assuming that the possiblyBelongs method has returned true, 
     * determine whether the candidate actually belongs to the relationship.
     */
    protected abstract boolean belongs(T candidate);

    /**
     * Return a Query that searches for the primary keys of the "baseline"
     * objects of this relationship. 
     */
    protected abstract Query query();

    /**
     * Determine whether the candidate object could possibly belong to the
     * relationship.
     */
    protected boolean possiblyBelongs(IEntity candidate) {
        Class<T> clas = classHolder.getContainedClass();
        if (clas.isAssignableFrom(candidate.getClass())) {
            return true;
        } else {
            return false;
        }
    }
    

    // ----------------------------------------
    
    public IEntity getOwner() {
        return owner;
    }
    
    public Class<T> getTargetClass() {
        return classHolder.getContainedClass();
    }
    
    // ----------------------------------------
    // Main public API
    
    public List<T> get() {
        load();
        return contents;
    }
    
    /**
     * A convenience method to get the first element of the collection.
     * This is useful for the common case of a OneToMany that is constrained
     * to have at most one element
     * 
     * @return the first value, or null if the set of values is empty.
     */
    public T getFirst() {
        List<T> list = get();
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }
    
    // -----------------------------------------
    
    /**
     * Ensure that the contents are loaded
     */
    protected void load() {
        
        if (contents != null) {
            // Already loaded
            return;
        }

        loadInt();
    }
    
    public void refresh() {
        contents = null;
    }
    
    protected Box getBox() {
        Box box = ( owner != null ? owner.getBox() : ownerBox );
        return box;
    }
    
    protected void loadInt() {
        
        contents = new ArrayList<T>();

        if (owner != null) {
            BOKey<T> ownerKey = (BOKey<T>) owner.getBOKey(); // Generics cast
            if (ownerKey == null || !ownerKey.isPersistent()) {
                return;
            }
        }
           
        Query query = query();
        List<Row> rows = query.execute();
        
        Class<T> entityClass = classHolder.getContainedClass();
        List<BOKey<T>> keys = new ArrayList<BOKey<T>>();
        
        Box box = getBox();
        
        for (Row row : rows) {
            long id = (Long) row.getValue(0);
            BOKey<T> key = box.createKey(entityClass, id);
            keys.add(key);
        }
        
        box.preload(keys);
        
        contents.clear();
        
        for (BOKey<T> key : keys) {
            T obj = box.load(key);
            if (obj != null) {
                contents.add(obj);
            }
        }
        
        box.join(this);
    }
        
    // ------------------------------
    // Business object listener
    
    public void changed(BusinessObject object) {
        
        if (! (object instanceof IManagedObject)) {
            // We don't know how to deal with non-managed objects 
            // (and we only listen to managed objects)
            return;
        }
        
        IEntity parent = ((IManagedObject)object).getOwningObject();
        considerLive(parent);
    }
    
    // ------------------------------
    // Box listener
    
    public void added(IEntity object) {
        considerLive(object);        
    }

    public void deleted(IEntity object) {
        considerDead(object);
    }

    public void loaded(IEntity object) {
        considerLive(object);
    }
    
    // -------------------------------
    // General change support
    
    private void considerLive(IEntity object) {
        if (contents == null) {
            return;
        }
        if (! possiblyBelongs(object)) {
            return;
        }
        
        T t = (T) object;
        boolean belongs = belongs(t);
        if (belongs) {
            if (!contents.contains(t)) {
                insert(t);
            }
        } else {
            contents.remove(t);
        }
    }

    private void considerDead(IEntity object) {
        if (contents == null) {
            return;
        }
        if (! possiblyBelongs(object)) {
            return;
        }
        
        T t = (T) object;
        contents.remove(t);
    }

    protected void insert(T object) {
        contents.add(object);
    }

}
