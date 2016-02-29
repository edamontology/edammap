package mapper.core;

public class EdamUri {

	private String uri;

	private BranchType branch;

	private int nr;

	public EdamUri(String uri) {
		if (uri == null || uri.isEmpty()) {
			throw new NullPointerException("EDAM URI can't be null or empty");
		}
		this.uri = uri;
		int slash = uri.lastIndexOf('/');
		int underscore = uri.lastIndexOf('_');
		if (slash >= 0 && slash < underscore) {
			branch = BranchType.valueOf(uri.substring(slash + 1, underscore));
			nr = Integer.parseInt(uri.substring(underscore + 1, uri.length()));
		} else {
			throw new IllegalArgumentException("Illegal EDAM URI");
		}
	}

	public String getUri() {
		return uri;
	}

	public BranchType getBranch() {
		return branch;
	}

	public int getNr() {
		return nr;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof EdamUri)) return false;
		EdamUri other = (EdamUri)obj;
		if (uri == null && other.uri == null) return true;
		return (uri != null && uri.equals(other.uri));
	}

	@Override
	public int hashCode() {
		if (uri == null) return 0;
		return uri.hashCode();
	}

	@Override
	public String toString() {
		if (uri == null) return "";
		return uri;
	}
}
