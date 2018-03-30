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

package org.edamontology.edammap.core.idf;

public class IdfTop implements Comparable<IdfTop> {

	private final String term;

	private final int count;

	public IdfTop(String term, int count) {
		this.term = term;
		this.count = count;
	}

	public String getTerm() {
		return term;
	}

	public int getCount() {
		return count;
	}

	@Override
	public int compareTo(IdfTop o) {
		if (o == null) return -1;
		if (this.count > o.count) return -1;
		if (this.count < o.count) return 1;
		return 0;
	}
}
