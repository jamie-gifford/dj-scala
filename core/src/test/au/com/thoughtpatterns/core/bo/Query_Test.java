package au.com.thoughtpatterns.core.bo;

import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.thoughtpatterns.core.bo.Query.Row;
import au.com.thoughtpatterns.core.bo.bos.Address;
import au.com.thoughtpatterns.core.bo.bos.Person;
import au.com.thoughtpatterns.core.unittest.hsqldb.TestDB;
import au.com.thoughtpatterns.core.util.Parameters;


public class Query_Test {

    private TestDB testDB = new TestDB();
    
    @Test
    public void testQuery() {
        Query query = QueryFactory.create("select id from test_person where given_name = ?");
        query.setNextValue("rumplestiltskin");
        List<Row> rows = query.execute();
        
        Assert.assertEquals(1, rows.size());
        Long id = (Long) rows.get(0).getValue(0);
        
        Assert.assertTrue(2 == id);
    }
    
    // -----------------------------
    // Fixture
    
    @Before
    public void setup() {

        Parameters.pushContext();

        testDB.executeFile(Person.class, "sequence_setup.sql");
        testDB.executeFile(Person.class, "person_setup.sql");
        testDB.executeFile(Address.class, "address_setup.sql");
        
        Parameters params = Parameters.instance();
        params.set("include", "au/com/thoughtpatterns/core/bo/bos/test_ibatis.properties");
        
    }
    
    @After
    public void tearDown() {
        testDB.executeFile(Address.class, "address_teardown.sql");
        testDB.executeFile(Person.class, "person_teardown.sql");
        testDB.executeFile(Person.class, "sequence_teardown.sql");
        Parameters.popContext();
    }
    

    
}
