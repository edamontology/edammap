/*
 * Copyright © 2016, 2017 Erik Jaaniso
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

package org.edamontology.edammap.core.mapping;

import java.util.ArrayList;
import java.util.List;

import org.edamontology.edammap.core.edam.EdamUri;

public class Match implements Comparable<Match> {

	private double score;

	private double bestOneScore = -1;

	private double withoutPathScore = -1;

	private final ConceptMatch conceptMatch;

	private final QueryMatch queryMatch;

	private List<MatchAverageStats> matchAverageStats = null;

	private EdamUri edamUri = null;

	private EdamUri edamUriReplaced = null;

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

	public List<MatchAverageStats> getMatchAverageStats() {
		return matchAverageStats;
	}
	public void setMatchAverageStats(List<MatchAverageStats> matchAverageStats) {
		this.matchAverageStats = matchAverageStats;
	}

	public EdamUri getEdamUri() {
		return edamUri;
	}
	public void setEdamUri(EdamUri edamUri) {
		if (edamUriReplaced == null) {
			edamUriReplaced = this.edamUri;
		}
		this.edamUri = edamUri;
	}

	public EdamUri getEdamUriReplaced() {
		return edamUriReplaced;
	}
	public EdamUri getEdamUriOriginal() {
		return edamUriReplaced != null ? edamUriReplaced : edamUri;
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
