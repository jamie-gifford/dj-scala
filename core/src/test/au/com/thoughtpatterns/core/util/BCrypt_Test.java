package au.com.thoughtpatterns.core.util;

import junit.framework.Assert;

import org.junit.Test;

public class BCrypt_Test {

    private static final Logger log = Logger.get(BCrypt_Test.class);
    
    @Test public void testA() {

        String password = "Bananaman";
        
        // Hash a password for the first time
        
        String salt = BCrypt.gensalt();
        
        String hashed = BCrypt.hashpw(password, salt);

        // gensalt's log_rounds parameter determines the complexity
        // the work factor is 2**log_rounds, and the default is 10
//        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));

        // Check that an unencrypted password matches one that has
        // previously been hashed

        log.info("Hash " + hashed);
        
        String hashed2 = BCrypt.hashpw(password, BCrypt.gensalt());
        
        Assert.assertEquals(hashed, hashed2);

    }

}
