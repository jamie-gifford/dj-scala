package au.com.thoughtpatterns.core.fn;

import java.io.Serializable;

public interface Ufn<T, R> extends Serializable {

    Fn<R> apply(T in);
    
}
