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

package org.edamontology.edammap.core.output;

public class ParamMain extends Param {

	private final boolean input;

	public ParamMain(String label, String id, Object value, boolean input) {
		super(label, id, value);
		this.input = input;
	}

	public ParamMain(String label, String id, Object value, Double min, Double max, boolean input) {
		super(label, id, value, min, max);
		this.input = input;
	}

	public ParamMain(String label, String id, Object value, String url, boolean input) {
		super(label, id, value, url);
		this.input = input;
	}

	public boolean getInput() {
		return input;
	}
}
