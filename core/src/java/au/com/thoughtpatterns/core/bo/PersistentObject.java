package au.com.thoughtpatterns.core.bo;

public class PersistentObject extends ManagedObject implements IEntity {

    private static final long serialVersionUID = 1L;
    private BOKeyImpl<?> bokey = new BOKeyImpl(); // Generics cast
    private Box box;
    
    public PersistentObject() {
        Class entityClass = getEntityClass(); // Generics cast
        bokey.setEntityClass(entityClass);
    }
    
    /**
     * Return the BOKey of this PersistentObject. May return null in the case
     * of an object that has not been added to the database.
     * @return the BOKey or null
     */
    public BOKey<? extends IEntity> getBOKey() {
        if (bokey != null) {
            bokey.setActivityId(getActivityId());
        }
        return bokey;
    }
    
    public void setBOKey(BOKey<?> aKey) {
        bokey = (BOKeyImpl<?>) aKey;
    }
    
//    /**
//     * Return the entity class of this persistent object. By default that is the 
//     * highest superclass that extends PersistentObject and is a "Manager"
//     * Subclasses may override if they have use a different taxonomy (for example, based 
//     * on interfaces). 
//     */
//    public Class<? extends PersistentObject> getEntityClass() {
//        Class clas = getClass();
//        while (true) {
//
//            Class sooper = clas.getSuperclass();
//            
//            if (PersistentObject.class == sooper || ! Manager.class.isAssignableFrom(sooper)) {
//                // We've got to the to
//                return (Class<? extends PersistentObject>) clas;
//            }
//            clas = sooper;
//        }
//    }

    public Long getId() {
        BOKeyImpl<?> key = (BOKeyImpl<?>) getBOKey();
        return ( key != null ? key.getId() : null );
    }
    
    public void setId(Long id) {
        BOKeyImpl<?> key = (BOKeyImpl<?>) getBOKey();
        key.setId(id);
    }

    @Override
    public ManagedObject getParent() {
        return this;
    }
    
    public Box getBox() {
        return box;
    }
    
    /**
     * Clients are not expected to use this method. It is for framework use.
     */
    public void setBox(Box aBox) {
        box = aBox;
    }
    
    public IEntity getOwningObject() {
        return this;
    }
    
    /**
     * Transfers the identity of one business object to another.
     * If the given business object has no identity, do nothing
     * @param other
     */
    public void loadIdentity(PersistentObject other) {
        Long identity = other.getId();
        if (identity != null) {
            setId(identity);
        }
    }
}
