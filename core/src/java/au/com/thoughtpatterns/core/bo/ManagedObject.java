package au.com.thoughtpatterns.core.bo;

import java.util.HashSet;
import java.util.Set;


public abstract class ManagedObject extends AbstractBusinessObject implements IManagedObject {

    private static final long serialVersionUID = 1L;

    private static PersistentTypeMapper typeMapper = new PersistentTypeMapper();
    
    private Set<DependentList> dependentLists = null; // Lazily constructed 
    
    /**
     * Get the parent object (null if there is no parent).
     */
    public abstract IManagedObject getParent();

    /**
     * Get the owning PersistentObject.
     * @return the owning (parent or ancestor) PersistentObject or null if there
     *  is no owning object
     */
    public IEntity getOwningObject() {
        IManagedObject parent = getParent();
        return parent.getOwningObject();
    }
    
    /**
     * Return a READ-ONLY list of dependent lists.
     */
    public Set<DependentList> getDependentLists() {
        return dependentLists;
    }
    
    public void addDependentList(DependentList list) {
        if (dependentLists == null) {
            dependentLists = new HashSet<DependentList>();
        }
        dependentLists.add(list);
    }
    
    // --------------------------------------
    // Methods for use by persistence frameworks only.
    
    public abstract Long getId();
    
    public abstract void setId(Long aId);
    
    public Class getEntityClass() {
        Class concreteClass = getClass();
        Class entityClass = typeMapper.getPersistentType(concreteClass);
        return entityClass;
    }
    
}
