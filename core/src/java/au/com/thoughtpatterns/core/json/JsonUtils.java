package au.com.thoughtpatterns.core.json;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.com.thoughtpatterns.core.util.Logger;

public class JsonUtils {

    private static final Logger log = Logger.get(JsonUtils.class);

    private static final Pattern ESCAPABLE = Pattern.compile("[\u0001-\u001f]|[\"\\\\\t\n\r]");

    /**
     * Given a java String, encode according to JSON rules into a "string". This
     * involves escaping ", \ and certain control codes
     */
    public static String escape(String str) {
        
        if (str == null) {
            return null;
        }

        StringBuffer out = new StringBuffer();
        
        int cursor = 0;
        int length = str.length();
        while (cursor < length) {
            Matcher m = ESCAPABLE.matcher(str);
            if (! m.find(cursor)) {
                out.append(str.substring(cursor));
                break;
            }
            
            int start = m.start();
            char c = m.group().charAt(0);
            
            String h;
            
            switch (c) {
            case '"': h = "\\\""; break;
            case '\\': h = "\\\\"; break;
            case '\t': h = "\\t"; break;
            case '\n': h = "\\n"; break;
            case '\r': h = "\\r"; break;
            default:
                h = Integer.toHexString(c);

                while (h.length() < 4) {
                    h = "0" + h;
                }
                h = "\\u" + h;
            }
            
            out.append(str.substring(cursor, start));
            out.append(h);
            
            cursor = m.end();
        }
        
        return out.toString();
    }
    
}
