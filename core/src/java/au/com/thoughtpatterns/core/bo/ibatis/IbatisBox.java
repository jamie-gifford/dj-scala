package au.com.thoughtpatterns.core.bo.ibatis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import au.com.thoughtpatterns.core.bo.BOKey;
import au.com.thoughtpatterns.core.bo.BOKeyImpl;
import au.com.thoughtpatterns.core.bo.CachingBox;
import au.com.thoughtpatterns.core.bo.DependentList;
import au.com.thoughtpatterns.core.bo.DependentObject;
import au.com.thoughtpatterns.core.bo.IEntity;
import au.com.thoughtpatterns.core.bo.IManagedObject;
import au.com.thoughtpatterns.core.bo.ManagedObject;
import au.com.thoughtpatterns.core.bo.SystemSqlException;
import au.com.thoughtpatterns.core.bo.VersionException;
import au.com.thoughtpatterns.core.sql.Connections;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.ProfilePoint;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * An implementation of Box that interacts with the database using 
 * Apache Ibatis as an SQL mapper.
 * 
 * Apache Ibatis requires configuration to know what SQL statements to issue.
 * The IbatisBox requires that the Ibatis configuration follow certain 
 * conventions. These are documented in the package javadoc for this package.
 * 
 * The {@link IbatisConfig} class is responsible for configuring Ibatis itself
 * and obtaining references to the Ibatis <code>SqlMapClient</code>.
 * 
 * @see IbatisConfig
 */
public class IbatisBox extends CachingBox {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.get(IbatisBox.class);
    
    private IbatisConfig config;

    public IbatisBox() {
        // Use the default (singleton) config by default
        config = IbatisConfig.instance();
    }

    public void setIbatisConfig(IbatisConfig aConfig) {
        config = aConfig;
    }

    @Override
    public void flush() {

        // ProfilePoint point = new ProfilePoint("ibatis", "flush");
        try {
            // point.start();
            
            Collection<IEntity> working;

            // First, do additions
            working = getAdded();
            for (IEntity obj : working) {
                insertInt(obj);
            }
            working.clear();
            // Now do updates
            working = getDirty();
            for (IEntity obj : working) {
                updateInt(obj);
            }
            working.clear();
            // Finally do deletes
            working = getDeleted();
            for (IEntity obj : working) {
                deleteInt(obj);
            }
            working.clear();
            getDeletedKeys().clear();
        } finally {
            // point.stop();
        }
    }

    @Override
    protected IEntity loadInt(BOKey key) {

        if (! key.isPersistent()) {
            return null;
        }
        
        BOKeyImpl keyInt = (BOKeyImpl) key;

        String queryId = keyInt.getEntityClass().getName() + ".load";
        Long pk = keyInt.getId();

        SqlMapClient sqlMapper = config.getSqlMapClient();

        // ProfilePoint point = new ProfilePoint("ibatis", queryId + ":" + pk);

        try {
            //point.start();
            IEntity object = (IEntity) sqlMapper
                    .queryForObject(queryId, pk);

            if (object != null) {
                loadDependents(object);
            }

            return object;
        } catch (SQLException ex) {
            throw new SystemSqlException(ex);
        } finally {
            //point.stop();
        }
    }

    @Override public <T extends IEntity> List<T> preload(List<BOKey<T>> keys) {
        // If the keys are "homogenous" and none are versioned, then
        // use the bulk load option. Otherwise, revert to the 
        // "simple" algorithm in the superclass (ie, load them each individually)

        // Perform work within a "transaction" to ensure that we have a single connection even if the 
        // connection pool is a trivial pool

        ProfilePoint p = new ProfilePoint("preload", "" + keys.size());
        log.debug("Preloading " + keys.size() + " objects...");
        
        p.start();
        Connections.startTransaction();
        try {
        
            boolean versioned = false;
            boolean homogenous = true;
            Class<T> entityClass = null;
            
            for (BOKey<T> key : keys) {
                BOKeyImpl<T> keyImpl = (BOKeyImpl<T>) key;
                Long version = keyImpl.getActivityId();
                if (version != null) {
                    versioned = true;
                    break;
                }
                Class<T> keyClass = keyImpl.getEntityClass();
                if (entityClass == null) {
                    entityClass = keyClass;
                } else {
                    if (entityClass != keyClass) {
                        homogenous = false;
                        break;
                    }
                }
            }
            
            if (versioned || ! homogenous) {
                super.preload(keys);
            }
            
            List<T> preloaded = preloadInt(entityClass, keys);
            for (T object : preloaded) {
                BOKey<T> key = (BOKey<T>) object.getBOKey(); // Generics cast
                discover(key, object);
            }
            
            // Now compose final list of loaded
            List<T> loaded = new ArrayList<T>();
            for (BOKey<T> key : keys) {
                if (isLoaded(key)) {
                    T t = load(key);
                    loaded.add(t);
                }
            }
            return loaded;
        
        } finally {
            Connections.endTransaction();
            log.debug("...done preloading " + keys.size() + " objects");
            p.stop();
        }
    }
    
    protected <T extends IEntity> List<T> preloadInt(Class entityClass, List<BOKey<T>> keys) {
        List<T> loaded = new ArrayList<T>();
        
        if (keys == null || keys.size() == 0) {
            return loaded;
        }
        ArrayList<Long> pkList = new ArrayList<Long>();
        for (BOKey<T> key : keys) {
            
            if (! isLoaded(key)) {
                BOKeyImpl<T> keyInt = (BOKeyImpl<T>) key;
                Long id = keyInt.getId();
                pkList.add(id);
            } 
        }
        
        if (pkList.size() == 0) {
            return loaded;
        }
        
        String queryId = entityClass.getName() + ".bulkload";
        SqlMapClient sqlMapper = config.getSqlMapClient();


        try {

            // Break the list into bite-sized pieces (50 in each hit)
            int cursor = 0;
            int size = pkList.size();
            int maxBiteSize = getPreloadBiteSize();
            ArrayList<Long> bite = new ArrayList<Long>(maxBiteSize);
            while (cursor < pkList.size()) {
                
                // ProfilePoint point = new ProfilePoint("ibatis", "bulkload");
                // point.start();

                int biteSize = size - cursor;
                if (biteSize > maxBiteSize) {
                    biteSize = maxBiteSize;
                }
                bite.clear();
                for (int i = 0; i < biteSize; i++) {
                    bite.add(pkList.get(cursor + i));
                }

                List objects = sqlMapper.queryForList(queryId, bite);

                for (Object object : objects) {
                    T mo = (T) object;
                    if (mo != null) {
                        loadDependents(mo);
                        loaded.add(mo);
                    }
                }

                cursor += biteSize;
                
                // point.stop();
            }

            return loaded;
        } catch (SQLException ex) {
            throw new SystemSqlException(ex);
        } finally {
            
        }
    }

    protected int getPreloadBiteSize() {
        return 50;
    }
    
    /**
     * Preload using an ibatis statement (named)
     * @param entityClass class for loaded objects
     * @param queryId the ibatis statement key
     * @param parameter the parameter for the ibatis statement (eg a Map)
     * @return
     */
    public <T extends IEntity> List<T> preload(Class<T> entityClass, String queryId, Object parameter) {
        SqlMapClient sqlMapper = config.getSqlMapClient();
        List<T> loaded = new ArrayList<T>();
        try {
            List<T> objects = sqlMapper.queryForList(queryId, parameter); // Generics cast
            for (T object : objects) {
                BOKey<T> key = (BOKey<T>) object.getBOKey(); // Generics cast
                T discovered = discover(key, object);
                if (discovered != null) {
                    loaded.add(discovered);
                }
            }
        } catch (SQLException ex) {
            throw new SystemSqlException(ex);
        }
        return loaded;
    }
    
    /**
     * Bulk load a bunch of objects, independent of BO framework.
     * This is just leveraging ibatis. No attempt at object identity resolution is made.
     */
    public <T> List<T> bulkloadGeneric(String queryId, Object parameter) {
        SqlMapClient sqlMapper = config.getSqlMapClient();
        try {
            List<T> objects = sqlMapper.queryForList(queryId, parameter); // Generics cast
            return objects;
        } catch (SQLException ex) {
            throw new SystemSqlException(ex);
        }
    }
    
    protected void insertInt(IManagedObject obj) {
        
        checkSizeConstraints(obj);
        
        if (obj.getId() == null) {
            autoAllocateKey(obj);
        }

        String queryId = obj.getEntityClass().getName() + ".insert";
        SqlMapClient sqlMapper = config.getSqlMapClient();

        try {
            configureActivity(obj);
            sqlMapper.insert(queryId, obj);

            configureLoadedActivity(obj);
            
            updateDependents(obj);

        } catch (SQLException ex) {
            throw new SystemSqlException(ex);
        }
    }

    /**
     * Called if an object is inserted but no explicit key is given.
     * Normally this will just fetch next sequence value. 
     * In some contexts, it may throw an exception (eg, if the box should not allow
     * auto allocation).
     */
    protected void autoAllocateKey(IManagedObject target) {
        long next = fetchNextSequenceValue();
        target.setId(next);
    }
    
    protected void updateInt(IManagedObject obj) {

        checkSizeConstraints(obj);

        SqlMapClient sqlMapper = config.getSqlMapClient();

        try {
            
            if (! getOptimisticLockOnWrites()) {
                
                // Find current activity id on DB before updating.
                String loadQueryId = obj.getEntityClass().getName() + ".load";
                Long pk = obj.getId();

                IEntity object = (IEntity) sqlMapper.queryForObject(loadQueryId, pk);
                if (object != null) {
                    Long loadedId = object.getActivityId();
                    obj.setLoadedActivityId(loadedId);
                }
            }
            
            Long aid = configureActivity(obj);

            String updateQueryId = obj.getEntityClass().getName() + ".update";
            
            int rows = sqlMapper.update(updateQueryId, obj);
            
            if (rows != 1 && aid != null) {
                log.info("Optimistic lock: failed to update " + obj + " with id " + obj.getId() + " because rows = " + rows + ", aid = " + aid + " and loaded activity id is " + obj.getLoadedActivityId());
                throw new VersionException();
            }
            configureLoadedActivity(obj);

            updateDependents(obj);
        } catch (SQLException ex) {
            throw new SystemSqlException(ex);
        }
    }

    protected void handleDirtyInt(IManagedObject obj) {
        Long id = obj.getId();
        if (id == null) {
            insertInt(obj);
        } else {
            updateInt(obj);
        }
    }
    
    protected void deleteInt(IManagedObject obj) {
        String queryId = obj.getEntityClass().getName() + ".delete";
        Long pk = obj.getId();

        if (pk == null) {
            // No persistent id - nothing to do
            return;
        }
        
        SqlMapClient sqlMapper = config.getSqlMapClient();

        try {
            deleteDependents(obj);
            configureActivity(obj);
            int rows = sqlMapper.delete(queryId, pk);
            if (rows != 1) {
                throw new VersionException();
            }

        } catch (SQLException ex) {
            throw new SystemSqlException(ex);
        }
    }

    public long fetchNextSequenceValue() {
        SqlMapClient sqlMapper = config.getSqlMapClient();

        try {
            long value = (Long) sqlMapper.queryForObject("main_sequence");
            return value;
        } catch (SQLException ex) {
            throw new SystemSqlException(ex);
        }
    }

    private void loadDependents(IManagedObject object) {
        Set<DependentList> dependencies = object.getDependentLists();

        if (dependencies == null) {
            return;
        }

        Long pk = object.getId();

        for (DependentList list : dependencies) {
            loadDependents(list, pk);
        }
    }

    private void loadDependents(DependentList list, Long pk) {
        Class clas = list.getDependentType();

        String queryId = clas.getName() + ".dependentLoad";

        SqlMapClient sqlMapper = config.getSqlMapClient();

        // This loadDependents algorithm is not efficient if we have
        // multiple levels of dependents.
        // It could be improved.
        try {
            List results = (List) sqlMapper.queryForList(queryId, pk);

            for (Object result : results) {
                list.add(result);

                ManagedObject mob = (ManagedObject) result;
                loadDependents(mob);
            }
            
            // Clear out the added list - they are not really added
            list.getDirty().clear();

        } catch (SQLException ex) {
            throw new SystemSqlException(ex);
        }
    }

    private void updateDependents(IManagedObject object) {
        
        // TODO don't forget to update the activity id on the parent
        
        Set<DependentList> dependencies = object.getDependentLists();

        if (dependencies == null) {
            return;
        }

        for (DependentList list : dependencies) {
            updateDependentList(list);
        }    
    }

    private void updateDependentList(DependentList list) {
        Set<DependentObject> dirties = list.getDirty();
        for (DependentObject dirty : dirties) {
            handleDirtyInt(dirty);
        }
        dirties.clear();

        Set<DependentObject> deletes = list.getDeleted();
        for (DependentObject delete : deletes) {
            deleteInt(delete);
        }
        deletes.clear();
    }
    
    private void deleteDependents(IManagedObject object) {
        // Don't forget to set the activity id on the parent

        Set<DependentList> dependencies = object.getDependentLists();

        if (dependencies == null) {
            return;
        }

        for (DependentList list : dependencies) {
            deleteDependentList(list);
        }    
    }

    private void deleteDependentList(DependentList list) {
        for (Object obj : list) {
            DependentObject d = (DependentObject) obj;
            deleteInt(d);
        }
    }
    
    protected Long configureActivity(IManagedObject obj) {
        Long activityId = getActivityId();
        if (activityId != null) {
            obj.setActivityId(activityId);
        }
        return activityId;
    }
    private void configureLoadedActivity(IManagedObject obj) {
        Long activityId = getActivityId();
        if (activityId != null) {
            obj.setLoadedActivityId(activityId);
        }
    }

    
    protected IbatisConfig getConfig() {
        return config;
    }
    
    public <T extends IEntity> BOKey<T> createNewKey(Class<T> entityClass) {
        BOKeyImpl<T> key = (BOKeyImpl<T>) super.createNewKey(entityClass);
        
        long next = fetchNextSequenceValue();
        key.setId(next);
        return key;
    }
}
