package au.com.thoughtpatterns.core.bo.ibatis;

import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.thoughtpatterns.core.bo.Box;
import au.com.thoughtpatterns.core.bo.OneToMany;
import au.com.thoughtpatterns.core.bo.Query;
import au.com.thoughtpatterns.core.bo.QueryFactory;
import au.com.thoughtpatterns.core.bo.bos.Address;
import au.com.thoughtpatterns.core.bo.bos.Person;
import au.com.thoughtpatterns.core.sql.Connections;
import au.com.thoughtpatterns.core.unittest.hsqldb.TestDB;
import au.com.thoughtpatterns.core.util.Parameters;

/**
 * Tests for "static" one-to-many relationships
 */
public class StaticOneToMany_Test {

    private IbatisBox ibatisBox;

    // Set up a test database
    TestDB testDB = new TestDB();
    
    @Test
    public void testPersonList() {
    
        ibatisBox = createBox();
        
        PersonList list = new PersonList(ibatisBox);
        
        List<Person> people = list.get();
        
        Assert.assertEquals(1, list.get().size());
        
        // Delete the Jorge, list should shrink
        
        Person jorge = people.get(0);
        ibatisBox.delete(jorge);
        
        Assert.assertEquals(0, list.get().size());
        
        // Create James, list should grow
        Person james = new Person();
        james.setGivenName("James");
        ibatisBox.add(james);
        
        Assert.assertEquals(1, list.get().size());
        
        // Change james's name, list should shrink
        
        james.setGivenName("Bob");
        Assert.assertEquals(0, list.get().size());
        
    }
    
    static class PersonList extends OneToMany<Person> {

        private static final long serialVersionUID = 1L;

        PersonList(Box aOwner) {
            super(aOwner, Person.class);
        }
        
        // All people with a name starting with "A" belong
        @Override protected boolean belongs(Person candidate) {
            String given = candidate.getGivenName();
            return (given != null && given.startsWith("J"));
        }

        @Override protected Query query() {
            String sql = "select id from test_person where given_name like 'J%'";
            return QueryFactory.create(sql);
        }
        
        
        
    }

    // -----------------------------
    // Fixture

    @Before public void setup() {

        Parameters.pushContext();

        Parameters params = Parameters.instance();
        params.set("include", "au/com/thoughtpatterns/core/bo/bos/test_ibatis.properties");

        Connections.startTransaction();

        testDB.executeFile(Person.class, "sequence_setup.sql");
        testDB.executeFile(Person.class, "person_setup.sql");
        testDB.executeFile(Address.class, "address_setup.sql");

        ibatisBox = createBox();
    }

    @After public void tearDown() {
        testDB.executeFile(Address.class, "address_teardown.sql");
        testDB.executeFile(Person.class, "person_teardown.sql");
        testDB.executeFile(Person.class, "sequence_teardown.sql");

        Connections.endTransaction();

        Parameters.popContext();
    }

    private IbatisBox createBox() {
        IbatisConfig config = new IbatisConfig();

        IbatisBox box = new IbatisBox();
        box.setIbatisConfig(config);
        return box;
    }


}
