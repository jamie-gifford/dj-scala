package au.com.thoughtpatterns.core.bo.ibatis;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;

import au.com.thoughtpatterns.core.bo.Box;
import au.com.thoughtpatterns.core.bo.CachingBox;
import au.com.thoughtpatterns.core.bo.PersistentObject;

import com.ibatis.common.io.ReaderInputStream;


public class XmlHelper {

    public static String toXml(Object bean) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLEncoder e = new XMLEncoder(new BufferedOutputStream(bos));
        e.writeObject(bean);
        e.close();
        String xml = new String(bos.toByteArray());
        return xml;
    }
    
    public static Object fromXml(String xml) {
        InputStream in = new BufferedInputStream(new ReaderInputStream(new StringReader(xml)));
        XMLDecoder d = new XMLDecoder(in);
        Object bean = d.readObject();
        return bean;
    }
    
    // More specific methods
    
    /**
     * A custom method for marshalling an entity. If the entity is owned
     * by a box, the entity is detached before proceeding.
     */
    public static String marshall(PersistentObject entity) {
        Box box = entity.getBox();
        if (box instanceof CachingBox) {
            CachingBox cache = (CachingBox) box;
            cache.detach(entity);
        }
        
        String xml = toXml(entity);
        return xml;
    }
}
