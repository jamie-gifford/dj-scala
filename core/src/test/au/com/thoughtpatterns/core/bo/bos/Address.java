package au.com.thoughtpatterns.core.bo.bos;

import au.com.thoughtpatterns.core.bo.ForeignKey;
import au.com.thoughtpatterns.core.bo.PersistentClass;
import au.com.thoughtpatterns.core.bo.PersistentField;
import au.com.thoughtpatterns.core.bo.PersistentObject;
import au.com.thoughtpatterns.core.bo.Relationship;
import au.com.thoughtpatterns.core.util.Generated;
import au.com.thoughtpatterns.core.util.Util;

@PersistentClass(table="test_address")
public class Address extends PersistentObject {

    private static final long serialVersionUID = 1L;

    @PersistentField
    private String streetNumber;
    
    @PersistentField
    private String streetName;
    
    @PersistentField
    private String country;
    
    @PersistentField(column="person_id")
    private ForeignKey personRel = new ForeignKey(this);

    private Relationship<Person> personR = new Relationship<Person>(this, Person.class, personRel);

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

    public Person getPerson() {
        return personR.get();
    }
    
    public void setPerson(Person aPerson) {
        personR.set(aPerson);
    }

    
    
    // ----------------------------------------
    // Methods that are necessary only for iBatis...

    public ForeignKey getPersonRel() {
        return personRel;
    }

    
    public void setPersonRel(ForeignKey personRel) {
        this.personRel = personRel;
    }


    
}
