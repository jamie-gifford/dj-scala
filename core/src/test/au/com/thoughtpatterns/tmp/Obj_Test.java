package au.com.thoughtpatterns.tmp;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;

import au.com.thoughtpatterns.core.json.JsonResolver;
import au.com.thoughtpatterns.core.json.Jsony;
import au.com.thoughtpatterns.core.json.JsonyArray;
import au.com.thoughtpatterns.core.json.JsonyException;
import au.com.thoughtpatterns.core.json.JsonyObject;
import au.com.thoughtpatterns.core.json.JsonyParser;
import au.com.thoughtpatterns.core.json.schema.Schema;
import au.com.thoughtpatterns.core.json.schema.Schema.ValidationResult;
import au.com.thoughtpatterns.core.util.Resources;


public class Obj_Test {

    @Test
    public void testObj() {
     
        Hobby h1 = new Hobby();
        h1.setHobby("dance");
        h1.setYearsPracticed(8);

        Hobby h2 = new Hobby();
        h2.setHobby("dance");
        h2.setYearsPracticed(8);

        Assert.assertEquals(h1, h2);
        
        Assert.assertTrue(h1.getYearsPracticed() == 8);
        Assert.assertTrue(h1.getHobby().equals("dance"));
    }
    
    @Test
    public void testUntypedParse() {
        JsonyObject person = (JsonyObject) loadJson("instance1.json");
        Assert.assertEquals("Jamie", person.getCast("given_name", String.class));
    }
    
    @Test
    public void testTypedParse() {
        String str = Resources.getResourceAsString(this, "instance1.json");
        Jsony j = new JsonyParser().parse(str);
        Schema<Jsony> schema = loadSchema("schema.json");
        
        JsonResolver r = new JsonResolver();
        Schema<Jsony> personSchema = (Schema<Jsony>) schema.from(r.pointer(schema, "/definitions/person"));
        
        ValidationResult res = personSchema.validate(j);
        Assert.assertTrue(res.isValid());
        
        Person person = (Person) personSchema.parse(j);
        
        Assert.assertEquals("Jamie", person.getGivenName());
    }

    @Test
    public void testTypedParse2() {
        String str = Resources.getResourceAsString(this, "instance2.json");
        Jsony j = new JsonyParser().parse(str);
        Schema<Jsony> schema = loadSchema("schema.json");

        JsonResolver r = new JsonResolver();
        Schema<Jsony> familySchema = (Schema<Jsony>) schema.from(r.pointer(schema, "/definitions/family"));

        Family family = (Family) familySchema.parse(j);
        
        Assert.assertEquals("Jenny", family.getMother().getGivenName());
        Assert.assertEquals((Integer)44, family.getMother().getAge());
    }

    @Test
    public void testTypedParse3() {
        String str = Resources.getResourceAsString(this, "instance3.json");
        Jsony j = new JsonyParser().parse(str);
        Schema<Jsony> schema = loadSchema("schema.json");

        JsonResolver r = new JsonResolver();
        Schema<Jsony> familySchema = (Schema<Jsony>) schema.from(r.pointer(schema, "/definitions/family"));

        Family family = (Family) familySchema.parse(j);
        
        JsonyArray<Person> children = family.getChildren();
        Person child1 = children.get(0);
        
        Assert.assertEquals("Benny", child1.getGivenName());
        Assert.assertEquals((Integer)7, child1.getAge());
    }

    @Test
    public void testTypedParse4() {
        String str = Resources.getResourceAsString(this, "instance4.json");
        Jsony j = new JsonyParser().parse(str);
        Schema<Jsony> schema = loadSchema("schema.json");

        JsonResolver r = new JsonResolver();
        Schema<Jsony> familySchema = (Schema<Jsony>) schema.from(r.pointer(schema, "/definitions/family"));

        ValidationResult res = familySchema.validate(j);
        Assert.assertFalse(res.isValid());
        
    }

    @Test
    public void testRoundTrip() {
        String str = Resources.getResourceAsString(this, "instance2.json");
        Jsony j = new JsonyParser().parse(str);
        Schema<Jsony> schema = loadSchema("schema.json");

        JsonResolver r = new JsonResolver();
        Schema<Jsony> familySchema = (Schema<Jsony>) schema.from(r.pointer(schema, "/definitions/family"));

        Family family = (Family) familySchema.parse(j);
        
        family.getMother().setAge(27);
        
        String str2 = family.toJson();
        
        Family family2 = (Family) familySchema.parse(new JsonyParser().parse(str2));
        
        Assert.assertEquals((Integer)27, family2.getMother().getAge());
    }

    private Schema<Jsony> loadSchema(String name) {
        try {
            URL url = getClass().getResource(name);
            try (InputStream stream = url.openStream();
                    InputStreamReader reader = new InputStreamReader(stream)) {
                String str = Resources.readString(reader);
                Jsony json = new JsonyParser().parse(str);
                Schema<Jsony> s = (Schema<Jsony>) Schema.schemaFrom(json, url.toURI());
                return s;
            }
        } catch (Exception ex) {
            throw new JsonyException("Bad load " + name, ex);
        }
    }

    private Jsony loadJson(String name) {
        String in = Resources.getResourceAsString(this, name);
        Jsony json = new JsonyParser().parse(in);
        return json;
    }

}
