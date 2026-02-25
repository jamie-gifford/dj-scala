package au.com.thoughtpatterns.core.json;

public class JsonyException extends RuntimeException {

    public JsonyException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonyException(String message) {
        super(message);
    }
    
}
