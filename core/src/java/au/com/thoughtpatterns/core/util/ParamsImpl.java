package au.com.thoughtpatterns.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParamsImpl extends AbstractParameters {

    private static Parameters INSTANCE;

    private static final String CORE = "thoughtpatterns_core.properties";

    private static final String APP = "thoughtpatterns.properties";

    private static final String LOCAL = "thoughtpatterns_local.properties";

    private static Pattern VARIABLE = Pattern.compile("\\$\\{[^\\}]+\\}");

    // Detect keys that end with +. This will work for properties like "x+=y".
    private static Pattern APPEND = Pattern.compile("^([^\\+]+)\\+");

    // Detect values that start with "+=". This will work if there is extra
    // whitespace
    // eg, "x += y"
    private static Pattern APPEND2 = Pattern.compile("^\\+=\\s*(.*)");

    static {
        initialise();
    }

    /**
     * The loaded state (raw). This does not include the backing state.
     */
    private HashMap<String, String> state = new HashMap<String, String>();
    
    /**
     * The "history" of each property in the state map
     */
    private HashMap<String, Stack<String>> history = new HashMap<String, Stack<String>>();

    /**
     * Indicates whether changes are permitted to this ParamsImpl instance
     */
    private boolean locked;
    
    /**
     * Cached, substituted state.
     */
    private HashMap<String, String> substituted = new HashMap<String, String>();
    private HashSet<String> substitutedKeys = new HashSet<String>();

    // -------------------------------------
    // State that is only used during the load operation
    
    private Stack<String> packages = new Stack<String>();
    private String currentPackage = "";
    
    // -------------------------------------
    
    static Parameters getInstance() {
        return INSTANCE;
    }

    /**
     * This constructor is for internal and testing purposes. Normal code should
     * use Parameters.instance() to get a pre-initialised Parameters object.
     */
    public ParamsImpl() {
        
    }

    /**
     * This constructor is for testing purposes. Normal code should use
     * Parameters.instance() to get a pre-initialised Parameters object.
     * 
     * @param resourceName name of properties file to load
     */
    public ParamsImpl(String resourceName, Parameters aBacking) {
        setBacking(aBacking);
        load(resourceName);
    }

    public String get(String key) {
        if (substitutedKeys.contains(key)) {
            return substituted.get(key);
        }

        String value = getInternal(key);

        value = substitute(value);
        substituted.put(key, value);
        substitutedKeys.add(key);

        return value;
    }
    
    public String getInternal(String key) {
        if (state.containsKey(key)) {
            String value = state.get(key);
            return value;
        }
        
        Parameters backing = getBacking();
        if (backing != null) {
            return backing.getInternal(key);
        }
        
        return null;
    }
    
    public Properties getProperties(String prefix, boolean truncate) {
        Properties p = new Properties();
        Set keys = getKeys();
        
        int prefixLength = prefix.length();
        for (Object keyo : keys) {
            String key = (String) keyo;
            if (key.startsWith(prefix)) {

                String key2;
                if (truncate) {
                    key2 = key.substring(prefixLength);
                } else {
                    key2 = key;
                }

                String value = get(key);
                p.put(key2, value);
            }
        }
        return p;
    }
    
    private static void initialise() {

        File working = new File(".").getAbsoluteFile();
        log("Loading parameters; working directory is " + working);

        // Simple implementation to start with - load resources from
        // 1. thoughtpatterns_core.properties
        // 2. thoughtpatterns.properties
        // 3. local_thoughtpatterns.properties
        // 4. file named in "thoughtpatterns_include" System property

        ParamsImpl core = new ParamsImpl(CORE, null);
        ParamsImpl app = new ParamsImpl(APP, core);
        ParamsImpl local = new ParamsImpl(LOCAL, app);

        core.setLocked(true);
        app.setLocked(true);
        local.setLocked(true);

        ParamsImpl top = local;
        String include = System.getProperty("thoughtpatterns_include");
        if (include != null) {
            top = new ParamsImpl(include, local);
            top.setLocked(true);
        }
        
        ParamsImpl system = loadSystemParams(top);
        
        INSTANCE = system;
    }

    private static ParamsImpl loadSystemParams(ParamsImpl backing) {
        ParamsImpl system = new ParamsImpl();
        system.setBacking(backing);
        Set<String> keys = backing.getKeys();
        boolean hasValue = false;
        for (String key : keys) {
            String value = System.getProperty(key);
            if (value != null) {
                system.loadParameter(key, value, "System property");
                hasValue = true;
            }
        }

        // -----------------------------------------
        // Add magic parameters
        String wd = new File(".").getAbsolutePath();
        system.loadParameter("wd", wd, "Magic property");
        hasValue = true;
        // -----------------------------------------
        
        if (hasValue) {
            system.setLocked(true);
            return system;
        } else {
            return backing;
        }
    }
    
    private void load(String resourceName) {
        // First look in classpath
        // then in working directory

        // Start off in the root package
        String pkg = "";
        
        pushPackage(pkg);

        try {
            Enumeration<URL> urls = getClass().getClassLoader().getResources(
                    resourceName);

            // Do the URLS in reverse order, to preserve classpath order

            Stack<URL> stack = new Stack<URL>();

            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                stack.push(url);
            }

            
            while (!stack.isEmpty()) {
                URL url = stack.pop();
                load(url);
            }

            // Now try working directory

            File file = new File(resourceName);
            if (file.exists() && file.isFile()) {
                URL url = file.toURL();
                load(url);
            }
            
        } catch (IOException ex) {
            throw new SystemException("Failed to load " + resourceName, ex);
        } finally {
            popPackage();
        }
    }

    private void load(URL url) throws IOException {

        log("Loading parameters from " + url.toExternalForm());

        InputStream stream = url.openStream();
        final String origin = url.toString();
        
        Properties parser = new Properties() {

            private static final long serialVersionUID = 1L;

            public synchronized Object put(Object key, Object value) {
                
                // XXX
//                log("...loading " + key + " = " + value + " from " + origin);
                
                loadParameter((String) key, (String) value, origin);
                return null;
            }

        };
        parser.load(stream);

    }

    private String getCurrentPackage() {
        return currentPackage;
    }
    
    private void pushPackage(String pkg) {
        packages.push(currentPackage);
        currentPackage = pkg;
    }
    
    private void popPackage() {
        currentPackage = packages.pop();
    }
    
    private void loadParameter(String key, String value, String origin) {
        // Intercept "magic" keys like "include".
        // Otherwise, put them into the state.
        // Also, handle appending if the key ends in a +

        // Trim the value
        if (value != null) {
            value = value.trim();
        }
        
        if ("include".equals(key)) {
            include(value);
            return;
        }
        
        if ("package".equals(key)) {
            setLoadingPackage(key, value);
            return;
        }
        
        if ("package+".equals(key)) {
            setLoadingPackage("package", "+=" + value);
            return;
        }
        
        String existing = null;

        // Prepend package
        if (currentPackage.length() > 0 && !(key.startsWith("."))) {
            key = currentPackage + "." + key;
        }

        if (key.startsWith(".")) {
            key = key.substring(1);
        }
        
        if (key.endsWith("+")) {
            Matcher matcher = APPEND.matcher(key);
            matcher.find();
            String prefix = matcher.group(1);

            key = prefix;
            existing = getInternal(key);
            
        } else if (value.startsWith("+=")) {
            Matcher matcher = APPEND2.matcher(value);
            matcher.find();

            value = matcher.group(1);
            existing = getInternal(key);
        }
        if (existing != null) {
            value = existing + "," + value;
        }

        state.put(key, value);
        recordOrigin(key, origin);
    }
    
    private void recordOrigin(String key, String origin) {
        Stack<String> h = history.get(key);
        if (h == null) {
            h = new Stack<String>();
            history.put(key, h);
        }
        h.add(0, origin);
    }

    private void include(String key) {
        load(key);
    }
    
    private void setLoadingPackage(String key, String value) {
        boolean relative = false;
        if (value.startsWith("+=")) {
            Matcher matcher = APPEND2.matcher(value);
            matcher.find();

            value = matcher.group(1);
            relative = true;
        }
        
        if (value == null || "".equals(value)) {
            // This is the "pop" case
            popPackage();
            return;
        }

        if (relative) {
            String current = getCurrentPackage();
            if (currentPackage.length() > 0) {
                value = currentPackage + "." + value;
            } 
        }

        // Special case for root package
        if (".".equals(value)) {
            value = "";
        }
        
        pushPackage(value);
    }

    public void set(String key, String value) {
        if (locked) {
            throw new SystemException("Parameters instance locked");
        }
//        state.put(key, value);
//        substitutedKeys.remove(key);
        // Clear cache
        clearCaches();
        loadParameter(key, value, "programatic");
    }
    
    public void setLocked(boolean isLocked) {
        locked = isLocked;
    }

    private void clearCaches() {
        substituted.clear();
        substitutedKeys.clear();
    }
    
    
    // ---------------------------------------
    // Variable substitution

    private String substitute(String value) {

        if (value == null) {
            return null;
        }

        do {
            Matcher m = VARIABLE.matcher(value);

            boolean hasVar = m.find();
            if (!hasVar) {
                break; // escape the loop
            }

            int start = m.start();
            int end = m.end();

            String varname = value.substring(start + 2, end - 1);

            String varvalue = get(varname);

            String before = value.substring(0, start);
            String after = value.substring(end);

            value = before + varvalue + after;

        } while (true);

        return value;
    }

    // Low level "logging" functionality. We don't depend on the Logger here (to
    // avoid circular
    // dependency)
    private static void log(String msg) {
        System.out.println(msg);
    }
    
    public Set<String> getKeys() {
        Set<String> keys = new HashSet<String>(state.keySet());
        if (getBacking() != null) {
            Set<String> backingKeys = getBacking().getKeys();
            keys.addAll(backingKeys);
        }
        return keys;
    }

    /**
     * Get a human-readable view of the state of the parameters object
     */
    public String dump() {
        List<String> strings = new ArrayList<String>();
        dump(strings);
        // Sort 
        Collections.sort(strings);
        StringBuffer out = new StringBuffer();
        for (String str : strings) {
            out.append(str).append("\n");
        }
        return out.toString();
    }

    public void dump(List<String> strings) {
        Set<String> keys = getKeys();
        for (String key : keys) {
            String origin = getHistory(key);
            String value = get(key);
            
            // Avoid passwords showing up in log files...
            if (key.toLowerCase().contains("password")) {
                value = "[password hidden]";
            }
            
            String line = key + " = " + value + " (" + origin + ")";
            strings.add(line);
        }
    }
    
    private String getHistory(String key) {
        List<String> trace = new ArrayList<String>();
        Parameters p = this;
        while (p instanceof ParamsImpl) {
            ParamsImpl pi = (ParamsImpl) p;
            List<String> ph = pi.history.get(key);
            if (ph != null) {
                trace.addAll(ph);
            }
            p = pi.getBacking();
        }
        String origin = Util.join("; ", trace);
        return origin;
    }
    
//    public void dump(List<String> strings) {
//
//        if (getBacking() != null) {
//            getBacking().dump(strings);
//        }
//        
//        for (Object key : state.keySet()) {
//            String value = (String) state.get(key);
//            String history = getHistory((String)key);
//            String line = key + " = " + value + " (" + history + ")";
//            strings.add(line);
//        }
//    }
    
//    private String getHistory(String key) {
//        Stack<String> stack = history.get(key);
//        if (stack == null) {
//            return null;
//        }
//        StringBuffer out = new StringBuffer();
//        for (int i = stack.size() - 1; i >= 0; i--) {
//            out.append(stack.get(i));
//            if (i > 0) {
//                out.append("; ");
//            }
//        }
//        return out.toString();
//    }
}
