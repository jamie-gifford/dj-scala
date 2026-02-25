package au.com.thoughtpatterns.tmp;

import au.com.thoughtpatterns.core.util.Generated;
import au.com.thoughtpatterns.core.json.AJsonyObject;
import au.com.thoughtpatterns.core.json.Jsony;
import au.com.thoughtpatterns.core.json.JsonyArray;

/**
 *
 * @Generated from schema.json by ThoughtPatterns JSON code generator.
 */ 
@Generated
public class Person extends AJsonyObject {


    @Generated
    public String getGivenName() {
        return getCast("given_name", String.class);
    }

    @Generated
    public void setGivenName(String val) {
        set("given_name", Jsony.of(val));
    }

    @Generated
    public String getFamilyName() {
        return getCast("family_name", String.class);
    }

    @Generated
    public void setFamilyName(String val) {
        set("family_name", Jsony.of(val));
    }

    @Generated
    public Integer getAge() {
        return getCast("age", Integer.class);
    }

    @Generated
    public void setAge(Integer val) {
        set("age", Jsony.of(val));
    }

    public JsonyArray<Hobby> getHobbies() {
        return (JsonyArray<Hobby>) get("hobbies");
    }
    
    public void setChildren(JsonyArray<Person> val) {
        set("children", val);
    }
}