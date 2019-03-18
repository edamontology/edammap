/*
 * Copyright © 2019 Erik Jaaniso
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

package org.edamontology.edammap.core.input.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RoleType {
	@JsonProperty("Primary contact")
	PRIMARY_CONTACT("Primary contact"),
	@JsonProperty("Contributor")
	CONTRIBUTOR("Contributor"),
	@JsonProperty("Developer")
	DEVELOPER("Developer"),
	@JsonProperty("Documentor")
	DOCUMENTOR("Documentor"),
	@JsonProperty("Maintainer")
	MAINTAINER("Maintainer"),
	@JsonProperty("Provider")
	PROVIDER("Provider"),
	@JsonProperty("Support")
	SUPPORT("Support");

	private String type;

	private RoleType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}
}
