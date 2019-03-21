/*
 * Copyright © 2019 Erik Jaaniso
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

import org.edamontology.edammap.core.input.json.DocumentationType;
import org.edamontology.edammap.core.input.json.DownloadType;
import org.edamontology.edammap.core.input.json.LinkType;
import org.edamontology.pubfetcher.core.db.publication.CorrespAuthor;
import org.edamontology.pubfetcher.core.db.publication.PublicationIds;

public class Diff {

	private double scoreScore2 = -1;

	private Set<Integer> possiblyRelated = null;

	private int existing = -1;

	private Set<PublicationIds> modifyPublications = new LinkedHashSet<>();

	private Set<PublicationIds> addPublications = null;

	private String modifyName = null;

	private String modifyHomepage = null;

	private Set<BiotoolsLink<LinkType>> addLinks = new LinkedHashSet<>();

	private Set<BiotoolsLink<DownloadType>> addDownloads = new LinkedHashSet<>();

	private Set<BiotoolsLink<DocumentationType>> addDocumentations = new LinkedHashSet<>();

	private Provenance modifyLicense = null;

	private Set<Provenance> addLanguages = new LinkedHashSet<>();

	private List<CorrespAuthor> modifyCredits = new ArrayList<>();

	private List<CorrespAuthor> addCredits = new ArrayList<>();

	public boolean include() {
		return possiblyRelated != null && !possiblyRelated.isEmpty()
			|| !modifyPublications.isEmpty() || addPublications != null && !addPublications.isEmpty()
			|| modifyName != null && !modifyName.isEmpty() || modifyHomepage != null && !modifyHomepage.isEmpty()
			|| !addLinks.isEmpty() || !addDownloads.isEmpty() || !addDocumentations.isEmpty()
			|| modifyLicense != null && !modifyLicense.isEmpty() || !addLanguages.isEmpty()
			|| !modifyCredits.isEmpty() || !addCredits.isEmpty();
	}

	public double getScoreScore2() {
		return scoreScore2;
	}
	public void setScoreScore2(double scoreScore2) {
		this.scoreScore2 = scoreScore2;
	}

	public Set<Integer> getPossiblyRelated() {
		return possiblyRelated;
	}
	public void setPossiblyRelated(Set<Integer> possiblyRelated) {
		this.possiblyRelated = possiblyRelated;
	}

	public int getExisting() {
		return existing;
	}
	public void setExisting(int existing) {
		this.existing = existing;
	}

	public Set<PublicationIds> getModifyPublications() {
		return modifyPublications;
	}
	public void addModifyPublication(PublicationIds publication) {
		modifyPublications.add(publication);
	}

	public Set<PublicationIds> getAddPublications() {
		return addPublications;
	}
	public void setAddPublications(Set<PublicationIds> addPublications) {
		this.addPublications = addPublications;
	}

	public String getModifyName() {
		return modifyName;
	}
	public void setModifyName(String modifyName) {
		this.modifyName = modifyName;
	}

	public String getModifyHomepage() {
		return modifyHomepage;
	}
	public void setModifyHomepage(String modifyHomepage) {
		this.modifyHomepage = modifyHomepage;
	}

	public Set<BiotoolsLink<LinkType>> getAddLinks() {
		return addLinks;
	}
	public void addAddLink(BiotoolsLink<LinkType> link) {
		addLinks.add(link);
	}

	public Set<BiotoolsLink<DownloadType>> getAddDownloads() {
		return addDownloads;
	}
	public void addAddDownload(BiotoolsLink<DownloadType> download) {
		addDownloads.add(download);
	}

	public Set<BiotoolsLink<DocumentationType>> getAddDocumentations() {
		return addDocumentations;
	}
	public void addAddDocumentation(BiotoolsLink<DocumentationType> documentation) {
		addDocumentations.add(documentation);
	}

	public Provenance getModifyLicense() {
		return modifyLicense;
	}
	public void setModifyLicense(Provenance modifyLicense) {
		this.modifyLicense = modifyLicense;
	}

	public Set<Provenance> getAddLanguages() {
		return addLanguages;
	}
	public void addAddLanguage(Provenance language) {
		addLanguages.add(language);
	}

	public List<CorrespAuthor> getModifyCredits() {
		return modifyCredits;
	}
	public void addModifyCredit(CorrespAuthor credit) {
		modifyCredits.add(credit);
	}

	public List<CorrespAuthor> getAddCredits() {
		return addCredits;
	}
	public void addAddCredit(CorrespAuthor credit) {
		addCredits.add(credit);
	}
}
