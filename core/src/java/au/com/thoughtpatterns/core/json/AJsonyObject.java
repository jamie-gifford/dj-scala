package au.com.thoughtpatterns.core.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import au.com.thoughtpatterns.core.util.Pipe;
import au.com.thoughtpatterns.core.util.Util;


public class AJsonyObject implements JsonyObject {

    private final Map<String, Jsony> properties = new HashMap<>();

    @Override public String toJson() {
        StringBuffer buff = new StringBuffer();
        buff.append("{");
        Set<String> keys = properties.keySet();
        int size = keys.size();
        int cnt = 0;
        for (String p : keys) {
            cnt ++;
            Jsony val = properties.get(p);
            buff.append("\"").append(JsonUtils.escape(p)).append("\":").append( val != null ? val.toJson() : "null" );
            if (cnt < size) {
                buff.append(",");
            }
        }
        buff.append("}");
        String out = buff.toString();
        return out;
    }

    @Override public Jsony get(String property) {
        return properties.get(property);
    }

    @Override public void set(String property, Jsony val) {
        properties.put(property, val);
    }

    @Override public void delete(String property) {
        properties.remove(property);
    }

    @Override public <T> T getCast(String property, Class<T> cls) {
        Jsony val = get(property);
        if (cls.isInstance(val)) {
            return (T) val;
        }
        if (! (val instanceof AJsonyPrimitive<?>)) {
            return null;
        }
        AJsonyPrimitive<T> p = (AJsonyPrimitive<T>) val;
        T v = (T) p.getValue();
        return v;
    }

    @Override public <T> T getCast0(String property, Class<T> cls) {
        Jsony val = get(property);
        if (cls.isInstance(val)) {
            return (T) val;
        }
        if (! (val instanceof AJsonyPrimitive<?>)) {
            return null;
        }
        AJsonyPrimitive<?> p = (AJsonyPrimitive<?>) val;
        Object v = p.getValue();
        if (v == null) {
            return null;
        }
        if (! cls.isInstance(v)) {
            return null;
        }
        return (T) v;
    }

    public void loadFrom(AJsonyObject other) {
        if (properties == other.properties) {
            return;
        }
        properties.clear();
        properties.putAll(other.properties);
    }

    @Override public PrimitiveType getJsonType() {
        return PrimitiveType.OBJECT;
    }

    @Override public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override public String toString() {
        return toJson();
    }
    
    public boolean equals(Object other) {
        if (! (other instanceof AJsonyObject)) {
            return false;
        }
        AJsonyObject o = (AJsonyObject) other;
        return properties.equals(o.properties);
    }
    
    public int hashCode() {
        return properties.hashCode();
    }

    @Override public Jsony copy() {
        return (Jsony) Pipe.copy(this);
    }

    @Override public void set(String property, String val) {
        set(property, Jsony.of(val));
    }

    @Override public void set(String property, Integer val) {
        set(property, Jsony.of(val));
    }

    @Override public void set(String property, Long val) {
        set(property, Jsony.of(val));
    }

    @Override public void set(String property, Double val) {
        set(property, Jsony.of(val));
    }

    @Override public void set(String property, Boolean val) {
        set(property, Jsony.of(val));
    }
    
}
