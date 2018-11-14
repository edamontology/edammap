/*
 * Copyright © 2018 Erik Jaaniso
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

public class BiotoolsLink {

	private final String url;

	private final String type;

	public BiotoolsLink(String url, String type) {
		if (!PubMedApps.SCHEMA_START.matcher(url).find()) {
			this.url = "http://" + url;
		} else {
			this.url = url;
		}
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public String getType() {
		return type;
	}
}
