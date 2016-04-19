package edammapper.query;

import java.util.Set;

import edammapper.edam.EdamUri;

public class QuerySEQwiki extends Query {

	private final String name;

	private final Set<String> domains;

	private final Set<String> methods;

	public QuerySEQwiki(String query, String url, Set<EdamUri> matches, String name, Set<String> domains, Set<String> methods) {
		super(query, url, matches);
		this.name = name;
		this.domains = domains;
		this.methods = methods;
	}

	public QuerySEQwiki(String query, String url, String name, Set<String> domains, Set<String> methods) {
		this(query, url, null, name, domains, methods);
	}

	public String getName() {
		return name;
	}

	public Set<String> getDomains() {
		return domains;
	}

	public Set<String> getMethods() {
		return methods;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((domains == null) ? 0 : domains.hashCode());
		result = prime * result + ((methods == null) ? 0 : methods.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (!(obj instanceof QuerySEQwiki)) return false;
		QuerySEQwiki other = (QuerySEQwiki)obj;
		if (domains == null) {
			if (other.domains != null) return false;
		} else if (!domains.equals(other.domains)) return false;
		if (methods == null) {
			if (other.methods != null) return false;
		} else if (!methods.equals(other.methods)) return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		return other.canEqual(this);
	}

	@Override
	public boolean canEqual(Object other) {
		return (other instanceof QuerySEQwiki);
	}
}
