package au.com.thoughtpatterns.core.unittest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates wrapper classes that sit over the top of objects and intercept 
 * method calls.
 */
public class WrapperFactory {

    public static Wrapper wrap(Object object, Class interfaz) {
        Class[] interfaces = new Class[] { interfaz, Wrapper.class };
        ClassLoader loader = WrapperFactory.class.getClassLoader();
        WrapperImpl wrapperImpl = new WrapperImpl();
        WrapperHandler h = new WrapperHandler(object, wrapperImpl);
        
        Wrapper wrapper = (Wrapper) Proxy.newProxyInstance(loader, interfaces, h);
        return wrapper;
    }
    
    private static class WrapperHandler implements InvocationHandler {

        private WrapperImpl wrapperImpl;
        private Object wrapped;
        
        public WrapperHandler(Object aWrapped, WrapperImpl aWrapperImpl) {
            wrapped = aWrapped;
            wrapperImpl = aWrapperImpl;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            Class clas = method.getDeclaringClass();
            if (clas == Wrapper.class) {
                // Delegate to wrapperImpl
                return method.invoke(wrapperImpl, args);
            }
            
            // Count method and delegate to the wrapped object
            wrapperImpl.incrementCounter(method);
            Object result = method.invoke(wrapped, args);
            
            return result;
        }        
    }
    
    private static class WrapperImpl implements Wrapper {

        private Map<Method, Integer> methodCount = new HashMap<Method, Integer>();
        
        public int getMethodInvocationCount(Method m) {
            Integer count = methodCount.get(m);
            return ( count != null ? count : 0 );
        }

        public void resetCounters() {
            methodCount.clear();
        }
        
        public void incrementCounter(Method m) {
            Integer count = methodCount.get(m);
            int c = ( count != null ? count : 0 );
            c++;
            methodCount.put(m, c);
        }
        
    }
}
