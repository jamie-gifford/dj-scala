package au.com.thoughtpatterns.core.util;

/**
 * Runnable ("main") class for testing runtime infrastructure
 */
public class Ping {

    public static void main(String[] args) {
        
        Logger log = Logger.get(Ping.class);
        log.info("Ping");
        
    }
    
}
