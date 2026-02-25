package au.com.thoughtpatterns.core.bo;

/**
 * Interface for listening to Box events
 */
public interface BoxListener {

    void added(IEntity object);
    void deleted(IEntity object);
    void loaded(IEntity object);
    
}
