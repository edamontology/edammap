package edammapper.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import edammapper.edam.Branch;
import edammapper.edam.Concept;
import edammapper.edam.EdamUri;
import edammapper.fetching.Publication;
import edammapper.mapping.Mapping;
import edammapper.mapping.Match;
import edammapper.query.Query;
import edammapper.query.QueryType;

class Txt {

	private static final String SEP = " | ";

	// TODO publications needed ?
	private static void out(QueryType type, Writer writer, Map<EdamUri, Concept> concepts, List<Query> queries, List<List<Publication>> publications, List<Mapping> mappings) throws IOException {
		for (int i = 0; i < queries.size(); ++i) {
			Query query = queries.get(i);
			Mapping mapping = mappings.get(i);

			if (type == QueryType.SEQwikiTags || type == QueryType.SEQwikiTool) {
				if (type == QueryType.SEQwikiTool) {
					writer.write(query.getName() + SEP);
				}
				writer.write(query.getKeywords().iterator().next().getValue());
				writer.write(SEP + query.getKeywords().iterator().next().getType());
				for (Branch branch : mapping.getBranches()) {
					for (int j = 0; j < mapping.getMatches(branch).size(); ++j) {
						Match match = mapping.getMatches(branch).get(j);
						Concept concept = concepts.get(match.getEdamUri());
						writer.write(SEP + concept.getLabel());
						writer.write(" (" + match.getEdamUri().getBranch() + "_" + match.getEdamUri().getNrString() + ")");
					}
				}
				writer.write("\n");
			} else {
				for (Branch branch : mapping.getBranches()) {
					for (int j = 0; j < mapping.getMatches(branch).size(); ++j) {
						Match match = mapping.getMatches(branch).get(j);
						Concept concept = concepts.get(match.getEdamUri());

						if (query.getName() != null) {
							writer.write(query.getName());
						} else {
							writer.write("NA");
						}

						// TODO
						writer.write(SEP + concept.getLabel()
							//+ SEP + Common.conceptMatchString(match, concept)
							+ SEP + match.getEdamUri()
							+ SEP + concept.isObsolete()
							//+ SEP + match.getMatchConfidence()
							+ SEP + match.getEdamUri().getBranch()
							+ SEP + match.getConceptMatch().getType()
							+ SEP + match.getQueryMatch().getType()
							//+ SEP + Common.queryMatchString(match, query, publications)
							+ SEP + match.getScore()
							+ "\n");
					}
				}
			}
		}
	}

	static void output(QueryType type, Path output, Map<EdamUri, Concept> concepts, List<Query> queries, List<List<Publication>> publications, List<Mapping> mappings) throws IOException {
		if (output == null) {
			try (OutputStreamWriter stdoutWriter = new OutputStreamWriter(System.out)) {
				out(type, stdoutWriter, concepts, queries, publications, mappings);
			}
		} else {
			try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
				out(type, writer, concepts, queries, publications, mappings);
			} catch (IOException e) {
				try (OutputStreamWriter stdoutWriter = new OutputStreamWriter(System.out)) {
					out(type, stdoutWriter, concepts, queries, publications, mappings);
				} catch (Exception e2) {
					throw e;
				}
				throw e;
			}
		}
	}
}
