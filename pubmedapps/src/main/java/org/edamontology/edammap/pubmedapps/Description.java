/*
 * Copyright © 2019 Erik Jaaniso
 *
 * This file is part of PubMedApps.
 *
 * PubMedApps is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PubMedApps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PubMedApps.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.edamontology.edammap.pubmedapps;

public class Description implements Comparable<Description> {

	private String description;

	private final String descriptionSeparated;

	private final int priority;

	public Description(String description, String descriptionSeparated, int priority) {
		this.description = description;
		this.descriptionSeparated = descriptionSeparated;
		this.priority = priority;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescriptionSeparated() {
		return descriptionSeparated;
	}

	public int getPriority() {
		return priority;
	}

	@Override
	public int compareTo(Description o) {
		if (o == null) return -1;
		if (this.priority < o.priority) return -1;
		if (this.priority > o.priority) return 1;
		return 0;
	}
}
