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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO remove OWL dependency, as not defined in POM
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import org.edamontology.edammap.core.args.MainArgs;
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
import org.edamontology.pubfetcher.Publication;

public class Cli implements Runnable {

	private static Object lock = new Object();

	private static boolean lockDone = false;

	private static int numThreads = 0;

	private static int index = 0;

	private static Processor processor;

	private static Map<EdamUri, ConceptProcessed> processedConcepts;

	private static List<Query> queries;

	private static List<List<Publication>> publications;

	private static MainArgs args;

	private static List<Mapping> mappings;

	@Override
	public void run() {
		synchronized (lock) {
			++numThreads;
			lockDone = true;
		}

		PreProcessor pp = null;
		try {
			pp = new PreProcessor(args.getProcessorArgs().getPreProcessorArgs());
		} catch (IOException e) {
			// TODO
			System.err.println(e);
			System.exit(1);
		}
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

			// TODO temp
			System.err.println(localIndex + " " + System.currentTimeMillis());

			QueryProcessed processedQuery = processor.getProcessedQuery(query, pp, args.getType());

			Mapping mapping = mapper.map(query, processedQuery, args.getMapperArgs());

			synchronized (mappings) {
				publications.set(localIndex, processedQuery.getPublications());
				mappings.set(localIndex, mapping);
			}
		}

		synchronized (lock) {
			--numThreads;
			lock.notifyAll();
		}
	}

	private static MainArgs parseArgs(String[] argv) {
		MainArgs args = new MainArgs();
		JCommander jcommander = new JCommander(args);
		try {
			jcommander.parse(argv);
		} catch (ParameterException e) {
			jcommander.usage();
			System.err.println(e);
			System.exit(1);
		}
		if (args.isHelp()) {
			jcommander.usage();
			System.exit(0);
		}
		return args;
	}

	public static void main(String[] argv) {
		System.err.println("Start");

		args = parseArgs(argv);

		Output output = null;
		try {
			output = new Output(args);
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}

		try {
			processor = new Processor(args.getProcessorArgs());
		} catch (IOException | ParseException e) {
			System.err.println(e);
			System.exit(1);
		}

		System.err.println("Loading concepts");
		Map<EdamUri, Concept> concepts = null;
		try {
			concepts = Edam.load(args.getEdam());
		} catch (OWLOntologyCreationException e) {
			System.err.println(e);
			System.exit(1);
		}

		System.err.println("Processing concepts");
		processedConcepts = processor.getProcessedConcepts(concepts, args.getMapperArgs().getIdfMultiplierArgs());

		System.err.println("Loading queries");
		try {
			queries = QueryLoader.get(args.getQuery(), args.getType(), concepts, args.getProcessorArgs().getFetcherArgs());
		} catch (IOException | ParseException e) {
			System.err.println(e);
			System.exit(1);
		}

		publications = new ArrayList<>(queries.size());
		mappings = new ArrayList<>(queries.size());
		for (int i = 0; i < queries.size(); ++i) {
			publications.add(null);
			mappings.add(null);
		}

		System.err.println("Starting threads");
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
					// TODO exit threads cleanly ?
					System.err.println(e);
					System.exit(1);
				}
			}
		}

		// TODO postprocessing ?

		try {
			output.output(concepts, queries, publications, mappings);
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
	}
}
