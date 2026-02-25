package au.com.thoughtpatterns.core.json.schema;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import au.com.thoughtpatterns.core.json.AJsonyObject;
import au.com.thoughtpatterns.core.json.JsonResolver;
import au.com.thoughtpatterns.core.json.Jsony;
import au.com.thoughtpatterns.core.json.JsonyArray;
import au.com.thoughtpatterns.core.json.JsonyException;
import au.com.thoughtpatterns.core.json.JsonyObject;
import au.com.thoughtpatterns.core.json.JsonyPrimitive;
import au.com.thoughtpatterns.core.json.JsonyVisitor;
import au.com.thoughtpatterns.core.json.JsonyVisitor.Path;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.SystemException;
import au.com.thoughtpatterns.core.util.Util;

public class Schema<T extends Jsony> extends AJsonyObject {

    private static final Logger log = Logger.get(Schema.class);

    private static final String TYPE = "type";

    private static final String REF = "$ref";

    private static final String ENUM = "enum";

    private static final String ALL_OF = "allOf";

    private static final String ANY_OF = "anyOf";

    private static final String ONE_OF = "oneOf";

    private static final String NOT = "not";

    // ----------------------------

    /**
     * Reference to a SchemaFactory which is common to the schema and its
     * subschemas.
     */
    private SchemaFactory factory;

    // ----------------------------------
    // Factory methods

    /**
     * Load a Schema from the given URI. The URI can have a non-empty fragment
     * identifier, in which case the fragment is navigated as a JSON Pointer.
     * 
     * @param schemaUri
     * @return schema
     */
    public static Schema<?> schemaFrom(URI schemaUri) {
        SchemaFactory f = new SchemaFactory();
        return f.load(schemaUri);
    }

    /**
     * Load a Schema from the given URL. This is a convenience method which
     * defers to {@link #schemaFrom(URI)}
     * 
     * @param schemaUrl
     * @return schema
     */
    public static Schema<?> schemaFrom(URL schemaUrl) {
        try {
            return (Schema<?>) schemaFrom(schemaUrl.toURI());
        } catch (URISyntaxException ex) {
            throw new JsonyException("Bad load from " + schemaUrl, ex);
        }
    }

    /**
     * Load a Schema from an Jsony object (ie, interpret the Jsony object as a
     * Schema), associating it with the given schemaUri.
     * 
     * @param schema a Jsony object to interpret as a Schema
     * @param schemaUri the URI to associate with the schema.
     * @return the interpreted Schema object
     */
    public static Schema<?> schemaFrom(Jsony schema, URI schemaUri) {
        SchemaFactory f = new SchemaFactory();
        return f.from0(schema, schemaUri);
    }

    public static Schema<Jsony> getMetaschema() {
        try {
            URI uri = Schema.class.getResource("metaschema.json").toURI();
            return (Schema<Jsony>) schemaFrom(uri);
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    /**
     * Convenience method for loading a schema from a resource relative to a given class. The URI can contain
     * a fragment identifier.
     * @param base the base class that the uri is take to be relative to.
     * @param uriString the relative URI, with optional fragment identifier.
     * @return the loaded schema or null if no such resource exists.
     */
    public static Schema<?> schemaFrom(Class base, String uriString) {
        try {
            URI uri = new URI(uriString);
            String path = uri.getSchemeSpecificPart();
            URL url = base.getResource(path);
            if (url == null) {
                return null;
            }
            URI uri2 = url.toURI();
            URI uri3 = new URI(uri2.getScheme(), uri2.getSchemeSpecificPart(), uri.getFragment());
            return schemaFrom(uri3);
        } catch (URISyntaxException ex) {
            throw new SystemException("Failed to load schema from " + uriString + " relative to " + base, ex);
        }
        
    }

    protected Schema() {
        super();
    }

    // ----------------------------------
    // Cached state

    private Set<String> acceptedTypes = null;

    // ----------------------------------

    public Schema<?> from(Jsony schema) {
        return factory.from(schema, this);
    }

    public ValidationResult selfValidate() {
        Schema<Jsony> meta = getMetaschema();
        ValidationResult r = meta.validate(this);
        return r;
    }

    /**
     * @return String or Array
     */
    public Jsony getType() {
        return get(TYPE);
    }

    /**
     * Get the "simple type" of the schema, or null if it doesn't have a simple
     * type.
     */
    public String getSimpleType() {
        Jsony j = get(TYPE);
        if (!(j instanceof JsonyPrimitive<?>)) {
            return null;
        }
        return ((JsonyPrimitive<String>) j).getValue();
    }

    public void setType(String aType) {
        set(TYPE, Jsony.of(aType));
        acceptedTypes = null;
    }

    public void setType(JsonyArray aTypes) {
        set(TYPE, aTypes);
        acceptedTypes = null;
    }

    public URI getResolutionScope() {
        return factory.resolutionScopes.get(this).resolutionScope;
    }

    public URI getSchemaLocation() {
        return factory.resolutionScopes.get(this).location();
    }

    /**
     * @return error message or null if okay
     */
    private String isTypeValid(String aType) {
        if (acceptedTypes == null) {
            acceptedTypes = new HashSet<>();

            final Jsony type = getType();
            if (type == null) {
                acceptedTypes.add("any");
            } else {

                if (type instanceof JsonyArray<?>) {

                    for (Jsony v : (JsonyArray<Jsony>) type) {
                        String t = ((JsonyPrimitive<String>) v).getValue();
                        acceptedTypes.add(t);
                    }

                } else {
                    // Must be string
                    String t = ((JsonyPrimitive<String>) type).getValue();
                    acceptedTypes.add(t);
                }
            }
        }

        // Special case: any
        if (acceptedTypes.contains("any")) {
            return null;
        }

        // Special case: number implies integer
        if (acceptedTypes.contains("number")) {
            acceptedTypes.add("integer");
        }

        if (acceptedTypes.contains(aType)) {
            return null;
        }

        return "Type " + aType + " not acceptable, required one of "
                + Util.join(", ", new ArrayList<>(acceptedTypes));

    }

    public JsonyArray<Jsony> getEnum() {
        return (JsonyArray<Jsony>) get(ENUM);
    }

    public void setEnum(JsonyArray<Jsony> val) {
        set(ENUM, val);
    }

    public JsonyArray<Jsony> getAllOf() {
        return (JsonyArray<Jsony>) get(ALL_OF);
    }

    public void setAllOf(JsonyArray<Jsony> val) {
        set(ALL_OF, val);
    }

    public JsonyArray<Jsony> getAnyOf() {
        return (JsonyArray<Jsony>) get(ANY_OF);
    }

    public void setAnyOf(JsonyArray<Jsony> val) {
        set(ANY_OF, val);
    }

    public JsonyArray<Jsony> getOneOf() {
        return (JsonyArray<Jsony>) get(ONE_OF);
    }

    public void setOneOf(JsonyArray<Jsony> val) {
        set(ONE_OF, val);
    }

    public JsonyObject getNot() {
        return (JsonyObject) get(NOT);
    }

    public void setNot(JsonyObject val) {
        set(NOT, val);
    }

    /**
     * Used internally to keep track of json pointer for error reporting
     */
    static class Pointer {

        String node;

        Pointer prev;

        Pointer(Pointer aPrev, String aNode) {
            node = aNode;
            prev = aPrev;
        }

        public String toString() {
            if (prev == null) {
                return node;
            } else {
                return prev + "/" + node;
            }
        }

    }

    public final ValidationResult validate(T value) {
        return validate(value, new Pointer(null, ""));
    }

    public final ValidationResult validate(T value, Pointer pointer) {
        String precheck = prevalidate(value);
        if (precheck != null) {
            ValidationResult r = new ValidationResult();
            r.error(precheck, pointer);
            return r;
        }
        return validate0(value, pointer);
    }

    /**
     * This method is here for use by the schemas for primitive types (ie, the
     * subclasses)
     */
    protected String prevalidatePrimitive(Jsony value, Class<?> primitiveClass) {
        JsonyPrimitive<?> v = (JsonyPrimitive<?>) value;
        Object o = v.getValue();
        if (o == null) {
            return "unexpected null value when expecting " + primitiveClass;
        }
        if (!(primitiveClass.isAssignableFrom(o.getClass()))) {
            return "value class " + o.getClass()
                    + " is not assignment-compatible with expected type " + primitiveClass;
        }
        return null;
    }

    /**
     * All subclasses should override to check
     * 
     * @return a String error message or null if okay
     */
    protected String prevalidate(Jsony value) {
        if (value == null) {
            return "bad type : unexpected null value";
        }
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type[] arguments = pt.getActualTypeArguments();
            // First argument should be the Jsony subtype
            Type jsonyType = arguments[0];

            if (jsonyType instanceof ParameterizedType) {
                jsonyType = ((ParameterizedType) jsonyType).getRawType();
            }

            Class<?> jsonyClass = null;
            if (jsonyType instanceof Class) {
                jsonyClass = (Class<?>) jsonyType;
            }

            if (jsonyClass != null && !jsonyClass.isAssignableFrom(value.getClass())) {
                return "value of " + value.getClass().toString()
                        + " is not assign-compatible with expected type " + jsonyClass.toString();
            }

        }

        return null;
    }

    protected ValidationResult validate0(T value, Pointer p) {
        ValidationResult r = new ValidationResult();

        validateEnum(value, r, p);
        validateType(value, r, p);
        validateAllOf(value, r, p);
        validateAnyOf(value, r, p);
        validateOneOf(value, r, p);
        validateNot(value, r, p);

        return r;
    }

    /**
     * An instance validates successfully against this keyword if its value is
     * equal to one of the elements in this keyword's array value.
     */
    void validateEnum(final T value, ValidationResult r, Pointer p) {

        final JsonyArray<Jsony> enums = getEnum();
        if (enums == null) {
            return;
        }

        for (Jsony v : enums) {
            if (Util.equals(v, value)) {
                return;
            }
        }
        r.error("Expected one of " + Util.join(", ", enums), p);
    }

    void validateType(final T value, ValidationResult r, Pointer p) {
        PrimitiveType type = value.getJsonType();
        String err = isTypeValid(type.getName());
        if (err == null) {
            return;
        } else {
            r.error(err, p);
        }
    }

    protected boolean typeOkay(T value, Pointer p) {
        ValidationResult r = new ValidationResult();
        validateType(value, r, p);
        return r.isValid();
    }

    void validateAllOf(final T value, ValidationResult r, Pointer p) {

        JsonyArray<Jsony> all = getAllOf();
        if (all == null) {
            return;
        }

        for (Jsony json : all) {
            Schema<T> s = (Schema<T>) factory.from(json, this);
            if (!s.validate(value, p).isValid()) {
                r.error("Type " + s + " not satisfied", p);
            }
        }

    }

    void validateAnyOf(final T value, ValidationResult r, Pointer p) {

        JsonyArray<Jsony> all = getAllOf();
        if (all == null) {
            return;
        }

        for (Jsony json : all) {
            Schema<T> s = (Schema<T>) factory.from(json, this);
            if (s.validate(value, p).isValid()) {
                return;
            }
        }

        r.error("Value did not implement any of required types", p);
    }

    void validateOneOf(final T value, ValidationResult r, Pointer p) {

        JsonyArray<Jsony> all = getOneOf();
        if (all == null) {
            return;
        }

        int cnt = 0;

        for (Jsony json : all) {
            Schema<T> s = (Schema<T>) factory.from(json, this);
            if (s.validate(value, p).isValid()) {
                cnt++;
            }
        }

        if (cnt == 1) {
            return;
        } else {
            r.error("Value implemented " + cnt + " types", p);
        }
    }

    void validateNot(final T value, ValidationResult r, Pointer p) {

        JsonyObject not = getNot();
        if (not == null) {
            return;
        }

        Schema<T> notSchema = (Schema<T>) factory.from(not, this);

        if (notSchema.validate(value, p).isValid()) {
            r.error("Value validated against " + notSchema, p);
        }

    }

    // ---------------------------------

    public static class ValidationResult {

        private List<String> list = new ArrayList<>();

        void error(String problem, Pointer pointer) {
            list.add(pointer + ": " + problem);
        }

        public boolean isValid() {
            return list.size() == 0;
        }

        public String invalidReason() {
            return Util.join(", ", list);
        }

        void loadFrom(ValidationResult other) {
            list.addAll(other.list);
        }

        public String toString() {
            if (isValid()) {
                return "valid";
            } else {
                return "invalid: " + Util.join(", ", list);
            }
        }

        public List<String> errors() {
            return list;
        }

    }

    // ---------------------------------
    // Parsing

    public T parse(T in) {
        return parse0(in);
    }

    protected T parse0(T in) {
        T out;
        Class<T> cls = getJavaClass();
        if (cls == null) {
            out = in;
        } else {
            try {
                out = cls.newInstance();
            } catch (Exception ex) {
                throw new JsonyException("Failed to instantiate " + cls, ex);
            }
        }
        return out;
    }

    protected Class<T> getJavaClass() {
        String cls = getCast("java", String.class);
        if (cls == null) {
            return null;
        } else {
            try {
                return (Class<T>) Class.forName(cls);
            } catch (ClassNotFoundException ex) {
                throw new JsonyException(cls + " missing", ex);
            }
        }
    }

    // --------------------------------------------
    // Transformation

    public boolean coerce(Jsony in, Pointer p) {
        // Subclasses override
        return true;
    }

    /**
     * Convenience version of the above method
     */
    public boolean coerce(Jsony in) {
        return coerce(in, null);
    }

    // --------------------------------------------

    private static class SchemaFactory {

        // Cached state:

        // URI -> schema mappings
        private Map<URI, Schema<?>> schemas = new HashMap<>();

        // Jsony schema -> real schema
        private Map<Jsony, Schema<?>> conversions = new HashMap<>();

        private JsonResolver resolver = new JsonResolver();

        /**
         * Resolution scopes for Jsony nodes. This cache is filled when a schema
         * is loaded.
         */
        private Map<Jsony, SchemaLocation> resolutionScopes = new HashMap<>();

        static class SchemaLocation {
            URI resolutionScope;
            String fragment;
            
            public String toString() {
                if (fragment != null) {
                    return resolutionScope.toString() + "#" + fragment;
                } else {
                    return resolutionScope.toString();
                }
            }
            
            public URI location() {
                URI base = resolutionScope;
                try {
                    URI location = new URI(base.getScheme(), base.getSchemeSpecificPart(), fragment);
                    return location;
                } catch (URISyntaxException impossible) {
                    throw new SystemException(impossible);
                }
            }
        }
        
        private SchemaFactory() {

        }

        /**
         * Load a schema based on URI. The URI can have a fragment identifier.
         * 
         * @param URI
         * @return the loaded schema
         */
        Schema<?> load(URI absUri) {

            URI abs = null;

            try {
                abs = new URI(absUri.getScheme(), absUri.getSchemeSpecificPart(), null);
            } catch (URISyntaxException impossible) {
                throw new JsonyException("Impossible problem with " + absUri, impossible);
            }

            Schema<?> t;

            // Resolve root schema
            Jsony j0 = resolver.getRef(abs);
            Schema<?> t0 = from0(j0);
            captureScopes(t0, abs);

            // Resolve relative schema (ie, follow fragment if any)
            Jsony j = resolver.getRef(absUri);
            if (j == null) {
            	throw new JsonyException("JSON schema not found at " + absUri);
            }
            t = from(j, t0);

            return t;
        }

        public Schema<?> from(Jsony schema, Schema<?> rootSchema) {

            Schema<?> out = conversions.get(schema);
            if (out != null) {
                return out;
            }

            AJsonyObject src = (AJsonyObject) schema;

            Jsony ref = src.get(REF);
            if (ref != null && ref instanceof JsonyPrimitive<?>) {
                Object ref1 = ((JsonyPrimitive<?>) ref).getValue();
                if (ref1 instanceof String) {
                    try {
                        URL base = rootSchema.getResolutionScope().toURL();
                        URL ref2 = new URL(base, (String) ref1);
                        out = load(ref2.toURI());
                    } catch (MalformedURLException | URISyntaxException impossible) {
                        throw new SystemException(impossible);
                    }
                }
            }

            if (out == null) {
                out = from0(src, rootSchema);
            }

            conversions.put(schema, out);

            return out;
        }

        /**
         * To be used for "attaching" JSON schemas that have already been
         * externally loaded.
         * 
         * @param schemaJson
         * @param schemaUri
         */
        private Schema<?> from0(Jsony schemaJson, URI schemaUri) {
            Schema<?> target = from0(schemaJson);

            if (schemaUri == null) {
                // Create new, temporary URI
                schemaUri = createTmpUri();
            }

            captureScopes(target, schemaUri);
            resolver.register(schemaUri, schemaJson);
            schemas.put(schemaUri, target);

            return target;
        }

        private Schema<?> from0(Jsony src, Schema<?> rootSchema) {
            Schema<?> target = from0(src);

            if (rootSchema == null) {
                // Create new, temporary URI
                URI r = createTmpUri();
                captureScopes(target, r);
                resolver.register(r, src);
                schemas.put(r, target);

                captureScopes(target, r);
            }

            return target;
        }

        /**
         * Create a temporary URI that can be stored in the resolver. We use the
         * file: protocol since Java will insist on a "known" protocol when
         * converting from URI to URL. But the URL will not actually resolve to
         * a real file; it's just a key used in the resolver cache.
         */
        private URI createTmpUri() {
            URI r = URI.create("file://tmp/schema-createTmpUri/" + UUID.randomUUID().toString());
            return r;
        }

        /**
         * Factory-internal use only
         */
        private Schema<?> from0(Jsony obj) {
            AJsonyObject src = (AJsonyObject) obj;
            JsonyPrimitive<String> t = (JsonyPrimitive<String>) src.get(TYPE);
            String type = t != null ? t.getValue() : "";

            Schema<?> target;
            switch (type) {
            case "array":
                target = new ArraySchema();
                break;
            case "boolean":
                target = new BooleanSchema();
                break;
            case "integer":
                target = new IntegerSchema();
                break;
            case "number":
                target = new NumberSchema();
                break;
            case "null":
                target = new NullSchema();
                break;
            case "object":
                target = new ObjectSchema();
                break;
            case "string":
                target = new StringSchema();
                break;
            default:
                target = new Schema<Jsony>();
            }

            target.loadFrom(src);
            target.factory = this;
            return target;
        }

        class ScopeResolver implements JsonyVisitor {

            Stack<URI> scopeStack = new Stack<>();

            ScopeResolver(URI initialScope) {
                scopeStack.push(initialScope);
            }

            void resolve(Jsony node, Path path) {
                
                boolean pop = false;
                
                if (node instanceof JsonyObject) {
                    JsonyObject obj = (JsonyObject) node;
                    Jsony id = obj.get("id");
                    if (id instanceof JsonyPrimitive) {
                        String uid = ((JsonyPrimitive<String>) id).getValue();
                        URI scope = scopeStack.peek();
                        URI newScope = resolve(scope, uid);
                        scopeStack.push(newScope);
                        resolver.register(newScope, node);
                        pop = true;
                    }
                }

                URI scope = scopeStack.peek();
                SchemaLocation loc = new SchemaLocation();
                loc.resolutionScope = scope;
                loc.fragment = path.toFragment();
                resolutionScopes.put(node, loc);
                
                log.debug("Schema location " + loc + " for " + node);
                
                JsonyVisitor.visitChildren(node, this, path);
                
                if (pop) {
                    scopeStack.pop();
                }
            }

            @Override public void visit(Jsony node, Path path) {
                resolve(node, path);
            }

            private URI resolve(URI base, String id) {
                try {
                    return new URL(base.toURL(), id).toURI();
                } catch (Exception ex) {
                    throw new SystemException(ex);
                }
            }

        }

        private void captureScopes(Jsony obj, URI resolutionScope) {

            if (resolutionScopes.keySet().contains(obj)) {
                return;
            }

            ScopeResolver r = new ScopeResolver(resolutionScope);
            Path path = JsonyVisitor.ROOT;
            r.resolve(obj, path);

        }
    }

}
