package au.com.thoughtpatterns.core.util;

import junit.framework.Assert;

import org.junit.Test;


public class BusinessTime_Test {

    @Test
    public void testFormat() {
        BusinessTime time = BusinessTime.newTime(10, 7);
        Assert.assertEquals("10:07", time.toString());

        time = BusinessTime.newTime(22, 7);
        Assert.assertEquals("22:07", time.toString());
    }
    
    @Test
    public void testParse() {
        BusinessTime time = BusinessTime.newTime("10h");
        Assert.assertEquals("10:00", time.toString());
        
        time = BusinessTime.newTime("23:17");
        Assert.assertEquals("23:17", time.toString());

        time = BusinessTime.newTime("21h");
        Assert.assertEquals("21:00", time.toString());

        time = BusinessTime.newTime("7h07");
        Assert.assertEquals("07:07", time.toString());

        time = BusinessTime.newTime("7h7"); // not parseable
        Assert.assertEquals(null, time);

        time = BusinessTime.newTime("25h"); // not parseable
        Assert.assertEquals(null, time);

        time = BusinessTime.newTime("10:60"); // not parseable
        Assert.assertEquals(null, time);

    }
    
}
