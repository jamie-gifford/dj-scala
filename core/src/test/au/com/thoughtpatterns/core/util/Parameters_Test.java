package au.com.thoughtpatterns.core.util;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class Parameters_Test {

    private static Logger log = Logger.get(Parameters_Test.class);
    
    @Test public void testGet() {
        Parameters params = Parameters.instance();

        // Exercise the logging machinery
        log.info("Testing parameters");
        
        String v = params.get("core.version");
        Assert.assertTrue(v != null);
    }
    
    @Test public void testLoad() {
        Parameters params = new ParamsImpl("au/com/thoughtpatterns/core/util/test.properties", null);
        String a = params.get("test.a");
        String z = params.get("test.z");
        
        Assert.assertEquals(a, z);
        Assert.assertEquals("A/A", params.get("test.e"));
    }
    
    @Test public void testArray() {
        Parameters params = new ParamsImpl("au/com/thoughtpatterns/core/util/test.properties", null);
        String[] args = params.getArray("test.array");
        
        Assert.assertEquals(3, args.length);
        Assert.assertEquals("x", args[0]);
        Assert.assertEquals("y", args[1]);
        Assert.assertEquals("z", args[2]);
    }
    
    @Test public void testSubpackages() {
        Parameters params = new ParamsImpl("au/com/thoughtpatterns/core/util/package.properties", null);
        
        String outer = params.get("au.com.thoughtpatterns.core.util.prop");
        String inner = params.get("au.com.thoughtpatterns.core.util.subpackage.prop");
        String root = params.get("root.property");
        String root2 = params.get("root.property2");
        
        Assert.assertEquals("outer", outer);
        Assert.assertEquals("subpackage", inner);
        Assert.assertEquals("root", root);
        Assert.assertEquals("root", root2);
    }
    
    @Test public void testLocked() {
        Parameters params = Parameters.instance();
        try {
            params.set("a", "b");
            Assert.fail("Parameters not locked");
        } catch (Exception expected) {};
    }
    
    @Test public void testContextual() {

        Parameters before = Parameters.instance();

        String version = before.get("core.version");
        String rubbish = before.get("rubbish");
        
        Parameters.pushContext();
        Parameters inside = Parameters.instance();
    
        inside.set("rubbish", "unittest");
        
        Assert.assertEquals("unittest", inside.get("rubbish"));
        Assert.assertEquals(version, inside.get("core.version"));
        Assert.assertEquals(rubbish, before.get("rubbish"));
        
        Parameters.popContext();

        Parameters after = Parameters.instance();
        Assert.assertEquals(rubbish, after.get("rubbish"));    
    }
    
    @Test public void testContextualLoad() {
        Parameters before = Parameters.instance();

        String[] versions = before.getArray("core.version");
        
        Parameters.pushContext();
        
        Parameters inside = Parameters.instance();
        inside.set("include", "au/com/thoughtpatterns/core/util/context.properties");
        
        String[] versionsInside = inside.getArray("core.version");
        
        Assert.assertEquals(versions.length + 1, versionsInside.length);
    }
    
    @Test public void testCaching() {
        Parameters.pushContext();
        Parameters params = Parameters.instance();
        
        String first = params.get("core.version");
        String changed = first + "-changed";
        
        params.set("core.version", changed);
        
        String second = params.get("core.version");
        Assert.assertEquals(changed, second);

    }
    
    @Test public void testDump() {
        Parameters before = Parameters.instance();

        String dump = before.dump();
        
        log.debug(dump);
    }

    @Test
    public void testCascade() {
        Parameters inner = new ParamsImpl("au/com/thoughtpatterns/core/util/test_inner.properties", null);
        Parameters outer = new ParamsImpl("au/com/thoughtpatterns/core/util/test_outer.properties", inner);

        String file = outer.get("filename");
        Assert.assertEquals("/tmp/file.txt", file);
        
        Properties props = outer.getProperties("filename", false);
        String fileProp = props.getProperty("filename");
        Assert.assertEquals("/tmp/file.txt", fileProp);
    }
}
