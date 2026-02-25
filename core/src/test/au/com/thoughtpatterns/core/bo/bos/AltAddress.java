package au.com.thoughtpatterns.core.bo.bos;

import au.com.thoughtpatterns.core.bo.DependentObject;
import au.com.thoughtpatterns.core.bo.PersistentField;
import au.com.thoughtpatterns.core.util.Generated;
import au.com.thoughtpatterns.core.util.Util;


public class AltAddress extends DependentObject {

    private static final long serialVersionUID = 1L;

    @PersistentField
    private String streetNumber;
    
    @PersistentField
    private String streetName;
    
    @PersistentField
    private String country;

    @Generated
    public String getStreetNumber() {
        return streetNumber;
    }

    @Generated
    public void setStreetNumber(String streetNumber) {
        if (Util.equals(this.streetNumber, streetNumber)) {
            return;
        }
        this.streetNumber = streetNumber;
        fireChanged();
    }

    @Generated
    public String getStreetName() {
        return streetName;
    }

    @Generated
    public void setStreetName(String streetName) {
        if (Util.equals(this.streetName, streetName)) {
            return;
        }
        this.streetName = streetName;
        fireChanged();
    }

    @Generated
    public String getCountry() {
        return country;
    }

    @Generated
    public void setCountry(String country) {
        if (Util.equals(this.country, country)) {
            return;
        }
        this.country = country;
        fireChanged();
    }

}
