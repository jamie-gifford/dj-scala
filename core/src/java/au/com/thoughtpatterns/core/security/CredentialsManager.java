package au.com.thoughtpatterns.core.security;

import java.util.Stack;

import au.com.thoughtpatterns.core.util.AppContext;
import au.com.thoughtpatterns.core.util.Logger;

/**
 * Provides access to the current Credentials for a Thread. 
 * Also provides authentication methods 
 */
public class CredentialsManager {

    private static final CredentialsManager INSTANCE = new CredentialsManager();
    
    private AppContext<Stack<Credentials>> currentCredentials = new AppContext<Stack<Credentials>>() {

        @Override
        protected Stack<Credentials> initialValue() {            
            return new Stack<Credentials>();
        }
        
    };
    
    /**
     * Return the singleton instance
     */
    public static CredentialsManager getInstance() {
        return INSTANCE;
    }

    private CredentialsManager() {} 
    
    // ------------------------------------
    // Current credentials methods
    
    public Credentials getCurrentCredentials() {
        Stack<Credentials> stack = currentCredentials.get();
        return ( ! stack.isEmpty() ? stack.peek() : null );
    }
    
    public void setCurrentCredentials(Credentials aCreds) {
        if (aCreds != null && aCreds.getSessionId() == null) {
            aCreds.allocateSessionId();
        }
        currentCredentials.get().push(aCreds);
        configure();
    }
    
    public void configure() {
        
        Credentials creds = getCurrentCredentials();
        // Configure the logging infrastructure to use this credentials 
        
        String username = ( creds != null ? creds.getUsername() : null );
        String sid = ( creds != null ? creds.getSessionId() : null );
        
        String id = (username != null ? username : "nobody");
        if (sid != null) {
            id = id + ":" + sid;
        }
        
        if (!"".equals(id)) {
            id = "[" + id + "] ";
        }
        
        Logger.setMDC("user", id);
    }

    // ------------------------------------
    // Convenience methods for logging in and out
    
    /**
     * A convenience method for logging in.
     */
    public void login(String username, String password) {
        Credentials creds = new SimpleCredentials(username, password);
        setCurrentCredentials(creds);
    }

    /**
     * Logout method
     */
    public void logout() {
        Stack<Credentials> stack = currentCredentials.get();
        if (stack.size() > 0) {
            stack.pop();
        }
        configure();        
    }
    
    /**
     * Remove all credentials from stack
     */
    public void clobber() {
        Stack<Credentials> stack = currentCredentials.get();
        while (stack.size() > 0) {
            stack.pop();
        }
        configure();        
    }
    
    // ------------------------------------
    // Authentication methods
    
    /**
     * Return true if the credentials can be authenticated as valid,
     * false otherwise
     */
    public boolean authenticate(Credentials creds) {
        if (creds == null) {
            return false;
        }
        Authenticator auth = creds.getAuthenticator();
        if (auth == null) {
            return false;
        }
        
        return auth.authenticate(creds);
    }
    
}
