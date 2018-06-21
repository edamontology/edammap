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

package org.edamontology.edammap.server;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.edamontology.edammap.core.args.CoreArgs;
import org.edamontology.edammap.core.edam.Branch;
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
import org.edamontology.pubfetcher.core.common.FetcherArgs;

public final class ParamParse {

	private static final Logger logger = LogManager.getLogger();

	static Boolean getParamBoolean(MultivaluedMap<String, String> params, String key) {
		List<String> values = params.get(key);
		if (values != null && values.size() > 0) {
			if (values.get(values.size() - 1).isEmpty()) {
				return true;
			} else {
				return Boolean.valueOf(values.get(values.size() - 1));
			}
		} else {
			return null;
		}
	}

	static String getParamString(MultivaluedMap<String, String> params, String key) {
		List<String> values = params.get(key);
		if (values != null && values.size() > 0) {
			return values.get(values.size() - 1);
		} else {
			return null;
		}
	}
	static String getParamStrings(MultivaluedMap<String, String> params, String key) {
		List<String> values = params.get(key);
		if (values != null && values.size() > 0) {
			return String.join("\n", values);
		} else {
			return null;
		}
	}

	private static Integer getParamInteger(MultivaluedMap<String, String> params, String key, Integer min, Integer max, boolean json) {
		List<String> values = params.get(key);
		if (values != null && values.size() > 0 && !values.get(values.size() - 1).isEmpty()) {
			Integer value;
			try {
				value = Integer.valueOf(values.get(values.size() - 1));
			} catch (NumberFormatException e) {
				throw new ParamException(key, values.get(values.size() - 1), "has wrong number format", json);
			}
			if (min != null && value < min) {
				throw new ParamException(key, value.toString(), "is below limit " + min, json);
			}
			if (max != null && value > max) {
				throw new ParamException(key, value.toString(), "is above limit " + max, json);
			}
			return value;
		} else {
			return null;
		}
	}

	private static Double getParamDouble(MultivaluedMap<String, String> params, String key, Double min, Double max, boolean json) {
		List<String> values = params.get(key);
		if (values != null && values.size() > 0 && !values.get(values.size() - 1).isEmpty()) {
			Double value;
			try {
				value = Double.valueOf(values.get(values.size() - 1));
			} catch (NumberFormatException e) {
				throw new ParamException(key, values.get(values.size() - 1), "has wrong number format", json);
			}
			if (min != null && value < min) {
				throw new ParamException(key, value.toString(), "is below limit " + min, json);
			}
			if (max != null && value > max) {
				throw new ParamException(key, value.toString(), "is above limit " + max, json);
			}
			return value;
		} else {
			return null;
		}
	}

	static <E extends Enum<E>> E getParamEnum(MultivaluedMap<String, String> params, String key, Class<E> enumClass, boolean json) {
		List<String> values = params.get(key);
		if (values != null && values.size() > 0 && !values.get(values.size() - 1).isEmpty()) {
			try {
				return Enum.valueOf(enumClass, values.get(values.size() - 1));
			} catch (IllegalArgumentException e) {
				throw new ParamException(key, values.get(values.size() - 1), "has invalid value", json);
			}
		} else {
			return null;
		}
	}

	private static <E extends Enum<E>> List<E> getParamEnums(MultivaluedMap<String, String> params, String key, Class<E> enumClass, boolean json) {
		List<String> values = params.get(key);
		if (values != null && values.size() > 0 && !values.get(values.size() - 1).isEmpty()) {
			List<E> enums = new ArrayList<>();
			for (String value : values) {
				try {
					enums.add(Enum.valueOf(enumClass, value));
				} catch (IllegalArgumentException e) {
					throw new ParamException(key, value, "has invalid value", json);
				}
			}
			return enums;
		} else {
			return null;
		}
	}

	static void parseFetcherParams(MultivaluedMap<String, String> params, FetcherArgs fetcherArgs, boolean json) {
		if (params == null || params.isEmpty()) {
			if (params == null) {
				logger.warn("No params to parse");
			}
			return;
		}

		Integer valueInteger = null;

		if ((valueInteger = getParamInteger(params, FetcherArgs.EMPTY_COOLDOWN, 0, null, json)) != null) {
			fetcherArgs.setEmptyCooldown(valueInteger);
		}
		if ((valueInteger = getParamInteger(params, FetcherArgs.NON_FINAL_COOLDOWN, 0, null, json)) != null) {
			fetcherArgs.setNonFinalCooldown(valueInteger);
		}
		if ((valueInteger = getParamInteger(params, FetcherArgs.FETCH_EXCEPTION_COOLDOWN, 0, null, json)) != null) {
			fetcherArgs.setFetchExceptionCooldown(valueInteger);
		}
		if ((valueInteger = getParamInteger(params, FetcherArgs.RETRY_LIMIT, null, null, json)) != null) {
			fetcherArgs.setRetryLimit(valueInteger);
		}
		if ((valueInteger = getParamInteger(params, FetcherArgs.TITLE_MIN_LENGTH, 0, null, json)) != null) {
			fetcherArgs.setTitleMinLength(valueInteger);
		}
		if ((valueInteger = getParamInteger(params, FetcherArgs.KEYWORDS_MIN_SIZE, 0, null, json)) != null) {
			fetcherArgs.setKeywordsMinSize(valueInteger);
		}
		if ((valueInteger = getParamInteger(params, FetcherArgs.MINED_TERMS_MIN_SIZE, 0, null, json)) != null) {
			fetcherArgs.setMinedTermsMinSize(valueInteger);
		}
		if ((valueInteger = getParamInteger(params, FetcherArgs.ABSTRACT_MIN_LENGTH, 0, null, json)) != null) {
			fetcherArgs.setAbstractMinLength(valueInteger);
		}
		if ((valueInteger = getParamInteger(params, FetcherArgs.FULLTEXT_MIN_LENGTH, 0, null, json)) != null) {
			fetcherArgs.setFulltextMinLength(valueInteger);
		}
		if ((valueInteger = getParamInteger(params, FetcherArgs.WEBPAGE_MIN_LENGTH, 0, null, json)) != null) {
			fetcherArgs.setWebpageMinLength(valueInteger);
		}
		if ((valueInteger = getParamInteger(params, FetcherArgs.WEBPAGE_MIN_LENGTH_JAVASCRIPT, 0, null, json)) != null) {
			fetcherArgs.setWebpageMinLengthJavascript(valueInteger);
		}
		if ((valueInteger = getParamInteger(params, FetcherArgs.TIMEOUT, 0, null, json)) != null) {
			fetcherArgs.setTimeout(valueInteger);
		}
	}

	static void parseParams(MultivaluedMap<String, String> params, CoreArgs args, boolean json) {
		if (params == null || params.isEmpty()) {
			if (params == null) {
				logger.warn("No params to parse");
			}
			return;
		}

		Boolean valueBoolean = null;
		Integer valueInteger = null;
		Double valueDouble = null;
		Enum<?> valueEnum = null;

		if ((valueBoolean = getParamBoolean(params, PreProcessorArgs.NUMBERS)) != null) {
			args.getPreProcessorArgs().setNumbers(valueBoolean);
		}
		if ((valueEnum = getParamEnum(params, PreProcessorArgs.STOPWORDS, Stopwords.class, json)) != null) {
			args.getPreProcessorArgs().setStopwords((Stopwords) valueEnum);
		}
		if ((valueBoolean = getParamBoolean(params, PreProcessorArgs.STEMMING)) != null) {
			args.getPreProcessorArgs().setStemming(valueBoolean);
		}
		if ((valueInteger = getParamInteger(params, PreProcessorArgs.MIN_LENGTH, 0, null, json)) != null) {
			args.getPreProcessorArgs().setMinLength(valueInteger);
		}

		parseFetcherParams(params, args.getFetcherArgs(), json);

		List<Branch> branches = getParamEnums(params, MapperArgs.BRANCHES, Branch.class, json);
		if (branches != null) {
			args.getMapperArgs().setBranches(branches);
		}
		if ((valueInteger = getParamInteger(params, MapperArgs.MATCHES, 0, null, json)) != null) {
			args.getMapperArgs().setMatches(valueInteger);
		}
		if ((valueBoolean = getParamBoolean(params, MapperArgs.OBSOLETE)) != null) {
			args.getMapperArgs().setObsolete(valueBoolean);
		}
		if ((valueBoolean = getParamBoolean(params, MapperArgs.DONE_ANNOTATIONS)) != null) {
			args.getMapperArgs().setDoneAnnotations(valueBoolean);
		}
		if ((valueBoolean = getParamBoolean(params, MapperArgs.INFERIOR_PARENTS_CHILDREN)) != null) {
			args.getMapperArgs().setInferiorParentsChildren(valueBoolean);
		}

		if ((valueInteger = getParamInteger(params, AlgorithmArgs.COMPOUND_WORDS, 0, null, json)) != null) {
			args.getMapperArgs().getAlgorithmArgs().setCompoundWords(valueInteger);
		}
		if ((valueDouble = getParamDouble(params, AlgorithmArgs.MISMATCH_MULTIPLIER, 0.0, null, json)) != null) {
			args.getMapperArgs().getAlgorithmArgs().setMismatchMultiplier(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, AlgorithmArgs.MATCH_MINIMUM, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getAlgorithmArgs().setMatchMinimum(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, AlgorithmArgs.POSITION_OFF_BY_1, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getAlgorithmArgs().setPositionOffBy1(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, AlgorithmArgs.POSITION_OFF_BY_2, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getAlgorithmArgs().setPositionOffBy2(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, AlgorithmArgs.POSITION_MATCH_SCALING, 0.0, null, json)) != null) {
			args.getMapperArgs().getAlgorithmArgs().setPositionMatchScaling(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, AlgorithmArgs.POSITION_LOSS, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getAlgorithmArgs().setPositionLoss(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, AlgorithmArgs.SCORE_SCALING, 0.0, null, json)) != null) {
			args.getMapperArgs().getAlgorithmArgs().setScoreScaling(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, AlgorithmArgs.CONCEPT_WEIGHT, 0.0, null, json)) != null) {
			args.getMapperArgs().getAlgorithmArgs().setConceptWeight(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, AlgorithmArgs.QUERY_WEIGHT, 0.0, null, json)) != null) {
			args.getMapperArgs().getAlgorithmArgs().setQueryWeight(valueDouble);
		}
		if ((valueEnum = getParamEnum(params, AlgorithmArgs.MAPPING_STRATEGY, MapperStrategy.class, json)) != null) {
			args.getMapperArgs().getAlgorithmArgs().setMappingStrategy((MapperStrategy) valueEnum);
		}
		if ((valueDouble = getParamDouble(params, AlgorithmArgs.PARENT_WEIGHT, 0.0, null, json)) != null) {
			args.getMapperArgs().getAlgorithmArgs().setParentWeight(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, AlgorithmArgs.PATH_WEIGHT, 0.0, null, json)) != null) {
			args.getMapperArgs().getAlgorithmArgs().setPathWeight(valueDouble);
		}

		if ((valueDouble = getParamDouble(params, IdfArgs.CONCEPT_IDF_SCALING, 0.0, null, json)) != null) {
			args.getMapperArgs().getIdfArgs().setConceptIdfScaling(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, IdfArgs.QUERY_IDF_SCALING, 0.0, null, json)) != null) {
			args.getMapperArgs().getIdfArgs().setQueryIdfScaling(valueDouble);
		}
		if ((valueBoolean = getParamBoolean(params, IdfArgs.LABEL_SYNONYMS_IDF)) != null) {
			args.getMapperArgs().getIdfArgs().setLabelSynonymsIdf(valueBoolean);
		}
		if ((valueBoolean = getParamBoolean(params, IdfArgs.NAME_KEYWORDS_IDF)) != null) {
			args.getMapperArgs().getIdfArgs().setNameKeywordsIdf(valueBoolean);
		}
		if ((valueBoolean = getParamBoolean(params, IdfArgs.DESCRIPTION_IDF)) != null) {
			args.getMapperArgs().getIdfArgs().setDescriptionIdf(valueBoolean);
		}
		if ((valueBoolean = getParamBoolean(params, IdfArgs.TITLE_KEYWORDS_IDF)) != null) {
			args.getMapperArgs().getIdfArgs().setTitleKeywordsIdf(valueBoolean);
		}
		if ((valueBoolean = getParamBoolean(params, IdfArgs.ABSTRACT_IDF)) != null) {
			args.getMapperArgs().getIdfArgs().setAbstractIdf(valueBoolean);
		}

		if ((valueDouble = getParamDouble(params, MultiplierArgs.LABEL_MULTIPLIER, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getMultiplierArgs().setLabelMultiplier(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, MultiplierArgs.EXACT_SYNONYM_MULTIPLIER, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getMultiplierArgs().setExactSynonymMultiplier(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, MultiplierArgs.NARROW_BROAD_SYNONYM_MULTIPLIER, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getMultiplierArgs().setNarrowBroadSynonymMultiplier(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, MultiplierArgs.DEFINITION_MULTIPLIER, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getMultiplierArgs().setDefinitionMultiplier(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, MultiplierArgs.COMMENT_MULTIPLIER, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getMultiplierArgs().setCommentMultiplier(valueDouble);
		}

		if ((valueDouble = getParamDouble(params, NormaliserArgs.NAME_NORMALISER, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getNormaliserArgs().setNameNormaliser(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, NormaliserArgs.KEYWORD_NORMALISER, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getNormaliserArgs().setKeywordNormaliser(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, NormaliserArgs.DESCRIPTION_NORMALISER, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getNormaliserArgs().setDescriptionNormaliser(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, NormaliserArgs.PUBLICATION_TITLE_NORMALISER, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getNormaliserArgs().setPublicationTitleNormaliser(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, NormaliserArgs.PUBLICATION_KEYWORD_NORMALISER, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getNormaliserArgs().setPublicationKeywordNormaliser(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, NormaliserArgs.PUBLICATION_MESH_NORMALISER, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getNormaliserArgs().setPublicationMeshNormaliser(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, NormaliserArgs.PUBLICATION_MINED_TERM_NORMALISER, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getNormaliserArgs().setPublicationMinedTermNormaliser(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, NormaliserArgs.PUBLICATION_ABSTRACT_NORMALISER, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getNormaliserArgs().setPublicationAbstractNormaliser(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, NormaliserArgs.PUBLICATION_FULLTEXT_NORMALISER, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getNormaliserArgs().setPublicationFulltextNormaliser(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, NormaliserArgs.DOC_NORMALISER, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getNormaliserArgs().setDocNormaliser(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, NormaliserArgs.WEBPAGE_NORMALISER, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getNormaliserArgs().setWebpageNormaliser(valueDouble);
		}

		if ((valueDouble = getParamDouble(params, WeightArgs.AVERAGE_SCALING, 0.0, null, json)) != null) {
			args.getMapperArgs().getWeightArgs().setAverageScaling(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, WeightArgs.NAME_WEIGHT, 0.0, null, json)) != null) {
			args.getMapperArgs().getWeightArgs().setNameWeight(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, WeightArgs.KEYWORD_WEIGHT, 0.0, null, json)) != null) {
			args.getMapperArgs().getWeightArgs().setKeywordWeight(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, WeightArgs.DESCRIPTION_WEIGHT, 0.0, null, json)) != null) {
			args.getMapperArgs().getWeightArgs().setDescriptionWeight(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, WeightArgs.PUBLICATION_TITLE_WEIGHT, 0.0, null, json)) != null) {
			args.getMapperArgs().getWeightArgs().setPublicationTitleWeight(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, WeightArgs.PUBLICATION_KEYWORD_WEIGHT, 0.0, null, json)) != null) {
			args.getMapperArgs().getWeightArgs().setPublicationKeywordWeight(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, WeightArgs.PUBLICATION_MESH_WEIGHT, 0.0, null, json)) != null) {
			args.getMapperArgs().getWeightArgs().setPublicationMeshWeight(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, WeightArgs.PUBLICATION_MINED_TERM_WEIGHT, 0.0, null, json)) != null) {
			args.getMapperArgs().getWeightArgs().setPublicationMinedTermWeight(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, WeightArgs.PUBLICATION_ABSTRACT_WEIGHT, 0.0, null, json)) != null) {
			args.getMapperArgs().getWeightArgs().setPublicationAbstractWeight(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, WeightArgs.PUBLICATION_FULLTEXT_WEIGHT, 0.0, null, json)) != null) {
			args.getMapperArgs().getWeightArgs().setPublicationFulltextWeight(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, WeightArgs.DOC_WEIGHT, 0.0, null, json)) != null) {
			args.getMapperArgs().getWeightArgs().setDocWeight(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, WeightArgs.WEBPAGE_WEIGHT, 0.0, null, json)) != null) {
			args.getMapperArgs().getWeightArgs().setWebpageWeight(valueDouble);
		}

		if ((valueDouble = getParamDouble(params, ScoreArgs.GOOD_SCORE_TOPIC, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getScoreArgs().setGoodScoreTopic(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, ScoreArgs.GOOD_SCORE_OPERATION, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getScoreArgs().setGoodScoreOperation(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, ScoreArgs.GOOD_SCORE_DATA, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getScoreArgs().setGoodScoreData(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, ScoreArgs.GOOD_SCORE_FORMAT, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getScoreArgs().setGoodScoreFormat(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, ScoreArgs.BAD_SCORE_TOPIC, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getScoreArgs().setBadScoreTopic(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, ScoreArgs.BAD_SCORE_OPERATION, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getScoreArgs().setBadScoreOperation(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, ScoreArgs.BAD_SCORE_DATA, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getScoreArgs().setBadScoreData(valueDouble);
		}
		if ((valueDouble = getParamDouble(params, ScoreArgs.BAD_SCORE_FORMAT, 0.0, 1.0, json)) != null) {
			args.getMapperArgs().getScoreArgs().setBadScoreFormat(valueDouble);
		}
		if ((valueBoolean = getParamBoolean(params, ScoreArgs.OUTPUT_GOOD_SCORES)) != null) {
			args.getMapperArgs().getScoreArgs().setOutputGoodScores(valueBoolean);
		}
		if ((valueBoolean = getParamBoolean(params, ScoreArgs.OUTPUT_MEDIUM_SCORES)) != null) {
			args.getMapperArgs().getScoreArgs().setOutputMediumScores(valueBoolean);
		}
		if ((valueBoolean = getParamBoolean(params, ScoreArgs.OUTPUT_BAD_SCORES)) != null) {
			args.getMapperArgs().getScoreArgs().setOutputBadScores(valueBoolean);
		}
	}
}
