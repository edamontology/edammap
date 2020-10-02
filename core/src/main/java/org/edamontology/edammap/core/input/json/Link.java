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

public class Link<T> {

	private String url;

	private List<T> type;

	private String note;

	public void check(Tool tool, int i, String index) throws ParseException {
		if (url == null || url.equals("")) {
			tool.parseException("url", i, index);
		}
		if (type == null || type.isEmpty()) {
			tool.parseException("type", i, index);
		}
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public List<T> getType() {
		return type;
	}
	public void setType(List<T> type) {
		this.type = type;
	}

	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}

	public String toStringType() {
		if (type.size() == 1) {
			return type.get(0).toString();
		} else {
			return type.toString();
		}
	}
}
