package au.com.thoughtpatterns.core.util;

/**
 * Represents an unrecoverable, "technical" failure of the system.
 * The exception should be logged and operator intervention is probably required unless
 * the problem is transient and fixes itself.
 */
public class SystemException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SystemException(String msg) {
        super(msg);
    }
    
    public SystemException(String msg, Throwable ex) {
        super(msg, ex);
    }
    
    public SystemException(Throwable ex) {
        super(ex);
    }
    
}
