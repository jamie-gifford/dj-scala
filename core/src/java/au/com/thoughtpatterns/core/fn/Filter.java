package au.com.thoughtpatterns.core.fn;

import java.util.Collection;


public class Filter<T> implements Fn<Collection<T>> {

    private static final long serialVersionUID = 1L;
    private Fn<Collection<T>> in;
    private Ufn<T, Boolean> filter;
    
    public Filter(Fn<Collection<T>> aIn, Ufn<T, Boolean> aFilter) {
        in = aIn;
        filter = aFilter;
    }
    
    public Collection<T> eval() {
        Collection<T> out = FnFactory.clone(in.eval());
        for (T t : in.eval()) {
            if (Boolean.TRUE.equals(filter.apply(t).eval())) {
                out.add(t);
            }
        }
        return out;
    }
    
}
