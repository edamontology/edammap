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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Result {
	private String pmid = "";

	private String pmcid = "";

	private String doi = "";

	private double score = 0;

	private String suggestion = "";

	private Set<String> links = new LinkedHashSet<>();

	private Set<String> docs = new LinkedHashSet<>();

	private List<String> sameSuggestions = new ArrayList<>();

	private List<String> otherSuggestions = new ArrayList<>();

	private Set<String> otherLinks = new LinkedHashSet<>();

	private Set<String> leftoverLinks = new LinkedHashSet<>();

	private Map<String, String> existingNames = new LinkedHashMap<>();

	private Map<String, String> possiblyExisting = new LinkedHashMap<>();

	private Set<String> newLinks = new LinkedHashSet<>();

	public String getPmid() {
		return pmid;
	}
	public void setPmid(String pmid) {
		this.pmid = pmid;
	}

	public String getPmcid() {
		return pmcid;
	}
	public void setPmcid(String pmcid) {
		this.pmcid = pmcid;
	}

	public String getDoi() {
		return doi;
	}
	public void setDoi(String doi) {
		this.doi = doi;
	}

	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}

	public String getSuggestion() {
		return suggestion;
	}
	public void setSuggestion(String suggestion) {
		this.suggestion = suggestion;
	}

	public Set<String> getLinks() {
		return links;
	}
	public void addLinks(List<String> links) {
		this.links.addAll(links);
	}
	public void setLinks(Set<String> links) {
		this.links = links;
	}

	public Set<String> getDocs() {
		return docs;
	}
	public void addDoc(String doc) {
		docs.add(doc);
	}

	public List<String> getSameSuggestions() {
		return sameSuggestions;
	}
	public void addSameSuggestion(String sameSuggestion) {
		sameSuggestions.add(sameSuggestion);
	}

	public List<String> getOtherSuggestions() {
		return otherSuggestions;
	}
	public void addOtherSuggestion(String otherSuggestion) {
		otherSuggestions.add(otherSuggestion);
	}

	public Set<String> getOtherLinks() {
		return otherLinks;
	}
	public void addOtherLinks(Collection<String> otherLinks) {
		this.otherLinks.addAll(otherLinks);
	}

	public Set<String> getLeftoverLinks() {
		return leftoverLinks;
	}
	public void addLeftoverLinks(Collection<String> leftoverLinks) {
		this.leftoverLinks.addAll(leftoverLinks);
	}

	public Map<String, String> getExistingNames() {
		return existingNames;
	}
	public void addExistingName(String existingId, String existingName) {
		existingNames.put(existingId, existingName);
	}

	public Map<String, String> getPossiblyExisting() {
		return possiblyExisting;
	}
	public void addPossiblyExisting(String possiblyExistingId, String possiblyExistingName) {
		possiblyExisting.put(possiblyExistingId, possiblyExistingName);
	}

	public Set<String> getNewLinks() {
		return newLinks;
	}
	public void addNewLink(String newLink) {
		newLinks.add(newLink);
	}
}
