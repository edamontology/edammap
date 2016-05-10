package edammapper;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.mapdb.DBException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import edammapper.args.MainArgs;
import edammapper.edam.Concept;
import edammapper.edam.Edam;
import edammapper.edam.EdamUri;
import edammapper.fetching.Publication;
import edammapper.mapping.Mapper;
import edammapper.mapping.Mapping;
import edammapper.output.Output;
import edammapper.preprocessing.PreProcessor;
import edammapper.processing.ConceptProcessed;
import edammapper.processing.Processor;
import edammapper.processing.QueryProcessed;
import edammapper.query.Query;
import edammapper.query.QueryLoader;

public class Main implements Runnable {

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

			Mapping mapping = mapper.map(processedQuery, args.getMapperArgs());

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
			System.err.println(e);
			jcommander.usage();
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
		} catch (AccessDeniedException | FileAlreadyExistsException e) {
			System.err.println(e);
			System.exit(1);
		}

		try {
			processor = new Processor(args.getProcessorArgs());
		} catch (IOException | DBException e) {
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
			queries = QueryLoader.get(args.getQuery(), args.getType());
		} catch (IOException | ParseException | XMLStreamException | FactoryConfigurationError e) {
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
			Thread t = new Thread(new Main());
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
