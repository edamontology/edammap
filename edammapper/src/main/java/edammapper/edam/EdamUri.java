package edammapper.edam;

public class EdamUri {

	private String uri;

	private Branch branch;

	private int nr;

	public EdamUri(String uri, String prefix) {
		if (!isEdamUri(uri, prefix)) {
			throw new IllegalArgumentException("Illegal EDAM URI: " + uri);
		}

		this.uri = uri;

		String[] branch_nr = uri.substring(prefix.length() + 1).split("_", 2);
		branch = Branch.valueOf(branch_nr[0]);
		nr = Integer.parseInt(branch_nr[1]);
	}

	public static boolean isEdamUri(String uri, String prefix) {
		if (uri == null || uri.isEmpty()) return false;
		if (!uri.startsWith(prefix)) return false;
		if (uri.charAt(prefix.length()) != '/') return false;

		String[] branch_nr = uri.substring(prefix.length() + 1).split("_", 2);
		try {
			Branch.valueOf(branch_nr[0]);
			int nr = Integer.parseInt(branch_nr[1]);
			if (nr < 0) return false;
		} catch (RuntimeException e) {
			return false;
		}

		return true;
	}

	public String getUri() {
		return uri;
	}

	public Branch getBranch() {
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
