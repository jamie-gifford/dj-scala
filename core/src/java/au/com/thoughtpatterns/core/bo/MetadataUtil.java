package au.com.thoughtpatterns.core.bo;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.thoughtpatterns.core.sql.Connections;
import au.com.thoughtpatterns.core.util.SystemException;
import au.com.thoughtpatterns.core.util.Util;


/**
 * Methods relating to the metadata annotations (PersistentClass, PersistentField)
 */
public class MetadataUtil {

    private static Map<Class, Field[]> persistentFields = new HashMap<Class, Field[]>();
    
    private static Map<Field, Integer> maxSize = new HashMap<Field, Integer>();
    
    /**
     * Get the PersistentClass annotation for the given class, or null if
     * not found. The class and its superclasses are searched
     */
    public static PersistentClass getPersistentClass(Class aClass) {

        Class c = aClass;
        while (c != null) {
            PersistentClass a = (PersistentClass) c.getAnnotation(PersistentClass.class);
            if (a != null) {
                return a;
            }
            c = c.getSuperclass();
        }
        return null;
    }
    
    /**
     * Get the "persistent data" class of the given PersistentObject. 
     * This is found by walking up the inheritance tree until a class
     * that has the PersistentClass annotation is found.
     */
    public static Class<? extends PersistentObject> getPersistentDataClass(IEntity obj) {
        Class clas = obj.getClass();
        while (true) {
            if (clas == null) {
                return null;
            }
            if (clas.getAnnotation(PersistentClass.class) != null) {
                return clas;
            }
            clas = clas.getSuperclass();
        }
    }
    
    /**
     * Search the class (and its superclasses) for a field with the PersistentField
     * annotation pointing to the given column
     * @param aClass the PersistentClass to search
     * @param aColumn the name of the column to search for
     * @return
     */
    public static Field getPersistentField(Class aClass, String aColumn) {
        
        Class c = aClass;
        while (PersistentObject.class.isAssignableFrom(c) && PersistentObject.class != c) {
            Field[] fields = c.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                PersistentField pf = (PersistentField) fields[i].getAnnotation(PersistentField.class);
                if (pf != null) {
                    String col = pf.column();
                    if (aColumn.equals(col)) {
                        return fields[i];
                    }
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }
    
    /**
     * Get the table for a given class.
     */
    public static String getTable(Class aClass) {
        PersistentClass pc = getPersistentClass(aClass);
        if (pc == null) {
            return null;
        }
        return pc.table();
    }
    
    /**
     * Get the table for a given persistent object
     */
    public static String getTable(IEntity aObject) {
        if (aObject == null) {
            return null;
        }
        Class clas = getPersistentDataClass(aObject);
        String table = getTable(clas);
        return table;
    }

    /**
     * Get the persistent fields for a class.
     * @param aClass
     * @return
     */
    public static Field[] getPersistentFields(Class aClass) {
        Field[] cached = persistentFields.get(aClass);
        if (cached != null) {
            return cached;
        }

        List<Field> list = new ArrayList<Field>();
        Class c = aClass;
        while (BusinessObject.class.isAssignableFrom(c)) {
            Field[] fields = c.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                PersistentField pf = (PersistentField) fields[i].getAnnotation(PersistentField.class);
                if (pf != null) {
                    fields[i].setAccessible(true);
                    list.add(fields[i]);
                }
            }
            c = c.getSuperclass();
        }
        cached = new Field[list.size()];
        list.toArray(cached);
        persistentFields.put(aClass, cached);
        return cached;
    }
    
    /**
     * Get the maximum size for a field, or 0 if not known
     * @param aField
     * @return the maximum size
     */
    public static int getMaxSize(Field aField) {
        Integer size = maxSize.get(aField);
        if (size != null) {
            return size;
        }
        PersistentField a = (PersistentField) aField.getAnnotation(PersistentField.class);
        if (a == null) {
            size = 0;
        } else {
            size = a.maxLength();
        }
        maxSize.put(aField, size);
        return size;
    }
    
    public static String getDisplayName(Field aField) {
        PersistentField a = (PersistentField) aField.getAnnotation(PersistentField.class);
        if (a == null) {
            return aField.getName();
        }
        String name = a.displayName();
        if (Util.empty(name)) {
            name = a.glossaryName();
        }
        if (Util.empty(name)) {
            name = aField.getName();
        }
        return name;
    }
    
    public static PersistentField getPersistentField(Field aField) {
        PersistentField pf = (PersistentField) aField.getAnnotation(PersistentField.class);
        return pf;
    }

    /**
     * Check that fields in the persistent object adhere to size constraints.
     * Add issues to the issue box (if not null) for each constraint violation, and return 
     * whether the object is okay
     * @param aObj
     * @return true if no size constraints are violated
     */
    public static boolean checkSizeConstraints(BusinessObject aObj, IssueBox issues) {
        if (aObj == null) {
            return true;
        }
        boolean okay = true;
        Field[] fields = getPersistentFields(aObj.getClass());
        for (Field field : fields) {
            int maxSize = getMaxSize(field);
            if (maxSize > 0) {
                try {
                    Object value = field.get(aObj);
                    if (value instanceof String) {
                        String str = (String) value;
                        if (str != null) {
                            if (str.length() > maxSize) {
                                okay = false;
                                
                                if (issues != null) {
                                    issues.add(Issue.maxLength(field, aObj, maxSize));
                                }
                                // truncate field so that we dont' get sql errors
                                str = str.substring(0, maxSize);
                                field.set(aObj, str);
                            }
                        }
                    }
                } catch (IllegalAccessException ex) {
                    throw new SystemException(ex);
                }
            }
        }
        return okay;
    }

    
    // -----------------------------------------------
    // Experimental (doesn't work yet)
    
    /**
     * Examine JDBC metadata for a table and compile a list of field-length
     * contraints. This requires connectivity to the database.
     */
    static FieldLengthConstraints getConstraints(Class aClass) {
     
        DefaultFieldLengthConstraints c = new DefaultFieldLengthConstraints();
        
        String table = getTable(aClass);
        if (table == null) {
            return c;
        }
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = Connections.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from " + table);
            ResultSetMetaData meta = rs.getMetaData();
            
            int cols = meta.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                int type = meta.getColumnType(i);
                if (type != Types.VARCHAR) {
                    continue;
                }
                String column = meta.getColumnName(i);
                int size = meta.getPrecision(i);
                Field field = getPersistentField(aClass, column);
                if (field != null) {
                    c.constraints.put(field, size);
                }
            }
        } catch (Exception ex) {
            throw new SystemException("Failed to read metadata for class " + aClass, ex);
        } finally {
            tidy(conn, stmt, rs);
        }
        
        return null;
    }
    
    private static void tidy(Connection c, Statement s, ResultSet rs) {
        try {
            try {
                if (rs != null) {
                    rs.close();
                }
            } finally {
                try {
                    if (s != null) {
                        s.close();
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        } catch (SQLException ex) {
            throw new SystemException(ex);
        }
    }
    
    static interface FieldLengthConstraints {
        
        Integer getMaxLength(Field aField);
        
    }
    
    static class DefaultFieldLengthConstraints implements FieldLengthConstraints {
        Map<Field, Integer> constraints = new HashMap<Field, Integer>();
        public Integer getMaxLength(Field aField) {
            return constraints.get(aField);
        }
    }
    
}
