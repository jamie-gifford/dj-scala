package au.com.thoughtpatterns.core.bo;


/**
 * Interface that marks classes as being "entities", that are handled by a box.
 */
public interface IEntity extends IManagedObject {

    BOKey<?> getBOKey();
    
    void setBOKey(BOKey<?> key);
    
    void setBox(Box aBox);
    
    Box getBox();
    

}
