package au.com.thoughtpatterns.core.unittest;

import java.lang.reflect.Method;

/**
 * Used for unit testing. 
 * @see WrapperFactory
 */
public interface Wrapper {

    /**
     * Return the number of times the given method has been invoked since
     * the last call to resetCounters
     */
    public int getMethodInvocationCount(Method m);

    /**
     * Reset the counters
     */
    public void resetCounters();
    
}
