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

import org.edamontology.pubfetcher.core.db.publication.Publication;

import org.edamontology.edammap.core.benchmarking.MappingTest;
import org.edamontology.edammap.core.benchmarking.MatchTest;
import org.edamontology.edammap.core.edam.Branch;
import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.mapping.Match;
import org.edamontology.edammap.core.query.Query;
import org.edamontology.edammap.core.query.QueryType;

public class Txt {

	private static final String SEP = "\t";

	static void out(QueryType type, PrintStream ps, Map<EdamUri, Concept> concepts, List<Query> queries, List<MappingTest> mappings) throws IOException {
		ps.print("query_id");
		ps.print(SEP);
		ps.print("query_name");
		ps.print(SEP);
		ps.print("edam_branch");
		ps.print(SEP);
		ps.print("edam_uri");
		ps.print(SEP);
		ps.print("edam_label");
		ps.print(SEP);
		ps.print("edam_obsolete");
		ps.print(SEP);
		ps.print("best_one_query");
		ps.print(SEP);
		ps.print("best_one_concept");
		ps.print(SEP);
		ps.print("best_one_score");
		ps.print(SEP);
		ps.print("without_path_score");
		ps.print(SEP);
		ps.print("score");
		ps.print(SEP);
		ps.print("test");
		ps.println();

		for (int i = 0; i < queries.size(); ++i) {
			Query query = queries.get(i);
			MappingTest mapping = mappings.get(i);

			for (Branch branch : Branch.values()) {
				for (MatchTest matchTest : mapping.getMatches(branch)) {
					Match match = matchTest.getMatch();
					Concept concept = concepts.get(match.getEdamUri());

					ps.print(query.getId() != null ? query.getId() : "");
					ps.print(SEP);
					ps.print(query.getName() != null ? query.getName() : "");
					ps.print(SEP);
					ps.print(branch);
					ps.print(SEP);
					ps.print(match.getEdamUri());
					ps.print(SEP);
					ps.print(concept.getLabel());
					ps.print(SEP);
					ps.print(concept.isObsolete());
					ps.print(SEP);
					ps.print(match.getQueryMatch().getType().name());
					ps.print(SEP);
					ps.print(match.getConceptMatch().getType().name());
					ps.print(SEP);
					ps.print(match.getBestOneScore() > -1 ? match.getBestOneScore() : "");
					ps.print(SEP);
					ps.print(match.getWithoutPathScore() > - 1 ? match.getWithoutPathScore() : "");
					ps.print(SEP);
					ps.print(match.getScore());
					ps.print(SEP);
					ps.print(matchTest.getTest().name());
					ps.println();
				}
			}
		}
	}

	static void output(QueryType type, Path txt, Path report, Map<EdamUri, Concept> concepts, List<Query> queries, List<List<Publication>> publications, List<MappingTest> mappings) throws IOException {
		if (txt == null && report == null) {
			out(type, System.out, concepts, queries, mappings);
		} else if (txt != null) {
			try (PrintStream ps = new PrintStream(new BufferedOutputStream(Files.newOutputStream(txt)), true, "UTF-8")) {
				out(type, ps, concepts, queries, mappings);
			} catch (IOException e) {
				try {
					out(type, System.out, concepts, queries, mappings);
				} catch (Exception e2) {
					throw e;
				}
				throw e;
			}
		}
	}
}
