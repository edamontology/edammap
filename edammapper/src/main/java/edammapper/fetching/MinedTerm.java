package edammapper.fetching;

import java.io.Serializable;
import java.util.List;

public class MinedTerm implements Serializable {

	private static final long serialVersionUID = 4674690901850332360L;

	private String term = null;

	private int count = 0;

	private List<String> altNames = null;

	private String dbName = null;

	private List<String> dbIds = null;

	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}

	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}

	public List<String> getAltNames() {
		return altNames;
	}
	public void setAltNames(List<String> altNames) {
		this.altNames = altNames;
	}

	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public List<String> getDbIds() {
		return dbIds;
	}
	public void setDbIds(List<String> dbIds) {
		this.dbIds = dbIds;
	}

	public double getFrequency(int fulltextWordCount) {
		double frequency = count;
		if (fulltextWordCount > 0) {
			frequency /= (double)fulltextWordCount;
		} else {
			frequency = 0;
		}
		if (frequency < 0) {
			frequency = 0;
		} else if (frequency > 1) {
			frequency = 1;
		}
		return frequency;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (term != null) {
			builder.append(term);
		}
		if (altNames != null && !altNames.isEmpty()) {
			builder.append(" ");
			builder.append(altNames);
		}
		return builder.toString();
	}
}
