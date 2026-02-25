package au.com.thoughtpatterns.core.bo.bos;

import au.com.thoughtpatterns.core.bo.PersistentClass;
import au.com.thoughtpatterns.core.bo.PersistentField;
import au.com.thoughtpatterns.core.bo.PersistentObject;
import au.com.thoughtpatterns.core.util.Generated;
import au.com.thoughtpatterns.core.util.Util;

@PersistentClass(table="test_person")
public class PersonData extends PersistentObject {

    // ---------------------------
    // State
    
    private static final long serialVersionUID = 1L;

    @PersistentField(column="given_name", maxLength=20)
    private String givenName;

    @PersistentField(column="family_name", maxLength=20)
    private String familyName;

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

}
