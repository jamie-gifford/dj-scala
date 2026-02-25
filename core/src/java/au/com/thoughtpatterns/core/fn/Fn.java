package au.com.thoughtpatterns.core.fn;

import java.io.Serializable;

/**
 * Support for functional programming idiom
 */
public interface Fn<T> extends Serializable {

    T eval();
    
}
