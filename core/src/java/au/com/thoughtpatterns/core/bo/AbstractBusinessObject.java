package au.com.thoughtpatterns.core.bo;

import java.lang.reflect.Field;

import au.com.thoughtpatterns.core.util.SystemException;
import au.com.thoughtpatterns.core.util.Util;


/**
 * Superclass for Business Objects
 * 
 */
public class AbstractBusinessObject implements BusinessObject {

    private static final long serialVersionUID = 1L;

    private BusinessObjectChangeSupport support;
    
    /**
     * The primary key of the last "activity" that touched this 
     * object.
     */
    private Long activityId;
    
    /**
     * The "loaded" activityId. This is activityId that is expected to be in 
     * the database when doing updates.
     */
    private Long loadedActivityId;

    /**
     * The "localizer" of this business object
     */
    private String localizer;
    
    public String getLocalizer() {
        return localizer;
    }
    
    public void setLocalizer(String localizer) {
        this.localizer = localizer;
    }

    public void setLocalizerIfNull(String aLocalizer) {
        if (getLocalizer() == null) {
            setLocalizer(aLocalizer);
        }
    }
    
    public void fireChanged() {
        if (support == null) {
            return;
        }
        support.fireChanged(this);
    }

    public void addListener(BusinessObjectListener l) {
        getChangeSupport().addListener(l);
    }

    public void removeListener(BusinessObjectListener l) {
        if (support != null) {
            support.removeListener(l);
            if (support.listeners() == 0) {
                support = null;
            }
        }
    }
    
    private BusinessObjectChangeSupport getChangeSupport() {
        if (support == null) {
            support = new BusinessObjectChangeSupport();
        }
        return support;
    }

    public BusinessObject getParent() {
        return null;
    }
    
    /**
     * Transfer data from a business object to this business object.
     * By default, all "persistent fields" will be transferred.
     * Relationships and collection-valued fields (dependent lists) won't 
     * be transferred.
     *  
     * @param other the BusinessObject to load data from
     */
    public void loadData(BusinessObject other) {
        loadData(other, false);
    }

    /**
     * Transfer data from a business object to this business object.
     * By default, all "persistent fields" will be transferred.
     * Collection-valued fields (dependent lists) won't 
     * be transferred, and foreign keys will be transferred or not depending
     * on value of "includeFk"
     *  
     * @param other the BusinessObject to load data from
     * @param includeFk determines whether foreign key values will be transferred
     */
    public void loadData(BusinessObject other, boolean includeFk) {

        // Walk up the inheritance tree looking for persistent fields
        Class clas = other.getClass();
        
        while (BusinessObject.class.isAssignableFrom(clas)) {
            loadData(other, clas, includeFk);
            clas = clas.getSuperclass();
        }
        
        String l = other.getLocalizer();
        if (l != null) {
            setLocalizer(l);    
        }        
    }
    
    /**
     * Transfer persistent fields, but exclude relationships
     */
    protected void loadData(BusinessObject other, Class clas, boolean includeFk) {
        Field[] fields = clas.getDeclaredFields();
        for (Field field : fields) {
            
            boolean transfer = true;
            if (! field.isAnnotationPresent(PersistentField.class)) {
                transfer = false;
            } else if (RelationshipLinked.class.isAssignableFrom(field.getType())) {
                if (! includeFk) {
                    transfer = false;
                } else {
                    // Only transfer ForeignKeys, not other types of relationships
                    if (! ForeignKey.class.isAssignableFrom(field.getType())) {
                        transfer = false;
                    }
                }
            }
            
            if (transfer) {
                loadData(other, field);
            }
        }
    }
    
    protected void loadData(BusinessObject other, Field field) {
        field.setAccessible(true);
        
        try {
            // Special treatment for ForeignKeys, which are containers
            if (ForeignKey.class.isAssignableFrom(field.getType())) {
                ForeignKey src = (ForeignKey) field.get(other);
                ForeignKey dest = (ForeignKey) field.get(this);
                dest.setIdentity(src);
            } else {
                Object value = field.get(other);
                Object already = field.get(this);
                
                if (!Util.equals(value, already)) {
                    field.set(this, value);
                    fireChanged();
                }
            }
        } catch (Exception ex) {
            throw new SystemException("Could not loadData from field " + field, ex);
        }        
    }
    
    public Long getActivityId() {
        return activityId;
    }
    
    public void setActivityId(Long id) {
        activityId = id;
    }

    
    public Long getLoadedActivityId() {
        return loadedActivityId;
    }

    public void setLoadedActivityId(Long expectedActivityId) {
        this.loadedActivityId = expectedActivityId;
    }
    
}
