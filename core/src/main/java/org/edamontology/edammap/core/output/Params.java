/*
 * Copyright © 2018, 2019 Erik Jaaniso
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

package org.edamontology.edammap.core.output;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;

import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.Args;
import org.edamontology.pubfetcher.core.common.FetcherArgs;
import org.edamontology.pubfetcher.core.common.PubFetcher;

import org.edamontology.edammap.core.args.ArgMain;
import org.edamontology.edammap.core.benchmarking.Measure;
import org.edamontology.edammap.core.benchmarking.Results;
import org.edamontology.edammap.core.benchmarking.Test;
import org.edamontology.edammap.core.edam.Branch;
import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.Edam;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.mapping.args.MapperArgs;
import org.edamontology.edammap.core.preprocessing.PreProcessorArgs;
import org.edamontology.edammap.core.processing.ProcessorArgs;
import org.edamontology.edammap.core.query.Query;

public class Params {

	private static final String MAIN_ARGS_ID = "mainArgs";
	private static final String MAIN_ARGS_LABEL = "Main";

	private static final String COUNTS_ID = "counts";
	private static final String COUNTS_LABEL = "Counts";
	private static final String MEASURES_ID = "measures";
	private static final String MEASURES_LABEL = "Measures";

	private static final String CONCEPTS_SIZE_ID = "conceptsSize";
	private static final String CONCEPTS_SIZE_LABEL = "EDAM concepts";
	private static final String TOPIC_SIZE_ID = "topicSize";
	private static final String TOPIC_SIZE_LABEL = "Topic terms";
	private static final String OPERATION_SIZE_ID = "operationSize";
	private static final String OPERATION_SIZE_LABEL = "Operation terms";
	private static final String DATA_SIZE_ID = "dataSize";
	private static final String DATA_SIZE_LABEL = "Data terms";
	private static final String FORMAT_SIZE_ID = "formatSize";
	private static final String FORMAT_SIZE_LABEL = "Format terms";
	private static final String QUERIES_SIZE_ID = "queriesSize";
	private static final String QUERIES_SIZE_LABEL = "Queries";
	private static final String RESULTS_SIZE_ID = "resultsSize";
	private static final String RESULTS_SIZE_LABEL = "Results";

	private static final String TOTAL_ID = "total";

	private static void write(Writer writer, Object value, Arg<?, ?> arg, boolean input) throws IOException {
		if (arg.getEnumClass() != null) {
			if (!input) {
				write(writer, arg.getId(), arg.getLabel(), arg.getDescription(), value.toString(), arg.getDefault() != null ? arg.getDefault().toString() : null, null, null, arg.getUrl(), input);
			} else {
				if (value instanceof List) {
					writeEnum(true, writer, arg.getId(), arg.getLabel(), arg.getDescription(), arg.getEnumClass(), value, arg.getDefault(), arg.getUrl());
				} else {
					writeEnum(false, writer, arg.getId(), arg.getLabel(), arg.getDescription(), arg.getEnumClass(), value, arg.getDefault(), arg.getUrl());
				}
			}
		} else if (value instanceof Boolean) {
			writeBoolean(writer, arg.getId(), arg.getLabel(), arg.getDescription(), (Boolean) value, (Boolean) arg.getDefault(), arg.getUrl(), input);
		} else if ((value instanceof Integer) || (value instanceof Double) || (value instanceof String)) {
			write(writer, arg.getId(), arg.getLabel(), arg.getDescription(), value, arg.getDefault(), arg.getMin(), arg.getMax(), arg.getUrl(), input);
		} else {
			throw new IllegalArgumentException("Param with id " + arg.getId() + " is of illegal class " + value.getClass().getName() + "!");
		}
	}
	private static void write(Writer writer, Arg<?, ?> arg, boolean input) throws IOException {
		write(writer, arg.getValue(), arg, input);
	}

	private static <T> void writeTooltip(Writer writer, String id, String description, T defaultValue) throws IOException {
		writer.write("\t\t\t\t<span class=\"tooltip\" tabindex=\"0\"></span>\n");
		writer.write("\t\t\t\t<div class=\"tooltip-box\" tabindex=\"0\">\n");
		writer.write("\t\t\t\t\t<span class=\"description\">");
		writer.write(description);
		writer.write("</span>");
		if (defaultValue != null) {
			writer.write("<br><span class=\"def\">Default:</span> <span class=\"value\">");
			if (!defaultValue.toString().isEmpty()) {
				writer.write(defaultValue.toString());
			} else {
				writer.write("&lt;empty string&gt;");
			}
			writer.write("</span>");
		}
		writer.write("<br><span class=\"def\">API key:</span> <span class=\"value\">");
		writer.write(id);
		writer.write("</span>\n");
		writer.write("\t\t\t\t</div>\n");
	}

	private static <T> void write(Writer writer, String id, String label, String description, T value, T defaultValue, T min, T max, String url, boolean input) throws IOException {
		writer.write("\t\t<div class=\"param");
		if (!input) {
			writer.write(" param-disabled");
		}
		writer.write("\">\n");
		writer.write("\t\t\t<label for=\"" + id + "\">");
		writer.write(PubFetcher.getLinkHtml(url, label));
		writer.write("</label>\n");
		writer.write("\t\t\t<div>\n");
		writer.write("\t\t\t\t<input");
		if (value instanceof Integer || value instanceof Double) {
			writer.write(" type=\"number\"");
		} else {
			writer.write(" type=\"text\"");
		}
		writer.write(" id=\"" + id + "\"");
		if (input) {
			writer.write(" name=\"" + id + "\"");
		}
		if (value instanceof Integer) {
			if (min != null) {
				writer.write(" min=\"" + min + "\"");
			}
			if (max != null) {
				writer.write(" max=\"" + max + "\"");
			}
			writer.write(" step=\"1\"");
		}
		if (value instanceof Double) {
			if (min != null) {
				writer.write(" min=\"" + min + "\"");
			}
			if (max != null) {
				writer.write(" max=\"" + max + "\"");
			}
			writer.write(" step=\"any\"");
		}
		writer.write(" value=\"" + PubFetcher.escapeHtmlAttribute(value.toString()) + "\"");
		if (input) {
			if (defaultValue != null) {
				writer.write(" data-default=\"" + PubFetcher.escapeHtmlAttribute(defaultValue.toString()) + "\"");
			}
			writer.write(" onchange=\"param()\"");
		} else {
			writer.write(" readonly");
		}
		writer.write(">\n");
		writeTooltip(writer, id, description, defaultValue);
		writer.write("\t\t\t</div>\n");
		writer.write("\t\t</div>\n");
	}

	private static void writeBoolean(Writer writer, String id, String label, String description, Boolean value, Boolean defaultValue, String url, boolean input) throws IOException {
		writer.write("\t\t<div class=\"param");
		if (!input) {
			writer.write(" param-disabled");
		}
		writer.write("\">\n");
		writer.write("\t\t\t<span class=\"label\">");
		writer.write(PubFetcher.getLinkHtml(url, label));
		writer.write("</span>\n");
		writer.write("\t\t\t<div>\n");
		if (input) {
			writer.write("\t\t\t\t<input type=\"hidden\" id=\"" + id + "-false\" name=\"" + id + "\" value=\"false\">\n");
		}
		writer.write("\t\t\t\t<input type=\"checkbox\" id=\"" + id + "\"");
		if (input) {
			writer.write(" name=\"" + id + "\"");
		}
		writer.write(" value=\"true\"");
		if (value) {
			if (input) {
				writer.write(" checked");
			} else {
				writer.write(" class=\"checked\"");
			}
		}
		if (input) {
			if (defaultValue != null) {
				writer.write(" data-default=\"" + defaultValue + "\"");
			}
			writer.write(" onchange=\"param()\"");
		} else {
			writer.write(" disabled");
		}
		writer.write(">\n");
		writer.write("\t\t\t\t<label for=\"" + id + "\"></label>\n");
		writeTooltip(writer, id, description, defaultValue);
		writer.write("\t\t\t</div>\n");
		writer.write("\t\t</div>\n");
	}

	@SuppressWarnings("unchecked")
	private static <T, E extends Enum<E>> void writeEnum(boolean multiple, Writer writer, String id, String label, String description, Class<E> enumClass, T value, T defaultValue, String url) throws IOException {
		writer.write("\t\t<div class=\"param");
		if (multiple) {
			writer.write(" param-multiple");
		}
		writer.write("\">\n");
		writer.write("\t\t\t<label for=\"" + id + "\">");
		writer.write(PubFetcher.getLinkHtml(url, label));
		writer.write("</label>\n");
		writer.write("\t\t\t<div>\n");
		writer.write("\t\t\t\t<select");
		writer.write(" id=\"" + id + "\" name=\"" + id + "\"");
		if (multiple) {
			writer.write(" multiple");
		}
		writer.write(" onchange=\"param()\"");
		writer.write(">\n");
		for (Enum<E> enumValue : enumClass.getEnumConstants()) {
			writer.write("\t\t\t\t\t<option");
			if (multiple) {
				if (((List<Enum<E>>) value).contains(enumValue)) {
					writer.write(" selected");
				}
				if (defaultValue != null) {
					if (((List<Enum<E>>) defaultValue).contains(enumValue)) {
						writer.write(" data-default=\"selected\"");
					}
				}
			} else {
				if ((Enum<E>) value == enumValue) {
					writer.write(" selected");
				}
				if (defaultValue != null) {
					if ((Enum<E>) defaultValue == enumValue) {
						writer.write(" data-default=\"selected\"");
					}
				}
			}
			writer.write(">" + enumValue.name() + "</option>\n");
		}
		writer.write("\t\t\t\t</select>\n");
		writeTooltip(writer, id, description, defaultValue);
		writer.write("\t\t\t</div>\n");
		writer.write("\t\t</div>\n");
	}

	private static void writeOutput(Writer writer, String id, String label, String value) throws IOException {
		writeOutput(writer, id, label, value, null);
	}

	private static void writeOutput(Writer writer, String id, String label, int value) throws IOException {
		writeOutput(writer, id, label, Integer.toString(value), null);
	}

	private static void writeOutput(Writer writer, Test test, Results results) throws IOException {
		writeOutput(writer, test.name(), test.getName(), results.toStringTest(test), test.getUrl());
	}

	private static void writeOutput(Writer writer, Measure measure, Results results) throws IOException {
		writeOutput(writer, measure.name(), measure.getName(), results.toStringMeasure(measure), measure.getUrl());
	}

	private static void writeOutput(Writer writer, String id, String label, String value, String url) throws IOException {
		writer.write("\t\t<div class=\"param\">\n");
		writer.write("\t\t\t<label for=\"" + id + "\" class=\"param-disabled\">");
		writer.write(PubFetcher.getLinkHtml(url, label));
		writer.write("</label>\n");
		writer.write("\t\t\t<output id=\"" + id + "\">" + PubFetcher.escapeHtml(value) + "</output>\n");
		writer.write("\t\t</div>\n");
	}

	private static void writeBegin(Writer writer, String id, String title, boolean checked) throws IOException {
		writer.write("<section class=\"tab\">\n");
		writer.write("\t<input type=\"radio\" id=\"tab-title-" + id + "\" name=\"tab-group\"" + (checked ? " checked" : "") + ">\n");
		writer.write("\t<label for=\"tab-title-" + id + "\">" + title + "</label>\n");
		writer.write("\t<div class=\"tab-content\">\n");
	}

	private static void writeEnd(Writer writer) throws IOException {
		writer.write("\t</div>\n");
		writer.write("</section>\n\n");
	}

	private static void writeArgs(Args args, Writer writer, boolean input) throws IOException {
		writeBegin(writer, args.getId(), args.getLabel(), false);
		for (Arg<?, ?> arg : args.getArgs()) {
			write(writer, arg, input);
		}
		writeEnd(writer);
	}

	private static <T, E extends Enum<E>> void writeArg(String id, T value, Class<E> enumClass, JsonGenerator generator) throws IOException {
		if (enumClass != null) {
			if (value instanceof List) {
				generator.writeObjectField(id, value);
			} else {
				generator.writeObjectField(id, value);
			}
		} else if (value instanceof Boolean) {
			generator.writeBooleanField(id, (Boolean) value);
		} else if (value instanceof Integer) {
			generator.writeNumberField(id, (Integer) value);
		} else if (value instanceof Double) {
			generator.writeNumberField(id, (Double) value);
		} else if (value instanceof String){
			generator.writeStringField(id, (String) value);
		} else {
			throw new IllegalArgumentException("Param with id " + id + " is of illegal class " + value.getClass().getName() + "!");
		}
	}
	private static void writeArgs(Args args, JsonGenerator generator) throws IOException {
		generator.writeFieldName(args.getId());
		generator.writeStartObject();
		for (Arg<?, ?> arg : args.getArgs()) {
			writeArg(arg.getId(), arg.getValue(), arg.getEnumClass(), generator);
		}
		generator.writeEndObject();
	}

	public static void writeProcessing(ProcessorArgs args, Writer writer) throws IOException {
		writeArgs(args, writer, false);
	}
	public static void writeProcessing(ProcessorArgs args, JsonGenerator generator) throws IOException {
		writeArgs(args, generator);
	}

	public static void writePreProcessing(PreProcessorArgs args, Writer writer, boolean input) throws IOException {
		writeArgs(args, writer, input);
	}
	public static void writePreProcessing(PreProcessorArgs args, JsonGenerator generator) throws IOException {
		writeArgs(args, generator);
	}

	public static void writeFetching(FetcherArgs args, Writer writer, boolean includePrivate, boolean input) throws IOException {
		writeBegin(writer, args.getId(), args.getLabel(), false);
		for (Arg<?, ?> arg : args.getArgs()) {
			write(writer, arg, input);
		}
		if (includePrivate) {
			for (Arg<?, ?> arg : args.getPrivateArgs().getArgs()) {
				write(writer, arg, input);
			}
		}
		writeEnd(writer);
	}
	public static void writeFetching(FetcherArgs args, boolean includePrivate, JsonGenerator generator) throws IOException {
		generator.writeFieldName(args.getId());
		generator.writeStartObject();
		for (Arg<?, ?> arg : args.getArgs()) {
			writeArg(arg.getId(), arg.getValue(), arg.getEnumClass(), generator);
		}
		if (includePrivate) {
			writeArgs(args.getPrivateArgs(), generator);
		}
		generator.writeEndObject();
	}

	public static void writeMapping(MapperArgs args, Writer writer, boolean input) throws IOException {
		writeArgs(args, writer, input);
		writeArgs(args.getAlgorithmArgs(), writer, input);
		writeArgs(args.getIdfArgs(), writer, input);
		writeArgs(args.getMultiplierArgs(), writer, input);
		writeArgs(args.getNormaliserArgs(), writer, input);
		writeArgs(args.getWeightArgs(), writer, input);
		writeArgs(args.getScoreArgs(), writer, input);
	}
	public static void writeMapping(MapperArgs args, JsonGenerator generator) throws IOException {
		generator.writeFieldName(args.getId());
		generator.writeStartObject();
		for (Arg<?, ?> arg : args.getArgs()) {
			writeArg(arg.getId(), arg.getValue(), arg.getEnumClass(), generator);
		}
		writeArgs(args.getAlgorithmArgs(), generator);
		writeArgs(args.getIdfArgs(), generator);
		writeArgs(args.getMultiplierArgs(), generator);
		writeArgs(args.getNormaliserArgs(), generator);
		writeArgs(args.getWeightArgs(), generator);
		writeArgs(args.getScoreArgs(), generator);
		generator.writeEndObject();
	}

	private static void writeCountsEdam(Writer writer, Map<EdamUri, Concept> concepts) throws IOException {
		writeOutput(writer, CONCEPTS_SIZE_ID, CONCEPTS_SIZE_LABEL, concepts.size());
		Map<Branch, Integer> branchCounts = Edam.branchCounts(concepts);
		writeOutput(writer, TOPIC_SIZE_ID, TOPIC_SIZE_LABEL, branchCounts.get(Branch.topic).toString());
		writeOutput(writer, OPERATION_SIZE_ID, OPERATION_SIZE_LABEL, branchCounts.get(Branch.operation).toString());
		writeOutput(writer, DATA_SIZE_ID, DATA_SIZE_LABEL, branchCounts.get(Branch.data).toString());
		writeOutput(writer, FORMAT_SIZE_ID, FORMAT_SIZE_LABEL, branchCounts.get(Branch.format).toString());
	}
	private static void writeCountsEdam(Map<EdamUri, Concept> concepts, JsonGenerator generator) throws IOException {
		generator.writeNumberField(CONCEPTS_SIZE_ID, concepts.size());
		Map<Branch, Integer> branchCounts = Edam.branchCounts(concepts);
		generator.writeNumberField(TOPIC_SIZE_ID, branchCounts.get(Branch.topic));
		generator.writeNumberField(OPERATION_SIZE_ID, branchCounts.get(Branch.operation));
		generator.writeNumberField(DATA_SIZE_ID, branchCounts.get(Branch.data));
		generator.writeNumberField(FORMAT_SIZE_ID, branchCounts.get(Branch.format));
	}

	public static void writeCountsEdamOnly(Writer writer, Map<EdamUri, Concept> concepts) throws IOException {
		writeBegin(writer, COUNTS_ID, COUNTS_LABEL, false);
		writeCountsEdam(writer, concepts);
		writeEnd(writer);
	}

	public static void writeBenchmarking(Writer writer, Map<EdamUri, Concept> concepts, List<Query> queries, Results results) throws IOException {
		writeBegin(writer, COUNTS_ID, COUNTS_LABEL, false);
		writeCountsEdam(writer, concepts);
		writeOutput(writer, QUERIES_SIZE_ID, QUERIES_SIZE_LABEL, queries.size());
		writeOutput(writer, RESULTS_SIZE_ID, RESULTS_SIZE_LABEL, results.getMappings().size());
		writeOutput(writer, Test.tp, results);
		writeOutput(writer, Test.fp, results);
		writeOutput(writer, Test.fn, results);
		writeEnd(writer);

		writeBegin(writer, MEASURES_ID, MEASURES_LABEL, false);
		writeOutput(writer, Measure.precision, results);
		writeOutput(writer, Measure.recall, results);
		writeOutput(writer, Measure.f1, results);
		writeOutput(writer, Measure.f2, results);
		writeOutput(writer, Measure.Jaccard, results);
		writeOutput(writer, Measure.AveP, results);
		writeOutput(writer, Measure.RP, results);
		writeOutput(writer, Measure.DCG, results);
		writeOutput(writer, Measure.DCGa, results);
		writeEnd(writer);
	}

	private static void writeTest(Results results, Test test, JsonGenerator generator) throws IOException {
		generator.writeFieldName(test.name());
		generator.writeStartObject();
		for (Branch branch : Branch.values()) {
			generator.writeNumberField(branch.name(), results.getMeasures(branch).getTest(test));
		}
		generator.writeNumberField(TOTAL_ID, results.getMeasuresTotal().getTest(test));
		generator.writeEndObject();
	}
	private static void writeMeasure(Results results, Measure measure, JsonGenerator generator) throws IOException {
		generator.writeFieldName(measure.name());
		generator.writeStartObject();
		for (Branch branch : Branch.values()) {
			generator.writeNumberField(branch.name(), results.getMeasures(branch).getMeasure(measure));
		}
		generator.writeNumberField(TOTAL_ID, results.getMeasuresTotal().getMeasure(measure));
		generator.writeEndObject();
	}

	public static void writeBenchmarking(Map<EdamUri, Concept> concepts, List<Query> queries, Results results, JsonGenerator generator) throws IOException {
		generator.writeFieldName(COUNTS_ID);
		generator.writeStartObject();
		writeCountsEdam(concepts, generator);
		generator.writeNumberField(QUERIES_SIZE_ID, queries.size());
		generator.writeNumberField(RESULTS_SIZE_ID, results.getMappings().size());
		writeTest(results, Test.tp, generator);
		writeTest(results, Test.fp, generator);
		writeTest(results, Test.fn, generator);
		generator.writeEndObject();

		generator.writeFieldName(MEASURES_ID);
		generator.writeStartObject();
		writeMeasure(results, Measure.precision, generator);
		writeMeasure(results, Measure.recall, generator);
		writeMeasure(results, Measure.f1, generator);
		writeMeasure(results, Measure.f2, generator);
		writeMeasure(results, Measure.Jaccard, generator);
		writeMeasure(results, Measure.AveP, generator);
		writeMeasure(results, Measure.RP, generator);
		writeMeasure(results, Measure.DCG, generator);
		writeMeasure(results, Measure.DCGa, generator);
		generator.writeEndObject();
	}

	public static void writeMain(List<ArgMain> argsMain, Writer writer) throws IOException {
		writeBegin(writer, MAIN_ARGS_ID, MAIN_ARGS_LABEL, true);
		for (ArgMain argMain : argsMain) {
			write(writer, argMain.getValue(), argMain.getArg(), argMain.isInput());
		}
		writeEnd(writer);
	}
	public static void writeMain(List<ArgMain> argsMain, JsonGenerator generator) throws IOException {
		generator.writeFieldName(MAIN_ARGS_ID);
		generator.writeStartObject();
		for (ArgMain argMain : argsMain) {
			writeArg(argMain.getArg().getId(), argMain.getValue(), argMain.getArg().getEnumClass(), generator);
		}
		generator.writeEndObject();
	}
}
