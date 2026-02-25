package au.com.thoughtpatterns.core.json.schema;

import au.com.thoughtpatterns.core.json.AJsonyPrimitive;
import au.com.thoughtpatterns.core.json.Jsony;
import au.com.thoughtpatterns.core.json.JsonyPrimitive;

public class NullSchema extends Schema<AJsonyPrimitive<Void>> {

    NullSchema() {
        super();
    }

    @Override protected String prevalidate(Jsony value) {
        String str = super.prevalidate(value);
        if (str != null) {
            return str;
        }
        JsonyPrimitive<?> v = (JsonyPrimitive<?>)value;
        Object o = v.getValue();
        if (o != null) {
            return "unexpected non-null value when expecting null";
        }
        return null;
    }

    @Override protected AJsonyPrimitive<Void> parse0(AJsonyPrimitive<Void> in) {
        return in;
    }
    
    
}
