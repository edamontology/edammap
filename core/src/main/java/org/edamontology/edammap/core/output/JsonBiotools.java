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

package org.edamontology.edammap.core.output;

import java.util.List;

import org.edamontology.edammap.core.input.json.Tool;

public class JsonBiotools {

	private int count;

	private List<Tool> list;

	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}

	public List<Tool> getList() {
		return list;
	}
	public void setList(List<Tool> list) {
		this.list = list;
	}
}
