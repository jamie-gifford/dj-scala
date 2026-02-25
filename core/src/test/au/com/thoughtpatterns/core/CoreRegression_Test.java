package au.com.thoughtpatterns.core;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;

public class CoreRegression_Test extends TestSuite {
    public static junit.framework.Test suite() { 
        return new JUnit4TestAdapter(Core_Suite.class); 
    }
}
