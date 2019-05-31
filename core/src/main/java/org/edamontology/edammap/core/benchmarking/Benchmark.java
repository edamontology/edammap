/*
 * Copyright © 2016, 2017, 2018 Erik Jaaniso
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
import java.util.List;
import java.util.Map;

import org.edamontology.edammap.core.edam.Branch;
import org.edamontology.edammap.core.mapping.Mapping;
import org.edamontology.edammap.core.mapping.Match;
import org.edamontology.edammap.core.query.Query;

public class Benchmark {

	public static Results calculate(List<Query> queries, List<Mapping> mappings) {
		Results results = new Results();

		Map<Branch, Long> size = new EnumMap<>(Branch.class);
		for (Branch branch : Branch.values()) {
			size.put(branch, 0l);
		}

		for (int i = 0; i < queries.size(); ++i) {
			Query query = queries.get(i);
			Mapping mapping = mappings.get(i);

			MappingTest mappingTest = new MappingTest(query.getId(), query.getName());

			for (Branch branch : mapping.getBranches()) {

				long annotationsSize;
				if (query.getAnnotations() != null) {
					annotationsSize = query.getAnnotations().stream().filter(e -> e.getBranch() == branch).count();
				} else {
					annotationsSize = 0;
				}

				if (annotationsSize > 0) {
					size.put(branch, size.get(branch) + 1);
				}

				int tp = 0, fp = 0, fn = 0;
				double DCG = 0, IDCG = 0, DCGa = 0, IDCGa = 0;

				for (int j = 0; j < mapping.getMatches(branch).size(); ++j) {
					Match match = mapping.getMatches(branch).get(j);

					if (match.isExistingAnnotation()) {
						mappingTest.matches.get(branch).add(new MatchTest(match, Test.tp));

						++tp;

						double precisionAve = tp / (double) (tp + fp);
						results.measures.get(branch).addMeasure(Measure.AveP, precisionAve / (double) annotationsSize);

						if (j < annotationsSize) {
							results.measures.get(branch).addMeasure(Measure.RP, 1 / (double) annotationsSize);
						}

						int rel = 1;
						if (j == 0) {
							DCG += rel;
						} else {
							DCG += rel / (Math.log(j + 1) / Math.log(2));
						}
						DCGa += (Math.pow(2, rel) - 1) / (Math.log(j + 1 + 1) / Math.log(2));
					} else {
						mappingTest.matches.get(branch).add(new MatchTest(match, Test.fp));

						++fp;
					}

					if (annotationsSize > 0) {
						int Mrel = ((annotationsSize - j <= 0) ? 0 : 1);
						if (j == 0) {
							IDCG += Mrel;
						} else {
							IDCG += Mrel / (Math.log(j + 1) / Math.log(2));
						}
						IDCGa += (Math.pow(2, Mrel) - 1) / (Math.log(j + 1 + 1) / Math.log(2));
					}
				}

				for (Match excludedAnnotation : mapping.getRemainingAnnotations(branch)) {
					mappingTest.matches.get(branch).add(new MatchTest(excludedAnnotation, Test.fn));

					++fn;
				}

				results.measuresTotal.addTest(Test.tp, tp);
				results.measuresTotal.addTest(Test.fp, fp);
				results.measuresTotal.addTest(Test.fn, fn);
				results.measures.get(branch).addTest(Test.tp, tp);
				results.measures.get(branch).addTest(Test.fp, fp);
				results.measures.get(branch).addTest(Test.fn, fn);

				if (annotationsSize > 0) {
					double precision = 0;
					if (tp > 0 || fp > 0) precision = tp / (double) (tp + fp);
					double recall = tp / (double) (tp + fn);
					results.measures.get(branch).addMeasure(Measure.precision, precision);
					results.measures.get(branch).addMeasure(Measure.recall, recall);
					if (tp > 0) {
						results.measures.get(branch).addMeasure(Measure.f1, 2 * (precision * recall) / (precision + recall));
						results.measures.get(branch).addMeasure(Measure.f2, (1 + Math.pow(2, 2)) * (precision * recall) / ((Math.pow(2, 2) * precision) + recall));
					}
					results.measures.get(branch).addMeasure(Measure.Jaccard, tp / (double) (tp + fp + fn));
					if (tp > 0 || fp > 0) {
						results.measures.get(branch).addMeasure(Measure.DCG, DCG / IDCG);
						results.measures.get(branch).addMeasure(Measure.DCGa, DCGa / IDCGa);
					}
				}
			}

			results.mappings.add(mappingTest);
		}

		for (Branch branch : Branch.values()) {
			long s = size.get(branch);
			if (s == 0) continue;
			for (Measure measure : Measure.values()) {
				results.measures.get(branch).divideMeasure(measure, s);
			}
		}

		int branchesSize = 0;
		for (Branch branch : Branch.values()) {
			if (size.get(branch) == 0) continue;
			++branchesSize;
			for (Measure measure : Measure.values()) {
				results.measuresTotal.addMeasure(measure, results.measures.get(branch).getMeasure(measure));
			}
		}
		if (branchesSize > 0) {
			for (Measure measure : Measure.values()) {
				results.measuresTotal.divideMeasure(measure, branchesSize);
			}
		}

		return results;
	}
}
