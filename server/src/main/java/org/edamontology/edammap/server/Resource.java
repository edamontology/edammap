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

package org.edamontology.edammap.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.edamontology.edammap.core.args.CoreArgs;
import org.edamontology.edammap.core.benchmarking.Benchmark;
import org.edamontology.edammap.core.benchmarking.Results;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.idf.Idf;
import org.edamontology.edammap.core.input.ServerInput;
import org.edamontology.edammap.core.mapping.Mapper;
import org.edamontology.edammap.core.mapping.Mapping;
import org.edamontology.edammap.core.output.Json;
import org.edamontology.edammap.core.output.JsonType;
import org.edamontology.edammap.core.output.Output;
import org.edamontology.edammap.core.preprocessing.PreProcessor;
import org.edamontology.edammap.core.processing.ConceptProcessed;
import org.edamontology.edammap.core.processing.QueryProcessed;
import org.edamontology.edammap.core.query.Query;
import org.edamontology.edammap.core.query.QueryLoader;
import org.edamontology.edammap.core.query.QueryType;
import org.edamontology.pubfetcher.DatabaseEntry;
import org.edamontology.pubfetcher.FetcherArgs;
import org.edamontology.pubfetcher.IllegalRequestException;
import org.edamontology.pubfetcher.Publication;
import org.edamontology.pubfetcher.Webpage;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.util.Header;

@Path("/")
public class Resource {

	private static final Logger logger = LogManager.getLogger();

	static final int MAX_NAME_LENGTH = 1000;
	static final int MAX_KEYWORDS_LENGTH = 10000;
	static final int MAX_DESCRIPTION_LENGTH = 100000;
	static final int MAX_LINKS_LENGTH = 10000;
	static final int MAX_PUBLICATION_IDS_LENGTH = 10000;
	static final int MAX_ANNOTATIONS_LENGTH = 10000;

	private static final int MAX_KEYWORDS_SIZE = 100;
	private static final int MAX_LINKS_SIZE = 10;
	private static final int MAX_PUBLICATION_IDS_SIZE = 10;

	private class PostResult {
		private final String jsonString;
		private final URI htmlLocation;
		private PostResult(String jsonString, URI htmlLocation) {
			this.jsonString = jsonString;
			this.htmlLocation = htmlLocation;
		}
	}

	static String runGet(MultivaluedMap<String, String> params, Request request) {
		try {
			logger.info("GET {} from {}", params, request.getRemoteAddr());
			CoreArgs args = new CoreArgs();
			ParamParse.parseParams(params, args, false);
			args.setProcessorArgs(Server.args.getProcessorArgs());
			args.getFetcherArgs().setPrivateArgs(Server.args.getFetcherPrivateArgs());
			boolean txt = Server.args.getTxt();
			boolean json = Server.args.getJson();
			Boolean valueBoolean;
			if ((valueBoolean = ParamParse.getParamBoolean(params, ServerArgs.TXT)) != null) {
				txt = valueBoolean;
			}
			if ((valueBoolean = ParamParse.getParamBoolean(params, ServerArgs.JSON)) != null) {
				json = valueBoolean;
			}
			return Page.get(args, txt, json);
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}

	@GET
	@Produces(MediaType.TEXT_HTML + ";charset=utf-8")
	public Response get(@Context UriInfo ui, @Context Request request) {
		String responseText = runGet(ui.getQueryParameters(), request);
		return Response.ok(responseText).header(Header.ContentLength.toString(), responseText.getBytes().length).build();
	}

	private PostResult runPost(MultivaluedMap<String, String> params, Request request, boolean isJson) throws IOException, URISyntaxException {
		logger.info("POST {} from {}", params, request.getRemoteAddr());

		long start = System.currentTimeMillis();
		Instant startInstant = Instant.ofEpochMilli(start);
		logger.info("Start: {}", startInstant);

		CoreArgs coreArgs = new CoreArgs();
		ParamParse.parseParams(params, coreArgs, isJson);
		coreArgs.setProcessorArgs(Server.args.getProcessorArgs());
		coreArgs.getFetcherArgs().setPrivateArgs(Server.args.getFetcherPrivateArgs());

		boolean txt = (isJson ? false : Server.args.getTxt());
		boolean html = (isJson ? false : true);
		boolean json = (isJson ? true : Server.args.getJson());
		Boolean valueBoolean;
		if ((valueBoolean = ParamParse.getParamBoolean(params, ServerArgs.TXT)) != null) {
			txt = valueBoolean;
		}
		if (isJson) {
			if ((valueBoolean = ParamParse.getParamBoolean(params, Server.HTML_ID)) != null) {
				html = valueBoolean;
			}
		} else {
			if ((valueBoolean = ParamParse.getParamBoolean(params, ServerArgs.JSON)) != null) {
				json = valueBoolean;
			}
		}

		ServerInput serverInput;
		if (isJson) {
			serverInput = new ServerInput(
				ParamParse.getParamStrings(params, Query.NAME),
				ParamParse.getParamStrings(params, Query.KEYWORDS),
				ParamParse.getParamStrings(params, Query.DESCRIPTION),
				ParamParse.getParamStrings(params, Query.WEBPAGE_URLS),
				ParamParse.getParamStrings(params, Query.DOC_URLS),
				ParamParse.getParamStrings(params, Query.PUBLICATION_IDS),
				ParamParse.getParamStrings(params, Query.ANNOTATIONS));
		} else {
			serverInput = new ServerInput(
				ParamParse.getParamString(params, Query.NAME),
				ParamParse.getParamString(params, Query.KEYWORDS),
				ParamParse.getParamString(params, Query.DESCRIPTION),
				ParamParse.getParamString(params, Query.WEBPAGE_URLS),
				ParamParse.getParamString(params, Query.DOC_URLS),
				ParamParse.getParamString(params, Query.PUBLICATION_IDS),
				ParamParse.getParamString(params, Query.ANNOTATIONS));
		}

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

		String uuidDirPrefix = Server.args.getFiles() + "/";
		String uuidButLast;
		String uuid;
		do {
			uuidButLast = Server.version.getVersion() + "/" + DateTimeFormatter.ofPattern("uuuu-MM").format(LocalDateTime.ofInstant(startInstant, ZoneId.of("Z")));
			uuid = uuidButLast + "/" + UUID.randomUUID().toString();
			if (isJson) {
				uuid += "-json";
			}
		} while (Files.exists(Paths.get(uuidDirPrefix + uuid)));
		Files.createDirectories(Paths.get(uuidDirPrefix + uuidButLast));
		Files.createDirectory(Paths.get(uuidDirPrefix + uuid));
		serverInput.setId(uuid);
		logger.info("UUID: {}", uuid);

		String txtOutput = (txt ? uuid + "/results.txt" : null);
		String htmlOutput = (html ? uuid + "/" : null);
		String jsonOutput = (json ? uuid + "/results.json" : null);
		Output output = new Output(
			txtOutput != null ? uuidDirPrefix + txtOutput : null,
			htmlOutput != null ? uuidDirPrefix + htmlOutput : null,
			jsonOutput != null ? uuidDirPrefix + jsonOutput : null, true);

		PreProcessor preProcessor = new PreProcessor(coreArgs.getPreProcessorArgs(), Server.stopwordsAll.get(coreArgs.getPreProcessorArgs().getStopwords()));

		logger.info("Processing {} concepts", Server.concepts.size());
		Map<EdamUri, ConceptProcessed> processedConcepts = Server.processor.getProcessedConcepts(Server.concepts,
			coreArgs.getMapperArgs().getIdfArgs(), coreArgs.getMapperArgs().getMultiplierArgs(), preProcessor);

		logger.info("Loading query");

		Query query = QueryLoader.fromServer(serverInput, Server.concepts, MAX_KEYWORDS_SIZE, MAX_LINKS_SIZE, MAX_PUBLICATION_IDS_SIZE);

		Idf idf;
		if (coreArgs.getPreProcessorArgs().isStemming()) {
			idf = Server.idfStemmed;
		} else {
			idf = Server.idf;
		}

		QueryProcessed processedQuery = Server.processor.getProcessedQuery(query, QueryType.server, preProcessor, idf, coreArgs.getFetcherArgs());

		logger.info("Mapping query");
		Mapping mapping = new Mapper(processedConcepts).map(query, processedQuery, coreArgs.getMapperArgs());

		List<Query> queries = Collections.singletonList(query);
		List<List<Webpage>> webpages = Collections.singletonList(processedQuery.getWebpages());
		List<List<Webpage>> docs = Collections.singletonList(processedQuery.getDocs());
		List<List<Publication>> publications = Collections.singletonList(processedQuery.getPublications());
		List<Mapping> mappings = Collections.singletonList(mapping);
		Results results = Benchmark.calculate(queries, mappings);

		long stop = System.currentTimeMillis();
		logger.info("Stop: {}", Instant.ofEpochMilli(stop));
		logger.info("Mapping took {}s", (stop - start) / 1000.0);

		URI baseLocation = new URI(Server.args.getHttpsProxy() ? "https" : request.getScheme(), null, request.getServerName(), Server.args.getHttpsProxy() ? 443 : request.getServerPort(), null, null, null);
		URI apiLocation = new URI(baseLocation.getScheme(), null, baseLocation.getHost(), baseLocation.getPort(), "/" + Server.args.getPath() + "/api", null, null);
		URI txtLocation = null;
		if (txtOutput != null) {
			txtLocation = new URI(baseLocation.getScheme(), null, baseLocation.getHost(), baseLocation.getPort(), "/" + Server.args.getPath() + "/" + txtOutput, null, null);
		}
		URI htmlLocation = null;
		if (htmlOutput != null) {
			htmlLocation = new URI(baseLocation.getScheme(), null, baseLocation.getHost(), baseLocation.getPort(), "/" + Server.args.getPath() + "/" + htmlOutput, null, null);
		}
		URI jsonLocation = null;
		if (jsonOutput != null) {
			jsonLocation = new URI(baseLocation.getScheme(), null, baseLocation.getHost(), baseLocation.getPort(), "/" + Server.args.getPath() + "/" + jsonOutput, null, null);
		}

		Map<String, String> jsonFields = new LinkedHashMap<>();
		jsonFields.put("api", apiLocation.toString());
		jsonFields.put("txt", txtLocation != null ? txtLocation.toString() : null);
		jsonFields.put("html", htmlLocation != null ? htmlLocation.toString() : null);
		jsonFields.put("json", jsonLocation != null ? jsonLocation.toString() : null);

		logger.info("Outputting results");
		output.output(coreArgs, Server.getParamsMain(false, txt, html, json), jsonFields, QueryType.server, 1, 1,
			Server.concepts, queries, webpages, docs, publications, results, start, stop, Server.version);

		if (isJson) {
			logger.info("POSTED JSON {}", jsonLocation);
		} else {
			logger.info("POSTED {}", htmlLocation);
		}

		String jsonString = null;
		if (isJson) {
			JsonType jsonType = JsonType.core;
			Enum<?> valueEnum;
			if ((valueEnum = ParamParse.getParamEnum(params, Json.TYPE_ID, JsonType.class, isJson)) != null) {
				if ((JsonType) valueEnum == JsonType.full) {
					jsonType = JsonType.full;
				}
			}
			jsonString = Json.output(coreArgs, Server.getParamsMain(false, txt, html, json), jsonFields, jsonType, null,
				Server.concepts, queries, publications, webpages, docs, results, start, stop, Server.version);
		}

		return new PostResult(jsonString, htmlLocation);
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response post(MultivaluedMap<String, String> params, @Context Request request) throws IOException, URISyntaxException {
		try {
			return Response.seeOther(runPost(params, request, false).htmlLocation).build();
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}

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
	public String json(JsonObject json, @Context Request request) throws IOException, URISyntaxException {
		try {
			logger.info("POST JSON {} from {}", json, request.getRemoteAddr());

			PostResult postResult = runPost(parseJson(json), request, true);

			return postResult.jsonString;
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}

	private Response patch(MultivaluedMap<String, String> params, String requestString, Request request, String resource, Class<?> clazz, boolean doc, int max, String jsonKey) throws IOException {
		boolean isJson = (jsonKey != null);
		logger.info("PATCH {} {} from {}", resource, requestString, request.getRemoteAddr());
		FetcherArgs fetcherArgs = new FetcherArgs();
		if (isJson) {
			ParamParse.parseFetcherParams(params, fetcherArgs, true);
		} else {
			// TODO get param values from HTML form
		}
		fetcherArgs.setPrivateArgs(Server.args.getFetcherPrivateArgs());
		List<? extends DatabaseEntry<?>> databaseEntries = Server.processor.getDatabaseEntries(QueryLoader.fromServerEntry(requestString, clazz, max), fetcherArgs, clazz, doc);
		String responseText;
		if (isJson) {
			responseText = Json.fromDatabaseEntries(jsonKey, databaseEntries, fetcherArgs);
		} else {
			responseText = databaseEntries.stream()
				.map(p -> p.toStringId() + " : " + p.getStatusString(fetcherArgs).toUpperCase(Locale.ROOT))
				.collect(Collectors.joining("\n"));
		}
		Response response = Response.ok(responseText).type(isJson ? MediaType.APPLICATION_JSON : MediaType.TEXT_PLAIN + ";charset=utf-8").build();
		logger.info("PATCHED {} {}", resource, response.getEntity());
		return response;
	}

	@Path("web")
	@POST
	@Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
	public Response patchWeb(String requestString, @Context Request request) throws IOException {
		try {
			return patch(null, requestString, request, "/web", Webpage.class, false, MAX_LINKS_SIZE, null);
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}

	@Path("doc")
	@POST
	@Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
	public Response patchDoc(String requestString, @Context Request request) throws IOException {
		try {
			return patch(null, requestString, request, "/doc", Webpage.class, true, MAX_LINKS_SIZE, null);
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}

	@Path("pub")
	@POST
	@Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
	public Response patchPub(String requestString, @Context Request request) throws IOException {
		try {
			return patch(null, requestString, request, "/pub", Publication.class, false, MAX_PUBLICATION_IDS_SIZE, null);
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}

	@Path("edam")
	@POST
	@Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
	public Response checkEdam(String requestString, @Context Request request) {
		try {
			logger.info("POST /edam {} from {}", requestString, request.getRemoteAddr());
			Response response = Response.ok(QueryLoader.fromServerEdam(requestString, Server.concepts).entrySet().stream()
				.map(c -> c.getKey() + " : " + c.getValue().getLabel())
				.collect(Collectors.joining("\n"))).type(MediaType.TEXT_PLAIN + ";charset=utf-8").build();
			logger.info("POSTED /edam {}", response.getEntity());
			return response;
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}

	private Response patchJson(JsonObject json, String key, Request request, String resource, Class<?> clazz, boolean doc, int max) throws IOException {
		logger.info("PATCH JSON {} {} from {}", resource, json, request.getRemoteAddr());
		MultivaluedHashMap<String, String> jsonParsed = parseJson(json);
		List<String> databaseEntries = jsonParsed.get(key);
		String patchString = "";
		if (databaseEntries != null) {
			patchString = String.join("\n", databaseEntries);
		}
		return patch(jsonParsed, patchString, request, resource, clazz, doc, max, key);
	}

	@Path("web")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response patchWeb(JsonObject json, @Context Request request) throws IOException {
		try {
			return patchJson(json, Query.WEBPAGE_URLS, request, "/web", Webpage.class, false, MAX_LINKS_SIZE);
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
			return patchJson(json, Query.DOC_URLS, request, "/doc", Webpage.class, true, MAX_LINKS_SIZE);
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
			return patchJson(json, Query.PUBLICATION_IDS, request, "/pub", Publication.class, false, MAX_PUBLICATION_IDS_SIZE);
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}
}