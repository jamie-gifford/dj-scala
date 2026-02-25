package au.com.thoughtpatterns.core.lookup;

public abstract class SimpleLookup implements Lookup {

    private static final long serialVersionUID = 1L;
    private String code; // Not null
    private String description;
    
    public SimpleLookup(String aCode, String aDescription) {
        code = aCode;
        description = aDescription;
    }
    
    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public abstract LookupType getLookupType();

    public boolean equals(Object o) {
        if (! (o instanceof Lookup)) {
            return false;
        }
        
        Lookup other = (Lookup) o;
        return code.equals(other.getCode());
    }
    
    public int hashCode() {
        return code.hashCode();
    }
    
    public String toString() {
        return code;
    }

}
