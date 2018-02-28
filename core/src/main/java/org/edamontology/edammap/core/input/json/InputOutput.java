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
import java.util.ArrayList;
import java.util.List;

public class InputOutput {

	private Edam data;

	private List<Edam> format = new ArrayList<>();

	public void check(ToolInput tool, int i, String index) throws ParseException {
		if (data == null) {
			tool.parseException("data", i, index);
		} else {
			data.check(tool, i, index);
		}
		if (format != null) {
			for (int j = 0; j < format.size(); ++j) {
				format.get(j).check(tool, i, index + ", format " + j);
			}
		}
	}

	public Edam getData() {
		return data;
	}
	public void setData(Edam data) {
		this.data = data;
	}

	public List<Edam> getFormat() {
		return format;
	}
	public void setFormat(List<Edam> format) {
		this.format = format;
	}
}
