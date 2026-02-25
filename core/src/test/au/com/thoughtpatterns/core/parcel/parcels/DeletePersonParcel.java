package au.com.thoughtpatterns.core.parcel.parcels;

import au.com.thoughtpatterns.core.bo.BOKey;
import au.com.thoughtpatterns.core.bo.IEntity;
import au.com.thoughtpatterns.core.parcel.DefaultParcel;
import au.com.thoughtpatterns.core.util.BusinessException;

/**
 * A test parcel that deletes a person
 */
public class DeletePersonParcel extends DefaultParcel {

    // ------------------------
    // Input state
    
    private static final long serialVersionUID = 1L;

    private BOKey deleteKey;
    
    // A silly parameter that will force the parameter to throw
    // a BusinessException (for testing rollback)
    private boolean throwException;

    public DeletePersonParcel(BOKey aKey, boolean aThrowException) {
        deleteKey = aKey;
        throwException = aThrowException;
    }
    
    @Override
    public void execute() throws BusinessException {
        
        IEntity object = getBox().load(deleteKey);
        if (object != null) {
            getBox().delete(object);
        }
        
        // There's no need to flush here, but we do so anyway so that
        // we are really testing the rollback in the next step
        getBox().flush();
        
        if (throwException) {
            throw new BusinessException("Throwing exception to force rollback");
        }
    }
    
}
