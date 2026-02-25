package au.com.thoughtpatterns.core.json.schema;

import java.util.ArrayList;
import java.util.List;

import au.com.thoughtpatterns.core.json.AJsonyArray;
import au.com.thoughtpatterns.core.json.Jsony;
import au.com.thoughtpatterns.core.json.JsonyArray;
import au.com.thoughtpatterns.core.json.JsonyObject;
import au.com.thoughtpatterns.core.json.JsonyPrimitive;
import au.com.thoughtpatterns.core.util.Logger;

public class ArraySchema extends Schema<JsonyArray<?>> {
    
    private static final Logger log = Logger.get(ArraySchema.class);

    private static final String ITEMS = "items";
    private static final String ADDITIONAL_ITEMS = "additionalItems";

    ArraySchema() {
        super();
    }
    
    @Override public ValidationResult validate0(JsonyArray<?> value, Pointer p) {
        
        ValidationResult r = super.validate0(value, p);

        validateItems(value, r, p);
        validateChildren(value, r, p);

        // TODO finish this

        return r;
    }

    private Schema<Jsony> isHomogeneous() {

        Jsony items = get(ITEMS);
        Jsony additional = get(ADDITIONAL_ITEMS);

        if (additional != null && additional instanceof JsonyPrimitive<?>) {
            Object x = ((JsonyPrimitive<?>)additional).getValue();
            if (x instanceof Boolean && ((Boolean)x)) {
                return null; // Not homogenous
            }
        }
        
        if (items == null || ! (items instanceof JsonyObject)) {
            return null; // Not homogenous or typed
        }
        
        Schema<Jsony> s = (Schema<Jsony>) from(items);
        return s;
    }
    
    void validateItems(final JsonyArray<?> value, ValidationResult r, Pointer p) {

        Jsony items = get(ITEMS);
        Jsony additional = get(ADDITIONAL_ITEMS);
        
        if (items == null || items instanceof JsonyObject) {
            // Validation always succeeds
            return;
        }
        
        if (additional == null) {
            return;
        }
        
        if (! (additional instanceof JsonyPrimitive<?>)) {
            return;
        }
        
        Object x = ((JsonyPrimitive<?>)additional).getValue();
        if (! (x instanceof Boolean)) {
            return;
        }

        Boolean b = (Boolean) x;
        if (b) {
            return;
        }
        
        if (! (items instanceof JsonyArray)) {
            return;
        }
        
        JsonyArray<?> arr = (JsonyArray<?>) items;
        int max = arr.size();
        
        if (value.size() <= max) {
            return;
        }
        
        r.error("Array size " + value.size() + " > " + max, p);
    }

    class Validator implements Visitor {

        ValidationResult r;
        
        Validator(ValidationResult r) {
            this.r = r;
        }
        
        @Override public void visit(Schema<Jsony> s, Jsony v, Pointer p) {
            
            s.validate(v);
            ValidationResult r2 = s.validate(v);
            r.loadFrom(r2);
            
        }
    }
    
    void validateChildren(final JsonyArray<?> value, ValidationResult r, Pointer p) {
        Validator validator = new Validator(r);
        visit(value, validator, p);
    }

    class Coercer implements Visitor {

        List<Jsony> prune = null;
        
        @Override public void visit(Schema<Jsony> s, Jsony v, Pointer p) {
            boolean okay = s.coerce(v, p);
            if (! okay) {
                if (prune == null) {
                    prune = new ArrayList<>();
                }
                prune.add(v);
            }
        }
        
        void prune(JsonyArray<?> value) {
            if (prune != null) {
                for (Jsony v : prune) {
                    value.remove(v);
                }
            }
        }
        
    }
    
    public boolean coerce(Jsony value0, Pointer p) {
        log.debug("Coerce array at " + p);
        if (value0 instanceof JsonyArray<?>) {
            JsonyArray<?> value = (JsonyArray<?>) value0;
            Coercer c = new Coercer();
            visit(value, c, null);
            c.prune(value);
        }
        return true;
    }

    private interface Visitor {
        
        void visit(Schema<Jsony> s, Jsony v, Pointer p);
        
    }
    
    void visit(final JsonyArray<?> value, Visitor visitor, Pointer p) {

        Jsony items = get(ITEMS);
        Jsony additional = get(ADDITIONAL_ITEMS);

        if (items == null) {
            return;
        }
        
        if (items instanceof JsonyObject) {
            Schema<Jsony> s = (Schema<Jsony>) from(items);
            for (int i = 0; i < value.size(); i++) {

                Pointer p2 = new Pointer(p, "" + (i + 1));
                Jsony v = value.get(i);
                visitor.visit(s, v, p2);
                
            }
        } else if (items instanceof JsonyArray) {
            
            JsonyArray<?> itemsArr = (JsonyArray<?>) items;
            
            Schema<Jsony> additionalSchema = null;
            if (additional != null && additional instanceof JsonyObject) {
                additionalSchema = (Schema<Jsony>) from(additional);
            }
            
            // First items must validate against items. Later, must validate against additionalItems

            for (int i = 0; i < value.size(); i++) {
                Jsony v = value.get(i);
                
                Pointer p2 = new Pointer(p, "" + (i + 1));
                if (i < itemsArr.size()) {
                    
                    Jsony item = itemsArr.get(i);
                    Schema<Jsony> s = (Schema<Jsony>) from(item);

                    visitor.visit(s, v, p2);
                    
                } else {
                    
                    if (additionalSchema != null) {
                        visitor.visit(additionalSchema, v, p2);
                    }
                }
            }
        }
    }
    
    
    @Override protected JsonyArray<?> parse0(JsonyArray<?> in) {
        AJsonyArray<Jsony> out = (AJsonyArray<Jsony>) super.parse0(in);
        out.loadFrom((AJsonyArray<Jsony>)in);
        
        Schema<Jsony> schema = isHomogeneous();
        if (schema != null) {
            for (int i = 0; i < out.size(); i++) {
                Jsony j = out.get(i);
                Jsony k = schema.parse(j);
                out.set(i, k);
            }
        }
        
        return out;
    }
    
}
