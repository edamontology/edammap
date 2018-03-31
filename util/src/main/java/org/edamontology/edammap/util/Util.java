/*
 * Copyright © 2016, 2017, 2018 Erik Jaaniso
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

package org.edamontology.edammap.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.edamontology.edammap.core.idf.Idf;
import org.edamontology.edammap.core.input.Input;
import org.edamontology.edammap.core.input.json.Biotools;
import org.edamontology.edammap.core.output.Report;
import org.edamontology.edammap.core.preprocessing.PreProcessor;
import org.edamontology.edammap.core.processing.Processor;
import org.edamontology.edammap.core.processing.ProcessorArgs;
import org.edamontology.edammap.core.query.QueryLoader;
import org.edamontology.edammap.server.Server;
import org.edamontology.pubfetcher.FetcherArgs;
import org.edamontology.pubfetcher.FetcherCommon;
import org.edamontology.pubfetcher.Version;

public final class Util {

	private static final Logger logger = LogManager.getLogger();

	private static void makeIdf(String queryPath, String database, String idfPath, UtilArgs args, boolean stemming) throws IOException, ParseException {
		logger.info("Make query IDF from file {} of type {} to {}{}", queryPath, args.makeIdfType, idfPath, database != null ? " using database " + database : "");

		ProcessorArgs processorArgs = new ProcessorArgs();
		processorArgs.setFetching(false);
		processorArgs.setDb(database);
		processorArgs.setIdf(null);
		processorArgs.setIdfStemmed(null);
		Processor processor = new Processor(processorArgs);

		int idfs = processor.makeQueryIdf(QueryLoader.get(queryPath, args.makeIdfType, args.fetcherArgs.getTimeout(), args.fetcherArgs.getPrivateArgs().getUserAgent()),
			args.makeIdfType, idfPath, args.makeIdfWebpagesDocs, args.makeIdfFulltext,
			new PreProcessor(stemming), null, args.fetcherArgs);
		logger.info("Wrote {} IDFs to {}", idfs, idfPath);
	}

	private static void printIdfTop(String inputPath, long n) throws IOException {
		new Idf(inputPath, true).getTop().stream()
			.limit(n).forEach(e -> System.out.println(e.getTerm() + "\t" + e.getCount()));
	}

	private static void printIdf(String inputPath, String term) throws IOException {
		System.out.println(new Idf(inputPath).getIdf(term));
	}

	private static void biotoolsFull(String outputPath, FetcherArgs fetcherArgs, boolean dev) throws IOException {
		logger.info("Make full {}bio.tools JSON to {}", dev ? "dev." : "", outputPath);
		String api = "https://" + (dev ? "dev." : "") + "bio.tools/api/tool";

		Path output = FetcherCommon.outputPath(outputPath);

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);

		Biotools biotoolsFull = new Biotools();
		int count = 0;

		String next = "?page=1";
		while (next != null) {
			try (InputStream is = Input.newInputStream(api + next + "&format=json", false, fetcherArgs.getTimeout(), fetcherArgs.getPrivateArgs().getUserAgent())) {
				Biotools biotools = mapper.readValue(is, Biotools.class);

				biotoolsFull.addTools(biotools.getList());
				biotoolsFull.setCount(biotools.getCount());
				count += biotools.getList().size();

				next = biotools.getNext();
			} catch (Exception e) {
				logger.error("Exception!", e);
				break;
			}
		}

		mapper.writeValue(output.toFile(), biotoolsFull);

		if (count != biotoolsFull.getCount()) {
			logger.error("Got {} entries instead of advertised {}", count, biotoolsFull.getCount());
		}
		logger.info("Made bio.tools JSON with {} entries", count);
	}

	private static void makeServerFiles(String outputPath, Version version) throws IOException {
		logger.info("Copying server HTML, CSS, JS and fonts to {}", outputPath);
		Path path = FetcherCommon.outputPath(outputPath, true, false);
		Files.createDirectory(path);
		Server.copyHtmlResources(path);
		Report.copyFontResources(path);
		logger.info("Copying output CSS and fonts to {}", outputPath);
		Path versionPath = FetcherCommon.outputPath(outputPath + "/" + version.getVersion(), true, false);
		Files.createDirectory(versionPath);
		Report.copyHtmlResources(versionPath, version);
		Report.copyFontResources(versionPath);
	}

	public static void run(UtilArgs args, Version version) throws IOException, ParseException {
		if (args == null) {
			throw new IllegalArgumentException("UtilArgs required!");
		}

		if (args.makeIdf != null) {
			makeIdf(args.makeIdf.get(0), args.makeIdf.get(1), args.makeIdf.get(2), args, false);
		}
		if (args.makeIdfNoDb != null) {
			makeIdf(args.makeIdfNoDb.get(0), null, args.makeIdfNoDb.get(1), args, false);
		}
		if (args.makeIdfStemmed != null) {
			makeIdf(args.makeIdfStemmed.get(0), args.makeIdfStemmed.get(1), args.makeIdfStemmed.get(2), args, true);
		}
		if (args.makeIdfStemmedNoDb != null) {
			makeIdf(args.makeIdfStemmedNoDb.get(0), null, args.makeIdfStemmedNoDb.get(1), args, true);
		}

		if (args.printIdfTop != null) {
			printIdfTop(args.printIdfTop.get(0), Long.parseLong(args.printIdfTop.get(1)));
		}
		if (args.printIdf != null) {
			printIdf(args.printIdf.get(0), args.printIdf.get(1));
		}

		if (args.biotoolsFull != null) {
			biotoolsFull(args.biotoolsFull, args.fetcherArgs, false);
		}
		if (args.biotoolsDevFull != null) {
			biotoolsFull(args.biotoolsDevFull, args.fetcherArgs, true);
		}

		if (args.makeServerFiles != null) {
			makeServerFiles(args.makeServerFiles, version);
		}
	}
}
