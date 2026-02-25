package au.com.thoughtpatterns.core.bo.ibatis;

import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.thoughtpatterns.core.bo.BOKey;
import au.com.thoughtpatterns.core.bo.bos.Address;
import au.com.thoughtpatterns.core.bo.bos.AltAddress;
import au.com.thoughtpatterns.core.bo.bos.AltPerson;
import au.com.thoughtpatterns.core.bo.bos.Person;
import au.com.thoughtpatterns.core.sql.Connections;
import au.com.thoughtpatterns.core.unittest.hsqldb.TestDB;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.Parameters;

/**
 * Test the dependent object framework
 */
public class DependentObject_Test {

    private static final Logger log = Logger.get(DependentObject_Test.class);
    
    private IbatisBox ibatisBox;

    // Set up a test database
    TestDB testDB = new TestDB();

    /**
     * Test that the IbatisBox doesn't blow up.
     */
    @Test
    public void testBomb() {

    }

    /**
     * Test that we can load an AltPerson and see the dependent AltAddresses
     */
    @Test
    public void testLoad() {
        BOKey<AltPerson> key = ibatisBox.createKey(AltPerson.class, 1L);
        AltPerson person = ibatisBox.load(key);

        Assert.assertNotNull(person);

        List<AltAddress> addresses = person.getAddresses();
        Assert.assertTrue(addresses.size() == 2);

        AltAddress address1 = addresses.get(0);
        Assert.assertEquals("13", address1.getStreetNumber());
        
        String xml = XmlHelper.marshall(person);
        log.info(xml);
        
        AltPerson d = (AltPerson) XmlHelper.fromXml(xml);
        List<AltAddress> a = d.getAddresses();
        Assert.assertTrue(a.size() == 2);
        AltAddress a1 = addresses.get(0);
        Assert.assertEquals("13", a1.getStreetNumber());
    }

    /**
     * Test that we can load an AltPerson, make a change to an address, and
     * persist the whole lot
     */
    @Test
    public void testUpdateAddress() {
        BOKey<AltPerson> key = ibatisBox.createKey(AltPerson.class, 1L);
        try {
            AltPerson person = ibatisBox.load(key);

            List<AltAddress> addresses = person.getAddresses();
            AltAddress address1 = addresses.get(0);

            address1.setStreetNumber("999");

            ibatisBox.flush();
        } finally {
            // End of scope
        }

        IbatisBox box2 = createBox();

        AltPerson person2 = box2.load(key);

        List<AltAddress> addresses2 = person2.getAddresses();
        AltAddress address2 = addresses2.get(0);

        Assert.assertEquals("999", address2.getStreetNumber());
    }

    /**
     * Test that we can load an AltPerson, delete addresses, and see
     * the deletions
     */
    @Test
    public void testClearAddresses() {
        BOKey<AltPerson> key = ibatisBox.createKey(AltPerson.class, 1L);
        try {
            AltPerson person = ibatisBox.load(key);

            List<AltAddress> addresses = person.getAddresses();
            addresses.clear();
            
            ibatisBox.flush();
        } finally {
            // End of scope
        }

        IbatisBox box2 = createBox();

        AltPerson person2 = box2.load(key);

        List<AltAddress> addresses2 = person2.getAddresses();
        Assert.assertEquals(0, addresses2.size());
        
    }

    /**
     * Test that we can load an AltPerson, add an address, and see
     * the addition
     */
    @Test
    public void testAddAddresses() {
        BOKey<AltPerson> key = ibatisBox.createKey(AltPerson.class, 1L);
        int beforeSize;
        try {
            AltPerson person = ibatisBox.load(key);

            List<AltAddress> addresses = person.getAddresses();
            beforeSize = addresses.size();
            
            AltAddress newAddress = new AltAddress();
            newAddress.setStreetName("New street");
            addresses.add(newAddress);
            
            ibatisBox.flush();
        } finally {
            // End of scope
        }

        IbatisBox box2 = createBox();

        AltPerson person2 = box2.load(key);

        List<AltAddress> addresses2 = person2.getAddresses();
        Assert.assertEquals(beforeSize + 1, addresses2.size());        
    }

    /**
     * Test that we can load an AltPerson, add an address, delete the address,
     * and that no change happens
     */
    @Test
    public void testAddDeleteAddresses() {
        BOKey<AltPerson> key = ibatisBox.createKey(AltPerson.class, 1L);
        int beforeSize;
        try {
            AltPerson person = ibatisBox.load(key);

            List<AltAddress> addresses = person.getAddresses();
            beforeSize = addresses.size();
            
            AltAddress newAddress = new AltAddress();
            newAddress.setStreetName("New street");
            addresses.add(newAddress);
            
            addresses.remove(newAddress);
            
            ibatisBox.flush();
        } finally {
            // End of scope
        }

        IbatisBox box2 = createBox();

        AltPerson person2 = box2.load(key);

        List<AltAddress> addresses2 = person2.getAddresses();
        Assert.assertEquals(beforeSize, addresses2.size());        
    }

    /**
     * Test that we can add 100 addresses and remove 99 of them, then flush
     * and only get one insert
     */
    @Test
    public void testAddAndDeleteLots() {
        BOKey<AltPerson> key = ibatisBox.createKey(AltPerson.class, 1L);
        int beforeSize;
        try {
            AltPerson person = ibatisBox.load(key);

            List<AltAddress> addresses = person.getAddresses();
            beforeSize = addresses.size();

            final int BIG = 100;
            for (int i = 0; i < BIG; i++) {
                AltAddress newAddress = new AltAddress();
                newAddress.setStreetName("New street " + i);
                addresses.add(newAddress);
            }

            for (int i = 0; i < BIG - 1; i++) {
                addresses.remove(0);
            }

            ibatisBox.flush();
        } finally {
            // End of scope
        }

        IbatisBox box2 = createBox();

        AltPerson person2 = box2.load(key);

        List<AltAddress> addresses2 = person2.getAddresses();
        Assert.assertEquals(beforeSize + 1, addresses2.size());        
    }
    
    // -----------------------------
    // Fixture

    @Before
    public void setup() {

        Parameters.pushContext();

        Connections.startTransaction();

        testDB.executeFile(Person.class, "sequence_setup.sql");
        testDB.executeFile(Person.class, "person_setup.sql");
        testDB.executeFile(Address.class, "address_setup.sql");

        Parameters params = Parameters.instance();
        params
                .set("include",
                        "au/com/thoughtpatterns/core/bo/bos/test_alt_ibatis.properties");

        // Create a config with the new parameters
        IbatisConfig config = new IbatisConfig();
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
