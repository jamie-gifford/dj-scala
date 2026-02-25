package au.com.thoughtpatterns.core.json;

import junit.framework.Assert;

import org.junit.Test;

import au.com.thoughtpatterns.core.util.Resources;


public class JsonPointer_Test {

    @Test
    public void test() {
        
        String in = Resources.getResourceAsString(this, "JsonPointer_Test_1.json");
        JsonyObject root = (JsonyObject) new JsonyParser().parse(in);            

        check(root, "", root);
        check(root, "/foo", root.get("foo"));
        check(root, "/foo/0", "bar");
        check(root, "/", 0l);
        check(root, "/a~1b", 1l);
        check(root, "/c%d", 2l);
        check(root, "/e^f", 3l);
        check(root, "/g|h", 4l);
        check(root, "/i\\j", 5l);
        check(root, "/k\"l", 6l);
        check(root, "/ ", 7l);
        check(root, "/m~0n", 8l);
    }
    
    private void check(Jsony root, String pointer, Object expected) {

        JsonPointer p = new JsonPointer(root, pointer);
        
        Jsony val = p.resolve();
        
        if (expected instanceof Jsony) {
            Assert.assertEquals(expected, val);
        } else {
            JsonyPrimitive<?> prim = (JsonyPrimitive<?>) val;
            Object v = prim.getValue();
            
            Assert.assertEquals(expected, v);
        }
        
    }
    
}
