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

public class Result2 implements Comparable<Result2> {

	private List<PubIds> pubIds = new ArrayList<>();

	private List<PubIds> sameSuggestions = new ArrayList<>();

	private List<Suggestion2> suggestions = new ArrayList<>();

	private List<List<String>> leftoverLinksAbstract = new ArrayList<>();

	private List<List<String>> leftoverLinksFulltext = new ArrayList<>();

	private List<Integer> nameMatch = new ArrayList<>();

	private List<Integer> linkMatch = new ArrayList<>();

	private List<List<String>> linkMatchLinks = new ArrayList<>();

	private List<Integer> nameWordMatch = new ArrayList<>();

	private List<String> title = new ArrayList<>();

	private List<List<String>> toolTitleOthers = new ArrayList<>();

	private List<String> toolTitleExtractedOriginal = new ArrayList<>();

	private List<String> toolTitle = new ArrayList<>();

	private List<String> toolTitlePruned = new ArrayList<>();

	private List<String> toolTitleAcronym = new ArrayList<>();

	private List<List<String>> abstractSentences = new ArrayList<>();

	private List<Boolean> oa = new ArrayList<>();

	private List<String> journalTitle = new ArrayList<>();

	private List<Long> pubDate = new ArrayList<>();

	private List<String> pubDateHuman = new ArrayList<>();

	private List<Integer> citationsCount = new ArrayList<>();

	private List<Long> citationsTimestamp = new ArrayList<>();

	private List<String> citationsTimestampHuman = new ArrayList<>();

	private List<List<CorrespAuthor>> correspAuthor = new ArrayList<>();

	public Result2(Result1 result1) {
		pubIds.add(result1.getPubIds());
		for (Suggestion1 suggestion : result1.getSuggestions()) {
			suggestions.add(new Suggestion2(suggestion));
		}
		leftoverLinksAbstract.add(result1.getLeftoverLinksAbstract());
		leftoverLinksFulltext.add(result1.getLeftoverLinksFulltext());
		title.add(result1.getTitle());
		toolTitleOthers.add(result1.getToolTitleOthers());
		toolTitleExtractedOriginal.add(result1.getToolTitleExtractedOriginal());
		toolTitle.add(result1.getToolTitle());
		toolTitlePruned.add(result1.getToolTitlePruned());
		toolTitleAcronym.add(result1.getToolTitleAcronym());
		abstractSentences.add(result1.getAbstractSentences());
		oa.add(result1.isOa());
		journalTitle.add(result1.getJournalTitle());
		pubDate.add(result1.getPubDate());
		pubDateHuman.add(result1.getPubDateHuman());
		citationsCount.add(result1.getCitationsCount());
		citationsTimestamp.add(result1.getCitationsTimestamp());
		citationsTimestampHuman.add(result1.getCitationsTimestampHuman());
		correspAuthor.add(result1.getCorrespAuthor());
	}

	public List<PubIds> getPubIds() {
		return pubIds;
	}
	public void addPubIds(PubIds pubIds) {
		this.pubIds.add(pubIds);
	}

	public List<PubIds> getSameSuggestions() {
		return sameSuggestions;
	}
	public void addSameSuggestion(PubIds sameSuggestion) {
		sameSuggestions.add(sameSuggestion);
	}

	public List<Suggestion2> getSuggestions() {
		return suggestions;
	}
	public void addSuggestion(Suggestion2 suggestion) {
		suggestions.add(suggestion);
	}

	public List<List<String>> getLeftoverLinksAbstract() {
		return leftoverLinksAbstract;
	}
	public void addLeftoverLinksAbstract(List<String> leftoverLinksAbstract) {
		this.leftoverLinksAbstract.add(leftoverLinksAbstract);
	}

	public List<List<String>> getLeftoverLinksFulltext() {
		return leftoverLinksFulltext;
	}
	public void addLeftoverLinksFulltext(List<String> leftoverLinksFulltext) {
		this.leftoverLinksFulltext.add(leftoverLinksFulltext);
	}

	public List<Integer> getNameMatch() {
		return nameMatch;
	}
	public void addNameMatch(Integer index) {
		nameMatch.add(index);
	}

	public List<Integer> getLinkMatch() {
		return linkMatch;
	}
	public void addLinkMatch(Integer index, List<String> links) {
		linkMatch.add(index);
		linkMatchLinks.add(links);
	}

	public List<List<String>> getLinkMatchLinks() {
		return linkMatchLinks;
	}

	public List<Integer> getNameWordMatch() {
		return nameWordMatch;
	}
	public void addNameWordMatch(Integer index) {
		nameWordMatch.add(index);
	}

	public List<String> getTitle() {
		return title;
	}
	public void addTitle(String title) {
		this.title.add(title);
	}

	public List<String> getToolTitleExtractedOriginal() {
		return toolTitleExtractedOriginal;
	}
	public void addToolTitleExtractedOriginal(String toolTitleExtractedOriginal) {
		this.toolTitleExtractedOriginal.add(toolTitleExtractedOriginal);
	}

	public List<String> getToolTitle() {
		return toolTitle;
	}
	public void addToolTitle(String toolTitle) {
		this.toolTitle.add(toolTitle);
	}

	public List<List<String>> getToolTitleOthers() {
		return toolTitleOthers;
	}
	public void addToolTitleOthers(List<String> toolTitleOthers) {
		this.toolTitleOthers.add(toolTitleOthers);
	}

	public List<String> getToolTitlePruned() {
		return toolTitlePruned;
	}
	public void addToolTitlePruned(String toolTitlePruned) {
		this.toolTitlePruned.add(toolTitlePruned);
	}

	public List<String> getToolTitleAcronym() {
		return toolTitleAcronym;
	}
	public void addToolTitleAcronym(String toolTitleAcronym) {
		this.toolTitleAcronym.add(toolTitleAcronym);
	}

	public List<List<String>> getAbstractSentences() {
		return abstractSentences;
	}
	public void addAbstractSentences(List<String> abstractSentences) {
		this.abstractSentences.add(abstractSentences);
	}

	public List<Boolean> isOa() {
		return oa;
	}
	public void addOa(boolean oa) {
		this.oa.add(oa);
	}

	public List<String> getJournalTitle() {
		return journalTitle;
	}
	public void addJournalTitle(String journalTitle) {
		this.journalTitle.add(journalTitle);
	}

	public List<Long> getPubDate() {
		return pubDate;
	}
	public void addPubDate(Long pubDate) {
		this.pubDate.add(pubDate);
	}

	public List<String> getPubDateHuman() {
		return pubDateHuman;
	}
	public void addPubDateHuman(String pubDateHuman) {
		this.pubDateHuman.add(pubDateHuman);
	}

	public List<Integer> getCitationsCount() {
		return citationsCount;
	}
	public void addCitationsCount(int citationsCount) {
		this.citationsCount.add(citationsCount);
	}

	public List<Long> getCitationsTimestamp() {
		return citationsTimestamp;
	}
	public void addCitationsTimestamp(Long citationsTimestamp) {
		this.citationsTimestamp.add(citationsTimestamp);
	}

	public List<String> getCitationsTimestampHuman() {
		return citationsTimestampHuman;
	}
	public void addCitationsTimestampHuman(String citationsTimestampHuman) {
		this.citationsTimestampHuman.add(citationsTimestampHuman);
	}

	public List<List<CorrespAuthor>> getCorrespAuthor() {
		return correspAuthor;
	}
	public void addCorrespAuthor(List<CorrespAuthor> correspAuthor) {
		this.correspAuthor.add(correspAuthor);
	}

	@Override
	public int compareTo(Result2 o) {
		if (o == null) return -1;
		if ((o.suggestions.isEmpty() || o.suggestions.get(0) == null) && (this.suggestions.isEmpty() || this.suggestions.get(0) == null)) return 0;
		if (o.suggestions.isEmpty() || o.suggestions.get(0) == null) return -1;
		if (this.suggestions.isEmpty() || this.suggestions.get(0) == null) return 1;
		return this.suggestions.get(0).compareTo(o.suggestions.get(0));
	}
}
