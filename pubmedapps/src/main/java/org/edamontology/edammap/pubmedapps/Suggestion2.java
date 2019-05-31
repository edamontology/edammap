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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.edamontology.edammap.core.input.json.DocumentationType;
import org.edamontology.edammap.core.input.json.DownloadType;
import org.edamontology.edammap.core.input.json.LinkType;

public class Suggestion2 extends Suggestion1 {

	private static final double SCORE_MIN = 1000;

	private static final double SCORE2_MIN = 1072.1;

	private static final double SCORE2_LOW_CONFIDENCE = 1750;

	private double score2 = -1;

	private Double[] score2Parts = { 0d, 0d, 0d, 0d };

	private List<Integer> publicationAndNameExisting = null;

	private List<Integer> nameExistingSomePublicationDifferent = null;

	private List<Set<PubIds>> nameExistingSomePublicationDifferentPubIds = null;

	private List<Integer> somePublicationExistingNameDifferent = null;

	private List<Set<PubIds>> somePublicationExistingNameDifferentPubIds = null;

	private List<Integer> nameExistingPublicationDifferent = null;

	private List<Set<PubIds>> nameExistingPublicationDifferentPubIds = null;

	private String homepage = "";

	private boolean homepageBroken = false;

	private boolean homepageMissing = false;

	private Set<BiotoolsLink<LinkType>> linkLinks = new LinkedHashSet<>();

	private Set<BiotoolsLink<DownloadType>> downloadLinks = new LinkedHashSet<>();

	private Set<BiotoolsLink<DocumentationType>> documentationLinks = new LinkedHashSet<>();

	private Set<BiotoolsLink<?>> brokenLinks = new LinkedHashSet<>();

	public Suggestion2(Suggestion1 suggestion1) {
		setScore(suggestion1.getScore());
		setOriginal(suggestion1.getOriginal());
		setExtracted(suggestion1.getExtracted());
		setProcessed(suggestion1.getProcessed());
		setLinksAbstract(suggestion1.getLinksAbstract());
		setLinksFulltext(suggestion1.getLinksFulltext());
		setFromAbstractLink(suggestion1.isFromAbstractLink());
	}

	public boolean calculateScore2() {
		return score < SCORE_MIN;
	}

	public boolean include() {
		return score >= SCORE_MIN || score2 >= SCORE2_MIN;
	}

	public boolean lowConfidence() {
		return score < SCORE_MIN && score2 >= SCORE2_MIN && score2 <= SCORE2_LOW_CONFIDENCE;
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

	public List<Set<PubIds>> getNameExistingSomePublicationDifferentPubIds() {
		return nameExistingSomePublicationDifferentPubIds;
	}
	public void setNameExistingSomePublicationDifferentPubIds(List<Set<PubIds>> nameExistingSomePublicationDifferentPubIds) {
		this.nameExistingSomePublicationDifferentPubIds = nameExistingSomePublicationDifferentPubIds;
	}

	public List<Integer> getSomePublicationExistingNameDifferent() {
		return somePublicationExistingNameDifferent;
	}
	public void setSomePublicationExistingNameDifferent(List<Integer> somePublicationExistingNameDifferent) {
		this.somePublicationExistingNameDifferent = somePublicationExistingNameDifferent;
	}

	public List<Set<PubIds>> getSomePublicationExistingNameDifferentPubIds() {
		return somePublicationExistingNameDifferentPubIds;
	}
	public void setSomePublicationExistingNameDifferentPubIds(List<Set<PubIds>> somePublicationExistingNameDifferentPubIds) {
		this.somePublicationExistingNameDifferentPubIds = somePublicationExistingNameDifferentPubIds;
	}

	public List<Integer> getNameExistingPublicationDifferent() {
		return nameExistingPublicationDifferent;
	}
	public void setNameExistingPublicationDifferent(List<Integer> nameExistingPublicationDifferent) {
		this.nameExistingPublicationDifferent = nameExistingPublicationDifferent;
	}

	public List<Set<PubIds>> getNameExistingPublicationDifferentPubIds() {
		return nameExistingPublicationDifferentPubIds;
	}
	public void setNameExistingPublicationDifferentPubIds(List<Set<PubIds>> nameExistingPublicationDifferentPubIds) {
		this.nameExistingPublicationDifferentPubIds = nameExistingPublicationDifferentPubIds;
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

	public Set<BiotoolsLink<LinkType>> getLinkLinks() {
		return linkLinks;
	}
	public void addLinkLinks(List<BiotoolsLink<LinkType>> linkLinks) {
		this.linkLinks.addAll(linkLinks);
	}

	public Set<BiotoolsLink<DownloadType>> getDownloadLinks() {
		return downloadLinks;
	}
	public void addDownloadLinks(List<BiotoolsLink<DownloadType>> downloadLinks) {
		this.downloadLinks.addAll(downloadLinks);
	}

	public Set<BiotoolsLink<DocumentationType>> getDocumentationLinks() {
		return documentationLinks;
	}
	public void addDocumentationLinks(List<BiotoolsLink<DocumentationType>> documentationLinks) {
		this.documentationLinks.addAll(documentationLinks);
	}

	public void removeHomepageFromLinks() {
		String homepageTrimmed = Common.trimUrl(homepage);
		linkLinks.removeIf(l -> l.getUrlTrimmed().equals(homepageTrimmed));
		downloadLinks.removeIf(l -> l.getUrlTrimmed().equals(homepageTrimmed));
		documentationLinks.removeIf(l -> l.getUrlTrimmed().equals(homepageTrimmed));
	}

	public Set<BiotoolsLink<?>> getBrokenLinks() {
		return brokenLinks;
	}

	@Override
	public int compareTo(Suggestion1 o) {
		if (o == null) return -1;
		if (o instanceof Suggestion2) {
			if (this.score < SCORE_MIN && o.score < SCORE_MIN) {
				if (this.score2 > ((Suggestion2) o).score2) return -1;
				if (this.score2 < ((Suggestion2) o).score2) return 1;
			} else {
				if (this.score > o.score) return -1;
				if (this.score < o.score) return 1;
			}
		} else {
			if (this.score > o.score) return -1;
			if (this.score < o.score) return 1;
		}
		return 0;
	}
}
