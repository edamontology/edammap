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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.Args;
import org.edamontology.pubfetcher.core.common.BasicArgs;
import org.edamontology.pubfetcher.core.common.FetcherArgs;
import org.edamontology.pubfetcher.core.common.PubFetcher;
import org.edamontology.pubfetcher.core.common.Version;

import org.edamontology.edammap.cli.CliArgs;
import org.edamontology.edammap.core.idf.Idf;
import org.edamontology.edammap.core.input.Input;
import org.edamontology.edammap.core.input.json.Biotools;
import org.edamontology.edammap.core.output.Report;
import org.edamontology.edammap.core.preprocessing.PreProcessor;
import org.edamontology.edammap.core.processing.Processor;
import org.edamontology.edammap.core.processing.ProcessorArgs;
import org.edamontology.edammap.core.query.QueryLoader;
import org.edamontology.edammap.server.Server;

public final class Util {

	private static final Logger logger = LogManager.getLogger();

	private static void makeIdf(String queryPath, String database, String idfPath, UtilArgs args, boolean stemming) throws IOException, ParseException {
		logger.info("Make query IDF from file {} of type {} to {}{}", queryPath, args.makeIdfType, idfPath, database != null ? " using database " + database : "");

		ProcessorArgs processorArgs = new ProcessorArgs();
		processorArgs.setFetching(false);
		processorArgs.setDb(database);
		processorArgs.setIdf(null);
		processorArgs.setIdfStemmed(null);
		Processor processor = new Processor(processorArgs, args.fetcherArgs.getPrivateArgs());

		int idfs = processor.makeQueryIdf(QueryLoader.get(queryPath, args.makeIdfType,
			args.fetcherArgs.getTimeout(), args.fetcherArgs.getPrivateArgs().getUserAgent()),
			args.makeIdfType, idfPath, args.makeIdfWebpagesDocs, args.makeIdfFulltext,
			new PreProcessor(stemming), null, args.fetcherArgs, true);
		logger.info("Wrote {} IDFs to {}", idfs, idfPath);

		processor.closeDatabase();
	}

	private static void printIdfTop(String inputPath, long n) throws IOException {
		new Idf(inputPath, true).getTop().stream()
			.limit(n).forEach(e -> System.out.println(e.getTerm() + "\t" + e.getCount()));
	}

	private static void printIdf(String inputPath, String term, boolean stemming) throws IOException {
		System.out.println(new Idf(inputPath).getIdf(new PreProcessor(stemming).process(term)));
	}

	private static void biotoolsFull(String outputPath, FetcherArgs fetcherArgs, boolean dev) throws IOException {
		logger.info("Make full {}bio.tools JSON to {}", dev ? "dev." : "", outputPath);
		String api = "https://" + (dev ? "dev." : "") + "bio.tools/api/tool";

		Path output = PubFetcher.outputPath(outputPath);

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
		logger.info("Copying server CSS, JS and fonts to {}", outputPath);
		Path path = PubFetcher.outputPath(outputPath, true, false);
		Files.createDirectory(path);
		Server.copyHtmlResources(path);
		Report.copyFontResources(path);
		logger.info("Copying output CSS and fonts to {}", outputPath);
		Path versionPath = PubFetcher.outputPath(outputPath + "/" + version.getVersion(), true, false);
		Files.createDirectory(versionPath);
		Report.copyHtmlResources(versionPath, version);
		Report.copyFontResources(versionPath);
	}

	private static void writeOptionsConfDescription(Writer writer, String description) throws IOException {
		writer.write("#");
		int lineLength = 1;
		for (String descriptionPart : description.split(" ")) {
			if (lineLength + 1 + descriptionPart.length() < 80) {
				writer.write(" ");
				writer.write(descriptionPart);
				lineLength += 1 + descriptionPart.length();
			} else {
				writer.write("\n#   ");
				writer.write(descriptionPart);
				lineLength = 4 + descriptionPart.length();
			}
		}
		writer.write("\n");
	}

	private static void writeOptionsConfArgs(Writer writer, Args args) throws IOException {
		writer.write("\n");
		writer.write("\n");

		writer.write("### ");
		writer.write(args.getLabel());
		writer.write("\n");

		for (Arg<?, ?> arg : args.getArgs()) {
			writer.write("\n");

			writeOptionsConfDescription(writer, arg.getDescription());
			writer.write("# ");
			if (arg.getDefault() != null) {
				writer.write("Default: ");
				if (!arg.getDefault().toString().isEmpty()) {
					writer.write(arg.getDefault().toString());
				} else {
					writer.write("<empty string>");
				}
			} else {
				writer.write("Mandatory");
			}
			writer.write("\n");

			if (arg.getEnumClass() != null) {
				writer.write("# Possible Values: [");
				writer.write(Arrays.stream(arg.getEnumClass().getEnumConstants()).map(e -> e.name()).collect(Collectors.joining(", ")));
				writer.write("]\n");
			}

			if (arg.getMin() != null) {
				writer.write("# Min: ");
				writer.write(arg.getMin().toString());
				writer.write("\n");
			}

			if (arg.getMax() != null) {
				writer.write("# Max: ");
				writer.write(arg.getMax().toString());
				writer.write("\n");
			}

			writer.write("#--");
			writer.write(arg.getId());
			writer.write("\n");

			if (arg.getDefault() != null) {
				if (arg.getDefault() instanceof List) {
					if (!((List<?>) arg.getDefault()).isEmpty()) {
						for (Object o : (List<?>) arg.getDefault()) {
							writer.write("#");
							writer.write(o.toString());
							writer.write("\n");
						}
					} else {
						writer.write("#\n");
					}
				} else {
					writer.write("#");
					writer.write(arg.getDefault().toString());
					writer.write("\n");
				}
			} else {
				writer.write("#\n");
			}
		}
	}

	private static void makeOptionsConf(String outputPath, Version version) throws IOException {
		logger.info("Make new options configuration file to {}", outputPath);
		try (BufferedWriter writer = Files.newBufferedWriter(PubFetcher.outputPath(outputPath), StandardCharsets.UTF_8)) {
			writer.write("### EDAMmap-CLI configuration ###\n");
			writer.write("# Generated by ");
			writer.write(version.getName());
			writer.write(" ");
			writer.write(version.getVersion());
			writer.write(" #\n");

			writer.write("\n");
			writer.write("\n");
			writeOptionsConfDescription(writer, BasicArgs.logDescription);
			writer.write("#--log\n");
			writer.write("#\n");

			CliArgs cliArgs = new CliArgs();
			writeOptionsConfArgs(writer, cliArgs);
			writeOptionsConfArgs(writer, cliArgs.getCoreArgs().getProcessorArgs());
			writeOptionsConfArgs(writer, cliArgs.getCoreArgs().getPreProcessorArgs());
			writeOptionsConfArgs(writer, cliArgs.getCoreArgs().getFetcherArgs());
			writeOptionsConfArgs(writer, cliArgs.getCoreArgs().getFetcherArgs().getPrivateArgs());
			writeOptionsConfArgs(writer, cliArgs.getCoreArgs().getMapperArgs());
			writeOptionsConfArgs(writer, cliArgs.getCoreArgs().getMapperArgs().getAlgorithmArgs());
			writeOptionsConfArgs(writer, cliArgs.getCoreArgs().getMapperArgs().getIdfArgs());
			writeOptionsConfArgs(writer, cliArgs.getCoreArgs().getMapperArgs().getMultiplierArgs());
			writeOptionsConfArgs(writer, cliArgs.getCoreArgs().getMapperArgs().getNormaliserArgs());
			writeOptionsConfArgs(writer, cliArgs.getCoreArgs().getMapperArgs().getWeightArgs());
			writeOptionsConfArgs(writer, cliArgs.getCoreArgs().getMapperArgs().getScoreArgs());
		}
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
			printIdf(args.printIdf.get(0), args.printIdf.get(1), false);
		}
		if (args.printIdfStemmed != null) {
			printIdf(args.printIdfStemmed.get(0), args.printIdfStemmed.get(1), true);
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

		if (args.makeOptionsConf != null) {
			makeOptionsConf(args.makeOptionsConf, version);
		}
	}
}
