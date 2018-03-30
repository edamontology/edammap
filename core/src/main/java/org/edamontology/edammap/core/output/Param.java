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

public class Param {

	private final String label;

	private final String id;

	private final Object value;

	private final Double min;

	private final Double max;

	private final String url;

	public Param(String label, String id, Object value) {
		this(label, id, value, null, null, null);
	}

	public Param(String label, String id, Object value, Double min, Double max) {
		this(label, id, value, min, max, null);
	}

	public Param(String label, String id, Object value, String url) {
		this(label, id, value, null, null, url);
	}

	public Param(String label, String id, Object value, Double min, Double max, String url) {
		this.label = label;
		this.id = id;
		this.value = value;
		this.min = min;
		this.max = max;
		this.url = url;
	}

	String getLabel() {
		return label;
	}

	String getId() {
		return id;
	}

	Object getValue() {
		return value;
	}

	Double getMin() {
		return min;
	}

	Double getMax() {
		return max;
	}

	String getUrl() {
		return url;
	}
}
