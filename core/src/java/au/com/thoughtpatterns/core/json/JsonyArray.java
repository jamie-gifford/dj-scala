package au.com.thoughtpatterns.core.json;

import java.util.List;


public interface JsonyArray<T extends Jsony> extends Jsony, Iterable<T>, List<T> {

    int size();
    
    T get(int i);
    
    T set(int i, T val);
    
    boolean add(T val);
    
    
    
}
