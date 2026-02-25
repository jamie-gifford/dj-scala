package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;

public interface BusinessObject extends Serializable, Localizable {

    /**
     * Return the parent business object or null if none.
     */
    public BusinessObject getParent();
    
    public void addListener(BusinessObjectListener l);
    
    public void removeListener(BusinessObjectListener l);

    public void fireChanged();

    
}
