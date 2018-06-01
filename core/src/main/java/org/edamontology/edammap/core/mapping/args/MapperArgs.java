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
import java.util.stream.Collectors;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.beust.jcommander.validators.PositiveInteger;

import org.edamontology.edammap.core.edam.Branch;

public class MapperArgs {

	public static final String BRANCHES = "branches";
	@Parameter(names = { "--" + BRANCHES }, variableArity = true, description = "Branches to include. Space separated from list [topic, operation, data, format].")
	private List<Branch> branches = new ArrayList<>(Arrays.asList(Branch.topic, Branch.operation));

	public static final String MATCHES = "matches";
	@Parameter(names = { "--" + MATCHES }, validateWith = PositiveInteger.class, description = "Number of best matches per branch to output")
	private int matches = 3;

	public static final String OBSOLETE = "obsolete";
	@Parameter(names = { "--" + OBSOLETE }, arity = 1, description = "Include obsolete concepts")
	private boolean obsolete = false;

	public static final String DONE_ANNOTATIONS = "doneAnnotations";
	@Parameter(names = { "--" + DONE_ANNOTATIONS }, arity = 1, description = "Suggest concepts already used for annotating query. Parents and children of these concepts are not suggested in any case (unless --inferior-parent-child is set to true).")
	private boolean doneAnnotations = true;

	public static final String INFERIOR_PARENTS_CHILDREN = "inferiorParentsChildren";
	@Parameter(names = { "--" + INFERIOR_PARENTS_CHILDREN }, arity = 1, description = "Include parents and children of a better matched concept in suggestion results")
	private boolean inferiorParentsChildren = false;

	public static final String ALGORITHM_ARGS = "algorithmArgs";
	@ParametersDelegate
	private AlgorithmArgs algorithmArgs = new AlgorithmArgs();

	public static final String IDF_ARGS = "idfArgs";
	@ParametersDelegate
	private IdfArgs idfArgs = new IdfArgs();

	public static final String MULTIPLIER_ARGS = "multiplierArgs";
	@ParametersDelegate
	private MultiplierArgs multiplierArgs = new MultiplierArgs();

	public static final String NORMALISER_ARGS = "normaliserArgs";
	@ParametersDelegate
	private NormaliserArgs normaliserArgs = new NormaliserArgs();

	public static final String WEIGHT_ARGS = "weightArgs";
	@ParametersDelegate
	private WeightArgs weightArgs = new WeightArgs();

	public static final String SCORE_ARGS = "scoreArgs";
	@ParametersDelegate
	private ScoreArgs scoreArgs = new ScoreArgs();

	public List<Branch> getBranches() {
		return branches;
	}
	public void setBranches(List<Branch> branches) {
		this.branches = branches.stream().distinct().collect(Collectors.toList());
	}

	public int getMatches() {
		return matches;
	}
	public void setMatches(int matches) {
		this.matches = matches;
	}

	public boolean isObsolete() {
		return obsolete;
	}
	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}

	public boolean isDoneAnnotations() {
		return doneAnnotations;
	}
	public void setDoneAnnotations(boolean doneAnnotations) {
		this.doneAnnotations = doneAnnotations;
	}

	public boolean isInferiorParentsChildren() {
		return inferiorParentsChildren;
	}
	public void setInferiorParentsChildren(boolean inferiorParentsChildren) {
		this.inferiorParentsChildren = inferiorParentsChildren;
	}

	public AlgorithmArgs getAlgorithmArgs() {
		return algorithmArgs;
	}
	public void setAlgorithmArgs(AlgorithmArgs algorithmArgs) {
		this.algorithmArgs = algorithmArgs;
	}

	public IdfArgs getIdfArgs() {
		return idfArgs;
	}
	public void setIdfArgs(IdfArgs idfArgs) {
		this.idfArgs = idfArgs;
	}

	public MultiplierArgs getMultiplierArgs() {
		return multiplierArgs;
	}
	public void setMultiplierArgs(MultiplierArgs multiplierArgs) {
		this.multiplierArgs = multiplierArgs;
	}

	public NormaliserArgs getNormaliserArgs() {
		return normaliserArgs;
	}
	public void setNormaliserArgs(NormaliserArgs normaliserArgs) {
		this.normaliserArgs = normaliserArgs;
	}

	public WeightArgs getWeightArgs() {
		return weightArgs;
	}
	public void setWeightArgs(WeightArgs weightArgs) {
		this.weightArgs = weightArgs;
	}

	public ScoreArgs getScoreArgs() {
		return scoreArgs;
	}
	public void setScoreArgs(ScoreArgs scoreArgs) {
		this.scoreArgs = scoreArgs;
	}
}
