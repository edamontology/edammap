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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.edamontology.edammap.core.args.MainArgs;
import org.edamontology.edammap.core.benchmarking.Benchmark;
import org.edamontology.edammap.core.benchmarking.Measure;
import org.edamontology.edammap.core.benchmarking.Results;
import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.Edam;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.mapping.Mapper;
import org.edamontology.edammap.core.mapping.Mapping;
import org.edamontology.edammap.core.output.Output;
import org.edamontology.edammap.core.preprocessing.PreProcessor;
import org.edamontology.edammap.core.processing.ConceptProcessed;
import org.edamontology.edammap.core.processing.Processor;
import org.edamontology.edammap.core.processing.QueryProcessed;
import org.edamontology.edammap.core.query.Query;
import org.edamontology.edammap.core.query.QueryLoader;
import org.edamontology.pubfetcher.BasicArgs;
import org.edamontology.pubfetcher.Publication;
import org.edamontology.pubfetcher.Version;
import org.edamontology.pubfetcher.Webpage;

public class Cli implements Runnable {

	private static Logger logger;

	private static Object lock = new Object();

	private static boolean lockDone = false;

	private static int numThreads = 0;

	private static int index = 0;

	private static long start;

	private static MainArgs args;

	private static List<String> stopwords;

	private static Processor processor;

	private static Map<EdamUri, ConceptProcessed> processedConcepts;

	private static List<Query> queries;

	private static List<List<Publication>> publications;
	private static List<List<Webpage>> webpages;
	private static List<List<Webpage>> docs;

	private static List<Mapping> mappings;

	@Override
	public void run() {
		synchronized (lock) {
			++numThreads;
			lockDone = true;
		}

		PreProcessor pp = new PreProcessor(args.getProcessorArgs().getPreProcessorArgs(), stopwords);
		Mapper mapper = new Mapper(processedConcepts);

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

			logger.info("{}/{} @ {}s", localIndex + 1, queries.size(), (System.currentTimeMillis() - start) / 1000.0);

			QueryProcessed processedQuery = processor.getProcessedQuery(query, pp, args.getType());

			Mapping mapping = mapper.map(query, processedQuery, args.getMapperArgs());

			synchronized (mappings) {
				publications.set(localIndex, processedQuery.getPublications());
				webpages.set(localIndex, processedQuery.getWebpages());
				docs.set(localIndex, processedQuery.getDocs());
				mappings.set(localIndex, mapping);
			}
		}

		synchronized (lock) {
			--numThreads;
			lock.notifyAll();
		}
	}

	private static void run(Version version) throws IOException, ParseException, ReflectiveOperationException {
		Output output = new Output(args);

		stopwords = PreProcessor.getStopwords(args.getProcessorArgs().getPreProcessorArgs());

		processor = new Processor(args.getProcessorArgs());

		logger.info("Loading concepts");
		Map<EdamUri, Concept> concepts = Edam.load(args.getEdam());

		logger.info("Processing {} concepts", concepts.size());
		processedConcepts = processor.getProcessedConcepts(concepts, args.getMapperArgs().getIdfArgs(), args.getMapperArgs().getMultiplierArgs());

		start = System.currentTimeMillis();
		logger.info("Start: {}", Instant.ofEpochMilli(start));

		logger.info("Loading queries");
		queries = QueryLoader.get(args.getQuery(), args.getType(), concepts, args.getProcessorArgs().getFetcherArgs());

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
					logger.error("Exception!", e);
					System.exit(1);
				}
			}
		}

		Results results = Benchmark.calculate(queries, mappings);

		long stop = System.currentTimeMillis();
		logger.info("Stop: {}", Instant.ofEpochMilli(stop));
		logger.info("Mapping took {}s", (stop - start) / 1000.0);

		logger.info("Outputting results");
		output.output(concepts, queries, publications, webpages, docs, results, start, stop, version);

		logger.info("{} : {}", results.toStringMeasure(Measure.recall), Measure.recall);
		logger.info("{} : {}", results.toStringMeasure(Measure.AveP), Measure.AveP);
	}

	public static void main(String[] argv) throws IOException, ReflectiveOperationException {
		Version version = new Version(Cli.class);

		args = BasicArgs.parseArgs(argv, MainArgs.class, version);

		// logger must be called only after configuration changes have been made in BasicArgs.parseArgs()
		// otherwise invalid.log will be created if arg --log is null
		logger = LogManager.getLogger();
		logger.debug(String.join(" ", argv));
		logger.info("This is {} {}", version.getName(), version.getVersion());

		try {
			run(version);
		} catch (Throwable e) {
			logger.error("Exception!", e);
		}
	}
}
