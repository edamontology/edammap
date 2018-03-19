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
import java.util.Locale;
import java.util.Map;

import org.edamontology.edammap.core.edam.Branch;

public class Results {

	final List<MappingTest> mappings;

	final Map<Branch, Measures> measures;

	final Measures measuresTotal;

	public Results() {
		mappings = new ArrayList<>();
		measures = new EnumMap<>(Branch.class);
		measuresTotal = new Measures();

		for (Branch branch : Branch.values()) {
			measures.put(branch, new Measures());
		}
	}

	public List<MappingTest> getMappings() {
		return mappings;
	}

	public Measures getMeasures(Branch branch) {
		return measures.get(branch);
	}

	public Measures getMeasuresTotal() {
		return measuresTotal;
	}

	private static String percent(double val) {
		return String.format(Locale.ROOT, "%.2f%%", val * 100);
	}

	public String toStringTest(Test test) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		List<String> tests = new ArrayList<>();
		for (Branch branch : Branch.values()) {
			tests.add(Integer.toString(this.measures.get(branch).getTest(test)));
		}
		sb.append(String.join(", ", tests));
		sb.append("] (");
		sb.append(Integer.toString(measuresTotal.getTest(test)));
		sb.append(")");
		return sb.toString();
	}

	public String toStringMeasure(Measure measure) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		List<String> measures = new ArrayList<>();
		for (Branch branch : Branch.values()) {
			measures.add(percent(this.measures.get(branch).getMeasure(measure)));
		}
		sb.append(String.join(", ", measures));
		sb.append("] (");
		sb.append(percent(measuresTotal.getMeasure(measure)));
		sb.append(")");
		return sb.toString();
	}
}
