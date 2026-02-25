package au.com.thoughtpatterns.core.fn;

import java.util.Collection;

public class Difference<T> implements Fn<Collection<T>> {

    private static final long serialVersionUID = 1L;
    private Fn<Collection<T>> a;
    private Fn<Collection<T>> b;
    
    public Difference(Fn<Collection<T>> aA, Fn<Collection<T>> aB) {
        a = aA;
        b = aB;
    }

    public Collection<T> eval() {
        Collection<T> out = FnFactory.clone(a.eval());
        out.addAll(a.eval());
        out.removeAll(b.eval());
        return out;
    }
    
}
