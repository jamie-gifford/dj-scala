package au.com.thoughtpatterns.core.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An extremely simple implementation of LookupType.
 * It is intended to be subclassed and used in a singleton
 * pattern. The subclasses's constructor should register lookup
 * values by calling the add method.
 */
public class SimpleLookupType<L extends Lookup> implements LookupType<L> {

    private List<L> values = new ArrayList<L>();
    private Map<String, L> byCode = new HashMap<String, L>();
    
    public L getByCode(String code) {
        if (code == null) {
            return null;
        }
        return byCode.get(code);
    }

    public List<L> getValues() {
        return values;
    }
    
    protected void add(L l) {
        values.add(l);
        byCode.put(l.getCode(), l);
    }    
}
