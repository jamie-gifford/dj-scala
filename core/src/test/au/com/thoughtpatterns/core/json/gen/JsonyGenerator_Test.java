package au.com.thoughtpatterns.core.json.gen;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Test;

import au.com.thoughtpatterns.core.json.Jsony;
import au.com.thoughtpatterns.core.json.JsonyException;
import au.com.thoughtpatterns.core.json.JsonyParser;
import au.com.thoughtpatterns.core.json.gen.JsonyGenerator.JsonyType;
import au.com.thoughtpatterns.core.json.schema.Schema;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.Resources;


public class JsonyGenerator_Test {

    private static final Logger log = Logger.get(JsonyGenerator_Test.class);
    
    @Test public void testFamily() {
        
        Schema<Jsony> familySchema = loadSchema("bo_schema.json#/definitions/family");
        
        JsonyGenerator gen = new JsonyGenerator();
        
        JsonyType type = gen.typeFrom(familySchema);
        
        log.info("Type of family: " + type);
        
        gen.dump();
    }

     @Test public void testTree() {
        
        Schema<Jsony> root = (Schema<Jsony>) loadSchema("tree_schema.json");
        
        JsonyGenerator gen = new JsonyGenerator();
        gen.generateAll(root);
    }

     private Schema<Jsony> loadSchema(String name) {
         try {
             URL url0 = getClass().getResource("package.html");
             URL url = new URL(url0, name);
             Schema<?> s = Schema.schemaFrom(url);
             return (Schema<Jsony>) s;
         } catch (Exception ex) {
             throw new JsonyException("Bad load " + name + ": " + ex.getMessage(), ex);
         }
     }

    private Jsony loadJson(String name) {
        String in = Resources.getResourceAsString(this, name);
        Jsony json = new JsonyParser().parse(in);
        return json;
    }

    
}
