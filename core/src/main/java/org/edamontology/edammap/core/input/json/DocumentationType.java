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

public enum DocumentationType {
	API_DOCUMENTATION("API documentation"),
	CITATION_INSTRUCTIONS("Citation instructions"),
	CONTRIBUTIONS_POLICY("Contributions policy"),
	GENERAL("General"),
	GOVERNANCE("Governance"),
	INSTALLATION_INSTRUCTIONS("Installation instructions"),
	MANUAL("Manual"),
	TERMS_OF_USE("Terms of use"),
	TRAINING_MATERIAL("Training material"),
	TUTORIAL("Tutorial"),
	OTHER("Other");

	private String type;

	private DocumentationType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}
}
