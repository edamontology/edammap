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
	@JsonProperty("Browser")
	BROWSER("Browser"),
	@JsonProperty("Helpdesk")
	HELPDESK("Helpdesk"),
	@JsonProperty("Issue tracker")
	ISSUE_TRACKER("Issue tracker"),
	@JsonProperty("Mailing list")
	MAILING_LIST("Mailing list"),
	@JsonProperty("Mirror")
	MIRROR("Mirror"),
	@JsonProperty("Registry")
	REGISTRY("Registry"),
	@JsonProperty("Repository")
	REPOSITORY("Repository"),
	@JsonProperty("Social media")
	SOCIAL_MEDIA("Social media"),
	@JsonProperty("Scientific benchmark")
	SCIENTIFIC_BENCHMARK("Scientific benchmark"),
	@JsonProperty("Technical monitoring")
	TECHNICAL_MONITORING("Technical monitoring"),
	// TODO hack https://github.com/bio-tools/biotoolsSchema/issues/129
	@JsonProperty("Galaxy service")
	GALAXY_SERVICE("Galaxy service"),
	// TODO hack https://github.com/bio-tools/biotoolsSchema/issues/128
	@JsonProperty("Other")
	OTHER("Other"),
	// TODO hack https://github.com/bio-tools/biotoolsSchema/issues/138
	@JsonProperty("Discussion forum")
	DISCUSSION_FORUM("Discussion forum");

	private String type;

	private LinkType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}
}
