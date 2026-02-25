package au.com.thoughtpatterns.core.bo.bos;

import java.util.List;

import au.com.thoughtpatterns.core.bo.Box;
import au.com.thoughtpatterns.core.bo.Issue;
import au.com.thoughtpatterns.core.bo.Manager;
import au.com.thoughtpatterns.core.bo.OneToMany;
import au.com.thoughtpatterns.core.bo.Query;
import au.com.thoughtpatterns.core.bo.QueryFactory;
import au.com.thoughtpatterns.core.bo.SimpleOneToMany;
import au.com.thoughtpatterns.core.util.BusinessException;

public class Person extends PersonData implements Manager {

    // ---------------------------
    // State
    
    private static final long serialVersionUID = 1L;
    /**
     * A list of all related addresses
     */
    private OneToMany addresses = new SimpleOneToMany<Address>(this, Address.class, "person_id");    
    /**
     * A list of all the person's Australian addresses (ie, addresses with country = Australia
     */
    private OneToMany ausAddresses = new OneToMany<Address>(this, Address.class) {

        private static final long serialVersionUID = 1L;

        @Override
        protected boolean belongs(Address candidate) {
            return candidate.getPerson() == Person.this && 
                "Australia".equals(candidate.getCountry());
        }

        @Override
        protected Query query() {
            Query q = QueryFactory.create("select id from test_address where person_id = ? and country = ?");
            q.setNextValue(Person.this);
            q.setNextValue("Australia");
            return q;
        }
        
    };

    // ---------------------------
    // Getters, setters
    
    public List<Address> getAddresses() {
        return addresses.get();
    }

    public List<Address> getAusAddresses() {
        return ausAddresses.get();
    }

    // -------------------------------------
    // Business methods
    
    // TODO consider vaidation framework. It may be better not to use exceptions
    public void validate(Box aBox) throws BusinessException {
        if (getFamilyName() == null) {
            aBox.getIssueBox().add(Issue.error("Family name must be provided", this, "familyName"));
        }
    }
    
}
