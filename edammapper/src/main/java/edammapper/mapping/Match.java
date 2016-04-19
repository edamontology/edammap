package edammapper.mapping;

import edammapper.edam.EdamUri;

public class Match implements Comparable<Match> {

	private final int queryLength;
	private final int matchLength;

	private final EdamUri edamUri;

	private final MatchType matchType;

	private final int synonymIndex;

	private final MatchConfidence matchConfidence;

	private final double score;

	public Match(int queryLength, int matchLength, EdamUri edamUri, MatchType matchType, int synonymIndex, MatchConfidence matchConfidence, double score) {
		this.queryLength = queryLength;
		this.matchLength = matchLength;
		this.edamUri = edamUri;
		this.matchType = matchType;
		this.synonymIndex = synonymIndex;
		this.matchConfidence = matchConfidence;
		this.score = score;
	}

	public Match(int queryLength, int matchLength, EdamUri edamUri, MatchType matchType, MatchConfidence matchConfidence, double score) {
		this(queryLength, matchLength, edamUri, matchType, -1, matchConfidence, score);
	}

	public EdamUri getEdamUri() {
		return edamUri;
	}

	public MatchType getMatchType() {
		return matchType;
	}

	public int getSynonymIndex() {
		return synonymIndex;
	}

	public MatchConfidence getMatchConfidence() {
		return matchConfidence;
	}

	public double getScore() {
		return score;
	}

	@Override
	public int compareTo(Match m) {
		if (m == null) return 1;

		if (this.score > m.score) return 1;
		if (this.score < m.score) return -1;

		if (this.matchConfidence == MatchConfidence.exact && m.matchConfidence == MatchConfidence.inexact) return 1;
		if (this.matchConfidence == MatchConfidence.inexact && m.matchConfidence == MatchConfidence.exact) return -1;

		switch (this.matchType) {
		case label:
			if (m.matchType != MatchType.label) return 1;
			break;
		case exact_synonym:
			if (m.matchType == MatchType.label) return -1;
			else if (m.matchType != MatchType.exact_synonym) return 1;
			break;
		case narrow_synonym:
		case broad_synonym:
			if (m.matchType == MatchType.label || m.matchType == MatchType.exact_synonym) return -1;
			else if (m.matchType == MatchType.definition || m.matchType == MatchType.comment) return 1;
			break;
		case definition:
			if (m.matchType == MatchType.comment) return 1;
			else if (m.matchType != MatchType.definition) return -1;
			break;
		case comment:
			if (m.matchType != MatchType.comment) return -1;
			break;
		}

		return Math.abs(m.queryLength - m.matchLength) - Math.abs(this.queryLength - this.matchLength);
	}
}
