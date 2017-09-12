package edammapper.mapping;

import java.util.ArrayList;
import java.util.List;

import edammapper.edam.EdamUri;

public class Match implements Comparable<Match> {

	private double score;

	private double bestOneScore = 0;

	private double withoutPathScore = 0;

	private final ConceptMatch conceptMatch;

	private final QueryMatch queryMatch;

	private EdamUri edamUri;

	private boolean removed = false;

	private boolean existingAnnotation = false;

	private List<EdamUri> parents = new ArrayList<>();
	private List<EdamUri> children = new ArrayList<>();
	private List<EdamUri> parentsAnnotation = new ArrayList<>();
	private List<EdamUri> childrenAnnotation = new ArrayList<>();
	private List<EdamUri> parentsRemainingAnnotation = new ArrayList<>();
	private List<EdamUri> childrenRemainingAnnotation = new ArrayList<>();

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

	public double getWithoutPathScore() {
		return withoutPathScore;
	}
	public void setWithoutPathScore(double withoutPathScore) {
		this.withoutPathScore = withoutPathScore;
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

	public boolean isRemoved() {
		return removed;
	}
	public void setRemoved(boolean removed) {
		this.removed = removed;
	}

	public boolean isExistingAnnotation() {
		return existingAnnotation;
	}
	public void setExistingAnnotation(boolean existingAnnotation) {
		this.existingAnnotation = existingAnnotation;
	}

	public List<EdamUri> getParents() {
		return parents;
	}
	public void addParent(EdamUri parent) {
		parents.add(parent);
	}

	public List<EdamUri> getChildren() {
		return children;
	}
	public void addChild(EdamUri child) {
		children.add(child);
	}

	public List<EdamUri> getParentsAnnotation() {
		return parentsAnnotation;
	}
	public void addParentAnnotation(EdamUri parentAnnotation) {
		parentsAnnotation.add(parentAnnotation);
	}

	public List<EdamUri> getChildrenAnnotation() {
		return childrenAnnotation;
	}
	public void addChildAnnotation(EdamUri childAnnotation) {
		childrenAnnotation.add(childAnnotation);
	}

	public List<EdamUri> getParentsRemainingAnnotation() {
		return parentsRemainingAnnotation;
	}
	public void addParentRemainingAnnotation(EdamUri parentRemainingAnnotation) {
		parentsRemainingAnnotation.add(parentRemainingAnnotation);
	}

	public List<EdamUri> getChildrenRemainingAnnotation() {
		return childrenRemainingAnnotation;
	}
	public void addChildRemainingAnnotation(EdamUri childRemainingAnnotation) {
		childrenRemainingAnnotation.add(childRemainingAnnotation);
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
