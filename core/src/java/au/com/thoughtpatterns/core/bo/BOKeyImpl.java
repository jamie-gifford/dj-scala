package au.com.thoughtpatterns.core.bo;

public class BOKeyImpl<T extends IEntity> implements BOKey<T> {

    private static final long serialVersionUID = 1L;

    private ClassHolder<T> classHolder;
    
    /**
     * The persistent id (ie, primary key). May be null in cases where the final 
     * primary key has not been allocated.
     * 
     * Note that subclasses may override the getId and setId method to store the id
     * elsewhere.
     */
    private Long myId;
    
    /**
     * A temporary id used if an object is created and we don't 
     * want to actively allocate a persistent primary key.
     */
    private long tempId;
    
    /**
     * Optimistic lock version id
     */
    private Long activityId;
    
    public BOKeyImpl(Class<T> aClas, Long aId) {
        myId = aId;
        classHolder = ClassHolder.get(aClas);
    }
    
    public BOKeyImpl() {
        
    }

    /**
     * Load state from other BOKeyImpl. This respects the possibility that the id is stored
     * in some other attribute, by using the getId and setId methods.
     */
    public void load(BOKeyImpl<T> other) {
        if (other != null) {
            tempId = other.tempId;
            activityId = other.activityId;
            setId(other.getId());
            classHolder = other.classHolder;
        } else {
            activityId = null;
            setId(null);
        }
    }
    
    public BOKey<T> copy() {
        BOKeyImpl<T> copy = new BOKeyImpl<T>(getEntityClass(), getId());
        copy.tempId = tempId;
        copy.activityId = activityId;
        return copy;
    }

    public Long getId() {
        return myId;
    }

    /**
     * Clients are not expected to use this method.
     * It is for use in the framework only.
     */
    public void setId(Long aId) {
        myId = aId;
    }

    public Class<T> getEntityClass() {
        return classHolder.getContainedClass();
    }
    
    /**
     * Clients are not expected to use this method.
     * It is for use in the framework only.
     */
    public void setEntityClass(Class<T> aClass) {
        classHolder = ClassHolder.get(aClass);
    }

    void setTemporaryId(long aTempId) {
        tempId = aTempId;
    }
    
    public BOKey<T> unversion() {
        activityId = null;
        return this;
    }
    
    public BOKey<T> version(Long aActivityId) {
        activityId = aActivityId;
        return this;
    }
    
    public Long getVersion() {
        return activityId;
    }
    
    public boolean equals(Object obj) {
        if (! (obj instanceof BOKeyImpl)) {
            return false;
        }
        
        BOKeyImpl<T> other = (BOKeyImpl<T>) obj;

        if (!(classHolder.getContainedClass() == other.classHolder.getContainedClass())) {
            return false;
        }

        Long id = getId();
        Long otherId = other.getId();
        
        if (id == null) {
            if (otherId != null) {
                return false;
            }
            // tempId 0 is special - like null
            return tempId == other.tempId && tempId != 0;
        }
        
        return (id.equals(otherId));
    }
    
    public int hashCode() {
        Long id = getId();
        return (int) (id != null ? id : tempId);
    }

    public boolean isPersistent() {
        Long id = getId();
        return id != null;
    }
    
    public void setActivityId(Long id) {
        activityId = id;
    }
    
    public Long getActivityId() {
        return activityId;
    }
    
    public String toString() {
        return "BOKey[" + classHolder.getContainedClassName() + "][" + getId() + "]";
    }

    public BOKey<?> adapt(Class<? extends IEntity> entityClass) {
        BOKeyImpl<T> copy = (BOKeyImpl<T>) copy();
        copy.setEntityClass((Class<T>)entityClass); // Generics cast
        return copy;
    }
    
}
