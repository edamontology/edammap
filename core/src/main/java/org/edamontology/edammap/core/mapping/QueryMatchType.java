/*
 * Copyright © 2016 Erik Jaaniso
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

package org.edamontology.edammap.core.mapping;

public enum QueryMatchType {
	name,
	keyword,
	description,
	publication_title("publication title"),
	publication_keyword("publication keyword"),
	publication_mesh("publication mesh"),
	publication_efo("publication efo"),
	publication_go("publication go"),
	publication_abstract("publication abstract"),
	publication_fulltext("publication fulltext"),
	doc,
	webpage,
	none;

	private String type;

	private QueryMatchType() {
		this.type = name();
	}
	private QueryMatchType(String type) {
		this.type = type;
	}

	public boolean isPublication() {
		return this == publication_title
			|| this == publication_keyword || this == publication_mesh || this == publication_efo || this == publication_go
			|| this == publication_abstract || this == publication_fulltext;
	}

	@Override
	public String toString() {
		return type;
	}
}
