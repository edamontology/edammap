/*
 * Copyright © 2016, 2018 Erik Jaaniso
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

package org.edamontology.edammap.core.benchmarking;

public enum Test {
	tp("True Positive (TP)", "https://en.wikipedia.org/wiki/Type_I_and_type_II_errors"),
	fp("False Positive (FP)", "https://en.wikipedia.org/wiki/Type_I_and_type_II_errors"),
	fn("False Negative (FN)", "https://en.wikipedia.org/wiki/Type_I_and_type_II_errors");

	private String name;

	private String url;

	private Test(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return name;
	}
}
