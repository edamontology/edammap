/*
 * Copyright © 2018, 2019 Erik Jaaniso
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
import java.util.List;

import org.edamontology.pubfetcher.core.db.publication.CorrespAuthor;

public class Result1 implements Comparable<Result1> {

	private PubIds pubIds;

	private List<Suggestion1> suggestions = new ArrayList<>();

	private List<String> leftoverLinksAbstract = new ArrayList<>();

	private List<String> leftoverLinksFulltext = new ArrayList<>();

	private String title;

	private List<String> toolTitleOthers = new ArrayList<>();

	private String toolTitleExtractedOriginal;

	private String toolTitle;

	private String toolTitlePruned;

	private String toolTitleAcronym;

	private List<String> abstractSentences = new ArrayList<>();

	private boolean oa;

	private String journalTitle;

	private long pubDate;

	private String pubDateHuman;

	private int citationsCount;

	private long citationsTimestamp;

	private String citationsTimestampHuman;

	private List<CorrespAuthor> correspAuthor = new ArrayList<>();

	private boolean hasSuggestionLink(String link) {
		for (Suggestion1 suggestion : suggestions) {
			if (suggestion.getLinksAbstract().contains(link) || suggestion.getLinksFulltext().contains(link)) {
				return true;
			}
		}
		return false;
	}

	public PubIds getPubIds() {
		return pubIds;
	}
	public void setPubIds(PubIds pubIds) {
		this.pubIds = pubIds;
	}

	public List<Suggestion1> getSuggestions() {
		return suggestions;
	}
	public void setSuggestions(List<Suggestion1> suggestions) {
		this.suggestions = suggestions;
	}
	public void addSuggestion(Suggestion1 suggestion) {
		suggestions.add(suggestion);
	}

	public List<String> getLeftoverLinksAbstract() {
		return leftoverLinksAbstract;
	}
	public void setLeftoverLinksAbstract(List<String> linksAbstract) {
		List<String> leftoverLinksAbstract = new ArrayList<>();
		for (String link : linksAbstract) {
			if (!hasSuggestionLink(link)) {
				leftoverLinksAbstract.add(link);
			}
		}
		this.leftoverLinksAbstract = leftoverLinksAbstract;
	}

	public List<String> getLeftoverLinksFulltext() {
		return leftoverLinksFulltext;
	}
	public void setLeftoverLinksFulltext(List<String> linksFulltext) {
		List<String> leftoverLinksFulltext = new ArrayList<>();
		for (String link : linksFulltext) {
			if (!hasSuggestionLink(link)) {
				leftoverLinksFulltext.add(link);
			}
		}
		this.leftoverLinksFulltext = leftoverLinksFulltext;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getToolTitleOthers() {
		return toolTitleOthers;
	}
	public void setToolTitleOthers(List<String> toolTitleOthers) {
		this.toolTitleOthers = toolTitleOthers;
	}

	public String getToolTitleExtractedOriginal() {
		return toolTitleExtractedOriginal;
	}
	public void setToolTitleExtractedOriginal(String toolTitleExtractedOriginal) {
		this.toolTitleExtractedOriginal = toolTitleExtractedOriginal;
	}

	public String getToolTitle() {
		return toolTitle;
	}
	public void setToolTitle(String toolTitle) {
		this.toolTitle = toolTitle;
	}

	public String getToolTitlePruned() {
		return toolTitlePruned;
	}
	public void setToolTitlePruned(String toolTitlePruned) {
		this.toolTitlePruned = toolTitlePruned;
	}

	public String getToolTitleAcronym() {
		return toolTitleAcronym;
	}
	public void setToolTitleAcronym(String toolTitleAcronym) {
		this.toolTitleAcronym = toolTitleAcronym;
	}

	public List<String> getAbstractSentences() {
		return abstractSentences;
	}
	public void setAbstractSentences(List<String> abstractSentences) {
		this.abstractSentences = abstractSentences;
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

	public long getPubDate() {
		return pubDate;
	}
	public void setPubDate(long pubDate) {
		this.pubDate = pubDate;
	}

	public String getPubDateHuman() {
		return pubDateHuman;
	}
	public void setPubDateHuman(String pubDateHuman) {
		this.pubDateHuman = pubDateHuman;
	}

	public int getCitationsCount() {
		return citationsCount;
	}
	public void setCitationsCount(int citationsCount) {
		this.citationsCount = citationsCount;
	}

	public long getCitationsTimestamp() {
		return citationsTimestamp;
	}
	public void setCitationsTimestamp(long citationsTimestamp) {
		this.citationsTimestamp = citationsTimestamp;
	}

	public String getCitationsTimestampHuman() {
		return citationsTimestampHuman;
	}
	public void setCitationsTimestampHuman(String citationsTimestampHuman) {
		this.citationsTimestampHuman = citationsTimestampHuman;
	}

	public List<CorrespAuthor> getCorrespAuthor() {
		return correspAuthor;
	}
	public void setCorrespAuthor(List<CorrespAuthor> correspAuthor) {
		this.correspAuthor = correspAuthor;
	}

	@Override
	public int compareTo(Result1 o) {
		if (o == null) return -1;
		if ((o.suggestions.isEmpty() || o.suggestions.get(0) == null) && (this.suggestions.isEmpty() || this.suggestions.get(0) == null)) return 0;
		if (o.suggestions.isEmpty() || o.suggestions.get(0) == null) return -1;
		if (this.suggestions.isEmpty() || this.suggestions.get(0) == null) return 1;
		return this.suggestions.get(0).compareTo(o.suggestions.get(0));
	}
}
