package mapper.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ontology.OntModel;

import mapper.cli.Args;

public class Benchmark {

	private Map<String, Keyword> keywords;
	private Map<String, List<ComparisonResult>> map;
	private OntModel model;
	private Args args;

	private int tp = 0, fp = 0, fn = 0;
	private double precision = 0, recall = 0;
	private double f1 = 0, f2 = 0;
	private double Jaccard = 0;
	private double AveP = 0, RP = 0;
	private double DCG = 0, DCGa = 0;
	private List<BenchmarkRow> benchmarkRows = new ArrayList<>();
	private Date date = new Date();

	public Benchmark(Map<String, Keyword> keywords, Map<String, List<ComparisonResult>> map, OntModel model, Args args) {
		this.keywords = keywords;
		this.map = map;
		this.model = model;
		this.args = args;
	}

	public void calculate() {
		tp = fp = fn = 0;
		precision = recall = 0;
		f1 = f2 = 0;
		Jaccard = 0;
		AveP = RP = 0;
		DCG = DCGa = 0;
		benchmarkRows.clear();

		// Brute-force algorithm, could possibly be improved if performance becomes an issue
		for (Map.Entry<String, Keyword> keywordEntry : keywords.entrySet()) {

			List<ComparisonResult> comparisonResultsList = map.get(keywordEntry.getKey());
			// Temporary hack to avoid comparisonResults with equal uri,
			// in case of such results only the best one is kept
			Set<ComparisonResult> comparisonResults = new LinkedHashSet<>();
			for (ComparisonResult comparisonResultList : comparisonResultsList) {
				boolean found = false;
				for (ComparisonResult comparisonResult : comparisonResults) {
					if (comparisonResultList.getUri().equals(comparisonResult.getUri())) {
						found = true;
						break;
					}
				}
				if (!found) {
					comparisonResults.add(comparisonResultList);
				}
			}

			int tpC = 0, fpC = 0, fnC = 0;
			int matchesSize = keywordEntry.getValue().getMatches().size();
			double DCG_i = 0, IDCG_i = 0, DCGa_i = 0, IDCGa_i = 0;

			int i = 0;
			for (ComparisonResult comparisonResult : comparisonResults) {
				i++;

				boolean found = false;
				for (EdamUri match : keywordEntry.getValue().getMatches()) {
					if (comparisonResult.getMatch().equals(match)) {
						found = true;
						break;
					}
				}

				if (found) {
					tpC++;
					benchmarkRows.add(new BenchmarkRow(keywordEntry.getValue(), comparisonResult, model, BenchmarkRow.Result.TP));

					double precisionAve = tpC / (double)(tpC + fpC);
					AveP += precisionAve / (double)matchesSize;

					if (i <= matchesSize) {
						RP += 1 / (double)matchesSize;
					}

					int rel = 1;
					if (i == 1) {
						DCG_i += rel;
					} else {
						DCG_i += rel / (Math.log(i) / Math.log(2));
					}
					DCGa_i += (Math.pow(2, rel) - 1) / (Math.log(i + 1) / Math.log(2));
				} else {
					fpC++;
					benchmarkRows.add(new BenchmarkRow(keywordEntry.getValue(), comparisonResult, model, BenchmarkRow.Result.FP));
				}

				int Irel = ((matchesSize - i < 0) ? 0 : 1);
				if (i == 1) {
					IDCG_i += Irel;
				} else {
					IDCG_i += Irel / (Math.log(i) / Math.log(2));
				}
				IDCGa_i += (Math.pow(2, Irel) - 1) / (Math.log(i + 1) / Math.log(2));
			}

			for (EdamUri match : keywordEntry.getValue().getMatches()) {
				boolean found = false;
				for (ComparisonResult comparisonResult : comparisonResults) {
					if (match.equals(comparisonResult.getMatch())) {
						found = true;
						break;
					}
				}
				if (!found) {
					fnC++;
					benchmarkRows.add(new BenchmarkRow(keywordEntry.getValue(), model, match));
				}
			}

			tp += tpC;
			fp += fpC;
			fn += fnC;
			double precisionC = tpC / (double)(tpC + fpC);
			double recallC = tpC / (double)(tpC + fnC);
			precision += precisionC;
			recall += recallC;
			if (tpC > 0) {
				f1 += 2 * (precisionC * recallC) / (precisionC + recallC);
				f2 += (1 + Math.pow(2, 2)) * (precisionC * recallC) / ((Math.pow(2, 2) * precisionC) + recallC);
			}
			Jaccard += tpC / (double)(tpC + fpC + fnC);
			DCG += DCG_i / IDCG_i;
			DCGa += DCGa_i / IDCGa_i;
		}

		precision /= (double)keywords.size();
		recall /= (double)keywords.size();
		f1 /= (double)keywords.size();
		f2 /= (double)keywords.size();
		Jaccard /= (double)keywords.size();
		AveP /= (double)keywords.size();
		RP /= (double)keywords.size();
		DCG /= (double)keywords.size();
		DCGa /= (double)keywords.size();

		date.setTime(System.currentTimeMillis());
	}

	private String percent(double val) {
		return String.format(Locale.ROOT, "%.2f%%", val * 100);
	}

	private void writeVarVal(Writer writer, String var, String val) throws IOException {
		writer.write("<dt>" + var + "</dt>\n");
		writer.write("<dd>" + val + "</dd>\n");
	}

	private void writeVarVal(Writer writer, String href, String var, String val) throws IOException {
		writer.write("<dt><a href=\"" + href + "\">" + var + "</a></dt>\n");
		writer.write("<dd>" + val + "</dd>\n");
	}

	private void writeVarVar(Writer writer, String var, int val) throws IOException {
		writer.write("<dt>" + var + "</dt>\n");
		writer.write("<dd>" + val + "</dd>\n");
	}

	private void writeVarVar(Writer writer, String var, int val, double valp) throws IOException {
		writer.write("<dt>" + var + "</dt>\n");
		writer.write("<dd>" + val + " (" + percent(valp) + ")</dd>\n");
	}

	private void writeVarVar(Writer writer, String href, String var, int val, double valp) throws IOException {
		writer.write("<dt><a href=\"" + href + "\">" + var + "</a></dt>\n");
		writer.write("<dd>" + val + " (" + percent(valp) + ")</dd>\n");
	}

	private void writeVarVar(Writer writer, String href, String var, double val) throws IOException {
		writer.write("<dt><a href=\"" + href + "\">" + var + "</a></dt>\n");
		writer.write("<dd>" + percent(val) + "</dd>\n");
	}

	private String getStyle() {
		return
		"dt { float:left; clear: left; text-align: right; width: 13em }\n" +
		"dt, dt a { color: blue }\n" +
		"dt:after { content: \":\" }\n" +
		"dd { margin-left: 13em; padding-left: 0.5em }\n" +
		"table { border-collapse: separate; border-spacing: 0px 2px }\n" +
		"tfoot { text-align: center; font-weight: bold }\n" +
		"thead th { border-bottom: 2px solid black }\n" +
		"tbody :last-child td { border-bottom: 2px solid black }\n" +
		"tfoot td { border-top: 2px solid black }\n" +
		"a { text-decoration: none; color: black }\n" +
		"a:hover, a:active { text-decoration: underline }\n" +
		".separator { height: 4em }\n" +
		".topic td ~ td { background-color: rgba(222,184,135,0.5) } /* BurlyWood */\n" +
		".topic:hover td ~ td { background-color: rgba(222,184,135,1) } /* BurlyWood */\n" +
		".operation td ~ td { background-color: rgba(211,211,211,0.5) } /* LightGray */\n" +
		".operation:hover td ~ td { background-color: rgba(211,211,211,1) } /* LightGray */\n" +
		".data td ~ td { background-color: rgba(135,206,235,0.5) } /* SkyBlue */\n" +
		".data:hover td ~ td { background-color: rgba(135,206,235,1) } /* SkyBlue */\n" +
		".format td ~ td { background-color: rgba(216,191,216,0.5) } /* Thistle */\n" +
		".format:hover td ~ td { background-color: rgba(216,191,216,1) } /* Thistle */\n" +
		".query { border-top: 2px solid black; font-weight: bold }\n" +
		".query ~ td { border-top: 2px solid black }\n" +
		"tr td.TP, tr:hover td.TP { background-color: green }\n" +
		"tr td.TP a { color: white }\n" +
		"tr td.FP, tr:hover td.FP { background-color: yellow }\n" +
		"tr td.FN, tr:hover td.FN { background-color: red }\n" +
		".score-exact, .score-big, .score-small { text-align: right; font-weight: bold }\n" +
		"tr td.score-exact { color: green }\n" +
		"tr td.score-big { color: yellow }\n" +
		"tr td.score-small { color: red }\n";
	}

	public void generateReport(String file) {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file), StandardCharsets.UTF_8)) {
			writer.write("<!DOCTYPE html>\n");
			writer.write("<html>\n");
			writer.write("\n");
			writer.write("<head>\n");
			writer.write("<meta charset=\"UTF-8\">\n");
			writer.write("<title>Benchmark report</title>\n");
			writer.write("<style type=\"text/css\">\n");
			writer.write(getStyle());
			writer.write("</style>\n");
			writer.write("</head>\n");
			writer.write("\n");
			writer.write("<body>\n");
			writer.write("<h1>Benchmark report</h1>\n");
			writer.write("<p>" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date) + "</p>\n");
			writer.write("\n");
			writer.write("<h2>Arguments</h2>\n");
			writer.write("<dl>\n");
			writeVarVal(writer, "Query file", new File(args.getFiles().get(0)).getName());
			writeVarVal(writer, "https://github.com/edamontology/edamontology/tree/master/releases", "Ontology file", new File(args.getFiles().get(1)).getName());
			writeVarVal(writer, "Output file", new File(args.getOutput()).getName());
			if (args.getBranches().isEmpty()) {
				writeVarVal(writer, "http://edamontology.org/page#Scope", "Branches", "[topic, operation, data, format]");
			} else {
				writeVarVal(writer, "http://edamontology.org/page#Scope", "Branches", args.getBranches().toString());
			}
			writeVarVar(writer, "Top matches per branch", args.getMatch());
			writer.write("</dl>\n");
			writer.write("\n");
			writer.write("<h2>Total</h2>\n");
			writer.write("<dl>\n");
			writeVarVar(writer, "Total query keywords", keywords.size());
			writeVarVar(writer, "Total mapper answers", map.size());
			int matches = tp + fp + fn;
			writeVarVar(writer, "Total matches", matches);
			writeVarVar(writer, "Total mapper matches", tp + fp, (tp + fp) / (double)matches);
			writeVarVar(writer, "Total query matches", tp + fn, (tp + fn) / (double)matches);
			writeVarVar(writer, "https://en.wikipedia.org/wiki/Type_I_and_type_II_errors", "Total mistakes", fp + fn, (fp + fn) / (double)matches);
			writeVarVar(writer, "https://en.wikipedia.org/wiki/Type_I_and_type_II_errors", "Total TP", tp, tp / (double)matches);
			writeVarVar(writer, "https://en.wikipedia.org/wiki/Type_I_and_type_II_errors", "Total FP", fp, fp / (double)matches);
			writeVarVar(writer, "https://en.wikipedia.org/wiki/Type_I_and_type_II_errors", "Total FN", fn, fn / (double)matches);
			writer.write("</dl>\n");
			writer.write("\n");
			writer.write("<h2>Mean</h2>\n");
			writer.write("<dl>\n");
			writeVarVar(writer, "https://en.wikipedia.org/wiki/Precision_and_recall#F-measure", "Precision", precision);
			writeVarVar(writer, "https://en.wikipedia.org/wiki/Precision_and_recall#F-measure", "Recall", recall);
			writeVarVar(writer, "https://en.wikipedia.org/wiki/F1_score", "F1 score", f1);
			writeVarVar(writer, "https://en.wikipedia.org/wiki/F1_score", "F2 score", f2);
			writeVarVar(writer, "https://en.wikipedia.org/wiki/Jaccard_index", "Jaccard index", Jaccard);
			writeVarVar(writer, "https://en.wikipedia.org/wiki/Information_retrieval#Average_precision", "Average precision", AveP);
			writeVarVar(writer, "https://en.wikipedia.org/wiki/Information_retrieval#R-Precision", "R-Precision", RP);
			writeVarVar(writer, "https://en.wikipedia.org/wiki/Discounted_cumulative_gain", "Discounted cumulative gain", DCG);
			writeVarVar(writer, "https://en.wikipedia.org/wiki/Discounted_cumulative_gain", "DCG (alternative)", DCGa);
			writer.write("</dl>\n");
			writer.write("\n");
			writer.write("<h2>Table</h2>\n");
			writer.write("<table>\n");
			writer.write("\n");
			writer.write("<colgroup>\n");
			writer.write("<col style=\"width:20%\">\n");
			writer.write("<col style=\"width:25%\">\n");
			writer.write("<col style=\"width:25%\">\n");
			writer.write("<col style=\"width:25%\">\n");
			writer.write("<col style=\"width:5%\">\n");
			writer.write("</colgroup>\n");
			writer.write("\n");
			writer.write("<thead>\n<tr>\n");
			writer.write("<th>Query</th>\n<th>TP</th>\n<th>FP</th>\n<th>FN</th>\n<th>Score</th>\n");
			writer.write("</tr>\n</thead>\n\n");
			writer.write("<tbody>\n\n");
			String previousKeyword = "";
			for (BenchmarkRow benchmarkRow : benchmarkRows) {
				if (!previousKeyword.equals(benchmarkRow.getKeyword()) && !previousKeyword.isEmpty()) {
					writer.write("<tr class=\"separator\">\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n</tr>\n\n");
				}
				writer.write("<tr class=\"" + benchmarkRow.getMatch().getBranch() + "\" ");
				writer.write("title=\"" + benchmarkRow.getMatch().getBranch() + "_" + benchmarkRow.getMatch().getNr() + "\">\n");
				if (!previousKeyword.equals(benchmarkRow.getKeyword())) {
					writer.write("<td class=\"query\"><a href=\"" + benchmarkRow.getUrl() + "\">" + benchmarkRow.getKeyword() + "</a></td>\n");
					previousKeyword = benchmarkRow.getKeyword();
				} else {
					writer.write("<td>&nbsp;</td>\n");
				}
				if (benchmarkRow.getResult() == BenchmarkRow.Result.FP || benchmarkRow.getResult() == BenchmarkRow.Result.FN) {
					writer.write("<td>&nbsp;</td>\n");
				}
				if (benchmarkRow.getResult() == BenchmarkRow.Result.FN) {
					writer.write("<td>&nbsp;</td>\n");
				}
				writer.write("<td class=\"" + benchmarkRow.getResult() + "\">");
				writer.write("<a href=\"" + benchmarkRow.getMatch() + "\">" + benchmarkRow.getMatchLabel());
				if (!benchmarkRow.getMatchedString().isEmpty()) {
					writer.write(" (" + benchmarkRow.getMatchedString() + ")");
				}
				writer.write("</a></td>\n");
				if (benchmarkRow.getResult() == BenchmarkRow.Result.TP || benchmarkRow.getResult() == BenchmarkRow.Result.FP) {
					writer.write("<td>&nbsp;</td>\n");
				}
				if (benchmarkRow.getResult() == BenchmarkRow.Result.TP) {
					writer.write("<td>&nbsp;</td>\n");
				}
				if (benchmarkRow.getScore() >= 0) {
					if (benchmarkRow.getScore() == 1.0) writer.write("<td class=\"score-exact\">");
					else if (benchmarkRow.getScore() >= 0.5) writer.write("<td class=\"score-big\">");
					else writer.write("<td class=\"score-small\">");
					writer.write(percent(benchmarkRow.getScore()));
				} else {
					writer.write("<td>&nbsp;");
				}
				writer.write("</td>\n");
				writer.write("</tr>\n\n");
			}
			writer.write("</tbody>\n\n");
			writer.write("<tfoot>\n<tr>\n");
			writer.write("<td>" + keywords.size() + "</td>\n");
			writer.write("<td>" + tp + "</td>\n");
			writer.write("<td>" + fp + "</td>\n");
			writer.write("<td>" + fn + "</td>\n");
			writer.write("<td></td>\n");
			writer.write("</tr>\n</tfoot>\n\n");
			writer.write("</table>\n\n");
			writer.write("</body>\n");
			writer.write("</html>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
