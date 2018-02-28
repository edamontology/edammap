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

package org.edamontology.edammap.core.idf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Idf {

	private final Map<String, Double> idfMap;

	private final Map<String, Integer> idfTop;

	public Idf(Map<String, Double> idfMap) {
		this.idfMap = idfMap;
		this.idfTop = null;
	}

	public Idf(String inputPath, boolean top) throws IOException {
		try (Stream<String> lines = Files.lines(Paths.get(inputPath), StandardCharsets.UTF_8)) {
			if (top) {
				this.idfTop = lines.map(s -> s.split("\t"))
					.sorted(Collections.reverseOrder(Comparator.comparing(i -> Integer.parseInt(i[1]))))
					.collect(Collectors.toMap(s -> s[0], i -> Integer.parseInt(i[1]),
						(u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
						LinkedHashMap::new));
				this.idfMap = null;
			} else {
				this.idfMap = lines.map(s -> s.split("\t"))
					.collect(Collectors.toMap(s -> s[0], d -> Double.parseDouble(d[2])));
				this.idfTop = null;
			}
		}
	}

	public Idf(String inputPath) throws IOException {
		this(inputPath, false);
	}

	public Double getIdf(String term) {
		Double idf = idfMap.get(term);
		if (idf != null) return idf;
		else return Double.valueOf(1.0);
	}

	public List<Double> getIdf(Collection<String> terms) {
		return terms.stream().map(term -> getIdf(term)).collect(Collectors.toList());
	}

	public Map<String, Integer> getTop() {
		return idfTop;
	}
}
