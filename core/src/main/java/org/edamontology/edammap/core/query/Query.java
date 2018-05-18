/*
 * Copyright © 2016, 2018 Erik Jaaniso
 *
 * This file is part of EDAMmap.
 *
 * EDAMmap is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EDAMmap is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EDAMmap.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.edamontology.edammap.core.query;

import java.util.List;
import java.util.Set;

import org.edamontology.edammap.core.edam.EdamUri;

public class Query {

	public static final String ID = "id";
	private final String id;

	public static final String NAME = "name";
	private final String name;

	public static final String KEYWORDS = "keywords";
	private final List<Keyword> keywords;

	public static final String DESCRIPTION = "description";
	private final String description;

	public static final String WEBPAGE_URLS = "webpageUrls";
	private final List<Link> webpageUrls;

	public static final String DOC_URLS = "docUrls";
	private final List<Link> docUrls;

	public static final String PUBLICATION_IDS = "publicationIds";
	private final List<PublicationIdsQuery> publicationIds;

	public static final String ANNOTATIONS = "annotations";
	private final Set<EdamUri> annotations;

	public Query(String id, String name, List<Keyword> keywords, String description,
			List<Link> webpageUrls, List<Link> docUrls, List<PublicationIdsQuery> publicationIds, Set<EdamUri> annotations) {
		this.id = id;
		this.name = name;
		this.keywords = keywords;
		this.description = description;
		this.webpageUrls = webpageUrls;
		this.docUrls = docUrls;
		this.publicationIds = publicationIds;
		this.annotations = annotations;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<Keyword> getKeywords() {
		return keywords;
	}

	public String getDescription() {
		return description;
	}

	public List<Link> getWebpageUrls() {
		return webpageUrls;
	}

	public List<Link> getDocUrls() {
		return docUrls;
	}

	public List<PublicationIdsQuery> getPublicationIds() {
		return publicationIds;
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
		if (id == null) {
			if (other.id != null) return false;
		} else if (!id.equals(other.id)) return false;
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
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
