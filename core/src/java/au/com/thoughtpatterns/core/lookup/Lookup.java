package au.com.thoughtpatterns.core.lookup;

import java.io.Serializable;

/**
 * Represents a "lookup" value belonging to a particular 
 * LookupType. A lookup value is characterised by
 * a code and a description. The code is unique amongst
 * the Lookup vales of a LookupType.
 * 
 * The toString value of a Lookup is the code.
 */
public interface Lookup extends Serializable {

    public String getCode();
    public String getDescription();
    
    /**
     * Get the LookupType of this Lookup
     */
    public LookupType getLookupType();
    
}
