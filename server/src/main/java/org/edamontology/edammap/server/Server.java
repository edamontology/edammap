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
import org.edamontology.edammap.core.output.Param;
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
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

public final class Server {

	private static Logger logger;

	static Version version;

	static ServerArgs args;

	static List<Param> paramsMain = new ArrayList<>();

	static EnumMap<Stopwords, List<String>> stopwordsAll = new EnumMap<>(Stopwords.class);

	static Processor processor;

	static Idf idf = null;
	static Idf idfStemmed = null;

	static Map<EdamUri, Concept> concepts;

	public static void copyHtmlResources(Path path) throws IOException {
		Files.copy(Server.class.getResourceAsStream("/style.css"), path.resolve("style.css"));
		Files.copy(Server.class.getResourceAsStream("/script.js"), path.resolve("script.js"));
	}

	private static void run() throws IOException, ParseException {
		paramsMain.add(new Param("Ontology file", ServerArgs.EDAM, new File(args.getEdam()).getName(), "https://github.com/edamontology/edamontology/tree/master/releases"));
		//paramsMain.add(new Param("Number of threads", ServerArgs.THREADS, args.getThreads(), 0.0, null)); // TODO

		// TODO check files + Server.version.getVersion() is existing writable directory
		//FetcherCommon.outputPath("", true);

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
		// TODO .property(JsonGenerator.PRETTY_PRINTING, true);

		HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(args.getBaseUri() + "/" + args.getPath() + "/api"), rc, false);

		final StaticHttpHandler filesHttpHandler = new StaticHttpHandler(args.getFiles());
		filesHttpHandler.setDirectorySlashOff(true);
		httpServer.getServerConfiguration().addHttpHandler(filesHttpHandler, "/" + args.getPath() + "/*");

		httpServer.getServerConfiguration().addHttpHandler(
			new HttpHandler() {
				@Override
				public void service(Request request, Response response) throws Exception {
					String responseText = Resource.runGet(null, request); // TODO replace null with request.getParameterMap()
					response.setContentType(MediaType.TEXT_HTML);
					response.setContentLength(responseText.length());
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
			//builder.format(ApacheLogFormat.COMBINED); // TODO
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
