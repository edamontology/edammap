package edammapper;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import edammapper.args.Args;
import edammapper.edam.Concept;
import edammapper.edam.ConceptPP;
import edammapper.edam.Edam;
import edammapper.edam.EdamUri;
import edammapper.mapping.Mapper;
import edammapper.mapping.Mapping;
import edammapper.output.Output;
import edammapper.preprocessing.PreProcessor;
import edammapper.query.LoadCsv;
import edammapper.query.Query;

public class Main {
	public static Args parseArgs(String[] argv) {
		Args args = new Args();
		JCommander jcommander = new JCommander(args);
		try {
			jcommander.parse(argv);
		} catch (ParameterException e) {
			System.err.println(e.getLocalizedMessage());
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
		Args args = parseArgs(argv);

		Output output = null;
		try {
			output = new Output(args);
		} catch (AccessDeniedException | FileAlreadyExistsException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}

		PreProcessor pp = null;
		try {
			pp = new PreProcessor(args.getPreProcessorArgs());
		} catch (IOException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}

		Map<EdamUri, Concept> concepts = null;
		try {
			concepts = Edam.load(args.getEdam());
		} catch (OWLOntologyCreationException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}

		Map<EdamUri, ConceptPP> conceptsPP = Edam.process(concepts, pp);

		List<Query> queries = null;
		try {
			queries = LoadCsv.load(args.getQuery(), args.getType());
		} catch (IOException | ParseException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}

		Mapper mapper = new Mapper(concepts, conceptsPP, pp, args.getMapperArgs());
		List<Mapping> mappings = mapper.map(queries);

		try {
			output.output(concepts, queries, mappings);
		} catch (IOException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}
	}
}
