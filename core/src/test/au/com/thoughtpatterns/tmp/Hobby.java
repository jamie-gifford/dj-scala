package au.com.thoughtpatterns.tmp;

import au.com.thoughtpatterns.core.util.Generated;
import au.com.thoughtpatterns.core.json.AJsonyObject;
import au.com.thoughtpatterns.core.json.Jsony;

/**
 *
 * @Generated from schema.json by ThoughtPatterns JSON code generator.
 */ 
@Generated
public class Hobby extends AJsonyObject {


    @Generated
    public Integer getYearsPracticed() {
        return getCast("years_practiced", Integer.class);
    }

    @Generated
    public void setYearsPracticed(Integer val) {
        set("years_practiced", Jsony.of(val));
    }

    @Generated
    public String getHobby() {
        return getCast("hobby", String.class);
    }

    @Generated
    public void setHobby(String val) {
        set("hobby", Jsony.of(val));
    }

}