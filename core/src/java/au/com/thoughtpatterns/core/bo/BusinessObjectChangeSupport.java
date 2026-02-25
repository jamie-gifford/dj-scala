package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class BusinessObjectChangeSupport implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<BusinessObjectListener> listeners = new ArrayList<BusinessObjectListener>();
    
    public void addListener(BusinessObjectListener l) {
        if (listeners.contains(l)) {
            return;
        }
        listeners.add(l);
    }
    
    public void removeListener(BusinessObjectListener l) {
        listeners.remove(l);
    }

    public void fireChanged(BusinessObject source) {
        for (BusinessObjectListener l : listeners) {
            l.changed(source);
        }
    }
    
    public int listeners() {
        return listeners.size();
    }
}
