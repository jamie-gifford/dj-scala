package au.com.thoughtpatterns.core.bo;

import java.util.ArrayList;
import java.util.List;

import au.com.thoughtpatterns.core.bo.Issue.Level;
import au.com.thoughtpatterns.core.util.BusinessException;

public abstract class AbstractIssueBox implements IssueBox {

    private static final long serialVersionUID = 1L;
    
    private List<Issue> issues = new ArrayList<Issue>();
    
    /**
     * If not null, represents issues "in progress".
     */
    private IssueBoxProgress progress = null;
    
    public void add(Issue aIssue) {
        if (issues.contains(aIssue)) {
            // Don't allow duplicates by default
            return;
        }
        issues.add(aIssue);
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public List<Issue> getIssues(IssueFilter aFilter) {
        if (aFilter == null) {
            return getIssues();
        }
        
        List<Issue> filtered = new ArrayList<Issue>();
        for (Issue issue : issues) {
            if (aFilter.filter(issue)) {
                filtered.add(issue);
            }
        }
        return filtered;
    }

    public boolean hasIssues() {
        return issues.size() > 0;
    }
    
    public void clear() {
        issues.clear();
        progress = null;
        IssueBox ci = getCommitIssues();
        if (ci != this) {
        	ci.clear();
        }
    }

    public String getIssueSummary() {
        StringBuffer summary = new StringBuffer();
        int i = 0;
        for (Issue issue : issues) {
            i ++;
            summary.append(i + ": ").append(issue.getDescription()).append("\n");
        }
        return summary.toString();
    }

	@Override
	public boolean inProgress() {
		return progress != null;
	}

	@Override
	public void updateProgress() {
		if (progress != null) {
			progress.update(this);
		}
	}

	public void setProgress(IssueBoxProgress aProgress) {
		progress = aProgress;
	}

    @Override
    public void checkForErrors() throws BusinessException {

        List<Issue> errors = getIssues(new IssueFilter() {
            
            @Override
            public boolean filter(Issue aIssue) {
                return aIssue.getLevel().compareTo(Level.ERROR) >= 0;
            }
            
        });
        
        if (errors.size() > 0) {
            throw new IssueBoxException(this);
        }
    }

	@Override
	public abstract IssueBox getCommitIssues();

}
