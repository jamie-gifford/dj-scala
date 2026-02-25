package au.com.thoughtpatterns.core.util;

import java.util.Properties;


public abstract class AbstractParameters extends Parameters {

    private Parameters backing;
    
    public String[] getArray(String key) {
        String value = get(key);
        if (value == null) {
            return new String[0];
        }
        String[] values = value.split("\\s*,\\s*");
        return values;
    }

    public int getInt(String key) {
        String value = get(key);
        return Integer.parseInt(value);
    }

    public long getLong(String key) {
        String value = get(key);
        return Long.parseLong(value);
    }

    public boolean getBoolean(String key) {
        String value = get(key);
        return Boolean.parseBoolean(value);
    }

    public Properties getProperties(String prefix) {
        return getProperties(prefix, false);
    }

    public void setBacking(Parameters aBacking) {
        backing = aBacking;
    }

    public Parameters getBacking() {
        return backing;
    }
}
