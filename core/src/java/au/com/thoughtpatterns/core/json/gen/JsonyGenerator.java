package au.com.thoughtpatterns.core.json.gen;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.thoughtpatterns.core.json.Jsony;
import au.com.thoughtpatterns.core.json.JsonyObject;
import au.com.thoughtpatterns.core.json.JsonyPrimitive;
import au.com.thoughtpatterns.core.json.schema.Schema;
import au.com.thoughtpatterns.core.util.Logger;

public class JsonyGenerator {

    private static final Logger log = Logger.get(JsonyGenerator.class);
    
    private Set<Schema<?>> done = new HashSet<>();
    
    private List<String> errors = new ArrayList<>();

    private final Members<JsonyClass> classes = new Members<JsonyClass>() {

        @Override JsonyClass newMember(String name) {
            return new JsonyClass(name);
        }
    };

    private final Members<JsonyType> types = new Members<JsonyType>() {

        @Override JsonyType newMember(String name) {
            return new JsonyType(name);
        }
    };

    public Members<JsonyClass> getClasses() {
        return classes;
    }

    public Members<JsonyType> getTypes() {
        return types;
    }

    public JsonyType typeFrom(Schema<?> schema) {

        log.info("Generating classes for schema " + schema);
        
        JsonyPrimitive<String> c = (JsonyPrimitive<String>) schema.get("java");

        String className = c != null ? ((JsonyPrimitive<String>) schema.get("java")).getValue()
                : null;

        if (c != null) {
            JsonyClass clazz = classes.peek(className);

            if (clazz == null) {
                clazz = classes.get(className);
                generate(clazz, schema);
            }

            return clazz.getType();
        }

        // No explicit classname

        String type = schema.getSimpleType();
        if (type == null) {
            // No explicit classname and no simple type - for the moment return
            // null but we need to decide what to do
            String error = "Missing type from schema " + schema.toJson();
            if (schema.getEnum() != null) {
                // Ah - it was an enum. Not supported yet..
                error = "Code generation for enum type not supported in schema " + schema.toJson();
            }
            errors.add(error);
            return null;
        }

        switch (type) {
        case "array":
            
            String t = "[?";
            
            // Generic type. Look to see if the type is parameterisable
            Jsony items = schema.get("items");
            if (items instanceof JsonyObject) {
                // Single object => homogenous, parameterisable list
                Schema<?> elementSchema = schema.from(items);
                JsonyType elementType = typeFrom(elementSchema);
                
                t = "[" + elementType.getName();
            }
            
            return types.get(t);
        default:
            return types.get(type);
        }
    }

    /**
     * Generate class clazz to match the given schema.
     */
    private void generate(JsonyClass clazz, Schema<?> schema) {

        clazz.desc = schema.getCast("description", String.class);
        clazz.jsonType = schema.getSimpleType();
        clazz.schemaLocation = schema.getSchemaLocation();
        
        JsonyObject props = (JsonyObject) schema.get("properties");
        if (props != null) {

            // Each property in the schema is a property of the Java class.

            for (String pname : props.getPropertyNames()) {
                Jsony p = props.get(pname);
                Schema<JsonyObject> propSchema = (Schema<JsonyObject>) schema.from(p);

                JsonyType propType = typeFrom(propSchema);
                
                if (propType == null) {
                    continue;
                }

                JsonyProperty property = clazz.properties.get(pname);

                property.type = propType;
                property.desc = propSchema.getCast("description", String.class);

            }

        }

    }

    public class JsonyType extends Member {

        public JsonyType(String aName) {
            super(aName);
        }

    }

    public class JsonyClass extends Member {

        private String jsonType;
        
        private URI schemaLocation;
        
        final Members<JsonyProperty> properties = new Members<JsonyProperty>() {

            @Override JsonyProperty newMember(String name) {
                return new JsonyProperty(name);
            }
        };

        JsonyClass(String aName) {
            super(aName);
        }

        public String toString() {
            return "Class " + getName();
        }
        
        public JsonyType getType() {
            return types.get(getName());
        }
        
        public Members<JsonyProperty> getProperties() {
            return properties;
        }
        
        public String getJsonType() {
            return jsonType;
        }

        public URI getSchemaLocation() {
            return schemaLocation;
        }
        
    }

    public class JsonyProperty extends Member {

        JsonyType type;
        
        public JsonyProperty(String aName) {
            super(aName);
        }
        
        public JsonyType getType() {
            return type;
        }
        
    }

    class Member {

        final String name;

        String desc;

        Member(String aName) {
            name = aName;
        }

        public String getName() {
            return name;
        }
        
        public String getDesc() {
            return desc;
        }

        public String toString() {
            return name;
        }

    }

    public abstract class Members<T extends Member> implements Iterable<T> {

        final List<T> members = new ArrayList<>();

        final Map<String, T> map = new HashMap<>();

        T get(String name) {
            T t = map.get(name);
            if (t == null) {
                t = newMember(name);
                map.put(name, t);
                members.add(t);
            }
            return t;
        }

        T peek(String name) {
            return map.get(name);
        }

        abstract T newMember(String name);

        @Override public Iterator<T> iterator() {
            return members.iterator();
        }

    }

    public void dump() {

        for (JsonyClass c : classes) {

            log.info("Class " + c + " from " + c.getSchemaLocation());

            for (JsonyProperty p : c.properties) {
                log.info("  " + p + " : " + p.type);
            }

        }

    }

    public void generateAll(Schema<?> schema) {
        
        // Handle recursion
        if (done.contains(schema)) {
            return;
        }
        done.add(schema);
        
        // Walk schema, find any objects with a "java" property, and generate
        Jsony j = schema.get("java");
        if (j != null) {
            typeFrom(schema);
        }
        if (schema instanceof JsonyObject) {
            JsonyObject obj = (JsonyObject) schema;
            for (String p : obj.getPropertyNames()) {
                Jsony k = obj.get(p);
                if (k instanceof JsonyObject) {
                    Schema<?> s = schema.from(k);
                    generateAll(s);
                }
            }
        }
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
}
