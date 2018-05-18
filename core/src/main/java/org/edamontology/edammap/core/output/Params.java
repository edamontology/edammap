/*
 * Copyright © 2018 Erik Jaaniso
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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.edamontology.edammap.core.args.CoreArgs;
import org.edamontology.edammap.core.benchmarking.Measure;
import org.edamontology.edammap.core.benchmarking.Results;
import org.edamontology.edammap.core.benchmarking.Test;
import org.edamontology.edammap.core.edam.Branch;
import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.Edam;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.mapping.MapperStrategy;
import org.edamontology.edammap.core.mapping.args.AlgorithmArgs;
import org.edamontology.edammap.core.mapping.args.IdfArgs;
import org.edamontology.edammap.core.mapping.args.MapperArgs;
import org.edamontology.edammap.core.mapping.args.MultiplierArgs;
import org.edamontology.edammap.core.mapping.args.NormaliserArgs;
import org.edamontology.edammap.core.mapping.args.ScoreArgs;
import org.edamontology.edammap.core.mapping.args.WeightArgs;
import org.edamontology.edammap.core.preprocessing.PreProcessorArgs;
import org.edamontology.edammap.core.preprocessing.Stopwords;
import org.edamontology.edammap.core.processing.ProcessorArgs;
import org.edamontology.edammap.core.query.Query;
import org.edamontology.pubfetcher.FetcherArgs;
import org.edamontology.pubfetcher.FetcherCommon;
import org.edamontology.pubfetcher.FetcherPrivateArgs;

import com.fasterxml.jackson.core.JsonGenerator;

public class Params {

	private static final String MAIN_ARGS_ID = "mainArgs";

	private static final String COUNTS_ID = "counts";
	private static final String MEASURES_ID = "measures";

	private static final String CONCEPTS_SIZE_ID = "conceptsSize";
	private static final String TOPIC_SIZE_ID = "topicSize";
	private static final String OPERATION_SIZE_ID = "operationSize";
	private static final String DATA_SIZE_ID = "dataSize";
	private static final String FORMAT_SIZE_ID = "formatSize";
	private static final String QUERIES_SIZE_ID = "queriesSize";
	private static final String RESULTS_SIZE_ID = "resultsSize";

	private static void write(Writer writer, Param param, boolean input) throws IOException {
		if (param.getValue() instanceof Boolean) {
			writeBoolean(writer, param, input);
			return;
		} else if (!(param.getValue() instanceof String) && !(param.getValue() instanceof Integer) && !(param.getValue() instanceof Double)) {
			throw new IllegalArgumentException("Param with id " + param.getId() + " is of illegal class " + param.getValue().getClass().getName() + "!");
		}
		writer.write("\t\t<div class=\"param\">\n");
		writer.write("\t\t\t<label for=\"" + param.getId() + "\">");
		writer.write(FetcherCommon.getLinkHtml(param.getUrl(), param.getLabel()));
		writer.write("</label>\n");
		writer.write("\t\t\t<input");
		if (param.getValue() instanceof Integer || param.getValue() instanceof Double) {
			writer.write(" type=\"number\"");
		} else {
			writer.write(" type=\"text\"");
		}
		writer.write(" id=\"" + param.getId() + "\"");
		if (input) {
			writer.write(" name=\"" + param.getId() + "\"");
		}
		if (param.getValue() instanceof Integer) {
			if (param.getMin() != null) {
				writer.write(" min=\"" + param.getMin().intValue() + "\"");
			}
			if (param.getMax() != null) {
				writer.write(" max=\"" + param.getMax().intValue() + "\"");
			}
			writer.write(" step=\"1\"");
		}
		if (param.getValue() instanceof Double) {
			if (param.getMin() != null) {
				writer.write(" min=\"" + param.getMin() + "\"");
			}
			if (param.getMax() != null) {
				writer.write(" max=\"" + param.getMax() + "\"");
			}
			writer.write(" step=\"any\"");
		}
		writer.write(" value=\"" + FetcherCommon.escapeHtmlAttribute(param.getValue().toString()) + "\"");
		if (!input) {
			writer.write(" readonly");
		}
		writer.write(">\n");
		writer.write("\t\t</div>\n");
	}

	private static void writeBoolean(Writer writer, Param param, boolean input) throws IOException {
		if (!(param.getValue() instanceof Boolean)) {
			throw new IllegalArgumentException("Param with id " + param.getId() + " is not boolean, but " + param.getValue().getClass().getName() + "!");
		}
		writer.write("\t\t<div class=\"param\">\n");
		writer.write("\t\t\t<span>");
		writer.write(FetcherCommon.getLinkHtml(param.getUrl(), param.getLabel()));
		writer.write("</span>\n");
		writer.write("\t\t\t<div>\n");
		if (input) {
			writer.write("\t\t\t\t<input type=\"hidden\" id=\"" + param.getId() + "-false\" name=\"" + param.getId() + "\" value=\"false\">\n");
		}
		writer.write("\t\t\t\t<input type=\"checkbox\" id=\"" + param.getId() + "\"");
		if (input) {
			writer.write(" name=\"" + param.getId() + "\"");
		}
		writer.write(" value=\"true\"");
		if ((Boolean) param.getValue()) {
			if (input) {
				writer.write(" checked");
			} else {
				writer.write(" class=\"checked\"");
			}
		}
		if (!input) {
			writer.write(" disabled");
		}
		writer.write(">\n");
		writer.write("\t\t\t\t<label for=\"" + param.getId() + "\"></label>\n");
		writer.write("\t\t\t</div>\n");
		writer.write("\t\t</div>\n");
	}

	private static <E extends Enum<E>> void writeEnum(Writer writer, String label, String id, Class<E> enumClass, E enumValue, String url, boolean input) throws IOException {
		if (!input) {
			write(writer, new Param(label, id, enumValue.toString(), url), input);
		} else {
			writeEnum(false, writer, label, id, enumClass, Collections.singletonList(enumValue), url);
		}
	}

	private static <E extends Enum<E>> void writeEnum(Writer writer, String label, String id, Class<E> enumClass, List<E> enumValues, String url, boolean input) throws IOException {
		if (!input) {
			write(writer, new Param(label, id, enumValues.toString(), url), input);
		} else {
			writeEnum(true, writer, label, id, enumClass, enumValues, url);
		}
	}

	private static <E extends Enum<E>> void writeEnum(boolean multiple, Writer writer, String label, String id, Class<E> enumClass, List<E> enumValues, String url) throws IOException {
		writer.write("\t\t<div class=\"param");
		if (multiple) {
			writer.write(" param-multiple");
		}
		writer.write("\">\n");
		writer.write("\t\t\t<label for=\"" + id + "\">");
		writer.write(FetcherCommon.getLinkHtml(url, label));
		writer.write("</label>\n");
		writer.write("\t\t\t<select");
		writer.write(" id=\"" + id + "\" name=\"" + id + "\"");
		if (multiple) {
			writer.write(" multiple");
		}
		writer.write(">\n");
		for (Enum<E> enumValue : enumClass.getEnumConstants()) {
			writer.write("\t\t\t\t<option");
			if (enumValues.contains(enumValue)) {
				writer.write(" selected");
			}
			writer.write(">" + enumValue.name() + "</option>\n");
		}
		writer.write("\t\t\t</select>\n");
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
		writer.write("\t\t\t<label for=\"" + id + "\">");
		writer.write(FetcherCommon.getLinkHtml(url, label));
		writer.write("</label>\n");
		writer.write("\t\t\t<output id=\"" + id + "\">" + FetcherCommon.escapeHtml(value) + "</output>\n");
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

	public static void writeProcessing(ProcessorArgs args, Writer writer) throws IOException {
		writeBegin(writer, CoreArgs.PROCESSOR_ARGS, "Processing", false);
		writeBoolean(writer, new Param("Fetching", ProcessorArgs.FETCHING, args.isFetching()), false);
		write(writer, new Param("Database file", ProcessorArgs.DB, new File(args.getDb()).getName()), false);
		write(writer, new Param("Query IDF file", ProcessorArgs.IDF, new File(args.getIdf()).getName()), false);
		write(writer, new Param("Stemmed query IDF file", ProcessorArgs.IDF_STEMMED, new File(args.getIdfStemmed()).getName()), false);
		writeEnd(writer);
	}

	public static void writePreProcessing(PreProcessorArgs args, Writer writer, boolean input) throws IOException {
		writeBegin(writer, CoreArgs.PRE_PROCESSOR_ARGS, "Preprocessing", false);
		writeBoolean(writer, new Param("Freestanding numbers", PreProcessorArgs.NUMBERS, args.isNumbers()), input);
		writeEnum(writer, "Stopword list", PreProcessorArgs.STOPWORDS, Stopwords.class, args.getStopwords(), null, input);
		writeBoolean(writer, new Param("Stemming", PreProcessorArgs.STEMMING, args.isStemming()), input);
		write(writer, new Param("Remove shorter than", PreProcessorArgs.MIN_LENGTH, args.getMinLength(), 0.0, null), input);
		writeEnd(writer);
	}

	public static void writeFetching(FetcherArgs args, Writer writer, boolean includePrivate, boolean input) throws IOException {
		writeBegin(writer, CoreArgs.FETCHER_ARGS, "Fetching", false);
		write(writer, new Param("Empty cooldown", FetcherArgs.EMPTY_COOLDOWN, args.getEmptyCooldown(), 0.0, null), input);
		write(writer, new Param("Non-final cooldown", FetcherArgs.NON_FINAL_COOLDOWN, args.getNonFinalCooldown(), 0.0, null), input);
		write(writer, new Param("Fetching exception cooldown", FetcherArgs.FETCH_EXCEPTION_COOLDOWN, args.getFetchExceptionCooldown(), 0.0, null), input);
		write(writer, new Param("Retry limit", FetcherArgs.RETRY_LIMIT, args.getRetryLimit()), input);
		write(writer, new Param("Title min. length", FetcherArgs.TITLE_MIN_LENGTH, args.getTitleMinLength(), 0.0, null), input);
		write(writer, new Param("Keywords min. size", FetcherArgs.KEYWORDS_MIN_SIZE, args.getKeywordsMinSize(), 0.0, null), input);
		write(writer, new Param("Mined terms min. size", FetcherArgs.MINED_TERMS_MIN_SIZE, args.getMinedTermsMinSize(), 0.0, null), input);
		write(writer, new Param("Abstract min. length", FetcherArgs.ABSTRACT_MIN_LENGTH, args.getAbstractMinLength(), 0.0, null), input);
		write(writer, new Param("Fulltext min. length", FetcherArgs.FULLTEXT_MIN_LENGTH, args.getFulltextMinLength(), 0.0, null), input);
		write(writer, new Param("Webpage min. length", FetcherArgs.WEBPAGE_MIN_LENGTH, args.getWebpageMinLength(), 0.0, null), input);
		write(writer, new Param("Webpage min. length JS", FetcherArgs.WEBPAGE_MIN_LENGTH_JAVASCRIPT, args.getWebpageMinLengthJavascript(), 0.0, null), input);
		write(writer, new Param("Timeout", FetcherArgs.TIMEOUT, args.getTimeout(), 0.0, null), input);
		if (includePrivate) {
			write(writer, new Param("Europe PMC e-mail", FetcherPrivateArgs.EUROPEPMC_EMAIL, args.getPrivateArgs().getEuropepmcEmail()), input);
			write(writer, new Param("oaDOI e-mail", FetcherPrivateArgs.OADOI_EMAIL, args.getPrivateArgs().getOadoiEmail()), input);
			write(writer, new Param("User Agent", FetcherPrivateArgs.USER_AGENT, args.getPrivateArgs().getUserAgent()), input);
		}
		writeEnd(writer);
	}
	public static void writeFetching(FetcherArgs args, boolean includePrivate, JsonGenerator generator) throws IOException {
		generator.writeFieldName(CoreArgs.FETCHER_ARGS);
		if (includePrivate) {
			generator.writeObject(args);
		} else {
			generator.writeStartObject();
			generator.writeNumberField(FetcherArgs.EMPTY_COOLDOWN, args.getEmptyCooldown());
			generator.writeNumberField(FetcherArgs.NON_FINAL_COOLDOWN, args.getNonFinalCooldown());
			generator.writeNumberField(FetcherArgs.FETCH_EXCEPTION_COOLDOWN, args.getFetchExceptionCooldown());
			generator.writeNumberField(FetcherArgs.RETRY_LIMIT, args.getRetryLimit());
			generator.writeNumberField(FetcherArgs.TITLE_MIN_LENGTH, args.getTitleMinLength());
			generator.writeNumberField(FetcherArgs.KEYWORDS_MIN_SIZE, args.getKeywordsMinSize());
			generator.writeNumberField(FetcherArgs.MINED_TERMS_MIN_SIZE, args.getMinedTermsMinSize());
			generator.writeNumberField(FetcherArgs.ABSTRACT_MIN_LENGTH, args.getAbstractMinLength());
			generator.writeNumberField(FetcherArgs.FULLTEXT_MIN_LENGTH, args.getFulltextMinLength());
			generator.writeNumberField(FetcherArgs.WEBPAGE_MIN_LENGTH, args.getWebpageMinLength());
			generator.writeNumberField(FetcherArgs.WEBPAGE_MIN_LENGTH_JAVASCRIPT, args.getWebpageMinLengthJavascript());
			generator.writeNumberField(FetcherArgs.TIMEOUT, args.getTimeout());
			generator.writeEndObject();
		}
	}

	public static void writeMapping(MapperArgs args, Writer writer, boolean input) throws IOException {
		writeBegin(writer, CoreArgs.MAPPER_ARGS, "Mapping", false);
		writeEnum(writer, "Branches", MapperArgs.BRANCHES, Branch.class, args.getBranches(), "http://edamontology.org/page#Scope", input);
		write(writer, new Param("Top matches per branch", MapperArgs.MATCHES, args.getMatches(), 0.0, null), input);
		writeBoolean(writer, new Param("Obsolete concepts", MapperArgs.OBSOLETE, args.isObsolete()), input);
		writeBoolean(writer, new Param("Done annotations", MapperArgs.DONE_ANNOTATIONS, args.isDoneAnnotations()), input);
		writeBoolean(writer, new Param("Inferior parents & children", MapperArgs.INFERIOR_PARENTS_CHILDREN, args.isInferiorParentsChildren()), input);
		writeBoolean(writer, new Param("Top level concepts", MapperArgs.TOP_LEVEL, args.isTopLevel()), input);
		writeEnd(writer);

		writeBegin(writer, MapperArgs.ALGORITHM_ARGS, "Mapping algorithm", false);
		write(writer, new Param("Compound words", AlgorithmArgs.COMPOUND_WORDS, args.getAlgorithmArgs().getCompoundWords(), 0.0, null), input);
		write(writer, new Param("Mismatch multiplier", AlgorithmArgs.MISMATCH_MULTIPLIER, args.getAlgorithmArgs().getMismatchMultiplier(), 0.0, null), input);
		write(writer, new Param("Match minimum", AlgorithmArgs.MATCH_MINIMUM, args.getAlgorithmArgs().getMatchMinimum(), 0.0, 1.0), input);
		write(writer, new Param("Position off by 1", AlgorithmArgs.POSITION_OFF_BY_1, args.getAlgorithmArgs().getPositionOffBy1(), 0.0, 1.0), input);
		write(writer, new Param("Position off by 2", AlgorithmArgs.POSITION_OFF_BY_2, args.getAlgorithmArgs().getPositionOffBy2(), 0.0, 1.0), input);
		write(writer, new Param("Position match scaling", AlgorithmArgs.POSITION_MATCH_SCALING, args.getAlgorithmArgs().getPositionMatchScaling(), 0.0, null), input);
		write(writer, new Param("Position loss", AlgorithmArgs.POSITION_LOSS, args.getAlgorithmArgs().getPositionLoss(), 0.0, 1.0), input);
		write(writer, new Param("Score scaling", AlgorithmArgs.SCORE_SCALING, args.getAlgorithmArgs().getScoreScaling(), 0.0, null), input);
		write(writer, new Param("Concept weight", AlgorithmArgs.CONCEPT_WEIGHT, args.getAlgorithmArgs().getConceptWeight(), 0.0, null), input);
		write(writer, new Param("Query weight", AlgorithmArgs.QUERY_WEIGHT, args.getAlgorithmArgs().getQueryWeight(), 0.0, null), input);
		writeEnum(writer, "Mapping strategy", AlgorithmArgs.MAPPING_STRATEGY, MapperStrategy.class, args.getAlgorithmArgs().getMappingStrategy(), null, input);
		write(writer, new Param("Parent weight", AlgorithmArgs.PARENT_WEIGHT, args.getAlgorithmArgs().getParentWeight(), 0.0, null), input);
		write(writer, new Param("Path weight", AlgorithmArgs.PATH_WEIGHT, args.getAlgorithmArgs().getPathWeight(), 0.0, null), input);
		writeEnd(writer);

		writeBegin(writer, MapperArgs.IDF_ARGS, "IDF", false);
		write(writer, new Param("Concept IDF scaling", IdfArgs.CONCEPT_IDF_SCALING, args.getIdfArgs().getConceptIdfScaling(), 0.0, null), input);
		write(writer, new Param("Query IDF scaling", IdfArgs.QUERY_IDF_SCALING, args.getIdfArgs().getQueryIdfScaling(), 0.0, null), input);
		writeBoolean(writer, new Param("Label/Synonyms IDF", IdfArgs.LABEL_SYNONYMS_IDF, args.getIdfArgs().isLabelSynonymsIdf()), input);
		writeBoolean(writer, new Param("Name/Keywords IDF", IdfArgs.NAME_KEYWORDS_IDF, args.getIdfArgs().isNameKeywordsIdf()), input);
		writeBoolean(writer, new Param("Description IDF", IdfArgs.DESCRIPTION_IDF, args.getIdfArgs().isDescriptionIdf()), input);
		writeBoolean(writer, new Param("Title/Keywords IDF", IdfArgs.TITLE_KEYWORDS_IDF, args.getIdfArgs().isTitleKeywordsIdf()), input);
		writeBoolean(writer, new Param("Abstract IDF", IdfArgs.ABSTRACT_IDF, args.getIdfArgs().isAbstractIdf()), input);
		writeEnd(writer);

		writeBegin(writer, MapperArgs.MULTIPLIER_ARGS, "Concept multipliers", false);
		write(writer, new Param("Label multiplier", MultiplierArgs.LABEL_MULTIPLIER, args.getMultiplierArgs().getLabelMultiplier(), 0.0, 1.0), input);
		write(writer, new Param("Exact synonym multiplier", MultiplierArgs.EXACT_SYNONYM_MULTIPLIER, args.getMultiplierArgs().getExactSynonymMultiplier(), 0.0, 1.0), input);
		write(writer, new Param("Narrow/Broad multiplier", MultiplierArgs.NARROW_BROAD_SYNONYM_MULTIPLIER, args.getMultiplierArgs().getNarrowBroadSynonymMultiplier(), 0.0, 1.0), input);
		write(writer, new Param("Definition multiplier", MultiplierArgs.DEFINITION_MULTIPLIER, args.getMultiplierArgs().getDefinitionMultiplier(), 0.0, 1.0), input);
		write(writer, new Param("Comment multiplier", MultiplierArgs.COMMENT_MULTIPLIER, args.getMultiplierArgs().getCommentMultiplier(), 0.0, 1.0), input);
		writeEnd(writer);

		writeBegin(writer, MapperArgs.NORMALISER_ARGS, "Query normalisers", false);
		write(writer, new Param("Name norm.", NormaliserArgs.NAME_NORMALISER, args.getNormaliserArgs().getNameNormaliser(), 0.0, 1.0), input);
		write(writer, new Param("Keyword norm.", NormaliserArgs.KEYWORD_NORMALISER, args.getNormaliserArgs().getKeywordNormaliser(), 0.0, 1.0), input);
		write(writer, new Param("Description norm.", NormaliserArgs.DESCRIPTION_NORMALISER, args.getNormaliserArgs().getDescriptionNormaliser(), 0.0, 1.0), input);
		write(writer, new Param("Publication title norm.", NormaliserArgs.PUBLICATION_TITLE_NORMALISER, args.getNormaliserArgs().getPublicationTitleNormaliser(), 0.0, 1.0), input);
		write(writer, new Param("Publication keyword norm.", NormaliserArgs.PUBLICATION_KEYWORD_NORMALISER, args.getNormaliserArgs().getPublicationKeywordNormaliser(), 0.0, 1.0), input);
		write(writer, new Param("Publication MeSH norm.", NormaliserArgs.PUBLICATION_MESH_NORMALISER, args.getNormaliserArgs().getPublicationMeshNormaliser(), 0.0, 1.0), input);
		write(writer, new Param("Publication EFO/GO norm.", NormaliserArgs.PUBLICATION_MINED_TERM_NORMALISER, args.getNormaliserArgs().getPublicationMinedTermNormaliser(), 0.0, 1.0), input);
		write(writer, new Param("Publication abstract norm.", NormaliserArgs.PUBLICATION_ABSTRACT_NORMALISER, args.getNormaliserArgs().getPublicationAbstractNormaliser(), 0.0, 1.0), input);
		write(writer, new Param("Publication fulltext norm.", NormaliserArgs.PUBLICATION_FULLTEXT_NORMALISER, args.getNormaliserArgs().getPublicationFulltextNormaliser(), 0.0, 1.0), input);
		write(writer, new Param("Doc norm.", NormaliserArgs.DOC_NORMALISER, args.getNormaliserArgs().getDocNormaliser(), 0.0, 1.0), input);
		write(writer, new Param("Webpage norm.", NormaliserArgs.WEBPAGE_NORMALISER, args.getNormaliserArgs().getWebpageNormaliser(), 0.0, 1.0), input);
		writeEnd(writer);

		writeBegin(writer, MapperArgs.WEIGHT_ARGS, "Query weights", false);
		write(writer, new Param("Average strategy scaling", WeightArgs.AVERAGE_SCALING, args.getWeightArgs().getAverageScaling(), 0.0, null), input);
		write(writer, new Param("Name weight", WeightArgs.NAME_WEIGHT, args.getWeightArgs().getNameWeight(), 0.0, null), input);
		write(writer, new Param("Keyword weight", WeightArgs.KEYWORD_WEIGHT, args.getWeightArgs().getKeywordWeight(), 0.0, null), input);
		write(writer, new Param("Description weight", WeightArgs.DESCRIPTION_WEIGHT, args.getWeightArgs().getDescriptionWeight(), 0.0, null), input);
		write(writer, new Param("Publication title weight", WeightArgs.PUBLICATION_TITLE_WEIGHT, args.getWeightArgs().getPublicationTitleWeight(), 0.0, null), input);
		write(writer, new Param("Publication keyword weight", WeightArgs.PUBLICATION_KEYWORD_WEIGHT, args.getWeightArgs().getPublicationKeywordWeight(), 0.0, null), input);
		write(writer, new Param("Publication MeSH weight", WeightArgs.PUBLICATION_MESH_WEIGHT, args.getWeightArgs().getPublicationMeshWeight(), 0.0, null), input);
		write(writer, new Param("Publication EFO/GO weight", WeightArgs.PUBLICATION_MINED_TERM_WEIGHT, args.getWeightArgs().getPublicationMinedTermWeight(), 0.0, null), input);
		write(writer, new Param("Publication abstract weight", WeightArgs.PUBLICATION_ABSTRACT_WEIGHT, args.getWeightArgs().getPublicationAbstractWeight(), 0.0, null), input);
		write(writer, new Param("Publication fulltext weight", WeightArgs.PUBLICATION_FULLTEXT_WEIGHT, args.getWeightArgs().getPublicationFulltextWeight(), 0.0, null), input);
		write(writer, new Param("Doc weight", WeightArgs.DOC_WEIGHT, args.getWeightArgs().getDocWeight(), 0.0, null), input);
		write(writer, new Param("Webpage weight", WeightArgs.WEBPAGE_WEIGHT, args.getWeightArgs().getWebpageWeight(), 0.0, null), input);
		writeEnd(writer);

		writeBegin(writer, MapperArgs.SCORE_ARGS, "Score limits", false);
		write(writer, new Param("Good score for topic", ScoreArgs.GOOD_SCORE_TOPIC, args.getScoreArgs().getGoodScoreTopic(), 0.0, 1.0), input);
		write(writer, new Param("Good score for operation", ScoreArgs.GOOD_SCORE_OPERATION, args.getScoreArgs().getGoodScoreOperation(), 0.0, 1.0), input);
		write(writer, new Param("Good score for data", ScoreArgs.GOOD_SCORE_DATA, args.getScoreArgs().getGoodScoreData(), 0.0, 1.0), input);
		write(writer, new Param("Good score for format", ScoreArgs.GOOD_SCORE_FORMAT, args.getScoreArgs().getGoodScoreFormat(), 0.0, 1.0), input);
		write(writer, new Param("Bad score for topic", ScoreArgs.BAD_SCORE_TOPIC, args.getScoreArgs().getBadScoreTopic(), 0.0, 1.0), input);
		write(writer, new Param("Bad score for operation", ScoreArgs.BAD_SCORE_OPERATION, args.getScoreArgs().getBadScoreOperation(), 0.0, 1.0), input);
		write(writer, new Param("Bad score for data", ScoreArgs.BAD_SCORE_DATA, args.getScoreArgs().getBadScoreData(), 0.0, 1.0), input);
		write(writer, new Param("Bad score for format", ScoreArgs.BAD_SCORE_FORMAT, args.getScoreArgs().getBadScoreFormat(), 0.0, 1.0), input);
		writeBoolean(writer, new Param("Matches with good scores", ScoreArgs.OUTPUT_GOOD_SCORES, args.getScoreArgs().isOutputGoodScores()), input);
		writeBoolean(writer, new Param("Matches with medium scores", ScoreArgs.OUTPUT_MEDIUM_SCORES, args.getScoreArgs().isOutputMediumScores()), input);
		writeBoolean(writer, new Param("Matches with bad scores", ScoreArgs.OUTPUT_BAD_SCORES, args.getScoreArgs().isOutputBadScores()), input);
		writeEnd(writer);
	}

	private static void writeCountsEdam(Writer writer, Map<EdamUri, Concept> concepts) throws IOException {
		writeOutput(writer, CONCEPTS_SIZE_ID, "EDAM concepts", concepts.size());
		Map<Branch, Integer> branchCounts = Edam.branchCounts(concepts);
		writeOutput(writer, TOPIC_SIZE_ID, "Topic terms", branchCounts.get(Branch.topic).toString());
		writeOutput(writer, OPERATION_SIZE_ID, "Operation terms", branchCounts.get(Branch.operation).toString());
		writeOutput(writer, DATA_SIZE_ID, "Data terms", branchCounts.get(Branch.data).toString());
		writeOutput(writer, FORMAT_SIZE_ID, "Format terms", branchCounts.get(Branch.format).toString());
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
		writeBegin(writer, COUNTS_ID, "Counts", false);
		writeCountsEdam(writer, concepts);
		writeEnd(writer);
	}

	public static void writeBenchmarking(Writer writer, Map<EdamUri, Concept> concepts, List<Query> queries, Results results) throws IOException {
		writeBegin(writer, COUNTS_ID, "Counts", false);
		writeCountsEdam(writer, concepts);
		writeOutput(writer, QUERIES_SIZE_ID, "Queries", queries.size());
		writeOutput(writer, RESULTS_SIZE_ID, "Results", results.getMappings().size());
		writeOutput(writer, Test.tp, results);
		writeOutput(writer, Test.fp, results);
		writeOutput(writer, Test.fn, results);
		writeEnd(writer);

		writeBegin(writer, MEASURES_ID, "Measures", false);
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
		generator.writeNumberField("total", results.getMeasuresTotal().getTest(test));
		generator.writeEndObject();
	}
	private static void writeMeasure(Results results, Measure measure, JsonGenerator generator) throws IOException {
		generator.writeFieldName(measure.name());
		generator.writeStartObject();
		for (Branch branch : Branch.values()) {
			generator.writeNumberField(branch.name(), results.getMeasures(branch).getMeasure(measure));
		}
		generator.writeNumberField("total", results.getMeasuresTotal().getMeasure(measure));
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

	public static void writeMain(List<ParamMain> paramsMain, Writer writer) throws IOException {
		writeBegin(writer, MAIN_ARGS_ID, "Main", true);
		for (ParamMain param : paramsMain) {
			write(writer, param, param.getInput());
		}
		writeEnd(writer);
	}
	public static void writeMain(List<ParamMain> paramsMain, JsonGenerator generator) throws IOException {
		generator.writeFieldName(MAIN_ARGS_ID);
		generator.writeStartObject();
		for (Param param : paramsMain) {
			if (param.getValue() instanceof Boolean) {
				generator.writeBooleanField(param.getId(), (Boolean) param.getValue());
			} else if (param.getValue() instanceof Integer) {
				generator.writeNumberField(param.getId(), (Integer) param.getValue());
			} else if (param.getValue() instanceof Double) {
				generator.writeNumberField(param.getId(), (Double) param.getValue());
			} else if (param.getValue() instanceof String){
				generator.writeStringField(param.getId(), (String) param.getValue());
			} else {
				throw new IllegalArgumentException("Param with id " + param.getId() + " is of illegal class " + param.getValue().getClass().getName() + "!");
			}
		}
		generator.writeEndObject();
	}
}
