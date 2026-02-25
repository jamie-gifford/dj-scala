package au.com.thoughtpatterns.core.bo;


public interface IDependentObject extends IManagedObject {

    void setParent(IManagedObject aParent);

    Long getParentId();
    
    /** NOP - you can't set the parent id this way.
        This method is only here for ibatis compatibility. */
    void setParentId(Long aId);

}
