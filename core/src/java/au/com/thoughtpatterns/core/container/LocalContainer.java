package au.com.thoughtpatterns.core.container;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import au.com.thoughtpatterns.core.sql.Connections;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.Pipe;
import au.com.thoughtpatterns.core.util.ProfilePoint;
import au.com.thoughtpatterns.core.util.SystemException;

/**
 * A simple, in-process implementation of the Container interface. This
 * Container would be suitable for using in a 2-tier web application (playing
 * the role of an EJB container with stateless session beans).
 */
public class LocalContainer implements Container {

    private static final Logger log = Logger.get(LocalContainer.class);

    public Object runTransaction(Method method, Object target)
            throws ContainedException {

        log.info("runTransaction called with method " + method);

        ProfilePoint profile = new ProfilePoint("container", method.getName());
        profile.start();
        
        Connections.startTransaction();
        boolean normalTermination = true;

        try {

            // Take a copy of the target
            Object containerSideCopy = Pipe.copy(target);

            // Run it
            method.setAccessible(true);
            Object result = method.invoke(containerSideCopy, new Object[0]);

            // Take a copy of it again
            Object resultCopy = Pipe.copy(result);

            // And return it
            return resultCopy;

        } catch (InvocationTargetException invokeEx) {

            Throwable cause = invokeEx.getCause();
            // All exceptions mean we roll back
            Connections.setRollbackOnly();
            normalTermination = false;
            
            if (cause instanceof RuntimeException || cause instanceof Error) {
                log.error("Caught runtime exception", cause);
            }
            
            throw new ContainedException(cause);

        } catch (Exception ex) {
            // Other types of exceptions come about because of gross 
            // problems - like the method can't be invoked on the target object
            // at all
            throw new SystemException(ex);
        } finally {

            Connections.endTransaction();
            
            profile.stop();
            
            log.info("runTransaction finished "
                    + (normalTermination ? " normally " : " abruptly ")
                    + " with method " + method);
        }
    }
}
