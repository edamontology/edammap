package edammapper.mapping;

public class QueryMatch {

	private final double score;

	private final QueryMatchType type;

	private final int index;

	private final int indexInPublication;

	QueryMatch(double score, QueryMatchType type, int index, int indexInPublication) {
		this.score = score;
		this.type = type;
		this.index = index;
		this.indexInPublication = indexInPublication;
	}

	public double getScore() {
		return score;
	}

	public QueryMatchType getType() {
		return type;
	}

	public int getIndex() {
		return index;
	}

	public int getIndexInPublication() {
		return indexInPublication;
	}
}
