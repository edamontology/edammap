package edammapper.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import edammapper.args.MainArgs;
import edammapper.edam.Branch;
import edammapper.edam.Concept;
import edammapper.edam.EdamUri;
import edammapper.fetching.Fetcher;
import edammapper.fetching.MeshTerm;
import edammapper.fetching.MinedTerm;
import edammapper.fetching.Publication;
import edammapper.mapping.ConceptMatchType;
import edammapper.mapping.MapperArgs;
import edammapper.mapping.Mapping;
import edammapper.mapping.Match;
import edammapper.mapping.QueryMatchType;
import edammapper.query.Keyword;
import edammapper.query.Query;

class Report {

	private enum ResultType {
		tp, fp, fn, empty
	}

	private static class ResultRow {
		private ResultType resultType;
		private Match match = null;
		private ResultRow() {
			this.resultType = ResultType.empty;
		}
		private ResultRow(ResultType resultType, Match match) {
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

	private static List<List<ResultRow>> calculate(List<Query> queries, List<Mapping> mappings) {
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

		List<List<ResultRow>> results = new ArrayList<>();

		for (int i = 0; i < queries.size(); ++i) {
			List<ResultRow> rows = new ArrayList<>();

			Query query = queries.get(i);
			Mapping mapping = mappings.get(i);

			for (int j = 0; j < mapping.getBranches().size(); ++j) {
				Branch branch = mapping.getBranches().get(j);

				long queryMatchesSize;
				if (query.getAnnotations() != null) {
					queryMatchesSize = query.getAnnotations().stream().filter(m -> m.getBranch() == branch).count();
				} else {
					queryMatchesSize = 0;
				}

				if (queryMatchesSize > 0) {
					++size[j];
				}

				int tpC = 0, fpC = 0, fnC = 0;
				double DCG_k = 0, IDCG_k = 0, DCGa_k = 0, IDCGa_k = 0;

				for (int k = 0; k < mapping.getMatches(branch).size(); ++k) {
					Match match = mapping.getMatches(branch).get(k);

					if (match.isExistingAnnotation()) {
						rows.add(new ResultRow(ResultType.tp, match));

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
						rows.add(new ResultRow(ResultType.fp, match));

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

				for (Match excludedAnnotation : mapping.getRemainingAnnotations(branch)) {
					rows.add(new ResultRow(ResultType.fn, excludedAnnotation));

					if (queryMatchesSize > 0) {
						fnC++;
					}
				}

				if (j < mapping.getBranches().size() - 1) {
					rows.add(new ResultRow());
				}

				if (queryMatchesSize > 0) {
					tpT += tpC;
					fpT += fpC;
					fnT += fnC;
					tp[j] += tpC;
					fp[j] += fpC;
					fn[j] += fnC;
					double precisionC = 0;
					if (tpC > 0 || fpC > 0) precisionC = tpC / (double)(tpC + fpC);
					double recallC = tpC / (double)(tpC + fnC);
					precision[j] += precisionC;
					recall[j] += recallC;
					if (tpC > 0) {
						f1[j] += 2 * (precisionC * recallC) / (precisionC + recallC);
						f2[j] += (1 + Math.pow(2, 2)) * (precisionC * recallC) / ((Math.pow(2, 2) * precisionC) + recallC);
					}
					Jaccard[j] += tpC / (double)(tpC + fpC + fnC);
					if (tpC > 0 || fpC > 0) {
						DCG[j] += DCG_k / IDCG_k;
						DCGa[j] += DCGa_k / IDCGa_k;
					}
				} else {
					fpT += mapping.getMatches(branch).size();
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


	private static String conceptMatchString(Match match, Concept concept) {
		switch (match.getConceptMatch().getType()) {
			case label: return concept.getLabel();
			case exact_synonym: return concept.getExactSynonyms().get(match.getConceptMatch().getSynonymIndex());
			case narrow_synonym: return concept.getNarrowSynonyms().get(match.getConceptMatch().getSynonymIndex());
			case broad_synonym: return concept.getBroadSynonyms().get(match.getConceptMatch().getSynonymIndex());
			case definition: return concept.getDefinition();
			case comment: return concept.getComment();
			default: return "";
		}
	}

	private static double averageArray(double[] a) {
		//return Arrays.stream(a).average().orElse(0);
		double numerator = 0, denominator = 0;
		for (double e : a) {
			if (!Double.isNaN(e)) {
				numerator += e;
				++denominator;
			}
		}
		if (denominator > 0) return numerator / denominator;
		else return 0;
	}

	private static String percent(double val) {
		return String.format(Locale.ROOT, "%.2f%%", val * 100);
	}

	private static void writeVar(Writer writer, String var) throws IOException {
		writer.write("<dt>" + var + "</dt>\n");
	}

	private static void writeVar(Writer writer, String href, String var) throws IOException {
		writer.write("<dt><a href=\"" + href + "\">" + var + "</a></dt>\n");
	}

	private static void writeVal(Writer writer, String val) throws IOException {
		writer.write("<dd>" + val + "</dd>\n");
	}

	private static void writeVal(Writer writer, int val) throws IOException {
		writer.write("<dd>" + val + "</dd>\n");
	}

	private static void writeVal(Writer writer, boolean val) throws IOException {
		writer.write("<dd>" + val + "</dd>\n");
	}

	private static void writeVal(Writer writer, double val) throws IOException {
		writer.write("<dd>" + val + "</dd>\n");
	}

	private static void writeVal(Writer writer, int[] a, int val) throws IOException {
		writer.write("<dd>[");
		for (int i = 0; i < a.length; ++i) {
			writer.write(String.valueOf(a[i]));
			if (i < a.length - 1) {
				writer.write(", ");
			}
		}
		writer.write("] (" + val + ")</dd>");
	}

	private static void writeVal(Writer writer, double[] a) throws IOException {
		writer.write("<dd>[");
		for (int i = 0; i < a.length; ++i) {
			writer.write(percent(a[i]));
			if (i < a.length - 1) {
				writer.write(", ");
			}
		}
		writer.write("] (" + percent(averageArray(a)) + ")</dd>");
	}

	// TODO temp
	private static void writeVal(double[] a) {
		System.err.print("[");
		for (int i = 0; i < a.length; ++i) {
			System.err.print(percent(a[i]));
			if (i < a.length - 1) {
				System.err.print(", ");
			}
		}
		System.err.println("] (" + percent(averageArray(a)) + ")");
	}

	private static void writeVarVal(Writer writer, String var, String val) throws IOException {
		writeVar(writer, var);
		writeVal(writer, val);
	}

	private static void writeVarVal(Writer writer, String href, String var, String val) throws IOException {
		writeVar(writer, href, var);
		writeVal(writer, val);
	}

	private static void writeVarVal(Writer writer, String var, int val) throws IOException {
		writeVar(writer, var);
		writeVal(writer, val);
	}

	private static void writeVarVal(Writer writer, String var, boolean val) throws IOException {
		writeVar(writer, var);
		writeVal(writer, val);
	}

	private static void writeVarVal(Writer writer, String var, double val) throws IOException {
		writeVar(writer, var);
		writeVal(writer, val);
	}

	private static void writeVarVal(Writer writer, String var, int[] a, int val) throws IOException {
		writeVar(writer, var);
		writeVal(writer, a, val);
	}

	private static void writeVarVal(Writer writer, String href, String var, int[] a, int val) throws IOException {
		writeVar(writer, href, var);
		writeVal(writer, a, val);
	}

	private static void writeVarVal(Writer writer, String href, String var, double[] a) throws IOException {
		writeVar(writer, href, var);
		writeVal(writer, a);
	}

	private static String getLinkHtml(String link) {
		return "<a href=\"" + link + "\">" + link + "</a>";
	}

	private static String getPublicationLinkHtml(String publicationId) {
		if (publicationId == null) return "";
		if (Fetcher.isDoi(publicationId)) {
			publicationId = Fetcher.normalizeDoi(publicationId);
		}
		String link = Fetcher.getPublicationLink(publicationId);
		if (link == null) {
			return publicationId;
		} else {
			return "<a href=\"" + link + "\">" + publicationId + "</a>";
		}
	}

	private static String getMeshLinkHtml(MeshTerm mesh) {
		String s = "";
		String link = Fetcher.getMeshLink(mesh);
		if (link != null) s += "<a href=\"" + link + "\">";
		s += (mesh.getTerm() == null ? "NA" : mesh.getTerm());
		if (link != null) s += "</a>";
		return s;
	}

	private static String getMinedLinkHtml(MinedTerm mined) {
		String s = "";
		String link = Fetcher.getMinedLink(mined);
		if (link != null) s += "<a href=\"" + link + "\">";
		s += (mined.getTerm() == null ? "NA" : mined.getTerm());
		if (link != null) s += "</a>";
		if (mined.getCount() != 0) s += " <small>" + mined.getCount() + "</small>";
		return s;
	}

	private static String getStyle() {
		return
		"a { text-decoration: none; color: black }\n" +
		"a:hover, a:active { text-decoration: underline }\n" +
		"dt { float:left; clear: left; text-align: right; width: 16em }\n" +
		"dt, dt a { color: blue }\n" +
		"dt:after { content: \":\" }\n" +
		"dd { margin-left: 16em; padding-left: 0.5em }\n" +
		"table { border-collapse: separate; border-spacing: 0px 0px; table-layout: fixed; width: 100% }\n" +
		"thead, tfoot { text-align: center; font-weight: bold }\n" +
		"thead th { border-bottom: 2px solid black }\n" +
		"tfoot td { border-top: 3px solid black }\n" +
		"td { word-wrap: break-word }\n" +
		"h3 { margin-top: 0 }\n" +
		"h4 { margin-bottom: 0; text-decoration: overline }\n" +
		"#legend { width: 13em }\n" +
		".query td { border-top: 1px solid black; vertical-align: top; padding-right: 1em; border-right: 1px dotted black }\n" +
		".sep { height: 3em }\n" +
		".sep td { border-bottom: 1px solid black }\n" +
		".sep-branch { height: 1em }\n" +
		".row td { border-top: 1px solid black; padding: 0.1em 0.2em }\n" +
		".obsolete { text-decoration: line-through }\n" +
		".topic td, .Domain { background-color: rgba(222,184,135,0.5) } /* BurlyWood */\n" +
		".topic:hover td, .Domain:hover { background-color: rgba(222,184,135,1) } /* BurlyWood */\n" +
		".operation td, .Method { background-color: rgba(211,211,211,0.5) } /* LightGray */\n" +
		".operation:hover td, .Method:hover { background-color: rgba(211,211,211,1) } /* LightGray */\n" +
		".data td { background-color: rgba(135,206,235,0.5) } /* SkyBlue */\n" +
		".data:hover td { background-color: rgba(135,206,235,1) } /* SkyBlue */\n" +
		".format td { background-color: rgba(216,191,216,0.5) } /* Thistle */\n" +
		".format:hover td { background-color: rgba(216,191,216,1) } /* Thistle */\n" +
		".pc, .pc a { color: #666 }\n" +
		".pc a { text-decoration: underline }\n" +
		".type, .type a { text-align: center; color: #333 }\n" +
		".score { text-align: right; font-weight: bold }\n" +
		"tr .good { color: rgba(0,128,0,1) }\n" +
		"tr .medium { color: rgba(192,192,0,1) }\n" +
		"tr:hover .medium { color: rgba(128,128,0,1) }\n" +
		"tr .bad { color: rgba(255,0,0,0.5) }\n" +
		"tr:hover .bad { color: rgba(255,0,0,1) }\n" +
		".exact { text-decoration: underline }\n" +
		".done { text-align: center }\n" +
		"tr .tp { background-color: rgba(0,128,0,0.5) }\n" +
		"tr:hover .tp { background-color: rgba(0,128,0,1) }\n" +
		"tr .fn { background-color: rgba(255,0,0,0.5) }\n" +
		"tr:hover .fn { background-color: rgba(255,0,0,1) }\n";
	}

	private static String getScript() {
		return
		"function hide(id) {\n" +
		"\tif (document.getElementById(id + \"b\").checked) {\n" +
		"\t\tdocument.getElementById(id).style.display = \"none\";\n" +
		"\t}\n" +
		"}\n";
	}

	private static void writePreamble(MainArgs args, Writer writer, Date date) throws IOException {
		writer.write("<!DOCTYPE html>\n");
		writer.write("<html>\n");
		writer.write("\n");

		writer.write("<head>\n");
		writer.write("<meta charset=\"UTF-8\">\n");
		writer.write("<title>Report</title>\n");
		writer.write("<style>\n");
		writer.write(getStyle());
		writer.write("</style>\n");
		writer.write("<script>\n");
		writer.write(getScript());
		writer.write("</script>\n");
		writer.write("</head>\n");
		writer.write("\n");

		writer.write("<body>\n");
		writer.write("<h1>Report</h1>\n");
		writer.write("<p>" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date) + "</p>\n");
		writer.write("\n");

		writer.write("<h2>Input/Output</h2>\n");
		writer.write("<dl>\n");
		writeVarVal(writer, "https://github.com/edamontology/edamontology/tree/master/releases", "Ontology file", new File(args.getEdam()).getName());
		writeVarVal(writer, "Query file", new File(args.getQuery()).getName());
		writeVarVal(writer, "Type", args.getType().toString());
		String outputFile = new File(args.getOutput()).getName();
		writeVarVal(writer, "Output file", (outputFile.isEmpty() ? "&nbsp;" : outputFile));
		String reportFile = new File(args.getReport()).getName();
		writeVarVal(writer, "Report file", (reportFile.isEmpty() ? "&nbsp;" : reportFile));
		writeVarVal(writer, "Number of threads", args.getThreads());
		writer.write("</dl>\n");
		writer.write("\n");

		writer.write("<h2>Preprocessing</h2>\n");
		writer.write("<dl>\n");
		writeVarVal(writer, "Remove freestanding numbers", args.getProcessorArgs().getPreProcessorArgs().isNumberRemove());
		writeVarVal(writer, "Stopword list", args.getProcessorArgs().getPreProcessorArgs().getStopwords().toString());
		writeVarVal(writer, "Do stemming", !args.getProcessorArgs().getPreProcessorArgs().isNoStemming());
		writeVarVal(writer, "Remove tokens of length", args.getProcessorArgs().getPreProcessorArgs().getShortWord());
		writer.write("</dl>\n");
		writer.write("\n");

		writer.write("<h2>Processing</h2>\n");
		writer.write("<dl>\n");
		writeVarVal(writer, "Fetching is disabled", args.getProcessorArgs().isFetchingDisabled());
		String databaseFile = new File(args.getProcessorArgs().getDatabase()).getName();
		writeVarVal(writer, "Database file", (databaseFile.isEmpty() ? "&nbsp;" : databaseFile));
		String queryIdfFile = new File(args.getProcessorArgs().getQueryIdf()).getName();
		writeVarVal(writer, "Query IDF file", (queryIdfFile.isEmpty() ? "&nbsp;" : queryIdfFile));
		writer.write("</dl>\n");
		writer.write("\n");

		writer.write("<h2>Mapping</h2>\n");
		writer.write("<dl>\n");
		writeVarVal(writer, "http://edamontology.org/page#Scope", "Branches", args.getMapperArgs().getBranches().toString());
		writeVarVal(writer, "Top matches per branch", args.getMapperArgs().getMatch());
		writeVarVal(writer, "Include obsolete concepts", args.getMapperArgs().getObsolete());
		writeVarVal(writer, "Matches with good scores", !args.getMapperArgs().isNoOutputGoodScores());
		writeVarVal(writer, "Matches with medium scores", !args.getMapperArgs().isNoOutputMediumScores());
		writeVarVal(writer, "Matches with bad scores", args.getMapperArgs().isOutputBadScores());
		writeVarVal(writer, "Exclude done annotations", args.getMapperArgs().isExcludeAnnotations());
		writeVarVal(writer, "Inferior parents &amp; children", args.getMapperArgs().isNoRemoveInferiorParentChild());
		writeVarVal(writer, "Include top level concepts", args.getMapperArgs().isNoRemoveTopLevel());
		writer.write("</dl>\n");
		writer.write("\n");

		writer.write("<h2>Mapping algorithm</h2>\n");
		writer.write("<dl>\n");
		writeVarVal(writer, "Compound words", args.getMapperArgs().getAlgorithmArgs().getCompoundWords());
		writeVarVal(writer, "Mismatch multiplier", args.getMapperArgs().getAlgorithmArgs().getMismatchMultiplier());
		writeVarVal(writer, "Match minimum", args.getMapperArgs().getAlgorithmArgs().getMatchMinimum());
		writeVarVal(writer, "Position off by 1", args.getMapperArgs().getAlgorithmArgs().getPositionOffBy1());
		writeVarVal(writer, "Position off by 2", args.getMapperArgs().getAlgorithmArgs().getPositionOffBy2());
		writeVarVal(writer, "Position match scaling", args.getMapperArgs().getAlgorithmArgs().getPositionMatchScaling());
		writeVarVal(writer, "Position loss", args.getMapperArgs().getAlgorithmArgs().getPositionLoss());
		writeVarVal(writer, "Score scaling", args.getMapperArgs().getAlgorithmArgs().getScoreScaling());
		writeVarVal(writer, "Concept weight", args.getMapperArgs().getAlgorithmArgs().getConceptWeight());
		writeVarVal(writer, "Query weight", args.getMapperArgs().getAlgorithmArgs().getQueryWeight());
		writeVarVal(writer, "Mapping strategy", args.getMapperArgs().getIdfMultiplierArgs().getMappingStrategy().toString());
		writeVarVal(writer, "Average strategy scaling", args.getMapperArgs().getIdfMultiplierArgs().getAverageScaling());
		writeVarVal(writer, "Parent weight", args.getMapperArgs().getAlgorithmArgs().getParentWeight());
		writeVarVal(writer, "Path weight", args.getMapperArgs().getAlgorithmArgs().getPathWeight());
		writer.write("</dl>\n");
		writer.write("\n");

		writer.write("<h2>IDF</h2>\n");
		writer.write("<dl>\n");
		writeVarVal(writer, "Concept IDF scaling", args.getMapperArgs().getIdfMultiplierArgs().getConceptIdfScaling());
		writeVarVal(writer, "Query IDF scaling", args.getMapperArgs().getIdfMultiplierArgs().getQueryIdfScaling());
		writeVarVal(writer, "Enable label/synonyms IDF", args.getMapperArgs().getIdfMultiplierArgs().isEnableLabelSynonymsIdf());
		writeVarVal(writer, "Disable name/keywords IDF", args.getMapperArgs().getIdfMultiplierArgs().isDisableNameKeywordsIdf());
		writeVarVal(writer, "Disable description IDF", args.getMapperArgs().getIdfMultiplierArgs().isDisableDescriptionIdf());
		writeVarVal(writer, "Disable title/keywords IDF", args.getMapperArgs().getIdfMultiplierArgs().isDisableTitleKeywordsIdf());
		writeVarVal(writer, "Disable abstract IDF", args.getMapperArgs().getIdfMultiplierArgs().isDisableAbstractIdf());
		writeVarVal(writer, "Disable query IDF branches", args.getMapperArgs().getIdfMultiplierArgs().getDisableQueryIdfBranches().toString());
		writer.write("</dl>\n");
		writer.write("\n");

		writer.write("<h2>Concept multipliers</h2>\n");
		writer.write("<dl>\n");
		writeVarVal(writer, "Label multiplier", args.getMapperArgs().getIdfMultiplierArgs().getLabelMultiplier());
		writeVarVal(writer, "Exact synonym multiplier", args.getMapperArgs().getIdfMultiplierArgs().getExactSynonymMultiplier());
		writeVarVal(writer, "Narrow/Broad multiplier", args.getMapperArgs().getIdfMultiplierArgs().getNarrowBroadMultiplier());
		writeVarVal(writer, "Definition multiplier", args.getMapperArgs().getIdfMultiplierArgs().getDefinitionMultiplier());
		writeVarVal(writer, "Comment multiplier", args.getMapperArgs().getIdfMultiplierArgs().getCommentMultiplier());
		writer.write("</dl>\n");
		writer.write("\n");

		writer.write("<h2>Query normalizers</h2>\n");
		writer.write("<dl>\n");
		writeVarVal(writer, "Name norm", args.getMapperArgs().getIdfMultiplierArgs().getNameNormalizer());
		writeVarVal(writer, "Webpage norm", args.getMapperArgs().getIdfMultiplierArgs().getWebpageNormalizer());
		writeVarVal(writer, "Description norm", args.getMapperArgs().getIdfMultiplierArgs().getDescriptionNormalizer());
		writeVarVal(writer, "Keyword norm", args.getMapperArgs().getIdfMultiplierArgs().getKeywordNormalizer());
		writeVarVal(writer, "Publication title norm", args.getMapperArgs().getIdfMultiplierArgs().getPublicationTitleNormalizer());
		writeVarVal(writer, "Publication keyword norm", args.getMapperArgs().getIdfMultiplierArgs().getPublicationKeywordNormalizer());
		writeVarVal(writer, "Publication MeSH norm", args.getMapperArgs().getIdfMultiplierArgs().getPublicationMeshNormalizer());
		writeVarVal(writer, "Publication EFO/GO norm", args.getMapperArgs().getIdfMultiplierArgs().getPublicationMinedNormalizer());
		writeVarVal(writer, "Publication abstract norm", args.getMapperArgs().getIdfMultiplierArgs().getPublicationAbstractNormalizer());
		writeVarVal(writer, "Publication fulltext norm", args.getMapperArgs().getIdfMultiplierArgs().getPublicationFulltextNormalizer());
		writeVarVal(writer, "Doc norm", args.getMapperArgs().getIdfMultiplierArgs().getDocNormalizer());
		writer.write("</dl>\n");
		writer.write("\n");

		writer.write("<h2>Query weights</h2>\n");
		writer.write("<dl>\n");
		writeVarVal(writer, "Name weight", args.getMapperArgs().getIdfMultiplierArgs().getNameWeight());
		writeVarVal(writer, "Webpage weight", args.getMapperArgs().getIdfMultiplierArgs().getWebpageWeight());
		writeVarVal(writer, "Description weight", args.getMapperArgs().getIdfMultiplierArgs().getDescriptionWeight());
		writeVarVal(writer, "Keyword weight", args.getMapperArgs().getIdfMultiplierArgs().getKeywordWeight());
		writeVarVal(writer, "Publication title weight", args.getMapperArgs().getIdfMultiplierArgs().getPublicationTitleWeight());
		writeVarVal(writer, "Publication keyword weight", args.getMapperArgs().getIdfMultiplierArgs().getPublicationKeywordWeight());
		writeVarVal(writer, "Publication MeSH weight", args.getMapperArgs().getIdfMultiplierArgs().getPublicationMeshWeight());
		writeVarVal(writer, "Publication EFO/GO weight", args.getMapperArgs().getIdfMultiplierArgs().getPublicationMinedWeight());
		writeVarVal(writer, "Publication abstract weight", args.getMapperArgs().getIdfMultiplierArgs().getPublicationAbstractWeight());
		writeVarVal(writer, "Publication fulltext weight", args.getMapperArgs().getIdfMultiplierArgs().getPublicationFulltextWeight());
		writeVarVal(writer, "Doc weight", args.getMapperArgs().getIdfMultiplierArgs().getDocWeight());
		writer.write("</dl>\n");
		writer.write("\n");

		writer.write("<h2>Score limits</h2>\n");
		writer.write("<dl>\n");
		writeVarVal(writer, "Good score for topic", args.getMapperArgs().getGoodScoreTopic());
		writeVarVal(writer, "Good score for operation", args.getMapperArgs().getGoodScoreOperation());
		writeVarVal(writer, "Good score for data", args.getMapperArgs().getGoodScoreData());
		writeVarVal(writer, "Good score for format", args.getMapperArgs().getGoodScoreFormat());
		writeVarVal(writer, "Bad score for topic", args.getMapperArgs().getBadScoreTopic());
		writeVarVal(writer, "Bad score for operation", args.getMapperArgs().getBadScoreOperation());
		writeVarVal(writer, "Bad score for data", args.getMapperArgs().getBadScoreData());
		writeVarVal(writer, "Bad score for format", args.getMapperArgs().getBadScoreFormat());
		writer.write("</dl>\n");
		writer.write("\n");
	}

	private static void writeLegend(Writer writer, MapperArgs args) throws IOException {
		writer.write("\n<table id=\"legend\">\n");
		for (Branch branch : args.getBranches()) {
			writer.write("<tr class=\"row " + branch + "\"><td>" + branch + "</td></tr>\n");
		}
		writer.write("<tr class=\"sep-branch\"><td></td></tr>\n");
		writer.write("</table>\n\n");
	}

	private static void writeQuery(Writer writer, Query query, List<Publication> publications, int rowspan, int id) throws IOException {
		writer.write("<tr class=\"query\">");
		writer.write("<td rowspan=\"" + rowspan + "\">\n");

		if (query.getName() != null || (query.getWebpageUrls() != null && !query.getWebpageUrls().isEmpty())) {
			writer.write("<h3>");

			writer.write("<input id=\"i" + id + "b\" type=\"checkbox\" onclick=\"hide('i" + id + "')\">");

			String webpageUrl = null;
			if (query.getWebpageUrls() != null && !query.getWebpageUrls().isEmpty()) {
				webpageUrl = query.getWebpageUrls().get(0);
			}

			if (webpageUrl != null) {
				writer.write("<a href=\"" + webpageUrl + "\">");
			}

			if (query.getName() != null) {
				writer.write(query.getName());
			} else if (webpageUrl != null) {
				writer.write(webpageUrl);
			}

			if (webpageUrl != null) {
				writer.write("</a>");
			}

			writer.write("</h3>\n");

			if (query.getWebpageUrls() != null) {
				for (int i = 1; i < query.getWebpageUrls().size(); ++i) {
					webpageUrl = query.getWebpageUrls().get(i);
					if (webpageUrl != null) {
						writer.write(getLinkHtml(webpageUrl) + "<br>\n");
					}
				}
			}

			// TODO if zero webpages, write "No homepage" (but sometimes we don't actually want any homepages ?)
		}

		if (query.getDescription() != null) {
			writer.write("<p>" + query.getDescription().replaceAll("\n", "<br>\n").replaceAll("<br>\n<br>\n", "</p>\n<p>") + "</p>\n");
		}

		if (query.getKeywords() != null && !query.getKeywords().isEmpty()) {
			Map<String, List<Keyword>> keywords = new LinkedHashMap<>();
			for (Keyword keyword : query.getKeywords()) {
				if (keywords.get(keyword.getType()) == null) {
					keywords.put(keyword.getType(), new ArrayList<>());
				}
				keywords.get(keyword.getType()).add(keyword);
			}
			for (Map.Entry<String, List<Keyword>> entry : keywords.entrySet()) {
				writer.write("<h4><span class=\"" + entry.getKey() + "\">" + entry.getKey() + "</span></h4>\n");
				writer.write(entry.getValue().stream()
					.map(k -> {
						if (k.getUrl() != null) {
							return "<a href=\"" + k.getUrl() + "\">" + k.getValue() + "</a>";
						} else {
							return k.getValue();
						}
					}).collect(Collectors.joining("; ")));
			}
			writer.write("\n");
		}

		for (int i = 0; i < publications.size(); ++i) {
			Publication publication = publications.get(i);
			if (publication == null) continue;

			writer.write("<h4>Publication ");
			writer.write(getPublicationLinkHtml(query.getPublicationIds().get(i))); // TODO why can't be out of bounds ?
			writer.write("</h4>\n");

			writer.write("<p><strong>Title:</strong> " + publication.getTitle() + "<p>\n");

			if (!publication.getKeywords().isEmpty()) {
				writer.write("<p><strong>Keywords:</strong> ");
				writer.write(publication.getKeywords().stream().collect(Collectors.joining("; ")));
				writer.write("</p>\n");
			}

			if (!publication.getMeshTerms().isEmpty()) {
				writer.write("<p><strong>MeSH terms:</strong> ");
				writer.write(publication.getMeshTerms().stream()
					.map(t -> getMeshLinkHtml(t))
					.collect(Collectors.joining("; ")));
				writer.write("</p>\n");
			}

			if (!publication.getEfoTerms().isEmpty()) {
				writer.write("<p><strong>EFO terms:</strong> ");
				writer.write(publication.getEfoTerms().stream()
					.map(t -> getMinedLinkHtml(t))
					.collect(Collectors.joining("; ")));
				writer.write("</p>\n");
			}

			if (!publication.getGoTerms().isEmpty()) {
				writer.write("<p><strong>GO terms:</strong> ");
				writer.write(publication.getGoTerms().stream()
					.map(t -> getMinedLinkHtml(t))
					.collect(Collectors.joining("; ")));
				writer.write("</p>\n");
			}

			if (!publication.getAbstract().isEmpty()) {
				writer.write("<p>" + publication.getAbstract().replaceAll("\n", "<br>\n").replaceAll("<br>\n<br>\n", "</p>\n<p>") + "</p>\n");
			}

			if (!publication.getFulltext().isEmpty()) {
				writer.write("<p><strong>Full text present</strong> (" + publication.getFulltext().length() + " characters)</p>\n");
			}
		}

		if (query.getDocUrls() != null && !query.getDocUrls().isEmpty()) {
			writer.write("<h4>Docs</h4>\n");
			for (String docUrl : query.getDocUrls()) {
				writer.write(getLinkHtml(docUrl) + "<br>\n");
			}
		}

		writer.write("</td></tr>\n\n");
	}

	private static void writeTr(Writer writer, EdamUri edamUri, int i, int j, int k, boolean excludedAnnotation) throws IOException {
		writer.write("<tr id=\"i" + i + "j" + j + "k" + k + "\"");
		writer.write(" class=\"row");
		if (!excludedAnnotation) {
			 writer.write(" " + edamUri.getBranch());
		}
		writer.write("\" title=\"" + edamUri.getBranch() + "_" + edamUri.getNrString() + "\">\n");
	}

	private static void writeParentsChildren(Writer writer, List<EdamUri> pc, String desc, Map<EdamUri, Concept> concepts) throws IOException {
		if (!pc.isEmpty()) {
			writer.write("<br>[" + desc + " ");
			writer.write(pc.stream()
				.map(a -> "<a href=\"" + a + "\">" + concepts.get(a).getLabel() + "</a>")
				.collect(Collectors.joining("; ")));
			writer.write("]");
		}
	}

	private static void writeMatch(Writer writer, Match match, Map<EdamUri, Concept> concepts, String clazz) throws IOException {
		EdamUri edamUri = match.getEdamUri();
		Concept concept = concepts.get(edamUri);
		writer.write("<td colspan=\"3\" class=\"" + clazz + "\"><a href=\"" + edamUri + "\"");
		if (concept == null) {
			writer.write(">" + edamUri); // TODO error
		} else {
			if (concept.isObsolete()) writer.write(" class=\"obsolete\"");
			writer.write(">" + concept.getLabel());
			if (match != null && match.getConceptMatch().getType() != ConceptMatchType.label && match.getConceptMatch().getType() != ConceptMatchType.none) {
				writer.write(" (" + conceptMatchString(match, concept) + ")");
			}
			writer.write("</a><span class=\"pc\">");
			writeParentsChildren(writer, match.getParents(), "Child of", concepts);
			writeParentsChildren(writer, match.getParentsAnnotation(), "Child of annotation", concepts);
			writeParentsChildren(writer, match.getParentsRemainingAnnotation(), "Child of excluded annotation", concepts);
			writeParentsChildren(writer, match.getChildren(), "Parent of", concepts);
			writeParentsChildren(writer, match.getChildrenAnnotation(), "Parent of annotation", concepts);
			writeParentsChildren(writer, match.getChildrenRemainingAnnotation(), "Parent of excluded annotation", concepts);
			writer.write("</span></td>\n");
		}
	}

	private static void writeMatchType(Writer writer, Match match) throws IOException {
		writer.write("<td class=\"type\">" + match.getConceptMatch().getType() + "</td>\n");
	}

	private static void writeQueryMatch(Writer writer, Match match, Query query, List<Publication> publications) throws IOException {
		writer.write("<td class=\"type\">");
		QueryMatchType type = match.getQueryMatch().getType();
		int index = match.getQueryMatch().getIndex();
		String link = null;
		if (match.getQueryMatch().getType() == QueryMatchType.webpage || match.getQueryMatch().getType() == QueryMatchType.doc) {
			if (match.getQueryMatch().getType() == QueryMatchType.webpage) {
				link = query.getWebpageUrls().get(index);
			} else if (match.getQueryMatch().getType() == QueryMatchType.doc) {
				link = query.getDocUrls().get(index);
			}
			if (link != null && !link.isEmpty()) {
				writer.write("<a href=\"" + link + "\">");
			}
		}
		writer.write(match.getQueryMatch().getType().toString());
		if (type == QueryMatchType.keyword) {
			writer.write("<br>" + query.getKeywords().get(index).getValue());
		}
		if (publications != null && publications.size() > 1 &&
			(type == QueryMatchType.publication_title ||
			type == QueryMatchType.publication_keyword ||
			type == QueryMatchType.publication_mesh ||
			type == QueryMatchType.publication_efo ||
			type == QueryMatchType.publication_go ||
			type == QueryMatchType.publication_abstract ||
			type == QueryMatchType.publication_fulltext)) {
			writer.write("<br>" + getPublicationLinkHtml(query.getPublicationIds().get(index)));
		}
		if (type == QueryMatchType.publication_keyword ||
			type == QueryMatchType.publication_mesh ||
			type == QueryMatchType.publication_efo ||
			type == QueryMatchType.publication_go) {
			writer.write("<br>");
			int indexInPublication = match.getQueryMatch().getIndexInPublication();
			if (type == QueryMatchType.publication_keyword) {
				writer.write(publications.get(index).getKeywords().get(indexInPublication));
			} else if (type == QueryMatchType.publication_mesh) {
				writer.write(getMeshLinkHtml(publications.get(index).getMeshTerms().get(indexInPublication)));
			} else if (type == QueryMatchType.publication_efo) {
				writer.write(getMinedLinkHtml(publications.get(index).getEfoTerms().get(indexInPublication)));
			} else if (type == QueryMatchType.publication_go) {
				writer.write(getMinedLinkHtml(publications.get(index).getGoTerms().get(indexInPublication)));
			}
		}
		if (link != null && !link.isEmpty()) {
			writer.write("</a>");
		}
		writer.write("</td>\n");
	}

	private static void writeScore(Writer writer, Match match, MapperArgs args) throws IOException {
		double bestOneScore;
		if (match.getBestOneScore() > 0) {
			bestOneScore = match.getBestOneScore();
		} else if (match.getWithoutPathScore() > 0) {
			bestOneScore = match.getWithoutPathScore();
		} else {
			bestOneScore = match.getScore();
		}
		writer.write("<td class=\"score");
		double goodScore = 0;
		double badScore = 0;
		switch (match.getEdamUri().getBranch()) {
		case topic:
			goodScore = args.getGoodScoreTopic();
			badScore = args.getBadScoreTopic();
			break;
		case operation:
			goodScore = args.getGoodScoreOperation();
			badScore = args.getBadScoreOperation();
			break;
		case data:
			goodScore = args.getGoodScoreData();
			badScore = args.getBadScoreData();
			break;
		case format:
			goodScore = args.getGoodScoreFormat();
			badScore = args.getBadScoreFormat();
			break;
		}
		if (bestOneScore > goodScore) writer.write(" good");
		else if (bestOneScore < badScore) writer.write(" bad");
		else writer.write(" medium");
		// TODO
		//if (match.getMatchConfidence() == MatchConfidence.exact) {
		//	writer.write(" exact");
		//}
		writer.write("\">" + percent(match.getScore()) + "</td>\n");
	}

	private static void writeCheckbox(Writer writer, int i, int j, int k) throws IOException {
		writer.write("<td class=\"done\"><input id=\"i" + i + "j" + j + "k" + k + "b\" type=\"checkbox\" onclick=\"hide('i" + i + "j" + j + "k" + k + "')\"></td>\n");
	}

	private static void out(MainArgs args, Writer writer, Map<EdamUri, Concept> concepts, List<Query> queries, List<List<Publication>> publications, List<List<ResultRow>> results) throws IOException {
		writePreamble(args, writer, date);

		writer.write("<h2>Total</h2>\n");
		writer.write("<dl>\n");
		writeVarVal(writer, "Total EDAM concepts", concepts.size());
		writeVarVal(writer, "Total query keywords", queries.size());
		writeVarVal(writer, "Total mapper answers", results.size());
		writeVarVal(writer, "Total matches", addArrays(addArrays(tp, fp), fn), tpT + fpT + fnT);
		writeVarVal(writer, "Total query matches", addArrays(tp, fn), tpT + fnT);
		writeVarVal(writer, "Total mapper matches", addArrays(tp, fp), tpT + fpT);
		writeVarVal(writer, "https://en.wikipedia.org/wiki/Type_I_and_type_II_errors", "Total mistakes", addArrays(fp, fn), fpT + fnT);
		writeVarVal(writer, "https://en.wikipedia.org/wiki/Type_I_and_type_II_errors", "Total TP", tp, tpT);
		writeVarVal(writer, "https://en.wikipedia.org/wiki/Type_I_and_type_II_errors", "Total FP", fp, fpT);
		writeVarVal(writer, "https://en.wikipedia.org/wiki/Type_I_and_type_II_errors", "Total FN", fn, fnT);
		writer.write("</dl>\n");
		writer.write("\n");

		writer.write("<h2>Mean</h2>\n");
		writer.write("<dl>\n");
		writeVarVal(writer, "https://en.wikipedia.org/wiki/Precision_and_recall#F-measure", "Precision", precision);
		writeVarVal(writer, "https://en.wikipedia.org/wiki/Precision_and_recall#F-measure", "Recall", recall);
		writeVarVal(writer, "https://en.wikipedia.org/wiki/F1_score", "F1 score", f1);
		writeVarVal(writer, "https://en.wikipedia.org/wiki/F1_score", "F2 score", f2);
		writeVarVal(writer, "https://en.wikipedia.org/wiki/Jaccard_index", "Jaccard index", Jaccard);
		writeVarVal(writer, "https://en.wikipedia.org/wiki/Information_retrieval#Average_precision", "Average precision", AveP);
		writeVarVal(writer, "https://en.wikipedia.org/wiki/Information_retrieval#R-Precision", "R-Precision", RP);
		writeVarVal(writer, "https://en.wikipedia.org/wiki/Discounted_cumulative_gain", "Discounted cumulative gain", DCG);
		writeVarVal(writer, "https://en.wikipedia.org/wiki/Discounted_cumulative_gain", "DCG (alternative)", DCGa);
		writer.write("</dl>\n");
		writer.write("\n");

		writeVal(recall);
		writeVal(AveP);

		writer.write("<h2>Table</h2>\n");
		writeLegend(writer, args.getMapperArgs());
		writer.write("<table>\n");
		writer.write("\n");
		writer.write("<colgroup>\n");

		boolean publicationPresent = false;
		boolean descriptionPresent = false;
		for (Query query : queries) {
			if (query.getPublicationIds() != null) {
				for (String publicationId : query.getPublicationIds()) {
					if (!publicationId.trim().isEmpty()) {
						publicationPresent = true;
					}
				}
			}
			if (publicationPresent) break;
			if (query.getDescription() != null) {
				if (!query.getDescription().trim().isEmpty()) {
					descriptionPresent = true;
				}
			}
		}
		if (publicationPresent) {
			writer.write("<col style=\"width:43%\">\n");
			writer.write("<col style=\"width:10%\">\n");
			writer.write("<col style=\"width:10%\">\n");
			writer.write("<col style=\"width:10%\">\n");
		} else if (descriptionPresent) {
			writer.write("<col style=\"width:25%\">\n");
			writer.write("<col style=\"width:16%\">\n");
			writer.write("<col style=\"width:16%\">\n");
			writer.write("<col style=\"width:16%\">\n");
		} else {
			writer.write("<col style=\"width:19%\">\n");
			writer.write("<col style=\"width:18%\">\n");
			writer.write("<col style=\"width:18%\">\n");
			writer.write("<col style=\"width:18%\">\n");
		}

		writer.write("<col style=\"width:10%\">\n");
		writer.write("<col style=\"width:10%\">\n");
		writer.write("<col style=\"width:5%\">\n");
		writer.write("<col style=\"width:2%\">\n");
		writer.write("</colgroup>\n");
		writer.write("\n");

		writer.write("<thead>\n<tr>\n");
		writer.write("<th>Query</th>\n<th class=\"tp\">TP</th>\n<th class=\"fp\">FP</th>\n<th class=\"fn\">FN</th>\n<th>Match Type</th>\n<th>Query Match</th>\n<th>Score</th>\n<th>✓</th>\n");
		writer.write("</tr>\n</thead>\n\n");

		for (int i = 0; i < queries.size(); ++i) {
			Query query = queries.get(i);
			List<Publication> publication = publications.get(i);
			List<ResultRow> rows = results.get(i);

			writer.write("<tbody id=\"i" + i + "\">\n\n");

			writeQuery(writer, query, publication, rows.size() + 1, i);

			for (int j = 0, k = 0; j < rows.size(); ++j) {
				ResultRow row = rows.get(j);

				if (row.resultType == ResultType.empty) {
					writer.write("<tr class=\"sep-branch\">\n");
					++k;
				} else if (row.resultType == ResultType.fn) {
					writeTr(writer, row.match.getEdamUri(), i, k, j, true);
				} else {
					writeTr(writer, row.match.getEdamUri(), i, k, j, false);
				}

				if (row.resultType == ResultType.empty) {
					writer.write("<td colspan=\"7\"></td>\n");
				} else {
					writeMatch(writer, row.match, concepts, row.resultType.toString());

					writeMatchType(writer, row.match);

					writeQueryMatch(writer, row.match, query, publication);

					writeScore(writer, row.match, args.getMapperArgs());

					writeCheckbox(writer, i, k, j);
				}

				writer.write("</tr>\n\n");

				/*
				if (j < mapping.getBranches().size() - 1) {
					writer.write("<tr class=\"sep-branch\">\n");
					writer.write("<td colspan=\"7\"></td>\n");
					writer.write("</tr>\n\n");
				}
				*/
			}

			if (i < queries.size() - 1) {
				writer.write("<tr class=\"sep\"><td colspan=\"8\">&nbsp;</td></tr>\n\n");
			}

			writer.write("</tbody>\n");
		}

		writer.write("<tfoot>\n<tr>\n");
		writer.write("<td>" + queries.size() + "</td>\n");
		writer.write("<td class=\"tp\">" + tpT + "</td>\n");
		writer.write("<td class=\"fp\">" + fpT + "</td>\n");
		writer.write("<td class=\"fn\">" + fnT + "</td>\n");
		writer.write("<td>&nbsp;</td>\n");
		writer.write("<td>&nbsp;</td>\n");
		writer.write("<td>&nbsp;</td>\n");
		writer.write("<td>&nbsp;</td>\n");
		writer.write("</tr>\n</tfoot>\n\n");

		writer.write("</table>\n\n");
		writer.write("</body>\n");
		writer.write("</html>\n");
	}

	static void output(MainArgs args, Path report, Map<EdamUri, Concept> concepts, List<Query> queries, List<List<Publication>> publications, List<Mapping> mappings) throws IOException {
		if (report != null) {
			try (BufferedWriter writer = Files.newBufferedWriter(report, StandardCharsets.UTF_8)) {
				List<List<ResultRow>> results = calculate(queries, mappings);
				out(args, writer, concepts, queries, publications, results);
			}
		}
	}
}
