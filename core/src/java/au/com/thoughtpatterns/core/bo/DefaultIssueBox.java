package au.com.thoughtpatterns.core.bo;


public class DefaultIssueBox extends AbstractIssueBox {

	private static final long serialVersionUID = 1L;

	private AbstractIssueBox commitBox = new AbstractIssueBox() {

		private static final long serialVersionUID = 1L;

		@Override
		public IssueBox getCommitIssues() {
			return commitBox;
		}
		
	};
	
	@Override
	public IssueBox getCommitIssues() {
		return commitBox;
	}


}
