package au.com.thoughtpatterns.core.parcel;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.thoughtpatterns.core.bo.BOKey;
import au.com.thoughtpatterns.core.bo.Box;
import au.com.thoughtpatterns.core.bo.DefaultIssueFilter;
import au.com.thoughtpatterns.core.bo.Issue;
import au.com.thoughtpatterns.core.bo.IssueBox;
import au.com.thoughtpatterns.core.bo.IssueBoxException;
import au.com.thoughtpatterns.core.bo.bos.Address;
import au.com.thoughtpatterns.core.bo.bos.Audit;
import au.com.thoughtpatterns.core.bo.bos.Person;
import au.com.thoughtpatterns.core.bo.bos.PersonData;
import au.com.thoughtpatterns.core.bo.ibatis.IbatisBox;
import au.com.thoughtpatterns.core.bo.ibatis.IbatisConfig;
import au.com.thoughtpatterns.core.parcel.parcels.CreatePersonParcel;
import au.com.thoughtpatterns.core.parcel.parcels.DeletePersonParcel;
import au.com.thoughtpatterns.core.sql.Connections;
import au.com.thoughtpatterns.core.unittest.hsqldb.TestDB;
import au.com.thoughtpatterns.core.util.BusinessException;
import au.com.thoughtpatterns.core.util.Parameters;

/**
 * Test checkout functionality
 */
public class Checkout_Test {

    // Set up a test database
    TestDB testDB = new TestDB();

    /**
     * Test that a simple "add" parcel works
     */
    @Test
    public void testAddPerson() throws BusinessException {

        PersonData personData = new PersonData();
        personData.setFamilyName("F");
        personData.setGivenName("G");

        CreatePersonParcel p = new CreatePersonParcel(personData);

        Checkout c = new Checkout();
        CreatePersonParcel p2 = (CreatePersonParcel) c.execute(p);

        BOKey addedKey = p2.getAddedKey();

        Assert.assertNotNull(addedKey);

        Box box = createBox();
        Person person = (Person) box.load(addedKey);

        Assert.assertEquals("F", person.getFamilyName());
        Assert.assertEquals("G", person.getGivenName());
    }

    /**
     * Test that a simple "add" function without mandatory parameters fails, with 
     * appropriate field-level issues
     */
    @Test
    public void testAddNullPerson() {

        PersonData personData = new PersonData();
        personData.setLocalizer("my localizer");

        CreatePersonParcel p = new CreatePersonParcel(personData);

        Checkout c = new Checkout();
        try {
            c.execute(p);
            Assert.fail();
        } catch (IssueBoxException expected) {
            IssueBox issueBox = expected.getIssueBox();
            DefaultIssueFilter filter = new DefaultIssueFilter();
            filter.setLocalizer("my localizer");
            filter.setPropertyName("familyName");
            
            List<Issue> issues = issueBox.getIssues(filter);
            Assert.assertTrue(issues.size() == 1);
            
        } catch (BusinessException unexpected) {
            Assert.fail();
        }
    }
    
    /**
     * Test another simple parcel, a delete operation
     */
    @Test
    public void testDeleteParcel() throws BusinessException {
        Box firstBox = createBox();

        BOKey key = firstBox.createKey(Person.class, 1L);
        Person firstPerson = (Person) firstBox.load(key);
        Assert.assertNotNull(firstPerson);
        
        DeletePersonParcel delete = new DeletePersonParcel(key, false);
        Checkout c = new Checkout();
        c.execute(delete);

        Box secondBox = createBox();
        Person secondPerson = (Person) secondBox.load(key);
        Assert.assertNull(secondPerson);
        
    }
    
    /**
     * Test that the Checkout rolls back changes if the parcel throws 
     * a BusinessException.
     * This is the same as the {@link testDeleteParcel} method except that
     * we tell the DeletePersonParcel to throw an exception as part of its processing.
     */
    @Test
    public void testRollback() {
        Box firstBox = createBox();

        BOKey key = firstBox.createKey(Person.class, 1L);
        Person firstPerson = (Person) firstBox.load(key);
        Assert.assertNotNull(firstPerson);
        
        DeletePersonParcel delete = new DeletePersonParcel(key, true);
        Checkout c = new Checkout();
        try {
            c.execute(delete);
            Assert.fail();
        } catch (BusinessException expected) {}

        // Now check that theh delete did not actually commit
        Box secondBox = createBox();
        Person secondPerson = (Person) secondBox.load(key);
        Assert.assertNotNull(secondPerson);
    }
    
    /**
     * Test that the audit facility works 
     */
    @Test
    public void testAudit() throws Exception {

        Audit audit = new Audit();
        audit.setTxnUser("USER");
        audit.setTxnTimestamp(new Date());
        audit.setTxnName("TEST FUNCTION");
        
        PersonData personData = new PersonData();
        personData.setFamilyName("F");
        personData.setGivenName("G");

        CreatePersonParcel p = new CreatePersonParcel(personData);

        Checkout c = new Checkout();
        c.setAudit(audit);
        CreatePersonParcel p2 = (CreatePersonParcel) c.execute(p);

        BOKey addedKey = p2.getAddedKey();

        Assert.assertNotNull(addedKey);

        Box box = createBox();
        Person person = (Person) box.load(addedKey);

        // TODO add assertions that verify the audit
    }

    // --------------------------------------
    // Fixtures

    @Before
    public void setup() {

        Parameters.pushContext();

        Parameters params = Parameters.instance();
        params.set("include",
                "au/com/thoughtpatterns/core/bo/bos/test_ibatis.properties");

        Connections.startTransaction();
        
        testDB.executeFile(Address.class, "audit_setup.sql");
        testDB.executeFile(Person.class, "sequence_setup.sql");
        testDB.executeFile(Person.class, "person_setup.sql");
        testDB.executeFile(Address.class, "address_setup.sql");

        Connections.endTransaction();
    }

    @After
    public void tearDown() {
        Connections.startTransaction();
        
        testDB.executeFile(Address.class, "address_teardown.sql");
        testDB.executeFile(Person.class, "person_teardown.sql");
        testDB.executeFile(Address.class, "audit_teardown.sql");
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
