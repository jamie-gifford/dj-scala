package au.com.thoughtpatterns.core.fn;

import java.util.Collection;

/**
 * Given Collection<Fn<T>>, produce Fn<Collection<T>>
 * 
 * @param <T>
 */
public class Collect<T> implements Fn<Collection<T>> {

    private static final long serialVersionUID = 1L;
    private Collection<Fn<T>> collect;
    
    public Collect(Collection<Fn<T>> collect) {
        this.collect = collect;
    }
    
    public Collection<T> eval() {
        return new LazyCollection<T>(collect);
    }
    
}
