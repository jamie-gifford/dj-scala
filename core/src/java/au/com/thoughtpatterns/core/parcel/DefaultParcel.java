package au.com.thoughtpatterns.core.parcel;

import au.com.thoughtpatterns.core.bo.Box;
import au.com.thoughtpatterns.core.bo.IssueBox;
import au.com.thoughtpatterns.core.util.BusinessException;


public class DefaultParcel implements Parcel {

    private static final long serialVersionUID = 1L;

    private transient Box box;
    
    private IssueBox issueBox;
    
    public Box getBox() {
        return box;
    }

    public IssueBox getIssueBox() {
        return issueBox;
    }
    
    public void setBox(Box aBox) {
        box = aBox;
        if (box != null) {
            issueBox = box.getIssueBox();
        }
    }

    public boolean hasReturnValue() {
        return false;
    }
    
    public void execute() throws BusinessException {
    }

    public void doSideEffects() {
    }
    
}
