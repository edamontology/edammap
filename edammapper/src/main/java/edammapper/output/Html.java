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
		Common.writeLegend(writer, args.getMapperArgs());
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
			writer.write("<col style=\"width:45%\">\n");
			writer.write("<col style=\"width:28%\">\n");
		} else if (descriptionPresent) {
			writer.write("<col style=\"width:26%\">\n");
			writer.write("<col style=\"width:47%\">\n");
		} else {
			writer.write("<col style=\"width:20%\">\n");
			writer.write("<col style=\"width:53%\">\n");
		}

		writer.write("<col style=\"width:10%\">\n");
		writer.write("<col style=\"width:10%\">\n");
		writer.write("<col style=\"width:5%\">\n");
		writer.write("<col style=\"width:2%\">\n");
		writer.write("</colgroup>\n");
		writer.write("\n");

		writer.write("<thead>\n<tr>\n");
		writer.write("<th>Query</th>\n<th>Match</th>\n<th>Match Type</th>\n<th>Query Match</th>\n<th>Score</th>\n<th>✓</th>\n");
		writer.write("</tr>\n</thead>\n\n");

		int matches = 0;
		for (int i = 0; i < queries.size(); ++i) {
			Query query = queries.get(i);
			List<Publication> publication = publications.get(i);
			Mapping mapping = mappings.get(i);

			writer.write("<tbody id=\"i" + i + "\">\n\n");

			int rowspan = 0;
			for (Branch branch : mapping.getBranches()) {
				for (int j = 0; j < mapping.getMatchesSize(branch); ++j) {
					++rowspan;
				}
				++rowspan;
			}
			Common.writeQuery(writer, query, publication, rowspan, i);

			for (int j = 0; j < mapping.getBranches().size(); ++j) {
				Branch branch = mapping.getBranches().get(j);

				for (int k = 0; k < mapping.getMatchesSize(branch); ++k) {
					Match match = mapping.getMatch(branch, k);
					Concept concept = concepts.get(match.getEdamUri());

					Common.writeTr(writer, match.getEdamUri(), i, j, k);

					Common.writeMatch(writer, match, match.getEdamUri(), concept, "match");

					Common.writeMatchType(writer, match);

					Common.writeQueryMatch(writer, match, query, publication);

					Common.writeScore(writer, match, args.getMapperArgs());

					Common.writeCheckbox(writer, i, j, k);

					writer.write("</tr>\n\n");

					++matches;
				}

				if (j < mapping.getBranches().size() - 1) {
					writer.write("<tr class=\"sep-branch\">\n");
					writer.write("<td colspan=\"5\"></td>\n");
					writer.write("</tr>\n\n");
				}
			}

			if (i < queries.size() - 1) {
				writer.write("<tr class=\"sep\"><td colspan=\"6\">&nbsp;</td></tr>\n\n");
			}

			writer.write("</tbody>\n");
		}

		writer.write("\n<tfoot>\n<tr>\n");
		writer.write("<td>" + queries.size() + "</td>\n");
		writer.write("<td>" + matches + "</td>\n");
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
				out(args, writer, concepts, queries, publications, mappings);
			}
		}
	}
}
