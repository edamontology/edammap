package mapper.core;

import java.util.LinkedHashSet;
import java.util.Set;

public class Keyword {

	private String keyword;

	private String url;

	private Set<EdamUri> matches = new LinkedHashSet<>();

	private Set<String> parents = new LinkedHashSet<>();

	public Keyword(String keyword, String url, String match, String parent) {
		this.keyword = keyword;
		this.url = url;
		if (match != null && !match.isEmpty()) {
			this.matches.add(new EdamUri(match));
		}
		if (parent != null && !parent.isEmpty()) {
			this.parents.add(parent);
		}
	}

	public String getKeyword() {
		return keyword;
	}

	public String getUrl() {
		return url;
	}

	public Set<EdamUri> getMatches() {
		return matches;
	}

	public Set<String> getParents() {
		return parents;
	}

	public Keyword merge(Keyword keyword) {
		if (keyword == null) return this;
		matches.addAll(keyword.matches);
		parents.addAll(keyword.parents);
		return this;
	}
}
