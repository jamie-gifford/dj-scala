package au.com.thoughtpatterns.core.bo;

import au.com.thoughtpatterns.core.util.BusinessException;

/**
 * A BusinessException that represents issues in an IssueBox
 */
public class IssueBoxException extends BusinessException {

    private static final long serialVersionUID = 1L;
    private IssueBox issueBox;
    
    public IssueBoxException(IssueBox aIssueBox) {
        this(aIssueBox.getIssueSummary(), aIssueBox);
    }
    
    public IssueBoxException(String message, IssueBox aIssueBox) {
        super(message);
        issueBox = aIssueBox;
    }
    
    public IssueBox getIssueBox() {
        return issueBox;
    }
    
}
