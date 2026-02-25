package au.com.thoughtpatterns.core.json;

import org.junit.Assert;
import org.junit.Test;

import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.Resources;


public class AJsonyObject_Test {

    private static final Logger log = Logger.get(AJsonyObject.class);
    
    @Test
    public void testToJson() {
        
        AJsonyObject x = new AJsonyObject();
        
        x.set("a", Jsony.of("hello\nthere"));
        x.set("b", Jsony.of(123));
        x.set("c", Jsony.of(true));
        
        log.info(x.toJson());
    }

    @Test
    public void testEquals() {

        String in = Resources.getResourceAsString(this, "inputs.json");
        Jsony a = new JsonyParser().parse(in);            
        Jsony b = new JsonyParser().parse(in);            

        boolean equal = a.equals(b);
        
        Assert.assertTrue(equal);
    }

    @Test
    public void testNotEquals() {

        String in = Resources.getResourceAsString(this, "inputs.json");
        Jsony a = new JsonyParser().parse(in);            
        JsonyObject b = (JsonyObject) new JsonyParser().parse(in);            

        b.set("new", Jsony.of("X"));
        
        boolean equal = a.equals(b);
        
        Assert.assertFalse(equal);
    }

}
