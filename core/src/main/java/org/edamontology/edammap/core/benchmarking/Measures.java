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

import java.util.EnumMap;
import java.util.Map;

public class Measures {

	private final Map<Test, Integer> test;

	private final Map<Measure, Double> measure;

	public Measures() {
		test = new EnumMap<>(Test.class);
		measure = new EnumMap<>(Measure.class);

		for (Test t : Test.values()) {
			test.put(t, 0);
		}
		for (Measure m : Measure.values()) {
			measure.put(m, 0.0);
		}
	}

	public int getTest(Test test) {
		return this.test.get(test);
	}
	void addTest(Test test, int value) {
		this.test.put(test, this.test.get(test) + value);
	}

	public double getMeasure(Measure measure) {
		return this.measure.get(measure);
	}
	void addMeasure(Measure measure, double value) {
		this.measure.put(measure, this.measure.get(measure) + value);
	}
	void divideMeasure(Measure measure, double by) {
		this.measure.put(measure, this.measure.get(measure) / by);
	}
}
