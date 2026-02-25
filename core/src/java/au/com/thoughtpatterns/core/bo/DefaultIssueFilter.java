package au.com.thoughtpatterns.core.bo;

/**
 * Filters Issues based on simple comparison with (optional) localizer, 
 * propertyName and level attributes.
 */
public class DefaultIssueFilter implements IssueFilter {

    private String localizer;
    private String propertyName;
    private Issue.Level level;
    
    public Issue.Level getLevel() {
        return level;
    }
    
    public void setLevel(Issue.Level level) {
        this.level = level;
    }
    
    public String getLocalizer() {
        return localizer;
    }
    
    public void setLocalizer(String localizer) {
        this.localizer = localizer;
    }
    
    public String getPropertyName() {
        return propertyName;
    }
    
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public boolean filter(Issue aIssue) {
        if (localizer != null && ! localizer.equals(aIssue.getLocalizer())) {
            return false;
        }
        
        if (propertyName != null && ! propertyName.equals(aIssue.getPropertyName())) {
            return false;
        }
        
        if (level != null) {
            int diff = level.compareTo(aIssue.getLevel());
            if (diff > 0) {
                return false;
            }
        }
        
        return true;
    }
}
