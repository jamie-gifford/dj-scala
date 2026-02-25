package au.com.thoughtpatterns.tmp;

import au.com.thoughtpatterns.core.json.AJsonyObject;
import au.com.thoughtpatterns.core.json.Jsony;
import au.com.thoughtpatterns.core.json.JsonyArray;
import au.com.thoughtpatterns.core.util.Generated;

/**
 *
 * @Generated from schema.json by ThoughtPatterns JSON code generator.
 */ 
@Generated
public class Family extends AJsonyObject {


    @Generated
    public Person getMother() {
        return getCast("mother", Person.class);
    }

    @Generated
    public void setMother(Person val) {
        set("mother", Jsony.of(val));
    }

    @Generated
    public Person getFather() {
        return getCast("father", Person.class);
    }

    @Generated
    public void setFather(Person val) {
        set("father", Jsony.of(val));
    }
 
    public JsonyArray<Person> getChildren() {
        return (JsonyArray<Person>) get("children");
    }
    
    public void setChildren(JsonyArray<Person> val) {
        set("children", val);
    }
    

}