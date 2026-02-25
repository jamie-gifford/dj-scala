package au.com.thoughtpatterns.core.util;

import java.util.Stack;

/**
 * Represents a "profiled" chunk of a transaction.
 * 
 * ProfilePoints can be started at the beginning of a chunk of the 
 * transaction, and stopped at the end. The elapsed time is tracked, and
 * logged into the "profile log".
 * 
 * ProfilePoints can be nested, and the profile log will indicate the nesting.
 */
public class ProfilePoint {

    /**
     * A logger that doesn't use the usual Java package structure
     * to define its category. The intention is that logs to the "profile"
     * category can be sent off to a separate log file.
     */
    private static final Logger profileLog = Logger.get("profile");
    
    /**
     * Identifies, for loggging, a "class" of profile point (eg, servlet, sql, txn, ...)
     */
    private String pointClass;
    
    /**
     * Identified, for logging, an "instance" for the ProfilePoint (eg, the servlet name,
     * the sql being executed, the name of the business txn),
     */
    private String pointInstance;
    
    /**
     * A link to a ProfiledTransaction that will be used to log the 
     * nested trace (the "stack" of integers). Usually this will be the 
     * "current" ProfiledTransaction.
     */
    private ProfiledTransaction txn;
    
    /**
     * Record when the ProfilePoint was started (for logging)
     */
    private long startTime = 0;
    
    /**
     * Cache the "trace stack" derived from the ProfiledTransaction when the 
     * ProfilePoint is started.
     */
    private String trace;
    
    /**
     * Create a ProfilePoint using the given pointClass and pointInstance 
     * strings. These two strings will appear in the profile logs, and should
     * orient the reader of the profile logs as to what the ProfilePoint is 
     * representing.
     * 
     * @param aPointClass
     * @param aPointInstance
     */
    public ProfilePoint(String aPointClass, String aPointInstance) {
        pointClass = aPointClass;
        pointInstance = aPointInstance;
    }
    
    /**
     * Set the ProfiledTransaction that this point works with.
     * This method does not usually need to be set - the ProfilePoint
     * will automatically use the current ProfiledTransaction when started
     * if this method is not used.
     * @param aTxn
     */
    public void setTxn(ProfiledTransaction aTxn) {
        txn = aTxn;
    }

    /**
     * Start the profile point. This method must be followed by a call to 
     * {@link #stop} later. Usually the {@link #stop} method should be called in 
     * a <code>finally</code> block to handle the case where exceptions are thrown
     * during the intervening code.
     */
    public void start() {
        startTime = System.currentTimeMillis();
        
        if (txn == null) {
            txn = ProfiledTransaction.getCurrent(true);
        }
        
        txn.push();
        logStart();
    }
    
    /**
     * Stop the profile point. The total time elapsed from start to finish will
     * be logged in the profile log.
     */
    public void stop() {
        
        if (startTime == 0) {
            throw new SystemException("Stop called on un-started profile point");
        }
        
        logStop();
        
        txn.pop();
    }

    // -----------------
    // Private methods
    
    private void logStart() {
        trace = getTrace();
        String summary = getSummary();        
        String msg = trace + " > " + summary;
        profileLog.info(msg);
    }
    
    private void logStop() {
        long now = System.currentTimeMillis();
        long elapsed = now - startTime;
        String summary = getSummary();        
        String msg = trace + " < " + elapsed + " ms " + summary;
        profileLog.info(msg);
    }
    
    private String getSummary() {
        Runtime r = Runtime.getRuntime();
        long used = ( r.totalMemory() - r.freeMemory() ) >> 20;
        long max = r.maxMemory() >> 20;
        return "[" + pointClass + ":" + pointInstance + "] (mem:" + used + "m of " + max + "m)";
    }
    
    private String getTrace() {
        Stack<Integer> stack = txn.getStack();
        StringBuffer buff = new StringBuffer();
        for (int i : stack) {
            buff.append(i);
            buff.append(" ");
        }
        String s = buff.toString();
        s = s.trim();
        return s;
    }
}
