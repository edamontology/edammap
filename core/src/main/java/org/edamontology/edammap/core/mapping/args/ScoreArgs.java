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

	@Parameter(names = { "--output-good-scores" }, arity = 1, description = "Output matches with good scores")
	private boolean outputGoodScores = true;

	@Parameter(names = { "--output-medium-scores" }, arity = 1, description = "Output matches with medium scores")
	private boolean outputMediumScores = true;

	@Parameter(names = { "--output-bad-scores" }, arity = 1, description = "Output matches with bad scores")
	private boolean outputBadScores = false;

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

	public boolean isOutputGoodScores() {
		return outputGoodScores;
	}

	public boolean isOutputMediumScores() {
		return outputMediumScores;
	}

	public boolean isOutputBadScores() {
		return outputBadScores;
	}
}
