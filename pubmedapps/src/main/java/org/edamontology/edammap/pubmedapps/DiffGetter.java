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

import org.edamontology.pubfetcher.core.common.PubFetcher;
import org.edamontology.pubfetcher.core.db.Database;
import org.edamontology.pubfetcher.core.db.publication.CorrespAuthor;
import org.edamontology.pubfetcher.core.db.webpage.Webpage;

import org.edamontology.edammap.core.input.json.Credit;
import org.edamontology.edammap.core.input.json.DocumentationType;
import org.edamontology.edammap.core.input.json.DownloadType;
import org.edamontology.edammap.core.input.json.Link;
import org.edamontology.edammap.core.input.json.LinkType;
import org.edamontology.edammap.core.input.json.ToolInput;

public final class DiffGetter {

	private static boolean linksEqual(String addLink, String addLinkTrimmed, String biotoolsLink, Database db, boolean addLinkDoc, boolean biotoolsLinkDoc) {
		String biotoolsLinkTrimmed = Common.trimUrl(biotoolsLink);
		if (addLinkTrimmed.equals(biotoolsLinkTrimmed)) {
			return true;
		}
		String addLinkFinalTrimmed = null;
		Webpage addLinkWebpage = null;
		if (addLinkDoc) {
			addLinkWebpage = db.getDoc(addLink, false);
		} else {
			addLinkWebpage = db.getWebpage(addLink, false);
		}
		if (addLinkWebpage != null) {
			addLinkFinalTrimmed = Common.trimUrl(addLinkWebpage.getFinalUrl());
		}
		String biotoolsLinkFinalTrimmed = null;
		Webpage biotoolsLinkWebpage = null;
		if (biotoolsLinkDoc) {
			biotoolsLinkWebpage = db.getDoc(biotoolsLink, false);
		} else {
			biotoolsLinkWebpage = db.getWebpage(biotoolsLink, false);
		}
		if (biotoolsLinkWebpage != null) {
			biotoolsLinkFinalTrimmed = Common.trimUrl(biotoolsLinkWebpage.getFinalUrl());
		}
		if (addLinkFinalTrimmed != null && !addLinkFinalTrimmed.isEmpty() && biotoolsLinkFinalTrimmed != null && !biotoolsLinkFinalTrimmed.isEmpty()) {
			if (addLinkFinalTrimmed.equals(biotoolsLinkFinalTrimmed)) {
				return true;
			}
		} else {
			if (addLinkFinalTrimmed != null && !addLinkFinalTrimmed.isEmpty()) {
				if (addLinkFinalTrimmed.equals(biotoolsLinkTrimmed)) {
					return true;
				}
			}
			if (biotoolsLinkFinalTrimmed != null && !biotoolsLinkFinalTrimmed.isEmpty()) {
				if (addLinkTrimmed.equals(biotoolsLinkFinalTrimmed)) {
					return true;
				}
			}
		}
		return false;
	}

	private static void addHomepageToLinks(List<BiotoolsLink<LinkType>> linkLinks, List<BiotoolsLink<DownloadType>> downloadLinks, List<BiotoolsLink<DocumentationType>> documentationLinks, Set<BiotoolsLink<LinkType>> links, Set<BiotoolsLink<DownloadType>> downloads, Set<BiotoolsLink<DocumentationType>> documentations, Database db, boolean biotoolsHomepage) {
		boolean found = false;
		if (!linkLinks.isEmpty()) {
			for (BiotoolsLink<LinkType> link : links) {
				if (linksEqual(link.getUrl(), link.getUrlTrimmed(), linkLinks.get(0).getUrl(), db, false, false)) {
					found = true;
					break;
				}
			}
			if (!found) {
				links.add(linkLinks.get(0));
			}
		} else if (!downloadLinks.isEmpty()) {
			for (BiotoolsLink<DownloadType> download : downloads) {
				if (linksEqual(download.getUrl(), download.getUrlTrimmed(), downloadLinks.get(0).getUrl(), db, false, false)) {
					found = true;
					break;
				}
			}
			if (!found) {
				downloads.add(downloadLinks.get(0));
			}
		} else if (!documentationLinks.isEmpty()) {
			for (BiotoolsLink<DocumentationType> documentation : documentations) {
				if (linksEqual(documentation.getUrl(), documentation.getUrlTrimmed(), documentationLinks.get(0).getUrl(), db, true, !biotoolsHomepage)) {
					found = true;
					break;
				}
			}
			if (!found) {
				documentations.add(documentationLinks.get(0));
			}
		}
	}

	static Diff makeDiff(double scoreScore2, Set<Integer> possiblyRelated, List<ToolInput> biotools, int existing, List<PubIds> publications, Set<PubIds> addPublications, String modifyName, String homepage, Set<BiotoolsLink<LinkType>> links, Set<BiotoolsLink<DownloadType>> downloads, Set<BiotoolsLink<DocumentationType>> documentations, Provenance license, List<Provenance> languages, List<CorrespAuthor> credits, Database db) {
		Diff diff = new Diff();

		diff.setScoreScore2(scoreScore2);
		diff.setPossiblyRelated(possiblyRelated);

		ToolInput biotool = biotools.get(existing);

		diff.setExisting(existing);
		diff.setAddPublications(addPublications);
		diff.setModifyName(modifyName);

		for (PubIds pubIds : publications) {
			if (biotool.getPublication() != null) {
				for (org.edamontology.edammap.core.input.json.Publication publicationIds : biotool.getPublication()) {
					if ((!pubIds.getPmid().isEmpty() && publicationIds.getPmid() != null && publicationIds.getPmid().trim().equals(pubIds.getPmid())
							|| !pubIds.getPmcid().isEmpty() && publicationIds.getPmcid() != null && publicationIds.getPmcid().trim().equals(pubIds.getPmcid())
							|| !pubIds.getDoi().isEmpty() && publicationIds.getDoi() != null && PubFetcher.normaliseDoi(publicationIds.getDoi().trim()).equals(pubIds.getDoi()))
						&& (!pubIds.getPmid().isEmpty() && publicationIds.getPmid() != null && !publicationIds.getPmid().isEmpty() && !publicationIds.getPmid().trim().equals(pubIds.getPmid())
							|| !pubIds.getPmcid().isEmpty() && publicationIds.getPmcid() != null && !publicationIds.getPmcid().isEmpty() && !publicationIds.getPmcid().trim().equals(pubIds.getPmcid())
							|| !pubIds.getDoi().isEmpty() && publicationIds.getDoi() != null && !publicationIds.getDoi().isEmpty() && !PubFetcher.normaliseDoi(publicationIds.getDoi().trim()).equals(pubIds.getDoi()))) {
						diff.addModifyPublication(pubIds);
					}
				}
			}
		}

		Set<BiotoolsLink<LinkType>> linksLocal = new LinkedHashSet<>();
		if (links != null) {
			linksLocal.addAll(links);
		}
		Set<BiotoolsLink<DownloadType>> downloadsLocal = new LinkedHashSet<>();
		if (downloads != null) {
			downloadsLocal.addAll(downloads);
		}
		Set<BiotoolsLink<DocumentationType>> documentationsLocal = new LinkedHashSet<>();
		if (documentations != null) {
			documentationsLocal.addAll(documentations);
		}

		if (homepage != null && !homepage.isEmpty()) {
			String homepageTrimmed = Common.trimUrl(homepage);
			if (!linksEqual(homepage, homepageTrimmed, biotool.getHomepage(), db, false, false)
					&& !linksEqual(homepage, homepageTrimmed, biotool.getHomepage(), db, true, false)) {
				Webpage webpage = db.getWebpage(biotool.getHomepage(), false);
				List<String> homepageLinks = new ArrayList<>();
				homepageLinks.add(homepage);
				List<BiotoolsLink<LinkType>> linkLinks = new ArrayList<>();
				List<BiotoolsLink<DownloadType>> downloadLinks = new ArrayList<>();
				List<BiotoolsLink<DocumentationType>> documentationLinks = new ArrayList<>();
				Common.makeBiotoolsLinks(homepageLinks, linkLinks, downloadLinks, documentationLinks);
				if (biotool.getHomepage_status() != 0 && (webpage == null || webpage.isBroken())) {
					diff.setModifyHomepage(homepage);
				} else if (!linkLinks.isEmpty() && linkLinks.get(0).getType() == LinkType.OTHER) {
					diff.setModifyHomepage(homepage);
					List<String> biotoolsHomepageLinks = new ArrayList<>();
					biotoolsHomepageLinks.add(biotool.getHomepage());
					List<BiotoolsLink<LinkType>> biotoolsLinkLinks = new ArrayList<>();
					List<BiotoolsLink<DownloadType>> biotoolsDownloadLinks = new ArrayList<>();
					List<BiotoolsLink<DocumentationType>> biotoolsDocumentationLinks = new ArrayList<>();
					Common.makeBiotoolsLinks(biotoolsHomepageLinks, biotoolsLinkLinks, biotoolsDownloadLinks, biotoolsDocumentationLinks);
					addHomepageToLinks(biotoolsLinkLinks, biotoolsDownloadLinks, biotoolsDocumentationLinks, linksLocal, downloadsLocal, documentationsLocal, db, true);
				} else {
					addHomepageToLinks(linkLinks, downloadLinks, documentationLinks, linksLocal, downloadsLocal, documentationsLocal, db, false);
				}
			}
		}

		for (BiotoolsLink<LinkType> link : linksLocal) {
			if (biotool.getLink() == null) {
				diff.addAddLink(link);
			} else {
				boolean found = false;
				for (Link<LinkType> linkBiotools : biotool.getLink()) {
					if (linksEqual(link.getUrl(), link.getUrlTrimmed(), linkBiotools.getUrl(), db, false, false)) {
						found = true;
						break;
					}
				}
				if (link.getType().equals(LinkType.OTHER)) {
					if (!found) {
						if (linksEqual(link.getUrl(), link.getUrlTrimmed(), biotool.getHomepage(), db, false, false) && (diff.getModifyHomepage() == null || diff.getModifyHomepage().isEmpty())) {
							found = true;
						}
					}
					if (!found) {
						for (Link<DownloadType> downloadBiotools : biotool.getDownload()) {
							if (linksEqual(link.getUrl(), link.getUrlTrimmed(), downloadBiotools.getUrl(), db, false, false)) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						for (Link<DocumentationType> documentationBiotools : biotool.getDocumentation()) {
							if (linksEqual(link.getUrl(), link.getUrlTrimmed(), documentationBiotools.getUrl(), db, false, true)) {
								found = true;
								break;
							}
						}
					}
				}
				if (!found) {
					diff.addAddLink(link);
				}
			}
		}

		for (BiotoolsLink<DownloadType> download : downloadsLocal) {
			if (biotool.getDownload() == null) {
				diff.addAddDownload(download);
			} else {
				boolean found = false;
				for (Link<DownloadType> downloadBiotools : biotool.getDownload()) {
					if (linksEqual(download.getUrl(), download.getUrlTrimmed(), downloadBiotools.getUrl(), db, false, false)) {
						found = true;
						break;
					}
				}
				if (!found) {
					diff.addAddDownload(download);
				}
			}
		}

		for (BiotoolsLink<DocumentationType> documentation : documentationsLocal) {
			if (biotool.getDocumentation() == null) {
				diff.addAddDocumentation(documentation);
			} else {
				boolean found = false;
				for (Link<DocumentationType> documentationBiotools : biotool.getDocumentation()) {
					if (linksEqual(documentation.getUrl(), documentation.getUrlTrimmed(), documentationBiotools.getUrl(), db, true, true)) {
						found = true;
						break;
					}
				}
				if (!found) {
					diff.addAddDocumentation(documentation);
				}
			}
		}

		if (license != null) {
			if (!license.isEmpty() && (biotool.getLicense() == null || !biotool.getLicense().equals(license.getObject()))) {
				diff.setModifyLicense(license);
			}
		}

		for (Provenance language : languages) {
			if (!language.isEmpty() && (biotool.getLanguage() == null || !biotool.getLanguage().contains(language.getObject()))) {
				diff.addAddLanguage(language);
			}
		}

		for (CorrespAuthor credit : credits) {
			if (biotool.getCredit() == null) {
				diff.addAddCredit(credit);
			} else {
				boolean found = false;
				boolean foundModify = false;
				for (Credit creditBiotools : biotool.getCredit()) {
					if ((credit.getName().isEmpty() || credit.getName().equals(creditBiotools.getName()))
							&& (credit.getOrcid().isEmpty() || credit.getOrcid().equals(creditBiotools.getOrcidid()))
							&& (credit.getEmail().isEmpty() || credit.getEmail().equals(creditBiotools.getEmail()))) {
						found = true;
						break;
					}
					if (creditBiotools.getName() != null && Common.creditNameEqual(credit.getName(), creditBiotools.getName())
							|| creditBiotools.getOrcidid() != null && Common.creditOrcidEqual(credit.getOrcid(), creditBiotools.getOrcidid())
							|| creditBiotools.getEmail() != null && Common.creditEmailEqual(credit.getEmail(), creditBiotools.getEmail())) {
						foundModify = true;
					}
				}
				if (!found) {
					if (!foundModify) {
						diff.addAddCredit(credit);
					} else {
						diff.addModifyCredit(credit);
					}
				}
			}
		}

		return diff;
	}

	static void addDiff(List<Diff> diffs, Diff diff) {
		boolean added = false;
		for (int i = diffs.size() - 1; i >= 0; --i) {
			if (diff.getExisting() == diffs.get(i).getExisting() && diff.include() && diffs.get(i).include()) {
				diffs.add(i + 1, diff);
				added = true;
				break;
			}
		}
		if (!added) {
			diffs.add(diff);
		}
	}
}
