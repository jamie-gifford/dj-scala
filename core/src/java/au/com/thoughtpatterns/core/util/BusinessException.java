package au.com.thoughtpatterns.core.util;

/**
 * Represents a business-level exception. Business code may throw 
 * BusinessExceptions. The framework will roll back the current transaction.
 */
public class BusinessException extends Exception {

    private static final long serialVersionUID = 1L;

    public BusinessException(String msg) {
        super(msg);
    }
    
}
