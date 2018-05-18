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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.Edam;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.idf.Idf;
import org.edamontology.edammap.core.output.ParamMain;
import org.edamontology.edammap.core.preprocessing.PreProcessor;
import org.edamontology.edammap.core.preprocessing.Stopwords;
import org.edamontology.edammap.core.processing.Processor;
import org.edamontology.pubfetcher.BasicArgs;
import org.edamontology.pubfetcher.Version;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.http.server.accesslog.AccessLogBuilder;
import org.glassfish.grizzly.http.util.ContentType;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;

public final class Server {

	private static Logger logger;

	static Version version;

	static ServerArgs args;

	static EnumMap<Stopwords, List<String>> stopwordsAll = new EnumMap<>(Stopwords.class);

	static Processor processor;

	static Idf idf = null;
	static Idf idfStemmed = null;

	static Map<EdamUri, Concept> concepts;

	static final String HTML_ID = "html";

	static List<ParamMain> getParamsMain(boolean input, boolean txt, boolean html, boolean json) {
		List<ParamMain> paramsMain = new ArrayList<>();
		paramsMain.add(new ParamMain("Ontology file", ServerArgs.EDAM, new File(args.getEdam()).getName(), "https://github.com/edamontology/edamontology/tree/master/releases", false));
		paramsMain.add(new ParamMain("Results to text", ServerArgs.TXT, txt, input));
		paramsMain.add(new ParamMain("Results to HTML", HTML_ID, html, false));
		paramsMain.add(new ParamMain("Results to JSON", ServerArgs.JSON, json, input));
		return paramsMain;
	}

	public static void copyHtmlResources(Path path) throws IOException {
		Files.copy(Server.class.getResourceAsStream("/style.css"), path.resolve("style.css"));
		Files.copy(Server.class.getResourceAsStream("/script.js"), path.resolve("script.js"));
	}

	private static void run() throws IOException, ParseException {
		Path filesDir = Paths.get(args.getFiles() + "/" + version.getVersion());
		if (!Files.isDirectory(filesDir) || !Files.isWritable(filesDir)) {
			throw new AccessDeniedException(filesDir.toAbsolutePath().normalize() + " is not a writeable directory!");
		}

		for (Stopwords stopwords : Stopwords.values()) {
			stopwordsAll.put(stopwords, PreProcessor.getStopwords(stopwords));
		}

		processor = new Processor(args.getProcessorArgs());

		if (args.getProcessorArgs().getIdf() != null && !args.getProcessorArgs().getIdf().isEmpty()) {
			idf = new Idf(args.getProcessorArgs().getIdf());
		}
		if (args.getProcessorArgs().getIdfStemmed() != null && !args.getProcessorArgs().getIdfStemmed().isEmpty()) {
			idfStemmed = new Idf(args.getProcessorArgs().getIdfStemmed());
		}

		logger.info("Loading concepts");
		concepts = Edam.load(args.getEdam());

		logger.info("Configuring server");

		final ResourceConfig rc = new ResourceConfig().packages("org.edamontology.edammap.server");

		HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(args.getBaseUri() + "/" + args.getPath() + "/api"), rc, false);

		final StaticHttpHandler filesHttpHandler = new StaticHttpHandler(args.getFiles()) {
			@Override
			protected boolean handle(String uri, Request request, Response response) throws Exception {
				response.getResponse().setCharacterEncoding("utf-8");
				String path = request.getPathInfo();
				int extStart = path.lastIndexOf(".");
				if (extStart > -1 && path.substring(extStart).equals(".json")) {
					response.getResponse().setContentType(MediaType.APPLICATION_JSON);
				}
				return super.handle(uri, request, response);
			}
		};
		filesHttpHandler.setDirectorySlashOff(true);
		httpServer.getServerConfiguration().addHttpHandler(filesHttpHandler, "/" + args.getPath() + "/*");

		httpServer.getServerConfiguration().addHttpHandler(
			new HttpHandler() {
				@Override
				public void service(Request request, Response response) throws Exception {
					String responseText = null;
					String mediaType = MediaType.TEXT_HTML;
					try {
						responseText = Resource.runGet(request.getParameterMap().entrySet().stream()
							.collect(Collectors.toMap(Map.Entry::getKey, e -> Arrays.asList(e.getValue()), (k, v) -> { throw new AssertionError(); }, MultivaluedHashMap<String, String>::new)), request);
					} catch (ParamException e) {
						if (e.getResponse().getEntity() instanceof String) {
							responseText = (String) e.getResponse().getEntity();
						} else {
							responseText = "400 Bad Request\n" + ExceptionCommon.time();
						}
						mediaType = MediaType.TEXT_PLAIN;
					} catch (Throwable e) {
						responseText = "500 Internal Server Error\n" + ExceptionCommon.time();
						mediaType = MediaType.TEXT_PLAIN;
					}
					response.setContentType(ContentType.newContentType(mediaType, "utf-8"));
					response.setContentLength(responseText.getBytes().length);
					response.getWriter().write(responseText);
				}
			},
			"/" + args.getPath() + "/");

		if (args.getLog() != null) {
			Path accessDir = Paths.get(args.getLog() + "/access");
			if (!Files.exists(accessDir)) {
				Files.createDirectory(accessDir);
			}
			final AccessLogBuilder builder = new AccessLogBuilder(accessDir + "/edammap-access.log");
			builder.rotatedDaily();
			//builder.format(ApacheLogFormat.COMBINED); // TODO change from default ApacheLogFormat.COMBINED?
			builder.instrument(httpServer.getServerConfiguration());
		}

		logger.info("Starting server");
		httpServer.start();
		logger.info("{} has started", version.getName());
	}

	public static void main(String[] argv) throws IOException, ReflectiveOperationException {
		version = new Version(Server.class);

		args = BasicArgs.parseArgs(argv, ServerArgs.class, version);

		// logger must be called only after configuration changes have been made in BasicArgs.parseArgs()
		// otherwise invalid.log will be created if arg --log is null
		logger = LogManager.getLogger();
		logger.debug(String.join(" ", argv));
		logger.info("This is {} {}", version.getName(), version.getVersion());

		try {
			run();
		} catch (Throwable e) {
			logger.error("Exception!", e);
		}
	}
}
