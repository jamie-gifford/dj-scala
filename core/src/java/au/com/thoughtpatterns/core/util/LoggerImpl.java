package au.com.thoughtpatterns.core.util;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.MDC;
import org.apache.log4j.PropertyConfigurator;

class LoggerImpl extends Logger {

    org.apache.log4j.Logger log4j;

    // Initialise on load
    static {
        initialise();
    }
    
    LoggerImpl(String logger) {
        log4j = org.apache.log4j.Logger.getLogger(logger);
    }
    
    public void debug(String msg, Throwable ex) {
        log4j.debug(msg, ex);
    }

    public void debug(String msg) {
        log4j.debug(msg);
    }

    public void error(String msg, Throwable ex) {
        log4j.error(msg, ex);
    }

    public void error(String msg) {
        log4j.error(msg);
    }

    public void info(String msg, Throwable ex) {
        log4j.info(msg, ex);
    }

    public void info(String msg) {
        log4j.info(msg);
    }

    public boolean isDebugOn() {
        return log4j.isDebugEnabled();
    }

    public boolean isErrorOn() {
        return log4j.isEnabledFor(Level.ERROR);
    }

    public boolean isInfoOn() {
        return log4j.isInfoEnabled();
    }
    
    public static void setMDCInt(String key, String value) {
        MDC.put(key, value);
    }

    private static void initialise() {
        Parameters params = Parameters.instance();
        Properties props = params.getProperties("log4j.");
        PropertyConfigurator.configure(props);

        String dump = null;
        if (params.getBoolean("params.dump")) {
            dump = dumpParams(params);
            get(Logger.class).info(dump);
        }
        if (params.getBoolean("params.dump.console")) {
            if (dump == null) {
                dump = dumpParams(params);
            }
            System.out.println(dump);
        } else {
            System.out.println("Logging configured, set params.dump.console=true for parameter dump");
        }
    }
    
    private static String dumpParams(Parameters params) {
        String out = "Logging configured, parameter dump follows...\n";
        
        File working = new File(".").getAbsoluteFile();
        out += ("Working directory is " + working + "\n");

        out = out + params.dump();
        return out;
    }
    
}
