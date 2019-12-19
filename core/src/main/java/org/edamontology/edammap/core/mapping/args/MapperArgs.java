/*
 * Copyright © 2016, 2017, 2018, 2019 Erik Jaaniso
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

import org.edamontology.edammap.core.args.ZeroToOneDouble;
import org.edamontology.edammap.core.edam.Branch;

import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.Args;
import org.edamontology.pubfetcher.core.common.PositiveInteger;

public class MapperArgs extends Args {

	private static final String branchesId = "branches";
	private static final String branchesDescription = "Branches to include. Can choose multiple at once from possible values.";
	private static final List<Branch> branchesDefault = new ArrayList<>(Arrays.asList(Branch.topic, Branch.operation));
	@Parameter(names = { "--" + branchesId }, variableArity = true, description = branchesDescription)
	private List<Branch> branches = branchesDefault;

	private static final String matchesId = "matches";
	private static final String matchesDescription = "Number of best matches per branch to output. Output amount can be less than requested if not enough match final scores fulfill score limits requirement.";
	private static final Integer matchesDefault = 5;
	@Parameter(names = { "--" + matchesId }, validateWith = PositiveInteger.class, description = matchesDescription)
	private Integer matches = matchesDefault;

	private static final String obsoleteId = "obsolete";
	private static final String obsoleteDescription = "Include matched obsolete concepts";
	private static final Boolean obsoleteDefault = false;
	@Parameter(names = { "--" + obsoleteId }, arity = 1, description = obsoleteDescription)
	private Boolean obsolete = obsoleteDefault;

	private static final String replaceObsoleteId = "replaceObsolete";
	private static final String replaceObsoleteDescription = "Replace matched obsolete concepts with their best matched replacement defined in EDAM (with \"replacedBy\" or \"consider\")";
	private static final Boolean replaceObsoleteDefault = true;
	@Parameter(names = { "--" + replaceObsoleteId }, arity = 1, description = replaceObsoleteDescription)
	private Boolean replaceObsolete = replaceObsoleteDefault;

	private static final String obsoletePenaltyId = "obsoletePenalty";
	private static final String obsoletePenaltyDescription = "The fraction of the final score that included or replaced obsolete concepts will get";
	private static final Double obsoletePenaltyDefault = 0.5;
	@Parameter(names = { "--" + obsoletePenaltyId }, validateWith = ZeroToOneDouble.class, description = obsoletePenaltyDescription)
	private Double obsoletePenalty = obsoletePenaltyDefault;

	private static final String doneAnnotationsId = "doneAnnotations";
	private static final String doneAnnotationsDescription = "Suggest concepts already used for annotating query. Parents and children of these concepts are not suggested in any case (unless --inferiorParentsChildren is set to true).";
	private static final Boolean doneAnnotationsDefault = true;
	@Parameter(names = { "--" + doneAnnotationsId }, arity = 1, description = doneAnnotationsDescription)
	private Boolean doneAnnotations = doneAnnotationsDefault;

	private static final String inferiorParentsChildrenId = "inferiorParentsChildren";
	private static final String inferiorParentsChildrenDescription = "Include parents and children of a better matched concept in suggestion results";
	private static final Boolean inferiorParentsChildrenDefault = false;
	@Parameter(names = { "--" + inferiorParentsChildrenId }, arity = 1, description = inferiorParentsChildrenDescription)
	private Boolean inferiorParentsChildren = inferiorParentsChildrenDefault;

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

	@Override
	protected void addArgs() {
		args.add(new Arg<>(this::getBranches, this::setBranches, branchesDefault, branchesId, "Branches", branchesDescription, Branch.class, "http://edamontology.org/page#Scope"));
		args.add(new Arg<>(this::getMatches, this::setMatches, matchesDefault, 0, null, matchesId, "Top matches per branch", matchesDescription, null));
		args.add(new Arg<>(this::isObsolete, this::setObsolete, obsoleteDefault, obsoleteId, "Obsolete concepts", obsoleteDescription, null));
		args.add(new Arg<>(this::isReplaceObsolete, this::setReplaceObsolete, replaceObsoleteDefault, replaceObsoleteId, "Replace obsolete concepts", replaceObsoleteDescription, null));
		args.add(new Arg<>(this::getObsoletePenalty, this::setObsoletePenalty, obsoletePenaltyDefault, obsoletePenaltyId, "Penalty for obsolete concepts", obsoletePenaltyDescription, null));
		args.add(new Arg<>(this::isDoneAnnotations, this::setDoneAnnotations, doneAnnotationsDefault, doneAnnotationsId, "Done annotations", doneAnnotationsDescription, null));
		args.add(new Arg<>(this::isInferiorParentsChildren, this::setInferiorParentsChildren, inferiorParentsChildrenDefault, inferiorParentsChildrenId, "Inferior parents & children", inferiorParentsChildrenDescription, null));
	}

	@Override
	public String getId() {
		return "mapperArgs";
	}

	@Override
	public String getLabel() {
		return "Mapping";
	}

	public List<Branch> getBranches() {
		return branches;
	}
	public void setBranches(List<Branch> branches) {
		this.branches = branches.stream().distinct().collect(Collectors.toList());
	}

	public Integer getMatches() {
		return matches;
	}
	public void setMatches(Integer matches) {
		this.matches = matches;
	}

	public Boolean isObsolete() {
		return obsolete;
	}
	public void setObsolete(Boolean obsolete) {
		this.obsolete = obsolete;
	}

	public Boolean isReplaceObsolete() {
		return replaceObsolete;
	}
	public void setReplaceObsolete(Boolean replaceObsolete) {
		this.replaceObsolete = replaceObsolete;
	}

	public Double getObsoletePenalty() {
		return obsoletePenalty;
	}
	public void setObsoletePenalty(Double obsoletePenalty) {
		this.obsoletePenalty = obsoletePenalty;
	}

	public Boolean isDoneAnnotations() {
		return doneAnnotations;
	}
	public void setDoneAnnotations(Boolean doneAnnotations) {
		this.doneAnnotations = doneAnnotations;
	}

	public Boolean isInferiorParentsChildren() {
		return inferiorParentsChildren;
	}
	public void setInferiorParentsChildren(Boolean inferiorParentsChildren) {
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
