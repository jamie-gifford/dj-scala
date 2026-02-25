package au.com.thoughtpatterns.core.bo.bos;

import java.util.List;

import au.com.thoughtpatterns.core.bo.DependentList;
import au.com.thoughtpatterns.core.bo.PersistentField;
import au.com.thoughtpatterns.core.bo.PersistentObject;
import au.com.thoughtpatterns.core.util.Generated;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.Util;

/**
 * An alternative Person BO which uses a dependent collection of
 * Addresses instead of a One-to-many
 * 
 */
public class AltPerson extends PersistentObject { // implements BeanInfo {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.get(AltPerson.class);
    
    @PersistentField
    private String givenName;

    @PersistentField
    private String familyName;
    
    private DependentList<AltAddress> addresses = new DependentList<AltAddress>(this, AltAddress.class);
    
    @Generated
    public String getGivenName() {
        return givenName;
    }

    @Generated
    public void setGivenName(String givenName) {
        if (Util.equals(this.givenName, givenName)) {
            return;
        }
        this.givenName = givenName;
        fireChanged();
    }

    @Generated
    public String getFamilyName() {
        return familyName;
    }

    @Generated
    public void setFamilyName(String familyName) {
        if (Util.equals(this.familyName, familyName)) {
            return;
        }
        this.familyName = familyName;
        fireChanged();
    }

    public List<AltAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AltAddress> aAddresses) {
        addresses.set(aAddresses);
    }
        
}
