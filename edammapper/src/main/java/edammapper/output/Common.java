package edammapper.output;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;

import edammapper.args.Args;
import edammapper.edam.Concept;
import edammapper.edam.EdamUri;
import edammapper.mapping.Match;
import edammapper.mapping.MatchConfidence;
import edammapper.mapping.MatchType;
import edammapper.query.IOType;
import edammapper.query.Query;
import edammapper.query.QueryMsutils;
import edammapper.query.QuerySEQwiki;
import edammapper.query.QuerySEQwikiTags;
import edammapper.query.QuerySEQwikiTool;

class Common {

	private static final String SEQWIKI = "http://seqanswers.com/wiki/";

	static String matchString(Match match, Concept concept) {
		switch (match.getMatchType()) {
			case label: return concept.getLabel();
			case exact_synonym: return concept.getExactSynonyms().get(match.getSynonymIndex());
			case narrow_synonym: return concept.getNarrowSynonyms().get(match.getSynonymIndex());
			case broad_synonym: return concept.getBroadSynonyms().get(match.getSynonymIndex());
			case definition: return concept.getDefinition();
			case comment: return concept.getComment();
			default: return "";
		}
	}

	private static double averageArray(double[] a) {
		return Arrays.stream(a).average().orElse(0);
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

	private static String getStyle() {
		return
		"a { text-decoration: none; color: black }\n" +
		"a:hover, a:active { text-decoration: underline }\n" +
		"dt { float:left; clear: left; text-align: right; width: 13em }\n" +
		"dt, dt a { color: blue }\n" +
		"dt:after { content: \":\" }\n" +
		"dd { margin-left: 13em; padding-left: 0.5em }\n" +
		"table { border-collapse: separate; border-spacing: 0px 0px }\n" +
		"thead, tfoot { text-align: center; font-weight: bold }\n" +
		"thead th { border-bottom: 2px solid black }\n" +
		"tfoot td { border-top: 3px solid black }\n" +
		"h3 { margin-top: 0 }\n" +
		"h4 { margin-bottom: 0 }\n" +
		".query td { border-top: 1px solid black; vertical-align: top }\n" +
		".sep { height: 3em }\n" +
		".sep td { border-bottom: 1px solid black }\n" +
		".sep-branch { height: 1em }\n" +
		".row td { border-top: 1px solid black }\n" +
		".obsolete { text-decoration: line-through }\n" +
		".topic td, .topic { background-color: rgba(222,184,135,0.5) } /* BurlyWood */\n" +
		".topic:hover td, .topic:hover { background-color: rgba(222,184,135,1) } /* BurlyWood */\n" +
		".operation td, .operation { background-color: rgba(211,211,211,0.5) } /* LightGray */\n" +
		".operation:hover td, .operation:hover { background-color: rgba(211,211,211,1) } /* LightGray */\n" +
		".data td, .data { background-color: rgba(135,206,235,0.5) } /* SkyBlue */\n" +
		".data:hover td, .data:hover { background-color: rgba(135,206,235,1) } /* SkyBlue */\n" +
		".format td, .format { background-color: rgba(216,191,216,0.5) } /* Thistle */\n" +
		".format:hover td, .format:hover { background-color: rgba(216,191,216,1) } /* Thistle */\n" +
		".type { text-align: center; color: #666 }\n" +
		".score { text-align: right; font-weight: bold }\n" +
		".good { color: green }\n" +
		".medium { color: yellow }\n" +
		".bad { color: red }\n" +
		".exact { text-decoration: underline }\n";
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

	static void writePreamble(boolean benchmark, Args args, Writer writer, Date date) throws IOException {
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
		writer.write("<style type=\"text/css\">\n");
		if (benchmark) {
			writer.write(getStyleBenchmark());
		} else {
			writer.write(getStyleReport());
		}
		writer.write("</style>\n");
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
		writer.write("</dl>\n");
		writer.write("\n");

		writer.write("<h2>Preprocessing</h2>\n");
		writer.write("<dl>\n");
		writeVarVal(writer, "Remove freestanding numbers", args.getPreProcessorArgs().isNumberRemove());
		writeVarVal(writer, "Stopword list", args.getPreProcessorArgs().getStopwords().toString());
		writeVarVal(writer, "Do stemming", !args.getPreProcessorArgs().isNoStemming());
		writeVarVal(writer, "Remove tokens of length", args.getPreProcessorArgs().getShortWord());
		writer.write("</dl>\n");
		writer.write("\n");

		writer.write("<h2>Mapping</h2>\n");
		writer.write("<dl>\n");
		writeVarVal(writer, "Top matches per branch", args.getMapperArgs().getMatch());
		writeVarVal(writer, "http://edamontology.org/page#Scope", "Branches", args.getMapperArgs().getBranches().toString());
		writeVarVal(writer, "Include obsolete concepts", args.getMapperArgs().getObsolete());
		writer.write("</dl>\n");
		writer.write("\n");

		writer.write("<h2>Mapping algorithm</h2>\n");
		writer.write("<dl>\n");
		writeVarVal(writer, "Compound words", args.getMapperArgs().algo().getCompoundWords());
		writeVarVal(writer, "Mismatch multiplier", args.getMapperArgs().algo().getMismatchMultiplier());
		writeVarVal(writer, "Position off by 1", args.getMapperArgs().algo().getPositionOffBy1());
		writeVarVal(writer, "Position off by 2", args.getMapperArgs().algo().getPositionOffBy2());
		writeVarVal(writer, "Position loss", args.getMapperArgs().algo().getPositionLoss());
		writeVarVal(writer, "Concept weight", args.getMapperArgs().algo().getConceptWeight());
		writeVarVal(writer, "Query weight", args.getMapperArgs().algo().getQueryWeight());
		writeVarVal(writer, "Label multiplier", args.getMapperArgs().algo().getLabelMultiplier());
		writeVarVal(writer, "Exact synonym multiplier", args.getMapperArgs().algo().getExactSynonymMultiplier());
		writeVarVal(writer, "Narrow/Broad multiplier", args.getMapperArgs().algo().getNarrowBroadMultiplier());
		writeVarVal(writer, "Definition multiplier", args.getMapperArgs().algo().getDefinitionMultiplier());
		writeVarVal(writer, "Comment multiplier", args.getMapperArgs().algo().getCommentMultiplier());
		writer.write("</dl>\n");
		writer.write("\n");
	}

	static void writeQuery(Writer writer, Query query, int rowspan, IOType type) throws IOException {
		writer.write("<tr class=\"query\">");
		writer.write("<td rowspan=\"" + rowspan + "\">\n");

		writer.write("<h3>");
		if (type == IOType.SEQwikiTool) {
			QuerySEQwikiTool querySEQwikiTool = (QuerySEQwikiTool)query;
			writer.write("<a href=\"" + SEQWIKI + querySEQwikiTool.getName().replace(" ", "_") + "\">");
			writer.write(querySEQwikiTool.getName());
			writer.write("</a>");
		} else if (type == IOType.SEQwiki) {
			QuerySEQwiki querySEQwiki = (QuerySEQwiki)query;
			writer.write("<a href=\"" + querySEQwiki.getUrl() + "\">");
			writer.write(querySEQwiki.getName());
			writer.write("</a>");
		} else if (type == IOType.msutils) {
			QueryMsutils queryMsutils = (QueryMsutils)query;
			writer.write("<a href=\"" + queryMsutils.getUrl() + "\">");
			writer.write(queryMsutils.getName());
			writer.write("</a>");
		} else {
			boolean a = (query.getUrl() != null && !query.getUrl().isEmpty());
			if (a) writer.write("<a href=\"" + query.getUrl() + "\">");
			writer.write(query.getQuery());
			if (a) writer.write("</a>");
		}
		writer.write("</h3>\n");

		if (type == IOType.SEQwikiTags || type == IOType.SEQwikiTool) {
			QuerySEQwikiTags querySEQwikiTags = (QuerySEQwikiTags)query;
			switch (querySEQwikiTags.getBranch()) {
				case domain: writer.write("<h4><span class=\"topic\">Domain</span></h4>\n"); break;
				case method: writer.write("<h4><span class=\"operation\">Method</span></h4>\n"); break;
			}
			if (type == IOType.SEQwikiTool) {
				writer.write("<span>");
				writer.write("<a href=\"" + SEQWIKI + querySEQwikiTags.getQuery().replace(" ", "_") + "\">");
				writer.write(querySEQwikiTags.getQuery());
				writer.write("</a>");
				writer.write("</span>\n");
			}
		} else if (type == IOType.SEQwiki) {
			QuerySEQwiki querySEQwiki = (QuerySEQwiki)query;
			writer.write("<p>" + querySEQwiki.getQuery() + "</p>\n");
			writer.write("<h4><span class=\"topic\">Domain</span></h4>\n");
			writer.write("<span>");
			writer.write(querySEQwiki.getDomains().stream().map(s -> "<a href=\"" + SEQWIKI + s.replace(" ", "_") + "\">" + s + "</a>").collect(Collectors.joining(", ")));
			writer.write("</span>\n");
			writer.write("<h4><span class=\"operation\">Method</span></h4>\n");
			writer.write("<span>");
			writer.write(querySEQwiki.getMethods().stream().map(s -> "<a href=\"" + SEQWIKI + s.replace(" ", "_") + "\">" + s + "</a>").collect(Collectors.joining(", ")));
			writer.write("</span>\n");
		} else if (type == IOType.msutils) {
			writer.write("<p>" + query.getQuery() + "</p>\n");
		}

		writer.write("</td></tr>\n\n");
	}

	static void writeTr(Writer writer, EdamUri edamUri) throws IOException {
		writer.write("<tr class=\"row " + edamUri.getBranch() + "\"");
		writer.write(" title=\"" + edamUri.getBranch() + "_" + edamUri.getNr() + "\">\n");
	}

	static void writeMatch(Writer writer, Match match, EdamUri edamUri, Concept concept, String clazz) throws IOException {
		writer.write("<td class=\"" + clazz + "\"><a href=\"" + edamUri + "\"");
		if (concept == null) {
			writer.write(">" + edamUri); // error
		} else {
			if (concept.isObsolete()) writer.write(" class=\"obsolete\"");
			writer.write(">" + concept.getLabel());
			if (match != null && match.getMatchType() != MatchType.label) {
				writer.write(" (" + matchString(match, concept) + ")");
			}
			writer.write("</a></td>\n");
		}
	}

	static void writeMatchType(Writer writer, Match match) throws IOException {
		writer.write("<td class=\"type\">" + match.getMatchType() + "</td>\n");
	}

	static void writeScore(Writer writer, Match match) throws IOException {
		double score = match.getScore();
		writer.write("<td class=\"score");
		if (score > 2.0 / 3.0) writer.write(" good");
		else if (score < 1.0 / 3.0) writer.write(" bad");
		else writer.write(" medium");
		if (match.getMatchConfidence() == MatchConfidence.exact) {
			writer.write(" exact");
		}
		writer.write("\">" + percent(score) + "</td>\n");
	}
}
