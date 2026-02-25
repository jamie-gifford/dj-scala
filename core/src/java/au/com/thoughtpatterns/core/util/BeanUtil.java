package au.com.thoughtpatterns.core.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * Simple JavaBean util
 */
public class BeanUtil {

    public static Object getProperty(Object bean, String property) {
        
        PropertyDescriptor pd = getPropertyDescriptor(bean, property);
        if (pd == null) {
            throw new SystemException("Unknown property " + property + " on " + bean.getClass());
        }
        try {
            Method reader = pd.getReadMethod();
            Object value = reader.invoke(bean);
            return value;
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    public static void setProperty(Object bean, String property, Object value) {
        
        PropertyDescriptor pd = getPropertyDescriptor(bean, property);
        if (pd == null) {
            throw new SystemException("Unknown property " + property + " on " + bean.getClass());
        }
        try {
            Method writer = pd.getWriteMethod();
            writer.invoke(bean, value);
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    private static PropertyDescriptor getPropertyDescriptor(Object bean, String property) {

        try {
            BeanInfo info = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] pds = info.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                String name = pd.getName();
                if (property.equals(name)) {
                    return pd;
                }
            }
        } catch (Exception ex) {
            throw new SystemException(ex);
        }

        return null;
    }

}
