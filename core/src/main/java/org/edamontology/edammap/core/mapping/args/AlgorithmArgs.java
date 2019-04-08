/*
 * Copyright © 2016, 2017, 2019 Erik Jaaniso
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

import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.Args;

public class AlgorithmArgs extends Args {

	private static final String compoundWordsId = "compoundWords";
	private static final String compoundWordsDescription = "Try to match words that have accidentally been made compound (given number is maximum number of words in an accidental compound minus one)";
	private static final Integer compoundWordsDefault = 0;
	@Parameter(names = { "--" + compoundWordsId }, validateWith = PositiveInteger.class, description = compoundWordsDescription)
	private Integer compoundWords = compoundWordsDefault;

	private static final String mismatchMultiplierId = "mismatchMultiplier";
	private static final String mismatchMultiplierDescription = "Multiplier for score decrease caused by mismatch";
	private static final Double mismatchMultiplierDefault = 2.0;
	@Parameter(names = { "--" + mismatchMultiplierId }, validateWith = PositiveDouble.class, description = mismatchMultiplierDescription)
	private Double mismatchMultiplier = mismatchMultiplierDefault;

	private static final String matchMinimumId = "matchMinimum";
	private static final String matchMinimumDescription = "Minimum score allowed for approximate match. Set to 1 to disable approximate matching.";
	private static final Double matchMinimumDefault = 1.0;
	@Parameter(names = { "--" + matchMinimumId }, validateWith = ZeroToOneDouble.class, description = matchMinimumDescription)
	private Double matchMinimum = matchMinimumDefault;

	private static final String positionOffBy1Id = "positionOffBy1";
	private static final String positionOffBy1Description = "Multiplier of a position score component for the case when a word is inserted between matched words or matched words are switched";
	private static final Double positionOffBy1Default = 0.35;
	@Parameter(names = { "--" + positionOffBy1Id }, validateWith = ZeroToOneDouble.class, description = positionOffBy1Description)
	private Double positionOffBy1 = positionOffBy1Default;

	private static final String positionOffBy2Id = "positionOffBy2";
	private static final String positionOffBy2Description = "Multiplier of a position score component for the case when two words are inserted between matched words or matched words are switched with an additional word between them";
	private static final Double positionOffBy2Default = 0.05;
	@Parameter(names = { "--" + positionOffBy2Id }, validateWith = ZeroToOneDouble.class, description = positionOffBy2Description)
	private Double positionOffBy2 = positionOffBy2Default;

	private static final String positionMatchScalingId = "positionMatchScaling";
	private static final String positionMatchScalingDescription = "Set to 0 to not have match score of neighbor influence position score. Setting to 1 means linear influence.";
	private static final Double positionMatchScalingDefault = 0.5;
	@Parameter(names = { "--" + positionMatchScalingId }, validateWith = PositiveDouble.class, description = positionMatchScalingDescription)
	private Double positionMatchScaling = positionMatchScalingDefault;

	private static final String positionLossId = "positionLoss";
	private static final String positionLossDescription = "Maximum loss caused by wrong positions of matched words";
	private static final Double positionLossDefault = 0.4;
	@Parameter(names = { "--" + positionLossId }, validateWith = ZeroToOneDouble.class, description = positionLossDescription)
	private Double positionLoss = positionLossDefault;

	private static final String scoreScalingId = "scoreScaling";
	private static final String scoreScalingDescription = "Score is scaled before applying multiplier and weighting with other direction match. Setting to 0 or 1 means no scaling.";
	private static final Double scoreScalingDefault = 0.2;
	@Parameter(names = { "--" + scoreScalingId }, validateWith = PositiveDouble.class, description = scoreScalingDescription)
	private Double scoreScaling = scoreScalingDefault;

	private static final String conceptWeightId = "conceptWeight";
	private static final String conceptWeightDescription = "Weight of matching a concept (with a query). Set to 0 to disable matching of concepts.";
	private static final Double conceptWeightDefault = 1.0;
	@Parameter(names = { "--" + conceptWeightId }, validateWith = PositiveDouble.class, description = conceptWeightDescription)
	private Double conceptWeight = conceptWeightDefault;

	private static final String queryWeightId = "queryWeight";
	private static final String queryWeightDescription = "Weight of matching a query (with a concept). Set to 0 to disable matching of queries.";
	private static final Double queryWeightDefault = 1.0;
	@Parameter(names = { "--" + queryWeightId }, validateWith = PositiveDouble.class, description = queryWeightDescription)
	private Double queryWeight = queryWeightDefault;

	private static final String mappingStrategyId = "mappingStrategy";
	private static final String mappingStrategyDescription = "Choose the best or take the average of query parts matches";
	private static final MapperStrategy mappingStrategyDefault = MapperStrategy.average;
	@Parameter(names = { "--" + mappingStrategyId }, description = mappingStrategyDescription)
	private MapperStrategy mappingStrategy = mappingStrategyDefault;

	private static final String parentWeightId = "parentWeight";
	private static final String parentWeightDescription = "Weight of concept's parent when computing path enrichment. Weight of grand-parent is parent-weight times parent-weight, etc. Set to 0 to disable path enrichment.";
	private static final Double parentWeightDefault = 0.5;
	@Parameter(names = { "--" + parentWeightId }, validateWith = PositiveDouble.class, description = parentWeightDescription)
	private Double parentWeight = parentWeightDefault;

	private static final String pathWeightId = "pathWeight";
	private static final String pathWeightDescription = "Weight of path enrichment. Weight of concept is 1. Set to 0 to disable path enrichment.";
	private static final Double pathWeightDefault = 0.7;
	@Parameter(names = { "--" + pathWeightId }, validateWith = PositiveDouble.class, description = pathWeightDescription)
	private Double pathWeight = pathWeightDefault;

	@Override
	protected void addArgs() {
		args.add(new Arg<>(this::getCompoundWords, this::setCompoundWords, compoundWordsDefault, 0, null, compoundWordsId, "Compound words", compoundWordsDescription, null));
		args.add(new Arg<>(this::getMismatchMultiplier, this::setMismatchMultiplier, mismatchMultiplierDefault, 0.0, null, mismatchMultiplierId, "Mismatch multiplier", mismatchMultiplierDescription, null));
		args.add(new Arg<>(this::getMatchMinimum, this::setMatchMinimum, matchMinimumDefault, 0.0, 1.0, matchMinimumId, "Match minimum", matchMinimumDescription, null));
		args.add(new Arg<>(this::getPositionOffBy1, this::setPositionOffBy1, positionOffBy1Default, 0.0, 1.0, positionOffBy1Id, "Position off by 1", positionOffBy1Description, null));
		args.add(new Arg<>(this::getPositionOffBy2, this::setPositionOffBy2, positionOffBy2Default, 0.0, 1.0, positionOffBy2Id, "Position off by 2", positionOffBy2Description, null));
		args.add(new Arg<>(this::getPositionMatchScaling, this::setPositionMatchScaling, positionMatchScalingDefault, 0.0, null, positionMatchScalingId, "Position match scaling", positionMatchScalingDescription, null));
		args.add(new Arg<>(this::getPositionLoss, this::setPositionLoss, positionLossDefault, 0.0, 1.0, positionLossId, "Position loss", positionLossDescription, null));
		args.add(new Arg<>(this::getScoreScaling, this::setScoreScaling, scoreScalingDefault, 0.0, null, scoreScalingId, "Score scaling", scoreScalingDescription, null));
		args.add(new Arg<>(this::getConceptWeight, this::setConceptWeight, conceptWeightDefault, 0.0, null, conceptWeightId, "Concept weight", conceptWeightDescription, null));
		args.add(new Arg<>(this::getQueryWeight, this::setQueryWeight, queryWeightDefault, 0.0, null, queryWeightId, "Query weight", queryWeightDescription, null));
		args.add(new Arg<>(this::getMappingStrategy, this::setMappingStrategy, mappingStrategyDefault, mappingStrategyId, "Mapping strategy", mappingStrategyDescription, MapperStrategy.class));
		args.add(new Arg<>(this::getParentWeight, this::setParentWeight, parentWeightDefault, 0.0, null, parentWeightId, "Parent weight", parentWeightDescription, null));
		args.add(new Arg<>(this::getPathWeight, this::setPathWeight, pathWeightDefault, 0.0, null, pathWeightId, "Path weight", pathWeightDescription, null));
	}

	@Override
	public String getId() {
		return "algorithmArgs";
	}

	@Override
	public String getLabel() {
		return "Mapping algorithm";
	}

	public int getCompoundWords() {
		return compoundWords;
	}
	public void setCompoundWords(Integer compoundWords) {
		this.compoundWords = compoundWords;
	}

	public double getMismatchMultiplier() {
		return mismatchMultiplier;
	}
	public void setMismatchMultiplier(Double mismatchMultiplier) {
		this.mismatchMultiplier = mismatchMultiplier;
	}

	public double getMatchMinimum() {
		return matchMinimum;
	}
	public void setMatchMinimum(Double matchMinimum) {
		this.matchMinimum = matchMinimum;
	}

	public double getPositionOffBy1() {
		return positionOffBy1;
	}
	public void setPositionOffBy1(Double positionOffBy1) {
		this.positionOffBy1 = positionOffBy1;
	}

	public double getPositionOffBy2() {
		return positionOffBy2;
	}
	public void setPositionOffBy2(Double positionOffBy2) {
		this.positionOffBy2 = positionOffBy2;
	}

	public double getPositionMatchScaling() {
		return positionMatchScaling;
	}
	public void setPositionMatchScaling(Double positionMatchScaling) {
		this.positionMatchScaling = positionMatchScaling;
	}

	public double getPositionLoss() {
		return positionLoss;
	}
	public void setPositionLoss(Double positionLoss) {
		this.positionLoss = positionLoss;
	}

	public double getScoreScaling() {
		return scoreScaling;
	}
	public void setScoreScaling(Double scoreScaling) {
		this.scoreScaling = scoreScaling;
	}

	public double getConceptWeight() {
		return conceptWeight;
	}
	public void setConceptWeight(Double conceptWeight) {
		this.conceptWeight = conceptWeight;
	}

	public double getQueryWeight() {
		return queryWeight;
	}
	public void setQueryWeight(Double queryWeight) {
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
	public void setParentWeight(Double parentWeight) {
		this.parentWeight = parentWeight;
	}

	public double getPathWeight() {
		return pathWeight;
	}
	public void setPathWeight(Double pathWeight) {
		this.pathWeight = pathWeight;
	}
}
