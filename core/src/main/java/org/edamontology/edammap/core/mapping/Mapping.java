/*
 * Copyright © 2016, 2017 Erik Jaaniso
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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.edamontology.edammap.core.edam.Branch;

public class Mapping {

	private Map<Branch, List<Match>> matches;

	private Map<Branch, List<Match>> remainingAnnotations;

	private final int matchesTop;

	private final List<Branch> branches;

	public Mapping(int matchesTop, List<Branch> branches) {
		if (matchesTop < 0) {
			throw new IllegalArgumentException("Matches per branch must be positive");
		} else if (branches == null || branches.isEmpty()) {
			throw new IllegalArgumentException("At least one branch must be specified");
		}

		matches = new EnumMap<>(Branch.class);
		remainingAnnotations = new EnumMap<>(Branch.class);

		this.matchesTop = matchesTop;
		this.branches = branches;

		for (Branch branch : this.branches) {
			matches.put(branch, new ArrayList<Match>(this.matchesTop));
		}
		for (Branch branch : this.branches) {
			remainingAnnotations.put(branch, new ArrayList<Match>());
		}
	}

	public int getMatchesTop() {
		return matchesTop;
	}

	public List<Branch> getBranches() {
		return branches;
	}

	public List<Match> getMatches(Branch branch) {
		return matches.get(branch);
	}
	public boolean addMatch(Match match) {
		List<Match> matchesBranch = matches.get(match.getEdamUri().getBranch());
		if (matchesBranch.size() < matchesTop) {
			matchesBranch.add(match);
			return true;
		} else {
			return false;
		}
	}

	public List<Match> getRemainingAnnotations(Branch branch) {
		return remainingAnnotations.get(branch);
	}
	public void addRemainingAnnotation(Match match) {
		remainingAnnotations.get(match.getEdamUriOriginal().getBranch()).add(match);
	}

	public boolean isFull(Branch branch) {
		if (matches.get(branch).size() < matchesTop) {
			return false;
		} else {
			return true;
		}
	}
	public boolean isFull() {
		for (Branch branch : branches) {
			if (!isFull(branch)) return false;
		}
		return true;
	}
}
