package edammapper.output;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import edammapper.mapping.Match;
import edammapper.mapping.QueryMatchType;
import edammapper.query.Keyword;
import edammapper.query.Query;

class Common {

	static String conceptMatchString(Match match, Concept concept) {
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
	static void writeVal(double[] a) {
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

	static void writeVarVal(Writer writer, String var, int val) throws IOException {
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

	static void writeVarVal(Writer writer, String var, int[] a, int val) throws IOException {
		writeVar(writer, var);
		writeVal(writer, a, val);
	}

	static void writeVarVal(Writer writer, String href, String var, int[] a, int val) throws IOException {
		writeVar(writer, href, var);
		writeVal(writer, a, val);
	}

	static void writeVarVal(Writer writer, String href, String var, double[] a) throws IOException {
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
		"dt { float:left; clear: left; text-align: right; width: 13em }\n" +
		"dt, dt a { color: blue }\n" +
		"dt:after { content: \":\" }\n" +
		"dd { margin-left: 13em; padding-left: 0.5em }\n" +
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
		".type { text-align: center; color: #666 }\n" +
		".score { text-align: right; font-weight: bold }\n" +
		".good { color: green }\n" +
		".medium { color: yellow }\n" +
		".bad { color: red }\n" +
		".exact { text-decoration: underline }\n" +
		".done { text-align: center }\n";
	}

	private static String getStyleReport() {
		return
		getStyle();
	}

	private static String getStyleBenchmark() {
		return
		getStyle() +
		"tr .tp, tr:hover .tp { background-color: green }\n" +
		"tr .tp a { color: white }\n" +
		"tr .fp, tr:hover .fp { background-color: yellow }\n" +
		"tr .fn, tr:hover .fn { background-color: red }\n";
	}

	private static String getScript() {
		return
		"function hide(id) {\n" +
		"\tif (document.getElementById(id + \"b\").checked) {\n" +
		"\t\tdocument.getElementById(id).style.display = \"none\";\n" +
		"\t}\n" +
		"}\n";
	}

	static void writePreamble(boolean benchmark, MainArgs args, Writer writer, Date date) throws IOException {
		writer.write("<!DOCTYPE html>\n");
		writer.write("<html>\n");
		writer.write("\n");

		writer.write("<head>\n");
		writer.write("<meta charset=\"UTF-8\">\n");
		if (benchmark) {
			writer.write("<title>Benchmark report</title>\n");
		} else {
			writer.write("<title>Report</title>\n");
		}
		writer.write("<style>\n");
		if (benchmark) {
			writer.write(getStyleBenchmark());
		} else {
			writer.write(getStyleReport());
		}
		writer.write("</style>\n");
		writer.write("<script>\n");
		writer.write(getScript());
		writer.write("</script>\n");
		writer.write("</head>\n");
		writer.write("\n");

		writer.write("<body>\n");
		if (benchmark) {
			writer.write("<h1>Benchmark report</h1>\n");
		} else {
			writer.write("<h1>Report</h1>\n");
		}
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
		String benchmarkReportFile = new File(args.getBenchmarkReport()).getName();
		writeVarVal(writer, "Benchmark report file", (benchmarkReportFile.isEmpty() ? "&nbsp;" : benchmarkReportFile));
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

	static void writeLegend(Writer writer, MapperArgs args) throws IOException {
		writer.write("\n<table id=\"legend\">\n");
		for (Branch branch : args.getBranches()) {
			writer.write("<tr class=\"row " + branch + "\"><td>" + branch + "</td></tr>\n");
		}
		writer.write("<tr class=\"sep-branch\"><td></td></tr>\n");
		writer.write("</table>\n\n");
	}

	static void writeQuery(Writer writer, Query query, List<Publication> publications, int rowspan, int id) throws IOException {
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

	static void writeTr(Writer writer, EdamUri edamUri, int i, int j, int k) throws IOException {
		writer.write("<tr id=\"i" + i + "j" + j + "k" + k + "\"");
		writer.write(" class=\"row " + edamUri.getBranch() + "\"");
		writer.write(" title=\"" + edamUri.getBranch() + "_" + edamUri.getNrString() + "\">\n");
	}

	static void writeMatch(Writer writer, Match match, EdamUri edamUri, Concept concept, String clazz) throws IOException {
		writer.write("<td class=\"" + clazz + "\"><a href=\"" + edamUri + "\"");
		if (concept == null) {
			writer.write(">" + edamUri); // TODO error
		} else {
			if (concept.isObsolete()) writer.write(" class=\"obsolete\"");
			writer.write(">" + concept.getLabel());
			if (match != null && match.getConceptMatch().getType() != ConceptMatchType.label && match.getConceptMatch().getType() != ConceptMatchType.none) {
				writer.write(" (" + conceptMatchString(match, concept) + ")");
			}
			writer.write("</a></td>\n");
		}
	}

	static void writeMatchType(Writer writer, Match match) throws IOException {
		writer.write("<td class=\"type\">" + match.getConceptMatch().getType() + "</td>\n");
	}

	static void writeQueryMatch(Writer writer, Match match, Query query, List<Publication> publications) throws IOException {
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

	static void writeScore(Writer writer, Match match, MapperArgs args) throws IOException {
		double bestOneScore;
		if (match.getBestOneScore() > 0) {
			bestOneScore = match.getBestOneScore();
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

	static void writeCheckbox(Writer writer, int i, int j, int k) throws IOException {
		writer.write("<td class=\"done\"><input id=\"i" + i + "j" + j + "k" + k + "b\" type=\"checkbox\" onclick=\"hide('i" + i + "j" + j + "k" + k + "')\"></td>\n");
	}
}
