/*
 * Copyright © 2018 Erik Jaaniso
 *
 * This file is part of PubMedApps.
 *
 * PubMedApps is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PubMedApps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PubMedApps.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.edamontology.edammap.pubmedapps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.edamontology.pubfetcher.core.db.publication.PublicationIds;

public class Result implements Comparable<Result> {

	private List<Suggestion> suggestions = new ArrayList<>();

	private List<PublicationIds> sameSuggestions = new ArrayList<>();

	private List<String> leftoverLinksAbstract = new ArrayList<>();

	private List<String> leftoverLinksFulltext = new ArrayList<>();

	private List<Integer> existing = new ArrayList<>();

	private List<Integer> possiblyExisting = new ArrayList<>();

	private List<Integer> possiblyRelated = new ArrayList<>();

	private String title = "";

	private String toolTitle = "";

	private String toolTitleTwo = "";

	private String toolTitleAcronym = "";

	private String toolTitleTwoAcronym = "";

	private String toolTitlePruned = "";

	private String toolTitleTwoPruned = "";

	private boolean oa = false;

	private String journalTitle = "";

	private String pubDate = "";

	private int citationsCount = -1;

	private String citationsTimestamp = "";

	private boolean hasSuggestionLink(String link) {
		for (Suggestion suggestion : suggestions) {
			if (suggestion.getLinksAbstract().contains(link) || suggestion.getLinksFulltext().contains(link)) {
				return true;
			}
		}
		return false;
	}

	private boolean removePossiblyExisting(Integer index) {
		Iterator<Integer> it = possiblyExisting.iterator();
		while (it.hasNext()) {
			if (index.equals(it.next())) {
				it.remove();
				return true;
			}
		}
		return false;
	}

	private boolean removePossiblyRelated(Integer index) {
		Iterator<Integer> it = possiblyRelated.iterator();
		while (it.hasNext()) {
			if (index.equals(it.next())) {
				it.remove();
				return true;
			}
		}
		return false;
	}

	public List<Suggestion> getSuggestions() {
		return suggestions;
	}
	public void addSuggestion(Suggestion suggestion) {
		suggestions.add(suggestion);
	}

	public List<PublicationIds> getSameSuggestions() {
		return sameSuggestions;
	}
	public void addSameSuggestion(PublicationIds sameSuggestion) {
		sameSuggestions.add(sameSuggestion);
	}

	public List<String> getLeftoverLinksAbstract() {
		return leftoverLinksAbstract;
	}
	public void addLeftoverLinksAbstract(List<String> linksAbstract) {
		for (String link : linksAbstract) {
			if (!hasSuggestionLink(link)) {
				leftoverLinksAbstract.add(link);
			}
		}
	}

	public List<String> getLeftoverLinksFulltext() {
		return leftoverLinksFulltext;
	}
	public void addLeftoverLinksFulltext(List<String> linksFulltext) {
		for (String link : linksFulltext) {
			if (!hasSuggestionLink(link)) {
				leftoverLinksFulltext.add(link);
			}
		}
	}

	public List<Integer> getExisting() {
		return existing;
	}
	public void addExisting(Integer index) {
		if (!existing.contains(index)) {
			existing.add(index);
			removePossiblyExisting(index);
			removePossiblyRelated(index);
		}
	}

	public List<Integer> getPossiblyExisting() {
		return possiblyExisting;
	}
	public void addPossiblyExisting(Integer index) {
		if (!existing.contains(index) && !possiblyExisting.contains(index)) {
			possiblyExisting.add(index);
			removePossiblyRelated(index);
		}
	}

	public List<Integer> getPossiblyRelated() {
		return possiblyRelated;
	}
	public void addPossiblyRelated(Integer index) {
		if (!existing.contains(index) && !possiblyExisting.contains(index) && !possiblyRelated.contains(index)) {
			possiblyRelated.add(index);
		}
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getToolTitle() {
		return toolTitle;
	}
	public void setToolTitle(String toolTitle) {
		this.toolTitle = toolTitle;
	}

	public String getToolTitleTwo() {
		return toolTitleTwo;
	}
	public void setToolTitleTwo(String toolTitleTwo) {
		this.toolTitleTwo = toolTitleTwo;
	}

	public String getToolTitleAcronym() {
		return toolTitleAcronym;
	}
	public void setToolTitleAcronym(String toolTitleAcronym) {
		this.toolTitleAcronym = toolTitleAcronym;
	}

	public String getToolTitleTwoAcronym() {
		return toolTitleTwoAcronym;
	}
	public void setToolTitleTwoAcronym(String toolTitleTwoAcronym) {
		this.toolTitleTwoAcronym = toolTitleTwoAcronym;
	}

	public String getToolTitlePruned() {
		return toolTitlePruned;
	}
	public void setToolTitlePruned(String toolTitlePruned) {
		this.toolTitlePruned = toolTitlePruned;
	}

	public String getToolTitleTwoPruned() {
		return toolTitleTwoPruned;
	}
	public void setToolTitleTwoPruned(String toolTitleTwoPruned) {
		this.toolTitleTwoPruned = toolTitleTwoPruned;
	}

	public boolean isOa() {
		return oa;
	}
	public void setOa(boolean oa) {
		this.oa = oa;
	}

	public String getJournalTitle() {
		return journalTitle;
	}
	public void setJournalTitle(String journalTitle) {
		this.journalTitle = journalTitle;
	}

	public String getPubDate() {
		return pubDate;
	}
	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	public int getCitationsCount() {
		return citationsCount;
	}
	public void setCitationsCount(int citationsCount) {
		this.citationsCount = citationsCount;
	}

	public String getCitationsTimestamp() {
		return citationsTimestamp;
	}
	public void setCitationsTimestamp(String citationsTimestamp) {
		this.citationsTimestamp = citationsTimestamp;
	}

	@Override
	public int compareTo(Result o) {
		if (o == null) return -1;
		if (o.suggestions.isEmpty() && this.suggestions.isEmpty()) return 0;
		if (o.suggestions.isEmpty()) return -1;
		if (this.suggestions.isEmpty()) return 1;
		if (this.suggestions.get(0).getScore() > o.suggestions.get(0).getScore()) return -1;
		if (this.suggestions.get(0).getScore() < o.suggestions.get(0).getScore()) return 1;
		return 0;
	}
}
