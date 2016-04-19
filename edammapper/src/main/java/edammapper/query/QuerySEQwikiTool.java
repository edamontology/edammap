package edammapper.query;

import java.util.Set;

import edammapper.edam.EdamUri;

public class QuerySEQwikiTool extends QuerySEQwikiTags {

	private final String name;

	public QuerySEQwikiTool(String query, String url, Set<EdamUri> matches, SEQwikiBranch branch, String name) {
		super(query, url, matches, branch);
		this.name = name;
	}

	public QuerySEQwikiTool(String query, String url, SEQwikiBranch branch, String name) {
		this(query, url, null, branch, name);
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (!(obj instanceof QuerySEQwikiTool)) return false;
		QuerySEQwikiTool other = (QuerySEQwikiTool)obj;
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
		return (other instanceof QuerySEQwikiTool);
	}
}
