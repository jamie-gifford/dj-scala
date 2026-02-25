package au.com.thoughtpatterns.core.fn;

import java.util.Collection;

/**
 * Given Ufn<T, R> and Collection<T>, produce Collection<R>
 */
public class Product<T, R> implements Fn<Collection<R>> {

    private static final long serialVersionUID = 1L;
    private Ufn<T, R> fn;
    private Fn<Collection<T>> collect;
    
    public Product(Ufn<T,R> aFn, Fn<Collection<T>> aCollect) {
        aFn = fn;
        collect = aCollect;
    }
    
    public Collection<R> eval() {
        Collection<R> out = FnFactory.clone(collect.eval());
        for (T t : collect.eval()) {
            Fn<R> rf = fn.apply(t);
            R r = rf.eval();
            out.add(r);
        }
        return out;
    }
}
