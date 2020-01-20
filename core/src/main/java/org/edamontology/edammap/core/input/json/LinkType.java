/*
 * Copyright © 2018 Erik Jaaniso
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

public enum LinkType {
	@JsonProperty("Discussion forum")
	DISCUSSION_FORUM("Discussion forum"),
	@JsonProperty("Galaxy service")
	GALAXY_SERVICE("Galaxy service"),
	@JsonProperty("Helpdesk")
	HELPDESK("Helpdesk"),
	@JsonProperty("Issue tracker")
	ISSUE_TRACKER("Issue tracker"),
	@JsonProperty("Mailing list")
	MAILING_LIST("Mailing list"),
	@JsonProperty("Mirror")
	MIRROR("Mirror"),
	@JsonProperty("Software catalogue")
	SOFTWARE_CATALOGUE("Software catalogue"),
	@JsonProperty("Repository")
	REPOSITORY("Repository"),
	@JsonProperty("Service")
	SERVICE("Service"),
	@JsonProperty("Social media")
	SOCIAL_MEDIA("Social media"),
	@JsonProperty("Technical monitoring")
	TECHNICAL_MONITORING("Technical monitoring"),
	@JsonProperty("Other")
	OTHER("Other"),
	// TODO remove (not in schema)
	@JsonProperty("Browser")
	BROWSER("Browser");

	private String type;

	private LinkType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}
}
