package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;

/**
 * Factory interface for creating boxes.
 */
public interface BoxFactory extends Serializable {

    Box createBox();
    
}
