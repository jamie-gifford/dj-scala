package au.com.thoughtpatterns.core.util;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

/**
 * Runtime parameters support.
 * 
 * Parameters are loaded, in order, into a map of key-value pairs. Redefinitions
 * of parameters overwrite previous parameters.
 * 
 * <h2>Resource loading</h2>
 * 
 * Resources (ie, files) can be loaded from the classpath or from the
 * filesystem. They are checked in both places - relative to the root package in
 * the classpath and relative to the working directory in the filesystem.
 * 
 * <p>
 * 
 * There are some directives that influence the loading.
 * 
 * <ul>
 * <li> include = name. If a parameter called "include" is defined, the named
 * resource is loaded immediately</li>
 * </ul>
 * 
 * <h2>Variable substitution</h2>
 * 
 * After all parameters are loaded, any strings of the form "${key}" are
 * substituted for the corresponding parameter value. This is done recursively.
 * Note that variable substitution does *not* happen on parameter names, just on
 * parameter values.
 * 
 * <h2>Concatenating parameters</h2>
 * 
 * A parameter definition of the form "x+=y" will append the value to an
 * existing parameter (separating values with commas). So this:
 * 
 * <pre>
 *   x = Apples
 *   x += Oranges
 * </pre>
 * 
 * is equivalent to this:
 * 
 * <pre>
 *   x = Apples,Oranges
 * </pre>
 * 
 * <b>Caution: </b> don't try starting a parameter value with "+=" - the parser
 * will get confused and think that you are trying to do a concatenation.
 * 
 * <h2>Packages</h2>
 * 
 * Parameter names are arranged into "packages", separated by dots.
 * 
 * <p />
 * Also, when defining parameter, a package can be defined by using the
 * <code>package=a.b.c</code> declaration. This can be undone with a
 * <code>package=</code> declaration, which "pops" the last package
 * declaration.
 * 
 * The package can be set <em>relative</em> to the current package by using
 * <code>package+=a.b.c</code>
 * 
 * The root package can be set with <code>package=.</code>
 * 
 * Parameter keys that begin with a dot are considered to be "global"
 * definitions and the current package is ignored.
 * 
 * All parameter files are interpreted initially as loading from the root
 * package, even if they are included from another file within a package scope.
 * 
 * <p />
 * Magic parameters:
 * 
 * "wd" is the working directory of the process.
 * 
 */
public abstract class Parameters {

    private static AppContext<Stack<Parameters>> contextualParams = new AppContext<Stack<Parameters>>() {

        protected Stack<Parameters> initialValue() {
            return new Stack<Parameters>();
        }

    };

    // ------------------------------
    // Factory methods

    /**
     * Get the "standard" global Parameters instance. This is the method that
     * normal code should use to obtain runtime parameters.
     * 
     * This parameters instance obtains parameters from the following resources
     * (in order):
     * 
     * <ol>
     * <li>Files called "thoughtpatterns_core.properties", in the classpath and
     * in the working directory</li>
     * <li>Files called "thoughtpatterns.properties", in the classpath and in
     * the working directory</li>
     * <li>Files called "thoughtpatterns_local.properties", in the classpath
     * and in the working directory</li>
     * <li>System properties</li>
     * </ol>
     * 
     * System properties can only be used to re-define a parameter that is
     * defined in one of the earlier properties files. New parameters cannot be
     * introduced via system properties.
     */
    public static Parameters instance() {
        if (contextualParams == null) {
            return ParamsImpl.getInstance();
        }
        Stack<Parameters> stack = contextualParams.get();
        if (stack.isEmpty()) {
            return ParamsImpl.getInstance();
        }
        return stack.peek();
    }

    // -------------------------------
    // Contextual API

    /**
     * Creates a new Parameters context. A new Parameters object is effectively
     * "pushed" onto the AppContext and will remain until the next call to
     * popContext.
     */
    public static void pushContext() {
        Parameters current = instance();
        Parameters gnu = new ParamsImpl();
        gnu.setBacking(current);
        contextualParams.get().push(gnu);
    }

    public static void popContext() {
        contextualParams.get().pop();
    }

    // -------------------------------
    // Configuration API

    /**
     * Set the backing Parameters object
     */
    public abstract void setBacking(Parameters aBacking);

    /**
     * Get the backing Parameters (may be null)
     */
    public abstract Parameters getBacking();

    /**
     * Internal method - don't use this.
     */
    public abstract String getInternal(String key);

    // ------------------------------
    // API

    /**
     * Get a String parameter
     */
    public abstract String get(String key);

    /**
     * Get a Boolean parameter
     */
    public abstract boolean getBoolean(String key);

    /**
     * Get an integer parameter.
     * 
     * @throws a system exception if the parameter is not parsable as an int
     */
    public abstract int getInt(String key);

    /**
     * Get an long parameter.
     * 
     * @throws a system exception if the parameter is not parsable as an int
     */
    public abstract long getLong(String key);

    /**
     * Gets a String parameter and splits it as a comma separated string
     * 
     * @param key
     * @return an array of strings, obtained from a comma separated list
     */
    public abstract String[] getArray(String key);

    /**
     * Bundle all parameters whose name begins with the given prefix into a
     * Properties object and return it.
     * 
     * @param prefix
     * @return a Properties object with all parameters whose key begins with the
     *         given prefix
     */
    public abstract Properties getProperties(String prefix);

    /**
     * Bundle all parameters whose name begins with the given prefix into a
     * Properties object and return it.
     * 
     * @param prefix
     * @param truncate indicates whether the prefix should be removed from the
     *            keys when populating the Properties object
     * @return a Properties object with all parameters whose key begins with the
     *         given prefix
     */
    public abstract Properties getProperties(String prefix, boolean truncate);

    /**
     * Get keys of all defined properties
     * 
     * @return a Set containing all keys.
     */
    public abstract Set<String> getKeys();

    // -------------------------------
    // Setters - these are mainly for testing.

    public abstract void set(String key, String value);

    // -------------------------------
    // Diagnostics

    public abstract String dump();

    public abstract void dump(List<String> strings);

}
