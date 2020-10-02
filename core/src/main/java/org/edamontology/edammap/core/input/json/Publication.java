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

package org.edamontology.edammap.core.input.json;

import java.text.ParseException;
import java.util.List;

public class Publication {

	private String doi;

	private String pmid;

	private String pmcid;

	private List<PublicationType> type;

	private String version;

	private String note;

	// TODO not in schema
	private PublicationMetadata metadata;

	public void check(Tool tool, int i, String index) throws ParseException {
		if ((doi == null || doi.equals("")) && (pmid == null || pmid.equals("")) && (pmcid == null || pmcid.equals(""))) {
			tool.parseException("doi and pmid and pmcid", i, index);
		}
	}

	public String getDoi() {
		return doi;
	}
	public void setDoi(String doi) {
		this.doi = doi;
	}

	public String getPmid() {
		return pmid;
	}
	public void setPmid(String pmid) {
		this.pmid = pmid;
	}

	public String getPmcid() {
		return pmcid;
	}
	public void setPmcid(String pmcid) {
		this.pmcid = pmcid;
	}

	public List<PublicationType> getType() {
		return type;
	}
	public void setType(List<PublicationType> type) {
		this.type = type;
	}

	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}

	public PublicationMetadata getMetadata() {
		return metadata;
	}
	public void setMetadata(PublicationMetadata metadata) {
		this.metadata = metadata;
	}

	public String toStringType() {
		if (type == null || type.isEmpty()) {
			return "";
		} else if (type.size() == 1) {
			return type.get(0).toString();
		} else {
			return type.toString();
		}
	}
}
