package au.com.thoughtpatterns.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for filtering lists.
 */
public abstract class CollectionFilter<T> {

    protected abstract boolean belongs(T t);
    
    public List<T> filter(List<T> list) {
        ArrayList<T> filtered = new ArrayList<T>();
        for (T t: list) {
            if (belongs(t)) {
                filtered.add(t);
            }
        }
        return filtered;
    }
    
}
