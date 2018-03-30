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

package org.edamontology.edammap.core.idf;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.carrotsearch.hppc.ObjectDoubleScatterMap;

public class Idf {

	private final ObjectDoubleScatterMap<String> idfMap;

	private final List<IdfTop> idfTop;

	public Idf(ObjectDoubleScatterMap<String> idfMap) {
		this.idfMap = idfMap;
		this.idfTop = null;
	}

	public Idf(String inputPath, boolean top) throws IOException {
		try (BufferedReader br = Files.newBufferedReader(Paths.get(inputPath), StandardCharsets.UTF_8)) {
			if (top) {
				this.idfTop = new ArrayList<>();
				for (String line; (line = br.readLine()) != null; ) {
					int tab = line.indexOf("\t");
					String key = line.substring(0, tab);
					int value = Integer.parseInt(line.substring(tab + 1, line.indexOf("\t", tab + 1)));
					idfTop.add(new IdfTop(key, value));
				}
				Collections.sort(idfTop);
				this.idfMap = null;
			} else {
				this.idfMap = new ObjectDoubleScatterMap<>();
				for (String line; (line = br.readLine()) != null; ) {
					int tab = line.indexOf("\t");
					String key = line.substring(0, tab);
					double value = Double.parseDouble(line.substring(line.indexOf("\t", tab + 1) + 1));
					this.idfMap.put(key, value);
				}
				this.idfTop = null;
			}
		}
	}

	public Idf(String inputPath) throws IOException {
		this(inputPath, false);
	}

	public double getIdf(String term) {
		if (idfMap.containsKey(term)) {
			return idfMap.get(term);
		} else {
			return 1.0d;
		}
	}

	public List<Double> getIdf(Collection<String> terms) {
		return terms.stream().map(term -> getIdf(term)).collect(Collectors.toList());
	}

	public List<IdfTop> getTop() {
		return idfTop;
	}
}
