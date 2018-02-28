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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.edamontology.pubfetcher.FetcherCommon;

public class IdfMake {

	private Path output;

	private int documentCount;

	private Map<String, Integer> termCounts;

	private Set<String> documentTerms;

	public IdfMake() {
		documentCount = 0;
		termCounts = new TreeMap<>();
		documentTerms = new HashSet<>();
	}

	public IdfMake(String outputPath) throws IOException {
		this();

		this.output = FetcherCommon.outputPath(outputPath, false);
	}

	public void addTerms(List<String> terms) {
		documentTerms.addAll(terms);
	}

	public void addTermsTerms(List<List<String>> terms) {
		terms.forEach(t -> documentTerms.addAll(t));
	}

	public void endDocument() {
		documentTerms.forEach(term -> termCounts.merge(term, 1, Integer::sum));
		documentTerms.clear();

		++documentCount;
	}

	// no +1, as for concepts, where all words will be in IDF
	public Map<String, Double> getIdf() {
		Map<String, Double> idfMap = new HashMap<>();
		double idf_max = Math.log10(documentCount);
		for (Map.Entry<String, Integer> termCount : termCounts.entrySet()) {
			double idf = Math.log10(documentCount / (double)(termCount.getValue())) / idf_max;
			idfMap.put(termCount.getKey(), idf);
		}
		return idfMap;
	}

	// +1, as for queries, where unknown words might be queried
	public void writeOutput() throws IOException {
		CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
		encoder.onMalformedInput(CodingErrorAction.REPLACE);
		encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(output), encoder))) {
			double idf_max = Math.log10(documentCount);
			for (Map.Entry<String, Integer> termCount : termCounts.entrySet()) {
				writer.write(termCount.getKey());
				writer.write("\t");
				writer.write(termCount.getValue().toString());
				writer.write("\t");
				double idf = Math.log10(documentCount / (double)(termCount.getValue() + 1)) / idf_max;
				if (idf < 0) idf = 0;
				writer.write(String.valueOf(idf));
				writer.write("\n");
			}
		}
	}
}
