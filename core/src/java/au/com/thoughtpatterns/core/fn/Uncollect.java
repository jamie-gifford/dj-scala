package au.com.thoughtpatterns.core.fn;

import java.util.Collection;

/**
 * Given Col(T) and g: T -> R, return Col(R)
 * @author james
 *
 */
public class Uncollect<T, R> implements Fn<Collection<R>> {

    private static final long serialVersionUID = 1L;
    private Ufn<T, R> g;
    private Fn<Collection<T>> collect;
    
    public Uncollect(Ufn<T, R> aG, Fn<Collection<T>> aCollect) {
        g = aG;
        collect = aCollect;
    }
    
    public Collection<R> eval() {
        Collection<Fn<R>> out = FnFactory.clone(collect.eval());
        
        for (T t : collect.eval()) {
            out.add(g.apply(t));
        }
        return new Collect<R>(out).eval();
    }
    
}
