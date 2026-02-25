package au.com.thoughtpatterns.core.parcel;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents a "business function" (for linking with documentation)
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Function {

    /**
     * The formal specification name
     */
    String functionName() default "";

    /**
     * A name suitable for display to an end user. May be null, in which case the function name 
     * may be used as a display name.
     */
    String displayName() default "";
    
}
