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
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.util.Header;

import org.edamontology.pubfetcher.core.common.FetcherPrivateArgs;
import org.edamontology.pubfetcher.core.db.publication.Publication;
import org.edamontology.pubfetcher.core.db.webpage.Webpage;

import org.edamontology.edammap.core.args.CoreArgs;
import org.edamontology.edammap.core.benchmarking.Results;
import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.input.ServerInput;
import org.edamontology.edammap.core.input.json.Tool;
import org.edamontology.edammap.core.output.Json;
import org.edamontology.edammap.core.output.JsonType;
import org.edamontology.edammap.core.output.Output;
import org.edamontology.edammap.core.preprocessing.PreProcessor;
import org.edamontology.edammap.core.processing.ConceptProcessed;
import org.edamontology.edammap.core.processing.Processor;
import org.edamontology.edammap.core.processing.QueryProcessed;
import org.edamontology.edammap.core.query.Query;
import org.edamontology.edammap.core.query.QueryType;

@Path("/")
public class Resource extends ResourceBase {

	private static final Logger logger = LogManager.getLogger();

	static String runGet(MultivaluedMap<String, String> params, Request request) {
		try {
			logger.info("GET {} from {}", params, request.getRemoteAddr());
			CoreArgs args = newCoreArgs(params, false, Server.args.getProcessorArgs(), Server.args.getFetcherPrivateArgs());
			boolean txt = Server.args.isTxt();
			boolean json = Server.args.isJson();
			Boolean valueBoolean;
			if ((valueBoolean = ParamParse.getParamBoolean(params, ServerArgs.txtId)) != null) {
				txt = valueBoolean;
			}
			if ((valueBoolean = ParamParse.getParamBoolean(params, ServerArgs.jsonId)) != null) {
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

	@Override
	protected PostResult runPost(MultivaluedMap<String, String> params, Tool tool, Request request, boolean isJson) throws IOException, URISyntaxException {
		logger.info("POST {} from {}", params, request.getRemoteAddr());

		long start = System.currentTimeMillis();
		Instant startInstant = Instant.ofEpochMilli(start);
		logger.info("Start: {}", startInstant);

		String jsonVersion = jsonVersion(params, isJson);

		CoreArgs coreArgs = newCoreArgs(params, isJson, Server.args.getProcessorArgs(), Server.args.getFetcherPrivateArgs());

		boolean txt = (isJson ? false : Server.args.isTxt());
		boolean html = (isJson ? false : true);
		boolean json = (isJson ? true : Server.args.isJson());
		Boolean valueBoolean;
		if ((valueBoolean = ParamParse.getParamBoolean(params, ServerArgs.txtId)) != null) {
			txt = valueBoolean;
		}
		if (isJson) {
			if ((valueBoolean = ParamParse.getParamBoolean(params, ServerArgs.htmlId)) != null) {
				html = valueBoolean;
			}
		} else {
			if ((valueBoolean = ParamParse.getParamBoolean(params, ServerArgs.jsonId)) != null) {
				json = valueBoolean;
			}
		}

		ServerInput serverInput = getServerInput(params, isJson, tool, true, false);

		checkInput(serverInput, tool);

		String uuidDirPrefix = Server.args.getServerPrivateArgs().getFiles() + "/";
		String uuid = getUuid(isJson ? "-json" : "", uuidDirPrefix, Server.version, startInstant);
		boolean toolMissingId = false;
		if (serverInput != null) {
			serverInput.setId(uuid);
		} else if (tool.getBiotoolsID() == null || tool.getBiotoolsID().isEmpty()) {
			toolMissingId = true;
			tool.setBiotoolsID(uuid);
		}

		String txtOutput = (txt ? uuid + "/results.txt" : null);
		String htmlOutput = (html ? uuid + "/" : null);
		String jsonOutput = (json ? uuid + "/results.json" : null);
		Output output = new Output(
			txtOutput != null ? uuidDirPrefix + txtOutput : null,
			htmlOutput != null ? uuidDirPrefix + htmlOutput : null,
			jsonOutput != null ? uuidDirPrefix + jsonOutput : null, null, QueryType.server, true);

		PreProcessor preProcessor = new PreProcessor(coreArgs.getPreProcessorArgs(), Server.stopwordsAll.get(coreArgs.getPreProcessorArgs().getStopwords()));

		Map<EdamUri, ConceptProcessed> processedConcepts = getProcessedConcepts(coreArgs, preProcessor);

		Query query = getQuery(serverInput, tool, toolMissingId, false, false);

		QueryProcessed processedQuery = getProcessedQuery(coreArgs, Server.idf, Server.idfStemmed, query, preProcessor);

		List<Query> queries = Collections.singletonList(query);
		List<List<Webpage>> webpages = Collections.singletonList(processedQuery.getWebpages());
		List<List<Webpage>> docs = Collections.singletonList(processedQuery.getDocs());
		List<List<Publication>> publications = Collections.singletonList(processedQuery.getPublications());

		Results results = getResults(processedConcepts, query, queries, processedQuery, coreArgs, Server.edamBlacklist);

		URI baseLocation = new URI(Server.args.getServerPrivateArgs().isHttpsProxy() ? "https" : request.getScheme(), null, request.getServerName(), Server.args.getServerPrivateArgs().isHttpsProxy() ? 443 : request.getServerPort(), null, null, null);
		URI apiLocation = new URI(baseLocation.getScheme(), null, baseLocation.getHost(), baseLocation.getPort(), Server.args.getServerPrivateArgs().getPath() + "/api", null, null);
		URI txtLocation = null;
		if (txtOutput != null) {
			txtLocation = new URI(baseLocation.getScheme(), null, baseLocation.getHost(), baseLocation.getPort(), Server.args.getServerPrivateArgs().getPath() + "/" + txtOutput, null, null);
		}
		URI htmlLocation = null;
		if (htmlOutput != null) {
			htmlLocation = new URI(baseLocation.getScheme(), null, baseLocation.getHost(), baseLocation.getPort(), Server.args.getServerPrivateArgs().getPath() + "/" + htmlOutput, null, null);
		}
		URI jsonLocation = null;
		if (jsonOutput != null) {
			jsonLocation = new URI(baseLocation.getScheme(), null, baseLocation.getHost(), baseLocation.getPort(), Server.args.getServerPrivateArgs().getPath() + "/" + jsonOutput, null, null);
		}

		Map<String, String> jsonFields = new LinkedHashMap<>();
		jsonFields.put("api", apiLocation.toString());
		jsonFields.put("txt", txtLocation != null ? txtLocation.toString() : null);
		jsonFields.put("html", htmlLocation != null ? htmlLocation.toString() : null);
		jsonFields.put("json", jsonLocation != null ? jsonLocation.toString() : null);

		long stop = System.currentTimeMillis();
		logger.info("Stop: {}", Instant.ofEpochMilli(stop));
		logger.info("Total time is {}s", (stop - start) / 1000.0);

		logger.info("Outputting results");

		output.output(coreArgs, Server.getArgsMain(false, txt, html, json), null, jsonFields, 1, 1,
			Server.concepts, queries, webpages, docs, publications, results, tool, start, stop, Server.version, jsonVersion, false);

		String jsonString = null;
		if (isJson) {
			JsonType jsonType = JsonType.core;
			Enum<?> valueEnum;
			if ((valueEnum = ParamParse.getParamEnum(params, Json.TYPE_ID, JsonType.class, isJson)) != null) {
				if ((JsonType) valueEnum == JsonType.full) {
					jsonType = JsonType.full;
				}
			}
			jsonString = Json.output(coreArgs, Server.getArgsMain(false, txt, html, json), jsonFields, QueryType.server, jsonType, null,
				Server.concepts, queries, publications, webpages, docs, results, tool, start, stop, Server.version, jsonVersion, false);
		}

		if (isJson) {
			logger.info("POSTED JSON {}", jsonLocation);
		} else {
			logger.info("POSTED {}", htmlLocation);
		}

		return new PostResult(jsonString, htmlLocation);
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response post(MultivaluedMap<String, String> params, @Context Request request) throws IOException, URISyntaxException {
		try {
			return Response.seeOther(runPost(params, null, request, false).htmlLocation).build();
		} catch (Throwable e) {
			logger.error("Exception!", e);
			throw e;
		}
	}

	@Override
	protected FetcherPrivateArgs getFetcherPrivateArgs() {
		return Server.args.getFetcherPrivateArgs();
	}

	@Override
	protected ServerPrivateArgsBase getServerPrivateArgs() {
		return Server.args.getServerPrivateArgs();
	}

	@Override
	protected Map<EdamUri, Concept> getConcepts() {
		return Server.concepts;
	}

	@Override
	protected Processor getProcessor() {
		return Server.processor;
	}
}
