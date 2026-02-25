package au.com.thoughtpatterns.core.util;

import junit.framework.Assert;

import org.junit.Test;


public class BusinessDate_Test {

    @Test
    public void testYearsSince() {
        
        checkYears("1971-03-06", "2007-09-21", 36);
        checkYears("1971-03-06", "2007-03-06", 36);
        checkYears("1971-03-06", "2007-03-05", 35);

        checkYears("1972-02-29", "2004-02-28", 31);
        checkYears("1972-02-29", "2004-02-29", 32);

    }
    
    @Test
    public void testAddDays() {
        BusinessDate a = BusinessDate.newYYYYMMDD_quiet("2001-02-02");
        BusinessDate b = a.addDays(28);
        String bs = b.toDD_MM_YYYY();
        Assert.assertEquals("02/03/2001", bs);

        BusinessDate c = a.addDays(-30);
        String cs = c.toDD_MM_YYYY();
        Assert.assertEquals("03/01/2001", cs);

        BusinessDate d = a.addDays(-60);
        String ds = d.toDD_MM_YYYY();
        Assert.assertEquals("04/12/2000", ds);
    }
    
    @Test
    public void testDecompose() {
        BusinessDate a = BusinessDate.newYYYYMMDD_quiet("2001-02-06");
        int[] bits = a.getDDMMYYYY();
        Assert.assertEquals(6, bits[0]);
        Assert.assertEquals(2, bits[1]);
        Assert.assertEquals(2001, bits[2]);
    }
    
    @Test
    public void testDaysSince() {
    	
    	int t3 = BusinessDate.newYYYYMMDD_quiet("2014-10-13").daysSince(BusinessDate.newYYYYMMDD_quiet("2014-10-03"));
    	
    	Assert.assertEquals(10, t3);
    	
    	int t4 = BusinessDate.newYYYYMMDD_quiet("2014-10-13").daysSince(BusinessDate.newYYYYMMDD_quiet("2014-10-06"));
    	
    	Assert.assertEquals(7, t4);
    	
    }
    
    @Test
    public void testAddYears() {
        // Check that adding years to 29th feb works okay
        BusinessDate a = BusinessDate.newDMY(29, 2, 1980);

        BusinessDate b = a.addYears(1);
        Assert.assertEquals("28/02/1981", b.toDD_MM_YYYY());

        BusinessDate c = a.addYears(4);
        Assert.assertEquals("29/02/1984", c.toDD_MM_YYYY());
}
    
    private void checkYears(String from, String to, int expected) {
        BusinessDate fromDate = BusinessDate.newYYYYMMDD_quiet(from);
        BusinessDate toDate = BusinessDate.newYYYYMMDD_quiet(to);
        int years = toDate.yearsSince(fromDate);
        
        Assert.assertEquals(expected, years);
    }
    
    
    
}
