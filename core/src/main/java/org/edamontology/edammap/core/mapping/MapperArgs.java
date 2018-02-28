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

package org.edamontology.edammap.core.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.beust.jcommander.validators.PositiveInteger;

import org.edamontology.edammap.core.args.ZeroToOneDouble;
import org.edamontology.edammap.core.edam.Branch;

public class MapperArgs {
	@Parameter(names = { "-b", "--branches" }, variableArity = true, description = "Branches to include. Space separated from list [topic, operation, data, format].")
	private List<Branch> branches = new ArrayList<>(Arrays.asList(Branch.topic, Branch.operation));

	@Parameter(names = { "-m", "--match" }, validateWith = PositiveInteger.class, description = "Number of best matches per branch to output")
	private int match = 3;

	@Parameter(names = { "--obsolete" }, description = "Include obsolete concepts")
	private boolean obsolete = false;

	@Parameter(names = { "--good-score-topic" }, validateWith = ZeroToOneDouble.class, description = "Final scores over this are considered good (in topic branch)")
	private double goodScoreTopic = 0.63;

	@Parameter(names = { "--good-score-operation" }, validateWith = ZeroToOneDouble.class, description = "Final scores over this are considered good (in operation branch)")
	private double goodScoreOperation = 0.63;

	@Parameter(names = { "--good-score-data" }, validateWith = ZeroToOneDouble.class, description = "Final scores over this are considered good (in data branch)")
	private double goodScoreData = 0.63;

	@Parameter(names = { "--good-score-format" }, validateWith = ZeroToOneDouble.class, description = "Final scores over this are considered good (in format branch)")
	private double goodScoreFormat = 0.63;

	@Parameter(names = { "--bad-score-topic" }, validateWith = ZeroToOneDouble.class, description = "Final scores under this are considered bad (in topic branch)")
	private double badScoreTopic = 0.57;

	@Parameter(names = { "--bad-score-operation" }, validateWith = ZeroToOneDouble.class, description = "Final scores under this are considered bad (in operation branch)")
	private double badScoreOperation = 0.57;

	@Parameter(names = { "--bad-score-data" }, validateWith = ZeroToOneDouble.class, description = "Final scores under this are considered bad (in data branch)")
	private double badScoreData = 0.57;

	@Parameter(names = { "--bad-score-format" }, validateWith = ZeroToOneDouble.class, description = "Final scores under this are considered bad (in format branch)")
	private double badScoreFormat = 0.57;

	@Parameter(names = { "--no-output-good-scores" }, description = "Do not output matches with good scores")
	private boolean noOutputGoodScores = false;

	@Parameter(names = { "--no-output-medium-scores" }, description = "Do not output matches with medium scores")
	private boolean noOutputMediumScores = false;

	@Parameter(names = { "--output-bad-scores" }, description = "Output matches with bad scores")
	private boolean outputBadScores = false;

	@Parameter(names = { "--exclude-annotations" }, description = "Don't suggest concepts already used for annotating query. Then parents and children of these concepts are not suggested either (unless --no-remove-inferior-parent-child is specified).")
	private boolean excludeAnnotations = false;

	@Parameter(names = { "--no-remove-inferior-parent-child" }, description = "Don't remove parents and children of a better matched concept from suggestion results")
	private boolean noRemoveInferiorParentChild = false;

	@Parameter(names = { "--no-remove-top-level" }, description = "Don't remove the top level concepts (topic, operation, data, format) from suggestion results")
	private boolean noRemoveTopLevel = false;

	@ParametersDelegate
	private MapperAlgorithmArgs algorithmArgs = new MapperAlgorithmArgs();

	@ParametersDelegate
	private MapperIdfMultiplierArgs idfMultiplierArgs = new MapperIdfMultiplierArgs();

	public int getMatch() {
		return match;
	}

	public List<Branch> getBranches() {
		return branches;
	}

	public boolean getObsolete() {
		return obsolete;
	}

	public double getGoodScoreTopic() {
		return goodScoreTopic;
	}

	public double getGoodScoreOperation() {
		return goodScoreOperation;
	}

	public double getGoodScoreData() {
		return goodScoreData;
	}

	public double getGoodScoreFormat() {
		return goodScoreFormat;
	}

	public double getBadScoreTopic() {
		return badScoreTopic;
	}

	public double getBadScoreOperation() {
		return badScoreOperation;
	}

	public double getBadScoreData() {
		return badScoreData;
	}

	public double getBadScoreFormat() {
		return badScoreFormat;
	}

	public boolean isNoOutputGoodScores() {
		return noOutputGoodScores;
	}

	public boolean isNoOutputMediumScores() {
		return noOutputMediumScores;
	}

	public boolean isOutputBadScores() {
		return outputBadScores;
	}

	public boolean isExcludeAnnotations() {
		return excludeAnnotations;
	}

	public boolean isNoRemoveInferiorParentChild() {
		return noRemoveInferiorParentChild;
	}

	public boolean isNoRemoveTopLevel() {
		return noRemoveTopLevel;
	}

	public MapperAlgorithmArgs getAlgorithmArgs() {
		return algorithmArgs;
	}

	public MapperIdfMultiplierArgs getIdfMultiplierArgs() {
		return idfMultiplierArgs;
	}
}
