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
import java.nio.file.Path;
import java.text.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.edamontology.edammap.core.idf.Idf;
import org.edamontology.edammap.core.input.Input;
import org.edamontology.edammap.core.input.json.Biotools;
import org.edamontology.edammap.core.processing.Processor;
import org.edamontology.edammap.core.processing.ProcessorArgs;
import org.edamontology.edammap.core.query.QueryLoader;
import org.edamontology.edammap.core.query.QueryType;
import org.edamontology.pubfetcher.FetcherArgs;
import org.edamontology.pubfetcher.FetcherCommon;

public final class Util {

	private static final Logger logger = LogManager.getLogger();

	private static void makeQueryIdf(String queryPath, QueryType type, String outputPath, String database, UtilArgs args) throws IOException, ParseException {
		logger.info("Make query IDF from file {} of type {} to {}{}", queryPath, type, outputPath, database != null ? " using database " + database : "");

		ProcessorArgs processorArgs = new ProcessorArgs();
		processorArgs.setFetcher(false);
		processorArgs.setDatabase(database);
		processorArgs.setIdf(null);
		processorArgs.setPreProcessorArgs(args.preProcessorArgs);

		Processor processor = new Processor(processorArgs);
		processor.makeQueryIdf(QueryLoader.get(queryPath, type, args.fetcherArgs), type, outputPath, args.makeQueryIdfNoWebpagesDocs, args.makeQueryIdfNoFulltext);

		logger.info("Make query IDF: success"); // TODO output number of IDF terms made
	}

	private static void printQueryIdfTop(String inputPath, long n) throws IOException {
		new Idf(inputPath, true).getTop().entrySet().stream()
			.limit(n).forEach(e -> System.out.println(e.getKey() + "\t" + e.getValue()));
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
			try (InputStream is = Input.newInputStream(api + next + "&format=json", false, fetcherArgs)) {
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

	public static void run(UtilArgs args) throws IOException, ParseException {
		if (args == null) {
			throw new IllegalArgumentException("UtilArgs required!");
		}

		if (args.makeQueryIdf != null) {
			makeQueryIdf(args.makeQueryIdf.get(0), QueryType.valueOf(args.makeQueryIdf.get(1)), args.makeQueryIdf.get(2), args.makeQueryIdf.get(3), args);
		}
		if (args.makeQueryIdfWithoutDatabase != null) {
			makeQueryIdf(args.makeQueryIdfWithoutDatabase.get(0), QueryType.valueOf(args.makeQueryIdfWithoutDatabase.get(1)), args.makeQueryIdfWithoutDatabase.get(2), null, args);
		}

		if (args.printQueryIdfTop != null) {
			printQueryIdfTop(args.printQueryIdfTop.get(0), Long.parseLong(args.printQueryIdfTop.get(1)));
		}

		if (args.biotoolsFull != null) {
			biotoolsFull(args.biotoolsFull, args.fetcherArgs, false);
		}
		if (args.biotoolsDevFull != null) {
			biotoolsFull(args.biotoolsDevFull, args.fetcherArgs, true);
		}
	}
}
