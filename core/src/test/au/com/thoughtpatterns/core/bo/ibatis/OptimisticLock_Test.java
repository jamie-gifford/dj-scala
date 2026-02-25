package au.com.thoughtpatterns.core.bo.ibatis;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.thoughtpatterns.core.bo.BOKey;
import au.com.thoughtpatterns.core.bo.BOKeyImpl;
import au.com.thoughtpatterns.core.bo.Box;
import au.com.thoughtpatterns.core.bo.VersionException;
import au.com.thoughtpatterns.core.bo.bos.Address;
import au.com.thoughtpatterns.core.bo.bos.Audit;
import au.com.thoughtpatterns.core.bo.bos.Person;
import au.com.thoughtpatterns.core.sql.Connections;
import au.com.thoughtpatterns.core.unittest.hsqldb.TestDB;
import au.com.thoughtpatterns.core.util.Parameters;

/**
 * Test the optimistic lock facility
 */
public class OptimisticLock_Test {

    private IbatisBox ibatisBox;

    // Set up a test database
    TestDB testDB = new TestDB();

    /**
     * Fetch an object, update it and then try to update it again. Should fail
     * (optimistic lock).
     */
    @Test
    public void testSerialLock() {
        BOKey<Person> key = ibatisBox.createKey(Person.class, 1L);
        Person person = ibatisBox.load(key);
        BOKey<?> versionedKey = person.getBOKey();

        IbatisBox updateBox = createBox();

        Audit audit = new Audit();
        updateBox.add(audit);
        updateBox.flush();
        BOKeyImpl<?> auditKey = (BOKeyImpl<?>) audit.getBOKey();

        updateBox.setActivityId(auditKey.getId());

        Person updatePerson = updateBox.load(key);

        updatePerson.setGivenName("Modified");
        updateBox.flush();

        Box altBox = createBox();
        try {
            // Load should fail - stale key
            altBox.load(versionedKey);
            Assert.fail();
        } catch (VersionException expected) {}
    }

    /**
     * Test the case where the load works but the update fails. This simulates
     * "simultaneous" overlapping transactions
     */
    @Test
    public void testParallelLock() {
        // Create a couple of audit records
        Box box = createBox();

        Audit audit = new Audit();
        Audit audit2 = new Audit();

        box.add(audit);
        box.add(audit2);

        box.flush();

        BOKeyImpl<?> auditKey = (BOKeyImpl<?>) audit.getBOKey();
        BOKeyImpl<?> auditKey2 = (BOKeyImpl<?>) audit2.getBOKey();

        // Create two parallel boxes and set them up with the two audit records

        IbatisBox box1 = createBox();
        IbatisBox box2 = createBox();

        box1.setActivityId(auditKey.getId());
        box2.setActivityId(auditKey2.getId());

        BOKey<Person> unversionedKey = box.createKey(Person.class, 1L);

        // Fetch two records in parallel
        Person person1 = box1.load(unversionedKey);
        Person person2 = box2.load(unversionedKey);

        // Update the records in parallel
        person1.setGivenName("1");
        person2.setGivenName("2");

        // Now try flushing - the second flush should fail
        box1.flush();
        try {
            box2.flush();
            Assert.fail();
        } catch (VersionException expected) {}
    }

    /**
     * Test that we can add an object then update it.
     * This tests that the optimistic lock code keeps the version up to date.
     */
    @Test
    public void testInsertUpdate() {
        IbatisBox box = createBox();

        Audit audit = new Audit();
        box.add(audit);
        box.flush();
        BOKeyImpl<?> auditKey = (BOKeyImpl<?>) audit.getBOKey();

        box.setActivityId(auditKey.getId());

        Person person = new Person();
        person.setGivenName("A");
        box.add(person);
        
        person.setFamilyName("B");
        
        box.flush();
        
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
        testDB.executeFile(Person.class, "audit_setup.sql");
        testDB.executeFile(Person.class, "person_setup.sql");
        testDB.executeFile(Address.class, "address_setup.sql");

        ibatisBox = createBox();
    }

    @After
    public void tearDown() {
        testDB.executeFile(Address.class, "address_teardown.sql");
        testDB.executeFile(Person.class, "person_teardown.sql");
        testDB.executeFile(Person.class, "audit_teardown.sql");
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
