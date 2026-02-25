package au.com.thoughtpatterns.core.container;

/**
 * A wrapper exception that the Container throws if the 
 * underlying operation throws an exception
 */
public class ContainedException extends Exception {

    private static final long serialVersionUID = 1L;

    public ContainedException(Throwable aCause) {
        super(aCause);
    }
    
}
