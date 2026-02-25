package au.com.thoughtpatterns.core.util;


public class Factory {

    private static final Logger log = Logger.get(Factory.class);
    
    /**
     * Create and return an object that implements the given interface.
     * The classname of the implementing class is read from Parameters
     * under the key factory.&lt;interface>
     * 
     * The class is expected to have a public no-parameter constructor.
     * 
     * @param interfaz the interface that the returned object will implement
     * @return a new object implementing the given interface.
     */
    public static <T> T create(Class<T> interfaz) {
       String paramKey = "factory." + interfaz.getName();
       return (T) create(paramKey);
    }
    
    /**
     * Create and return an object determined by the value of the 
     * given runtime parametsr.
     */
    public static Object create(String paramKey) {
        Parameters params = Parameters.instance();
        String classname = params.get(paramKey);
        if (classname == null) {
            log.error("No class for parameter " + paramKey);
            log.error("You must set the " +  paramKey + " parameter");
            throw new SystemException("No class for " + paramKey);
        }
            
        try {
            Class clazz = Class.forName(classname);
            Object instance = clazz.newInstance();
            return instance;
        } catch (Exception ex) {
            throw new SystemException("Failed to instantiate " + classname, ex);
        }    
    }

    public static void configure(Class interfaz, Class impl) {
        Parameters params = Parameters.instance();
        params.set("factory." + interfaz.getName(), impl.getName());
    }
    
}
