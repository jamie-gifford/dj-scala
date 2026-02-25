package au.com.thoughtpatterns.core.bo;

public class UnusedIssueFilter implements IssueFilter {

    public boolean filter(Issue aIssue) {
        return !aIssue.isUsed();
    }

}
