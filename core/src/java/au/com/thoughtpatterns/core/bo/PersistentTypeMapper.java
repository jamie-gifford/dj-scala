package au.com.thoughtpatterns.core.bo;

import java.util.Properties;

import au.com.thoughtpatterns.core.util.ClassMapper;
import au.com.thoughtpatterns.core.util.Parameters;
import au.com.thoughtpatterns.core.util.SystemException;

/**
 * Maps the concrete Java classes that are used for business objects to 
 * Java classes that are used to identify families of persistent types.
 * 
 * If you are not using subclasses of persistent objects then the default
 * configuration of this map is all you need.
 * 
 * If you've got class hierarchies of persistent classes (eg StructuredAddress
 * extends Address), then you need to configure this mapper so that the persistence
 * layer knows that a StructuredAddress is really an Address.
 */
public class PersistentTypeMapper {

    private ClassMapper<String> mapper = new ClassMapper<String>();
    
    public PersistentTypeMapper() {
        // Configure the classmapper with data from runtime parameters
        Parameters params = Parameters.instance();
        Properties config = params.getProperties("persistence.types.", true);
        mapper.initialise(config);
    }
    
    public Class getPersistentType(Class concreteClass) {
        String mappedClassname = mapper.get(concreteClass);
        if (mappedClassname == null) {
            // No mapping so return the original class
            return concreteClass;
        }
        try {
            Class mappedClass = Class.forName(mappedClassname);
            return mappedClass;
        } catch (ClassNotFoundException ex) {
            throw new SystemException("Incorrect configuration of PersistentTypeMapper - no class for " + concreteClass, ex);
        }
    }
    
}
