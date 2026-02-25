package au.com.thoughtpatterns.core.json;

import org.junit.Assert;
import org.junit.Test;

import au.com.thoughtpatterns.core.util.Logger;

public class AJsonyArray_Test {

    private static final Logger log = Logger.get(AJsonyArray_Test.class);  
    
    @Test
    public void testArray() {
        AJsonyArray arr = new AJsonyArray();
        
        arr.add(Jsony.of(132));
        arr.add(Jsony.of("hi"));
        arr.add(Jsony.of(null));
        arr.add(Jsony.of(true));
        
        String b = arr.toJson();
        
        Assert.assertEquals("[132,\"hi\",null,true]", b);
    }
    
}
