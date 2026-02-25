package au.com.thoughtpatterns.core.fn;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Function for finding union of sets of type T.
 */
public class Union<T> implements Ufn<Fn<Collection<Collection<T>>>, Collection<T>> {

    private static final long serialVersionUID = 1L;

    public Fn<Collection<T>> apply(final Fn<Collection<Collection<T>>> in) {
        return new Fn<Collection<T>>() {
            private static final long serialVersionUID = 1L;

            public Collection<T> eval() {
                // Need to return a collection of T from union of collections
                if (in.eval().size() == 0) {
                    return new ArrayList<T>();
                }
                Collection<T> out = FnFactory.clone(in.eval());
                for (Collection<T> inset : in.eval()) {
                    for (T t : inset) {
                        if (! (out.contains(t))) {
                            out.add(t);
                        }
                    }
                }
                return out;
            }
        };
    }

    
}
