package au.com.thoughtpatterns.core.bo.ibatis;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.thoughtpatterns.core.bo.BOKey;
import au.com.thoughtpatterns.core.bo.Box;
import au.com.thoughtpatterns.core.bo.RemoteBox;
import au.com.thoughtpatterns.core.bo.bos.Address;
import au.com.thoughtpatterns.core.bo.bos.Person;
import au.com.thoughtpatterns.core.sql.Connections;
import au.com.thoughtpatterns.core.unittest.hsqldb.TestDB;
import au.com.thoughtpatterns.core.util.Parameters;


public class RemoteBox_Test {
    
    // Set up a test database
    TestDB testDB = new TestDB();

    /**
     * Test that we can load a person
     */
    @Test
    public void testLoad() {
        Box box = createBox();
        BOKey key = box.createKey(Person.class, 1L);
        Person person = (Person) box.load(key);

        Assert.assertEquals("Jorge", person.getGivenName());
    }


    /**
     * Test that we can preload a person
     */
    @Test
    public void testPreload() {
        Box box = createBox();
        BOKey<Person> key = box.createKey(Person.class, 1L);
        List<BOKey<Person>> keys = new ArrayList<BOKey<Person>>();
        keys.add(key);
        List<Person> people = box.preload(keys);

        Assert.assertEquals(1, people.size());
        
        for (Person person : people) {
            Assert.assertEquals(box, person.getBox());
        }
    }

    // -----------------------------
    // Fixture

    @Before
    public void setup() {

        Parameters.pushContext();

        Parameters params = Parameters.instance();
        params.set("include",
                "au/com/thoughtpatterns/core/bo/bos/test_ibatis.properties");

        Connections.startTransaction();
        
        testDB.executeFile(Person.class, "sequence_setup.sql");
        testDB.executeFile(Person.class, "person_setup.sql");
        testDB.executeFile(Address.class, "address_setup.sql");
    }

    @After
    public void tearDown() {
        testDB.executeFile(Address.class, "address_teardown.sql");
        testDB.executeFile(Person.class, "person_teardown.sql");
        testDB.executeFile(Person.class, "sequence_teardown.sql");

        Connections.endTransaction();

        Parameters.popContext();
    }

    private Box createBox() {
        return new RemoteBox();
    }

}
