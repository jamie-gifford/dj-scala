package au.com.thoughtpatterns.core.bo;

/**
 * Represents an object that can be identified (roughly) by a String "localizer".
 * This is intended for matching field-level errors generated from objects in 
 * "server code" against corresponding objects in the "presentation tier".
 * 
 * The point is that we can't normally use object identity to identify the object
 * with the error beccause objects get copied during serialization from one tier to 
 * the next. The "localizer" interface allows a loosely coupled object identification
 * across tiers.
 * 
 * Objects that don't have an unambiguous string representation can leave their localizer
 * as null. This will typically result in slightly degraded behaviour in the client - eg, 
 * an error message might appear but no error marker against the relevant field.
 */
public interface Localizable {

    /**
     * Get the "localizer" of this BusinessObject instance.
     * A localizer is not persistent, but is preserved in 
     * "client-server rounds trips", and hence can be used 
     * to correlate Issues (errors) with business objects.
     * 
     * The localizer will normally be null, but can be set by 
     * clients before dispatching Parcels to the server for 
     * processing.
     */
    public String getLocalizer();
    
    public void setLocalizer(String aLocalizer);

    
}
