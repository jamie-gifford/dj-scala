package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;

import au.com.thoughtpatterns.core.util.Util;

/**
 * Represents a reference from one entity to a PersistentObject.
 */
public class Relationship<T extends IEntity> implements Serializable, RelationshipLinked {

    private static final long serialVersionUID = 1L;

    private ClassHolder<T> classHolder;
    
    private IManagedObject owner;
    
    private BOKeyImpl<T> foreignKey;

    private ForeignKey fk;
    
    public Relationship(IManagedObject aOwner, Class<T> aEntityClass) {
        classHolder = ClassHolder.get(aEntityClass);
        owner = aOwner;
    }

    public Relationship(IManagedObject aOwner, Class<T> aEntityClass, ForeignKey aFk) {
        classHolder = ClassHolder.get(aEntityClass);
        owner = aOwner;
        fk = aFk;
        foreignKey = new FOKey(fk);
        foreignKey.setEntityClass(aEntityClass);
        fk.setRelationship(this);
    }

    static class FOKey<T extends PersistentObject> extends BOKeyImpl<T> {
        private static final long serialVersionUID = 1L;
        private ForeignKey fk;
        
        FOKey(ForeignKey aFk) {
            fk = aFk;
        }
        
        @Override public Long getId() {
            return fk.getId();
        }

        @Override public void setId(Long id) {
            fk.setId(id);
        }
    }
    
    public T get() {
        BOKey<T> key = getForeignKey();
        if (key == null) {
            return null;
        }
        
        IEntity entity = owner.getOwningObject();
        Box box = entity.getBox();
        T endpoint = (T) box.load(key);
        return endpoint;
    }
    
    public void set(T aEndpoint) {
        BOKey<T> aKey = (BOKey<T>) ( aEndpoint != null ? aEndpoint.getBOKey() : null ); // Generics cast
        setForeignKey(aKey);
    }
    
    public BOKey<T> getForeignKey() {
        return foreignKey;
    }
    
    public void setForeignKey(BOKey<T> aKey) {
        if (Util.equals(foreignKey, aKey)) {
            return;
        }
        if (foreignKey != null) {
            // Don't blow away the fk, rather, just update the id
            foreignKey.load((BOKeyImpl<T>)aKey);
        } else {
            foreignKey = (BOKeyImpl<T>) aKey;
        }
        IEntity entity = owner.getOwningObject();
        entity.fireChanged();
    }
    
    public Class<T> getEntityClass() {
        return classHolder.getContainedClass();
    }
    
    public Relationship<T> getRelationship() {
        return this;
    }

    // ----------------------------------
    // These methods are not intended to be used by clients.
    // They are for framework use only (mainly to accommodate jdbc mapping)
    
    public Long getId() {
        BOKey<T> key = getForeignKey();
        BOKeyImpl<T> keyImpl = (BOKeyImpl<T>) key;
        return ( keyImpl != null ? keyImpl.getId() : null );
    }
    
    public void setId(Long id) {
        if (id == null) {
            setForeignKey(null);
        } else {
            Class<T> clas = getEntityClass();
            BOKey<T> key = new BOKeyImpl<T>(clas, id);
            setForeignKey(key);
        }
    }
    
}