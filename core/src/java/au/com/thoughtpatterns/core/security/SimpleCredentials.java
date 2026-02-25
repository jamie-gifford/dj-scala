package au.com.thoughtpatterns.core.security;

import java.util.Random;

/**
 * A simple implementation of {@link Credentials}.
 */
public class SimpleCredentials implements Credentials {

    private static final long serialVersionUID = 1L;

    private static final Random random = new Random();
    
    private String username;
    private String password;
    private String sessionId;
    
    public SimpleCredentials(String aUser, String aPassword) {
        username = aUser;
        password = aPassword;
    }
    
    // JavaBean support
    public SimpleCredentials() {
        super();
    }
    
    public Credentials getDelegate() {
        return null;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
    
    public String getSessionId() {
        return sessionId;
    }

    private String createSessionId() {
        // Create a 4-digit hex identifier
        int id = random.nextInt(0x10000);
        String sid = Integer.toHexString(id);
        return sid;
    }
    
    public void allocateSessionId() {
        setSessionId(createSessionId());
    }

    // JavaBean setters
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public Authenticator getAuthenticator() {
        return new Authenticator() {
            public boolean authenticate(Credentials creds) {
                // Dummy implementation - return true if the password is "true"
                // TODO fix this
                if (! (creds instanceof SimpleCredentials)) {
                    return false;
                }
                
                SimpleCredentials s = (SimpleCredentials) creds;
                return "true".equals(s.getPassword());
            }
        };
    }
}
