package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import au.com.thoughtpatterns.core.util.SystemException;

public class ClassHolder<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient Class<T> clas;
    private String classname;

    private static final Map<Class, ClassHolder> cache = new HashMap<Class, ClassHolder>();
    
    public static ClassHolder get(Class aClass) {
        ClassHolder h = cache.get(aClass);
        if (h == null) {
            h = new ClassHolder(aClass);
            cache.put(aClass, h);
        }
        return h;
    }
    
    private ClassHolder(Class<T> aClass) {
        clas = aClass;
        classname = clas.getName();
    }
    
    public Class<T> getContainedClass() {
        
        if (clas == null) {
            if (classname == null) {
                return null;
            }
            try
            {
                clas = (Class<T>) Class.forName(classname); // Generics cast
            } catch (ClassNotFoundException ex) {
                throw new SystemException(ex);
            }
        }
        return clas;
    }
    
    public String getContainedClassName() {
        return classname;
    }
}
