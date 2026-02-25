package au.com.thoughtpatterns.core.parcel;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import au.com.thoughtpatterns.core.bo.BOKeyImpl;
import au.com.thoughtpatterns.core.bo.Box;
import au.com.thoughtpatterns.core.bo.CachingBox;
import au.com.thoughtpatterns.core.bo.DefaultIssueFilter;
import au.com.thoughtpatterns.core.bo.Issue;
import au.com.thoughtpatterns.core.bo.IssueBox;
import au.com.thoughtpatterns.core.bo.IssueBoxException;
import au.com.thoughtpatterns.core.bo.PersistentObject;
import au.com.thoughtpatterns.core.container.ContainedException;
import au.com.thoughtpatterns.core.container.Container;
import au.com.thoughtpatterns.core.container.ContainerFactory;
import au.com.thoughtpatterns.core.util.BusinessDate;
import au.com.thoughtpatterns.core.util.BusinessException;
import au.com.thoughtpatterns.core.util.Factory;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.SystemException;

/**
 * Contains methods for executing parcels
 */
public class Checkout {
    
    private static final Logger log = Logger.get(Checkout.class);
    
    private PersistentObject auditRecord;
    
    private BusinessDate today;
    
    /**
     * Controls whether side effects are performed by the execute operation.
     */
    private boolean doSideEffects = false;
    
    public void setAudit(PersistentObject aAuditRecord) {
        auditRecord = aAuditRecord;
    }
    
    public void setToday(BusinessDate aDate) {
        today = aDate;
    }
    
    /**
     * Configure whether the checkout will perform side effects or not.
     * By default this is false - applications must explicitly enable side effects
     * @param flag
     */
    public void setDoSideEffects(boolean flag) {
        doSideEffects = flag;
    }
    
    /**
     * Executes a single parcel.
     */
    public Parcel execute(Parcel input) throws BusinessException {
        Container container = ContainerFactory.createContainer();
        
        log.info("Executing " + input);
        
        ParcelRunner r = new ParcelRunner(auditRecord, input, today, doSideEffects);
        try {
            
            Method method = ParcelRunner.class.getMethod("execute", new Class[0]);
            Parcel copy = (Parcel) container.runTransaction(method, r);
            
            return copy;
            
        } catch (NoSuchMethodException impossible) {
            // Impossible
            throw new SystemException(impossible);
            
        } catch (ContainedException ex) {
            Throwable cause = ex.getCause();
            
            if (cause instanceof BusinessException) {
                throw (BusinessException) cause;
            }
            
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            
            // Yikes - can't propagate cause!
            throw new SystemException("Unexpected exception", ex);
        } 
    }

    private static class ParcelRunner implements Serializable {

        private static final long serialVersionUID = 1L;
        private PersistentObject auditRecord;
        private Parcel parcel;
        private BusinessDate today;
        private boolean doSideEffects;
        
        ParcelRunner(PersistentObject aAuditRecord, Parcel aParcel, BusinessDate aDate, boolean aDoSideEffects) {
            auditRecord = aAuditRecord;
            parcel = aParcel;
            today = aDate;
            doSideEffects = aDoSideEffects;
        }
        
        public Parcel execute() throws BusinessException {
            
            CachingBox box = createBox();
            if (today != null) {
                box.setToday(today);
            }
            
            if (auditRecord != null) {
                box.add(auditRecord);
                box.flush();
                
                BOKeyImpl key = (BOKeyImpl) auditRecord.getBOKey();
                Long id = key.getId();
                box.setActivityId(id);
                
                log.info("Using txn " + id);
            }
            
            parcel.setBox(box);
            parcel.execute();
            box.flush();

            checkForErrors(box.getIssueBox());
            
            // Also check for "commit" level issues
            checkForErrors(box.getIssueBox().getCommitIssues());
            
            // Do side effects
            if (doSideEffects) {
                parcel.doSideEffects();
            }

            // Can be errors in side effects, too
            checkForErrors(box.getIssueBox());
            checkForErrors(box.getIssueBox().getCommitIssues());

            Parcel returnParcel = null;
            if (parcel.hasReturnValue() || parcel.getIssueBox().hasIssues() || parcel.getIssueBox().getCommitIssues().hasIssues()) {
                returnParcel = parcel;
                returnParcel.setBox(null);
            }
            
            return returnParcel;
        }        

        private void checkForErrors(IssueBox issueBox) throws IssueBoxException {
            // Are there any errors in the IssueBox?
            DefaultIssueFilter filter = new DefaultIssueFilter();
            filter.setLevel(Issue.Level.ERROR);
            List<Issue> issues = issueBox.getIssues(filter);
            if (issues.size() > 0) {
                throw new IssueBoxException(issueBox);
            }
        }
        
        private CachingBox createBox() {
            CachingBox box = (CachingBox) Factory.create(Checkout.class.getName() + ".boxImpl");
            return box;
        }
    }    
}
