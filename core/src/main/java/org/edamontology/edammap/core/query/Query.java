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

	private final String id;

	private final String name;

	private final List<Link> webpageUrls;

	private final String description;

	private final List<Keyword> keywords;

	private final List<PublicationIdsQuery> publicationIds;

	private final List<Link> docUrls;

	private final Set<EdamUri> annotations;

	public Query(String id, String name, List<Link> webpageUrls, String description, List<Keyword> keywords,
			List<PublicationIdsQuery> publicationIds, List<Link> docUrls, Set<EdamUri> annotations) {
		this.id = id;
		this.name = name;
		this.webpageUrls = webpageUrls;
		this.description = description;
		this.keywords = keywords;
		this.publicationIds = publicationIds;
		this.docUrls = docUrls;
		this.annotations = annotations;
	}

	public String getId() {
		return id;
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

	public List<PublicationIdsQuery> getPublicationIds() {
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