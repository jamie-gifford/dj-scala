package au.com.thoughtpatterns.core.security;

import java.io.Serializable;

/**
 * Represents a user (or system actor). 
 * 
 * Credentials can be checked for validity by the {@link CredentialsManager}.
 * 
 * Privileges associated with Credentials can be derived from the {@link PrivilegesManager}.
 */
public interface Credentials extends Serializable {

    /**
     * Return a (String) representation of the user represented by this
     * Credentials object. This is mainly useful for logging.
     * 
     * @return the username
     */
    public String getUsername();

    /**
     * Get the password that is used to authenticate this Credentials object.
     * This may be null if the "delegate credentials" are used to authenticate.
     * Or there may be (future) sub-interfaces of Credentials which use some other 
     * authentication scheme. 
     * 
     * @return the password or null if none
     */
    public String getPassword();
    
    /**
     * Get the "delegate" Credentials. Usually this will be null, but it may 
     * be non-null in the case where a system user is "vouching" for a human user, 
     * for instance. 
     * @return the delegate Credentials or null if there are none.
     */
    public Credentials getDelegate();
    
    /**
     * An optional identifier that distinguishes this credentials instance 
     * from other instances with the same user id. This is intended for profile logging.
     * @return some sort of session identifier. 
     */
    public String getSessionId();
    
    /**
     * Allocate a new session id.
     */
    public void allocateSessionId();
    
    /**
     * Return an Authenticator capable of authenticating these credentials
     */
    public Authenticator getAuthenticator();
    
}
