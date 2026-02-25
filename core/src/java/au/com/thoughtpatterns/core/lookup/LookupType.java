package au.com.thoughtpatterns.core.lookup;

import java.util.List;

/**
 * Represents a set of Lookup values.
 */
public interface LookupType<L extends Lookup> {

    public List<L> getValues();
    
    /**
     * Return the Lookup value (of type L) for the given code, 
     * or null if no such lookup value exists
     */
    public L getByCode(String code);
    
}
