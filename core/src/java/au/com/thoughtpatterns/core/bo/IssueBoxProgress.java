package au.com.thoughtpatterns.core.bo;

import java.io.Serializable;

public interface IssueBoxProgress extends Serializable {

	/**
	 * Update the given issue box with the current "issues".
	 */
	void update(AbstractIssueBox aIssues);
	
}
