package au.com.thoughtpatterns.core.util;

/**
 * Password is a class to implement password encryption as used on Unix systems.
 * It is compatible with the crypt(3c) system function. This version is a based
 * on the DES encryption algorithm in Andrew Tanenbaum's book "Computer
 * Networks". It was rewritten in C and used in Perl release 4.035. This version
 * was rewritten in Java by David Scott, Siemens Ltd., Australia.
 * 
 * For further details on the methods in this class, refer to the Unix man pages
 * for crypt(3c).
 * 
 * Downloaded from http://www.dynamic.net.au/christos/crypt/Password.txt
 */
public class Crypt {

    /**
     * Returns a String containing the encrypted passwd
     * 
     * @param strpw A String containing the un-encrypted password
     * @param strsalt A 2 character String, containing the salt to encrypt the
     *            password with.
     * @returns String containing encrypted password.
     */
    public static String crypt(String strpw, String strsalt) {
        if (strsalt == null || strpw == null) {
            return null;
        }
        // Distinguish between algorithms based on salt
        if (strsalt.startsWith("$2a$")) {
            return BCrypt.hashpw(strpw, strsalt);
        } else {
            return DesCrypt.crypt(strpw, strsalt);
        }
    }
    
    public static String genSalt() {
        return BCrypt.gensalt(12);
    }
    
    
}
