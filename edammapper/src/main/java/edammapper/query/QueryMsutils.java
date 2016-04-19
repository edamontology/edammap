package edammapper.query;

import java.util.Set;

import edammapper.edam.EdamUri;

public class QueryMsutils extends Query {

	private final String name;

	public QueryMsutils(String query, String url, Set<EdamUri> matches, String name) {
		super(query, url, matches);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (!(obj instanceof QueryMsutils)) return false;
		QueryMsutils other = (QueryMsutils)obj;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		return other.canEqual(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean canEqual(Object other) {
		return (other instanceof QueryMsutils);
	}
}
