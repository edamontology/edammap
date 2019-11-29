/*
 * Copyright © 2016, 2018 Erik Jaaniso
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

package org.edamontology.edammap.cli;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.PubFetcher;
import org.edamontology.pubfetcher.core.common.Version;
import org.edamontology.pubfetcher.core.db.publication.Publication;
import org.edamontology.pubfetcher.core.db.webpage.Webpage;

import org.edamontology.edammap.core.args.ArgMain;
import org.edamontology.edammap.core.benchmarking.Benchmark;
import org.edamontology.edammap.core.benchmarking.Measure;
import org.edamontology.edammap.core.benchmarking.Results;
import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.Edam;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.idf.Idf;
import org.edamontology.edammap.core.mapping.Mapper;
import org.edamontology.edammap.core.mapping.Mapping;
import org.edamontology.edammap.core.output.Output;
import org.edamontology.edammap.core.preprocessing.PreProcessor;
import org.edamontology.edammap.core.processing.ConceptProcessed;
import org.edamontology.edammap.core.processing.Processor;
import org.edamontology.edammap.core.processing.QueryProcessed;
import org.edamontology.edammap.core.query.Query;
import org.edamontology.edammap.core.query.QueryLoader;

public class Cli implements Runnable {

	private static final String JSON_VERSION = "1";

	private static final Logger logger = LogManager.getLogger();

	private static Object lock = new Object();

	private static boolean lockDone = false;

	private static int numThreads = 0;

	private static int index = 0;

	private static long start;

	private static boolean stderr;

	private static CliArgs args;

	private static List<String> stopwords;

	private static Set<EdamUri> edamBlacklist;

	private static Processor processor = null;

	private static Idf idf;

	private static Map<EdamUri, ConceptProcessed> processedConcepts;

	private static List<Query> queries;

	private static List<List<Webpage>> webpages;
	private static List<List<Webpage>> docs;
	private static List<List<Publication>> publications;

	private static List<Mapping> mappings;

	@Override
	public void run() {
		synchronized (lock) {
			++numThreads;
			lockDone = true;
		}
		try {
			PreProcessor pp = new PreProcessor(args.getCoreArgs().getPreProcessorArgs(), stopwords);
			Mapper mapper = new Mapper(processedConcepts, edamBlacklist);

			while (true) {
				Query query;
				int localIndex;
				synchronized (queries) {
					if (index >= queries.size()) {
						break;
					}
					query = queries.get(index);
					localIndex = index;
					++index;
				}

				logger.info("Map {}", PubFetcher.progress(localIndex + 1, queries.size(), start));

				QueryProcessed processedQuery = processor.getProcessedQuery(query, args.getType(), pp, idf, args.getCoreArgs().getFetcherArgs(), null);

				Mapping mapping = mapper.map(query, processedQuery, args.getCoreArgs().getMapperArgs());

				if (stderr) {
					System.err.print("Map " + PubFetcher.progress(localIndex + 1, queries.size(), start) + "  \r");
				}

				synchronized (mappings) {
					webpages.set(localIndex, processedQuery.getWebpages());
					docs.set(localIndex, processedQuery.getDocs());
					publications.set(localIndex, processedQuery.getPublications());
					mappings.set(localIndex, mapping);
				}
			}
		} finally {
			synchronized (lock) {
				--numThreads;
				lock.notifyAll();
			}
		}
	}

	public static int run(CliArgs cliArgs, Version version, boolean progressToStderr, boolean trimBiotools) throws IOException, ParseException {
		args = cliArgs;

		List<ArgMain> argsMain = new ArrayList<>();
		for (Arg<?, ?> arg : args.getArgs()) {
			argsMain.add(new ArgMain(arg.getValue(), arg, false));
		}

		Output output = new Output(args.getOutput(), args.getReport(), args.getJson(), args.getBiotools(), args.getType(), false);

		stopwords = PreProcessor.getStopwords(args.getCoreArgs().getPreProcessorArgs().getStopwords());

		edamBlacklist = Edam.getBlacklist();

		processor = new Processor(args.getCoreArgs().getProcessorArgs(), args.getCoreArgs().getFetcherArgs().getPrivateArgs());

		idf = null;
		if (args.getCoreArgs().getPreProcessorArgs().isStemming()) {
			if (args.getCoreArgs().getProcessorArgs().getIdfStemmed() != null && !args.getCoreArgs().getProcessorArgs().getIdfStemmed().isEmpty()) {
				logger.info("Loading IDF from {}", args.getCoreArgs().getProcessorArgs().getIdfStemmed());
				idf = new Idf(args.getCoreArgs().getProcessorArgs().getIdfStemmed());
			}
		} else {
			if (args.getCoreArgs().getProcessorArgs().getIdf() != null && !args.getCoreArgs().getProcessorArgs().getIdf().isEmpty()) {
				logger.info("Loading IDF from {}", args.getCoreArgs().getProcessorArgs().getIdf());
				idf = new Idf(args.getCoreArgs().getProcessorArgs().getIdf());
			}
		}

		logger.info("Loading concepts from {}", args.getEdam());
		Map<EdamUri, Concept> concepts = Edam.load(args.getEdam());

		logger.info("Processing {} concepts", concepts.size());
		processedConcepts = processor.getProcessedConcepts(concepts, args.getCoreArgs().getMapperArgs().getIdfArgs(), args.getCoreArgs().getMapperArgs().getMultiplierArgs(),
			new PreProcessor(args.getCoreArgs().getPreProcessorArgs(), stopwords));

		logger.info("Loading queries from {}", args.getQuery());
		queries = QueryLoader.get(args.getQuery(), args.getType(), concepts,
			args.getCoreArgs().getFetcherArgs().getTimeout(), args.getCoreArgs().getFetcherArgs().getPrivateArgs().getUserAgent());
		logger.info("Loaded {} queries", queries.size());

		publications = new ArrayList<>(queries.size());
		webpages = new ArrayList<>(queries.size());
		docs = new ArrayList<>(queries.size());
		mappings = new ArrayList<>(queries.size());
		for (int i = 0; i < queries.size(); ++i) {
			publications.add(null);
			webpages.add(null);
			docs.add(null);
			mappings.add(null);
		}

		start = System.currentTimeMillis();
		logger.info("Start: {}", Instant.ofEpochMilli(start));

		stderr = progressToStderr;

		logger.info("Starting mapper threads");
		for (int i = 0; i < args.getThreads(); ++i) {
			Thread t = new Thread(new Cli());
			t.setDaemon(true);
			t.start();
		}

		synchronized (lock) {
			while (!lockDone || numThreads > 0) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					// TODO exit threads cleanly? give timeout for threads to exit? close db? print that exiting and waiting for threads to terminate?
					// Runtime.getRuntime().addShutdownHook(new Thread() {
					logger.error("Exception!", e);
					System.exit(1);
				}
			}
		}
		logger.info("All mapper threads stopped");

		long stop = System.currentTimeMillis();
		logger.info("Stop: {}", Instant.ofEpochMilli(stop));
		logger.info("Mapping took {}s", (stop - start) / 1000.0);

		Results results = Benchmark.calculate(queries, mappings);

		logger.info("Outputting results");
		output.output(args.getCoreArgs(), argsMain, args.getQuery(), null, args.getReportPageSize(), args.getReportPaginationSize(),
			concepts, queries, webpages, docs, publications, results, null, start, stop, version, JSON_VERSION);

		logger.info("{} : {}", results.toStringMeasure(Measure.recall), Measure.recall);
		logger.info("{} : {}", results.toStringMeasure(Measure.AveP), Measure.AveP);

		return results.getMappings().size();
	}

	public static void closeDatabase() throws IOException {
		if (processor != null) {
			processor.closeDatabase();
		}
	}
}
