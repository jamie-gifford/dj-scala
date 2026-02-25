package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;

import au.com.thoughtpatterns.core.util.Util;

/**
 * Simple stub for holding a foreign key
 * @author james
 */
public class ForeignKey implements Serializable, RelationshipLinked {

    private static final long serialVersionUID = 1L;

    private Long id;
    
    /**
     * Optional link to covering Relationship. This is set by the Relationship on construction.
     */
    private Relationship covering;
    
    private PersistentObject owner;
    
//    public ForeignKey() {}
    
    public ForeignKey(PersistentObject aOwner) {
        owner = aOwner;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        if (Util.equals(this.id, id)) {
            return;
        }
        this.id = id;
        if (owner != null) {
            owner.fireChanged();
        }
    }

    public Relationship getRelationship() {
        return covering;
    }
    
    public void setRelationship(Relationship aRel) {
        covering = aRel;
    }
    
    public void setIdentity(ForeignKey other) {
        setId(other != null ? other.getId() : null);
    }
    
    public void setIdentity(BOKey other) {
        BOKeyImpl keyimpl = (BOKeyImpl) other;
        setId(keyimpl != null ? keyimpl.getId() : null);
    }

    public void setIdentity(PersistentObject other) {
        setIdentity(other != null ? other.getBOKey() : null);
    }
    
    public boolean isNull() {
        return id == null;
    }
    
    public <T extends IEntity> BOKey<T> toBOKey(Class<T> clas) {
        if (id == null) {
            return null;
        }
        BOKeyImpl<T> bokey = new BOKeyImpl<T>(clas, id);
        return bokey;
    }
}
