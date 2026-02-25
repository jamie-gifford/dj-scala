package au.com.thoughtpatterns.core.json;


public interface JsonyPrimitive<T> extends Jsony {

    T getValue();
    
    void setValue(T val);
}
