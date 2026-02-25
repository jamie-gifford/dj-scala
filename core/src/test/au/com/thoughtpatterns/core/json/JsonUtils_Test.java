package au.com.thoughtpatterns.core.json;

import junit.framework.Assert;

import org.junit.Test;

import au.com.thoughtpatterns.core.util.Logger;


public class JsonUtils_Test {

    private static final Logger log = Logger.get(JsonUtils_Test.class);

    @Test
    public void testEscape() {
        
        String a = "\"Hello\" there, this string contäins a backslash (\\), a pound (£), España\n\u0008 a bell!";
        String b = "\\\"Hello\\\" there, this string contäins a backslash (\\\\), a pound (£), España\\n\\u0008 a bell!";
        
        String e = JsonUtils.escape(a);
        
        log.info("a: " + a);
        log.info("b: " + b);
        log.info("e: " + e);

        Assert.assertEquals(b, e);
        
    }
    
}
