/*
 * Copyright © 2018, 2019 Erik Jaaniso
 *
 * This file is part of PubMedApps.
 *
 * PubMedApps is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PubMedApps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PubMedApps.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.edamontology.edammap.pubmedapps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.beust.jcommander.Parameter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.filter.MarkerFilter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.edamontology.pubfetcher.cli.PubFetcherMethods;
import org.edamontology.pubfetcher.core.common.BasicArgs;
import org.edamontology.pubfetcher.core.common.FetcherArgs;
import org.edamontology.pubfetcher.core.common.PubFetcher;
import org.edamontology.pubfetcher.core.common.Version;
import org.edamontology.pubfetcher.core.db.Database;
import org.edamontology.pubfetcher.core.db.DatabaseEntryType;
import org.edamontology.pubfetcher.core.db.publication.PublicationIds;
import org.edamontology.pubfetcher.core.fetching.Fetcher;
import org.edamontology.edammap.cli.Cli;
import org.edamontology.edammap.cli.CliArgs;
import org.edamontology.edammap.core.args.CoreArgs;
import org.edamontology.edammap.core.input.BiotoolsFull;
import org.edamontology.edammap.core.input.Input;
import org.edamontology.edammap.core.mapping.args.MapperArgs;
import org.edamontology.edammap.core.preprocessing.PreProcessor;
import org.edamontology.edammap.core.preprocessing.PreProcessorArgs;
import org.edamontology.edammap.core.processing.ProcessorArgs;
import org.edamontology.edammap.core.query.QueryType;

public final class PubMedApps {

	private static Logger logger;

	static final String MAIN_MARKER = "MAIN";

	// TODO remove
	//private static final String MESH_QUERY = "(Software[MeSH Terms]) AND (genetics[MeSH Subheading] OR Genetics[MeSH Terms] OR Genomics[MeSH Terms] OR Genetic Phenomena[MeSH Terms] OR Biochemical Phenomena[MeSH Terms] OR Genetic Techniques[MeSH Terms] OR Molecular Probe Techniques[MeSH Terms] OR Nucleic Acids, Nucleotides, and Nucleosides[MeSH Terms] OR Amino Acids, Peptides, and Proteins[MeSH Terms]) AND (\"2013/01/01\"[PDat] : \"2100/01/01\"[PDat])";
	private static final String MESH_QUERY = "(Software[MeSH Terms]) AND (\"2013/01/01\"[PDat] : \"2017/12/31\"[PDat])";

	// TODO remove
	private static void meshQuery(FetcherArgs fetcherArgs) throws IOException, ParseException, URISyntaxException {
		String meshQuery = new URI("https", "eutils.ncbi.nlm.nih.gov", "/entrez/eutils/esearch.fcgi", "db=pubmed&term=" + MESH_QUERY + "&retmax=1000000", null).toASCIIString();

		Set<String> pmids = new LinkedHashSet<>();

		Document doc = new Fetcher(fetcherArgs.getPrivateArgs()).getDoc(meshQuery, false, fetcherArgs);
		if (doc != null) {
			for (Element id : doc.getElementsByTag("Id")) {
				pmids.add(id.text());
			}
		}

		for (String pmid : pmids) {
			System.out.println(pmid + "\t\t");
		}
	}

	// TODO remove
	private static void journalQuery(FetcherArgs fetcherArgs) throws IOException, ParseException, URISyntaxException {
		List<String> journal = PubFetcher.getResource(PubMedApps.class, "resources/journal.txt").stream().map(j -> "\"" + j + "\"[Journal]").collect(Collectors.toList());

		String journalQuery = new URI("https", "eutils.ncbi.nlm.nih.gov", "/entrez/eutils/esearch.fcgi", "db=pubmed&term=" + "(" + String.join(" OR ", journal) + ") AND (\"2018/01/01\"[PDat] : \"2018/12/31\"[PDat])" + "&retmax=1000000", null).toASCIIString();

		Set<String> pmids = new LinkedHashSet<>();

		Document doc = new Fetcher(fetcherArgs.getPrivateArgs()).getDoc(journalQuery, false, fetcherArgs);
		if (doc != null) {
			for (Element id : doc.getElementsByTag("Id")) {
				pmids.add(id.text());
			}
		}

		for (String pmid : pmids) {
			System.out.println(pmid + "\t\t");
		}
		System.out.println(journalQuery);
		System.out.println("pmids size: " + pmids.size());
	}

	private static Step getStep(Path outputPath) throws IOException {
		Path stepPath = outputPath.resolve(Common.STEP_FILE);
		if (!stepPath.toFile().exists()) {
			return Step.none;
		}
		try (BufferedReader br = Files.newBufferedReader(stepPath, StandardCharsets.UTF_8)) {
			String line = br.readLine();
			if (line != null) {
				return Step.fromString(line);
			} else {
				return Step.none;
			}
		}
	}

	private static void setStep(Path outputPath, Step step) throws IOException {
		Path stepPath = outputPath.resolve(Common.STEP_FILE);
		try (BufferedWriter bw = Files.newBufferedWriter(stepPath, StandardCharsets.UTF_8)) {
			bw.write(step.toString());
			bw.write("\n");
		}
	}

	private static void checkStepNone(Path outputPath) throws IOException {
		Step step = getStep(outputPath);
		if (step != Step.none) {
			throw new IllegalArgumentException("Can't run setup operations anymore! Next step is " + step + ". For continuing with current setup, try -resume " + outputPath.toString() + ".");
		}
	}

	private static void checkSetupDone(Path outputPath, Step step) {
		Path edamPath = outputPath.resolve(Common.EDAM_FILE);
		Path idfPath = outputPath.resolve(Common.IDF_FILE);
		Path idfStemmedPath = outputPath.resolve(Common.IDF_STEMMED_FILE);
		Path biotoolsPath = outputPath.resolve(Common.BIOTOOLS_FILE);
		Path pubPath = outputPath.resolve(Common.PUB_FILE);
		Path dbPath = outputPath.resolve(Common.DB_FILE);
		List<String> missing = new ArrayList<>();
		if (!edamPath.toFile().exists()) missing.add(edamPath.toString());
		if (!idfPath.toFile().exists()) missing.add(idfPath.toString());
		if (!idfStemmedPath.toFile().exists()) missing.add(idfStemmedPath.toString());
		if (!biotoolsPath.toFile().exists()) missing.add(biotoolsPath.toString());
		if (!pubPath.toFile().exists()) missing.add(pubPath.toString());
		if (!dbPath.toFile().exists()) missing.add(dbPath.toString());
		if (!missing.isEmpty()) {
			throw new IllegalArgumentException("Can't run step " + step + ", as setup is not yet done! Missing files: " + String.join(", ", missing) + ".");
		}
	}

	private static void copy(String inputFile, Path outputPath, FetcherArgs fetcherArgs) throws IOException {
		if (Input.isProtocol(inputFile)) {
			InputStream from = Input.newInputStream(inputFile, true, fetcherArgs.getTimeout(), fetcherArgs.getPrivateArgs().getUserAgent());
			Files.copy(from, outputPath, StandardCopyOption.REPLACE_EXISTING);
		} else {
			Files.copy(Paths.get(inputFile), outputPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static void copyEdam(Path outputPath, String edam, FetcherArgs fetcherArgs, String logPrefix) throws IOException {
		Path edamTo = outputPath.resolve(Common.EDAM_FILE);
		logger.info(MarkerManager.getMarker(MAIN_MARKER), "{}Copy EDAM ontology from {} to {}", logPrefix, edam, edamTo.toString());
		copy(edam, edamTo, fetcherArgs);
	}

	private static void copyIdf(Path outputPath, String idf, String idfStemmed, FetcherArgs fetcherArgs, String logPrefix) throws IOException {
		Marker mainMarker = MarkerManager.getMarker(MAIN_MARKER);

		Path idfTo = outputPath.resolve(Common.IDF_FILE);
		logger.info(mainMarker, "{}Copy non-stemmed IDF from {} to {}", logPrefix, idf, idfTo.toString());
		copy(idf, idfTo, fetcherArgs);

		Path idfStemmedTo = outputPath.resolve(Common.IDF_STEMMED_FILE);
		logger.info(mainMarker, "{}Copy stemmed IDF from {} to {}", logPrefix, idfStemmed, idfStemmedTo.toString());
		copy(idfStemmed, idfStemmedTo, fetcherArgs);
	}

	private static void getBiotools(Path outputPath, FetcherArgs fetcherArgs, LogLevel logLevel, String logPrefix) throws IOException {
		String biotoolsFile = outputPath.resolve(Common.BIOTOOLS_FILE).toString();
		logger.info(MarkerManager.getMarker(MAIN_MARKER), "{}Get all bio.tools content to {}", logPrefix, biotoolsFile);
		int count = BiotoolsFull.get(biotoolsFile, fetcherArgs, false, logLevel != LogLevel.INFO && logLevel != LogLevel.DEBUG);
		logger.info(MarkerManager.getMarker(MAIN_MARKER), "{}Got {} bio.tools entries to {}", logPrefix, count, biotoolsFile);
	}

	private static void copyBiotools(Path outputPath, String biotools, FetcherArgs fetcherArgs, String logPrefix) throws IOException {
		Path biotoolsTo = outputPath.resolve(Common.BIOTOOLS_FILE);
		logger.info(MarkerManager.getMarker(MAIN_MARKER), "{}Copy all bio.tools content from {} to {}", logPrefix, biotools, biotoolsTo.toString());
		copy(biotools, biotoolsTo, fetcherArgs);
	}

	private static void selectPub(Path outputPath, String logPrefix) {
		// TODO
	}

	private static void copyPub(Path outputPath, String pub, FetcherArgs fetcherArgs, String logPrefix) throws IOException {
		Path pubTo = outputPath.resolve(Common.PUB_FILE);
		logger.info(MarkerManager.getMarker(MAIN_MARKER), "{}Copy publication IDs from {} to {}", logPrefix, pub, pubTo.toString());
		copy(pub, pubTo, fetcherArgs);
	}

	private static void initDb(Path outputPath, String logPrefix) throws IOException {
		String dbFile = outputPath.resolve(Common.DB_FILE).toString();
		logger.info(MarkerManager.getMarker(MAIN_MARKER), "{}Initialise database file {}", logPrefix, dbFile);
		Database.init(dbFile);
	}

	private static void copyDb(Path outputPath, String db, FetcherArgs fetcherArgs, String logPrefix) throws IOException {
		Path dbTo = outputPath.resolve(Common.DB_FILE);
		logger.info(MarkerManager.getMarker(MAIN_MARKER), "{}Copy database file from {} to {}", logPrefix, db, dbTo.toString());
		copy(db, dbTo, fetcherArgs);
	}

	private static void fetchPub(Path outputPath, int threads, FetcherArgs fetcherArgs, LogLevel logLevel, String logPrefix) throws IOException, ParseException {
		setStep(outputPath, Step.fetchPub);

		Marker mainMarker = MarkerManager.getMarker(MAIN_MARKER);

		String pubFile = outputPath.resolve(Common.PUB_FILE).toString();
		String dbFile = outputPath.resolve(Common.DB_FILE).toString();
		List<PublicationIds> publicationIdsList = PubFetcher.pubFile(Collections.singletonList(pubFile), PubMedApps.class.getSimpleName());
		logger.info(mainMarker, "{}Loaded {} publication IDs from {}", logPrefix, publicationIdsList.size(), pubFile);
		Set<PublicationIds> publicationIds = new LinkedHashSet<>();
		publicationIds.addAll(publicationIdsList);
		logger.info(mainMarker, "{}Fetching {} publications using PubFetcher", logPrefix, publicationIds.size());
		PubFetcherMethods.dbFetch(null, threads, publicationIds, dbFile, new Fetcher(fetcherArgs.getPrivateArgs()), null, fetcherArgs, true, publicationIds.size(), logLevel != LogLevel.INFO && logLevel != LogLevel.DEBUG, DatabaseEntryType.publication);
		try (Database db = new Database(dbFile)) {
			logger.info(mainMarker, "{}Database {} has {} publications", logPrefix, dbFile, db.getPublicationsSize());
		}

		setStep(outputPath, Step.pass1);
	}

	private static void pass1(Path outputPath, PreProcessor preProcessor, String logPrefix) throws IOException {
		Pass1.run(outputPath, preProcessor, logPrefix);
		setStep(outputPath, Step.fetchWeb);
	}

	private static void fetchWeb(Path outputPath, int threads, FetcherArgs fetcherArgs, LogLevel logLevel, String logPrefix) throws IOException, ParseException {
		Marker mainMarker = MarkerManager.getMarker(MAIN_MARKER);

		String webFile = outputPath.resolve(Common.WEB_FILE).toString();
		String docFile = outputPath.resolve(Common.DOC_FILE).toString();
		String dbFile = outputPath.resolve(Common.DB_FILE).toString();

		List<String> webpageUrlsList = PubFetcher.webFile(Collections.singletonList(webFile));
		logger.info(mainMarker, "{}Loaded {} webpage URLs from {}", logPrefix, webpageUrlsList.size(), webFile);
		Set<String> webpageUrls = new LinkedHashSet<>();
		webpageUrls.addAll(webpageUrlsList);
		logger.info(mainMarker, "{}Fetching {} webpages using PubFetcher", logPrefix, webpageUrls.size());
		PubFetcherMethods.dbFetch(null, threads, webpageUrls, dbFile, new Fetcher(fetcherArgs.getPrivateArgs()), null, fetcherArgs, true, webpageUrls.size(), logLevel != LogLevel.INFO && logLevel != LogLevel.DEBUG, DatabaseEntryType.webpage);
		try (Database db = new Database(dbFile)) {
			logger.info(mainMarker, "{}Database {} has {} webpages", logPrefix, dbFile, db.getWebpagesSize());
		}

		List<String> docUrlsList = PubFetcher.webFile(Collections.singletonList(docFile));
		logger.info(mainMarker, "{}Loaded {} doc URLs from {}", logPrefix, docUrlsList.size(), docFile);
		Set<String> docUrls = new LinkedHashSet<>();
		docUrls.addAll(docUrlsList);
		logger.info(mainMarker, "{}Fetching {} docs using PubFetcher", logPrefix, docUrls.size());
		PubFetcherMethods.dbFetch(null, threads, docUrls, dbFile, new Fetcher(fetcherArgs.getPrivateArgs()), null, fetcherArgs, true, docUrls.size(), logLevel != LogLevel.INFO && logLevel != LogLevel.DEBUG, DatabaseEntryType.doc);
		try (Database db = new Database(dbFile)) {
			logger.info(mainMarker, "{}Database {} has {} docs", logPrefix, dbFile, db.getDocsSize());
		}

		setStep(outputPath, Step.pass2);
	}

	private static void pass2(Path outputPath, PreProcessor preProcessor, FetcherArgs fetcherArgs, String logPrefix) throws IOException, ParseException {
		Pass2.run(outputPath, preProcessor, fetcherArgs, logPrefix);
		setStep(outputPath, Step.map);
	}

	private static void map(Path outputPath, int threads, PreProcessorArgs preProcessorArgs, FetcherArgs fetcherArgs, MapperArgs mapperArgs, LogLevel logLevel, Version version, String logPrefix) throws IOException, ParseException {
		Marker mainMarker = MarkerManager.getMarker(MAIN_MARKER);

		String edamFile = outputPath.resolve(Common.EDAM_FILE).toString();
		String newFile = outputPath.resolve(Common.NEW_FILE).toString();
		String mapTxtFile = outputPath.resolve(Common.MAP_TXT_FILE).toString();
		String mapHtmlDirectory = outputPath.resolve(Common.MAP_HTML_DIRECTORY).toString();
		String mapJsonFile = outputPath.resolve(Common.MAP_JSON_FILE).toString();
		String toBiotoolsFile = outputPath.resolve(Common.TO_BIOTOOLS_FILE).toString();

		File mapHtmlFile = outputPath.resolve(Common.MAP_HTML_DIRECTORY).toFile();
		if (mapHtmlFile.isDirectory()) {
			logger.info(mainMarker, "{}Deleting old mapping HTML report directory {}/", logPrefix, mapHtmlDirectory.toString());
			for (File file : mapHtmlFile.listFiles()) {
				file.delete();
			}
			mapHtmlFile.delete();
		}

		logger.info(mainMarker, "{}Mapping annotations from {} to bio.tools entries in {} using EDAMmap", logPrefix, edamFile, newFile);

		CliArgs cliArgs = new CliArgs();
		cliArgs.setEdam(edamFile);
		cliArgs.setQuery(newFile);
		cliArgs.setType(QueryType.biotools);
		cliArgs.setOutput(mapTxtFile);
		cliArgs.setReport(mapHtmlDirectory);
		cliArgs.setJson(mapJsonFile);
		cliArgs.setBiotools(toBiotoolsFile);
		cliArgs.setThreads(threads);

		CoreArgs coreArgs = new CoreArgs();
		ProcessorArgs processorArgs = new ProcessorArgs();
		processorArgs.setFetching(false);
		processorArgs.setDb(outputPath.resolve(Common.DB_FILE).toString());
		processorArgs.setIdf(outputPath.resolve(Common.IDF_FILE).toString());
		processorArgs.setIdfStemmed(outputPath.resolve(Common.IDF_STEMMED_FILE).toString());
		coreArgs.setProcessorArgs(processorArgs);
		coreArgs.setPreProcessorArgs(preProcessorArgs);
		coreArgs.setFetcherArgs(fetcherArgs);
		coreArgs.setMapperArgs(mapperArgs);
		cliArgs.setCoreArgs(coreArgs);

		int resultsSize;
		try {
			resultsSize = Cli.run(cliArgs, version, logLevel != LogLevel.INFO && logLevel != LogLevel.DEBUG);
		} finally {
			Cli.closeDatabase();
		}
		logger.info(mainMarker, "{}Mapping results can be found in {} or {}/ or {}", logPrefix, mapTxtFile, mapHtmlDirectory, mapJsonFile);

		logger.info(mainMarker, "{}PubMedApps has produced {} new entries to add to bio.tools in {}", logPrefix, resultsSize, toBiotoolsFile);
		setStep(outputPath, Step.done);
	}

	private static boolean requiredArgs(String[] requiredArgs, String arg, PubMedAppsArgs args) throws ReflectiveOperationException {
		for (String requiredArg : requiredArgs) {
			Field requiredArgField = PubMedAppsArgs.class.getDeclaredField(requiredArg);
			if (requiredArgField.get(args) == null) {
				logger.error(MarkerManager.getMarker(MAIN_MARKER), "{} is required for {}", Arrays.toString(requiredArgField.getAnnotation(Parameter.class).names()),
					Arrays.toString(PubMedAppsArgs.class.getDeclaredField(arg).getAnnotation(Parameter.class).names()));
				throw new IllegalArgumentException("Missing required parameter");
			}
		}
		return true;
	}

	private static void oneCommand(PubMedAppsArgs args) throws ReflectiveOperationException {
		List<String> specifiedCommands = new ArrayList<>();
		List<String> allCommands = new ArrayList<>();
		for (Field field : PubMedAppsArgs.class.getDeclaredFields()) {
			Parameter parameter = field.getAnnotation(Parameter.class);
			if (parameter != null && parameter.names().length != 0) {
				String name = parameter.names()[0];
				if (name.matches("-[^-].*")) {
					Object value = field.get(args);
					if (value instanceof Boolean) {
						if ((Boolean) value) {
							specifiedCommands.add(name);
						}
					} else if (value != null) {
						specifiedCommands.add(name);
					}
					allCommands.add(name);
				}
			}
		}
		if (specifiedCommands.size() < 1) {
			logger.error(MarkerManager.getMarker(MAIN_MARKER), "Must specify one command: one of {}", allCommands);
			throw new IllegalArgumentException("No commands specified");
		} else if (specifiedCommands.size() > 1) {
			logger.error(MarkerManager.getMarker(MAIN_MARKER), "Can specify only one command: specified {}", specifiedCommands);
			throw new IllegalArgumentException("More than one command specified");
		}
	}

	private static String outputDir(PubMedAppsArgs args) {
		if (args.copyEdam != null) return args.copyEdam;
		if (args.copyIdf != null) return args.copyIdf;
		if (args.getBiotools != null) return args.getBiotools;
		if (args.copyBiotools != null) return args.copyBiotools;
		if (args.selectPub != null) return args.selectPub;
		// TODO other args.select
		if (args.copyPub != null) return args.copyPub;
		if (args.initDb != null) return args.initDb;
		if (args.copyDb != null) return args.copyDb;
		if (args.fetchPub != null) return args.fetchPub;
		if (args.pass1 != null) return args.pass1;
		if (args.fetchWeb != null) return args.fetchWeb;
		if (args.pass2 != null) return args.pass2;
		if (args.map != null) return args.map;
		if (args.all != null) return args.all;
		if (args.resume != null) return args.resume;
		return null;
	}

	private static void run(PubMedAppsArgs args, Version version) throws IOException, ParseException, ReflectiveOperationException, URISyntaxException {
		oneCommand(args);

		Path outputPath = null;
		String output = outputDir(args);
		if (output != null) {
			outputPath = PubFetcher.outputPath(output, true, true);
		}

		boolean stemming = args.preProcessorArgs.isStemming();
		args.preProcessorArgs.setStemming(false);
		PreProcessor preProcessor = new PreProcessor(args.preProcessorArgs);
		args.preProcessorArgs.setStemming(stemming);

		if (args.copyEdam != null && requiredArgs(new String[] { "edam" }, "copyEdam", args)) {
			checkStepNone(outputPath);
			copyEdam(outputPath, args.edam, args.fetcherArgs, "");
		}

		if (args.copyIdf != null && requiredArgs(new String[] { "idf", "idfStemmed" }, "copyIdf", args)) {
			checkStepNone(outputPath);
			copyIdf(outputPath, args.idf, args.idfStemmed, args.fetcherArgs, "");
		}

		if (args.getBiotools != null) {
			checkStepNone(outputPath);
			getBiotools(outputPath, args.fetcherArgs, args.verbose, "");
		}

		if (args.copyBiotools != null && requiredArgs(new String[] { "biotools" }, "copyBiotools", args)) {
			checkStepNone(outputPath);
			copyBiotools(outputPath, args.biotools, args.fetcherArgs, "");
		}

		if (args.selectPub != null) {
			checkStepNone(outputPath);
			selectPub(outputPath, "");
		}

		if (args.copyPub != null && requiredArgs(new String[] { "pub" }, "copyPub", args)) {
			checkStepNone(outputPath);
			copyPub(outputPath, args.pub, args.fetcherArgs, "");
		}

		if (args.initDb != null) {
			checkStepNone(outputPath);
			Path dbPath = outputPath.resolve(Common.DB_FILE);
			if (dbPath.toFile().exists()) {
				throw new IllegalArgumentException("Can't run -init-db, as " + dbPath.toString() + " already exists! Try -resume " + outputPath.toString() + ", if rest of setup is done.");
			}
			initDb(outputPath, "");
		}

		if (args.copyDb != null && requiredArgs(new String[] { "db" }, "copyDb", args)) {
			checkStepNone(outputPath);
			copyDb(outputPath, args.db, args.fetcherArgs, "");
		}

		if (args.fetchPub != null) {
			checkSetupDone(outputPath, Step.fetchPub);
			fetchPub(outputPath, args.fetcherThreads, args.fetcherArgs, args.verbose, "");
		}

		if (args.pass1 != null) {
			checkSetupDone(outputPath, Step.pass1);
			String dbFile = outputPath.resolve(Common.DB_FILE).toString();
			try (Database db = new Database(dbFile)) {
				if (db.getPublicationsSize() == 0) {
					throw new IllegalArgumentException("Can't run step -pass1, as database file " + dbFile + " is empty! Try running step -fetch-pub first.");
				}
			}
			pass1(outputPath, preProcessor, "");
		}

		if (args.fetchWeb != null) {
			checkSetupDone(outputPath, Step.fetchWeb);
			Path webPath = outputPath.resolve(Common.WEB_FILE);
			Path docPath = outputPath.resolve(Common.DOC_FILE);
			boolean webExists = webPath.toFile().exists();
			boolean docExists = docPath.toFile().exists();
			if (!webExists || !docExists) {
				throw new IllegalArgumentException("Can't run step -fetch-web, as missing " + (!webExists ? webPath.toString() + (!docExists ? " and " : "") : "") + (!docExists ? docPath.toString() : "") + "! Try running steps -fetch-pub and -pass1 first.");
			}
			fetchWeb(outputPath, args.fetcherThreads, args.fetcherArgs, args.verbose, "");
		}

		if (args.pass2 != null) {
			checkSetupDone(outputPath, Step.pass2);
			Path pass1Path = outputPath.resolve(Common.PASS1_FILE);
			if (!pass1Path.toFile().exists()) {
				throw new IllegalArgumentException("Can't run step -pass2, as missing " + pass1Path.toString() + "! Try running steps -fetch-pub, -pass1 and -fetch-web first.");
			}
			pass2(outputPath, preProcessor, args.fetcherArgs, "");
		}

		if (args.map != null) {
			checkSetupDone(outputPath, Step.map);
			Path newPath = outputPath.resolve(Common.NEW_FILE);
			if (!newPath.toFile().exists()) {
				throw new IllegalArgumentException("Can't run step -map, as missing " + newPath.toString() + "! Try running steps -fetch-pub, -pass1, -fetch-web and -pass2 first.");
			}
			map(outputPath, args.mapperThreads, args.preProcessorArgs, args.fetcherArgs, args.mapperArgs, args.verbose, version, "");
		}

		if (args.all != null && requiredArgs(new String[] { "edam", "idf", "idfStemmed" }, "all", args)) {
			Marker mainMarker = MarkerManager.getMarker(MAIN_MARKER);
			logger.info(mainMarker, "Running setup and all steps");
			logger.info(mainMarker, "0 setup");
			checkStepNone(outputPath);
			copyEdam(outputPath, args.edam, args.fetcherArgs, "0/5 ");
			copyIdf(outputPath, args.idf, args.idfStemmed, args.fetcherArgs, "0/5 ");
			if (args.biotools != null) {
				copyBiotools(outputPath, args.biotools, args.fetcherArgs, "0/5 ");
			} else {
				getBiotools(outputPath, args.fetcherArgs, args.verbose, "0/5 ");
			}
			if (args.pub != null) {
				copyPub(outputPath, args.pub, args.fetcherArgs, "0/5 ");
			} else {
				selectPub(outputPath, "0/5 ");
			}
			if (args.db != null) {
				copyDb(outputPath, args.db, args.fetcherArgs, "0/5 ");
			} else {
				Path dbPath = outputPath.resolve(Common.DB_FILE);
				if (dbPath.toFile().exists()) {
					logger.warn(mainMarker, "0/5 Not initialising {}, as it already exists!", dbPath.toString());
				} else {
					initDb(outputPath, "0/5 ");
				}
			}
			logger.info(mainMarker, "1 step {}", Step.fetchPub);
			fetchPub(outputPath, args.fetcherThreads, args.fetcherArgs, args.verbose, "1/5 ");
			logger.info(mainMarker, "2 step {}", Step.pass1);
			pass1(outputPath, preProcessor, "2/5 ");
			logger.info(mainMarker, "3 step {}", Step.fetchWeb);
			fetchWeb(outputPath, args.fetcherThreads, args.fetcherArgs, args.verbose, "3/5 ");
			logger.info(mainMarker, "4 step {}", Step.pass2);
			pass2(outputPath, preProcessor, args.fetcherArgs, "4/5 ");
			logger.info(mainMarker, "5 step {}", Step.map);
			map(outputPath, args.mapperThreads, args.preProcessorArgs, args.fetcherArgs, args.mapperArgs, args.verbose, version, "5/5 ");
		}

		if (args.resume != null) {
			Marker mainMarker = MarkerManager.getMarker(MAIN_MARKER);
			Step step = getStep(outputPath);
			if (step == Step.done) {
				logger.info(mainMarker, "Not resuming, as according to {} all steps are done", outputPath.resolve(Common.STEP_FILE));
				return;
			}
			if (step == Step.none) {
				step = Step.fetchPub;
			}
			logger.info(mainMarker, "Resuming from step {}", step);
			checkSetupDone(outputPath, step);
			if (step == Step.fetchPub) {
				logger.info(mainMarker, "1 step {}", Step.fetchPub);
				fetchPub(outputPath, args.fetcherThreads, args.fetcherArgs, args.verbose, "1/5 ");
			}
			if (step == Step.fetchPub || step == Step.pass1) {
				logger.info(mainMarker, "2 step {}", Step.pass1);
				pass1(outputPath, preProcessor, "2/5 ");
			}
			if (step == Step.fetchPub || step == Step.pass1 || step == Step.fetchWeb) {
				logger.info(mainMarker, "3 step {}", Step.fetchWeb);
				fetchWeb(outputPath, args.fetcherThreads, args.fetcherArgs, args.verbose, "3/5 ");
			}
			if (step == Step.fetchPub || step == Step.pass1 || step == Step.fetchWeb || step == Step.pass2) {
				logger.info(mainMarker, "4 step {}", Step.pass2);
				pass2(outputPath, preProcessor, args.fetcherArgs, "4/5 ");
			}
			if (step == Step.fetchPub || step == Step.pass1 || step == Step.fetchWeb || step == Step.pass2 || step == Step.map) {
				logger.info(mainMarker, "5 step {}", Step.map);
				map(outputPath, args.mapperThreads, args.preProcessorArgs, args.fetcherArgs, args.mapperArgs, args.verbose, version, "5/5 ");
			}
		}

		// TODO remove
		if (args.meshQuery) {
			meshQuery(args.fetcherArgs);
		}

		// TODO remove
		if (args.journalQuery) {
			journalQuery(args.fetcherArgs);
		}

		if (args.beforeAfter && requiredArgs(new String[] { "idf", "pub", "db" }, "beforeAfter", args)) {
			Test.beforeAfter(args.idf, args.pub, args.db, preProcessor);
		}

		if (args.europepmcAbstract != null && requiredArgs(new String[] { "db" }, "europepmcAbstract", args)) {
			Test.europepmcAbstract(args.europepmcAbstract, args.db, preProcessor, args.fetcherArgs);
		}
	}

	public static void main(String[] argv) throws IOException, ReflectiveOperationException {
		Version version = new Version(PubMedApps.class);

		PubMedAppsArgs args = BasicArgs.parseArgs(argv, PubMedAppsArgs.class, version, true);
		String output = outputDir(args);
		// this will create the output directory
		BasicArgs.setExternalLogPath(args, output != null ? Paths.get(output).resolve(Common.LOG_FILE).toString() : null);

		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		if (args.verbose != LogLevel.OFF) {
			Level level;
			switch (args.verbose) {
				case ERROR: level = Level.ERROR; break;
				case WARN: level = Level.WARN; break;
				case INFO: level = Level.INFO; break;
				case DEBUG: level = Level.DEBUG; break;
				default: level = Level.OFF; break;
			}
			config.getLoggerConfig("org.edamontology").addAppender(config.getAppender("Console"), level, MarkerFilter.createFilter(MAIN_MARKER, Result.DENY, Result.NEUTRAL));
		}
		ctx.updateLoggers();

		Marker mainMarker = MarkerManager.getMarker(MAIN_MARKER);

		// logger must be called only after configuration changes have been made in BasicArgs.parseArgs()
		// otherwise invalid.log will be created if arg --log is null (and var output is also null)
		logger = LogManager.getLogger();
		logger.debug(String.join(" ", argv));
		logger.info(mainMarker, "This is {} {}", version.getName(), version.getVersion());

		try {
			run(args, version);
			logger.info(mainMarker, "Done");
		} catch (Throwable e) {
			logger.error(mainMarker, "Exception!", e);
			System.exit(1);
		}
	}
}
