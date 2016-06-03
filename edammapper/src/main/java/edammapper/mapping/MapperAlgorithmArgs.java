package edammapper.mapping;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

import edammapper.args.PositiveDouble;
import edammapper.args.ZeroToOneDouble;

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
}
