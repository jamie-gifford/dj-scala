package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;
import java.util.List;

import au.com.thoughtpatterns.core.util.BusinessException;

/**
 * Contains a set of Issues. These are errors, informations and/or warnings related to a potential
 * business transaction.
 * 
 * <p>
 * There is also a "secondary" ("commit") issue box in which issues related to actually 
 * committing the transaction. Issues in the commit issue box are not issues relating directly to the 
 * business transaction but are ancillary issues - eg, that a SecurePay payment needs to be made before the
 * transaction can go ahead.
 */
public interface IssueBox extends Serializable {

    public void add(Issue aIssue);
    
    public boolean hasIssues();
    
    /**
     * Get the list of issues in this issue box. This is a modifiable list - it is acceptable
     * to remove issues from the list.
     * @return
     */
    public List<Issue> getIssues();
    
    /**
     * Get a filtered list of issues. This is a non-modifiable list.
     * @param aFilter
     * @return
     */
    public List<Issue> getIssues(IssueFilter aFilter);
    
    public void clear();
    
    /**
     * Produce a summary of the errors. This is for debugging and junit work
     * more than for use in the UI (which would work with the individual 
     * issues in the issue box).
     * @return
     */
    public String getIssueSummary();
    
    /**
     * @return true if the issues are "in progress" (ie, asynchronous operation)
     */
    public boolean inProgress();
    
    /**
     * For asynchronous processing, update contents with new issue state. 
     */
    public void updateProgress();
    
    /**
     * Retrieve the "commit" issue box
     */
    public IssueBox getCommitIssues();
    
    /**
     * If there are errors in the issue box, throws an exception
     * @throws BusinessException
     */
    public void checkForErrors() throws BusinessException;
    
}
