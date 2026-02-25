package au.com.thoughtpatterns.core.bo.ibatis;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.thoughtpatterns.core.bo.BOKey;
import au.com.thoughtpatterns.core.bo.Box;
import au.com.thoughtpatterns.core.bo.Query;
import au.com.thoughtpatterns.core.bo.QueryFactory;
import au.com.thoughtpatterns.core.bo.ReadOnlyBoxException;
import au.com.thoughtpatterns.core.bo.bos.Address;
import au.com.thoughtpatterns.core.bo.bos.Person;
import au.com.thoughtpatterns.core.sql.Connections;
import au.com.thoughtpatterns.core.unittest.hsqldb.TestDB;
import au.com.thoughtpatterns.core.util.Parameters;

public class IbatisBox_Test {

    private IbatisBox ibatisBox;

    // Set up a test database
    TestDB testDB = new TestDB();

    /**
     * Test that the IbatisBox doesn't blow up.
     */
    @Test public void testBomb() {

    }

    /**
     * Test that we can fetch a sequence value
     */
    @Test public void testSequence() {
        long next = ibatisBox.fetchNextSequenceValue();
        Assert.assertTrue(next > 0);
    }

    /**
     * Test that we can load a person
     */
    @Test public void testLoad() {
        BOKey key = ibatisBox.createKey(Person.class, 1L);
        Person person = (Person) ibatisBox.load(key);

        Assert.assertEquals("Jorge", person.getGivenName());
    }

    /**
     * Test that we can load a person using a custom loader
     */
    @Test public void testCustom() {
        HashMap<String, Long> params = new HashMap<String, Long>();
        params.put("id", 1L);
        List<Person> results = ibatisBox.preload(Person.class, "au.com.thoughtpatterns.core.bo.bos.Person.custom", params);
        
        Assert.assertEquals(2, results.size());
    }

    /**
     * Test that we can add a person
     */
    @Test public void testAdd() {
        Person person = new Person();
        person.setGivenName("Slartibartfast");
        ibatisBox.add(person);
        ibatisBox.flush();

        Assert.assertTrue(person.getBOKey().isPersistent());

        BOKey key = person.getBOKey();

        // Check that the box now knows about the person instance
        Person persona = (Person) ibatisBox.load(key);
        Assert.assertEquals(person, persona);

        Box box2 = createBox();
        Person person2 = (Person) box2.load(key);

        Assert.assertNotNull(person2);
        Assert.assertEquals("Slartibartfast", person2.getGivenName());
    }

    /**
     * Test that we can update a person
     */
    @Test public void testUpdate() {
        BOKey key = ibatisBox.createKey(Person.class, 1L);
        Person person = (Person) ibatisBox.load(key);

        person.setGivenName("UPDATED");
        ibatisBox.flush();

        Box box2 = createBox();
        Person person2 = (Person) box2.load(key);

        Assert.assertNotNull(person2);
        Assert.assertEquals("UPDATED", person2.getGivenName());
    }

    /**
     * Test read only works
     */
    @Test public void testUpdateReadOnly() {
        BOKey key = ibatisBox.createKey(Person.class, 1L);
        Person person = (Person) ibatisBox.load(key);

        try {
            ibatisBox.setReadOnly(true);
            person.setGivenName("UPDATED");
            ibatisBox.flush();
            Assert.fail();
        } catch (ReadOnlyBoxException expected) {}
    }

    /**
     * Test that we can delete a person
     */
    @Test public void testDelete() {
        BOKey key = ibatisBox.createKey(Person.class, 1L);
        Person person = (Person) ibatisBox.load(key);

        ibatisBox.delete(person);
        ibatisBox.flush();

        Box box2 = createBox();
        Person person2 = (Person) box2.load(key);

        Assert.assertNull(person2);
    }

    /**
     * Test that we can delete a person and see the change *in the same box*
     */
    @Test public void testDeleteSameBox() {
        BOKey key = ibatisBox.createKey(Person.class, 1L);
        Person person = (Person) ibatisBox.load(key);

        ibatisBox.delete(person);
        ibatisBox.flush();

        Person person2 = (Person) ibatisBox.load(key);

        Assert.assertNull(person2);
    }

    /**
     * Test that we can add an address
     */
    @Test public void testAddAddress() {
        Address address = new Address();
        address.setStreetName("Street");
        ibatisBox.add(address);
        ibatisBox.flush();
    }

    /**
     * Test that we can resolve a person from an address
     */
    @Test public void testRelationship() {
        BOKey<Address> key = ibatisBox.createKey(Address.class, 1L);
        Address address = ibatisBox.load(key);
        Person person = address.getPerson();

        Assert.assertNotNull(person);
    }

    /**
     * Test that the OneToMany relationship can be navigated
     */
    @Test public void testOneToMany() {
        BOKey<Address> key = ibatisBox.createKey(Address.class, 1L);
        Address address = ibatisBox.load(key);
        Person person = address.getPerson();

        List<Address> addresses = person.getAddresses();
        Assert.assertEquals(2, addresses.size());

        Assert.assertEquals(address, addresses.get(0));
    }

    /**
     * Test bulk load (should only execute a single SQL)
     */
    @Test public void testBulkLoad() {
        List<BOKey<Person>> keys = new ArrayList<BOKey<Person>>();
        keys.add(ibatisBox.createKey(Person.class, 1L));
        keys.add(ibatisBox.createKey(Person.class, 2L));
        keys.add(ibatisBox.createKey(Person.class, 3L));

        // Do the preload
        ibatisBox.preload(keys);

        // Now individual loads - results should be cached
        ibatisBox.load(keys.get(0));
        ibatisBox.load(keys.get(1));
        ibatisBox.load(keys.get(2));

        // TODO add some mocking so that we can actually assert that the
        // bulk load operation has worked.
    }
    
    /**
     * Test big bulkload (1000 entries)
     */
    @Test public void testBigBulkLoad() throws Exception {
        final int BIG = 1000;
        Connections.startTransaction();
        Connection conn = Connections.getConnection();
        for (int i = 0; i < BIG; i++) {
            Statement s = conn.createStatement();
            s.execute("insert into test_person ( id, given_name ) values ( " + (i+10000) + ", " + "'X" + i + "')");
        }
        conn.close();
        Connections.endTransaction();

        Box box = createBox();
        Query q = QueryFactory.create("select id from test_person where id >= 10000");
        
        List<Person> people = box.preload(q, Person.class);
        Assert.assertEquals(BIG, people.size());
    }

    /**
     * Test that we can navigate a OneToMany relationship and that it responds
     * to a deletion of an entity correctly
     */
    @Test public void testOneToManyDelete() {
        BOKey<Address> key = ibatisBox.createKey(Address.class, 1L);
        Address address = ibatisBox.load(key);
        Person person = address.getPerson();

        List<Address> addresses = person.getAddresses();
        Assert.assertEquals(2, addresses.size());

        Assert.assertEquals(address, addresses.get(0));

        ibatisBox.delete(address);

        Assert.assertEquals(1, addresses.size());
    }

    /**
     * Test that we can navigate a OneToMany relationship and that it responds
     * to a addition of an entity correctly
     */
    @Test public void testOneToManyInsert() {
        BOKey<Address> key = ibatisBox.createKey(Address.class, 1L);
        Address address = ibatisBox.load(key);
        Person person = address.getPerson();

        List<Address> addresses = person.getAddresses();
        Assert.assertEquals(2, addresses.size());

        Address newAddress = new Address();
        newAddress.setStreetNumber("10");
        newAddress.setPerson(person);
        ibatisBox.add(newAddress);

        Assert.assertEquals(3, addresses.size());
    }

    /**
     * Test that our shadowed one-to-many (Australian addresses) works
     */
    @Test public void testOneToManyShadowed() {
        BOKey<Address> key = ibatisBox.createKey(Address.class, 1L);
        Address address = ibatisBox.load(key);
        Person person = address.getPerson();

        List<Address> addresses = person.getAusAddresses();
        Assert.assertEquals(1, addresses.size());

        // Add a non-australian address and check that it doesn't show
        // up in the list of australian addresses
        Address osAddress = new Address();
        osAddress.setCountry("Rhodesia");
        osAddress.setPerson(person);
        ibatisBox.add(osAddress);

        Assert.assertFalse(addresses.contains(osAddress));

        // Add an australian address and check that it *does* show up
        // in the list of australian addresses
        Address ausAddress = new Address();
        ausAddress.setCountry("Australia");
        ausAddress.setPerson(person);
        ibatisBox.add(ausAddress);

        Assert.assertTrue(addresses.contains(ausAddress));

        // Change the first australian address to a non-australian
        // address and check that it disappears out of the list

        Address chango = addresses.get(0);
        chango.setCountry("Fiji");
        Assert.assertFalse(addresses.contains(chango));

        ibatisBox.flush();
    }

    /**
     * Test that we can't put oversize fields into the DB without incurring
     * "issues"
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
