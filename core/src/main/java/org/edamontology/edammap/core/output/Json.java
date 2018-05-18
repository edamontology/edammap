/*
 * Copyright © 2018 Erik Jaaniso
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
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.edamontology.edammap.core.args.CoreArgs;
import org.edamontology.edammap.core.benchmarking.MappingTest;
import org.edamontology.edammap.core.benchmarking.MatchTest;
import org.edamontology.edammap.core.benchmarking.Results;
import org.edamontology.edammap.core.edam.Branch;
import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.EdamUri;
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
import org.edamontology.pubfetcher.DatabaseEntry;
import org.edamontology.pubfetcher.FetcherArgs;
import org.edamontology.pubfetcher.FetcherCommon;
import org.edamontology.pubfetcher.Publication;
import org.edamontology.pubfetcher.PublicationPart;
import org.edamontology.pubfetcher.PublicationPartList;
import org.edamontology.pubfetcher.PublicationPartName;
import org.edamontology.pubfetcher.PublicationPartString;
import org.edamontology.pubfetcher.Version;
import org.edamontology.pubfetcher.Webpage;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Json {

	public static final String TYPE_ID = "type";

	private static void webpageMeta(Webpage webpage, JsonGenerator generator, FetcherArgs fetcherArgs) throws IOException {
		generator.writeStartObject();
		generator.writeNumberField("fetchTime", webpage.getFetchTime());
		generator.writeStringField("fetchTimeHuman", webpage.getFetchTimeHuman());
		generator.writeNumberField("retryCounter", webpage.getRetryCounter());
		generator.writeStringField("startUrl", webpage.getStartUrl());
		generator.writeStringField("finalUrl", webpage.getFinalUrl());
		generator.writeStringField("contentType", webpage.getContentType());
		generator.writeNumberField("statusCode", webpage.getStatusCode());
		generator.writeNumberField("contentTime", webpage.getContentTime());
		generator.writeStringField("contentTimeHuman", webpage.getContentTimeHuman());
		generator.writeNumberField("titleLength", webpage.getTitle().length());
		generator.writeNumberField("contentLength", webpage.getContent().length());
		generator.writeStringField("title", webpage.getTitle());
		generator.writeBooleanField("broken", webpage.isBroken());
		generator.writeBooleanField("empty", webpage.isEmpty());
		generator.writeBooleanField("final", webpage.isFinal(fetcherArgs));
		generator.writeBooleanField("usable", webpage.isUsable(fetcherArgs));
		generator.writeEndObject();
	}

	private static void publicationPartMeta(Publication publication, PublicationPartName name, JsonGenerator generator, FetcherArgs fetcherArgs) throws IOException {
		PublicationPart part = publication.getPart(name);
		generator.writeStringField("type", part.getType().name());
		generator.writeStringField("url", part.getUrl());
		generator.writeNumberField("timestamp", part.getTimestamp());
		generator.writeStringField("timestampHuman", part.getTimestampHuman());
		generator.writeNumberField("size", part.getSize());
		generator.writeBooleanField("empty", part.isEmpty());
		generator.writeBooleanField("final", publication.isPartFinal(name, fetcherArgs));
		generator.writeBooleanField("usable", publication.isPartUsable(name, fetcherArgs));
	}

	private static void publicationPartStringMeta(Publication publication, PublicationPartName name, JsonGenerator generator, FetcherArgs fetcherArgs) throws IOException {
		PublicationPartString part = (PublicationPartString) publication.getPart(name);
		generator.writeFieldName(name.name());
		generator.writeStartObject();
		generator.writeStringField("content", part.getContent());
		publicationPartMeta(publication, name, generator, fetcherArgs);
		generator.writeEndObject();
	}

	private static void publicationPartListMeta(Publication publication, PublicationPartName name, JsonGenerator generator, FetcherArgs fetcherArgs) throws IOException {
		PublicationPartList<?> part = (PublicationPartList<?>) publication.getPart(name);
		generator.writeFieldName(name.name());
		generator.writeStartObject();
		generator.writeObjectField("list", part.getList());
		publicationPartMeta(publication, name, generator, fetcherArgs);
		generator.writeEndObject();
	}

	private static void publicationMeta(Publication publication, JsonGenerator generator, FetcherArgs fetcherArgs) throws IOException {
		generator.writeStartObject();

		generator.writeNumberField("fetchTime", publication.getFetchTime());
		generator.writeStringField("fetchTimeHuman", publication.getFetchTimeHuman());
		generator.writeNumberField("retryCounter", publication.getRetryCounter());

		generator.writeBooleanField("oa", publication.isOA());
		generator.writeStringField("journalTitle", publication.getJournalTitle());
		generator.writeNumberField("pubDate", publication.getPubDate());
		generator.writeNumberField("citationsCount", publication.getCitationsCount());
		generator.writeNumberField("citationsTimestamp", publication.getCitationsTimestamp());
		generator.writeStringField("citationsTimestampHuman", publication.getCitationsTimestampHuman());
		generator.writeStringField("correspAuthor", publication.getCorrespAuthor());
		generator.writeObjectField("visitedSites", publication.getVisitedSites());

		generator.writeBooleanField("empty", publication.isEmpty());
		generator.writeBooleanField("final", publication.isFinal(fetcherArgs));
		generator.writeBooleanField("totallyFinal", publication.isTotallyFinal(fetcherArgs));
		generator.writeBooleanField("usable", publication.isUsable(fetcherArgs));

		publicationPartStringMeta(publication, PublicationPartName.pmid, generator, fetcherArgs);
		publicationPartStringMeta(publication, PublicationPartName.pmcid, generator, fetcherArgs);
		publicationPartStringMeta(publication, PublicationPartName.doi, generator, fetcherArgs);
		publicationPartStringMeta(publication, PublicationPartName.title, generator, fetcherArgs);

		publicationPartListMeta(publication, PublicationPartName.keywords, generator, fetcherArgs);
		publicationPartListMeta(publication, PublicationPartName.mesh, generator, fetcherArgs);
		publicationPartListMeta(publication, PublicationPartName.efo, generator, fetcherArgs);
		publicationPartListMeta(publication, PublicationPartName.go, generator, fetcherArgs);

		generator.writeFieldName("abstract");
		generator.writeStartObject();
		generator.writeStringField("content", publication.getAbstract().getContent());
		publicationPartMeta(publication, PublicationPartName.theAbstract, generator, fetcherArgs);
		generator.writeEndObject();

		generator.writeFieldName("fulltext");
		generator.writeStartObject();
		publicationPartMeta(publication, PublicationPartName.fulltext, generator, fetcherArgs);
		generator.writeEndObject();

		generator.writeEndObject();
	}

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
		Concept concept = concepts.get(edamUri); // TODO if edamUri not in concepts
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
			return FetcherCommon.getIdLink(query.getPublicationIds().get(index));
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

	public static String output(CoreArgs args, List<ParamMain> paramsMain, Map<String, String> jsonFields, JsonType type, Path json, Map<EdamUri, Concept> concepts, List<Query> queries, List<List<Publication>> publicationsAll, List<List<Webpage>> webpagesAll, List<List<Webpage>> docsAll, Results results, long start, long stop, Version version) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
		JsonFactory factory = mapper.getFactory();
		StringWriter writer = new StringWriter();
		JsonGenerator generator;
		if (json == null) {
			generator = factory.createGenerator(writer);
		} else {
			generator = factory.createGenerator(json.toFile(), JsonEncoding.UTF8);
		}
		generator.useDefaultPrettyPrinter();
		generator.writeStartObject();

		generator.writeBooleanField("success", true);

		generator.writeStringField(TYPE_ID, type.name());

		boolean server = (type == JsonType.core || type == JsonType.full);
		if (server && queries.size() != 1) {
			throw new IllegalArgumentException("Number of queries must be 1 for type \"core\" and \"full\"");
		}
		boolean full = (type == JsonType.full || type == JsonType.biotools);

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
					webpageMeta(webpage, generator, args.getFetcherArgs());
				}
				generator.writeEndArray();

				generator.writeFieldName("docs");
				generator.writeStartArray();
				for (Webpage doc : docs) {
					webpageMeta(doc, generator, args.getFetcherArgs());
				}
				generator.writeEndArray();

				generator.writeFieldName("publications");
				generator.writeStartArray();
				for (Publication publication : publications) {
					publicationMeta(publication, generator, args.getFetcherArgs());
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
					Concept concept = concepts.get(match.getEdamUri()); // TODO if edamUri not in concepts
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
		Params.writeMain(paramsMain, generator);
		generator.writeObjectField(CoreArgs.PROCESSOR_ARGS, args.getProcessorArgs());
		generator.writeObjectField(CoreArgs.PRE_PROCESSOR_ARGS, args.getPreProcessorArgs());
		Params.writeFetching(args.getFetcherArgs(), !server, generator);
		generator.writeObjectField(CoreArgs.MAPPER_ARGS, args.getMapperArgs());
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

	public static String fromDatabaseEntries(String key, List<? extends DatabaseEntry<?>> databaseEntries, FetcherArgs fetcherArgs) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
		JsonFactory factory = mapper.getFactory();
		StringWriter writer = new StringWriter();
		JsonGenerator generator = factory.createGenerator(writer);
		generator.useDefaultPrettyPrinter();
		generator.writeStartObject();

		generator.writeFieldName(key);

		// TODO allow for choosing more complete output
		generator.writeStartArray();
		for (DatabaseEntry<?> databaseEntry : databaseEntries) {
			generator.writeStartObject();
			if (databaseEntry instanceof Publication) {
				Publication publication = (Publication) databaseEntry;
				generator.writeFieldName("id");
				generator.writeStartObject();
				generator.writeStringField("pmid", publication.getPmid().getContent());
				generator.writeStringField("pmcid", publication.getPmcid().getContent());
				generator.writeStringField("doi", publication.getDoi().getContent());
				generator.writeEndObject();
			} else {
				generator.writeStringField("id", databaseEntry.toStringId());
			}
			generator.writeStringField("status", databaseEntry.getStatusString(fetcherArgs));
			generator.writeEndObject();
		}
		generator.writeEndArray();

		generator.writeEndObject();
		generator.close();
		return writer.toString();
	}
}
