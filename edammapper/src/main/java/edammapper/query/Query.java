package edammapper.query;

import java.util.List;
import java.util.Set;

import edammapper.edam.EdamUri;

public class Query {

	private final String name;

	private final List<Link> webpageUrls;

	private final String description;

	private final List<Keyword> keywords;

	private final List<PublicationIds> publicationIds;

	private final List<Link> docUrls;

	private final Set<EdamUri> annotations;

	public Query(String name, List<Link> webpageUrls, String description, List<Keyword> keywords,
			List<PublicationIds> publicationIds, List<Link> docUrls, Set<EdamUri> annotations) {
		this.name = name;
		this.webpageUrls = webpageUrls;
		this.description = description;
		this.keywords = keywords;
		this.publicationIds = publicationIds;
		this.docUrls = docUrls;
		this.annotations = annotations;
	}

	public String getName() {
		return name;
	}

	public List<Link> getWebpageUrls() {
		return webpageUrls;
	}

	public String getDescription() {
		return description;
	}

	public List<Keyword> getKeywords() {
		return keywords;
	}

	public List<PublicationIds> getPublicationIds() {
		return publicationIds;
	}

	public List<Link> getDocUrls() {
		return docUrls;
	}

	public Set<EdamUri> getAnnotations() {
		return annotations;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Query)) return false;
		Query other = (Query) obj;
		if (annotations == null) {
			if (other.annotations != null) return false;
		} else if (!annotations.equals(other.annotations)) return false;
		if (description == null) {
			if (other.description != null) return false;
		} else if (!description.equals(other.description)) return false;
		if (docUrls == null) {
			if (other.docUrls != null) return false;
		} else if (!docUrls.equals(other.docUrls)) return false;
		if (keywords == null) {
			if (other.keywords != null) return false;
		} else if (!keywords.equals(other.keywords)) return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (publicationIds == null) {
			if (other.publicationIds != null) return false;
		} else if (!publicationIds.equals(other.publicationIds)) return false;
		if (webpageUrls == null) {
			if (other.webpageUrls != null) return false;
		} else if (!webpageUrls.equals(other.webpageUrls)) return false;
		return other.canEqual(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotations == null) ? 0 : annotations.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((docUrls == null) ? 0 : docUrls.hashCode());
		result = prime * result + ((keywords == null) ? 0 : keywords.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((publicationIds == null) ? 0 : publicationIds.hashCode());
		result = prime * result + ((webpageUrls == null) ? 0 : webpageUrls.hashCode());
		return result;
	}

	public boolean canEqual(Object other) {
		return (other instanceof Query);
	}
}
