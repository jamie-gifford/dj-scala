package au.com.thoughtpatterns.core.json;

import au.com.thoughtpatterns.core.util.Pipe;
import au.com.thoughtpatterns.core.util.Util;


public class AJsonyPrimitive<T> implements JsonyPrimitive<T> {

    private T value;

    public AJsonyPrimitive(T val) {
        value = val;
    }

    @Override public String toJson() {
        if (value == null) {
            return "null";
        } else if (value instanceof Number) {
            return ((Number)value).toString();
        } else if (value instanceof Boolean) {
            return ((Boolean)value) ? "true" : "false";
        } else {
            return "\"" + JsonUtils.escape(value.toString()) + "\"";
        }
    }

    @Override public T getValue() {
        return value;
    }
    
    @Override public void setValue(T val) {
        value = val;
    }
    
    @Override public PrimitiveType getJsonType() {
        if (value == null) {
            return PrimitiveType.NULL;
        } else if (value instanceof Long) {
            return PrimitiveType.INTEGER;
        } else if (value instanceof Number) {
            return PrimitiveType.NUMBER;
        } else if (value instanceof Boolean) {
            return PrimitiveType.BOOLEAN;
        } else {
            return PrimitiveType.STRING;
        }
    }
    
    @Override public String toString() {
        return toJson();
    }

    public boolean equals(Object other) {
        if (! (other instanceof AJsonyPrimitive)) {
            return false;
        }
        AJsonyPrimitive<?> o = (AJsonyPrimitive<?>) other;
        return Util.equals(getValue(), o.getValue());
    }
    
    public int hashCode() {
        T value = getValue();
        return value != null ? value.hashCode() : 1;
    }

    @Override public Jsony copy() {
        return (Jsony) Pipe.copy(this);
    }

}
