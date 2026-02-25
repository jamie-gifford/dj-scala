package au.com.thoughtpatterns.core.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Pipe {
    
    /**
     * Create and return a serialized copy of the given object.
     * @param in the input object
     * @return a serialized copy of the input object
     */
    public static Object copy(Object in) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream ooze = new ObjectOutputStream(bos);
            ooze.writeObject(in);
            ooze.close();
            byte[] bytes = bos.toByteArray();
            
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object copy = ois.readObject();
            return copy;
        } catch (Exception ex) {
            throw new SystemException("Failed to serialize " + in, ex);
        }
    }
    
    public static byte[] toByteArray(Object in) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream ooze = new ObjectOutputStream(bos);
            ooze.writeObject(in);
            ooze.close();
            byte[] bytes = bos.toByteArray();

            return bytes;
        } catch (Exception ex) {
            throw new SystemException("Failed to serialize " + in, ex);
        }
    }
    
    public static Object copyBean(Object in) {
        String xml = marshallBean(in);
        Object obj = demarshallBean(xml);
        return obj;
    }
    
    public static String marshallBean(Object in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(out);
        encoder.writeObject(in);
        encoder.close();
        byte[] bytes = out.toByteArray();
        String str = new String(bytes);
        return str;
    }
    
    public static Object demarshallBean(String in) {
        InputStream is = new ByteArrayInputStream(in.getBytes());
        XMLDecoder decoder = new XMLDecoder(is); 
        Object obj = decoder.readObject();
        return obj;
    }

    public static String marshallBase64(Object in) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream ooze = new ObjectOutputStream(bos);
            ooze.writeObject(in);
            ooze.close();
            byte[] bytes = bos.toByteArray();

            String encoded = Base64.encode(bytes);
            return encoded;
        } catch (Exception ex) {
            throw new SystemException("Failed to serialize " + in, ex);
        }
    }
    
    public static Object demarshallBase64(String in) {
        try {
            byte[] bytes = Base64.decode(in);
            
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object copy = ois.readObject();
            return copy;
        } catch (Exception ex) {
            throw new SystemException("Failed to deserialized " + in, ex);
        }        
    }
    
    public static int measureBytes(Object in) {
        
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream ooze = new ObjectOutputStream(bos);
            ooze.writeObject(in);
            ooze.close();
            byte[] bytes = bos.toByteArray();
            
            return bytes.length;
            
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

}
