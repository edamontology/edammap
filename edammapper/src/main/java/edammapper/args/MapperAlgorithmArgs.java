package edammapper.args;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

public class MapperAlgorithmArgs {
	@Parameter(names = { "--compound-words" }, validateWith = PositiveInteger.class, description = "Try to match words that have accidentally been made compound (given number is maximum number of words in an accidental compound minus one)")
	private int compoundWords = 1;

	@Parameter(names = { "--mismatch-multiplier" }, validateWith = PositiveDouble.class, description = "Multiplier for score decrease caused by mismatch")
	private double mismatchMultiplier = 2;

	@Parameter(names = { "--position-off-by-1" }, validateWith = ZeroToOneDouble.class, description = "Multiplier of a position score component for the case when a word is inserted between matched words or matched words are switched")
	private double positionOffBy1 = 0.5;

	@Parameter(names = { "--position-off-by-2" }, validateWith = ZeroToOneDouble.class, description = "Multiplier of a position score component for the case when two words are inserted between matched words or matched words are switched with an additional word between them")
	private double positionOffBy2 = 0.125;

	@Parameter(names = { "--position-loss" }, validateWith = ZeroToOneDouble.class, description = "Maximum loss, given as fraction of score, caused by wrong positions of matched words")
	private double positionLoss = 0.5;

	@Parameter(names = { "--concept-weight" }, validateWith = PositiveDouble.class, description = "Weight of matching a concept (with a query)")
	private double conceptWeight = 1;

	@Parameter(names = { "--query-weight" }, validateWith = PositiveDouble.class, description = "Weight of matching a query (with a concept)")
	private double queryWeight = 1;

	@Parameter(names = { "--label-multiplier" }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a label. Set to 0 to disable matching of labels.")
	private double labelMultiplier = 1;

	@Parameter(names = { "--exact-synonym-multiplier" }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching an exact synonym. Set to 0 to disable matching of exact synonyms.")
	private double exactSynonymMultiplier = 0.95;

	@Parameter(names = { "--narrow-broad-synonym-multiplier" }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a narrow or broad synonym. Set to 0 to disable matching of narrow and broad synonyms.")
	private double narrowBroadMultiplier = 0.9;

	@Parameter(names = { "--definition-multiplier" }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a definition. Set to 0 to disable matching of definitions.")
	private double definitionMultiplier = 0.5;

	@Parameter(names = { "--comment-multiplier" }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a comment. Set to 0 to disable matching of comments.")
	private double commentMultiplier = 0.5;

	public int getCompoundWords() {
		return compoundWords;
	}

	public double getMismatchMultiplier() {
		return mismatchMultiplier;
	}

	public double getPositionOffBy1() {
		return positionOffBy1;
	}

	public double getPositionOffBy2() {
		return positionOffBy2;
	}

	public double getPositionLoss() {
		return positionLoss;
	}

	public double getConceptWeight() {
		return conceptWeight;
	}

	public double getQueryWeight() {
		return queryWeight;
	}

	public double getLabelMultiplier() {
		return labelMultiplier;
	}

	public double getExactSynonymMultiplier() {
		return exactSynonymMultiplier;
	}

	public double getNarrowBroadMultiplier() {
		return narrowBroadMultiplier;
	}

	public double getDefinitionMultiplier() {
		return definitionMultiplier;
	}

	public double getCommentMultiplier() {
		return commentMultiplier;
	}
}
