package au.com.thoughtpatterns.core.json.schema;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.com.thoughtpatterns.core.json.Jsony;
import au.com.thoughtpatterns.core.json.JsonyPrimitive;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.SystemException;

public class StringSchema extends Schema<JsonyPrimitive<String>> {

    private static final Logger log = Logger.get(StringSchema.class);
    
    private static final String MAX_LENGTH = "maxLength";
    private static final String MIN_LENGTH = "minLength";
    private static final String PATTERN = "pattern";
    
    // TODO consider garbage-collecting the cache if it gets too large
    private static final Map<String, Pattern> PATTERN_CACHE = new HashMap<>();
    
    StringSchema() {
        super();
    }

    @Override protected String prevalidate(Jsony value) {
        String str = super.prevalidate(value);
        if (str != null) {
            return str;
        }
        return prevalidatePrimitive(value, String.class);
    }

    public Long getMaxLength() {
        return getCast(MAX_LENGTH, Long.class);
    }

    public Long getMinLength() {
        return getCast(MIN_LENGTH, Long.class);
    }

    public String getPattern() {
        return getCast(PATTERN, String.class);
    }

    @Override public ValidationResult validate0(JsonyPrimitive<String> value, Pointer p) {

        ValidationResult r = super.validate0(value, p);
        
        if (typeOkay(value, p)) {
            validateMinLength(value, r, p);
            validateMaxLength(value, r, p);
            validatePattern(value, r, p);
        }
        
        return r;
    }

    void validateMinLength(final JsonyPrimitive<String> value, ValidationResult r, Pointer p) {
        Long limit = getMinLength();
        if (limit == null) {
            return;
        }
        
        String v = value.getValue();
        
        if (v != null && v.length() < limit) {
            r.error("Value length less than minimum", p);
        }
    }

    void validateMaxLength(final JsonyPrimitive<String> value, ValidationResult r, Pointer p) {
        Long limit = getMaxLength();
        if (limit == null) {
            return;
        }
        
        String v = value.getValue();
        
        if (v != null && v.length() > limit) {
            r.error("Value length greater than minimum", p);
        }
    }

    void validatePattern(final JsonyPrimitive<String> value, ValidationResult r, Pointer p) {
        Pattern pattern = getRE();
        if (pattern == null) {
            return;
        }
        
        String v = value.getValue();
        
        if (v == null) {
            return;
        }
        
        Matcher m = pattern.matcher(v);
        if (! m.find()) {
            r.error("Value does not match pattern " + getPattern(), p);
        }
    }
    
    private Pattern getRE() {
        String pattern = getPattern();
        if (pattern == null) {
            return null;
        }
        Pattern re = PATTERN_CACHE.get(pattern);
        if (re == null) {
            re = Pattern.compile(pattern);
            PATTERN_CACHE.put(pattern, re);
        }
        return re;
    }

    protected JsonyPrimitive<String> parse0(JsonyPrimitive<String> in) {
        
        Class<JsonyPrimitive<String>> cls = getJavaClass();
        if (cls == null) {
            return in;
        } else {
            try {
                Constructor<JsonyPrimitive<String>> constructor = cls.getConstructor(String.class);
                Object val = in.getValue();
                JsonyPrimitive<String> out = (JsonyPrimitive<String>) constructor.newInstance(val);
                return out;
            } catch (Exception ex) {
                throw new SystemException("Failed to find single-parameter constructor for " + cls, ex);
            }
        }
    }


    @Override public boolean coerce(Jsony value0, Pointer p) {
        log.debug("coerce string at " + p);
        boolean keep = true;
        if (value0 instanceof JsonyPrimitive<?>) {
            JsonyPrimitive<Object> value = (JsonyPrimitive<Object>)value0;
            
            log.debug("coercion found type " + value.getJsonType());
            
            if (prevalidate(value) != null) {
                
                // Wrong type, so try to coerce
                Object x = value.getValue();
                if (x != null) {
                    String n = x.toString();
                    value.setValue(n);
                } else {
                    keep = false;
                }
            }
        }
        return keep;
    }

}
