package au.com.thoughtpatterns.core.json.schema;

import au.com.thoughtpatterns.core.json.Jsony;
import au.com.thoughtpatterns.core.json.JsonyPrimitive;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.Util;


public class NumberSchema extends Schema<JsonyPrimitive<Number>> {

    private static final Logger log = Logger.get(NumberSchema.class);
    
    private static final String MIN = "minimum";
    private static final String MIN_EXCLUSIVE = "exclusiveMinimum";

    private static final String MAX = "maximum";
    private static final String MAX_EXCLUSIVE = "exclusiveMaximum";

    private static final String MULTIPLE_OF = "multipleOf";
    
    NumberSchema() {
        super();
    }

    @Override protected String prevalidate(Jsony value) {
        String str = super.prevalidate(value);
        if (str != null) {
            return str;
        }
        return prevalidatePrimitive(value, Number.class);
    }

    public Long getMinimum() {
        return getCast(MIN, Long.class);
    }
    
    public void setMinimum(Long val) {
        set(MIN, Jsony.of(val));
    }
    
    public Boolean getMinimumExclusive() {
        return getCast(MIN_EXCLUSIVE, Boolean.class);
    }
    
    public void setMinimumExclusive(Boolean val) {
        set(MIN_EXCLUSIVE, Jsony.of(val));
    }

    public Long getMaximum() {
        return getCast(MAX, Long.class);
    }
    
    public void setMaximum(Long val) {
        set(MAX, Jsony.of(val));
    }
    
    public Boolean getMaximumExclusive() {
        return getCast(MAX_EXCLUSIVE, Boolean.class);
    }
    
    public void setMaximumExclusive(Boolean val) {
        set(MAX_EXCLUSIVE, Jsony.of(val));
    }

    public Long getMultipleOf() {
        return getCast(MULTIPLE_OF, Long.class);
    }
    
    public void setMultipleOf(Long val) {
        set(MULTIPLE_OF, Jsony.of(val));
    }
    

    @Override public ValidationResult validate0(JsonyPrimitive<Number> value, Pointer p) {
        
        ValidationResult r = super.validate0(value, p);
        
        validateMinimum(value, r, p);
        validateMaximum(value, r, p);
        validateMultipleOf(value, r, p);
        
        return r;
    }

    void validateMinimum(final JsonyPrimitive<Number> value, ValidationResult r, Pointer p) {
        Number limit = getMinimum();
        if (limit == null) {
            return;
        }
        
        Number v = value.getValue();
        
        if (v != null && v.doubleValue() < limit.doubleValue()) {
            r.error("Value less than minimum", p);
        }

        Boolean exclusive = getMinimumExclusive();
        if (Util.isTrue(exclusive)) {
            if (v != null && v.doubleValue() == limit.doubleValue()) {
                r.error("Value equal to exclusive minimum", p);
            }
        }

    }
    
    void validateMaximum(final JsonyPrimitive<Number> value, ValidationResult r, Pointer p) {
        Number limit = getMaximum();
        if (limit == null) {
            return;
        }
        
        Number v = value.getValue();
        
        if (v != null && v.doubleValue() > limit.doubleValue()) {
            r.error("Value greater than maximum", p);
        }

        Boolean exclusive = getMaximumExclusive();
        if (Util.isTrue(exclusive)) {
            if (v != null && v.doubleValue() == limit.doubleValue()) {
                r.error("Value equal to exclusive maximum", p);
            }
        }

    }
    
    void validateMultipleOf(final JsonyPrimitive<Number> value, ValidationResult r, Pointer p) {
        Number limit = getMultipleOf();
        if (limit == null) {
            return;
        }
        
        Number v0 = value.getValue();
        
        if (v0 == null) {
            return;
        }
        
        double v = v0.doubleValue(); 
        
        int l = limit.intValue();
        
        double z = ((double)v) / l;
        
        z = Math.round(z);
        
        double v2 = ((double)z) * l;
        
        if (v2 != v) {
            r.error("Value not multiple of " + limit, p);
        }
    }

    protected JsonyPrimitive<Number> parse0(JsonyPrimitive<Number> in) {
        return in;
    }

    @Override public boolean coerce(Jsony value0, Pointer p) {
        log.debug("coerce number at " + p);
        boolean keep = true;
        if (value0 instanceof JsonyPrimitive<?>) {
            JsonyPrimitive<Object> value = (JsonyPrimitive<Object>)value0;
            
            log.debug("coercion found type " + value.getJsonType());
            
            if (prevalidate(value) != null) {
                
                // Wrong type, so try to coerce
                Object x = value.getValue();
                
                if (x == null || x.equals("")) {
                    keep = false;
                } else {
                    try {
                        Number n = Double.parseDouble(x.toString());
                        value.setValue(n);
                    } catch (NumberFormatException ex) {
                        // Coercion failed...
                    }
                }
            }
        }
        return keep;
    }
    
}
