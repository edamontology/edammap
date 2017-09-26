package edammapper.query;

public class PublicationIds {

	private String pmid;

	private String pmcid;

	private String doi;

	private String type;

	public PublicationIds(String pmid, String pmcid, String doi, String type) {
		this.pmid = pmid;
		this.pmcid = pmcid;
		this.doi = doi;
		this.type = type;
	}

	public String getPmid() {
		return pmid;
	}
	public void setPmid(String pmid) {
		this.pmid = pmid;
	}

	public String getPmcid() {
		return pmcid;
	}
	public void setPmcid(String pmcid) {
		this.pmcid = pmcid;
	}

	public String getDoi() {
		return doi;
	}
	public void setDoi(String doi) {
		this.doi = doi;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof PublicationIds)) return false;
		PublicationIds other = (PublicationIds) obj;
		if (pmid == null) {
			if (other.pmid != null) return false;
		} else if (!pmid.equals(other.pmid)) return false;
		if (pmcid == null) {
			if (other.pmcid != null) return false;
		} else if (!pmcid.equals(other.pmcid)) return false;
		if (doi == null) {
			if (other.doi != null) return false;
		} else if (!doi.equals(other.doi)) return false;
		if (type == null) {
			if (other.type != null) return false;
		} else if (!type.equals(other.type)) return false;
		return other.canEqual(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pmid == null) ? 0 : pmid.hashCode());
		result = prime * result + ((pmcid == null) ? 0 : pmcid.hashCode());
		result = prime * result + ((doi == null) ? 0 : doi.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	public boolean canEqual(Object other) {
		return (other instanceof PublicationIds);
	}

	@Override
	public String toString() {
		String s = "";
		if (pmid != null && !pmid.isEmpty()) {
			s += pmid; 
		}
		if (pmcid != null && !pmcid.isEmpty()) {
			if (!s.isEmpty()) s += ", ";
			s += pmcid;
		}
		if (doi != null && !doi.isEmpty()) {
			if (!s.isEmpty()) s += ", ";
			s += doi;
		}
		return "[" + s + "]";
	}
}
