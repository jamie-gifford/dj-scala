package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;
import java.lang.reflect.Field;

import au.com.thoughtpatterns.core.util.Util;

/**
 * An Issue represents an error, warning or message. Issues can be generated
 * during processing business operations (by managers or parcels). They can be 
 * recorded in an {@link IssueBox}, using the {@link Box#getIssueBox} method to 
 * obtain the IssueBox from the Box.
 * 
 * An issue contains a (String) message. It also contains optional propertyName
 * and localizer properties. These are used to associate the Issue with a
 * particular BusinessObject and property within the BusinessObject.
 */
public class Issue implements Serializable {

    private static final long serialVersionUID = 1L;

    public String message;
    public String propertyName;
    public String localizer;
    public Level level;
    private boolean isUsed;
    
    public enum Level {
        INFORMATION,
        WARNING,
        ERROR
    }
    
    public Issue(String aMessage, String aLocalizer, String aPropertyName, Level aLevel) {
        message = aMessage;
        propertyName = aPropertyName;
        localizer = aLocalizer;
        level = ( aLevel != null ? aLevel : Level.ERROR );
    }
    
    public static Issue error(String aMessage, BusinessObject aObject, String aPropertyName) {
    	if (aObject == null) {
            return new Issue(aMessage, null, aPropertyName, Level.ERROR);
    		
    	} else {
            return new Issue(aMessage, aObject.getLocalizer(), aPropertyName, Level.ERROR);
    	}
    }
    
    public static Issue info(String message) {
        return new Issue(message, null, null, Level.INFORMATION);
    }

    public static Issue warn(String message) {
        return new Issue(message, null, null, Level.WARNING);
    }

    /**
     * Create a "mandatory field" error message
     * @param aFieldName the (human readable) field name (could also be an entire object, eg Residential Address. This will be incorporated into the error message
     * @param aObject the business object containing the field
     * @param aPropertyName the javabeans property name
     */
    public static Issue mandatory(String aFieldName, BusinessObject aObject, String aPropertyName) {
        return error(getMandatoryError(aFieldName), aObject, aPropertyName);
    }

    public static String getMandatoryError(String aFieldName) {
        return "A value for " + aFieldName + " is required";
    }
    
    public static Issue maxLength(Field aField, BusinessObject aObject, int maxLength) {
        String name = MetadataUtil.getDisplayName(aField);
        return error("Maximum length for field '" + name + "' is " + maxLength + " characters", aObject, aField.getName());
    }
    
    public String getLocalizer() {
        return localizer;
    }
    
    public void setLocalizer(String localizer) {
        this.localizer = localizer;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getPropertyName() {
        return propertyName;
    }
    
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Level getLevel() {
        return level;
    }
    
    public void setLevel(Level level) {
        this.level = level;
    }

    public boolean isUsed() {
        return isUsed;
    }
    
    public void setUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }

    public String toString() {
        return "Issue[" + message + "][" + localizer + "][" + propertyName + "][" + level + "]";
    }
    
    /**
     * Get a description of this issue. The description will include the 
     * localizer (if defined) and the message (if defined)
     */
    public String getDescription() {
        StringBuffer out = new StringBuffer();
        if (localizer != null) {
            out.append(localizer).append(": ");
        }
        out.append(message);
        return out.toString();
    }
    
    /*
     *     public String message;
    public String propertyName;
    public String localizer;
    public Level level;
    private boolean isUsed;

     */

    public boolean equals(Object other) {
        if (! (other instanceof Issue)) {
            return false;
        }
        Issue i = (Issue) other;
        return Util.equals(message, i.message) &&
            Util.equals(propertyName, i.propertyName) &&
            Util.equals(localizer, i.localizer) &&
            Util.equals(level, i.level);
    }
    
    public int hashCode() {
        return ( message != null ? message.hashCode() : 1 );
    }
    
}
