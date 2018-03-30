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
import java.text.ParseException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

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
import org.edamontology.edammap.core.output.Output;
import org.edamontology.edammap.core.preprocessing.PreProcessor;
import org.edamontology.edammap.core.processing.ConceptProcessed;
import org.edamontology.edammap.core.processing.QueryProcessed;
import org.edamontology.edammap.core.query.Query;
import org.edamontology.edammap.core.query.QueryLoader;
import org.edamontology.edammap.core.query.QueryType;
import org.edamontology.pubfetcher.FetcherArgs;
import org.edamontology.pubfetcher.Publication;
import org.edamontology.pubfetcher.Webpage;
import org.glassfish.grizzly.http.server.Request;

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

	private String runGet(MultivaluedMap<String, String> params, Request request) {
		try {
			logger.info("GET {} from {}", params, request.getRemoteAddr());
			CoreArgs args = new CoreArgs();
			ParamParse.parseParams(params, args);
			args.setProcessorArgs(Server.args.getProcessorArgs());
			args.getFetcherArgs().setPrivateArgs(Server.args.getFetcherPrivateArgs());
			return Page.get(args);
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String get(@Context UriInfo ui, @Context Request request) {
		return runGet(ui.getQueryParameters(), request);
	}

	private Response runPost(MultivaluedMap<String, String> params, Request request) throws IOException, ParseException, URISyntaxException {
		logger.info("POST {} from {}", params, request.getRemoteAddr());

		long start = System.currentTimeMillis();
		logger.info("Start: {}", Instant.ofEpochMilli(start));

		CoreArgs coreArgs = new CoreArgs();
		ParamParse.parseParams(params, coreArgs);
		coreArgs.setProcessorArgs(Server.args.getProcessorArgs());
		coreArgs.getFetcherArgs().setPrivateArgs(Server.args.getFetcherPrivateArgs());

		ServerInput serverInput = new ServerInput(
				ParamParse.getParamString(params, "name"),
				ParamParse.getParamString(params, "keywords"),
				ParamParse.getParamString(params, "description"),
				ParamParse.getParamString(params, "webpage-urls"),
				ParamParse.getParamString(params, "doc-urls"),
				ParamParse.getParamString(params, "publication-ids"),
				ParamParse.getParamString(params, "annotations"));
		if (serverInput.getName() != null && serverInput.getName().length() > MAX_NAME_LENGTH) {
			throw new IllegalArgumentException("Name length (" + serverInput.getName().length() + ") is greater than maximum allowed (" + MAX_NAME_LENGTH + ")");
		}
		if (serverInput.getKeywords() != null && serverInput.getKeywords().length() > MAX_KEYWORDS_LENGTH) {
			throw new IllegalArgumentException("Keywords length (" + serverInput.getKeywords().length() + ") is greater than maximum allowed (" + MAX_KEYWORDS_LENGTH + ")");
		}
		if (serverInput.getDescription() != null && serverInput.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
			throw new IllegalArgumentException("Description length (" + serverInput.getDescription().length() + ") is greater than maximum allowed (" + MAX_DESCRIPTION_LENGTH + ")");
		}
		if (serverInput.getWebpageUrls() != null && serverInput.getWebpageUrls().length() > MAX_LINKS_LENGTH) {
			throw new IllegalArgumentException("Webpage URLs length (" + serverInput.getWebpageUrls().length() + ") is greater than maximum allowed (" + MAX_LINKS_LENGTH + ")");
		}
		if (serverInput.getDocUrls() != null && serverInput.getDocUrls().length() > MAX_LINKS_LENGTH) {
			throw new IllegalArgumentException("Doc URLs length (" + serverInput.getDocUrls().length() + ") is greater than maximum allowed (" + MAX_LINKS_LENGTH + ")");
		}
		if (serverInput.getPublicationIds() != null && serverInput.getPublicationIds().length() > MAX_PUBLICATION_IDS_LENGTH) {
			throw new IllegalArgumentException("Publication IDs length (" + serverInput.getPublicationIds().length() + ") is greater than maximum allowed (" + MAX_PUBLICATION_IDS_LENGTH + ")");
		}
		if (serverInput.getAnnotations() != null && serverInput.getAnnotations().length() > MAX_ANNOTATIONS_LENGTH) {
			throw new IllegalArgumentException("Annotations length (" + serverInput.getAnnotations().length() + ") is greater than maximum allowed (" + MAX_ANNOTATIONS_LENGTH + ")");
		}

		String uuid;
		String uuidDir;
		do {
			uuid = Server.version.getVersion() + "/" + UUID.randomUUID().toString();
			uuidDir = Server.args.getFiles() + "/" + uuid;
		} while (Files.exists(Paths.get(uuidDir)));
		Files.createDirectory(Paths.get(uuidDir));
		serverInput.setId(uuid);
		logger.info("UUID: {}", uuid);

		Output output = new Output(uuidDir + "/results.txt", uuidDir, true);
		// TODO params to choose if HTML or TXT output desired

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

		logger.info("Outputting results");
		output.output(coreArgs, Server.paramsMain, QueryType.server, 1, 1,
			Server.concepts, queries, webpages, docs, publications, results, start, stop, Server.version);

		URI location = new URI("../files/" + uuid + "/");
		logger.info("POSTED {}", location);

		return Response.seeOther(location).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response post(MultivaluedMap<String, String> params, @Context Request request) throws IOException, ParseException, URISyntaxException {
		try {
			return runPost(params, request);
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}

/* TODO JSON
	// curl -H "Content-Type: application/json" -X POST -d '{"threads":2,"reportPaginationSize":"7","mapperArgs":{"algorithmArgs":{"compoundWords":2}}}' http://localhost:8080/api
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String json(JsonObject json) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, JsonValue> entry : json.entrySet()) {
			if (entry.getValue().getValueType() == JsonValue.ValueType.STRING || entry.getValue().getValueType() == JsonValue.ValueType.NUMBER) {
				sb.append(entry.getKey()).append(" --> ").append(entry.getValue().toString()).append("\n");
			}
		}
		return sb.toString();
	}
*/

	private Response patch(String requestString, Request request, String resource, Class<?> clazz, boolean doc, int max) {
		try {
			logger.info("PATCH {} {} from {}", resource, requestString, request.getRemoteAddr());
			FetcherArgs fetcherArgs = new FetcherArgs(); // TODO get actual args from form
			fetcherArgs.setPrivateArgs(Server.args.getFetcherPrivateArgs());
			Response response = Response.ok(
				Server.processor.getDatabaseEntries(QueryLoader.fromServerEntry(requestString, clazz, max), fetcherArgs, clazz, doc).stream()
				.map(p -> p.toStringId() + " : " + p.getStatusString(fetcherArgs).toUpperCase(Locale.ROOT))
				.collect(Collectors.joining("\n"))).build();
			logger.info("PATCHED {} {}", resource, response.getEntity());
			return response;
		} catch (IllegalArgumentException e) {
			logger.error("Exception!", e);
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}

	@Path("web")
	@PATCH
	@Produces(MediaType.TEXT_PLAIN)
	public Response patchWeb(String requestString, @Context Request request) {
		return patch(requestString, request, "/web", Webpage.class, false, MAX_LINKS_SIZE);
	}

	@Path("doc")
	@PATCH
	@Produces(MediaType.TEXT_PLAIN)
	public Response patchDoc(String requestString, @Context Request request) {
		return patch(requestString, request, "/doc", Webpage.class, true, MAX_LINKS_SIZE);
	}

	@Path("pub")
	@PATCH
	@Produces(MediaType.TEXT_PLAIN)
	public Response patchPub(String requestString, @Context Request request) {
		return patch(requestString, request, "/pub", Publication.class, false, MAX_PUBLICATION_IDS_SIZE);
	}

	@Path("edam")
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public Response checkEdam(String requestString, @Context Request request) {
		try {
			logger.info("POST /edam {} from {}", requestString, request.getRemoteAddr());
			Response response = Response.ok(QueryLoader.fromServerEdam(requestString, Server.concepts).entrySet().stream()
				.map(c -> c.getKey() + " : " + c.getValue().getLabel())
				.collect(Collectors.joining("\n"))).build();
			logger.info("POSTED /edam {}", response.getEntity());
			return response;
		} catch (IllegalArgumentException e) {
			logger.error("Exception!", e);
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}
}
