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

package org.edamontology.edammap.core.benchmarking;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.edamontology.edammap.core.edam.Branch;

public class MappingTest {

	final String id;

	final String name;

	final Map<Branch, List<MatchTest>> matches;

	public MappingTest(String id, String name) {
		this.id = id;

		this.name = name;

		matches = new EnumMap<>(Branch.class);

		for (Branch branch : Branch.values()) {
			matches.put(branch, new ArrayList<MatchTest>());
		}
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<MatchTest> getMatches(Branch branch) {
		return matches.get(branch);
	}
}
