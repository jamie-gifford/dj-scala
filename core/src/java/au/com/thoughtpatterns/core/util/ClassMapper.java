package au.com.thoughtpatterns.core.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Maps Classes to objects. The class/interface inheritance tree is respected,
 * in the sense that a given Class inherits the parent Class's object, unless a
 * specific object is bound to that class
 */
public class ClassMapper<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger log = Logger.get(ClassMapper.class);

    /**
     * For eficiency, we maintain a fast registry that stores the results of get
     * lookups.
     */
    private HashMap<Class, T> fastRegistry = new HashMap<Class, T>();

    /**
     * The main configuration item
     */
    private HashMap<Class, T> registry = new HashMap<Class, T>();

    /**
     * An optional delegate ClassMapper
     */
    private ClassMapper delegate;

    public T get(Class in) {
        if (in == null) {
            return null;
        }
        Getter getter = new Getter();
        return getter.get(in);
    }

    private class Getter {

        int depth = 0;

        Class original;

        T get(Class in) {
            ArrayList<Class> list = new ArrayList<Class>(1);
            list.add(in);
            original = in;
            return get(list);
        }

        private T get(List<Class> classes) {
            T value = fastRegistry.get(classes.get(0));
            if (value != null) {
                return value;
            }

            // Try all the classes in the list; if one is in the registry,
            // great. Otherwise, replace the list with all
            // superclasses/interfaces
            // and repeat.

            while (classes.size() != 0) {
//                if (log.isDebugOn()) {
//                    log.debug("Searching " + classes.size() + " at depth "
//                            + depth);
//                }

                ArrayList<Class> next = new ArrayList<Class>();
                for (int i = 0; i < classes.size(); i++) {
                    Class clas = (Class) classes.get(i);
                    value = getFromRegistryChain(clas);
                    if (value != null) {
                        fastRegistry.put(original, value);
                        // Got it
                        return value;
                    }

                    // Otherwise, put all superclasses/interfaces of clas
                    // into the next list
                    Class sooper = clas.getSuperclass();
                    if (sooper != null) {
                        next.add(sooper);
                    }
                    Class[] interfaces = clas.getInterfaces();

                    // Add to next, repeat
                    for (int j = 0; j < interfaces.length; j++) {
                        next.add(interfaces[j]);
                    }
                }
                classes = next;
                depth++;
            }
            return null;
        }
    }

    public T getFromRegistryChain(Class key) {
        T value = null;
        Map<Class, T> map = getRegistry();
        value = map.get(key);
        if (value == null && delegate != null) {
            value = (T) delegate.getFromRegistryChain(key);
        }
        return value;
    }

    protected Map<Class, T> getRegistry() {
        return registry;
    }

    protected void setRegistry(HashMap<Class, T> aMap) {
        registry = aMap;
    }

    public void initialise(Properties props) {
        Iterator iter = props.keySet().iterator();
        while (iter.hasNext()) {
            String type = (String) iter.next();
            T value = (T) props.get(type);
            try {
                Class clas = Class.forName(type);
                register(clas, value);
            } catch (ClassNotFoundException ex) {
                log.error("No class definition found for " + type);
            }
        }
    }
    
    public void register(Class aClass, T value) {
        Map<Class, T> r = getRegistry();
        r.put(aClass, value);
    }

    public ClassMapper getDelegate() {
        return delegate;
    }

    public void setDelegate(ClassMapper delegate) {
        this.delegate = delegate;
    }
}