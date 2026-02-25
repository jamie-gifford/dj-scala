package au.com.thoughtpatterns.core.util;

import org.junit.Test;

import au.com.thoughtpatterns.core.security.CredentialsManager;

/**
 * Exercise the ProfilePoint code.
 */
public class ProfilePoint_Test {

    @Test
    public void testProfilePoint() {
        
        CredentialsManager.getInstance().login("test_user", null);
        
        ProfilePoint a = new ProfilePoint("A", "a");
        a.start();

        ProfilePoint b = new ProfilePoint("B", "b");
        b.start();
        b.stop();
        
        b = new ProfilePoint("B", "b2");
        b.start();
        b.stop();
        
        a.stop();
        
        CredentialsManager.getInstance().logout();
    }
    
}
