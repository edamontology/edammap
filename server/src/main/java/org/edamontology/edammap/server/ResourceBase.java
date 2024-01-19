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

package org.edamontology.edammap.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.Request;

import org.edamontology.pubfetcher.core.common.FetcherArgs;
import org.edamontology.pubfetcher.core.common.FetcherPrivateArgs;
import org.edamontology.pubfetcher.core.common.IllegalRequestException;
import org.edamontology.pubfetcher.core.common.Version;
import org.edamontology.pubfetcher.core.db.DatabaseEntryType;

import org.edamontology.edammap.core.args.CoreArgs;
import org.edamontology.edammap.core.benchmarking.Benchmark;
import org.edamontology.edammap.core.benchmarking.Results;
import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.idf.Idf;
import org.edamontology.edammap.core.input.DatabaseEntryId;
import org.edamontology.edammap.core.input.ServerInput;
import org.edamontology.edammap.core.input.json.Tool;
import org.edamontology.edammap.core.mapping.Mapper;
import org.edamontology.edammap.core.mapping.Mapping;
import org.edamontology.edammap.core.output.DatabaseEntryEntry;
import org.edamontology.edammap.core.output.Json;
import org.edamontology.edammap.core.preprocessing.PreProcessor;
import org.edamontology.edammap.core.processing.ConceptProcessed;
import org.edamontology.edammap.core.processing.Processor;
import org.edamontology.edammap.core.processing.ProcessorArgs;
import org.edamontology.edammap.core.processing.QueryProcessed;
import org.edamontology.edammap.core.query.Query;
import org.edamontology.edammap.core.query.QueryLoader;
import org.edamontology.edammap.core.query.QueryType;

public abstract class ResourceBase {

	private static final Logger logger = LogManager.getLogger();

	private static final String VERSION_ID = "version";

	private static final int RETRY_LIMIT_DEFAULT = 0;
	private static final int TIMEOUT_DEFAULT = 5000;
	private static final boolean QUICK_DEFAULT = true;

	public static final int MAX_NAME_LENGTH = 1000;
	public static final int MAX_KEYWORDS_LENGTH = 10000;
	public static final int MAX_DESCRIPTION_LENGTH = 100000;
	public static final int MAX_LINKS_LENGTH = 10000;
	public static final int MAX_PUBLICATION_IDS_LENGTH = 10000;
	public static final int MAX_ANNOTATIONS_LENGTH = 10000;

	protected static final int MAX_KEYWORDS_SIZE = 100;
	protected static final int MAX_LINKS_SIZE = 10;
	protected static final int MAX_PUBLICATION_IDS_SIZE = 10;

	private static final int MAX_JSON_TOOL_LENGTH = 1000000;

	protected class PostResult {
		protected final String jsonString;
		protected final URI htmlLocation;
		public PostResult(String jsonString, URI htmlLocation) {
			this.jsonString = jsonString;
			this.htmlLocation = htmlLocation;
		}
	}

	protected static CoreArgs newCoreArgs(MultivaluedMap<String, String> params, boolean json, ProcessorArgs processorArgs, FetcherPrivateArgs fetcherPrivateArgs) {
		CoreArgs coreArgs = new CoreArgs(RETRY_LIMIT_DEFAULT, TIMEOUT_DEFAULT, QUICK_DEFAULT);
		ParamParse.parseParams(params, coreArgs, json);
		coreArgs.setProcessorArgs(processorArgs);
		coreArgs.getFetcherArgs().setPrivateArgs(fetcherPrivateArgs);
		return coreArgs;
	}

	protected String jsonVersion(MultivaluedMap<String, String> params, boolean isJson) {
		String jsonVersion = null;
		if (isJson) {
			if ((jsonVersion = ParamParse.getParamString(params, VERSION_ID)) != null) {
				if (!jsonVersion.equals("1")) {
					throw new IllegalRequestException("Illegal API version: '" + jsonVersion + "'; possible values: '1'");
				}
			} else {
				jsonVersion = "1";
			}
		} else {
			jsonVersion = "1";
		}
		return jsonVersion;
	}

	protected ServerInput getServerInput(MultivaluedMap<String, String> params, boolean isJson, Tool tool, boolean nameMandatory, boolean publicationIdsMandatory) {
		ServerInput serverInput = null;
		if (isJson) {
			if (tool == null) {
				serverInput = new ServerInput(
					ParamParse.getParamStrings(params, Query.NAME),
					ParamParse.getParamStrings(params, Query.KEYWORDS),
					ParamParse.getParamStrings(params, Query.DESCRIPTION),
					ParamParse.getParamStrings(params, Query.WEBPAGE_URLS),
					ParamParse.getParamStrings(params, Query.DOC_URLS),
					ParamParse.getParamStrings(params, Query.PUBLICATION_IDS),
					ParamParse.getParamStrings(params, Query.ANNOTATIONS),
					nameMandatory, publicationIdsMandatory);
			}
		} else {
			serverInput = new ServerInput(
				ParamParse.getParamString(params, Query.NAME),
				ParamParse.getParamString(params, Query.KEYWORDS),
				ParamParse.getParamString(params, Query.DESCRIPTION),
				ParamParse.getParamString(params, Query.WEBPAGE_URLS),
				ParamParse.getParamString(params, Query.DOC_URLS),
				ParamParse.getParamString(params, Query.PUBLICATION_IDS),
				ParamParse.getParamString(params, Query.ANNOTATIONS),
				nameMandatory, publicationIdsMandatory);
		}
		return serverInput;
	}

	protected void checkInput(ServerInput serverInput, Tool tool) {
		if (serverInput != null) {
			if (serverInput.getName() != null && serverInput.getName().length() > MAX_NAME_LENGTH) {
				throw new IllegalRequestException("Name length (" + serverInput.getName().length() + ") is greater than maximum allowed (" + MAX_NAME_LENGTH + ")");
			}
			if (serverInput.getKeywords() != null && serverInput.getKeywords().length() > MAX_KEYWORDS_LENGTH) {
				throw new IllegalRequestException("Keywords length (" + serverInput.getKeywords().length() + ") is greater than maximum allowed (" + MAX_KEYWORDS_LENGTH + ")");
			}
			if (serverInput.getDescription() != null && serverInput.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
				throw new IllegalRequestException("Description length (" + serverInput.getDescription().length() + ") is greater than maximum allowed (" + MAX_DESCRIPTION_LENGTH + ")");
			}
			if (serverInput.getWebpageUrls() != null && serverInput.getWebpageUrls().length() > MAX_LINKS_LENGTH) {
				throw new IllegalRequestException("Webpage URLs length (" + serverInput.getWebpageUrls().length() + ") is greater than maximum allowed (" + MAX_LINKS_LENGTH + ")");
			}
			if (serverInput.getDocUrls() != null && serverInput.getDocUrls().length() > MAX_LINKS_LENGTH) {
				throw new IllegalRequestException("Doc URLs length (" + serverInput.getDocUrls().length() + ") is greater than maximum allowed (" + MAX_LINKS_LENGTH + ")");
			}
			if (serverInput.getPublicationIds() != null && serverInput.getPublicationIds().length() > MAX_PUBLICATION_IDS_LENGTH) {
				throw new IllegalRequestException("Publication IDs length (" + serverInput.getPublicationIds().length() + ") is greater than maximum allowed (" + MAX_PUBLICATION_IDS_LENGTH + ")");
			}
			if (serverInput.getAnnotations() != null && serverInput.getAnnotations().length() > MAX_ANNOTATIONS_LENGTH) {
				throw new IllegalRequestException("Annotations length (" + serverInput.getAnnotations().length() + ") is greater than maximum allowed (" + MAX_ANNOTATIONS_LENGTH + ")");
			}
		} else {
			if (tool.getName() != null && tool.getName().length() > MAX_NAME_LENGTH) {
				throw new IllegalRequestException("Name length (" + tool.getName().length() + ") is greater than maximum allowed (" + MAX_NAME_LENGTH + ")");
			}
			if (tool.getDescription() != null && tool.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
				throw new IllegalRequestException("Description length (" + tool.getDescription().length() + ") is greater than maximum allowed (" + MAX_DESCRIPTION_LENGTH + ")");
			}
		}
	}

	protected String getUuid(String uuidSuffix, String uuidDirPrefix, Version version, Instant startInstant) throws IOException {
		String uuidButLast;
		String uuid;
		do {
			uuidButLast = version.getVersion() + "/" + DateTimeFormatter.ofPattern("uuuu-MM").format(LocalDateTime.ofInstant(startInstant, ZoneId.of("Z")));
			uuid = uuidButLast + "/" + UUID.randomUUID().toString();
			uuid += uuidSuffix;
		} while (Files.exists(Paths.get(uuidDirPrefix + uuid)));
		Files.createDirectories(Paths.get(uuidDirPrefix + uuidButLast));
		Files.createDirectory(Paths.get(uuidDirPrefix + uuid));
		logger.info("UUID: {}", uuid);
		return uuid;
	}

	protected Map<EdamUri, ConceptProcessed> getProcessedConcepts(CoreArgs coreArgs, PreProcessor preProcessor) {
		logger.info("Processing {} concepts", getConcepts().size());
		long startConcepts = System.currentTimeMillis();
		Map<EdamUri, ConceptProcessed> processedConcepts = getProcessor().getProcessedConcepts(getConcepts(),
			coreArgs.getMapperArgs().getIdfArgs(), coreArgs.getMapperArgs().getMultiplierArgs(), preProcessor);
		logger.info("Processing concepts took {}s", (System.currentTimeMillis() - startConcepts) / 1000.0);
		return processedConcepts;
	}

	protected Query getQuery(ServerInput serverInput, Tool tool, boolean toolMissingId, boolean isHomepageDoc, boolean homepageMissing) {
		Query query;
		if (serverInput != null) {
			query = QueryLoader.fromServer(serverInput, getConcepts(), MAX_KEYWORDS_SIZE, MAX_LINKS_SIZE, MAX_PUBLICATION_IDS_SIZE);
		} else {
			query = QueryLoader.getBiotools(tool, getConcepts(), MAX_LINKS_SIZE, MAX_PUBLICATION_IDS_SIZE, QueryType.server.name(), isHomepageDoc, homepageMissing);
			if (toolMissingId) {
				tool.setBiotoolsID(null);
			}
		}
		return query;
	}

	protected QueryProcessed getProcessedQuery(CoreArgs coreArgs, Idf idf, Idf idfStemmed, Query query, PreProcessor preProcessor) {
		logger.info("Loading query");
		long startQuery = System.currentTimeMillis();

		Idf idfChosen;
		if (coreArgs.getPreProcessorArgs().isStemming()) {
			idfChosen = idfStemmed;
		} else {
			idfChosen = idf;
		}

		QueryProcessed processedQuery = getProcessor().getProcessedQuery(query, QueryType.server, preProcessor, idfChosen, coreArgs.getFetcherArgs(), getServerPrivateArgs().getFetchingThreads());

		logger.info("Loading query took {}s", (System.currentTimeMillis() - startQuery) / 1000.0);

		return processedQuery;
	}

	protected Results getResults(Map<EdamUri, ConceptProcessed> processedConcepts, Query query, List<Query> queries, QueryProcessed processedQuery, CoreArgs coreArgs, Set<EdamUri> edamBlacklist) {
		logger.info("Mapping query");
		long startMapping = System.currentTimeMillis();

		Mapping mapping = new Mapper(processedConcepts, edamBlacklist).map(query, processedQuery, coreArgs.getMapperArgs());

		List<Mapping> mappings = Collections.singletonList(mapping);
		Results results = Benchmark.calculate(queries, mappings);

		logger.info("Mapping query took {}s", (System.currentTimeMillis() - startMapping) / 1000.0);

		return results;
	}

	protected abstract PostResult runPost(MultivaluedMap<String, String> params, Tool tool, Request request, boolean isJson) throws IOException, URISyntaxException, ParseException;

	private String toJsonString(JsonValue jsonValue) {
		if (jsonValue == null) return "";
		switch (jsonValue.getValueType()) {
			case STRING: return ((JsonString) jsonValue).getString();
			case NUMBER: return ((JsonNumber) jsonValue).toString();
			case TRUE: return "true";
			case FALSE: return "false";
			default: return "";
		}
	}

	private MultivaluedHashMap<String, String> parseJson(JsonObject json) {
		return json.entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, e -> {
				switch (e.getValue().getValueType()) {
					case STRING: case NUMBER: case TRUE: case FALSE: return Collections.singletonList(toJsonString(e.getValue()));
					case ARRAY:
						List<String> array = new ArrayList<>();
						for (JsonValue value : (JsonArray) e.getValue()) {
							switch (value.getValueType()) {
								case STRING: case NUMBER: case TRUE: case FALSE: array.add(toJsonString(value)); break;
								case OBJECT:
									if (e.getKey().equals(Query.PUBLICATION_IDS)) {
										JsonObject object = (JsonObject) value;
										array.add(toJsonString(object.get("pmid")) + "\t" + toJsonString(object.get("pmcid")) + "\t" + toJsonString(object.get("doi")));
									}
									break;
								default: break;
							}
						}
						return array;
					default: return Collections.emptyList();
				}
			}, (k, v) -> { throw new AssertionError(); }, MultivaluedHashMap<String, String>::new));
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String json(JsonObject json, @Context Request request) throws IOException, URISyntaxException, ParseException {
		try {
			logger.info("POST JSON {} from {}", json, request.getRemoteAddr());

			JsonObject tool;
			try {
				tool = json.getJsonObject("tool");
			} catch (ClassCastException e) {
				throw new IllegalRequestException(e);
			}
			String toolString = null;
			if (tool != null) {
				toolString = tool.toString();
				if (toolString.length() > MAX_JSON_TOOL_LENGTH) {
					throw new IllegalRequestException("\"tool\" length (" + toolString.length() + ") is greater than maximum allowed (" + MAX_JSON_TOOL_LENGTH + ")");
				}
			}

			PostResult postResult = runPost(parseJson(json), toolString != null ? Tool.fromString(toolString) : null, request, true);

			return postResult.jsonString;
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}

	protected abstract FetcherPrivateArgs getFetcherPrivateArgs();

	protected abstract ServerPrivateArgsBase getServerPrivateArgs();

	protected abstract Map<EdamUri, Concept> getConcepts();

	protected abstract Processor getProcessor();

	protected Response patch(JsonObject json, String key, Request request, String resource, DatabaseEntryType type, int max) throws IOException {
		logger.info("PATCH JSON {} {} from {}", resource, json, request.getRemoteAddr());
		MultivaluedHashMap<String, String> params = parseJson(json);
		List<String> databaseEntryIds = params.get(key);
		String requestString = "";
		if (databaseEntryIds != null) {
			requestString = String.join("\n", databaseEntryIds);
		}
		logger.info("PATCH {} {} from {}", resource, requestString, request.getRemoteAddr());
		FetcherArgs fetcherArgs = new FetcherArgs(RETRY_LIMIT_DEFAULT, TIMEOUT_DEFAULT, QUICK_DEFAULT);
		ParamParse.parseFetcherParams(params, fetcherArgs, true);
		fetcherArgs.setPrivateArgs(getFetcherPrivateArgs());
		List<DatabaseEntryId> ids = new ArrayList<>();
		for (Object id : QueryLoader.fromServerEntry(requestString, type, max)) {
			ids.add(new DatabaseEntryId(id, type));
		}
		List<DatabaseEntryEntry> databaseEntries = getProcessor().getDatabaseEntries(ids, fetcherArgs, getServerPrivateArgs().getFetchingThreads());
		Response response = Response.ok(Json.fromDatabaseEntries(key, databaseEntries, fetcherArgs)).type(MediaType.APPLICATION_JSON).build();
		logger.info("PATCHED {} {}", resource, response.getEntity());
		return response;
	}

	@Path("web")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response patchWeb(JsonObject json, @Context Request request) throws IOException {
		try {
			return patch(json, Query.WEBPAGE_URLS, request, "/web", DatabaseEntryType.webpage, MAX_LINKS_SIZE);
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}

	@Path("doc")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response patchDoc(JsonObject json, @Context Request request) throws IOException {
		try {
			return patch(json, Query.DOC_URLS, request, "/doc", DatabaseEntryType.doc, MAX_LINKS_SIZE);
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}

	@Path("pub")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response patchPub(JsonObject json, @Context Request request) throws IOException {
		try {
			return patch(json, Query.PUBLICATION_IDS, request, "/pub", DatabaseEntryType.publication, MAX_PUBLICATION_IDS_SIZE);
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}

	@Path("edam")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkEdam(JsonObject json, @Context Request request) throws IOException {
		try {
			logger.info("CHECK JSON /edam {} from {}", json, request.getRemoteAddr());
			MultivaluedHashMap<String, String> params = parseJson(json);
			List<String> annotations = params.get(Query.ANNOTATIONS);
			String requestString = "";
			if (annotations != null) {
				requestString = String.join("\n", annotations);
			}
			logger.info("CHECK /edam {} from {}", requestString, request.getRemoteAddr());
			Response response = Response.ok(Json.fromAnnotations(QueryLoader.fromServerEdam(requestString, getConcepts()))).type(MediaType.APPLICATION_JSON).build();
			logger.info("CHECKED /edam {}", response.getEntity());
			return response;
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}
}
