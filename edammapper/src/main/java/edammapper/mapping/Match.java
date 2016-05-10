package edammapper.mapping;

import edammapper.edam.EdamUri;

public class Match implements Comparable<Match> {

	private double score;

	private double bestOneScore = 0;

	private final ConceptMatch conceptMatch;

	private final QueryMatch queryMatch;

	private EdamUri edamUri;

	Match(double score, ConceptMatch conceptMatch, QueryMatch queryMatch) {
		this.score = score;
		this.conceptMatch = conceptMatch;
		this.queryMatch = queryMatch;
	}

	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}

	public double getBestOneScore() {
		return bestOneScore;
	}
	public void setBestOneScore(double bestOneScore) {
		this.bestOneScore = bestOneScore;
	}

	public ConceptMatch getConceptMatch() {
		return conceptMatch;
	}

	public QueryMatch getQueryMatch() {
		return queryMatch;
	}

	public EdamUri getEdamUri() {
		return edamUri;
	}
	public void setEdamUri(EdamUri edamUri) {
		this.edamUri = edamUri;
	}

	@Override
	public int compareTo(Match m) {
		if (m == null) return 1;

		if (this.score > m.score) return 1;
		if (this.score < m.score) return -1;

		switch (this.conceptMatch.getType()) {
		case label:
			if (m.conceptMatch.getType() != ConceptMatchType.label) return 1;
			break;
		case exact_synonym:
			if (m.conceptMatch.getType() == ConceptMatchType.label) return -1;
			else if (m.conceptMatch.getType() != ConceptMatchType.exact_synonym) return 1;
			break;
		case narrow_synonym:
		case broad_synonym:
			if (m.conceptMatch.getType() == ConceptMatchType.label || m.conceptMatch.getType() == ConceptMatchType.exact_synonym) return -1;
			else if (m.conceptMatch.getType() == ConceptMatchType.definition || m.conceptMatch.getType() == ConceptMatchType.comment || m.conceptMatch.getType() == ConceptMatchType.none) return 1;
			break;
		case definition:
			if (m.conceptMatch.getType() == ConceptMatchType.comment || m.conceptMatch.getType() == ConceptMatchType.none) return 1;
			else if (m.conceptMatch.getType() != ConceptMatchType.definition) return -1;
			break;
		case comment:
			if (m.conceptMatch.getType() == ConceptMatchType.none) return 1;
			else if (m.conceptMatch.getType() != ConceptMatchType.comment) return -1;
			break;
		case none:
			if (m.conceptMatch.getType() != ConceptMatchType.none) return -1;
			break;
		}

		return 0;
	}
}
