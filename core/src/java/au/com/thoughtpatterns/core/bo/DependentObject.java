package au.com.thoughtpatterns.core.bo;

/**
 * A dependent object can be used to model collection-valued attributes 
 * of PersistentObjects (or of other dependent objects).
 */
public class DependentObject extends ManagedObject implements IDependentObject {

    private static final long serialVersionUID = 1L;

    private IManagedObject parent;
    
    private Long persistentId;
    
    @Override
    public IManagedObject getParent() {
        return parent;
    }
    
    public void setParent(IManagedObject aParent) {
        parent = aParent;
    }

    @Override
    public Long getId() {
        return persistentId;
    }

    @Override
    public void setId(Long aId) {
        persistentId = aId;
    }

    public Long getParentId() {
        return ( parent != null ? parent.getId() : null );
    }
    
    public void setParentId(Long aId) {
        // NOP - you can't set the parent id this way.
        // This method is only here for ibatis compatibility.
    }
}
