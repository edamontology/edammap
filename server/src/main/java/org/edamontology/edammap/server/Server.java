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
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.http.server.accesslog.AccessLogBuilder;
import org.glassfish.grizzly.http.server.accesslog.ApacheLogFormat;
import org.glassfish.grizzly.http.util.ContentType;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import org.edamontology.edammap.core.args.ArgMain;
import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.Edam;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.idf.Idf;
import org.edamontology.edammap.core.preprocessing.PreProcessor;
import org.edamontology.edammap.core.preprocessing.Stopwords;
import org.edamontology.edammap.core.processing.Processor;

import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.BasicArgs;
import org.edamontology.pubfetcher.core.common.Version;

public final class Server {

	private static Logger logger;

	static Version version;

	static ServerArgs args;

	static EnumMap<Stopwords, List<String>> stopwordsAll = new EnumMap<>(Stopwords.class);

	static Set<EdamUri> edamBlacklist;

	static Processor processor = null;

	static Idf idf = null;
	static Idf idfStemmed = null;

	static Map<EdamUri, Concept> concepts;

	static final String VERSION_ID = "version";

	static List<ArgMain> getArgsMain(boolean input, Boolean txt, Boolean html, Boolean json) {
		List<ArgMain> argsMain = new ArrayList<>();
		for (Arg<?, ?> arg : args.getArgs()) {
			switch (arg.getId()) {
				case ServerArgs.txtId: argsMain.add(new ArgMain(txt, arg, input)); break;
				case ServerArgs.htmlId: argsMain.add(new ArgMain(html, arg, false)); break;
				case ServerArgs.jsonId: argsMain.add(new ArgMain(json, arg, input)); break;
				default: argsMain.add(new ArgMain(arg.getValue(), arg, false)); break;
			}
		}
		return argsMain;
	}

	public static void copyHtmlResources(Path path) throws IOException {
		Files.copy(Server.class.getResourceAsStream("/style.css"), path.resolve("style.css"));
		Files.copy(Server.class.getResourceAsStream("/script.js"), path.resolve("script.js"));
	}

	private static void run() throws IOException, ParseException {
		Path filesDir = Paths.get(args.getServerPrivateArgs().getFiles() + "/" + version.getVersion());
		if (!Files.isDirectory(filesDir) || !Files.isWritable(filesDir)) {
			throw new AccessDeniedException(filesDir.toAbsolutePath().normalize() + " is not a writeable directory!");
		}

		for (Stopwords stopwords : Stopwords.values()) {
			stopwordsAll.put(stopwords, PreProcessor.getStopwords(stopwords));
		}

		edamBlacklist = Edam.getBlacklist();

		processor = new Processor(args.getProcessorArgs(), args.getFetcherPrivateArgs());

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

		HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(args.getServerPrivateArgs().getBaseUri() + "/" + args.getServerPrivateArgs().getPath() + "/api"), rc, false);

		final StaticHttpHandler filesHttpHandler = new StaticHttpHandler(args.getServerPrivateArgs().getFiles()) {
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
		httpServer.getServerConfiguration().addHttpHandler(filesHttpHandler, "/" + args.getServerPrivateArgs().getPath() + "/*");

		httpServer.getServerConfiguration().addHttpHandler(
			new HttpHandler() {
				@Override
				public void service(Request request, Response response) throws Exception {
					String responseText = null;
					try {
						responseText = Resource.runGet(request.getParameterMap().entrySet().stream()
							.collect(Collectors.toMap(Map.Entry::getKey, e -> Arrays.asList(e.getValue()), (k, v) -> { throw new AssertionError(); }, MultivaluedHashMap<String, String>::new)), request);
					} catch (ParamException e) {
						if (e.getResponse().getEntity() instanceof String) {
							responseText = (String) e.getResponse().getEntity();
						} else {
							responseText = "400 Bad Request\n" + ExceptionCommon.time();
						}
					} catch (Throwable e) {
						responseText = "500 Internal Server Error\n" + ExceptionCommon.time();
					}
					response.setContentType(ContentType.newContentType(MediaType.TEXT_HTML, "utf-8"));
					response.setContentLength(responseText.getBytes().length);
					response.getWriter().write(responseText);
				}
			},
			"/" + args.getServerPrivateArgs().getPath() + "/");

		if (args.getLog() != null) {
			Path accessDir = Paths.get(args.getLog() + "/access");
			if (!Files.exists(accessDir)) {
				Files.createDirectory(accessDir);
			}
			final AccessLogBuilder builder = new AccessLogBuilder(accessDir + "/edammap-access.log");
			builder.rotatedDaily();
			// Default access log format is ApacheLogFormat.COMBINED
			builder.format(ApacheLogFormat.COMBINED);
			builder.instrument(httpServer.getServerConfiguration());
		}

		logger.info("Starting server");
		httpServer.start();
		logger.info("{} has started", version.getName());

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Stopping server");
				if (processor != null) {
					try {
						processor.closeDatabase();
					} catch (IOException e) {
						logger.error("Exception!", e);
					}
				}
				httpServer.shutdown();
				logger.info("{} has stopped", version.getName());
			}
		});
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
