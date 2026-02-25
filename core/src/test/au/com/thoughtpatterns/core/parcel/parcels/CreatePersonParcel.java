package au.com.thoughtpatterns.core.parcel.parcels;

import au.com.thoughtpatterns.core.bo.BOKey;
import au.com.thoughtpatterns.core.bo.bos.Person;
import au.com.thoughtpatterns.core.bo.bos.PersonData;
import au.com.thoughtpatterns.core.parcel.DefaultParcel;
import au.com.thoughtpatterns.core.util.BusinessException;


public class CreatePersonParcel extends DefaultParcel {

    // -----------------------------
    // Input data
    
    private static final long serialVersionUID = 1L;

    private PersonData personData;
    
    // -----------------------------
    // Output data
    
    private BOKey addedKey;
    
    // -----------------------------
    
    public CreatePersonParcel(PersonData aPersonData) {
        personData = aPersonData;
    }
    
    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public void execute() throws BusinessException {

        // Create a person
        Person person = new Person();
        
        // Populate person with data
        person.setGivenName(personData.getGivenName());
        person.setFamilyName(personData.getFamilyName());
        person.setLocalizer(personData.getLocalizer());

        // Check constraints
        person.validate(getBox());
        
        // Add to box
        getBox().add(person);
        
        // We want to return the BOKey of the person as state, so that
        // it's available to the caller
        addedKey = person.getBOKey();        
    }

    public BOKey getAddedKey() {
        return addedKey;
    }
}
