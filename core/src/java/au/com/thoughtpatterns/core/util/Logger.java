package au.com.thoughtpatterns.core.util;

public abstract class Logger {

    // Factory method
    public static Logger get(String logger) {
        return new LoggerImpl(logger);
    }
    
    public static Logger get(Class logger) {
        return get(logger.getName());
    }
    
    // ---------------------------------------
    // Logging API
    
    public abstract void debug(String msg);

    public abstract void info(String msg);

    public abstract void error(String msg);

    public abstract void debug(String msg, Throwable ex);

    public abstract void info(String msg, Throwable ex);

    public abstract void error(String msg, Throwable ex);

    public abstract boolean isDebugOn();
    public abstract boolean isInfoOn();
    public abstract boolean isErrorOn();
    
    public static void setMDC(String key, String value) {
        LoggerImpl.setMDCInt(key, value);
    }
    
    // ---------------------------------------
}
