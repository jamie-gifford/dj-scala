package au.com.thoughtpatterns.core.bo;

import junit.framework.Assert;

import org.junit.Test;

import au.com.thoughtpatterns.core.bo.bos.Person;
import au.com.thoughtpatterns.core.bo.bos.PersonData;


public class BusinessObject_Test {

    /**
     * Test that the loadData operation works
     *
     */
    @Test
    public void testLoad() {
        PersonData data = new PersonData();
        data.setFamilyName("F");
        data.setGivenName("G");
        
        Person p = new Person();
        p.loadData(data);
        
        Assert.assertEquals("F", p.getFamilyName());
        Assert.assertEquals("G", p.getGivenName());
    }
    
}
