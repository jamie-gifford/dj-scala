package au.com.thoughtpatterns.core.security;

/**
 * Methods for authenticating credentials
 */
public interface Authenticator {

    /**
     * Test the validity of the credentials and return true if the credentials
     * are valid.
     */
    boolean authenticate(Credentials aCreds);
    
}
