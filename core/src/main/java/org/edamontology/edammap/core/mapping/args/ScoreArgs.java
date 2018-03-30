/*
 * Copyright © 2016 Erik Jaaniso
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

import com.beust.jcommander.Parameter;

public class ScoreArgs {

	public static final String GOOD_SCORE_TOPIC = "good-score-topic";
	@Parameter(names = { "--" + GOOD_SCORE_TOPIC }, validateWith = ZeroToOneDouble.class, description = "Final scores over this are considered good (in topic branch)")
	private double goodScoreTopic = 0.63;

	public static final String GOOD_SCORE_OPERATION = "good-score-operation";
	@Parameter(names = { "--" + GOOD_SCORE_OPERATION }, validateWith = ZeroToOneDouble.class, description = "Final scores over this are considered good (in operation branch)")
	private double goodScoreOperation = 0.63;

	public static final String GOOD_SCORE_DATA = "good-score-data";
	@Parameter(names = { "--" + GOOD_SCORE_DATA }, validateWith = ZeroToOneDouble.class, description = "Final scores over this are considered good (in data branch)")
	private double goodScoreData = 0.63;

	public static final String GOOD_SCORE_FORMAT = "good-score-format";
	@Parameter(names = { "--" + GOOD_SCORE_FORMAT }, validateWith = ZeroToOneDouble.class, description = "Final scores over this are considered good (in format branch)")
	private double goodScoreFormat = 0.63;

	public static final String BAD_SCORE_TOPIC = "bad-score-topic";
	@Parameter(names = { "--" + BAD_SCORE_TOPIC }, validateWith = ZeroToOneDouble.class, description = "Final scores under this are considered bad (in topic branch)")
	private double badScoreTopic = 0.57;

	public static final String BAD_SCORE_OPERATION = "bad-score-operation";
	@Parameter(names = { "--" + BAD_SCORE_OPERATION }, validateWith = ZeroToOneDouble.class, description = "Final scores under this are considered bad (in operation branch)")
	private double badScoreOperation = 0.57;

	public static final String BAD_SCORE_DATA = "bad-score-data";
	@Parameter(names = { "--" + BAD_SCORE_DATA }, validateWith = ZeroToOneDouble.class, description = "Final scores under this are considered bad (in data branch)")
	private double badScoreData = 0.57;

	public static final String BAD_SCORE_FORMAT = "bad-score-format";
	@Parameter(names = { "--" + BAD_SCORE_FORMAT }, validateWith = ZeroToOneDouble.class, description = "Final scores under this are considered bad (in format branch)")
	private double badScoreFormat = 0.57;

	public static final String OUTPUT_GOOD_SCORES = "output-good-scores";
	@Parameter(names = { "--" + OUTPUT_GOOD_SCORES }, arity = 1, description = "Output matches with good scores")
	private boolean outputGoodScores = true;

	public static final String OUTPUT_MEDIUM_SCORES = "output-medium-scores";
	@Parameter(names = { "--" + OUTPUT_MEDIUM_SCORES }, arity = 1, description = "Output matches with medium scores")
	private boolean outputMediumScores = true;

	public static final String OUTPUT_BAD_SCORES = "output-bad-scores";
	@Parameter(names = { "--" + OUTPUT_BAD_SCORES }, arity = 1, description = "Output matches with bad scores")
	private boolean outputBadScores = false;

	public double getGoodScoreTopic() {
		return goodScoreTopic;
	}
	public void setGoodScoreTopic(double goodScoreTopic) {
		this.goodScoreTopic = goodScoreTopic;
	}

	public double getGoodScoreOperation() {
		return goodScoreOperation;
	}
	public void setGoodScoreOperation(double goodScoreOperation) {
		this.goodScoreOperation = goodScoreOperation;
	}

	public double getGoodScoreData() {
		return goodScoreData;
	}
	public void setGoodScoreData(double goodScoreData) {
		this.goodScoreData = goodScoreData;
	}

	public double getGoodScoreFormat() {
		return goodScoreFormat;
	}
	public void setGoodScoreFormat(double goodScoreFormat) {
		this.goodScoreFormat = goodScoreFormat;
	}

	public double getBadScoreTopic() {
		return badScoreTopic;
	}
	public void setBadScoreTopic(double badScoreTopic) {
		this.badScoreTopic = badScoreTopic;
	}

	public double getBadScoreOperation() {
		return badScoreOperation;
	}
	public void setBadScoreOperation(double badScoreOperation) {
		this.badScoreOperation = badScoreOperation;
	}

	public double getBadScoreData() {
		return badScoreData;
	}
	public void setBadScoreData(double badScoreData) {
		this.badScoreData = badScoreData;
	}

	public double getBadScoreFormat() {
		return badScoreFormat;
	}
	public void setBadScoreFormat(double badScoreFormat) {
		this.badScoreFormat = badScoreFormat;
	}

	public boolean isOutputGoodScores() {
		return outputGoodScores;
	}
	public void setOutputGoodScores(boolean outputGoodScores) {
		this.outputGoodScores = outputGoodScores;
	}

	public boolean isOutputMediumScores() {
		return outputMediumScores;
	}
	public void setOutputMediumScores(boolean outputMediumScores) {
		this.outputMediumScores = outputMediumScores;
	}

	public boolean isOutputBadScores() {
		return outputBadScores;
	}
	public void setOutputBadScores(boolean outputBadScores) {
		this.outputBadScores = outputBadScores;
	}
}
