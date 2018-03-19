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

package org.edamontology.edammap.core.mapping.args;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.beust.jcommander.validators.PositiveInteger;

import org.edamontology.edammap.core.edam.Branch;

public class MapperArgs {
	@Parameter(names = { "-b", "--branches" }, variableArity = true, description = "Branches to include. Space separated from list [topic, operation, data, format].")
	private List<Branch> branches = new ArrayList<>(Arrays.asList(Branch.topic, Branch.operation));

	@Parameter(names = { "-m", "--matches" }, validateWith = PositiveInteger.class, description = "Number of best matches per branch to output")
	private int matches = 3;

	@Parameter(names = { "--obsolete" }, arity = 1, description = "Include/exclude obsolete concepts")
	private boolean obsolete = false;

	@Parameter(names = { "--annotations" }, arity = 1, description = "Do/don't suggest concepts already used for annotating query. Then parents and children of these concepts are not suggested either (unless --inferior-parent-child is set to true).")
	private boolean annotations = true;

	@Parameter(names = { "--inferior-parents-children" }, arity = 1, description = "Include/exclude parents and children of a better matched concept in suggestion results")
	private boolean inferiorParentsChildren = false;

	@Parameter(names = { "--top-level" }, arity = 1, description = "Include/exclude top level concepts (topic, operation, data, format) in suggestion results")
	private boolean topLevel = false;

	@ParametersDelegate
	private AlgorithmArgs algorithmArgs = new AlgorithmArgs();

	@ParametersDelegate
	private IdfArgs idfArgs = new IdfArgs();

	@ParametersDelegate
	private MultiplierArgs multiplierArgs = new MultiplierArgs();

	@ParametersDelegate
	private NormaliserArgs normaliserArgs = new NormaliserArgs();

	@ParametersDelegate
	private WeightArgs weightArgs = new WeightArgs();

	@ParametersDelegate
	private ScoreArgs scoreArgs = new ScoreArgs();

	public int getMatches() {
		return matches;
	}

	public List<Branch> getBranches() {
		return branches;
	}

	public boolean isObsolete() {
		return obsolete;
	}

	public boolean isAnnotations() {
		return annotations;
	}

	public boolean isInferiorParentsChildren() {
		return inferiorParentsChildren;
	}

	public boolean isTopLevel() {
		return topLevel;
	}

	public AlgorithmArgs getAlgorithmArgs() {
		return algorithmArgs;
	}

	public IdfArgs getIdfArgs() {
		return idfArgs;
	}

	public MultiplierArgs getMultiplierArgs() {
		return multiplierArgs;
	}

	public NormaliserArgs getNormaliserArgs() {
		return normaliserArgs;
	}

	public WeightArgs getWeightArgs() {
		return weightArgs;
	}

	public ScoreArgs getScoreArgs() {
		return scoreArgs;
	}
}
