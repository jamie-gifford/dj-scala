package au.com.thoughtpatterns.core.fn;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

/**
 * Lazy collection (readonly)
 */
public class LazyCollection<T> extends AbstractCollection<T> {

    private Collection<Fn<T>> backing;

    public LazyCollection(Collection<Fn<T>> aBacking) {
        backing = aBacking;
    }
    
    @Override
    public Iterator<T> iterator() {
        
        final Iterator<Fn<T>> iter = backing.iterator();
        
        return new Iterator<T>() {

            public boolean hasNext() {
                return iter.hasNext();
            }

            public T next() {
                return iter.next().eval();
            }

            public void remove() {
                iter.remove();
            }
            
        };
    }

    @Override
    public int size() {
        return backing.size();
    }
}
