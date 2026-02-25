package au.com.thoughtpatterns.core.json.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.thoughtpatterns.core.json.AJsonyObject;
import au.com.thoughtpatterns.core.json.Jsony;
import au.com.thoughtpatterns.core.json.JsonyArray;
import au.com.thoughtpatterns.core.json.JsonyObject;
import au.com.thoughtpatterns.core.json.JsonyPrimitive;
import au.com.thoughtpatterns.core.util.Logger;


public class ObjectSchema extends Schema<JsonyObject> {
    
    private static final Logger log = Logger.get(ObjectSchema.class);

    private static final String MAX_PROPERTIES = "maxProperties";
    private static final String MIN_PROPERTIES = "minProperties";
    private static final String REQUIRED = "required";
    private static final String ADDITIONAL_PROPERTIES = "additionalProperties";
    private static final String PROPERTIES = "properties";
    private static final String PATTERN_PROPERTIES = "patternProperties";
    private static final String DEPENDENCIES = "dependencies";

    // Cache for property schemas
    private Map<String, List<Schema<?>>> propertySchemas = new HashMap<>();
    
    ObjectSchema() {
        super();
    }

    public Long getMaxProperties() {
        return getCast(MAX_PROPERTIES, Long.class);
    }
    
    public void setMaxProperties(Integer val) {
        set(MAX_PROPERTIES, Jsony.of(val));
    }
    
    public Long getMinProperties() {
        return getCast(MIN_PROPERTIES, Long.class);
    }
    
    public void setMinProperties(Integer val) {
        set(MIN_PROPERTIES, Jsony.of(val));
    }
    
    public JsonyArray<Jsony> getRequired() {
        return (JsonyArray<Jsony>) get(REQUIRED);
    }
    
    public void setRequired(JsonyArray val) {
        set(REQUIRED, val);
    }
    
    @Override public ValidationResult validate0(JsonyObject value, Pointer p) {
        
        ValidationResult r = super.validate0(value, p);
        
        validateMax(value, r, p);
        validateMin(value, r, p);
        validateRequired(value, r, p);
        validateProperties(value, r, p);
        validatePatterns(value, r, p);
        validateDependencies(value, r, p);
        
        return r;
    }
    
    void validateMax(final JsonyObject value, ValidationResult r, Pointer p) {
        Long limit = getMaxProperties();
        if (limit == null) {
            return;
        }
        int size = value.getPropertyNames().size();
        if (size > limit) {
            r.error("Too many properties", p);
        }
    }

    void validateMin(final JsonyObject value, ValidationResult r, Pointer p) {
        Long limit = getMinProperties();
        if (limit == null) {
            return;
        }
        int size = value.getPropertyNames().size();
        if (size < limit) {
            r.error("Too few properties", p);
        }
    }

    void validateRequired(final JsonyObject value, ValidationResult r, Pointer p) {
        JsonyArray<Jsony> required = getRequired();
        if (required == null) {
            return;
        }
        Set<String> props = value.getPropertyNames();
        for (Jsony req : getRequired()) {
            String prop = ((JsonyPrimitive<String>)req).getValue();
            if (! props.contains(prop)) {
                r.error("Missing property " + prop, p);
            }
        }
    }
    
    void validateProperties(final JsonyObject value, ValidationResult r, Pointer p) {
        
        Set<String> props = value.getPropertyNames();
        for (String prop : props) {
            Jsony val = value.get(prop);
            if (val != null) {
                List<Schema<?>> schemas = getSchemasForProperty(prop);
                for (Schema<?> schema : schemas) {
                    Pointer p2 = new Pointer(p, prop);
                    ValidationResult r2 = ((Schema<Jsony>)schema).validate(val, p2);
                    r.loadFrom(r2);
                }
            }
        }
    }
    
    public List<Schema<?>> getSchemasForProperty(String propertyName) {
        List<Schema<?>> schemas = propertySchemas.get(propertyName);

        if (schemas == null) {

            schemas = new ArrayList<>();
            
            // Explicit properties
            JsonyObject properties = (JsonyObject) get(PROPERTIES);
            if (properties != null) {
                Jsony schemaJ = properties.get(propertyName);
                if (schemaJ instanceof JsonyObject) {
                    Schema<Jsony> schema = (Schema<Jsony>) from((JsonyObject) schemaJ);
                    schemas.add(schema);
                }
            }
            
            // TODO patternProperties
            
            // Additional properties
            if (schemas.size() == 0) {
                Jsony addJ = get(ADDITIONAL_PROPERTIES);
                if (addJ instanceof JsonyObject) {
                    Schema<Jsony> schema = (Schema<Jsony>) from((JsonyObject) addJ);
                    schemas.add(schema);
                }
            }

            propertySchemas.put(propertyName, schemas);
        }

        return schemas;
    }

    void validatePatterns(final JsonyObject value, ValidationResult r, Pointer p) {
        // TODO
    }

    void validateDependencies(final JsonyObject value, ValidationResult r, Pointer p) {
        // TODO
    }

    @Override protected JsonyObject parse0(JsonyObject in) {
        AJsonyObject out = (AJsonyObject) super.parse0(in);
        out.loadFrom((AJsonyObject)in);
        
        JsonyObject properties = (JsonyObject) get(PROPERTIES);
        if (properties != null) {
            for (String prop : properties.getPropertyNames()) {
                
                Schema<Jsony> schema = (Schema<Jsony>) from((JsonyObject) properties.get(prop));
                
                Jsony val = out.get(prop);
                if (val != null) {
                    Jsony val2 = schema.parse(val);
                    out.set(prop, val2);
                }
            }
        }
        
        return out;
    }

    @Override public boolean coerce(Jsony value0, Pointer p) {
        log.debug("Coerce object at " + p);

        Set<String> prune = null;
        
        if (value0 instanceof JsonyObject) {
            JsonyObject value = (JsonyObject) value0;
            Set<String> props = value.getPropertyNames();
            for (String prop : props) {
                Jsony val = value.get(prop);
                if (val != null) {
                    List<Schema<?>> schemas = getSchemasForProperty(prop);
                    for (Schema<?> schema : schemas) {
                        Pointer p2 = new Pointer(p, prop);
                        boolean okay = ((Schema<Jsony>)schema).coerce(val, p2);
                        if (! okay) {
                            if (prune == null) {
                                prune = new HashSet<>();
                            }
                            prune.add(prop);
                        }
                    }
                }
            }
            if (prune != null) {
                for (String prop : prune) {
                    value.delete(prop);
                }
            }
        }
        return true;
    }
    
}
