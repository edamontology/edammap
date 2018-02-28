/*
 * Copyright © 2016 Erik Jaaniso
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

package org.edamontology.edammap.core.mapping;

public class QueryMatch {

	private final double score;

	private final QueryMatchType type;

	private final int index;

	private final int indexInPublication;

	QueryMatch(double score, QueryMatchType type, int index, int indexInPublication) {
		this.score = score;
		this.type = type;
		this.index = index;
		this.indexInPublication = indexInPublication;
	}

	public double getScore() {
		return score;
	}

	public QueryMatchType getType() {
		return type;
	}

	public int getIndex() {
		return index;
	}

	public int getIndexInPublication() {
		return indexInPublication;
	}
}
