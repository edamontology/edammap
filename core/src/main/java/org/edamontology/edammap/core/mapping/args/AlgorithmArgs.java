/*
 * Copyright © 2016, 2017 Erik Jaaniso
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

package org.edamontology.edammap.core.mapping.args;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

import org.edamontology.edammap.core.args.PositiveDouble;
import org.edamontology.edammap.core.args.ZeroToOneDouble;
import org.edamontology.edammap.core.mapping.MapperStrategy;

public class AlgorithmArgs {

	public static final String COMPOUND_WORDS = "compoundWords";
	@Parameter(names = { "--" + COMPOUND_WORDS }, validateWith = PositiveInteger.class, description = "Try to match words that have accidentally been made compound (given number is maximum number of words in an accidental compound minus one)")
	private int compoundWords = 0;

	public static final String MISMATCH_MULTIPLIER = "mismatchMultiplier";
	@Parameter(names = { "--" + MISMATCH_MULTIPLIER }, validateWith = PositiveDouble.class, description = "Multiplier for score decrease caused by mismatch")
	private double mismatchMultiplier = 2.0;

	public static final String MATCH_MINIMUM = "matchMinimum";
	@Parameter(names = { "--" + MATCH_MINIMUM }, validateWith = ZeroToOneDouble.class, description = "Minimum score allowed for approximate match. Set to 1 to disable approximate matching.")
	private double matchMinimum = 1.0;

	public static final String POSITION_OFF_BY_1 = "positionOffBy1";
	@Parameter(names = { "--" + POSITION_OFF_BY_1 }, validateWith = ZeroToOneDouble.class, description = "Multiplier of a position score component for the case when a word is inserted between matched words or matched words are switched")
	private double positionOffBy1 = 0.35;

	public static final String POSITION_OFF_BY_2 = "positionOffBy2";
	@Parameter(names = { "--" + POSITION_OFF_BY_2 }, validateWith = ZeroToOneDouble.class, description = "Multiplier of a position score component for the case when two words are inserted between matched words or matched words are switched with an additional word between them")
	private double positionOffBy2 = 0.05;

	public static final String POSITION_MATCH_SCALING = "positionMatchScaling";
	@Parameter(names = { "--" + POSITION_MATCH_SCALING }, validateWith = PositiveDouble.class, description = "Set to 0 to not have match score of neighbor influence position score. Setting to 1 means linear influence.")
	private double positionMatchScaling = 0.5;

	public static final String POSITION_LOSS = "positionLoss";
	@Parameter(names = { "--" + POSITION_LOSS }, validateWith = ZeroToOneDouble.class, description = "Maximum loss caused by wrong positions of matched words")
	private double positionLoss = 0.4;

	public static final String SCORE_SCALING = "scoreScaling";
	@Parameter(names = { "--" + SCORE_SCALING }, validateWith = PositiveDouble.class, description = "Score is scaled before applying multiplier and weighting with other direction match. Setting to 0 or 1 means no scaling.")
	private double scoreScaling = 0.2;

	public static final String CONCEPT_WEIGHT = "conceptWeight";
	@Parameter(names = { "--" + CONCEPT_WEIGHT }, validateWith = PositiveDouble.class, description = "Weight of matching a concept (with a query). Set to 0 to disable matching of concepts.")
	private double conceptWeight = 1.0;

	public static final String QUERY_WEIGHT = "queryWeight";
	@Parameter(names = { "--" + QUERY_WEIGHT }, validateWith = PositiveDouble.class, description = "Weight of matching a query (with a concept). Set to 0 to disable matching of queries.")
	private double queryWeight = 1.0;

	public static final String MAPPING_STRATEGY = "mappingStrategy";
	@Parameter(names = { "--" + MAPPING_STRATEGY }, description = "Choose the best or take the average of query parts matches")
	private MapperStrategy mappingStrategy = MapperStrategy.average;

	public static final String PARENT_WEIGHT = "parentWeight";
	@Parameter(names = { "--" + PARENT_WEIGHT }, validateWith = PositiveDouble.class, description = "Weight of concept's parent when computing path enrichment. Weight of grand-parent is parent-weight times parent-weight, etc. Set to 0 to disable path enrichment.")
	private double parentWeight = 0.5;

	public static final String PATH_WEIGHT = "pathWeight";
	@Parameter(names = { "--" + PATH_WEIGHT }, validateWith = PositiveDouble.class, description = "Weight of path enrichment. Weight of concept is 1. Set to 0 to disable path enrichment.")
	private double pathWeight = 0.7;

	public int getCompoundWords() {
		return compoundWords;
	}
	public void setCompoundWords(int compoundWords) {
		this.compoundWords = compoundWords;
	}

	public double getMismatchMultiplier() {
		return mismatchMultiplier;
	}
	public void setMismatchMultiplier(double mismatchMultiplier) {
		this.mismatchMultiplier = mismatchMultiplier;
	}

	public double getMatchMinimum() {
		return matchMinimum;
	}
	public void setMatchMinimum(double matchMinimum) {
		this.matchMinimum = matchMinimum;
	}

	public double getPositionOffBy1() {
		return positionOffBy1;
	}
	public void setPositionOffBy1(double positionOffBy1) {
		this.positionOffBy1 = positionOffBy1;
	}

	public double getPositionOffBy2() {
		return positionOffBy2;
	}
	public void setPositionOffBy2(double positionOffBy2) {
		this.positionOffBy2 = positionOffBy2;
	}

	public double getPositionMatchScaling() {
		return positionMatchScaling;
	}
	public void setPositionMatchScaling(double positionMatchScaling) {
		this.positionMatchScaling = positionMatchScaling;
	}

	public double getPositionLoss() {
		return positionLoss;
	}
	public void setPositionLoss(double positionLoss) {
		this.positionLoss = positionLoss;
	}

	public double getScoreScaling() {
		return scoreScaling;
	}
	public void setScoreScaling(double scoreScaling) {
		this.scoreScaling = scoreScaling;
	}

	public double getConceptWeight() {
		return conceptWeight;
	}
	public void setConceptWeight(double conceptWeight) {
		this.conceptWeight = conceptWeight;
	}

	public double getQueryWeight() {
		return queryWeight;
	}
	public void setQueryWeight(double queryWeight) {
		this.queryWeight = queryWeight;
	}

	public MapperStrategy getMappingStrategy() {
		return mappingStrategy;
	}
	public void setMappingStrategy(MapperStrategy mappingStrategy) {
		this.mappingStrategy = mappingStrategy;
	}

	public double getParentWeight() {
		return parentWeight;
	}
	public void setParentWeight(double parentWeight) {
		this.parentWeight = parentWeight;
	}

	public double getPathWeight() {
		return pathWeight;
	}
	public void setPathWeight(double pathWeight) {
		this.pathWeight = pathWeight;
	}
}
