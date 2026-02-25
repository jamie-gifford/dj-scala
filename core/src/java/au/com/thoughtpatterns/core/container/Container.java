package au.com.thoughtpatterns.core.container;

import java.lang.reflect.Method;

public interface Container {

    /**
     * Invoke a method "in the container", within a transaction.
     * Returns the return value of the method. If the method throws an 
     * exception, the exception is wrapped in a ContainedException.
     * 
     * Only no-parameter methods can be invoked this way.
     */
    public Object runTransaction(Method method, Object target) throws ContainedException;
    
}
