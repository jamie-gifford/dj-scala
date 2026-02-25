package au.com.thoughtpatterns.core.bo.ibatis;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.thoughtpatterns.core.bo.BOKey;
import au.com.thoughtpatterns.core.bo.Box;
import au.com.thoughtpatterns.core.bo.bos.DateBO;
import au.com.thoughtpatterns.core.sql.Connections;
import au.com.thoughtpatterns.core.unittest.hsqldb.TestDB;
import au.com.thoughtpatterns.core.util.AusDollar;
import au.com.thoughtpatterns.core.util.BusinessDate;
import au.com.thoughtpatterns.core.util.Parameters;


public class DateBO_Test {
    
    // Set up a test database
    TestDB testDB = new TestDB();

    /**
     * Test that the IbatisBox doesn't blow up.
     */
    @Test
    public void testBomb() {

    }

    /**
     * Test that we can CRUD a DateBO
     */
    @Test
    public void testCrud() throws Exception {

        final BusinessDate start = BusinessDate.newYYYYMMDD("2000-03-04");
        final BusinessDate finish = BusinessDate.newYYYYMMDD("2000-07-08");
        
        Box box1 = createBox();
        DateBO date = new DateBO();
        date.setStartDate(start);
        
        box1.add(date);
        box1.flush();
        
        BOKey key = date.getBOKey();
        key.unversion();
        Box box2 = createBox();
        DateBO date2 = (DateBO) box2.load(key);
        
        Assert.assertEquals(start, date2.getStartDate());        
        Assert.assertNull(date2.getFinishDate());
        
        AusDollar amount = AusDollar.new_cents(125);
        
        date2.setFinishDate(finish);
        date2.setAmount(amount);
        box2.flush();
        
        Box box3 = createBox();
        DateBO date3 = (DateBO) box3.load(key);
        
        Assert.assertEquals(start, date3.getStartDate());
        Assert.assertEquals(finish, date3.getFinishDate());
        Assert.assertEquals(amount, date3.getAmount());
        
        box3.delete(date3);
        box3.flush();
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
        
        testDB.executeFile(DateBO.class, "sequence_setup.sql");
        testDB.executeFile(DateBO.class, "date_test_setup.sql");
    }

    @After
    public void tearDown() {
        testDB.executeFile(DateBO.class, "date_test_teardown.sql");
        testDB.executeFile(DateBO.class, "sequence_teardown.sql");

        Connections.endTransaction();

        Parameters.popContext();
    }

    private IbatisBox createBox() {
        IbatisConfig config = new IbatisConfig();

        IbatisBox box = new IbatisBox();
        box.setIbatisConfig(config);
        box.setActivityId(1L);
        return box;
    }
}
