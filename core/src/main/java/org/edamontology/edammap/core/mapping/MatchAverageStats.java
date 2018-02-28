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

package org.edamontology.edammap.core.mapping;

public class MatchAverageStats implements Comparable<MatchAverageStats> {

	private final QueryMatch queryMatch;

	private final ConceptMatch conceptMatch;

	double score;

	MatchAverageStats(QueryMatch queryMatch, ConceptMatch conceptMatch, double score) {
		this.queryMatch = queryMatch;
		this.conceptMatch = conceptMatch;
		this.score = score;
	}

	public QueryMatch getQueryMatch() {
		return queryMatch;
	}

	public ConceptMatch getConceptMatch() {
		return conceptMatch;
	}

	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public int compareTo(MatchAverageStats m) {
		if (m == null) return 1;

		if (this.score > m.score) return 1;
		if (this.score < m.score) return -1;

		return 0;
	}
}
