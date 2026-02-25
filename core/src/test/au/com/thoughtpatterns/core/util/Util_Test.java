package au.com.thoughtpatterns.core.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import au.com.thoughtpatterns.core.util.Util;

public class Util_Test {

    @Test
    public void testJoin() {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        
        String join = Util.join("/", list);
        Assert.assertEquals("a/b/c", join);
    }

    @Test
    public void testSha() {
        String in = "Hello";
        String sha = Util.sha1(in);
        
        Assert.assertEquals("f7ff9e8b7bb2e09b70935a5d785e0cc5d9d0abf0", sha);
    }
    
}
