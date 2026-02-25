package au.com.thoughtpatterns.core.bo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks an instance variable in a BusinessObject as persistent.
 * 
 * The code generation tools detect this annotation and create 
 * suitable getters/setters.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PersistentField {

    String column() default "";
    String glossaryName() default "";
    String displayName() default "";
    int maxLength() default 0;
    
}
