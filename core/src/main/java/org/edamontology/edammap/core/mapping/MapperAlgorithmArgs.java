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

package org.edamontology.edammap.core.mapping;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

import org.edamontology.edammap.core.args.PositiveDouble;
import org.edamontology.edammap.core.args.ZeroToOneDouble;

public class MapperAlgorithmArgs {
	@Parameter(names = { "--compound-words" }, validateWith = PositiveInteger.class, description = "Try to match words that have accidentally been made compound (given number is maximum number of words in an accidental compound minus one)")
	private int compoundWords = 0;

	@Parameter(names = { "--mismatch-multiplier" }, validateWith = PositiveDouble.class, description = "Multiplier for score decrease caused by mismatch")
	private double mismatchMultiplier = 2;

	@Parameter(names = { "--match-minimum" }, validateWith = ZeroToOneDouble.class, description = "Minimum score allowed for approximate match. Set to 1 to disable approximate matching.")
	private double matchMinimum = 1;

	@Parameter(names = { "--position-off-by-1" }, validateWith = ZeroToOneDouble.class, description = "Multiplier of a position score component for the case when a word is inserted between matched words or matched words are switched")
	private double positionOffBy1 = 0.35;

	@Parameter(names = { "--position-off-by-2" }, validateWith = ZeroToOneDouble.class, description = "Multiplier of a position score component for the case when two words are inserted between matched words or matched words are switched with an additional word between them")
	private double positionOffBy2 = 0.05;

	@Parameter(names = { "--position-match-scaling" }, validateWith = PositiveDouble.class, description = "Set to 0 to not have match score of neighbor influence position score. Setting to 1 means linear influence.")
	private double positionMatchScaling = 0.5;

	@Parameter(names = { "--position-loss" }, validateWith = ZeroToOneDouble.class, description = "Maximum loss caused by wrong positions of matched words")
	private double positionLoss = 0.4;

	@Parameter(names = { "--score-scaling" }, validateWith = PositiveDouble.class, description = "Score is scaled before appyling multiplier and weighting with other direction match. Setting to 0 or 1 means no scaling.")
	private double scoreScaling = 0.2;

	@Parameter(names = { "--concept-weight" }, validateWith = PositiveDouble.class, description = "Weight of matching a concept (with a query). Set to 0 to disable matching of concepts.")
	private double conceptWeight = 1;

	@Parameter(names = { "--query-weight" }, validateWith = PositiveDouble.class, description = "Weight of matching a query (with a concept). Set to 0 to disable matching of queries.")
	private double queryWeight = 1;

	@Parameter(names = { "--parent-weight" }, validateWith = PositiveDouble.class, description = "Weight of concept's parent when computing path enrichment. Weight of grand-parent is parent-weight times parent-weight, etc. Set to 0 to disable path enrichment.")
	private double parentWeight = 0.5;

	@Parameter(names = { "--path-weight" }, validateWith = PositiveDouble.class, description = "Weight of path enrichment. Weight of concept is 1. Set to 0 to disable path enrichment")
	private double pathWeight = 0.7;

	public int getCompoundWords() {
		return compoundWords;
	}

	public double getMismatchMultiplier() {
		return mismatchMultiplier;
	}

	public double getMatchMinimum() {
		return matchMinimum;
	}

	public double getPositionOffBy1() {
		return positionOffBy1;
	}

	public double getPositionOffBy2() {
		return positionOffBy2;
	}

	public double getPositionMatchScaling() {
		return positionMatchScaling;
	}

	public double getPositionLoss() {
		return positionLoss;
	}

	public double getScoreScaling() {
		return scoreScaling;
	}

	public double getConceptWeight() {
		return conceptWeight;
	}

	public double getQueryWeight() {
		return queryWeight;
	}

	public double getParentWeight() {
		return parentWeight;
	}

	public double getPathWeight() {
		return pathWeight;
	}
}
