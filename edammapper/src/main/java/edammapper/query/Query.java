package edammapper.query;

import java.util.Set;

import edammapper.edam.EdamUri;

public class Query {

	private final String query;

	private final String url;

	private final Set<EdamUri> matches;

	public Query(String query, String url, Set<EdamUri> matches) {
		this.query = query;
		this.url = url;
		this.matches = matches;
	}

	public String getQuery() {
		return query;
	}

	public String getUrl() {
		return url;
	}

	public Set<EdamUri> getMatches() {
		return matches;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Query)) return false;
		Query other = (Query)obj;
		if (query == null) {
			if (other.query != null) return false;
		} else if (!query.equals(other.query)) return false;
		if (url == null) { 
			if (other.url != null) return false;
		} else if (!url.equals(other.url)) return false;
		if (matches == null) {
			if (other.matches != null) return false;
		} else if (!matches.equals(other.matches)) return false;
		return other.canEqual(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((query == null) ? 0 : query.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((matches == null) ? 0 : matches.hashCode());
		return result;
	}

	public boolean canEqual(Object other) {
		return (other instanceof Query);
	}
}
