package au.com.thoughtpatterns.core.json;

import java.io.Serializable;


public interface Jsony extends Serializable {

    public static enum PrimitiveType {
        
        ARRAY("array"),
        BOOLEAN("boolean"),
        INTEGER("integer"),
        NUMBER("number"),
        NULL("null"),
        OBJECT("object"),
        STRING("string");
        
        private String name;
        
        private PrimitiveType(String aName) {
            name = aName;
        }
        
        public String getName() {
            return name;
        }
    }
    
    public static <T> JsonyPrimitive<T> of(T val) {
        return new AJsonyPrimitive<T>(val);
    }
    
    public static Jsony wrap(Object val) {
        if (val instanceof Jsony) {
            return (Jsony) val;
        } else {
            return new AJsonyPrimitive<Object>(val);
        }
    }
    
    String toJson();
    
    PrimitiveType getJsonType();
    
    Jsony copy();
    
}
