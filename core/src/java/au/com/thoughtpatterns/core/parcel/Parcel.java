package au.com.thoughtpatterns.core.parcel;

import java.io.Serializable;

import au.com.thoughtpatterns.core.bo.Box;
import au.com.thoughtpatterns.core.bo.IssueBox;
import au.com.thoughtpatterns.core.util.BusinessException;

/**
 * Represents a command that acts on BusinessObjects via a Box.
 * 
 * A Parcel can be created within a client session and then dispatched
 * to the "Container" for final execution.
 *  
 * Parcels can also be executed against a RemoteBox as a "dry run" within 
 * a client session.
 * 
 * A Parcel might contain state that can be examined after the 
 * execute method has been invoked (for instance, for returning results
 * to the caller).
 */
public interface Parcel extends Serializable {

    // --------------------------------------------
    // Configuration
    
    /**
     * Set the Box that this parcel will work with. This must be called 
     * before any of the lifecycle methods
     */
    public void setBox(Box aBox);
    
    public Box getBox();

    /**
     * Get the issue box of this parcel. Usually this is the same as the Box's IssueBox.
     * For response parcels which don't have a box, the IssueBox is detached and is 
     * available via this method
     */
    public IssueBox getIssueBox();
    
    // --------------------------------------------
    // Lifecycle

    /**
     * Most Parcels have no need to return any data to the caller.
     * However, some parcels do need to return data - for instance,
     * a Create parcel may return the primary key of the newly created
     * object.
     * 
     * Parcels that need to return data will be reserialized after the
     * execute method has been invoked. The caller may then access methods
     * on the deserialized, post-execute copy of the Parcel.
     * 
     * Parcels that don't need to return any data should return false for 
     * this method. This will save unnecessary serialization of the 
     * parcel.
     * 
     * @return true if the parcel will contain post-execute data that should
     * be available to the caller, false if not (false is the "normal" case).
     */
    public boolean hasReturnValue();
    
    /**
     * Execute the Parcel (using the configured Box).
     * Normally you wouldn't call this method directly, instead 
     * you'd use a Checkout or similar wrapper.
     */
    public void execute() throws BusinessException;
    
    /**
     * Execute any "side effects" that should only be performed 
     * after the execute method has been invoked and the results 
     * committed into the database (eg printing).
     * 
     * This method doesn't throw a BusinessException because it 
     * occurs outside of the business transaction context.
     */
    public void doSideEffects();
    
}
