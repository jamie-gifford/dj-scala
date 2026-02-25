package au.com.thoughtpatterns.core.bo;

import java.util.HashSet;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.thoughtpatterns.core.bo.bos.Person;
import au.com.thoughtpatterns.core.unittest.hsqldb.TestDB;

public class Box_Test {

    private TestDB testDB = new TestDB();

    private MockBox box = new MockBox();
    private Listener listener = new Listener();

    /**
     * Test that the setup of hypersonic doesn't bomb out
     *
     */
    @Test
    public void testBomb() {
        
    }

    @Test
    public void testDetectDirty() {
        BOKey key = box.createKey(Person.class, 1L);
        Person person = (Person) box.load(key);
        
        person.addListener(listener);
        Assert.assertFalse(listener.changed.contains(person));
        person.setGivenName("G");
        Assert.assertTrue(listener.changed.contains(person));
    }

    @Test
    public void testLoad() {
        
    }
    
    
    private class MockBox extends CachingBox {

        private static final long serialVersionUID = 1L;

        @Override
        public void flush() {
            // NOP
        }

        @Override
        protected PersistentObject loadInt(BOKey key) {
            Person person = new Person();
            return person;
        }
    }
    
    private class Listener implements BusinessObjectListener {
        private static final long serialVersionUID = 1L;
        HashSet<BusinessObject> changed = new HashSet<BusinessObject>();
        
        public void changed(BusinessObject object) {
            changed.add(object);
        }   
    }
    
    @Before
    public void setUp() {
        testDB.startHypersonic();
        testDB.executeFile(this, "bos/person_setup.sql");
    }

    @After
    public void tearDown() {
        testDB.executeFile(this, "bos/person_teardown.sql");
        testDB.stopHypersonic();
    }

}
