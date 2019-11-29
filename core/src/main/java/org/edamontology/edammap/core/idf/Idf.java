/*
 * Copyright © 2016, 2018, 2019 Erik Jaaniso
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.carrotsearch.hppc.ObjectDoubleScatterMap;
import com.carrotsearch.hppc.ObjectIntScatterMap;

public class Idf {

	private static final Logger logger = LogManager.getLogger();

	private final ObjectDoubleScatterMap<String> idfMap;

	private final ObjectIntScatterMap<String> countsMap;

	private final int documentCount;

	private final List<IdfTop> idfTop;

	public Idf(ObjectDoubleScatterMap<String> idfMap, ObjectIntScatterMap<String> countsMap, int documentCount) {
		this.idfMap = idfMap;
		this.countsMap = countsMap;
		this.documentCount = documentCount;
		this.idfTop = null;
	}

	public Idf(String inputPath, boolean top) throws IOException {
		try (BufferedReader br = Files.newBufferedReader(Paths.get(inputPath), StandardCharsets.UTF_8)) {
			if (top) {
				this.idfTop = new ArrayList<>();
				String line = br.readLine();
				if (line != null) {
					this.documentCount = Integer.parseInt(line);
				} else {
					throw new IOException("First line must be document count!");
				}
				while ((line = br.readLine()) != null) {
					int tab = line.indexOf("\t");
					String key = line.substring(0, tab);
					int value = Integer.parseInt(line.substring(tab + 1, line.indexOf("\t", tab + 1)));
					idfTop.add(new IdfTop(key, value));
				}
				Collections.sort(idfTop);
				this.idfMap = null;
				this.countsMap = null;
			} else {
				this.idfMap = new ObjectDoubleScatterMap<>();
				this.countsMap = new ObjectIntScatterMap<>();
				String line = br.readLine();
				if (line != null) {
					this.documentCount = Integer.parseInt(line);
				} else {
					throw new IOException("First line must be document count!");
				}
				long entries = 0;
				while ((line = br.readLine()) != null) {
					++entries;
					int tab = line.indexOf("\t");
					String key = line.substring(0, tab);
					int tab2 = line.indexOf("\t", tab + 1);
					int count = Integer.parseInt(line.substring(tab + 1, tab2));
					this.countsMap.put(key, count);
					double idf = Double.parseDouble(line.substring(tab2 + 1));
					this.idfMap.put(key, idf);
				}
				logger.debug("Loaded IDF {} with {} entries", inputPath, entries);
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

	// getIdf(String) has shift +1
	public double getIdfShifted(String term, int shift) {
		int termCount = 0;
		if (countsMap.containsKey(term)) {
			termCount = countsMap.get(term);
		}
		int totalCount = termCount + shift;
		if (totalCount < 1) totalCount = 1;
		double idf = Math.log10(documentCount / (double) totalCount) / Math.log10(documentCount);
		if (idf < 0) idf = 0;
		return idf;
	}

	public List<Double> getIdf(Collection<String> terms) {
		return terms.stream().map(term -> getIdf(term)).collect(Collectors.toList());
	}

	public List<IdfTop> getTop() {
		return idfTop;
	}
}
