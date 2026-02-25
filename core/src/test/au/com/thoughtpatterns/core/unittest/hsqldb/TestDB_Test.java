package au.com.thoughtpatterns.core.unittest.hsqldb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.thoughtpatterns.core.sql.Connections;

/**
 * Tests for TestDB interface to hypersonic
 */
public class TestDB_Test {

    private TestDB testDB = new TestDB();

    /**
     * Test that the setup of hypersonic doesn't bomb out
     * 
     */
    @Test
    public void testBomb() {

    }

    /**
     * Test that we can execute an sql statement and see the results via JDBC
     * 
     * @throws Exception
     */
    @Test
    public void testInsert() throws Exception {

        String sql = "insert into test_db ( id, name ) values ( 1, 'Fred')";
        testDB.execute(sql);

        Connection connection = null;
        try {
            connection = Connections.getConnection();

            PreparedStatement stmt = connection
                    .prepareStatement("select name from test_db where id = 1");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            rs.next();
            String name = rs.getString(1);

            Assert.assertEquals("Fred", name);
        } finally {
            connection.close();
        }
    }

    @Before
    public void setUp() {
        testDB.startHypersonic();
        testDB.executeFile(this, "TestDB_Test_setup.sql");
    }

    @After
    public void tearDown() {
        testDB.executeFile(this, "TestDB_Test_teardown.sql");
        testDB.stopHypersonic();
    }

}
