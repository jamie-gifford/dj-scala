package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import au.com.thoughtpatterns.core.bo.ibatis.IbatisBoxFactory;
import au.com.thoughtpatterns.core.container.ContainedException;
import au.com.thoughtpatterns.core.container.Container;
import au.com.thoughtpatterns.core.util.Factory;
import au.com.thoughtpatterns.core.util.SystemException;

/**
 * A "connectionless" Box implementation, for use on the "client" side of a
 * stateless server.
 * 
 * A RemoteBox does not update the backing database. The flush operation does
 * nothing.
 */
public class RemoteBox extends CachingBox {

    private static final long serialVersionUID = 1L;
    private static Method LOAD = null;
    private static Method PRELOAD = null;
    static {
        try {
            LOAD = Loader.class.getDeclaredMethod("load", new Class[0]);
            PRELOAD = Preloader.class.getDeclaredMethod("preload", new Class[0]);
        } catch (NoSuchMethodException ex) {
            throw new SystemException(ex);
        }
    }
    
    public RemoteBox() {
        
    }
    
    @Override
    public void flush() {
        // Flush does nothing for a RemoteBox
    }

    @Override
    protected IEntity loadInt(BOKey key) {
        BoxFactory factory = getBoxFactory();
        Loader loader = new Loader(key, factory);
        Container container = (Container) Factory.create(Container.class);
        
        try {
            PersistentObject loaded = (PersistentObject) container.runTransaction(LOAD, loader);
            return loaded;
        } catch (ContainedException ex) {
            // There are no valid runtime exceptions expected
            throw new SystemException(ex.getCause());
        }
    }

    // There's a bug here that makes objects appear with no box...
    // Can reproduce by trying to print race results in AKA/RaceImpl
    public <T extends IEntity> List<T> preload(List<BOKey<T>> keys) {
        BoxFactory factory = getBoxFactory();
        Preloader<T> loader = new Preloader<T>(keys, factory);
        Container container = (Container) Factory.create(Container.class);
        
        try {
            List<T> preloaded = (List<T>) container.runTransaction(PRELOAD, loader);
            List<T> loaded = new ArrayList<T>();
            for (T t : preloaded) {
                BOKey<T> k = (BOKey<T>)t.getBOKey();
                discover((BOKey<T>)t.getBOKey(), t);
                T t2 = load(k);
                loaded.add(t2);
            }
            return loaded;
        } catch (ContainedException ex) {
            // There are no valid runtime exceptions expected
            throw new SystemException(ex.getCause());
        }
    }
    
    /**
     * By default, return an IbatisBoxFactory. Subclasses can override this.
     */
    protected BoxFactory getBoxFactory() {
        return new IbatisBoxFactory();
    }
    
    /**
     * A class for running Box operations "in the container", on behalf
     * of the RemoteBox
     */
    static class Loader implements Serializable {
        
        private static final long serialVersionUID = 1L;

        private BOKey key;
        
        private BoxFactory factory;
        
        public Loader(BOKey aKey, BoxFactory aFactory) {
            key = aKey;
            factory = aFactory;
        }
        
        public IEntity load() {
            Box box = factory.createBox();
            IEntity loaded = box.load(key);
            
            if (loaded != null) {
                // We need to detach from the loading box, otherwise when 
                // we serialize we will drag the box with us, which is not
                // what we want.
                box.detach(loaded);
            }
            
            return loaded;
        }
    }

    /**
     * A class for running Box operations "in the container", on behalf
     * of the RemoteBox
     */
    static class Preloader<T extends IEntity> implements Serializable {
        
        private static final long serialVersionUID = 1L;

        private List<BOKey<T>> keys;
        
        private BoxFactory factory;
        
        public Preloader(List<BOKey<T>> aKeys, BoxFactory aFactory) {
            keys = aKeys;
            factory = aFactory;
        }
        
        public List<T> preload() {
            Box box = factory.createBox();
            List<T> loaded = box.preload(keys);
            
            for (T t : loaded) {
                // We need to detach from the loading box, otherwise when 
                // we serialize we will drag the box with us, which is not
                // what we want.
                box.detach(t);
            }
            return loaded;
        }
    }

}
