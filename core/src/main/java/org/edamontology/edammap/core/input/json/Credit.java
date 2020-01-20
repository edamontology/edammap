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

import java.text.ParseException;
import java.util.List;

public class Credit {

	private String name;

	private String email;

	private String url;

	private String orcidid;

	private String gridid;

	private EntityType typeEntity;

	private List<RoleType> typeRole;

	private String note;

	public void check(Tool tool, int i, String index) throws ParseException {
		if ((name == null || name.equals("")) && (email == null || email.equals("")) && (url == null || url.equals(""))) {
			tool.parseException("name and email and url", i, index);
		}
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String getOrcidid() {
		return orcidid;
	}
	public void setOrcidid(String orcidid) {
		this.orcidid = orcidid;
	}

	public String getGridid() {
		return gridid;
	}
	public void setGridid(String gridid) {
		this.gridid = gridid;
	}

	public EntityType getTypeEntity() {
		return typeEntity;
	}
	public void setTypeEntity(EntityType typeEntity) {
		this.typeEntity = typeEntity;
	}

	public List<RoleType> getTypeRole() {
		return typeRole;
	}
	public void setTypeRole(List<RoleType> typeRole) {
		this.typeRole = typeRole;
	}

	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
}
