package au.com.thoughtpatterns.core.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * Miscellaneous utility methods.
 */
public class Util {

    public static boolean equals(Object a, Object b) {
        if (a == null) {
            return b == null;
        }

        return a.equals(b);
    }

    public static boolean sameData(byte[] a, byte[] b) {
        if (a == null) {
            return b == null;
        }

        if (b == null) {
            return false;
        }
        
        if (a.length != b.length) {
            return false;
        }
        
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean empty(String aString) {
        return aString == null || "".equals(aString);
    }

    public static boolean isTrue(Boolean aBool) {
        return aBool != null && aBool;
    }

    public static <T> String join(String joiner, List<T> strings) {
        if (strings == null) {
            return null;
        }
        int size = strings.size();
        if (size == 0) {
            return "";
        }
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < size; i++) {
            T str = strings.get(i);
            if (str != null) {
                out.append(str.toString());
                if (i < size - 1) {
                    out.append(joiner);
                }
            }
        }
        return out.toString();
    }

    public static String join(String joiner, String... strings) {
        if (strings == null) {
            return null;
        }
        int size = strings.length;
        if (size == 0) {
            return "";
        }
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < size; i++) {
            String str = strings[i];
            if (str != null) {
                out.append(str);
                if (i < size - 1) {
                    out.append(joiner);
                }
            }
        }
        return out.toString();
    }

    public static String truncate(String s, int length) {
        if (s != null && s.length() > length) {
            return s.substring(0, length);
        } else {
            return s;
        }

    }

    public static String sha1(String in) {
        try {
            MessageDigest cript = MessageDigest.getInstance("SHA-1");
            cript.reset();
            cript.update(in.getBytes("utf8"));
            byte[] hash = cript.digest();
            
            String tmp = new BigInteger(1, hash).toString(16);
            
            while (tmp.length() < 40) {
                tmp = "0" + tmp;
            }
            
            return tmp.toLowerCase();
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    public static String md5(String in) {
        try {
            MessageDigest cript = MessageDigest.getInstance("MD5");
            cript.reset();
            cript.update(in.getBytes("utf8"));
            byte[] hash = cript.digest();
            
            String tmp = new BigInteger(1, hash).toString(16);
            
            while (tmp.length() < 32) {
                tmp = "0" + tmp;
            }
            
            return tmp.toLowerCase();
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    /**
     * Convert an input list of strings to a pruned list with empties removed
     * @param strings
     * @return
     */
    public static List<String> prune(Iterable<String> strings) {
        List<String> out = new ArrayList<>();
        for (String string : strings) {
            if (! empty(string)) {
                out.add(string);
            }
        }
        return out;
    }

    public static List<String> prune(String... strings) {
        List<String> out = new ArrayList<>();
        for (String string : strings) {
            if (! empty(string)) {
                out.add(string);
            }
        }
        return out;
    }

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    
    /**
     * Format byte array to hex (upper case)
     * @param bytes
     * @return formatted hex (upper case)
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    public static <T extends Comparable<T>> int compareNullPositiveInfinity(T a, T b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        return a.compareTo(b);
    }
    
}
