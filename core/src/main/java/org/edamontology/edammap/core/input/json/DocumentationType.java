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

public enum DocumentationType {
	@JsonProperty("API documentation")
	API_DOCUMENTATION("API documentation"),
	@JsonProperty("Citation instructions")
	CITATION_INSTRUCTIONS("Citation instructions"),
	@JsonProperty("Code of conduct")
	CODE_OF_CONDUCT("Code of conduct"),
	@JsonProperty("Command-line options")
	COMMAND_LINE_OPTIONS("Command-line options"),
	@JsonProperty("Contributions policy")
	CONTRIBUTIONS_POLICY("Contributions policy"),
	@JsonProperty("FAQ")
	FAQ("FAQ"),
	@JsonProperty("General")
	GENERAL("General"),
	@JsonProperty("Governance")
	GOVERNANCE("Governance"),
	@JsonProperty("Installation instructions")
	INSTALLATION_INSTRUCTIONS("Installation instructions"),
	@JsonProperty("User manual")
	USER_MANUAL("User manual"),
	@JsonProperty("Release notes")
	RELEASE_NOTES("Release notes"),
	@JsonProperty("Terms of use")
	TERMS_OF_USE("Terms of use"),
	@JsonProperty("Training material")
	TRAINING_MATERIAL("Training material"),
	@JsonProperty("Other")
	OTHER("Other"),
	// TODO remove (not in schema)
	@JsonProperty("Tutorial")
	TUTORIAL("Tutorial"),
	// TODO remove (not in schema)
	@JsonProperty("Manual")
	MANUAL("Manual");

	private String type;

	private DocumentationType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}
}
