package edammapper.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

class Html {

	private static void out(Args args, Writer writer, Map<EdamUri, Concept> concepts, List<Query> queries, List<Mapping> mappings) throws IOException {
		Common.writePreamble(false, args, writer, new Date());

		writer.write("<h2>Total</h2>\n");
		writer.write("<dl>\n");
		Common.writeVarVal(writer, "Total EDAM concepts", concepts.size());
		Common.writeVarVal(writer, "Total query keywords", queries.size());
		Common.writeVarVal(writer, "Total mapper answers", mappings.size());
		writer.write("</dl>\n");
		writer.write("\n");

		writer.write("<h2>Table</h2>\n");
		writer.write("<table>\n");
		writer.write("\n");
		writer.write("<colgroup>\n");

		if (args.getType() == IOType.SEQwiki) {
			writer.write("<col style=\"width:50%\">\n");
			writer.write("<col style=\"width:35%\">\n");
		} else {
			writer.write("<col style=\"width:25%\">\n");
			writer.write("<col style=\"width:60%\">\n");
		}

		writer.write("<col style=\"width:10%\">\n");
		writer.write("<col style=\"width:5%\">\n");
		writer.write("</colgroup>\n");
		writer.write("\n");

		writer.write("<thead>\n<tr>\n");
		writer.write("<th>Query</th>\n<th>Match</th>\n<th>Match Type</th>\n<th>Score</th>\n");
		writer.write("</tr>\n</thead>\n\n");

		writer.write("<tbody>\n\n");
		int matches = 0;
		for (int i = 0; i < queries.size(); ++i) {
			Query query = queries.get(i);
			Mapping mapping = mappings.get(i);

			int rowspan = 0;
			for (Branch branch : mapping.getBranches()) {
				for (int j = 0; j < mapping.getMatchesSize(branch); ++j) {
					++rowspan;
				}
				++rowspan;
			}
			Common.writeQuery(writer, query, rowspan, args.getType());

			for (int j = 0; j < mapping.getBranches().size(); ++j) {
				Branch branch = mapping.getBranches().get(j);

				for (int k = 0; k < mapping.getMatchesSize(branch); ++k) {
					Match match = mapping.getMatch(branch, k);
					Concept concept = concepts.get(match.getEdamUri());

					Common.writeTr(writer, match.getEdamUri());

					Common.writeMatch(writer, match, match.getEdamUri(), concept, "match");

					Common.writeMatchType(writer, match);

					Common.writeScore(writer, match);

					writer.write("</tr>\n\n");

					++matches;
				}

				if (j < mapping.getBranches().size() - 1) {
					writer.write("<tr class=\"sep-branch\">\n");
					writer.write("<td colspan=\"3\"></td>\n");
					writer.write("</tr>\n\n");
				}
			}

			if (i < queries.size() - 1) {
				writer.write("<tr class=\"sep\"><td colspan=\"4\">&nbsp;</td></tr>\n\n");
			}
		}
		writer.write("</tbody>\n\n");

		writer.write("<tfoot>\n<tr>\n");
		writer.write("<td>" + queries.size() + "</td>\n");
		writer.write("<td>" + matches + "</td>\n");
		writer.write("<td>&nbsp;</td>\n");
		writer.write("<td>&nbsp;</td>\n");
		writer.write("</tr>\n</tfoot>\n\n");

		writer.write("</table>\n\n");
		writer.write("</body>\n");
		writer.write("</html>\n");
	}

	static void output(Args args, Path report, Map<EdamUri, Concept> concepts, List<Query> queries, List<Mapping> mappings) throws IOException {
		if (report != null) {
			try (BufferedWriter writer = Files.newBufferedWriter(report, StandardCharsets.UTF_8)) {
				out(args, writer, concepts, queries, mappings);
			}
		}
	}
}
