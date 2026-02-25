package au.com.thoughtpatterns.core.json.schema;

import au.com.thoughtpatterns.core.json.Jsony;
import au.com.thoughtpatterns.core.json.JsonyPrimitive;

public class BooleanSchema extends Schema<JsonyPrimitive<Boolean>> {

    BooleanSchema() {
        super();
    }

    @Override protected String prevalidate(Jsony value) {
        String str = super.prevalidate(value);
        if (str != null) {
            return str;
        }
        return prevalidatePrimitive(value, Boolean.class);
    }

    @Override public ValidationResult validate0(JsonyPrimitive<Boolean> value, Pointer p) {
        
        ValidationResult r = super.validate0(value, p);
        
        return r;
    }

    protected JsonyPrimitive<Boolean> parse0(JsonyPrimitive<Boolean> in) {
        return in;
    }

}
