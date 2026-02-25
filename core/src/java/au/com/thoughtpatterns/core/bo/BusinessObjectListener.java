package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;

/**
 * An interface for listening to changes to BusinessObjects
 */
public interface BusinessObjectListener extends Serializable {

    void changed(BusinessObject object);
    
}
