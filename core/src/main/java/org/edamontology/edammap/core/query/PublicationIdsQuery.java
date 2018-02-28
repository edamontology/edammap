/*
 * Copyright © 2017 Erik Jaaniso
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

import org.edamontology.pubfetcher.PublicationIds;

public class PublicationIdsQuery extends PublicationIds {

	private static final long serialVersionUID = 1L;

	private final String type;

	public PublicationIdsQuery(String pmid, String pmcid, String doi, String pmidUrl, String pmcidUrl, String doiUrl, String type) {
		super(pmid, pmcid, doi, pmidUrl, pmcidUrl, doiUrl);
		this.type = type;
	}

	public String getType() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof PublicationIdsQuery)) return false;
		PublicationIdsQuery other = (PublicationIdsQuery) obj;
		if (!super.equals(other)) return false;
		if (type == null) {
			if (other.type != null) return false;
		} else if (!type.equals(other.type)) return false;
		return other.canEqual(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean canEqual(Object other) {
		return (other instanceof PublicationIdsQuery);
	}
}
