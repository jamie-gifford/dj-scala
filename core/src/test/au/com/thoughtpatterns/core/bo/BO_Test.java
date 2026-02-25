package au.com.thoughtpatterns.core.bo;

import junit.framework.Assert;

import org.junit.Test;

import au.com.thoughtpatterns.core.bo.bos.Person;


public class BO_Test implements BusinessObjectListener {

    private static final long serialVersionUID = 1L;
    private int changes = 0;
    
    @Test
    public void testChange() {
        Person person = new Person();
        
        person.addListener(this);

        person.setFamilyName("X");
        
        Assert.assertEquals(1, changes);
    }
    
    @Test
    public void testLoadData() {
        
        Person person = new Person();
        person.setFamilyName("Smith");
        
        Person person2 = new Person();
        person2.setFamilyName("Jones");
        
        Person person3 = new Person();
        person3.setFamilyName("Jones");
        
        // If we load person with person3, then there should be a change event
        // If we load person2 with person3, there should not be
        
        changes = 0;
        person.addListener(this);
        person.loadData(person3);
        
        Assert.assertTrue(changes > 0);
        
        changes = 0;
        person2.addListener(this);
        person2.loadData(person3);
        
        Assert.assertTrue(changes == 0);
    }

    public void changed(BusinessObject object) {
        changes ++;
    }
    
}
