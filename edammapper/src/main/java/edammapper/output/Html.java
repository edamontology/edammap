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

import edammapper.args.MainArgs;
import edammapper.edam.Branch;
import edammapper.edam.Concept;
import edammapper.edam.EdamUri;
import edammapper.fetching.Publication;
import edammapper.mapping.Mapping;
import edammapper.mapping.Match;
import edammapper.query.Query;

class Html {

	private static void out(MainArgs args, Writer writer, Map<EdamUri, Concept> concepts, List<Query> queries, List<List<Publication>> publications, List<Mapping> mappings) throws IOException {
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

		// TODO look more than just the first entry (which might be missing publications for example)
		if (!queries.isEmpty() && queries.get(0).getPublicationIds() != null && !queries.get(0).getPublicationIds().isEmpty() && !queries.get(0).getPublicationIds().get(0).trim().isEmpty()) {
			writer.write("<col style=\"width:45%\">\n");
			writer.write("<col style=\"width:30%\">\n");
		} else if (!queries.isEmpty() && queries.get(0).getDescription() != null && !queries.get(0).getDescription().isEmpty()) {
			writer.write("<col style=\"width:26%\">\n");
			writer.write("<col style=\"width:49%\">\n");
		} else {
			writer.write("<col style=\"width:20%\">\n");
			writer.write("<col style=\"width:55%\">\n");
		}

		writer.write("<col style=\"width:10%\">\n");
		writer.write("<col style=\"width:10%\">\n");
		writer.write("<col style=\"width:5%\">\n");
		writer.write("</colgroup>\n");
		writer.write("\n");

		writer.write("<thead>\n<tr>\n");
		writer.write("<th>Query</th>\n<th>Match</th>\n<th>Match Type</th>\n<th>Query Match</th>\n<th>Score</th>\n");
		writer.write("</tr>\n</thead>\n\n");

		writer.write("<tbody>\n\n");
		int matches = 0;
		for (int i = 0; i < queries.size(); ++i) {
			Query query = queries.get(i);
			List<Publication> publication = publications.get(i);
			Mapping mapping = mappings.get(i);

			int rowspan = 0;
			for (Branch branch : mapping.getBranches()) {
				for (int j = 0; j < mapping.getMatchesSize(branch); ++j) {
					++rowspan;
				}
				++rowspan;
			}
			Common.writeQuery(writer, query, publication, rowspan);

			for (int j = 0; j < mapping.getBranches().size(); ++j) {
				Branch branch = mapping.getBranches().get(j);

				for (int k = 0; k < mapping.getMatchesSize(branch); ++k) {
					Match match = mapping.getMatch(branch, k);
					Concept concept = concepts.get(match.getEdamUri());

					Common.writeTr(writer, match.getEdamUri());

					Common.writeMatch(writer, match, match.getEdamUri(), concept, "match");

					Common.writeMatchType(writer, match);

					Common.writeQueryMatch(writer, match, query, publication);

					Common.writeScore(writer, match, args.getMapperArgs());

					writer.write("</tr>\n\n");

					++matches;
				}

				if (j < mapping.getBranches().size() - 1) {
					writer.write("<tr class=\"sep-branch\">\n");
					writer.write("<td colspan=\"4\"></td>\n");
					writer.write("</tr>\n\n");
				}
			}

			if (i < queries.size() - 1) {
				writer.write("<tr class=\"sep\"><td colspan=\"5\">&nbsp;</td></tr>\n\n");
			}
		}
		writer.write("</tbody>\n\n");

		writer.write("<tfoot>\n<tr>\n");
		writer.write("<td>" + queries.size() + "</td>\n");
		writer.write("<td>" + matches + "</td>\n");
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
				out(args, writer, concepts, queries, publications, mappings);
			}
		}
	}
}
