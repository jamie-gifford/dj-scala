package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.com.thoughtpatterns.core.util.SystemException;

/**
 * Holds a list of dependent objects
 * @author james
 *
 */
public class DependentList<T extends DependentObject> extends AbstractList<T> 
    implements Serializable{

    private static final long serialVersionUID = 1L;
    
    private ManagedObject parent;
    private ClassHolder dependentType; 

    private List<T> backing = new ArrayList<T>();
    
    private HashSet<DependentObject> dirty = new HashSet<DependentObject>();
    private HashSet<DependentObject> deleted = new HashSet<DependentObject>();
    
    private Listener listener = new Listener();
    
    public DependentList(ManagedObject aParent, Class aDependentType) {
        parent = aParent;
        aParent.addDependentList(this);
        dependentType = ClassHolder.get(aDependentType);
    }
    
    public ManagedObject getParent() {
        return parent;
    }
    
    public Class getDependentType() {
        return dependentType.getContainedClass();
    }
    
    @Override
    public T get(int index) {
        return backing.get(index);
    }

    @Override
    public int size() {
        return backing.size();
    }

    @Override
    public void add(int index, T element) {
        backing.add(index, element);
        ManagedObject parent = getParent();
        element.setParent(parent);
        element.addListener(listener);
        dirty.add(element);
        getParent().fireChanged();
    }

    @Override
    public T remove(int index) {
        T object = backing.remove(index);
        if (object != null) {
            object.removeListener(listener);
            dirty.remove(object);
            deleted.add(object);
            getParent().fireChanged();
        }
        return object;
    }

    @Override
    public T set(int index, T element) {
        T existing = get(index);
        
        if (existing == element) {
            return existing;
        }
        
        existing.removeListener(listener);

        // This is not finished...
        if (true) { throw new SystemException("Unfinished code ..."); }
        
        element.addListener(listener);
        dirty.add(element);
        return backing.set(index, element);
    }    

    public void set(List<T> from) {
        clear();
        addAll(from);
    }
    
    // --------------------------------------
    // Lifecycle management
    
    private class Listener implements BusinessObjectListener {

        private static final long serialVersionUID = 1L;

        public void changed(BusinessObject object) {
            T obj = (T) object;
            dirty.add(obj);
            getParent().fireChanged();
        }
    }
    
    public Set<DependentObject> getDirty() {
        return dirty;
    }

    public Set<DependentObject> getDeleted() {
        return deleted;
    }
}
