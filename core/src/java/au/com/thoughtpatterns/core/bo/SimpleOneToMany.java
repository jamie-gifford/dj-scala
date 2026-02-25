package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

    import au.com.thoughtpatterns.core.util.SystemException;
import au.com.thoughtpatterns.core.util.Util;

/**
 * A convenience subclass of OneToMany to handle the frequent case of a simple
 * "inverse" of a Relationship
 * 
 * @author james
 * 
 */
public class SimpleOneToMany<T extends IEntity> extends OneToMany<T> {

    private static final long serialVersionUID = 1L;
    private Info info;

    protected static class Info implements Serializable {

        private static final long serialVersionUID = 1L;

        String table;

        String fk;

        /**
         * May be a Relationship or a ForeignKey
         */
        transient Field relationship;
    }

    private static Method getter;
    static {
        try {
            getter = Relationship.class.getMethod("get");
        } catch (NoSuchMethodException ex) {
            // Shouldn't happen
            throw new SystemException(ex);
        }
    }

    /**
     * Create a SimpleOneToMany map from the given "owner" to the type
     * specificed in the targetClass, using the given fk in the target class's
     * table as a selector.
     * 
     * @param aOwner
     * @param targetClass
     * @param fk
     */
    public SimpleOneToMany(IEntity aOwner,
            Class<T> targetClass, String fk) {
        super(aOwner, targetClass);
        configureTable(fk);
    }

    @Override
    protected Query query() {

        String table = getTable();
        String fk = getFk();

        String sql = "select id from " + table + " where " + fk + " = ?";
        
        String constraint = getConstraint();
        if (constraint != null) {
            sql = sql + " and ( " + constraint + " ) "; 
        }
        
        String order = getOrderSql();
        if (order != null) {
            sql = sql + " order by " + order;
        }

        Query q = QueryFactory.create(sql);
        q.setNextValue(getOwner());
        return q;
    }
    
    protected String getConstraint() {
        return null;
    }
    
    /**
     * Subclasses may override to provide an "order" clause in the 
     * sql used to select members of the collection.
     * 
     * For instance, a possible return value could be "creation_date desc"
     * 
     * @return the order clause (excluding the "order by" prefix), or null
     */
    protected String getOrderSql() {
        return null;
    }

    @Override
    protected boolean belongs(T candidate) {
        try {
            Field relationship = getRelationship();
            RelationshipLinked relInstance = (RelationshipLinked) relationship.get(candidate);
            // Long other = (Long) getter.invoke(relInstance);
            Relationship rel = relInstance.getRelationship();

            // Old version requires deferencing....
//            IEntity other = rel.get();
//            IEntity owner = getOwner();
//            return other == owner;

            BOKey fk = getOwner().getBOKey();
            BOKey otherFk = rel.getForeignKey(); 
            
            return Util.equals(fk, otherFk);
            
        } catch (Exception ex) {
            throw new SystemException(
                    "Automatic 'belongs' method failed for target class "
                            + getTargetClass() + ", table " + getTable()
                            + " and fk " + getFk() + ": " + ex.getMessage(), ex);
        }
    }

    public String getTable() {
        Info info = getInfo();
        return info.table;
    }

    public String getFk() {
        Info info = getInfo();
        return info.fk;
    }

    public Field getRelationship() {
        Info info = getInfo();
        if (info.relationship == null) {
            info.relationship = createRelationship();
        }
        return info.relationship;
    }

    protected Info getInfo() {
        return info;
    }

    /**
     * Given a subclass of SimpleOneToMany, extract the table and fk info from
     * the ForeignKey annotation
     * 
     * @param aClass
     */
    protected void configureTable(String fk) {
        // Can derive the table from the PersistentClass
        // Annotation

        info = new Info();

        Class clas = getTargetClass();
        PersistentClass p = MetadataUtil.getPersistentClass(clas);
        if (p != null) {
            String table = p.table();
            info.table = table;
        } else {
            throw new SystemException("No table declared for class " + clas);
        }
        info.fk = fk;
    }

    // Could use a static cache to speed this up. Currently it requires
    // reflective access *per instance* of a SimpleOneToMany.
    protected Field createRelationship() {

        // Find the PersistentField with the given column
        String fk = getFk();
        Class clas = getTargetClass();
        Field pf = MetadataUtil.getPersistentField(clas, fk);
        if (pf == null) {
            throw new SystemException("Can't find persistent field for column "
                    + fk + " in class " + clas);
        }
        // Check that the field is a relationship or a ForeignKey
        if (!RelationshipLinked.class.isAssignableFrom(pf.getType())) {
            throw new SystemException("Field " + pf.getName() + " of type "
                    + pf.getType() + " is not a RelationshipLinked type");
        }

        Field relationship = pf;
        relationship.setAccessible(true);
        return relationship;
    }
}
