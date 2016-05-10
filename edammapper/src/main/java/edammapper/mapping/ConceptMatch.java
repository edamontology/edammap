package edammapper.mapping;

public class ConceptMatch {

	private final double score;

	private final ConceptMatchType type;

	private final int synonymIndex;

	ConceptMatch(double score, ConceptMatchType type, int synonymIndex) {
		this.score = score;
		this.type = type;
		this.synonymIndex = synonymIndex;
	}

	public double getScore() {
		return score;
	}

	public ConceptMatchType getType() {
		return type;
	}

	public int getSynonymIndex() {
		return synonymIndex;
	}
}
