/*
 * Copyright © 2016, 2019 Erik Jaaniso
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

import org.edamontology.edammap.core.args.ZeroToOneDouble;

import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.Args;

import com.beust.jcommander.Parameter;

public class ScoreArgs extends Args {

	private static final String goodScoreTopicId = "goodScoreTopic";
	private static final String goodScoreTopicDescription = "Final scores over this are considered good (in topic branch)";
	private static final Double goodScoreTopicDefault = 0.63;
	@Parameter(names = { "--" + goodScoreTopicId }, validateWith = ZeroToOneDouble.class, description = goodScoreTopicDescription)
	private Double goodScoreTopic = goodScoreTopicDefault;

	private static final String goodScoreOperationId = "goodScoreOperation";
	private static final String goodScoreOperationDescription = "Final scores over this are considered good (in operation branch)";
	private static final Double goodScoreOperationDefault = 0.63;
	@Parameter(names = { "--" + goodScoreOperationId }, validateWith = ZeroToOneDouble.class, description = goodScoreOperationDescription)
	private Double goodScoreOperation = goodScoreOperationDefault;

	private static final String goodScoreDataId = "goodScoreData";
	private static final String goodScoreDataDescription = "Final scores over this are considered good (in data branch)";
	private static final Double goodScoreDataDefault = 0.63;
	@Parameter(names = { "--" + goodScoreDataId }, validateWith = ZeroToOneDouble.class, description = goodScoreDataDescription)
	private Double goodScoreData = goodScoreDataDefault;

	private static final String goodScoreFormatId = "goodScoreFormat";
	private static final String goodScoreFormatDescription = "Final scores over this are considered good (in format branch)";
	private static final Double goodScoreFormatDefault = 0.63;
	@Parameter(names = { "--" + goodScoreFormatId }, validateWith = ZeroToOneDouble.class, description = goodScoreFormatDescription)
	private Double goodScoreFormat = goodScoreFormatDefault;

	private static final String badScoreTopicId = "badScoreTopic";
	private static final String badScoreTopicDescription = "Final scores under this are considered bad (in topic branch)";
	private static final Double badScoreTopicDefault = 0.57;
	@Parameter(names = { "--" + badScoreTopicId }, validateWith = ZeroToOneDouble.class, description = badScoreTopicDescription)
	private Double badScoreTopic = badScoreTopicDefault;

	private static final String badScoreOperationId = "badScoreOperation";
	private static final String badScoreOperationDescription = "Final scores under this are considered bad (in operation branch)";
	private static final Double badScoreOperationDefault = 0.57;
	@Parameter(names = { "--" + badScoreOperationId }, validateWith = ZeroToOneDouble.class, description = badScoreOperationDescription)
	private Double badScoreOperation = badScoreOperationDefault;

	private static final String badScoreDataId = "badScoreData";
	private static final String badScoreDataDescription = "Final scores under this are considered bad (in data branch)";
	private static final Double badScoreDataDefault = 0.57;
	@Parameter(names = { "--" + badScoreDataId }, validateWith = ZeroToOneDouble.class, description = badScoreDataDescription)
	private Double badScoreData = badScoreDataDefault;

	private static final String badScoreFormatId = "badScoreFormat";
	private static final String badScoreFormatDescription = "Final scores under this are considered bad (in format branch)";
	private static final Double badScoreFormatDefault = 0.57;
	@Parameter(names = { "--" + badScoreFormatId }, validateWith = ZeroToOneDouble.class, description = badScoreFormatDescription)
	private Double badScoreFormat = badScoreFormatDefault;

	private static final String outputGoodScoresId = "outputGoodScores";
	private static final String outputGoodScoresDescription = "Output matches with good scores";
	private static final Boolean outputGoodScoresDefault = true;
	@Parameter(names = { "--" + outputGoodScoresId }, arity = 1, description = outputGoodScoresDescription)
	private Boolean outputGoodScores = outputGoodScoresDefault;

	private static final String outputMediumScoresId = "outputMediumScores";
	private static final String outputMediumScoresDescription = "Output matches with medium scores";
	private static final Boolean outputMediumScoresDefault = true;
	@Parameter(names = { "--" + outputMediumScoresId }, arity = 1, description = outputMediumScoresDescription)
	private Boolean outputMediumScores = outputMediumScoresDefault;

	private static final String outputBadScoresId = "outputBadScores";
	private static final String outputBadScoresDescription = "Output matches with bad scores";
	private static final Boolean outputBadScoresDefault = false;
	@Parameter(names = { "--" + outputBadScoresId }, arity = 1, description = outputBadScoresDescription)
	private Boolean outputBadScores = outputBadScoresDefault;

	@Override
	protected void addArgs() {
		args.add(new Arg<>(this::getGoodScoreTopic, this::setGoodScoreTopic, goodScoreTopicDefault, 0.0, 1.0, goodScoreTopicId, "Good score for topic", goodScoreTopicDescription, null));
		args.add(new Arg<>(this::getGoodScoreOperation, this::setGoodScoreOperation, goodScoreOperationDefault, 0.0, 1.0, goodScoreOperationId, "Good score for operation", goodScoreOperationDescription, null));
		args.add(new Arg<>(this::getGoodScoreData, this::setGoodScoreData, goodScoreDataDefault, 0.0, 1.0, goodScoreDataId, "Good score for data", goodScoreDataDescription, null));
		args.add(new Arg<>(this::getGoodScoreFormat, this::setGoodScoreFormat, goodScoreFormatDefault, 0.0, 1.0, goodScoreFormatId, "Good score for format", goodScoreFormatDescription, null));
		args.add(new Arg<>(this::getBadScoreTopic, this::setBadScoreTopic, badScoreTopicDefault, 0.0, 1.0, badScoreTopicId, "Bad score for topic", badScoreTopicDescription, null));
		args.add(new Arg<>(this::getBadScoreOperation, this::setBadScoreOperation, badScoreOperationDefault, 0.0, 1.0, badScoreOperationId, "Bad score for operation", badScoreOperationDescription, null));
		args.add(new Arg<>(this::getBadScoreData, this::setBadScoreData, badScoreDataDefault, 0.0, 1.0, badScoreDataId, "Bad score for data", badScoreDataDescription, null));
		args.add(new Arg<>(this::getBadScoreFormat, this::setBadScoreFormat, badScoreFormatDefault, 0.0, 1.0, badScoreFormatId, "Bad score for format", badScoreFormatDescription, null));
		args.add(new Arg<>(this::isOutputGoodScores, this::setOutputGoodScores, outputGoodScoresDefault, outputGoodScoresId, "Matches with good scores", outputGoodScoresDescription, null));
		args.add(new Arg<>(this::isOutputMediumScores, this::setOutputMediumScores, outputMediumScoresDefault, outputMediumScoresId, "Matches with medium scores", outputMediumScoresDescription, null));
		args.add(new Arg<>(this::isOutputBadScores, this::setOutputBadScores, outputBadScoresDefault, outputBadScoresId, "Matches with bad scores", outputBadScoresDescription, null));
	}

	@Override
	public String getId() {
		return "scoreArgs";
	}

	@Override
	public String getLabel() {
		return "Score limits";
	}

	public Double getGoodScoreTopic() {
		return goodScoreTopic;
	}
	public void setGoodScoreTopic(Double goodScoreTopic) {
		this.goodScoreTopic = goodScoreTopic;
	}

	public Double getGoodScoreOperation() {
		return goodScoreOperation;
	}
	public void setGoodScoreOperation(Double goodScoreOperation) {
		this.goodScoreOperation = goodScoreOperation;
	}

	public Double getGoodScoreData() {
		return goodScoreData;
	}
	public void setGoodScoreData(Double goodScoreData) {
		this.goodScoreData = goodScoreData;
	}

	public Double getGoodScoreFormat() {
		return goodScoreFormat;
	}
	public void setGoodScoreFormat(Double goodScoreFormat) {
		this.goodScoreFormat = goodScoreFormat;
	}

	public Double getBadScoreTopic() {
		return badScoreTopic;
	}
	public void setBadScoreTopic(Double badScoreTopic) {
		this.badScoreTopic = badScoreTopic;
	}

	public Double getBadScoreOperation() {
		return badScoreOperation;
	}
	public void setBadScoreOperation(Double badScoreOperation) {
		this.badScoreOperation = badScoreOperation;
	}

	public Double getBadScoreData() {
		return badScoreData;
	}
	public void setBadScoreData(Double badScoreData) {
		this.badScoreData = badScoreData;
	}

	public Double getBadScoreFormat() {
		return badScoreFormat;
	}
	public void setBadScoreFormat(Double badScoreFormat) {
		this.badScoreFormat = badScoreFormat;
	}

	public Boolean isOutputGoodScores() {
		return outputGoodScores;
	}
	public void setOutputGoodScores(Boolean outputGoodScores) {
		this.outputGoodScores = outputGoodScores;
	}

	public Boolean isOutputMediumScores() {
		return outputMediumScores;
	}
	public void setOutputMediumScores(Boolean outputMediumScores) {
		this.outputMediumScores = outputMediumScores;
	}

	public Boolean isOutputBadScores() {
		return outputBadScores;
	}
	public void setOutputBadScores(Boolean outputBadScores) {
		this.outputBadScores = outputBadScores;
	}
}
