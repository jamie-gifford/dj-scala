package au.com.thoughtpatterns.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;

/**
 * Miscellaneous utilities for dealing with resources.
 */
public class Resources {

    /**
     * Loads a resource from the classpath, using a relative resource name (relative 
     * to the package of the object given as a reference). Returns the contents of 
     * the resource as a String.
     * 
     * @param reference an object whose package forms the base package, that the 
     *  resource name is relative to. Use null to indicate the default package.
     *  @param resourceName a relative resource name
     *  @return the contents of the resource
     */
    public static String getResourceAsString(Object reference, String resourceName) {
        byte[] bytes = getResourceAsByteArray(reference, resourceName);
        return new String(bytes);
    }    

    /**
     * Loads a resource from the classpath, using a relative resource name (relative 
     * to the package of the object given as a reference). Returns the contents of 
     * the resource as a byte array.
     * 
     * @param reference an object whose package forms the base package, that the 
     *  resource name is relative to. Use null to indicate the default package.
     *  @param resourceName a relative resource name
     *  @return the contents of the resource
     */
    public static byte[] getResourceAsByteArray(Object reference, String resourceName) {
        try {
            
            InputStream stream = null;
            if (reference == null) {
                stream = Resources.class.getClassLoader().getResourceAsStream(resourceName);
            } else {
                Class<?> cls;
                if (reference instanceof Class<?>) {
                    cls = (Class<?>) reference;
                } else {
                    cls = reference.getClass();
                }
                stream = cls.getResourceAsStream(resourceName);
            }
            if (stream == null) {
                throw new SystemException("Can't find resource " + resourceName + " relative to " + reference);
            }

            byte[] bytes = readByteArray(stream);
            
            stream.close();
            return bytes;
        } catch (IOException ex) {
            throw new SystemException("Can't load resource " + resourceName + " relative to " + reference, ex);
        }        
    }    
    
    public static byte[] readByteArray(InputStream stream) throws IOException {
        byte[] buffer = new byte[16384];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (true) {
            int read = stream.read(buffer);
            if (read <= 0) {
                break;
            }
            bos.write(buffer, 0, read);
        }
        bos.close();
        return bos.toByteArray();
    }

    public static String readString(Reader reader) throws IOException {
        char[] buffer = new char[16384];
        StringWriter bos = new StringWriter();
        while (true) {
            int read = reader.read(buffer);
            if (read <= 0) {
                break;
            }
            bos.write(buffer, 0, read);
        }
        bos.close();
        return bos.toString();
    }

}
