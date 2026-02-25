package au.com.thoughtpatterns.core.json.schema;

import java.net.URI;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;

import au.com.thoughtpatterns.core.json.AJsonyObject;
import au.com.thoughtpatterns.core.json.AJsonyPrimitive;
import au.com.thoughtpatterns.core.json.JsonResolver;
import au.com.thoughtpatterns.core.json.Jsony;
import au.com.thoughtpatterns.core.json.JsonyException;
import au.com.thoughtpatterns.core.json.JsonyObject;
import au.com.thoughtpatterns.core.json.JsonyParser;
import au.com.thoughtpatterns.core.json.JsonyPrimitive;
import au.com.thoughtpatterns.core.json.schema.Schema.ValidationResult;
import au.com.thoughtpatterns.core.json.schema.c.TestDate;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.Resources;

public class Schema_Test {
    
    private static final Logger log = Logger.get(Schema_Test.class);

    @Test public void testValidate1() {

        JsonyObject j = new AJsonyObject();
        j.set("type", Jsony.of("string"));
        
        Schema<Jsony> s = (Schema<Jsony>) Schema.schemaFrom(j, null);

        JsonyPrimitive<Integer> p = new AJsonyPrimitive<>(123);
        ValidationResult r = s.validate(p);
        Assert.assertFalse(r.isValid());

        JsonyPrimitive<String> p2 = new AJsonyPrimitive<>("hello");
        ValidationResult r2 = s.validate(p2);
        Assert.assertTrue(r2.isValid());
    }

    @Test public void testValidate2() {
        Schema<Jsony> s = loadSchema("schema2.json");
        Jsony val = loadJson("schema2_instance1.json");

        assertValid(s, val);
    }

    @Test public void testValidate3() {
        Schema<Jsony> s = loadSchema("schema2.json");
        Jsony val = loadJson("schema2_instance2.json");

        assertInvalid(s, val);
    }

    @Test public void testValidate4() {
        Schema<Jsony> s = loadSchema("schema2.json");
        Jsony val = loadJson("schema2_instance3.json");

        assertInvalid(s, val);
    }

    @Test public void testValidate5() {
        Schema<Jsony> s = loadSchema("schema2.json");
        Jsony val = loadJson("schema2_instance4.json");

        assertInvalid(s, val);
    }

    @Test public void testValidate6() {
        Schema<Jsony> s = loadSchema("schema3.json");
        Jsony val = loadJson("schema3_instance1.json");

        assertInvalid(s, val);
    }

    @Test public void testValidate7() {
        Schema<Jsony> s = loadSchema("schema3.json");
        Jsony val = loadJson("schema3_instance2.json");

        assertValid(s, val);
    }

    @Test public void testValidate8() {
        Schema<Jsony> s = loadSchema("schema3.json");
        Jsony val = loadJson("schema3_instance3.json");

        assertInvalid(s, val);
    }
    
    @Test public void testIntList1() {
        Schema<Jsony> s = loadSchema("schema_int_array.json");
        Jsony val = loadJson("int_array_instance1.json");
        assertValid(s, val);
    }

    @Test public void testIntList2() {
        Schema<Jsony> s = loadSchema("schema_int_array.json");
        Jsony val = loadJson("int_array_instance2.json");
        assertInvalid(s, val);
    }

    @Test public void testIntList3() {
        Schema<Jsony> s = loadSchema("schema_int_array.json");
        Jsony val = loadJson("int_array_instance3.json");
        assertInvalid(s, val);
    }

    @Test public void testRef1() {
        Schema<Jsony> s = loadSchema("schema_person.json");
        Jsony val = loadJson("person_instance1.json");

        assertValid(s, val);
    }

    @Test public void testRef2() {
        Schema<Jsony> s = loadSchema("schema_person.json");
        Jsony val = loadJson("person_instance2.json");

        assertInvalid(s, val);
    }

    @Test public void testRef3() {
        Schema<Jsony> s = loadSchema("schema_person.json");
        Jsony val = loadJson("person_instance3.json");

        assertInvalid(s, val);
    }

    @Test public void testRef4() {
        Schema<Jsony> s = loadSchema("schema_person.json");
        Jsony val = loadJson("person_instance4.json");

        assertInvalid(s, val);
    }

    @Test public void testRefId() {
        Schema<Jsony> s = loadSchema("schema_person.json");
        Jsony val = loadJson("person_instance4.json");

        assertInvalid(s, val);
    }

    @Test public void testShortString() {
        Schema<Jsony> s = loadSchema("schema_person.json");
        Jsony val = loadJson("person_instance_shortname.json");

        assertInvalid(s, val);
        
    }
    
    /**
     * Test that inline defeferencing works too 
     */
    @Test public void testRef4a() {
        Jsony j = loadJson("schema_person_inline.json");;
        Schema<Jsony> s = (Schema<Jsony>) Schema.schemaFrom(j, null);
        Jsony val = loadJson("person_instance4.json");

        assertInvalid(s, val);
    }

    @Test public void testRef5() {
        Schema<Jsony> s = loadSchema("schema_person.json");
        Jsony val = loadJson("person_instance5.json");

        assertValid(s, val);
    }

    /**
     * Test that schemas loaded from URIs with fragments are handled correctly.
     */
    @Test public void testFragment1() {
        Schema<Jsony> s = loadSchema("schema_fragment.json#/definitions/person");
        String comment = s.getCast("comment", String.class);
        Assert.assertEquals("Person", comment);
    }
    
    @Test public void testFragment2() {
        Schema<Jsony> s = loadSchema("bo_schema.json#/definitions/family");

        JsonyObject props = (JsonyObject) s.get("properties");
        Jsony p = props.get("mother");
        Schema<JsonyObject> m = (Schema<JsonyObject>) s.from(p);

        String comment = m.getCast("comment", String.class);
        Assert.assertEquals("Person", comment);
    }
    
    @Test public void testJsonPointer() {
        Schema<Jsony> s = loadSchema("schema_person.json");
        s.get("hobbies");
    }

    @Test public void testArray1() {
        Schema<Jsony> s = loadSchema("schema_array_short.json");
        Jsony val = loadJson("schema_array_short_instance1.json");

        assertValid(s, val);
    }
    
    @Test public void testArray2() {
        Schema<Jsony> s = loadSchema("schema_array_short.json");
        Jsony val = loadJson("schema_array_short_instance2.json");

        assertInvalid(s, val);
    }

    @Test public void testNumeric1() {
        Schema<Jsony> s = loadSchema("schema_array_numeric.json");
        Jsony val = loadJson("schema_array_numeric_instance1.json");

        assertValid(s, val);
    }
    
    @Test public void testNumeric2() {
        Schema<Jsony> s = loadSchema("schema_array_numeric.json");
        Jsony val = loadJson("schema_array_numeric_instance2.json");

        assertInvalid(s, val);
    }

    @Test public void testRefs1() {
        Schema<Jsony> s = loadSchema("b/b.json");
        Jsony val = loadJson("b/b_instance1.json");

        assertValid(s, val);
    }
    
    @Test public void testRefs2() {
        Schema<Jsony> s = loadSchema("b/b.json");
        Jsony val = loadJson("b/b_instance2.json");

        assertInvalid(s, val);
    }
    
    @Test public void testNumericSchema1() {
        Schema<Jsony> s = loadSchema("schema_numeric.json");
        Jsony val = loadJson("schema_numeric_instance_1.json");

        assertValid(s, val);
    }
    
    @Test public void testNumericSchema2() {
        Schema<Jsony> s = loadSchema("schema_numeric.json");
        Jsony val = loadJson("schema_numeric_instance_2.json");

        assertInvalid(s, val, 4);
    }
    
    @Test public void testPattern1() {
        Schema<Jsony> s = loadSchema("schema_pattern.json");
        Jsony val = loadJson("schema_pattern_instance_1.json");

        assertValid(s, val);
    }

    @Test public void testPattern2() {
        Schema<Jsony> s = loadSchema("schema_pattern.json");
        Jsony val = loadJson("schema_pattern_instance_2.json");

        assertInvalid(s, val, 2);
    }
    
    @Test public void testCoerce() {
        Schema<Jsony> s = loadSchema("schema_coerce.json");
        Jsony val = loadJson("schema_coerce_instance1.json");

        assertInvalid(s, val, 2);
        
        s.coerce(val, null);
        assertValid(s, val);
        
    }

    @Test public void testCoerce2() {
        Schema<Jsony> s = loadSchema("schema_coerce.json");
        Jsony val = loadJson("schema_coerce_instance2.json");

        assertInvalid(s, val);
        
        s.coerce(val, null);

        assertInvalid(s, val);
    }

    @Test public void testCoerce3() {
        Schema<Jsony> s = loadSchema("schema_coerce.json");
        Jsony val = loadJson("schema_coerce_instance3.json");

        assertInvalid(s, val);
        
        s.coerce(val, null);

        assertValid(s, val);
    }

    @Test public void testObjectAdditional1() {
        Schema<Jsony> s = loadSchema("schema_object_additional.json");
        Jsony val = loadJson("schema_object_additional_instance1.json");

        assertValid(s, val);
    }

    @Test public void testObjectAdditional2() {
        Schema<Jsony> s = loadSchema("schema_object_additional.json");
        Jsony val = loadJson("schema_object_additional_instance2.json");

        assertInvalid(s, val);
    }
    
    @Test public void testAllOf1() {
        Schema<Jsony> s = loadSchema("schema_allOf.json");
        Jsony val = loadJson("schema_allOf_instance1.json");

        assertInvalid(s, val);
    }

    @Test public void testAllOf2() {
        Schema<Jsony> s = loadSchema("schema_allOf.json");
        Jsony val = loadJson("schema_allOf_instance2.json");

        assertValid(s, val);
    }
    
    @Test public void testDate1() {
        Schema<Jsony> s = loadSchema("c/date.json");
        Jsony val = loadJson("c/date_instance1.json");

        assertValid(s, val);
        
        Jsony p = s.parse(val);
        
        Assert.assertTrue(p instanceof TestDate);
    }
    
    @Test public void testSelfValidate1() {
        Schema<Jsony> s = loadSchema("bad_schema.json");
        Schema<Jsony> meta = Schema.getMetaschema();

        assertInvalid(meta, s);
    }

    @Test public void testSelfValidate2() {
        Schema<Jsony> s = loadSchema("schema_object_additional.json");
        Schema<Jsony> meta = Schema.getMetaschema();

        assertValid(meta, s);
    }
    
    @Test public void testResolutionScope() {
        Schema<Jsony> s = loadSchema("resolution_scope.json");
        Assert.assertEquals("http://x.y.z/rootschema.json#", s.getResolutionScope().toString());
        
        Jsony js1 = s.get("schema1");
        Schema<?> s1 = s.from(js1);
        
        Assert.assertEquals("http://x.y.z/rootschema.json#foo", s1.getResolutionScope().toString());
    }

    @Test public void testResolutionScope2a() {
        Schema<Jsony> s = loadSchema("resolution_scope2.json#/definitions/schema2");
        Jsony val = loadJson("resolution_scope2_instance1.json");
        assertValid(s, val);
    }

    @Test public void testResolutionScope2b() {
        Schema<Jsony> s = loadSchema("resolution_scope2.json#/definitions/schema2");
        Jsony val = loadJson("resolution_scope2_instance2.json");
        assertInvalid(s, val);
    }
    
    @Test public void testSchemaLocation() {
        Schema<Jsony> s = loadSchema("bo_schema.json#/definitions/given_name");
        URI uri = s.getSchemaLocation();
        
        Schema<?> s2 = Schema.schemaFrom(uri);
        
        String str = s.toJson();
        String str2 = s2.toJson();
        
        Assert.assertEquals(str, str2);
    }
    
    @Test public void testSchemaLoad() {
        Schema<?> s = Schema.schemaFrom(Schema_Test.class, "bo_schema.json#/definitions/person");
        String comment = s.getCast("comment", String.class);
        Assert.assertEquals("Person", comment);
    }

    private void assertValid(Schema<Jsony> s, Jsony val) {
        ValidationResult r = s.validate(val);
        Assert.assertTrue(r.invalidReason(), r.isValid());
    }

    private void assertInvalid(Schema<Jsony> s, Jsony val) {
        ValidationResult r = s.validate(val);
        log.debug(r.toString());
        Assert.assertFalse(r.isValid());
    }

    private void assertInvalid(Schema<Jsony> s, Jsony val, int errorCount) {
        ValidationResult r = s.validate(val);
        log.debug(r.toString());
        Assert.assertEquals(errorCount, r.errors().size());
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
