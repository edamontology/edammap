package edammapper.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edammapper.args.Args;
import edammapper.edam.Branch;
import edammapper.edam.Concept;
import edammapper.edam.EdamUri;
import edammapper.mapping.Mapping;
import edammapper.mapping.Match;
import edammapper.query.IOType;
import edammapper.query.Query;

class Benchmark {

	private enum ResultType {
		tp, fp, fn, empty
	}

	private static class BenchmarkRow {
		private ResultType resultType;
		private Match match = null;
		private EdamUri edamUri = null;
		private BenchmarkRow() {
			this.resultType = ResultType.empty;
		}
		private BenchmarkRow(EdamUri edamUri) {
			this.resultType = ResultType.fn;
			this.edamUri = edamUri;
		}
		private BenchmarkRow(ResultType resultType, Match match) {
			this.resultType = resultType;
			this.match = match;
		}
	}

	private static int tpT = 0, fpT = 0, fnT = 0;
	private static int[] tp, fp, fn;
	private static double[] precision, recall;
	private static double[] f1, f2;
	private static double[] Jaccard;
	private static double[] AveP, RP;
	private static double[] DCG, DCGa;
	private static int[] size;
	private static Date date = new Date();

	private static int[] initArray(int[] a, int branchesSize) {
		if (a == null || a.length < branchesSize) {
			a = new int[branchesSize];
		}
		Arrays.fill(a, 0);
		return a;
	}

	private static double[] initArray(double[] a, int branchesSize) {
		if (a == null || a.length < branchesSize) {
			a = new double[branchesSize];
		}
		Arrays.fill(a, 0.0d);
		return a;
	}

	private static void averageElements(double[] a, int[] size) {
		for (int i = 0; i < a.length; ++i) {
			a[i] /= (double)size[i];
		}
	}

	private static int[] addArrays(int[] a, int[] b) {
		int[] c = new int[a.length];
		for (int i = 0; i < a.length; ++i) {
			c[i] = a[i] + b[i];
		}
		return c;
	}

	private static List<List<BenchmarkRow>> calculate(List<Query> queries, List<Mapping> mappings) {
		tpT = fpT = fnT = 0;

		int branchesSize = 0;
		if (mappings.size() > 0) {
			branchesSize = mappings.get(0).getBranches().size();
		}

		tp = initArray(tp, branchesSize);
		fp = initArray(fp, branchesSize);
		fn = initArray(fn, branchesSize);

		precision = initArray(precision, branchesSize);
		recall = initArray(recall, branchesSize);

		f1 = initArray(f1, branchesSize);
		f2 = initArray(f2, branchesSize);

		Jaccard = initArray(Jaccard, branchesSize);

		AveP = initArray(AveP, branchesSize);
		RP = initArray(RP, branchesSize);

		DCG = initArray(DCG, branchesSize);
		DCGa = initArray(DCGa, branchesSize);

		size = initArray(size, branchesSize);

		date = new Date();

		List<List<BenchmarkRow>> results = new ArrayList<>();

		for (int i = 0; i < queries.size(); ++i) {
			List<BenchmarkRow> rows = new ArrayList<>();

			Query query = queries.get(i);
			Mapping mapping = mappings.get(i);

			for (int j = 0; j < mapping.getBranches().size(); ++j) {
				Branch branch = mapping.getBranches().get(j);

				long queryMatchesSize;
				if (query.getMatches() != null) {
					queryMatchesSize = query.getMatches().stream().filter(m -> m.getBranch() == branch).count();
				} else {
					queryMatchesSize = 0;
				}

				if (queryMatchesSize > 0) {
					++size[j];
				}

				int tpC = 0, fpC = 0, fnC = 0;
				double DCG_k = 0, IDCG_k = 0, DCGa_k = 0, IDCGa_k = 0;

				for (int k = 0; k < mapping.getMatchesSize(branch); ++k) {
					Match match = mapping.getMatch(branch, k);

					boolean found = false;
					if (query.getMatches() != null) {
						for (EdamUri queryMatch : query.getMatches()) {
							if (match.getEdamUri().equals(queryMatch)) {
								found = true;
								break;
							}
						}
					}

					if (found) {
						rows.add(new BenchmarkRow(ResultType.tp, match));

						if (queryMatchesSize > 0) {
							tpC++;

							double precisionAve = tpC / (double)(tpC + fpC);
							AveP[j] += precisionAve / (double)queryMatchesSize;

							if (k < queryMatchesSize) {
								RP[j] += 1 / (double)queryMatchesSize;
							}

							int rel = 1;
							if (k == 0) {
								DCG_k += rel;
							} else {
								DCG_k += rel / (Math.log(k + 1) / Math.log(2));
							}
							DCGa_k += (Math.pow(2, rel) - 1) / (Math.log(k + 1 + 1) / Math.log(2));
						}
					} else {
						rows.add(new BenchmarkRow(ResultType.fp, match));

						if (queryMatchesSize > 0) {
							fpC++;
						}
					}

					if (queryMatchesSize > 0) {
						int Mrel = ((queryMatchesSize - k <= 0) ? 0 : 1);
						if (k == 0) {
							IDCG_k += Mrel;
						} else {
							IDCG_k += Mrel / (Math.log(k + 1) / Math.log(2));
						}
						IDCGa_k += (Math.pow(2, Mrel) - 1) / (Math.log(k + 1 + 1) / Math.log(2));
					}
				}

				if (query.getMatches() != null) {
					for (EdamUri queryMatch : query.getMatches()) {
						if (queryMatch.getBranch() == branch) {
							boolean found = false;
							for (int k = 0; k < mapping.getMatchesSize(branch); ++k) {
								if (queryMatch.equals(mapping.getMatch(branch, k).getEdamUri())) {
									found = true;
									break;
								}
							}
							if (!found) {
								rows.add(new BenchmarkRow(queryMatch));
	
								if (queryMatchesSize > 0) {
									fnC++;
								}
							}
						}
					}
				}

				if (j < mapping.getBranches().size() - 1) {
					rows.add(new BenchmarkRow());
				}

				if (queryMatchesSize > 0) {
					tpT += tpC;
					fpT += fpC;
					fnT += fnC;
					tp[j] += tpC;
					fp[j] += fpC;
					fn[j] += fnC;
					double precisionC = tpC / (double)(tpC + fpC);
					double recallC = tpC / (double)(tpC + fnC);
					precision[j] += precisionC;
					recall[j] += recallC;
					if (tpC > 0) {
						f1[j] += 2 * (precisionC * recallC) / (precisionC + recallC);
						f2[j] += (1 + Math.pow(2, 2)) * (precisionC * recallC) / ((Math.pow(2, 2) * precisionC) + recallC);
					}
					Jaccard[j] += tpC / (double)(tpC + fpC + fnC);
					DCG[j] += DCG_k / IDCG_k;
					DCGa[j] += DCGa_k / IDCGa_k;
				} else {
					fpT += mapping.getMatchesSize(branch);
				}
			}

			results.add(rows);
		}

		averageElements(precision, size);
		averageElements(recall, size);
		averageElements(f1, size);
		averageElements(f2, size);
		averageElements(Jaccard, size);
		averageElements(AveP, size);
		averageElements(RP, size);
		averageElements(DCG, size);
		averageElements(DCGa, size);

		date.setTime(System.currentTimeMillis());

		return results;
	}

	private static void out(Args args, Writer writer, Map<EdamUri, Concept> concepts, List<Query> queries, List<List<BenchmarkRow>> results) throws IOException {
		Common.writePreamble(true, args, writer, date);

		writer.write("<h2>Total</h2>\n");
		writer.write("<dl>\n");
		Common.writeVarVal(writer, "Total EDAM concepts", concepts.size());
		Common.writeVarVal(writer, "Total query keywords", queries.size());
		Common.writeVarVal(writer, "Total mapper answers", results.size());
		Common.writeVarVal(writer, "Total matches", addArrays(addArrays(tp, fp), fn), tpT + fpT + fnT);
		Common.writeVarVal(writer, "Total query matches", addArrays(tp, fn), tpT + fnT);
		Common.writeVarVal(writer, "Total mapper matches", addArrays(tp, fp), tpT + fpT);
		Common.writeVarVal(writer, "https://en.wikipedia.org/wiki/Type_I_and_type_II_errors", "Total mistakes", addArrays(fp, fn), fpT + fnT);
		Common.writeVarVal(writer, "https://en.wikipedia.org/wiki/Type_I_and_type_II_errors", "Total TP", tp, tpT);
		Common.writeVarVal(writer, "https://en.wikipedia.org/wiki/Type_I_and_type_II_errors", "Total FP", fp, fpT);
		Common.writeVarVal(writer, "https://en.wikipedia.org/wiki/Type_I_and_type_II_errors", "Total FN", fn, fnT);
		writer.write("</dl>\n");
		writer.write("\n");

		writer.write("<h2>Mean</h2>\n");
		writer.write("<dl>\n");
		Common.writeVarVal(writer, "https://en.wikipedia.org/wiki/Precision_and_recall#F-measure", "Precision", precision);
		Common.writeVarVal(writer, "https://en.wikipedia.org/wiki/Precision_and_recall#F-measure", "Recall", recall);
		Common.writeVarVal(writer, "https://en.wikipedia.org/wiki/F1_score", "F1 score", f1);
		Common.writeVarVal(writer, "https://en.wikipedia.org/wiki/F1_score", "F2 score", f2);
		Common.writeVarVal(writer, "https://en.wikipedia.org/wiki/Jaccard_index", "Jaccard index", Jaccard);
		Common.writeVarVal(writer, "https://en.wikipedia.org/wiki/Information_retrieval#Average_precision", "Average precision", AveP);
		Common.writeVarVal(writer, "https://en.wikipedia.org/wiki/Information_retrieval#R-Precision", "R-Precision", RP);
		Common.writeVarVal(writer, "https://en.wikipedia.org/wiki/Discounted_cumulative_gain", "Discounted cumulative gain", DCG);
		Common.writeVarVal(writer, "https://en.wikipedia.org/wiki/Discounted_cumulative_gain", "DCG (alternative)", DCGa);
		writer.write("</dl>\n");
		writer.write("\n");

		writer.write("<h2>Table</h2>\n");
		writer.write("<table>\n");
		writer.write("\n");
		writer.write("<colgroup>\n");

		if (args.getType() == IOType.SEQwiki) {
			writer.write("<col style=\"width:49%\">\n");
			writer.write("<col style=\"width:12%\">\n");
			writer.write("<col style=\"width:12%\">\n");
			writer.write("<col style=\"width:12%\">\n");
		} else {
			writer.write("<col style=\"width:25%\">\n");
			writer.write("<col style=\"width:20%\">\n");
			writer.write("<col style=\"width:20%\">\n");
			writer.write("<col style=\"width:20%\">\n");
		}

		writer.write("<col style=\"width:10%\">\n");
		writer.write("<col style=\"width:5%\">\n");
		writer.write("</colgroup>\n");
		writer.write("\n");

		writer.write("<thead>\n<tr>\n");
		writer.write("<th>Query</th>\n<th>TP</th>\n<th>FP</th>\n<th>FN</th>\n<th>Match Type</th>\n<th>Score</th>\n");
		writer.write("</tr>\n</thead>\n\n");

		writer.write("<tbody>\n\n");
		for (int i = 0; i < queries.size(); ++i) {
			Query query = queries.get(i);
			List<BenchmarkRow> rows = results.get(i);

			Common.writeQuery(writer, query, rows.size() + 1, args.getType());

			for (int j = 0; j < rows.size(); ++j) {
				BenchmarkRow row = rows.get(j);

				if (row.resultType == ResultType.empty) {
					writer.write("<tr class=\"sep-branch\">\n");
				} else if (row.resultType == ResultType.fn) {
					Common.writeTr(writer, row.edamUri);
				} else {
					Common.writeTr(writer, row.match.getEdamUri());
				}

				if (row.resultType == ResultType.empty) {
					writer.write("<td colspan=\"5\"></td>\n");
				} else if (row.resultType == ResultType.fn) {
					Concept concept = concepts.get(row.edamUri);

					writer.write("<td>&nbsp;</td>\n");
					writer.write("<td>&nbsp;</td>\n");

					Common.writeMatch(writer, null, row.edamUri, concept, row.resultType.toString());

					writer.write("<td>&nbsp;</td>\n");
					writer.write("<td>&nbsp;</td>\n");
				} else {
					Concept concept = concepts.get(row.match.getEdamUri());

					if (row.resultType == ResultType.fp) writer.write("<td>&nbsp;</td>\n");

					Common.writeMatch(writer, row.match, row.match.getEdamUri(), concept, row.resultType.toString());

					if (row.resultType == ResultType.tp) writer.write("<td>&nbsp;</td>\n");

					writer.write("<td>&nbsp;</td>\n");

					Common.writeMatchType(writer, row.match);

					Common.writeScore(writer, row.match);
				}

				writer.write("</tr>\n\n");
			}

			if (i < queries.size() - 1) {
				writer.write("<tr class=\"sep\"><td colspan=\"6\">&nbsp;</td></tr>\n\n");
			}
		}
		writer.write("</tbody>\n\n");

		writer.write("<tfoot>\n<tr>\n");
		writer.write("<td>" + queries.size() + "</td>\n");
		writer.write("<td>" + tpT + "</td>\n");
		writer.write("<td>" + fpT + "</td>\n");
		writer.write("<td>" + fnT + "</td>\n");
		writer.write("<td>&nbsp;</td>\n");
		writer.write("<td>&nbsp;</td>\n");
		writer.write("</tr>\n</tfoot>\n\n");

		writer.write("</table>\n\n");
		writer.write("</body>\n");
		writer.write("</html>\n");
	}

	static void output(Args args, Path benchmarkReport, Map<EdamUri, Concept> concepts, List<Query> queries, List<Mapping> mappings) throws IOException {
		if (benchmarkReport != null) {
			try (BufferedWriter writer = Files.newBufferedWriter(benchmarkReport, StandardCharsets.UTF_8)) {
				List<List<BenchmarkRow>> results = calculate(queries, mappings);
				out(args, writer, concepts, queries, results);
			}
		}
	}
}
