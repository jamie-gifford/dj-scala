package au.com.thoughtpatterns.core.bo;

import java.util.ArrayList;
import java.util.List;


public abstract class FilterOneToMany<T extends IEntity> extends OneToMany<T> {

    private static final long serialVersionUID = 1L;

    private OneToMany<T> backing;
    
    public FilterOneToMany(IEntity owner, OneToMany<T> aBacking) {
        super(owner, aBacking.getTargetClass());
        backing = aBacking;
    }

    @Override
    protected void loadInt() {
        List<T> list = backing.get();
        
        contents = new ArrayList<T>();
        
        for (T t : list) {
            if (accept(t)) {
                contents.add(t);
            }
        }
        
        getBox().join(this);
    }

    @Override
    protected boolean possiblyBelongs(IEntity candidate) {
        return backing.possiblyBelongs(candidate);
    }

    @Override
    protected boolean belongs(T candidate) {
        return backing.belongs(candidate) && accept(candidate);
    }
    
    protected abstract boolean accept(T candidate);

    @Override
    protected Query query() {
        // Unused
        return null;
    }
    
    
}
