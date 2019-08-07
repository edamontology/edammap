/*
 * Copyright © 2018, 2019 Erik Jaaniso
 *
 * This file is part of EDAMmap.
 *
 * EDAMmap is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EDAMmap is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EDAMmap.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.edamontology.edammap.core.output;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.edamontology.pubfetcher.core.common.FetcherArgs;
import org.edamontology.pubfetcher.core.common.PubFetcher;
import org.edamontology.pubfetcher.core.common.Version;
import org.edamontology.pubfetcher.core.db.DatabaseEntryType;
import org.edamontology.pubfetcher.core.db.publication.Publication;
import org.edamontology.pubfetcher.core.db.webpage.Webpage;

import org.edamontology.edammap.core.args.ArgMain;
import org.edamontology.edammap.core.args.CoreArgs;
import org.edamontology.edammap.core.benchmarking.MappingTest;
import org.edamontology.edammap.core.benchmarking.MatchTest;
import org.edamontology.edammap.core.benchmarking.Results;
import org.edamontology.edammap.core.edam.Branch;
import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.input.Input;
import org.edamontology.edammap.core.input.json.Biotools;
import org.edamontology.edammap.core.input.json.Edam;
import org.edamontology.edammap.core.input.json.Function;
import org.edamontology.edammap.core.input.json.InputOutput;
import org.edamontology.edammap.core.input.json.Tool;
import org.edamontology.edammap.core.input.json.ToolInput;
import org.edamontology.edammap.core.mapping.ConceptMatch;
import org.edamontology.edammap.core.mapping.ConceptMatchType;
import org.edamontology.edammap.core.mapping.Match;
import org.edamontology.edammap.core.mapping.MatchAverageStats;
import org.edamontology.edammap.core.mapping.QueryMatch;
import org.edamontology.edammap.core.mapping.QueryMatchType;
import org.edamontology.edammap.core.mapping.args.ScoreArgs;
import org.edamontology.edammap.core.query.Keyword;
import org.edamontology.edammap.core.query.Link;
import org.edamontology.edammap.core.query.PublicationIdsQuery;
import org.edamontology.edammap.core.query.Query;
import org.edamontology.edammap.core.query.QueryLoader;
import org.edamontology.edammap.core.query.QueryType;

public class Json {

	public static final String VERSION_ID = "version";
	public static final String TYPE_ID = "type";

	// TODO merge with biotoolsSchema restrictions in Pub2Tools
	private static final int FUNCTION_NOTE_MIN = 10;
	private static final int FUNCTION_NOTE_MAX = 1000;

	private static void parentsChildren(Map<EdamUri, Concept> concepts, List<EdamUri> pc, String field, JsonGenerator generator) throws IOException {
		if (!pc.isEmpty()) {
			generator.writeFieldName(field);
			generator.writeStartArray();
			for (EdamUri edamUri : pc) {
				generator.writeStartObject();
				generator.writeStringField("edamUri", edamUri.toString());
				generator.writeStringField("label", concepts.get(edamUri).getLabel());
				generator.writeEndObject();
			}
			generator.writeEndArray();
		}
	}

	private static void concept(Map<EdamUri, Concept> concepts, Match match, JsonGenerator generator) throws IOException {
		EdamUri edamUri = match.getEdamUri();
		generator.writeStringField("edamUri", edamUri.toString());
		Concept concept = concepts.get(edamUri);
		generator.writeStringField("label", concept.getLabel());
		generator.writeBooleanField("obsolete", concept.isObsolete());

		parentsChildren(concepts, match.getParents(), "childOf", generator);
		parentsChildren(concepts, match.getParentsAnnotation(), "childOfAnnotation", generator);
		parentsChildren(concepts, match.getParentsRemainingAnnotation(), "childOfExcludedAnnotation", generator);
		parentsChildren(concepts, match.getChildren(), "parentOf", generator);
		parentsChildren(concepts, match.getChildrenAnnotation(), "parentOfAnnotation", generator);
		parentsChildren(concepts, match.getChildrenRemainingAnnotation(), "parentOfExcludedAnnotation", generator);
	}

	private static String queryMatchUrl(Query query, QueryMatch queryMatch) {
		QueryMatchType type = queryMatch.getType();
		int index = queryMatch.getIndex();
		if (type == QueryMatchType.webpage && index >= 0 && query.getWebpageUrls().get(index) != null) {
			return query.getWebpageUrls().get(index).getUrl();
		} else if (type == QueryMatchType.doc && index >= 0 && query.getDocUrls().get(index) != null) {
			return query.getDocUrls().get(index).getUrl();
		} else if (type.isPublication() && index >= 0) {
			return PubFetcher.getIdLink(query.getPublicationIds().get(index));
		} else {
			return null;
		}
	}

	private static String publicationListItem(QueryMatch queryMatch, List<Publication> publications) {
		int index = queryMatch.getIndex();
		int indexInPublication = queryMatch.getIndexInPublication();
		if (index >= 0) {
			switch (queryMatch.getType()) {
				case publication_keyword: return publications.get(index).getKeywords().getList().get(indexInPublication);
				case publication_mesh: return publications.get(index).getMeshTerms().getList().get(indexInPublication).getTerm();
				case publication_efo: return publications.get(index).getEfoTerms().getList().get(indexInPublication).getTerm();
				case publication_go: return publications.get(index).getGoTerms().getList().get(indexInPublication).getTerm();
				default: return null;
			}
		} else {
			return null;
		}
	}

	private static void queryMatch(Query query, List<Publication> publications, QueryMatch queryMatch, boolean main, JsonGenerator generator) throws IOException {
		QueryMatchType type = queryMatch.getType();
		int index = queryMatch.getIndex();
		if (main) {
			generator.writeFieldName("bestOneQuery");
		} else {
			generator.writeFieldName("queryMatch");
		}
		generator.writeStartObject();
		generator.writeStringField("type", type.name());
		String queryMatchUrl = queryMatchUrl(query, queryMatch);
		if (queryMatchUrl != null) {
			generator.writeStringField("url", queryMatchUrl);
		}
		String publicationListItem = publicationListItem(queryMatch, publications);
		if (publicationListItem != null) {
			generator.writeStringField("value", publicationListItem);
		}
		if (type == QueryMatchType.keyword && index >= 0 && query.getKeywords().get(index) != null) {
			String url = query.getKeywords().get(index).getUrl();
			if (url != null) {
				generator.writeStringField("url", url);
			}
			String value = query.getKeywords().get(index).getValue();
			if (value != null) {
				generator.writeStringField("value", value);
			}
		}
		if (!main) {
			generator.writeNumberField("score", queryMatch.getScore());
		}
		generator.writeEndObject();
	}

	private static String conceptMatchString(Concept concept, ConceptMatch conceptMatch) {
		switch (conceptMatch.getType()) {
			case label: return concept.getLabel();
			case exact_synonym: return concept.getExactSynonyms().get(conceptMatch.getSynonymIndex());
			case narrow_synonym: return concept.getNarrowSynonyms().get(conceptMatch.getSynonymIndex());
			case broad_synonym: return concept.getBroadSynonyms().get(conceptMatch.getSynonymIndex());
			case definition: return concept.getDefinition();
			case comment: return concept.getComment();
			default: return "";
		}
	}

	private static void conceptMatch(Concept concept, ConceptMatch conceptMatch, boolean main, JsonGenerator generator) throws IOException {
		ConceptMatchType type = conceptMatch.getType();
		if (main) {
			generator.writeFieldName("bestOneConcept");
		} else {
			generator.writeFieldName("conceptMatch");
		}
		generator.writeStartObject();
		generator.writeStringField("type", type.name());
		if (main) {
			if (type != ConceptMatchType.none) {
				generator.writeStringField("value", conceptMatchString(concept, conceptMatch));
			}
		} else {
			if (type != ConceptMatchType.definition && type != ConceptMatchType.comment && type != ConceptMatchType.none) {
				generator.writeStringField("value", conceptMatchString(concept, conceptMatch));
			}
		}
		if (!main) {
			generator.writeNumberField("score", conceptMatch.getScore());
		}
		generator.writeEndObject();
	}

	private static void score(ScoreArgs scoreArgs, Branch branch, Match match, JsonGenerator generator) throws IOException {
		generator.writeFieldName("score");
		generator.writeStartObject();
		generator.writeStringField("class", Report.getScoreClass(scoreArgs, branch, match));
		generator.writeNumberField("bestOneScore", match.getBestOneScore());
		generator.writeNumberField("withoutPathScore", match.getWithoutPathScore());
		generator.writeNumberField("score", match.getScore());
		generator.writeEndObject();
	}

	private static JsonGenerator createGenerator(StringWriter writer, Path json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
		JsonFactory factory = mapper.getFactory();
		JsonGenerator generator;
		if (json == null) {
			generator = factory.createGenerator(writer);
		} else {
			generator = factory.createGenerator(json.toFile(), JsonEncoding.UTF8);
		}
		generator.useDefaultPrettyPrinter();
		return generator;
	}

	// concepts must contain the key match.getEdamUri(), but also all keys match.getParents(), match.getChildren(), etc
	public static String output(CoreArgs args, List<ArgMain> argsMain, Map<String, String> jsonFields, QueryType type, JsonType jsonType, Path json, Map<EdamUri, Concept> concepts, List<Query> queries, List<List<Publication>> publicationsAll, List<List<Webpage>> webpagesAll, List<List<Webpage>> docsAll, Results results, long start, long stop, Version version, String jsonVersion) throws IOException {
		StringWriter writer = new StringWriter();
		JsonGenerator generator = createGenerator(writer, json);
		generator.writeStartObject();

		generator.writeBooleanField("success", true);

		if (jsonVersion != null) {
			generator.writeStringField(VERSION_ID, jsonVersion);
		}

		generator.writeStringField(TYPE_ID, jsonType.name());

		boolean server = (type == QueryType.server);
		if (server && queries.size() != 1) {
			throw new IllegalArgumentException("Number of queries must be 1");
		}
		boolean full = (jsonType == JsonType.full || jsonType == JsonType.cli);

		if (jsonFields != null) {
			for (Map.Entry<String, String> jsonField : jsonFields.entrySet()) {
				generator.writeStringField(jsonField.getKey(), jsonField.getValue());
			}
		}

		generator.writeFieldName("generator");
		generator.writeObject(version);

		generator.writeFieldName("time");
		generator.writeStartObject();
		generator.writeNumberField("start", start);
		generator.writeStringField("startHuman", Instant.ofEpochMilli(start).toString());
		generator.writeNumberField("stop", stop);
		generator.writeStringField("stopHuman", Instant.ofEpochMilli(stop).toString());
		generator.writeNumberField("duration", (stop - start) / 1000.0);
		generator.writeEndObject();

		if (server) {
			generator.writeFieldName("mapping");
		} else {
			generator.writeNumberField("mappingSize", queries.size());
			generator.writeFieldName("mapping");
			generator.writeStartArray();
		}
		for (int i = 0; i < queries.size(); ++i) {
			Query query = queries.get(i);
			List<Webpage> webpages = webpagesAll.get(i);
			List<Webpage> docs = docsAll.get(i);
			List<Publication> publications = publicationsAll.get(i);
			MappingTest mapping = results.getMappings().get(i);

			generator.writeStartObject();

			generator.writeFieldName("query");
			generator.writeStartObject();

			if (server || query.getId() == null) {
				generator.writeStringField(Query.ID, query.getId());
			} else {
				generator.writeStringField(Query.ID, QueryLoader.BIOTOOLS + query.getId());
			}

			generator.writeStringField(Query.NAME, query.getName());

			generator.writeFieldName(Query.KEYWORDS);
			if (query.getKeywords() != null) {
				generator.writeStartArray();
				for (Keyword keyword : query.getKeywords()) {
					if (server) {
						generator.writeString(keyword.getValue());
					} else {
						generator.writeObject(keyword);
					}
				}
				generator.writeEndArray();
			} else {
				generator.writeObject(null);
			}

			generator.writeStringField(Query.DESCRIPTION, query.getDescription());

			generator.writeFieldName(Query.WEBPAGE_URLS);
			if (query.getWebpageUrls() != null) {
				generator.writeStartArray();
				for (Link webpageUrl : query.getWebpageUrls()) {
					if (server) {
						generator.writeString(webpageUrl.getUrl());
					} else {
						generator.writeObject(webpageUrl);
					}
				}
				generator.writeEndArray();
			} else {
				generator.writeObject(null);
			}

			generator.writeFieldName(Query.DOC_URLS);
			if (query.getDocUrls() != null) {
				generator.writeStartArray();
				for (Link docUrl : query.getDocUrls()) {
					if (server) {
						generator.writeString(docUrl.getUrl());
					} else {
						generator.writeObject(docUrl);
					}
				}
				generator.writeEndArray();
			} else {
				generator.writeObject(null);
			}

			generator.writeFieldName(Query.PUBLICATION_IDS);
			if (query.getPublicationIds() != null) {
				generator.writeStartArray();
				for (PublicationIdsQuery publicationIds : query.getPublicationIds()) {
					if (server) {
						generator.writeStartObject();
						generator.writeStringField("pmid", publicationIds.getPmid());
						generator.writeStringField("pmcid", publicationIds.getPmcid());
						generator.writeStringField("doi", publicationIds.getDoi());
						generator.writeEndObject();
					} else {
						generator.writeObject(publicationIds);
					}
				}
				generator.writeEndArray();
			} else {
				generator.writeObject(null);
			}

			generator.writeFieldName(Query.ANNOTATIONS);
			if (query.getAnnotations() != null) {
				generator.writeStartArray();
				for (EdamUri edamUri : query.getAnnotations()) {
					generator.writeString(edamUri.toString());
				}
				generator.writeEndArray();
			} else {
				generator.writeObject(null);
			}

			generator.writeEndObject();

			if (full) {
				generator.writeFieldName("queryFetched");
				generator.writeStartObject();

				generator.writeFieldName("webpages");
				generator.writeStartArray();
				for (Webpage webpage : webpages) {
					if (webpage != null) {
						webpage.toStringJson(generator, args.getFetcherArgs(), false);
					} else {
						generator.writeNull();
					}
				}
				generator.writeEndArray();

				generator.writeFieldName("docs");
				generator.writeStartArray();
				for (Webpage doc : docs) {
					if (doc != null) {
						doc.toStringJson(generator, args.getFetcherArgs(), false);
					} else {
						generator.writeNull();
					}
				}
				generator.writeEndArray();

				generator.writeFieldName("publications");
				generator.writeStartArray();
				for (Publication publication : publications) {
					if (publication != null) {
						publication.toStringJson(generator, args.getFetcherArgs(), false);
					} else {
						generator.writeNull();
					}
				}
				generator.writeEndArray();

				generator.writeEndObject();
			}

			generator.writeFieldName("results");
			generator.writeStartObject();

			for (Branch branch : Branch.values()) {
				generator.writeFieldName(branch.name());
				List<MatchTest> matches = mapping.getMatches(branch);
				if (matches.isEmpty() && !args.getMapperArgs().getBranches().contains(branch)) {
					generator.writeObject(null);
					continue;
				}
				generator.writeStartArray();
				for (MatchTest matchTest : matches) {
					generator.writeStartObject();
					Match match = matchTest.getMatch();
					concept(concepts, match, generator);
					queryMatch(query, publications, match.getQueryMatch(), true, generator);
					Concept concept = concepts.get(match.getEdamUri());
					conceptMatch(concept, match.getConceptMatch(), true, generator);
					score(args.getMapperArgs().getScoreArgs(), branch, match, generator);
					generator.writeStringField("test", matchTest.getTest().name());
					if (full) {
						List<MatchAverageStats> matchAverageStats = match.getMatchAverageStats();
						if (matchAverageStats != null && !matchAverageStats.isEmpty()) {
							generator.writeFieldName("parts");
							generator.writeStartArray();
							for (MatchAverageStats mas : matchAverageStats) {
								generator.writeStartObject();
								queryMatch(query, publications, mas.getQueryMatch(), false, generator);
								conceptMatch(concept, mas.getConceptMatch(), false, generator);
								generator.writeNumberField("score", mas.getScore());
								generator.writeEndObject();
							}
							generator.writeEndArray();
						}
					}
					generator.writeEndObject();
				}
				generator.writeEndArray();
			}

			generator.writeEndObject();

			generator.writeEndObject();
		}
		if (!server) {
			generator.writeEndArray();
		}

		generator.writeFieldName("args");
		generator.writeStartObject();
		Params.writeMain(argsMain, generator);
		Params.writeProcessing(args.getProcessorArgs(), generator);
		Params.writePreProcessing(args.getPreProcessorArgs(), generator);
		Params.writeFetching(args.getFetcherArgs(), !server, generator);
		Params.writeMapping(args.getMapperArgs(), generator);
		generator.writeEndObject();

		if (full) {
			Params.writeBenchmarking(concepts, queries, results, generator);
		}

		generator.writeEndObject();
		generator.close();
		if (json == null) {
			return writer.toString();
		} else {
			return null;
		}
	}

	public static String fromDatabaseEntries(String key, List<DatabaseEntryEntry> databaseEntries, FetcherArgs fetcherArgs) throws IOException {
		StringWriter writer = new StringWriter();
		JsonGenerator generator = createGenerator(writer, null);
		generator.writeStartObject();

		generator.writeBooleanField("success", true);

		generator.writeFieldName(key);

		generator.writeStartArray();
		for (DatabaseEntryEntry databaseEntry : databaseEntries) {
			generator.writeStartObject();
			if (databaseEntry.getType() == DatabaseEntryType.publication) {
				Publication publication = (Publication) databaseEntry.getEntry();
				generator.writeFieldName("id");
				generator.writeStartObject();
				generator.writeStringField("pmid", publication.getPmid().getContent());
				generator.writeStringField("pmcid", publication.getPmcid().getContent());
				generator.writeStringField("doi", publication.getDoi().getContent());
				generator.writeEndObject();
			} else {
				generator.writeStringField("id", databaseEntry.getEntry().toStringId());
			}
			generator.writeStringField("status", databaseEntry.getEntry().getStatusString(fetcherArgs));
			generator.writeEndObject();
		}
		generator.writeEndArray();

		generator.writeEndObject();
		generator.close();
		return writer.toString();
	}

	public static String fromAnnotations(Map<EdamUri, Concept> annotations) throws IOException {
		StringWriter writer = new StringWriter();
		JsonGenerator generator = createGenerator(writer, null);
		generator.writeStartObject();

		generator.writeBooleanField("success", true);

		generator.writeFieldName(Query.ANNOTATIONS);

		generator.writeStartArray();
		for (Map.Entry<EdamUri, Concept> annotation : annotations.entrySet()) {
			generator.writeStartObject();
			generator.writeStringField("uri", annotation.getKey().toString());
			generator.writeStringField("label", annotation.getValue().getLabel());
			generator.writeEndObject();
		}
		generator.writeEndArray();

		generator.writeEndObject();
		generator.close();
		return writer.toString();
	}

	public static void outputBiotools(Writer writer, List<Tool> tools) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
		JsonBiotools jsonBiotools = new JsonBiotools();
		jsonBiotools.setCount(tools.size());
		jsonBiotools.setList(tools);
		mapper.writeValue(writer, jsonBiotools);
	}

	private static boolean existingAnnotation(List<Edam> annotations, EdamUri edamUri) {
		for (Edam annotation : annotations) {
			if (annotation.getUri().equals(edamUri.getUri())) {
				return true;
			}
		}
		return false;
	}

	private static Edam getEdam(EdamUri edamUri, Map<EdamUri, Concept> concepts) {
		Edam edam = new Edam();
		edam.setUri(edamUri.toString());
		edam.setTerm(concepts.get(edamUri).getLabel());
		return edam;
	}

	private static String getEdamString(EdamUri edamUri, Map<EdamUri, Concept> concepts) {
		return edamUri.toString() + " (" + concepts.get(edamUri).getLabel() + ")";
	}

	public static void outputBiotools(CoreArgs args, String queryPath, Path biotoolsPath, Map<EdamUri, Concept> concepts, Results results, boolean trim) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);

		Biotools biotools = null;
		try (InputStream is = Input.newInputStream(queryPath, true, args.getFetcherArgs().getTimeout(), args.getFetcherArgs().getPrivateArgs().getUserAgent())) {
			biotools = mapper.readValue(is, Biotools.class);
		}

		if (biotools.getList().size() != results.getMappings().size()) {
			throw new RuntimeException("Number of results (" + results.getMappings().size() + ") does not correspond to number of tools (" + biotools.getList().size() + ") from " + queryPath);
		}

		for (int i = 0; i < biotools.getList().size(); ++i) {
			ToolInput tool = biotools.getList().get(i);
			MappingTest mapping = results.getMappings().get(i);
			if (tool.getBiotoolsID() != null && !tool.getBiotoolsID().equals(mapping.getId())) {
				throw new RuntimeException("Tool ID from " + queryPath + " (" + tool.getBiotoolsID() + ") does not correspond to ID from results (" + mapping.getId() + ")");
			}
			if (!tool.getName().equals(mapping.getName())) {
				throw new RuntimeException("Tool name from " + queryPath + " (" + tool.getName() + ") does not correspond to name from results (" + mapping.getName() + ")");
			}
		}

		for (int i = 0; i < biotools.getList().size(); ++i) {
			ToolInput tool = biotools.getList().get(i);
			MappingTest mapping = results.getMappings().get(i);

			if (args.getMapperArgs().getBranches().contains(Branch.topic)) {
				List<Edam> topic = tool.getTopic();
				for (MatchTest match : mapping.getMatches(Branch.topic)) {
					EdamUri edamUri = match.getMatch().getEdamUri();
					if (!existingAnnotation(topic, edamUri)) {
						topic.add(getEdam(edamUri, concepts));
					}
				}
			}

			if (args.getMapperArgs().getBranches().contains(Branch.operation)) {
				Function newFunction = new Function();

				for (MatchTest match : mapping.getMatches(Branch.operation)) {
					EdamUri edamUri = match.getMatch().getEdamUri();
					boolean existing = false;
					for (Function function : tool.getFunction()) {
						if (existingAnnotation(function.getOperation(), edamUri)) {
							existing = true;
							break;
						}
					}
					if (!existing) {
						newFunction.getOperation().add(getEdam(edamUri, concepts));
					}
				}

				if (!newFunction.getOperation().isEmpty()) {
					tool.getFunction().add(newFunction);
				}

				if (!tool.getFunction().isEmpty()) {
					Function lastFunction = tool.getFunction().get(tool.getFunction().size() - 1);

					String note = lastFunction.getNote();
					if (note == null) {
						note = "";
					}
					List<String> data = new ArrayList<>();
					List<String> format = new ArrayList<>();

					if (args.getMapperArgs().getBranches().contains(Branch.data)) {
						for (MatchTest match : mapping.getMatches(Branch.data)) {
							EdamUri edamUri = match.getMatch().getEdamUri();
							boolean existing = false;
							for (Function function : tool.getFunction()) {
								for (InputOutput input : function.getInput()) {
									if (input.getData().getUri().equals(edamUri.getUri())) {
										existing = true;
										break;
									}
								}
								if (existing) {
									break;
								}
								for (InputOutput output : function.getOutput()) {
									if (output.getData().getUri().equals(edamUri.getUri())) {
										existing = true;
										break;
									}
								}
								if (existing) {
									break;
								}
							}
							if (!existing) {
								data.add(getEdamString(edamUri, concepts));
							}
						}
					}

					if (args.getMapperArgs().getBranches().contains(Branch.format)) {
						for (MatchTest match : mapping.getMatches(Branch.format)) {
							EdamUri edamUri = match.getMatch().getEdamUri();
							boolean existing = false;
							for (Function function : tool.getFunction()) {
								for (InputOutput input : function.getInput()) {
									if (existingAnnotation(input.getFormat(), edamUri)) {
										existing = true;
										break;
									}
								}
								if (existing) {
									break;
								}
								for (InputOutput output : function.getOutput()) {
									if (existingAnnotation(output.getFormat(), edamUri)) {
										existing = true;
										break;
									}
								}
								if (existing) {
									break;
								}
							}
							if (!existing) {
								format.add(getEdamString(edamUri, concepts));
							}
						}
					}

					boolean exceeded = false;
					if (note.length() + (note.isEmpty() ? 0 : 3) + 3 > FUNCTION_NOTE_MAX) {
						exceeded = true;
					}
					if (!exceeded) {
						for (String d : data) {
							if (note.length() + (note.isEmpty() ? 0 : 3) + d.length() + 3 + 3 > FUNCTION_NOTE_MAX) {
								if (!note.isEmpty()) {
									note += " | ";
								}
								note += "...";
								exceeded = true;
								break;
							} else {
								if (!note.isEmpty()) {
									note += " | ";
								}
								note += d;
							}
						}
					}
					if (!exceeded) {
						for (String f : format) {
							if (note.length() + (note.isEmpty() ? 0 : 3) + f.length() + 3 + 3 > FUNCTION_NOTE_MAX) {
								if (!note.isEmpty()) {
									note += " | ";
								}
								note += "...";
								exceeded = true;
								break;
							} else {
								if (!note.isEmpty()) {
									note += " | ";
								}
								note += f;
							}
						}
					}

					if (note.length() >= FUNCTION_NOTE_MIN) {
						if (note.length() > FUNCTION_NOTE_MAX) {
							note = note.substring(0, FUNCTION_NOTE_MAX);
						}
						lastFunction.setNote(note);
					}
				}
			}
		}

		if (trim) {
			JsonBiotools jsonBiotools = new JsonBiotools();
			jsonBiotools.setCount(biotools.getCount());
			jsonBiotools.setList(biotools.getList().stream().map(t -> t.trim()).collect(Collectors.toList()));
			mapper.writeValue(biotoolsPath.toFile(), jsonBiotools);
		} else {
			mapper.writeValue(biotoolsPath.toFile(), biotools);
		}
	}
}
