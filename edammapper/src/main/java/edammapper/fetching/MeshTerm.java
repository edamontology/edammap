package edammapper.fetching;

import java.io.Serializable;

public class MeshTerm implements Serializable {

	private static final long serialVersionUID = 5663855704880035618L;

	private String term = null;

	private boolean majorTopic = false;

	private String uniqueId = null;

	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}

	public boolean isMajorTopic() {
		return majorTopic;
	}
	public void setMajorTopic(boolean majorTopic) {
		this.majorTopic = majorTopic;
	}

	public String getUniqueId() {
		return uniqueId;
	}
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (term != null) {
			builder.append(term);
		}
		return builder.toString();
	}
}
