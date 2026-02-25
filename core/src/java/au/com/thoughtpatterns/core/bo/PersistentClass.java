package au.com.thoughtpatterns.core.bo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a persistent class
 * @see PersistentField
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PersistentClass {

    String table() default "";
    
}
