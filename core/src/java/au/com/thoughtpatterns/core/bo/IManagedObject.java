package au.com.thoughtpatterns.core.bo;

import java.util.Set;


public interface IManagedObject extends BusinessObject {

    <T extends IManagedObject> Class<T> getEntityClass();

    void setId(Long aId);
    
    Long getId();
    
    /**
     * Return a READ-ONLY list of dependent lists.
     */
    public Set<DependentList> getDependentLists();

    public IEntity getOwningObject();
    
    Long getActivityId();
    
    void setActivityId(Long aId);
    
    void setLoadedActivityId(Long aId);

    public Long getLoadedActivityId();

}
