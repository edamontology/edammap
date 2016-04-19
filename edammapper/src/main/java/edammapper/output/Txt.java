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
import edammapper.mapping.Mapping;
import edammapper.mapping.Match;
import edammapper.query.IOType;
import edammapper.query.Query;
import edammapper.query.QueryMsutils;
import edammapper.query.QuerySEQwiki;
import edammapper.query.QuerySEQwikiTags;
import edammapper.query.QuerySEQwikiTool;

class Txt {

	private static final String SEP = " | ";

	private static void out(IOType type, Writer writer, Map<EdamUri, Concept> concepts, List<Query> queries, List<Mapping> mappings) throws IOException {
		for (int i = 0; i < queries.size(); ++i) {
			Query query = queries.get(i);
			Mapping mapping = mappings.get(i);

			if (type == IOType.SEQwikiTags || type == IOType.SEQwikiTool) {
				QuerySEQwikiTags querySEQwikiTags = (QuerySEQwikiTags)query;
				if (type == IOType.SEQwikiTool) {
					writer.write(((QuerySEQwikiTool)query).getName() + SEP);
				}
				writer.write(querySEQwikiTags.getQuery());
				writer.write(SEP + querySEQwikiTags.getBranch());
				for (Branch branch : mapping.getBranches()) {
					for (int j = 0; j < mapping.getMatchesSize(branch); ++j) {
						Match match = mapping.getMatch(branch, j);
						Concept concept = concepts.get(match.getEdamUri());
						writer.write(SEP + concept.getLabel());
						writer.write(" (" + match.getEdamUri().getBranch() + "_" + match.getEdamUri().getNr() + ")");
					}
				}
				writer.write("\n");
			} else {
				for (Branch branch : mapping.getBranches()) {
					for (int j = 0; j < mapping.getMatchesSize(branch); ++j) {
						Match match = mapping.getMatch(branch, j);
						Concept concept = concepts.get(match.getEdamUri());
						String matchString = Common.matchString(match, concept);

						if (type == IOType.SEQwiki) {
							QuerySEQwiki querySEQwiki = (QuerySEQwiki)query;
							writer.write(querySEQwiki.getName());
						} else if (type == IOType.msutils) {
							QueryMsutils queryMsutils = (QueryMsutils)query;
							writer.write(queryMsutils.getName());
						} else {
							writer.write(query.getQuery());
						}

						writer.write(SEP + concept.getLabel()
							+ SEP + matchString
							+ SEP + match.getEdamUri()
							+ SEP + concept.isObsolete()
							+ SEP + match.getMatchType()
							+ SEP + match.getMatchConfidence()
							+ SEP + match.getEdamUri().getBranch()
							+ SEP + match.getScore()
							+ "\n");
					}
				}
			}
		}
	}

	static void output(IOType type, Path output, Map<EdamUri, Concept> concepts, List<Query> queries, List<Mapping> mappings) throws IOException {
		if (output == null) {
			try (OutputStreamWriter stdoutWriter = new OutputStreamWriter(System.out)) {
				out(type, stdoutWriter, concepts, queries, mappings);
			}
		} else {
			try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
				out(type, writer, concepts, queries, mappings);
			} catch (IOException e) {
				try (OutputStreamWriter stdoutWriter = new OutputStreamWriter(System.out)) {
					out(type, stdoutWriter, concepts, queries, mappings);
				} catch (Exception e2) {
					throw e;
				}
				throw e;
			}
		}
	}
}
