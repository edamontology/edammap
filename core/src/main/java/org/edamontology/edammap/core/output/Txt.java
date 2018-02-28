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

package org.edamontology.edammap.core.output;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.edamontology.edammap.core.edam.Branch;
import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.mapping.Mapping;
import org.edamontology.edammap.core.mapping.Match;
import org.edamontology.edammap.core.query.Query;
import org.edamontology.edammap.core.query.QueryType;
import org.edamontology.pubfetcher.Publication;

class Txt {

	private static final String SEP = " | ";

	// TODO publications needed ?
	private static void out(QueryType type, PrintStream ps, Map<EdamUri, Concept> concepts, List<Query> queries, List<List<Publication>> publications, List<Mapping> mappings) throws IOException {
		for (int i = 0; i < queries.size(); ++i) {
			Query query = queries.get(i);
			Mapping mapping = mappings.get(i);

			if (type == QueryType.SEQwikiTags || type == QueryType.SEQwikiTool) {
				if (type == QueryType.SEQwikiTool) {
					ps.print(query.getName() + SEP);
				}
				ps.print(query.getKeywords().iterator().next().getValue());
				ps.print(SEP + query.getKeywords().iterator().next().getType());
				for (Branch branch : mapping.getBranches()) {
					for (int j = 0; j < mapping.getMatches(branch).size(); ++j) {
						Match match = mapping.getMatches(branch).get(j);
						Concept concept = concepts.get(match.getEdamUri());
						ps.print(SEP + concept.getLabel());
						ps.print(" (" + match.getEdamUri().getBranch() + "_" + match.getEdamUri().getNrString() + ")");
					}
				}
				ps.println();
			} else {
				for (Branch branch : mapping.getBranches()) {
					for (int j = 0; j < mapping.getMatches(branch).size(); ++j) {
						Match match = mapping.getMatches(branch).get(j);
						Concept concept = concepts.get(match.getEdamUri());

						if (query.getName() != null) {
							ps.print(query.getName());
						} else {
							ps.print("NA");
						}

						// TODO
						ps.println(SEP + concept.getLabel()
							//+ SEP + Common.conceptMatchString(match, concept)
							+ SEP + match.getEdamUri()
							+ SEP + concept.isObsolete()
							//+ SEP + match.getMatchConfidence()
							+ SEP + match.getEdamUri().getBranch()
							+ SEP + match.getConceptMatch().getType()
							+ SEP + match.getQueryMatch().getType()
							//+ SEP + Common.queryMatchString(match, query, publications)
							+ SEP + match.getScore()
							+ SEP + match.getWithoutPathScore()
							+ SEP + match.getBestOneScore());
					}
				}
			}
		}
	}

	static void output(QueryType type, Path output, Map<EdamUri, Concept> concepts, List<Query> queries, List<List<Publication>> publications, List<Mapping> mappings) throws IOException {
		if (output == null) {
			out(type, System.out, concepts, queries, publications, mappings);
		} else {
			try (PrintStream ps = new PrintStream(new BufferedOutputStream(Files.newOutputStream(output)), true, "UTF-8")) {
				out(type, ps, concepts, queries, publications, mappings);
			} catch (IOException e) {
				try {
					out(type, System.out, concepts, queries, publications, mappings);
				} catch (Exception e2) {
					throw e;
				}
				throw e;
			}
		}
	}
}
