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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Suggestion {

	private double score = 0;

	private String extracted = "";

	private String processed = "";

	private List<String> linksAbstract = new ArrayList<>();

	private List<String> linksFulltext = new ArrayList<>();

	private String homepage = "";

	private boolean homepageBroken = false;

	private boolean homepageMissing = false;

	private Set<BiotoolsLink> linkLinks = new LinkedHashSet<>();

	private Set<BiotoolsLink> downloadLinks = new LinkedHashSet<>();

	private Set<BiotoolsLink> documentationLinks = new LinkedHashSet<>();

	private Set<BiotoolsLink> brokenLinks = new LinkedHashSet<>();

	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
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

	public List<String> getLinksAbstract() {
		return linksAbstract;
	}
	public void setLinksAbstract(List<String> linksAbstract) {
		this.linksAbstract = linksAbstract;
	}
	public void addLinkAbstract(String linkAbstract) {
		linksAbstract.add(linkAbstract);
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

	public List<String> getLinks() {
		List<String> links = new ArrayList<>();
		links.addAll(linksAbstract);
		links.addAll(linksFulltext);
		return links;
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
}
