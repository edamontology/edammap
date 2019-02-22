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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.edamontology.pubfetcher.core.db.publication.PublicationIds;

public class Suggestion implements Comparable<Suggestion> {

	private static final double SCORE_MIN = 1000;

	private static final double SCORE2_MIN = 1072.1;

	private static final double SCORE2_LOW_CONFIDENCE = 1750;

	private double score = 0;

	private double score2 = -1;

	private Double[] score2Parts = { 0d, 0d, 0d, 0d };

	private String extracted = "";

	private String processed = "";

	private List<Integer> publicationAndNameExisting = null;

	private List<Integer> nameExistingSomePublicationDifferent = null;

	private List<List<PublicationIds>> nameExistingSomePublicationDifferentPublicationIds = null;

	private List<Integer> somePublicationExistingNameDifferent = null;

	private List<List<PublicationIds>> somePublicationExistingNameDifferentPublicationIds = null;

	private List<Integer> nameExistingPublicationDifferent = null;

	private List<List<PublicationIds>> nameExistingPublicationDifferentPublicationIds = null;

	private List<String> linksAbstract = new ArrayList<>();

	private List<String> linksFulltext = new ArrayList<>();

	private String homepage = "";

	private boolean homepageBroken = false;

	private boolean homepageMissing = false;

	private Set<BiotoolsLink> linkLinks = new LinkedHashSet<>();

	private Set<BiotoolsLink> downloadLinks = new LinkedHashSet<>();

	private Set<BiotoolsLink> documentationLinks = new LinkedHashSet<>();

	private Set<BiotoolsLink> brokenLinks = new LinkedHashSet<>();

	private boolean fromAbstractLink = false;

	public boolean calculateScore2() {
		return score < SCORE_MIN;
	}

	public boolean include() {
		return score >= SCORE_MIN || score2 >= SCORE2_MIN;
	}

	public boolean lowConfidence() {
		return score < SCORE_MIN && score2 >= SCORE2_MIN && score2 <= SCORE2_LOW_CONFIDENCE;
	}

	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}

	public double getScore2() {
		return score2;
	}
	public void setScore2(double score2) {
		this.score2 = score2;
	}

	public Double[] getScore2Parts() {
		return score2Parts;
	}

	public String getExtracted() {
		return extracted;
	}
	public void setExtracted(String extracted) {
		this.extracted = extracted;
	}

	public String getProcessed() {
		return processed;
	}
	public void setProcessed(String processed) {
		this.processed = processed;
	}

	public List<Integer> getPublicationAndNameExisting() {
		return publicationAndNameExisting;
	}
	public void setPublicationAndNameExisting(List<Integer> publicationAndNameExisting) {
		this.publicationAndNameExisting = publicationAndNameExisting;
	}

	public List<Integer> getNameExistingSomePublicationDifferent() {
		return nameExistingSomePublicationDifferent;
	}
	public void setNameExistingSomePublicationDifferent(List<Integer> nameExistingSomePublicationDifferent) {
		this.nameExistingSomePublicationDifferent = nameExistingSomePublicationDifferent;
	}

	public List<List<PublicationIds>> getNameExistingSomePublicationDifferentPublicationIds() {
		return nameExistingSomePublicationDifferentPublicationIds;
	}
	public void setNameExistingSomePublicationDifferentPublicationIds(List<List<PublicationIds>> nameExistingSomePublicationDifferentPublicationIds) {
		this.nameExistingSomePublicationDifferentPublicationIds = nameExistingSomePublicationDifferentPublicationIds;
	}

	public List<Integer> getSomePublicationExistingNameDifferent() {
		return somePublicationExistingNameDifferent;
	}
	public void setSomePublicationExistingNameDifferent(List<Integer> somePublicationExistingNameDifferent) {
		this.somePublicationExistingNameDifferent = somePublicationExistingNameDifferent;
	}

	public List<List<PublicationIds>> getSomePublicationExistingNameDifferentPublicationIds() {
		return somePublicationExistingNameDifferentPublicationIds;
	}
	public void setSomePublicationExistingNameDifferentPublicationIds(List<List<PublicationIds>> somePublicationExistingNameDifferentPublicationIds) {
		this.somePublicationExistingNameDifferentPublicationIds = somePublicationExistingNameDifferentPublicationIds;
	}

	public List<Integer> getNameExistingPublicationDifferent() {
		return nameExistingPublicationDifferent;
	}
	public void setNameExistingPublicationDifferent(List<Integer> nameExistingPublicationDifferent) {
		this.nameExistingPublicationDifferent = nameExistingPublicationDifferent;
	}

	public List<List<PublicationIds>> getNameExistingPublicationDifferentPublicationIds() {
		return nameExistingPublicationDifferentPublicationIds;
	}
	public void setNameExistingPublicationDifferentPublicationIds(List<List<PublicationIds>> nameExistingPublicationDifferentPublicationIds) {
		this.nameExistingPublicationDifferentPublicationIds = nameExistingPublicationDifferentPublicationIds;
	}

	public List<String> getLinksAbstract() {
		return linksAbstract;
	}
	public void setLinksAbstract(List<String> linksAbstract) {
		this.linksAbstract = linksAbstract;
	}
	public void addLinkAbstract(String linkAbstract) {
		linksAbstract.add(linkAbstract);
	}
	public void addLinksAbstract(List<String> linksAbstract) {
		this.linksAbstract.addAll(linksAbstract);
	}

	public List<String> getLinksFulltext() {
		return linksFulltext;
	}
	public void setLinksFulltext(List<String> linksFulltext) {
		this.linksFulltext = linksFulltext;
	}
	public void addLinkFulltext(String linkFulltext) {
		linksFulltext.add(linkFulltext);
	}
	public void addLinksFulltext(List<String> linksFulltext) {
		this.linksFulltext.addAll(linksFulltext);
	}

	public String getHomepage() {
		return homepage;
	}
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public boolean isHomepageBroken() {
		return homepageBroken;
	}
	public void setHomepageBroken(boolean homepageBroken) {
		this.homepageBroken = homepageBroken;
	}

	public boolean isHomepageMissing() {
		return homepageMissing;
	}
	public void setHomepageMissing(boolean homepageMissing) {
		this.homepageMissing = homepageMissing;
	}

	public Set<BiotoolsLink> getLinkLinks() {
		return linkLinks;
	}
	public void addLinkLinks(List<BiotoolsLink> linkLinks) {
		this.linkLinks.addAll(linkLinks);
	}

	public Set<BiotoolsLink> getDownloadLinks() {
		return downloadLinks;
	}
	public void addDownloadLinks(List<BiotoolsLink> downloadLinks) {
		this.downloadLinks.addAll(downloadLinks);
	}

	public Set<BiotoolsLink> getDocumentationLinks() {
		return documentationLinks;
	}
	public void addDocumentationLinks(List<BiotoolsLink> documentationLinks) {
		this.documentationLinks.addAll(documentationLinks);
	}

	public void removeHomepageFromLinks() {
		String homepageTrimmed = PubMedApps.trimUrl(homepage);
		linkLinks.removeIf(l -> l.getUrlTrimmed().equals(homepageTrimmed));
		downloadLinks.removeIf(l -> l.getUrlTrimmed().equals(homepageTrimmed));
		documentationLinks.removeIf(l -> l.getUrlTrimmed().equals(homepageTrimmed));
	}

	public Set<BiotoolsLink> getBrokenLinks() {
		return brokenLinks;
	}

	public boolean isFromAbstractLink() {
		return fromAbstractLink;
	}
	public void setFromAbstractLink(boolean fromAbstractLink) {
		this.fromAbstractLink = fromAbstractLink;
	}

	@Override
	public int compareTo(Suggestion o) {
		if (o == null) return -1;
		if (this.score < SCORE_MIN && o.score < SCORE_MIN) {
			if (this.score2 > o.score2) return -1;
			if (this.score2 < o.score2) return 1;
		} else {
			if (this.score > o.score) return -1;
			if (this.score < o.score) return 1;
		}
		return 0;
	}
}
