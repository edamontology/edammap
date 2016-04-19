package edammapper.query;

import java.util.Set;

import edammapper.edam.EdamUri;

public class QuerySEQwikiTags extends Query {

	private final SEQwikiBranch branch;

	public QuerySEQwikiTags(String query, String url, Set<EdamUri> matches, SEQwikiBranch branch) {
		super(query, url, matches);
		this.branch = branch;
	}

	public QuerySEQwikiTags(String query, String url, SEQwikiBranch branch) {
		this(query, url, null, branch);
	}

	public SEQwikiBranch getBranch() {
		return branch;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (!(obj instanceof QuerySEQwikiTags)) return false;
		QuerySEQwikiTags other = (QuerySEQwikiTags)obj;
		if (branch != other.branch) return false;
		return other.canEqual(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((branch == null) ? 0 : branch.hashCode());
		return result;
	}

	@Override
	public boolean canEqual(Object other) {
		return (other instanceof QuerySEQwikiTags);
	}
}
