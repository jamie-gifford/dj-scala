package au.com.thoughtpatterns.core.util;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;


public class CsvUtils_Test {

    private static final Logger log = Logger.get(CsvUtils_Test.class);
    
    private CsvUtils csv = new CsvUtils();
    
    @Test
    public void testCsv() {
        
        String[][] data = new String[][] {
                { "1997", "Ford", "E350", "ac, abs, moon"},
                { "1996", "Jeep", "Grand Cherokee", "Come now\nMust sell!"}
        };

        csv.toCsv(data);
        
        String out = csv.getFormattedString();
        log.debug(out);
        
        String expected = "1997,Ford,E350,\"ac, abs, moon\"\n" + 
            "1996,Jeep,Grand Cherokee,\"Come now\n" + 
            "Must sell!\"\n";

        Assert.assertEquals(expected, out);
    }
    
    @Test
    public void testFromCsv1() {
        checkRoundTrip("1997,Ford,E350,\"ac, abs, moon\"\n" + 
        "1996,Jeep,Grand Cherokee,\"Come now\n" + 
        "Must sell!\"\n");
    }
    
    @Test
    public void testFromCsv2() {
        checkRoundTrip("Hi,,there\n" + "Lo,,there\n");
    }
    
    @Test
    public void testFromCsv3() {
        checkRoundTrip("Lo,,there,\n");
    }

    @Test
    public void testFromCsv4() {
        checkRoundTrip("\n");
    }

    private void checkRoundTrip(String data) {
        List<String[]> records = csv.fromCsv(data);
        
        // Round trip
        String[][] r = new String[records.size()][];
        records.toArray(r);
        
        csv.toCsv(r);
        String round = csv.getFormattedString();
        
        Assert.assertEquals(data, round);
    }
    
}
