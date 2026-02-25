package au.com.thoughtpatterns.core.fn;

import java.util.Collection;

/**
 * Find first element of list that matches filter
 */
public class First<T> implements Fn<T> {

    private static final long serialVersionUID = 1L;
    private Fn<Collection<T>> in;
    private Ufn<T, Boolean> filter;
    
    public First(Fn<Collection<T>> aIn, Ufn<T, Boolean> aFilter) {
        in = aIn;
        filter = aFilter;
    }
    
    public T eval() {
        for (T t : in.eval()) {
            if (Boolean.TRUE.equals(filter.apply(t).eval())) {
                return t;
            }
        }
        return null;
    }
    
}
