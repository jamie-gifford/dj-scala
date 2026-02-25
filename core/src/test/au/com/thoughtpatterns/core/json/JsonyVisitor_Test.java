package au.com.thoughtpatterns.core.json;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import au.com.thoughtpatterns.core.util.Resources;


public class JsonyVisitor_Test {

    @Test
    public void testDepthVisit() {
        
        String in = Resources.getResourceAsString(this, "JsonyVisitor_Test_1.json");
        Jsony node = new JsonyParser().parse(in);            

        final Set<Jsony> nodes = new HashSet<>();
        
        JsonyVisitor visitor = new JsonyVisitor() {
            
            @Override public void visit(Jsony node, Path path) {
                nodes.add(node);
            }
        };
        
        JsonyVisitor.depthFirst(node, visitor, null);
        
        Assert.assertEquals(10, nodes.size());
    }
    
}
