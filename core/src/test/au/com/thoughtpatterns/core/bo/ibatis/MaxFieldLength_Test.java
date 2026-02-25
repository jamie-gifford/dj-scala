package au.com.thoughtpatterns.core.bo.ibatis;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.thoughtpatterns.core.bo.BOKey;
import au.com.thoughtpatterns.core.bo.bos.Address;
import au.com.thoughtpatterns.core.bo.bos.Person;
import au.com.thoughtpatterns.core.sql.Connections;
import au.com.thoughtpatterns.core.unittest.hsqldb.TestDB;
import au.com.thoughtpatterns.core.util.Parameters;


public class MaxFieldLength_Test {

    private IbatisBox ibatisBox;

    // Set up a test database
    TestDB testDB = new TestDB();

    /**
     * Test that we can't put oversize fields into the DB
     */
    @Test public void testFieldSize() throws Exception {
        BOKey key = ibatisBox.createKey(Person.class, 1L);
        Person person = (Person) ibatisBox.load(key);

        person.setFamilyName("aaaaabbbbbcccccdddddeeeeefffff");
        ibatisBox.flush();
        Assert.assertTrue(ibatisBox.getIssueBox().hasIssues());
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

        ibatisBox = createBox();
    }

    @After
    public void tearDown() {
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
