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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.edamontology.pubfetcher.core.common.FetcherArgs;
import org.edamontology.pubfetcher.core.common.PubFetcher;
import org.edamontology.pubfetcher.core.db.Database;
import org.edamontology.pubfetcher.core.db.publication.CorrespAuthor;
import org.edamontology.pubfetcher.core.db.publication.PublicationIds;
import org.edamontology.pubfetcher.core.db.webpage.Webpage;
import org.edamontology.pubfetcher.core.scrape.Scrape;

import org.edamontology.edammap.core.idf.Idf;
import org.edamontology.edammap.core.input.Json;
import org.edamontology.edammap.core.input.json.Credit;
import org.edamontology.edammap.core.input.json.DocumentationType;
import org.edamontology.edammap.core.input.json.DownloadType;
import org.edamontology.edammap.core.input.json.EntityType;
import org.edamontology.edammap.core.input.json.Link;
import org.edamontology.edammap.core.input.json.LinkType;
import org.edamontology.edammap.core.input.json.LinkVersion;
import org.edamontology.edammap.core.input.json.Tool;
import org.edamontology.edammap.core.input.json.ToolInput;
import org.edamontology.edammap.core.preprocessing.PreProcessor;
import org.edamontology.edammap.core.query.QueryType;
import org.edamontology.edammap.pubmedapps.Language.LanguageSearch;
import org.edamontology.edammap.pubmedapps.License.LicenseSearch;

public final class Pass2 {

	private static final Logger logger = LogManager.getLogger();

	private static final double ABSTRACT_LINK_INCREASE = 1000;
	private static final double FULLTEXT_LINK_INCREASE = 500;
	private static final double FROM_ABSTRACT_LINK_INCREASE = 400;
	private static final double NOT_FIRST_SUGGESTION_LINK_DIVIDER = 2;

	private static final double TOOL_TITLE_1_INCREASE = 900;
	private static final double TOOL_TITLE_2_INCREASE = 600;
	private static final double TOOL_TITLE_3_INCREASE = 400;
	private static final double TOOL_TITLE_4_INCREASE = 250;
	private static final double TOOL_TITLE_5_INCREASE = 150;
	private static final int TOOL_TITLE_ORIGINAL_MAX_SIZE_FOR_ACRONYM = 6;

	private static final Pattern CASE_REMOVE_HYPHEN = Pattern.compile("^(\\p{Lu}.*)-(\\p{Lu})(.*)$");
	private static final Pattern CASE_REMOVE_PLURAL = Pattern.compile("^(.*\\p{Lu})s$");
	private static final Pattern CASE_LOWERCASE = Pattern.compile("^\\p{Ll}+$");
	private static final Pattern CASE_FIRST_CAPITAL = Pattern.compile("^\\p{Lu}\\p{Ll}+$");
	private static final Pattern CASE_UPPERCASE = Pattern.compile("^\\p{Lu}+$");
	private static final Pattern CASE_MIXED_AS_REST = Pattern.compile("^.*\\p{L}.*$");
	private static final double CASE_LOWERCASE_INCREASE = 100;
	private static final double CASE_FIRST_CAPITAL_INCREASE = 200;
	private static final double CASE_UPPERCASE_INCREASE = 300;
	private static final double CASE_MIXED_INCREASE = 400;
	private static final double CASE_DECREASE = 100;
	private static final double SCORE_MIN_FOR_MIXED_IDF_INCREASE = 3.5;
	private static final double SCORE_MIN_FOR_UPPERCASE_IDF_INCREASE = 7;
	private static final double SCORE_MIN_FOR_MIXED_IDF_INCREASE2 = 12.1;

	private static final double IDF_POWER = 5;
	private static final double IDF_MULTIPLIER = 500;

	private static final int NAME_WORD_MATCH_LIMIT = 5;

	private static final Pattern TOOL_TITLE_NOT_ALPHANUM = Pattern.compile("[^\\p{L}\\p{N}]");

	private static boolean isBroken(String url, Database db) {
		if (db.getWebpage(url, false) != null && !db.getWebpage(url, false).isBroken()) {
			return false;
		}
		if (db.getDoc(url, false) != null && !db.getDoc(url, false).isBroken()) {
			return false;
		}
		return true;
	}

	private static void linksMatch(Map<Integer, List<String>> linkMatchMap, String suggestionLink, Suggestion2 suggestion, List<List<String>> queryLinks) {
		suggestionLink = Common.trimUrl(suggestionLink);
		for (int j = 0; j < queryLinks.size(); ++j) {
			if (suggestion.getPublicationAndNameExisting() != null && suggestion.getPublicationAndNameExisting().contains(j)
					|| suggestion.getNameExistingSomePublicationDifferent() != null && suggestion.getNameExistingSomePublicationDifferent().contains(j)
					|| suggestion.getSomePublicationExistingNameDifferent() != null && suggestion.getSomePublicationExistingNameDifferent().contains(j)
					|| suggestion.getNameExistingPublicationDifferent() != null && suggestion.getNameExistingPublicationDifferent().contains(j)) {
				continue;
			}
			List<String> matchedLinks = null;
			for (String queryLink : queryLinks.get(j)) {
				if (suggestionLink.equalsIgnoreCase(queryLink)) {
					if (matchedLinks == null) {
						matchedLinks = new ArrayList<>();
					}
					matchedLinks.add(queryLink);
				} else if (suggestionLink.startsWith(queryLink)) {
					String rest = suggestionLink.substring(queryLink.length() - 1);
					if (Common.LINK_COMPARE_REST.matcher(rest).matches()) {
						if (matchedLinks == null) {
							matchedLinks = new ArrayList<>();
						}
						matchedLinks.add(queryLink);
					}
				}
			}
			if (matchedLinks != null && !matchedLinks.isEmpty()) {
				List<String> linkMatchLinks = linkMatchMap.get(j);
				if (linkMatchLinks == null) {
					linkMatchLinks = new ArrayList<>();
					linkMatchMap.put(j, linkMatchLinks);
				}
				linkMatchLinks.addAll(matchedLinks);
			}
		}
	}

	private static <T> void removeBroken(List<BiotoolsLink<T>> links, Set<BiotoolsLink<?>> broken, Database db, boolean doc, String name) {
		for (Iterator<BiotoolsLink<T>> it = links.iterator(); it.hasNext(); ) {
			BiotoolsLink<T> link = it.next();
			boolean removed = false;
			if (!doc) {
				if (db.getWebpage(link.getUrl(), true) == null || db.getWebpage(link.getUrl(), true).isBroken()) {
					broken.add(link);
					it.remove();
					removed = true;
				}
			} else {
				if (db.getDoc(link.getUrl(), true) == null || db.getDoc(link.getUrl(), true).isBroken()) {
					broken.add(link);
					it.remove();
					removed = true;
				}
			}
			if (!removed) {
				if (!Common.BIOTOOLS_SCHEMA_URLFTP_PATTERN.matcher(link.getUrl()).matches()) {
					logger.warn("Discarded invalid link url '{}' (for name '{}')", link.getUrl(), name);
					it.remove();
				}
			}
		}
	}

	private static String chooseHomepage(List<String> links, List<BiotoolsLink<LinkType>> linkLinks, List<BiotoolsLink<DocumentationType>> documentationLinks, Database db) {
		for (Iterator<BiotoolsLink<LinkType>> it =  linkLinks.iterator(); it.hasNext(); ) {
			BiotoolsLink<LinkType> linkLink = it.next();
			if (linkLink.getType() == LinkType.OTHER && Common.BIOTOOLS_SCHEMA_URL_PATTERN.matcher(linkLink.getUrl()).matches()) {
				it.remove();
				return linkLink.getUrl();
			}
		}
		for (Iterator<BiotoolsLink<LinkType>> it =  linkLinks.iterator(); it.hasNext(); ) {
			BiotoolsLink<LinkType> linkLink = it.next();
			if (linkLink.getType() == LinkType.REPOSITORY && Common.BIOTOOLS_SCHEMA_URL_PATTERN.matcher(linkLink.getUrl()).matches()) {
				it.remove();
				return linkLink.getUrl();
			}
		}
		for (Iterator<BiotoolsLink<DocumentationType>> it =  documentationLinks.iterator(); it.hasNext(); ) {
			BiotoolsLink<DocumentationType> documentationLink = it.next();
			if (documentationLink.getType() == DocumentationType.GENERAL && Common.BIOTOOLS_SCHEMA_URL_PATTERN.matcher(documentationLink.getUrl()).matches()) {
				it.remove();
				return documentationLink.getUrl();
			}
		}
		for (Iterator<BiotoolsLink<DocumentationType>> it =  documentationLinks.iterator(); it.hasNext(); ) {
			BiotoolsLink<DocumentationType> documentationLink = it.next();
			if ((documentationLink.getType() == DocumentationType.MANUAL
					|| documentationLink.getType() == DocumentationType.INSTALLATION_INSTRUCTIONS
					|| documentationLink.getType() == DocumentationType.TUTORIAL
					|| documentationLink.getType() == DocumentationType.TRAINING_MATERIAL
					|| documentationLink.getType() == DocumentationType.API_DOCUMENTATION)
					&& Common.BIOTOOLS_SCHEMA_URL_PATTERN.matcher(documentationLink.getUrl()).matches()) {
				it.remove();
				return documentationLink.getUrl();
			}
		}
		for (String link : links) {
			link = Common.prependHttp(link);
			if (db.getWebpage(link, false) != null && !db.getWebpage(link, false).isBroken() || db.getDoc(link, false) != null && !db.getDoc(link, false).isBroken()) {
				if (!Common.DOWNLOAD_EXT.matcher(link).find() && Common.BIOTOOLS_SCHEMA_URL_PATTERN.matcher(link).matches()) {
					return link;
				}
			}
		}
		return null;
	}

	private static List<Integer> removeExisting(List<Integer> existing, List<ToolInput> biotools, List<String> toolTitleOthers, String suggestionProcessed) {
		List<Integer> removeIndex = new ArrayList<>();
		if (existing != null) {
			for (int i = 0; i < existing.size(); ++i) {
				ToolInput biotool = biotools.get(existing.get(i));
				String idProcessed = String.join("", biotool.getBiotoolsID());
				String nameProcessed = String.join("", biotool.getName());
				for (String other : toolTitleOthers) {
					if ((idProcessed.contains(other) || nameProcessed.contains(other)) && !(suggestionProcessed.equals(idProcessed) || suggestionProcessed.equals(nameProcessed))) {
						removeIndex.add(i);
					}
				}
			}
		}
		return removeIndex;
	}

	private static String currentHomepage(ToolInput biotool, Database db) {
		String homepage = biotool.getHomepage();
		if (biotool.getHomepage_status() != 0) {
			homepage += " (homepage_status: " + biotool.getHomepage_status() + ")";
		}
		Webpage webpage = db.getWebpage(biotool.getHomepage(), false);
		if (webpage != null && webpage.isBroken()) {
			homepage += " (broken)";
		}
		return homepage;
	}

	private static void writeField(Writer writer, String value, boolean last) throws IOException {
		if (value != null && value.length() > 0 && (value.charAt(0) == '"' || value.indexOf('\t') > -1)) {
			value = "\"" + value.replace("\"", "\"\"") + "\"";
		}
		if (value != null) {
			writer.write(value);
		}
		if (last) {
			writer.write("\n");
		} else {
			writer.write("\t");
		}
	}

	private static void writeField(Writer writer, String value) throws IOException {
		writeField(writer, value, false);
	}

	private static void addDiffTool(Suggestion2 suggestion, Result2 result, Database db, List<ToolInput> biotools, PreProcessor preProcessor, List<Diff> diffs, List<Tool> tools,
			String name, String description, String homepage, Set<BiotoolsLink<LinkType>> linkLinks, Set<BiotoolsLink<DownloadType>> downloadLinks, Set<BiotoolsLink<DocumentationType>> documentationLinks,
			Provenance bestLicense, Provenance bestAbstractLicense, List<Provenance> allLanguages, List<Provenance> abstractLanguagesUnique, List<CorrespAuthor> credits) {
		double scoreScore2 = suggestion.getScore2() < 0 ? suggestion.getScore() + 10000 : suggestion.getScore2();

		Set<Integer> possiblyRelated = new LinkedHashSet<>();
		if (suggestion.include()) {
			if (suggestion.getNameExistingPublicationDifferent() != null) {
				possiblyRelated.addAll(suggestion.getNameExistingPublicationDifferent());
			}
			possiblyRelated.addAll(result.getNameMatch());
			possiblyRelated.addAll(result.getLinkMatch());
			// result.getNameWordMatch() is omitted
		}

		List<Integer> publicationAndNameExistingRemoveIndex = new ArrayList<>();
		List<Integer> nameExistingSomePublicationDifferentRemoveIndex = new ArrayList<>();
		List<Integer> somePublicationExistingNameDifferentRemoveIndex = new ArrayList<>();
		List<String> toolTitleOthers = new ArrayList<>();
		for (List<String> toolTitleOther : result.getToolTitleOthers()) {
			for (String other : toolTitleOther) {
				for (String otherPart : TOOL_TITLE_NOT_ALPHANUM.split(other)) {
					otherPart = Common.TOOL_TITLE_TRIM.matcher(String.join("", preProcessor.process(otherPart))).replaceFirst("");
					if (otherPart.length() > 1) {
						toolTitleOthers.add(otherPart);
						break;
					}
				}
			}
		}
		if (!toolTitleOthers.isEmpty()) {
			String suggestionProcessed = suggestion.getProcessed().replace(" ", "");
			publicationAndNameExistingRemoveIndex = removeExisting(suggestion.getPublicationAndNameExisting(), biotools, toolTitleOthers, suggestionProcessed);
			nameExistingSomePublicationDifferentRemoveIndex= removeExisting(suggestion.getNameExistingSomePublicationDifferent(), biotools, toolTitleOthers, suggestionProcessed);
			somePublicationExistingNameDifferentRemoveIndex = removeExisting(suggestion.getSomePublicationExistingNameDifferent(), biotools, toolTitleOthers, suggestionProcessed);
		}

		boolean foundDiff = false;

		if (suggestion.getPublicationAndNameExisting() != null) {
			for (int i = 0; i < suggestion.getPublicationAndNameExisting().size(); ++i) {
				if (publicationAndNameExistingRemoveIndex.contains(i)) {
					continue;
				}
				if (suggestion.include()) {
					DiffGetter.addDiff(diffs, DiffGetter.makeDiff(scoreScore2, possiblyRelated, biotools, suggestion.getPublicationAndNameExisting().get(i), result.getPubIds(), null, null, homepage, linkLinks, downloadLinks, documentationLinks, bestLicense, allLanguages, credits, db));
				} else {
					DiffGetter.addDiff(diffs, DiffGetter.makeDiff(scoreScore2, possiblyRelated, biotools, suggestion.getPublicationAndNameExisting().get(i), result.getPubIds(), null, null, null, null, null, null, bestAbstractLicense, abstractLanguagesUnique, credits, db));
				}
				foundDiff = true;
			}
		}
		if (suggestion.getNameExistingSomePublicationDifferent() != null) {
			for (int i = 0; i < suggestion.getNameExistingSomePublicationDifferent().size(); ++i) {
				if (nameExistingSomePublicationDifferentRemoveIndex.contains(i)) {
					continue;
				}
				if (suggestion.include()) {
					DiffGetter.addDiff(diffs, DiffGetter.makeDiff(scoreScore2, possiblyRelated, biotools, suggestion.getNameExistingSomePublicationDifferent().get(i), result.getPubIds(), suggestion.getNameExistingSomePublicationDifferentPubIds().get(i), null, homepage, linkLinks, downloadLinks, documentationLinks, bestLicense, allLanguages, credits, db));
				} else {
					DiffGetter.addDiff(diffs, DiffGetter.makeDiff(scoreScore2, possiblyRelated, biotools, suggestion.getNameExistingSomePublicationDifferent().get(i), result.getPubIds(), suggestion.getNameExistingSomePublicationDifferentPubIds().get(i), null, null, null, null, null, bestAbstractLicense, abstractLanguagesUnique, credits, db));
				}
				foundDiff = true;
			}
		}
		if (suggestion.getSomePublicationExistingNameDifferent() != null) {
			for (int i = 0; i < suggestion.getSomePublicationExistingNameDifferent().size(); ++i) {
				if (somePublicationExistingNameDifferentRemoveIndex.contains(i)) {
					continue;
				}
				if (suggestion.include()) {
					DiffGetter.addDiff(diffs, DiffGetter.makeDiff(scoreScore2, possiblyRelated, biotools, suggestion.getSomePublicationExistingNameDifferent().get(i), result.getPubIds(), suggestion.getSomePublicationExistingNameDifferentPubIds().get(i), name, homepage, linkLinks, downloadLinks, documentationLinks, bestLicense, allLanguages, credits, db));
				} else {
					DiffGetter.addDiff(diffs, DiffGetter.makeDiff(scoreScore2, possiblyRelated, biotools, suggestion.getSomePublicationExistingNameDifferent().get(i), result.getPubIds(), suggestion.getSomePublicationExistingNameDifferentPubIds().get(i), null, null, null, null, null, bestAbstractLicense, abstractLanguagesUnique, credits, db));
				}
				foundDiff = true;
			}
		}

		if (!foundDiff && suggestion.include()) {
			Tool tool = new Tool();

			tool.setName(name);
			tool.setDescription(description);
			tool.setHomepage(suggestion.getHomepage());

			tool.setLanguage(allLanguages.stream().map(p -> p.getObject()).collect(Collectors.toList()));
			if (bestLicense != null) {
				tool.setLicense(bestLicense.getObject());
			}

			List<Link<LinkType>> links = new ArrayList<>();
			for (BiotoolsLink<LinkType> link : linkLinks) {
				Link<LinkType> newLink = new Link<>();
				newLink.setUrl(link.getUrl());
				newLink.setType(link.getType());
				links.add(newLink);
			}
			tool.setLink(links);

			List<LinkVersion<DownloadType>> downloads = new ArrayList<>();
			for (BiotoolsLink<DownloadType> download : downloadLinks) {
				LinkVersion<DownloadType> newDownload = new LinkVersion<>();
				newDownload.setUrl(download.getUrl());
				newDownload.setType(download.getType());
				downloads.add(newDownload);
			}
			tool.setDownload(downloads);

			List<Link<DocumentationType>> documentations = new ArrayList<>();
			for (BiotoolsLink<DocumentationType> documentation : documentationLinks) {
				Link<DocumentationType> newDocumentation = new Link<>();
				newDocumentation.setUrl(documentation.getUrl());
				newDocumentation.setType(documentation.getType());
				documentations.add(newDocumentation);
			}
			tool.setDocumentation(documentations);

			List<org.edamontology.edammap.core.input.json.Publication> publication = new ArrayList<>();
			for (PubIds pubIds : result.getPubIds().stream().filter(id -> !id.getPmid().isEmpty() || !id.getPmcid().isEmpty() || !id.getDoi().isEmpty()).collect(Collectors.toCollection(LinkedHashSet::new))) {
				org.edamontology.edammap.core.input.json.Publication newPublication = new org.edamontology.edammap.core.input.json.Publication();
				if (!pubIds.getDoi().isEmpty()) {
					newPublication.setDoi(pubIds.getDoi());
				}
				if (!pubIds.getPmid().isEmpty()) {
					newPublication.setPmid(pubIds.getPmid());
				}
				if (!pubIds.getPmcid().isEmpty()) {
					newPublication.setPmcid(pubIds.getPmcid());
				}
				publication.add(newPublication);
			}
			tool.setPublication(publication);

			List<Credit> credit = new ArrayList<>();
			for (CorrespAuthor ca : credits) {
				Credit newCredit = new Credit();
				if (!ca.getName().isEmpty()) {
					newCredit.setName(ca.getName());
				}
				if (!ca.getEmail().isEmpty()) {
					newCredit.setEmail(ca.getEmail());
				}
				if (!ca.getUri().isEmpty()) {
					newCredit.setUrl(ca.getUri());
				}
				if (!ca.getOrcid().isEmpty()) {
					newCredit.setOrcidid(ca.getOrcid());
				}
				newCredit.setTypeEntity(EntityType.PERSON);
				credit.add(newCredit);
			}
			tool.setCredit(credit);

			tools.add(tool);
		}
	}

	private static void writeResult(Result2 result, Database db, Writer resultsWriter,
			List<ToolInput> biotools, List<License> licenses, List<Language> languages, List<String> languageKeywords, Scrape scrape, PreProcessor preProcessor,
			List<Diff> diffs, List<Tool> tools) throws IOException {

		final String name;
		if (!result.getSuggestions().isEmpty()) {
			name = result.getSuggestions().get(0).getExtracted();
		} else {
			name = "";
		}

		final Suggestion2 suggestion;
		if (!result.getSuggestions().isEmpty()) {
			suggestion = result.getSuggestions().get(0);
		} else {
			suggestion = null;
		}

		writeField(resultsWriter, result.getPubIds().stream().map(p -> p.getPmid()).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, result.getPubIds().stream().map(p -> p.getPmcid()).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, result.getPubIds().stream().map(p -> p.getDoi()).collect(Collectors.joining(" | ")));

		writeField(resultsWriter, result.getSameSuggestions().stream().map(pubIds -> pubIds.toString()).collect(Collectors.joining(" | ")));

		writeField(resultsWriter, suggestion != null ? String.valueOf(suggestion.getScore()) : null);
		writeField(resultsWriter, suggestion != null && suggestion.getScore2() > -1 ? String.valueOf(suggestion.getScore2()) : null);
		writeField(resultsWriter, suggestion != null && suggestion.getScore2() > -1 ? Arrays.toString(suggestion.getScore2Parts()) : null);

		writeField(resultsWriter, suggestion != null ? suggestion.getOriginal() : null);
		writeField(resultsWriter, name);
		writeField(resultsWriter, suggestion != null ? suggestion.getProcessed() : null);

		List<Integer> existing = new ArrayList<>();
		if (suggestion != null) {
			if (suggestion.getPublicationAndNameExisting() != null) {
				existing.addAll(suggestion.getPublicationAndNameExisting());
			}
			if (suggestion.getNameExistingSomePublicationDifferent() != null) {
				existing.addAll(suggestion.getNameExistingSomePublicationDifferent());
			}
			if (suggestion.getSomePublicationExistingNameDifferent() != null) {
				existing.addAll(suggestion.getSomePublicationExistingNameDifferent());
			}
			if (suggestion.getNameExistingPublicationDifferent() != null) {
				existing.addAll(suggestion.getNameExistingPublicationDifferent());
			}
		}

		List<String> publicationAndNameExisting = null;
		if (suggestion != null && suggestion.getPublicationAndNameExisting() != null) {
			publicationAndNameExisting = suggestion.getPublicationAndNameExisting().stream().map(e -> biotools.get(e)).map(q -> q.getBiotoolsID()).collect(Collectors.toList());
			writeField(resultsWriter, String.join(" | ", publicationAndNameExisting));
		} else {
			writeField(resultsWriter, null);
		}

		List<String> nameExistingSomePublicationDifferent = null;
		if (suggestion != null && suggestion.getNameExistingSomePublicationDifferent() != null) {
			nameExistingSomePublicationDifferent = IntStream.range(0, suggestion.getNameExistingSomePublicationDifferent().size())
				.mapToObj(i -> biotools.get(suggestion.getNameExistingSomePublicationDifferent().get(i)).getBiotoolsID() +
					((suggestion.getNameExistingSomePublicationDifferentPubIds() != null && suggestion.getNameExistingSomePublicationDifferentPubIds().get(i) != null) ? (" (" +
						String.join(" ; ", suggestion.getNameExistingSomePublicationDifferentPubIds().get(i).stream().map(p -> p.toString()).collect(Collectors.toList()))
					+ ")") : "")).collect(Collectors.toList());
			writeField(resultsWriter, String.join(" | ", nameExistingSomePublicationDifferent));
		} else {
			writeField(resultsWriter, null);
		}

		List<String> somePublicationExistingNameDifferent = null;
		if (suggestion != null && suggestion.getSomePublicationExistingNameDifferent() != null) {
			somePublicationExistingNameDifferent = IntStream.range(0, suggestion.getSomePublicationExistingNameDifferent().size())
				.mapToObj(i -> biotools.get(suggestion.getSomePublicationExistingNameDifferent().get(i)).getBiotoolsID() + " (" + biotools.get(suggestion.getSomePublicationExistingNameDifferent().get(i)).getName() + ")" +
					((suggestion.getSomePublicationExistingNameDifferentPubIds() != null && suggestion.getSomePublicationExistingNameDifferentPubIds().get(i) != null && !suggestion.getSomePublicationExistingNameDifferentPubIds().get(i).isEmpty()) ? (" (" +
						String.join(" ; ", suggestion.getSomePublicationExistingNameDifferentPubIds().get(i).stream().map(p -> p.toString()).collect(Collectors.toList()))
					+ ")") : "")).collect(Collectors.toList());
			writeField(resultsWriter, String.join(" | ", somePublicationExistingNameDifferent));
		} else {
			writeField(resultsWriter, null);
		}

		List<String> nameExistingPublicationDifferent = null;
		if (suggestion != null && suggestion.getNameExistingPublicationDifferent() != null) {
			nameExistingPublicationDifferent = IntStream.range(0, suggestion.getNameExistingPublicationDifferent().size())
				.mapToObj(i -> biotools.get(suggestion.getNameExistingPublicationDifferent().get(i)).getBiotoolsID() +
					((suggestion.getNameExistingPublicationDifferentPubIds() != null && suggestion.getNameExistingPublicationDifferentPubIds().get(i) != null) ? (" (" +
						String.join(" ; ", suggestion.getNameExistingPublicationDifferentPubIds().get(i).stream().map(p -> p.toString()).collect(Collectors.toList()))
					+ ")") : "")).collect(Collectors.toList());
			writeField(resultsWriter, String.join(" | ", nameExistingPublicationDifferent));
		} else {
			writeField(resultsWriter, null);
		}

		List<String> nameMatch = result.getNameMatch().stream().map(e -> biotools.get(e)).map(q -> q.getBiotoolsID() + " (" + q.getName() + ")").collect(Collectors.toList());
		writeField(resultsWriter, String.join(" | ", nameMatch));

		List<String> linkMatch = IntStream.range(0, result.getLinkMatch().size())
			.mapToObj(i -> biotools.get(result.getLinkMatch().get(i)).getBiotoolsID() + " (" +
				String.join(" ; ", result.getLinkMatchLinks().get(i))
			+ ")").collect(Collectors.toList());
		writeField(resultsWriter, String.join(" | ", linkMatch));

		List<String> nameWordMatch = result.getNameWordMatch().stream().map(e -> biotools.get(e)).map(q -> q.getBiotoolsID() + " (" + q.getName() + ")").collect(Collectors.toList());
		writeField(resultsWriter, String.join(" | ", nameWordMatch));

		List<String> linksAbstract = new ArrayList<>();
		if (suggestion != null) {
			linksAbstract = suggestion.getLinksAbstract();
			writeField(resultsWriter, String.join(" | ", linksAbstract));
		} else {
			writeField(resultsWriter, null);
		}
		if (suggestion != null) {
			writeField(resultsWriter, String.join(" | ", suggestion.getLinksFulltext()));
		} else {
			writeField(resultsWriter, null);
		}

		writeField(resultsWriter, result.getSuggestions().stream().map(s -> String.valueOf(s.isFromAbstractLink())).collect(Collectors.joining(" | ")));

		final String homepage;
		boolean homepageBroken = false;
		boolean homepageMissing = true;
		if (suggestion != null) {
			homepageBroken = suggestion.isHomepageBroken();
			homepageMissing = suggestion.isHomepageMissing();
			if (!homepageBroken && !homepageMissing) {
				homepage = suggestion.getHomepage();
			} else {
				homepage = "";
			}
			writeField(resultsWriter, suggestion.getHomepage() + (homepageBroken ? " (broken)" : "") + (homepageMissing ? " (missing)" : ""));
		} else {
			writeField(resultsWriter, null);
			homepage = "";
		}
		writeField(resultsWriter, existing.stream().map(e -> biotools.get(e)).map(q -> currentHomepage(q, db)).collect(Collectors.joining(" | ")));

		Set<BiotoolsLink<LinkType>> linkLinks = new LinkedHashSet<>();
		if (suggestion != null) {
			linkLinks = suggestion.getLinkLinks();
			writeField(resultsWriter, linkLinks.stream().map(l -> l.getUrl() + " (" + l.getType() + ")").collect(Collectors.joining(" | ")));
		} else {
			writeField(resultsWriter, null);
		}
		writeField(resultsWriter, existing.stream().map(e -> biotools.get(e)).map(t -> (t.getLink() == null ? "" : t.getLink().stream().map(l -> l.getUrl() + " (" + l.getType() + ")").collect(Collectors.joining(" ; ")))).collect(Collectors.joining(" | ")));

		Set<BiotoolsLink<DownloadType>> downloadLinks = new LinkedHashSet<>();
		if (suggestion != null) {
			downloadLinks = suggestion.getDownloadLinks();
			writeField(resultsWriter, downloadLinks.stream().map(l -> l.getUrl() + " (" + l.getType() + ")").collect(Collectors.joining(" | ")));
		} else {
			writeField(resultsWriter, null);
		}
		writeField(resultsWriter, existing.stream().map(e -> biotools.get(e)).map(t -> (t.getDownload() == null ? "" : t.getDownload().stream().map(l -> l.getUrl() + " (" + l.getType() + ")").collect(Collectors.joining(" ; ")))).collect(Collectors.joining(" | ")));

		Set<BiotoolsLink<DocumentationType>> documentationLinks = new LinkedHashSet<>();
		if (suggestion != null) {
			documentationLinks = suggestion.getDocumentationLinks();
			writeField(resultsWriter, documentationLinks.stream().map(l -> l.getUrl() + " (" + l.getType() + ")").collect(Collectors.joining(" | ")));
		} else {
			writeField(resultsWriter, null);
		}
		writeField(resultsWriter, existing.stream().map(e -> biotools.get(e)).map(t -> (t.getDocumentation() == null ? "" : t.getDocumentation().stream().map(l -> l.getUrl() + " (" + l.getType() + ")").collect(Collectors.joining(" ; ")))).collect(Collectors.joining(" | ")));

		if (suggestion != null) {
			writeField(resultsWriter, suggestion.getBrokenLinks().stream().map(l -> l.getUrl() + " (" + l.getType() + ")").collect(Collectors.joining(" | ")));
		} else {
			writeField(resultsWriter, null);
		}

		writeField(resultsWriter, result.getSuggestions().stream().skip(1).map(s -> String.format(Locale.ROOT, "%.1f", s.getScore())).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, result.getSuggestions().stream().skip(1).map(s -> (s.getScore2() > -1) ? String.format(Locale.ROOT, "%.1f", s.getScore2()) : "").collect(Collectors.joining(" | ")));
		writeField(resultsWriter, result.getSuggestions().stream().skip(1).map(s -> (s.getScore2() > -1) ? ("[" + Arrays.asList(s.getScore2Parts()).stream().map(part -> String.format(Locale.ROOT, "%.1f", part)).collect(Collectors.joining(", ")) + "]") : "").collect(Collectors.joining(" | ")));

		writeField(resultsWriter, result.getSuggestions().stream().skip(1).map(s -> s.getOriginal()).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, result.getSuggestions().stream().skip(1).map(s -> s.getExtracted()).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, result.getSuggestions().stream().skip(1).map(s -> s.getProcessed()).collect(Collectors.joining(" | ")));

		List<List<String>> otherPublicationAndNameExisting = result.getSuggestions().stream().skip(1).map(s -> s.getPublicationAndNameExisting() != null ? s.getPublicationAndNameExisting().stream().map(e -> biotools.get(e)).map(q -> q.getBiotoolsID()).collect(Collectors.toList()) : null).collect(Collectors.toList());
		writeField(resultsWriter, otherPublicationAndNameExisting.stream().map(e -> e != null ? String.join(" ; ", e) : "").collect(Collectors.joining(" | ")));

		List<List<String>> otherNameExistingSomePublicationDifferent = result.getSuggestions().stream().skip(1)
			.map(s -> s.getNameExistingSomePublicationDifferent() != null ? IntStream.range(0, s.getNameExistingSomePublicationDifferent().size())
				.mapToObj(i -> biotools.get(s.getNameExistingSomePublicationDifferent().get(i)).getBiotoolsID() +
					((s.getNameExistingSomePublicationDifferentPubIds() != null && s.getNameExistingSomePublicationDifferentPubIds().get(i) != null) ? (" (" +
						String.join(" ; ", s.getNameExistingSomePublicationDifferentPubIds().get(i).stream().map(p -> p.toString()).collect(Collectors.toList()))
					+ ")") : "")).collect(Collectors.toList())
			: null).collect(Collectors.toList());
		writeField(resultsWriter, otherNameExistingSomePublicationDifferent.stream().map(e -> e != null ? String.join(" ; ", e) : "").collect(Collectors.joining(" | ")));

		List<List<String>> otherSomePublicationExistingNameDifferent = result.getSuggestions().stream().skip(1)
			.map(s -> s.getSomePublicationExistingNameDifferent() != null ? IntStream.range(0, s.getSomePublicationExistingNameDifferent().size())
				.mapToObj(i -> biotools.get(s.getSomePublicationExistingNameDifferent().get(i)).getBiotoolsID() + " (" + biotools.get(s.getSomePublicationExistingNameDifferent().get(i)).getName() + ")" +
					((s.getSomePublicationExistingNameDifferentPubIds() != null && s.getSomePublicationExistingNameDifferentPubIds().get(i) != null && !s.getSomePublicationExistingNameDifferentPubIds().get(i).isEmpty()) ? (" (" +
						String.join(" ; ", s.getSomePublicationExistingNameDifferentPubIds().get(i).stream().map(p -> p.toString()).collect(Collectors.toList()))
					+ ")") : "")).collect(Collectors.toList())
			: null).collect(Collectors.toList());
		writeField(resultsWriter, otherSomePublicationExistingNameDifferent.stream().map(e -> e != null ? String.join(" ; ", e) : "").collect(Collectors.joining(" | ")));

		List<List<String>> otherNameExistingPublicationDifferent = result.getSuggestions().stream().skip(1)
			.map(s -> s.getNameExistingPublicationDifferent() != null ? IntStream.range(0, s.getNameExistingPublicationDifferent().size())
				.mapToObj(i -> biotools.get(s.getNameExistingPublicationDifferent().get(i)).getBiotoolsID() +
					((s.getNameExistingPublicationDifferentPubIds() != null && s.getNameExistingPublicationDifferentPubIds().get(i) != null) ? (" (" +
						String.join(" ; ", s.getNameExistingPublicationDifferentPubIds().get(i).stream().map(p -> p.toString()).collect(Collectors.toList()))
					+ ")") : "")).collect(Collectors.toList())
			: null).collect(Collectors.toList());
		writeField(resultsWriter, otherNameExistingPublicationDifferent.stream().map(e -> e != null ? String.join(" ; ", e) : "").collect(Collectors.joining(" | ")));

		List<String> otherLinksAbstract = new ArrayList<>();
		boolean otherLinksAbstractEmpty = true;
		for (int i = 1; i < result.getSuggestions().size(); ++i) {
			otherLinksAbstract.add(result.getSuggestions().get(i).getLinksAbstract().stream().collect(Collectors.joining(" ; ")));
			if (!result.getSuggestions().get(i).getLinksAbstract().isEmpty()) {
				otherLinksAbstractEmpty = false;
			}
		}
		writeField(resultsWriter, !otherLinksAbstractEmpty ? String.join(" | ", otherLinksAbstract) : null);

		List<String> otherLinksFulltext = new ArrayList<>();
		boolean otherLinksFulltextEmpty = true;
		for (int i = 1; i < result.getSuggestions().size(); ++i) {
			otherLinksFulltext.add(result.getSuggestions().get(i).getLinksFulltext().stream().collect(Collectors.joining(" ; ")));
			if (!result.getSuggestions().get(i).getLinksFulltext().isEmpty()) {
				otherLinksFulltextEmpty = false;
			}
		}
		writeField(resultsWriter, !otherLinksFulltextEmpty ? String.join(" | ", otherLinksFulltext) : null);

		writeField(resultsWriter, result.getLeftoverLinksAbstract().stream().map(l -> String.join(" ; ", l)).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, result.getLeftoverLinksFulltext().stream().map(l -> String.join(" ; ", l)).collect(Collectors.joining(" | ")));

		writeField(resultsWriter, String.join(" | ", result.getTitle()));
		writeField(resultsWriter, result.getToolTitleOthers().stream().map(t -> String.join(" ; ", t)).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, String.join(" | ", result.getToolTitleExtractedOriginal()));
		writeField(resultsWriter, IntStream.range(0, result.getToolTitle().size())
			.mapToObj(i -> result.getToolTitle().get(i).equals(result.getToolTitleExtractedOriginal().get(i)) ? "" : result.getToolTitle().get(i)).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, IntStream.range(0, result.getToolTitlePruned().size())
			.mapToObj(i -> result.getToolTitlePruned().get(i).equals(result.getToolTitle().get(i)) ? "" : result.getToolTitlePruned().get(i)).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, String.join(" | ", result.getToolTitleAcronym()));

		final String description = DescriptionGetter.get(suggestion, homepageBroken, homepageMissing, biotools, result, homepage, linkLinks, documentationLinks, downloadLinks, db, scrape, name, preProcessor);
		writeField(resultsWriter, description);
		writeField(resultsWriter, existing.stream().map(e -> biotools.get(e)).map(q -> q.getDescription().replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r").replaceAll("\t", "\\\\t")).collect(Collectors.joining(" | ")));

		List<Provenance> webpageLicenses = new ArrayList<>();
		if (!homepage.isEmpty()) {
			String homepageLicense = null;
			if (db.getWebpage(homepage, false) != null) {
				homepageLicense = db.getWebpage(homepage, false).getLicense();
			} else if (db.getDoc(homepage, false) != null) {
				homepageLicense = db.getDoc(homepage, false).getLicense();
			}
			if (homepageLicense != null && !homepageLicense.isEmpty()) {
				writeField(resultsWriter, homepageLicense);
				webpageLicenses.add(new Provenance(homepageLicense, homepage));
			} else {
				writeField(resultsWriter, null);
			}
		} else {
			writeField(resultsWriter, null);
		}

		List<Provenance> linkLicenses = linkLinks.stream().map(l -> db.getWebpage(l.getUrl(), true) != null ? new Provenance(db.getWebpage(l.getUrl(), true).getLicense(), l.getUrl()) : new Provenance()).collect(Collectors.toList());
		writeField(resultsWriter, linkLicenses.stream().map(p -> p.toString()).collect(Collectors.joining(" | ")));
		linkLicenses.removeIf(p -> p.isEmpty());
		webpageLicenses.addAll(linkLicenses);

		List<Provenance> downloadLicenses = downloadLinks.stream().map(l -> db.getWebpage(l.getUrl(), true) != null ? new Provenance(db.getWebpage(l.getUrl(), true).getLicense(), l.getUrl()) : new Provenance()).collect(Collectors.toList());
		writeField(resultsWriter, downloadLicenses.stream().map(p -> p.toString()).collect(Collectors.joining(" | ")));
		downloadLicenses.removeIf(p -> p.isEmpty());
		webpageLicenses.addAll(downloadLicenses);

		List<Provenance> documentationLicenses = documentationLinks.stream().map(l -> db.getDoc(l.getUrl(), true) != null ? new Provenance(db.getDoc(l.getUrl(), true).getLicense(), l.getUrl()) : new Provenance()).collect(Collectors.toList());
		writeField(resultsWriter, documentationLicenses.stream().map(p -> p.toString()).collect(Collectors.joining(" | ")));
		documentationLicenses.removeIf(p -> p.isEmpty());
		webpageLicenses.addAll(documentationLicenses);

		List<List<Provenance>> abstractLicenses = new ArrayList<>();
		for (int i = 0; i < result.getPubIds().size(); ++i) {
			List<String> abstractSentences = result.getAbstractSentences().get(i);
			String provenance = result.getPubIds().get(i).toString();
			abstractLicenses.add(abstractSentences.stream()
				.map(s -> new LicenseSearch(s).bestMatch(licenses, false))
				.filter(l -> l != null)
				.map(l -> new Provenance(l.getOriginal(), provenance))
				.collect(Collectors.toList()));
		}
		writeField(resultsWriter, abstractLicenses.stream().map(lp -> lp.stream().map(p -> p.toString()).collect(Collectors.joining(" ; "))).collect(Collectors.joining(" | ")));

		List<Provenance> allLicenses = new ArrayList<>();
		for (Provenance webpageLicense : webpageLicenses) {
			License l = new LicenseSearch(webpageLicense.getObject()).bestMatch(licenses, true);
			if (l != null) {
				allLicenses.add(new Provenance(l.getOriginal(), webpageLicense.getProvenances()));
			}
		}
		for (List<Provenance> abstractLicense : abstractLicenses) {
			allLicenses.addAll(abstractLicense);
		}

		Map<String, Integer> abstractLicenseCount = new HashMap<>();
		Map<String, Provenance> abstractLicenseProvenances = new HashMap<>();
		Provenance bestAbstractLicense = null;
		int bestAbstractCount = 0;
		for (List<Provenance> ls : abstractLicenses) {
			for (Provenance l : ls) {
				int count = 0;
				if (abstractLicenseCount.get(l.getObject()) != null) {
					count = abstractLicenseCount.get(l.getObject());
				}
				++count;
				abstractLicenseCount.put(l.getObject(), count);
				Provenance provenance;
				if (abstractLicenseProvenances.get(l.getObject()) != null) {
					provenance = abstractLicenseProvenances.get(l.getObject());
					provenance.addProvenances(l.getProvenances());
				} else {
					provenance = l;
					abstractLicenseProvenances.put(l.getObject(), provenance);
				}
				if (count > bestAbstractCount) {
					bestAbstractLicense = provenance;
					bestAbstractCount = count;
				}
			}
		}

		Map<String, Integer> licenseCount = new HashMap<>();
		Map<String, Provenance> licenseProvenances = new HashMap<>();
		Provenance bestLicense = null;
		int bestCount = 0;
		for (Provenance l : allLicenses) {
			int count = 0;
			if (licenseCount.get(l.getObject()) != null) {
				count = licenseCount.get(l.getObject());
			}
			++count;
			licenseCount.put(l.getObject(), count);
			Provenance provenance;
			if (licenseProvenances.get(l.getObject()) != null) {
				provenance = licenseProvenances.get(l.getObject());
				provenance.addProvenances(l.getProvenances());
			} else {
				provenance = l;
				licenseProvenances.put(l.getObject(), provenance);
			}
			if (count > bestCount) {
				bestLicense = provenance;
				bestCount = count;
			}
		}

		writeField(resultsWriter, bestLicense != null ? bestLicense.toString() : null);
		writeField(resultsWriter, existing.stream().map(e -> biotools.get(e)).map(t -> (t.getLicense() == null ? "" : t.getLicense())).collect(Collectors.joining(" | ")));

		List<Provenance> webpageLanguages = new ArrayList<>();
		if (!homepage.isEmpty()) {
			String homepageLanguage = null;
			if (db.getWebpage(homepage, false) != null) {
				homepageLanguage = db.getWebpage(homepage, false).getLanguage();
			} else if (db.getDoc(homepage, false) != null) {
				homepageLanguage = db.getDoc(homepage, false).getLanguage();
			}
			if (homepageLanguage != null && !homepageLanguage.isEmpty()) {
				writeField(resultsWriter, homepageLanguage);
				webpageLanguages.add(new Provenance(homepageLanguage, homepage));
			} else {
				writeField(resultsWriter, null);
			}
		} else {
			writeField(resultsWriter, null);
		}

		List<Provenance> linkLanguages = linkLinks.stream().map(l -> db.getWebpage(l.getUrl(), true) != null ? new Provenance(db.getWebpage(l.getUrl(), true).getLanguage(), l.getUrl()) : new Provenance()).collect(Collectors.toList());
		writeField(resultsWriter, linkLanguages.stream().map(p -> p.toString()).collect(Collectors.joining(" | ")));
		linkLanguages.removeIf(p -> p.isEmpty());
		webpageLanguages.addAll(linkLanguages);

		List<Provenance> downloadLanguages = downloadLinks.stream().map(l -> db.getWebpage(l.getUrl(), true) != null ? new Provenance(db.getWebpage(l.getUrl(), true).getLanguage(), l.getUrl()) : new Provenance()).collect(Collectors.toList());
		writeField(resultsWriter, downloadLanguages.stream().map(p -> p.toString()).collect(Collectors.joining(" | ")));
		downloadLanguages.removeIf(p -> p.isEmpty());
		webpageLanguages.addAll(downloadLanguages);

		List<Provenance> documentationLanguages = documentationLinks.stream().map(l -> db.getDoc(l.getUrl(), true) != null ? new Provenance(db.getDoc(l.getUrl(), true).getLanguage(), l.getUrl()) : new Provenance()).collect(Collectors.toList());
		writeField(resultsWriter, documentationLanguages.stream().map(p -> p.toString()).collect(Collectors.joining(" | ")));
		documentationLanguages.removeIf(p -> p.isEmpty());
		webpageLanguages.addAll(documentationLanguages);

		List<List<Provenance>> abstractLanguages = new ArrayList<>();
		for (int i = 0; i < result.getPubIds().size(); ++i) {
			List<String> abstractSentences = result.getAbstractSentences().get(i);
			String provenance = result.getPubIds().get(i).toString();
			abstractLanguages.add(abstractSentences.stream()
				.map(s -> new LanguageSearch(s).getMatches(languages, false, languageKeywords))
				.flatMap(l -> l.stream().map(s -> new Provenance(s, provenance)))
				.collect(Collectors.toList()));
		}
		List<Provenance> abstractLanguagesUnique = new ArrayList<>();
		for (List<Provenance> ls : abstractLanguages) {
			for (Provenance l : ls) {
				boolean found = false;
				for (Provenance unique : abstractLanguagesUnique) {
					if (l.getObject().equals(unique.getObject())) {
						unique.addProvenances(l.getProvenances());
						found = true;
						break;
					}
				}
				if (!found) {
					abstractLanguagesUnique.add(l);
				}
			}
		}
		writeField(resultsWriter, abstractLanguages.stream().map(lp -> lp.stream().map(p -> p.toString()).collect(Collectors.joining(" ; "))).collect(Collectors.joining(" | ")));

		webpageLanguages = webpageLanguages.stream()
			.flatMap(s -> new LanguageSearch(s.getObject()).getMatches(languages, true, languageKeywords).stream().map(l -> new Provenance(l, s.getProvenances())))
			.collect(Collectors.toList());
		List<Provenance> allLanguages = new ArrayList<>();
		for (Provenance l : webpageLanguages) {
			boolean found = false;
			for (Provenance all : allLanguages) {
				if (l.getObject().equals(all.getObject())) {
					all.addProvenances(l.getProvenances());
					found = true;
					break;
				}
			}
			if (!found) {
				allLanguages.add(l);
			}
		}
		for (List<Provenance> ls : abstractLanguages) {
			for (Provenance l : ls) {
				boolean found = false;
				for (Provenance all : allLanguages) {
					if (l.getObject().equals(all.getObject())) {
						all.addProvenances(l.getProvenances());
						found = true;
						break;
					}
				}
				if (!found) {
					allLanguages.add(l);
				}
			}
		}
		writeField(resultsWriter, allLanguages.stream().map(p -> p.toString()).collect(Collectors.joining(" ; ")));
		writeField(resultsWriter, existing.stream().map(e -> biotools.get(e)).map(t -> (t.getLanguage() == null ? "" : String.join(" ; ", t.getLanguage()))).collect(Collectors.joining(" | ")));

		writeField(resultsWriter, result.isOa().stream().map(b -> String.valueOf(b)).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, String.join(" | ", result.getJournalTitle()));
		writeField(resultsWriter, IntStream.range(0, result.getPubDate().size()).mapToObj(i -> result.getPubDateHuman().get(i) + " (" + result.getPubDate().get(i) + ")").collect(Collectors.joining(" | ")));
		writeField(resultsWriter, result.getCitationsCount().stream().map(i -> String.valueOf(i)).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, IntStream.range(0, result.getCitationsTimestamp().size()).mapToObj(i -> result.getCitationsTimestampHuman().get(i) + " (" + result.getCitationsTimestamp().get(i) + ")").collect(Collectors.joining(" | ")));
		writeField(resultsWriter, IntStream.range(0, result.getCitationsCount().size())
			.mapToObj(i -> (result.getCitationsCount().get(i) > -1 && result.getCitationsTimestamp().get(i) > -1 && result.getPubDate().get(i) > -1) ?
				(result.getCitationsCount().get(i) / (double) (result.getCitationsTimestamp().get(i) - result.getPubDate().get(i)) * 1000000000) : -1)
			.map(f -> String.valueOf(f))
			.collect(Collectors.joining(" | ")));

		writeField(resultsWriter, result.getCorrespAuthor().stream().map(p -> p.stream().map(ca -> ca.getName()).collect(Collectors.joining(" ; "))).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, existing.stream().map(e -> biotools.get(e)).map(t -> (t.getCredit() == null ? "" : t.getCredit().stream().map(c -> (c.getName() == null ? "" : c.getName())).collect(Collectors.joining(" ; ")))).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, result.getCorrespAuthor().stream().map(p -> p.stream().map(ca -> ca.getOrcid()).collect(Collectors.joining(" ; "))).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, existing.stream().map(e -> biotools.get(e)).map(t -> (t.getCredit() == null ? "" : t.getCredit().stream().map(c -> (c.getOrcidid() == null ? "" : c.getOrcidid())).collect(Collectors.joining(" ; ")))).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, result.getCorrespAuthor().stream().map(p -> p.stream().map(ca -> ca.getEmail()).collect(Collectors.joining(" ; "))).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, existing.stream().map(e -> biotools.get(e)).map(t -> (t.getCredit() == null ? "" : t.getCredit().stream().map(c -> (c.getEmail() == null ? "" : c.getEmail())).collect(Collectors.joining(" ; ")))).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, result.getCorrespAuthor().stream().map(p -> p.stream().map(ca -> ca.getPhone()).collect(Collectors.joining(" ; "))).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, result.getCorrespAuthor().stream().map(p -> p.stream().map(ca -> ca.getUri()).collect(Collectors.joining(" ; "))).collect(Collectors.joining(" | ")));
		writeField(resultsWriter, existing.stream().map(e -> biotools.get(e)).map(t -> (t.getCredit() == null ? "" : t.getCredit().stream().map(c -> (c.getUrl() == null ? "" : c.getUrl())).collect(Collectors.joining(" ; ")))).collect(Collectors.joining(" | ")));

		List<CorrespAuthor> credits = new ArrayList<>();
		for (List<CorrespAuthor> correspAuthor : result.getCorrespAuthor()) {
			for (CorrespAuthor ca : correspAuthor) {
				boolean exist = false;
				for (CorrespAuthor credit : credits) {
					if (Common.creditNameEqual(ca.getName(), credit.getName()) || Common.creditOrcidEqual(ca.getOrcid(), credit.getOrcid()) || Common.creditEmailEqual(ca.getEmail(), credit.getEmail())) {
						if (credit.getName().isEmpty()) {
							credit.setName(ca.getName());
						}
						if (credit.getOrcid().isEmpty()) {
							credit.setOrcid(ca.getOrcid());
						}
						if (credit.getEmail().isEmpty()) {
							credit.setEmail(ca.getEmail());
						}
						if (credit.getUri().isEmpty()) {
							credit.setUri(ca.getUri());
						}
						exist = true;
						break;
					}
				}
				if (!exist) {
					CorrespAuthor credit = new CorrespAuthor();
					credit.setName(ca.getName());
					credit.setOrcid(ca.getOrcid());
					credit.setEmail(ca.getEmail());
					credit.setUri(ca.getUri());
					credits.add(credit);
				}
			}
		}
		writeField(resultsWriter, credits.stream().map(ca -> ca.toString()).collect(Collectors.joining(" | ")), true);

		if (suggestion != null) {
			addDiffTool(suggestion, result, db, biotools, preProcessor, diffs, tools,
					name, description, homepage, linkLinks, downloadLinks, documentationLinks,
					bestLicense, bestAbstractLicense, allLanguages, abstractLanguagesUnique, credits);
		}
	}

	static void run(Path outputPath, PreProcessor preProcessor, FetcherArgs fetcherArgs, String logPrefix) throws IOException, ParseException {
		Marker mainMarker = MarkerManager.getMarker(PubMedApps.MAIN_MARKER);

		List<String> license = PubFetcher.getResource(PubMedApps.class, "resources/license.txt");
		List<License> licenses = license.stream().map(l -> new License(l)).collect(Collectors.toList());
		List<String> language = PubFetcher.getResource(PubMedApps.class, "resources/language.txt");
		List<String> languageKeywords = PubFetcher.getResource(PubMedApps.class, "resources/language_keywords.txt");
		List<Language> languages = language.stream().map(l -> new Language(l)).collect(Collectors.toList());

		Scrape scrape = new Scrape(fetcherArgs.getPrivateArgs().getJournalsYaml(), fetcherArgs.getPrivateArgs().getWebpagesYaml());

		String idfFile = outputPath.resolve(Common.IDF_FILE).toString();
		logger.info(mainMarker, "{}Loading IDF from {}", logPrefix, idfFile);
		Idf idf = new Idf(idfFile);

		String biotoolsFile = outputPath.resolve(Common.BIOTOOLS_FILE).toString();
		logger.info(mainMarker, "{}Loading all bio.tools content from {}", logPrefix, biotoolsFile);
		@SuppressWarnings("unchecked")
		List<ToolInput> biotools = (List<ToolInput>) Json.load(biotoolsFile, QueryType.biotools, fetcherArgs.getTimeout(), fetcherArgs.getPrivateArgs().getUserAgent());

		Path pass1Path = outputPath.resolve(Common.PASS1_FILE);
		logger.info(mainMarker, "{}Loading pass1 results from {}", logPrefix, pass1Path.toString());
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
		List<Result1> results1 = mapper.readValue(pass1Path.toFile(), new TypeReference<List<Result1>>() {});

		Path resultsPath = PubFetcher.outputPath(outputPath.resolve(Common.RESULTS_FILE).toString());
		Path diffPath = PubFetcher.outputPath(outputPath.resolve(Common.DIFF_FILE).toString());
		Path newPath = PubFetcher.outputPath(outputPath.resolve(Common.NEW_FILE).toString());

		List<Result2> results = new ArrayList<>();
		for (Result1 result1 : results1) {
			results.add(new Result2(result1));
		}

		logger.info(mainMarker, "{}Making pass2 results from {} pass1 results", logPrefix, results1.size());

		CharsetEncoder resultsEncoder = StandardCharsets.UTF_8.newEncoder();
		resultsEncoder.onMalformedInput(CodingErrorAction.REPLACE);
		resultsEncoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

		CharsetEncoder diffEncoder = StandardCharsets.UTF_8.newEncoder();
		diffEncoder.onMalformedInput(CodingErrorAction.REPLACE);
		diffEncoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

		CharsetEncoder newEncoder = StandardCharsets.UTF_8.newEncoder();
		newEncoder.onMalformedInput(CodingErrorAction.REPLACE);
		newEncoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

		try (Database db = new Database(outputPath.resolve(Common.DB_FILE).toString());
				BufferedWriter resultsWriter = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(resultsPath), resultsEncoder));
				BufferedWriter diffWriter = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(diffPath), diffEncoder));
				BufferedWriter newWriter = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(newPath), newEncoder))) {

			resultsWriter.write("pmid\tpmcid\tdoi\tsame_suggestions"
					+ "\tscore\tscore2\tscore2_parts\tsuggestion_original\tsuggestion\tsuggestion_processed"
					+ "\tpublication_and_name_existing\tname_existing_some_publication_different\tsome_publication_existing_name_different\tname_existing_publication_different"
					+ "\tname_match\tlink_match\tname_word_match"
					+ "\tlinks_abstract\tlinks_fulltext\tfrom_abstract_link"
					+ "\thomepage\thomepage_biotools\tlink\tlink_biotools\tdownload\tdownload_biotools\tdocumentation\tdocumentation_biotools\tbroken_links"
					+ "\tother_scores\tother_scores2\tother_scores2_parts\tother_suggestions_original\tother_suggestions\tother_suggestions_processed"
					+ "\tother_publication_and_name_existing\tother_name_existing_some_publication_different\tother_some_publication_existing_name_different\tother_name_existing_publication_different"
					+ "\tother_links_abstract\tother_links_fulltext"
					+ "\tleftover_links_abstract\tleftover_links_fulltext"
					+ "\ttitle\ttool_title_others\ttool_title_extracted_original\ttool_title\ttool_title_pruned\ttool_title_acronym"
					+ "\tdescription\tdescription_biotools"
					+ "\tlicense_homepage\tlicense_link\tlicense_download\tlicense_documentation\tlicense_abstract\tlicense\tlicense_biotools"
					+ "\tlanguage_homepage\tlanguage_link\tlanguage_download\tlanguage_documentation\tlanguage_abstract\tlanguage\tlanguage_biotools"
					+ "\toa\tjournal_title\tpub_date\tcitations_count\tcitations_timestamp\tcitations_count_normalised"
					+ "\tcorresp_author_name\tcredit_name_biotools\tcorresp_author_orcid\tcredit_orcidid_biotools\tcorresp_author_email\tcredit_email_biotools\tcorresp_author_phone\tcorresp_author_uri\tcredit_url_biotools\tcredit\n");

			diffWriter.write("biotools_id\tscore_score2\tcurrent_publications\tmodify_publications\tadd_publications\tcurrent_name\tmodify_name\tpossibly_related"
					+ "\tcurrent_homepage\tmodify_homepage\tcurrent_links\tadd_links\tcurrent_downloads\tadd_downloads\tcurrent_documentations\tadd_documentations"
					+ "\tcurrent_license\tmodify_license\tcurrent_languages\tadd_languages\tcurrent_credits\tmodify_credits\tadd_credits\n");

			logger.info(mainMarker, "{}Calculating score2 for relevant results", logPrefix);
			for (Result2 result : results) {
				if (!result.getSuggestions().isEmpty() && result.getSuggestions().get(0).calculateScore2()) {
					result.getSuggestions().get(0).setScore2(result.getSuggestions().get(0).getScore());

					double firstScore = result.getSuggestions().get(0).getScore();
					for (int i = 0; i < result.getSuggestions().size(); ++i) {
						Suggestion2 suggestion = result.getSuggestions().get(i);
						boolean increased = false;
						for (String url : suggestion.getLinksAbstract()) {
							if (!isBroken(url, db)) {
								if (suggestion.getScore2() < 0) {
									suggestion.setScore2(suggestion.getScore());
								}
								double score2Part = 0;
								if (suggestion.isFromAbstractLink()) {
									if (i == 0 || suggestion.getScore() == firstScore) {
										score2Part = FROM_ABSTRACT_LINK_INCREASE;
									} else {
										score2Part = FROM_ABSTRACT_LINK_INCREASE / NOT_FIRST_SUGGESTION_LINK_DIVIDER;
									}
								} else {
									if (i == 0 || suggestion.getScore() == firstScore) {
										score2Part = ABSTRACT_LINK_INCREASE;
									} else {
										score2Part = ABSTRACT_LINK_INCREASE / NOT_FIRST_SUGGESTION_LINK_DIVIDER;
									}
								}
								suggestion.getScore2Parts()[0] = score2Part;
								suggestion.setScore2(suggestion.getScore2() + score2Part);
								increased = true;
								break;
							}
						}
						if (!increased) {
							for (String url : suggestion.getLinksFulltext()) {
								if (!isBroken(url, db)) {
									if (suggestion.getScore2() < 0) {
										suggestion.setScore2(suggestion.getScore());
									}
									double score2Part = 0;
									if (i == 0 || suggestion.getScore() == firstScore) {
										score2Part = FULLTEXT_LINK_INCREASE;
									} else {
										score2Part = FULLTEXT_LINK_INCREASE / NOT_FIRST_SUGGESTION_LINK_DIVIDER;
									}
									suggestion.getScore2Parts()[0] = score2Part;
									suggestion.setScore2(suggestion.getScore2() + score2Part);
									break;
								}
							}
						}
					}

					if (result.getSuggestions().size() > 0) {
						List<String> toolTitlePrunedProcessed = preProcessor.process(result.getToolTitlePruned().get(0));
						String toolTitlePrunedProcessedString = String.join(" ", toolTitlePrunedProcessed);
						String toolTitleAcronymProcessedString = String.join(" ", preProcessor.process(result.getToolTitleAcronym().get(0)));
						String toolTitleExtractedOriginal = result.getToolTitleExtractedOriginal().get(0);
						int toolTitleExtractedOriginalSize = toolTitleExtractedOriginal.split(" ").length;
						for (Suggestion2 suggestion : result.getSuggestions()) {
							List<String> suggestionExtracted = new ArrayList<>(Arrays.asList(suggestion.getExtracted().split(" ")));
							String suggestionPrunedProcessed = String.join(" ", preProcessor.process(Common.toolTitlePrune(suggestionExtracted)));
							int match = 0;
							if (suggestionPrunedProcessed.length() > 2) {
								if (toolTitlePrunedProcessedString.equals(suggestionPrunedProcessed)) {
									match = toolTitlePrunedProcessed.size();
								} else if (toolTitleAcronymProcessedString.equals(suggestionPrunedProcessed)) {
									match = 1;
								} else if (!toolTitlePrunedProcessedString.isEmpty() && Common.isAcronym(toolTitlePrunedProcessedString, suggestion.getExtracted(), false)
										|| !toolTitleAcronymProcessedString.isEmpty() && Common.isAcronym(toolTitleAcronymProcessedString, suggestion.getExtracted(), false)
										|| !toolTitleExtractedOriginal.isEmpty() && toolTitleExtractedOriginalSize <= TOOL_TITLE_ORIGINAL_MAX_SIZE_FOR_ACRONYM && Common.isAcronym(suggestionPrunedProcessed, toolTitleExtractedOriginal, false)) {
									match = 2;
								} else if (toolTitlePrunedProcessedString.contains(suggestionPrunedProcessed)) {
									match = toolTitlePrunedProcessed.size() + 1;
								}
							} else if (suggestionPrunedProcessed.length() > 0) {
								if (toolTitlePrunedProcessedString.equals(suggestionPrunedProcessed)
										|| toolTitleAcronymProcessedString.equals(suggestionPrunedProcessed)) {
									match = 1;
								} else if (!toolTitleExtractedOriginal.isEmpty() && toolTitleExtractedOriginalSize <= TOOL_TITLE_ORIGINAL_MAX_SIZE_FOR_ACRONYM && Common.isAcronym(suggestionPrunedProcessed, toolTitleExtractedOriginal, false)) {
									match = 2;
								}
							}
							if (match > 0 && match < 6) {
								if (suggestion.getScore2() < 0) {
									suggestion.setScore2(suggestion.getScore());
								}
								double score2Part = 0;
								if (match == 1) {
									score2Part = TOOL_TITLE_1_INCREASE;
								} else if (match == 2) {
									score2Part = TOOL_TITLE_2_INCREASE;
								} else if (match == 3) {
									score2Part = TOOL_TITLE_3_INCREASE;
								} else if (match == 4) {
									score2Part = TOOL_TITLE_4_INCREASE;
								} else if (match == 5) {
									score2Part = TOOL_TITLE_5_INCREASE;
								}
								suggestion.getScore2Parts()[1] = score2Part;
								suggestion.setScore2(suggestion.getScore2() + score2Part);
							}
						}
					}

					for (Suggestion2 suggestion : result.getSuggestions()) {
						if (suggestion.getScore2() < 0) {
							continue;
						}
						String suggestionExtractedString = suggestion.getExtracted();
						if (suggestion.isFromAbstractLink()) {
							suggestionExtractedString = Common.PATH_SPLIT.matcher(suggestionExtractedString).replaceAll(" ").trim();
						}
						List<String> suggestionExtracted = new ArrayList<>(Arrays.asList(suggestionExtractedString.split(" ")));
						String[] suggestionPruned = Common.toolTitlePrune(suggestionExtracted).split(" ");
						double min = -1;
						for (String suggestionPart : suggestionPruned) {
							Matcher removeHyphen = CASE_REMOVE_HYPHEN.matcher(suggestionPart);
							if (removeHyphen.matches()) {
								suggestionPart = removeHyphen.group(1) + removeHyphen.group(2).toLowerCase(Locale.ROOT) + removeHyphen.group(3);
							}
							suggestionPart = suggestionPart.replaceAll("-", "");
							Matcher removePlural = CASE_REMOVE_PLURAL.matcher(suggestionPart);
							if (removePlural.matches()) {
								suggestionPart = removePlural.group(1);
							}
							if (CASE_LOWERCASE.matcher(suggestionPart).matches()) {
								if (CASE_LOWERCASE_INCREASE < min || min < 0) {
									min = CASE_LOWERCASE_INCREASE;
								}
							} else if (CASE_FIRST_CAPITAL.matcher(suggestionPart).matches()) {
								if (CASE_FIRST_CAPITAL_INCREASE < min || min < 0) {
									min = CASE_FIRST_CAPITAL_INCREASE;
								}
							} else if (CASE_UPPERCASE.matcher(suggestionPart).matches()) {
								if (CASE_UPPERCASE_INCREASE < min || min < 0) {
									min = CASE_UPPERCASE_INCREASE;
								}
							} else if (CASE_MIXED_AS_REST.matcher(suggestionPart).matches()) {
								if (CASE_MIXED_INCREASE < min || min < 0) {
									min = CASE_MIXED_INCREASE;
								}
							}
						}
						min -= (suggestionPruned.length - 1) * CASE_DECREASE;
						if (min > 0) {
							double score2Part = min;
							suggestion.getScore2Parts()[2] = score2Part;
							suggestion.setScore2(suggestion.getScore2() + score2Part);
						}
					}

					for (Suggestion2 suggestion : result.getSuggestions()) {
						if (suggestion.getScore2() < 0) {
							continue;
						}
						double sum = 0;
						String[] suggestionProcessed = suggestion.getProcessed().split(" ");
						for (String suggestionPart : suggestionProcessed) {
							sum += Math.pow(idf.getIdfShifted(suggestionPart, 0), IDF_POWER) * IDF_MULTIPLIER;
						}
						if (sum > 0) {
							double score2Part = sum / suggestionProcessed.length;
							suggestion.getScore2Parts()[3] = score2Part;
							suggestion.setScore2(suggestion.getScore2() + score2Part);
						}
					}

					for (Suggestion2 suggestion : result.getSuggestions()) {
						if (suggestion.getScore2() < 0) {
							continue;
						}
						if (suggestion.getScore2Parts()[2] == CASE_MIXED_INCREASE && suggestion.getScore() >= SCORE_MIN_FOR_MIXED_IDF_INCREASE || suggestion.getScore2Parts()[2] == CASE_UPPERCASE_INCREASE && suggestion.getScore() >= SCORE_MIN_FOR_UPPERCASE_IDF_INCREASE) {
							double score2Part = suggestion.getScore2Parts()[3];
							if (suggestion.getScore2Parts()[2] == CASE_MIXED_INCREASE && suggestion.getScore() >= SCORE_MIN_FOR_MIXED_IDF_INCREASE2) {
								score2Part += suggestion.getScore2Parts()[3] / 2;
							}
							suggestion.getScore2Parts()[2] += score2Part;
							suggestion.setScore2(suggestion.getScore2() + score2Part);
						}
					}

					Collections.sort(result.getSuggestions());
				}
			}

			logger.info(mainMarker, "{}Resorting results", logPrefix);
			Collections.sort(results);

			logger.info(mainMarker, "{}Merging results based on same suggestions", logPrefix);
			TreeSet<Integer> removeResult = new TreeSet<>();
			for (int i = 0; i < results.size() - 1; ++i) {
				if (removeResult.contains(i)) {
					continue;
				}
				Result2 resultI = results.get(i);
				if (!resultI.getSuggestions().isEmpty()) {
					if (!resultI.getSuggestions().get(0).include()) {
						break;
					}
					for (int j = i + 1; j < results.size(); ++j) {
						if (removeResult.contains(j)) {
							continue;
						}
						Result2 resultJ = results.get(j);
						if (!resultJ.getSuggestions().isEmpty()) {
							if (!resultJ.getSuggestions().get(0).include()) {
								break;
							}
							if (resultI.getSuggestions().get(0).getExtracted().equals(resultJ.getSuggestions().get(0).getExtracted())) {
								resultI.addPubIds(resultJ.getPubIds().get(0));

								resultI.addTitle(resultJ.getTitle().get(0));
								resultI.addToolTitleOthers(resultJ.getToolTitleOthers().get(0));
								resultI.addToolTitleExtractedOriginal(resultJ.getToolTitleExtractedOriginal().get(0));
								resultI.addToolTitle(resultJ.getToolTitle().get(0));
								resultI.addToolTitlePruned(resultJ.getToolTitlePruned().get(0));
								resultI.addToolTitleAcronym(resultJ.getToolTitleAcronym().get(0));
								resultI.addAbstractSentences(resultJ.getAbstractSentences().get(0));
								resultI.addOa(resultJ.isOa().get(0));
								resultI.addJournalTitle(resultJ.getJournalTitle().get(0));
								resultI.addPubDate(resultJ.getPubDate().get(0));
								resultI.addPubDateHuman(resultJ.getPubDateHuman().get(0));
								resultI.addCitationsCount(resultJ.getCitationsCount().get(0));
								resultI.addCitationsTimestamp(resultJ.getCitationsTimestamp().get(0));
								resultI.addCitationsTimestampHuman(resultJ.getCitationsTimestampHuman().get(0));

								resultI.addCorrespAuthor(resultJ.getCorrespAuthor().get(0));

								for (Iterator<Suggestion2> iterI = resultI.getSuggestions().iterator(); iterI.hasNext(); ) {
									Suggestion2 suggestionI = iterI.next();
									for (Iterator<Suggestion2> iterJ = resultJ.getSuggestions().iterator(); iterJ.hasNext(); ) {
										Suggestion2 suggestionJ = iterJ.next();
										if (suggestionI.getExtracted().equals(suggestionJ.getExtracted())) {
											if (suggestionI.compareTo(suggestionJ) > 0) {
												suggestionJ.addLinksAbstract(suggestionI.getLinksAbstract());
												suggestionJ.addLinksFulltext(suggestionI.getLinksFulltext());
												iterI.remove();
											} else {
												suggestionI.addLinksAbstract(suggestionJ.getLinksAbstract());
												suggestionI.addLinksFulltext(suggestionJ.getLinksFulltext());
												iterJ.remove();
											}
											break;
										}
									}
								}
								for (Suggestion2 suggestionJ : resultJ.getSuggestions()) {
									resultI.addSuggestion(suggestionJ);
								}
								Collections.sort(resultI.getSuggestions());

								resultI.addLeftoverLinksAbstract(resultJ.getLeftoverLinksAbstract().get(0));
								resultI.addLeftoverLinksFulltext(resultJ.getLeftoverLinksFulltext().get(0));

								removeResult.add(j);
							}
						}
					}
				}
			}
			for (Iterator<Integer> it = removeResult.descendingIterator(); it.hasNext(); ) {
				results.remove(it.next().intValue());
			}
			logger.info(mainMarker, "{}Merged {} pass1 results to {} pass2 results", logPrefix, results1.size(), results.size());

			logger.info(mainMarker, "{}Filling same suggestions field for non-merged results", logPrefix, results1.size(), results.size());
			for (int i = 0; i < results.size() - 1; ++i) {
				Result2 resultI = results.get(i);
				if (!resultI.getSuggestions().isEmpty()) {
					for (int j = i + 1; j < results.size(); ++j) {
						Result2 resultJ = results.get(j);
						if (!resultJ.getSuggestions().isEmpty()) {
							if (resultI.getSuggestions().get(0).getExtracted().equals(resultJ.getSuggestions().get(0).getExtracted())) {
								resultI.addSameSuggestion(resultJ.getPubIds().get(0));
								resultJ.addSameSuggestion(resultI.getPubIds().get(0));
							}
						}
					}
				}
			}

			logger.info(mainMarker, "{}Processing bio.tools names and links", logPrefix);
			List<List<String>> queryNamesExtracted = new ArrayList<>();
			List<String> queryNamesProcessed = new ArrayList<>();
			List<List<String>> queryLinks = new ArrayList<>();
			for (ToolInput biotool : biotools) {
				List<String> queryNameExtracted = preProcessor.extract(biotool.getName());
				List<String> queryNameProcessed = preProcessor.process(biotool.getName(), queryNameExtracted);
				queryNamesExtracted.add(Arrays.asList(Common.BIOTOOLS_EXTRACTED_VERSION_TRIM.matcher(String.join(" ", queryNameExtracted)).replaceFirst("").split(" ")));
				queryNamesProcessed.add(Common.BIOTOOLS_PROCESSED_VERSION_TRIM.matcher(String.join(" ", queryNameProcessed)).replaceFirst(""));
				List<String> links = new ArrayList<>();
				links.add(biotool.getHomepage());
				if (biotool.getLink() != null) {
					links.addAll(biotool.getLink().stream().map(l -> l.getUrl()).collect(Collectors.toList()));
				}
				if (biotool.getDownload() != null) {
					links.addAll(biotool.getDownload().stream().map(l -> l.getUrl()).collect(Collectors.toList()));
				}
				if (biotool.getDocumentation() != null) {
					links.addAll(biotool.getDocumentation().stream().map(l -> l.getUrl()).collect(Collectors.toList()));
				}
				queryLinks.add(links.stream()
					.map(l -> Common.trimUrl(l.trim()))
					.filter(l -> !l.isEmpty())
					.collect(Collectors.toList()));
			}

			logger.info(mainMarker, "{}Finding existing bio.tools entries", logPrefix);

			int resultIndex = 0;
			long start = System.currentTimeMillis();
			for (Result2 result : results) {
				++resultIndex;
				System.err.print(PubFetcher.progress(resultIndex, results.size(), start) + "  \r");

				List<Boolean> oneMatches = new ArrayList<>();
				List<Boolean> allMatches = new ArrayList<>();
				List<Set<PubIds>> notMatches = new ArrayList<>();
				for (int i = 0; i < biotools.size(); ++i) {
					ToolInput biotool = biotools.get(i);
					boolean oneMatch = false;
					boolean allMatch = true;
					Set<PubIds> notMatch = null;
					for (PubIds pubIds : result.getPubIds()) {
						boolean match = false;
						if (biotool.getPublication() != null) {
							for (org.edamontology.edammap.core.input.json.Publication publicationIds : biotool.getPublication()) {
								if (!pubIds.getPmid().isEmpty() && publicationIds.getPmid() != null && publicationIds.getPmid().trim().equals(pubIds.getPmid())
										|| !pubIds.getPmcid().isEmpty() && publicationIds.getPmcid() != null && publicationIds.getPmcid().trim().equals(pubIds.getPmcid())
										|| !pubIds.getDoi().isEmpty() && publicationIds.getDoi() != null && PubFetcher.normaliseDoi(publicationIds.getDoi().trim()).equals(pubIds.getDoi())) {
									match = true;
									break;
								}
							}
						}
						if (match) {
							oneMatch = true;
						} else {
							allMatch = false;
							if (notMatch == null) {
								notMatch = new LinkedHashSet<>();
							}
							notMatch.add(pubIds);
						}
					}
					oneMatches.add(oneMatch);
					allMatches.add(allMatch);
					notMatches.add(notMatch);
				}
				for (int i = 0; i < result.getSuggestions().size(); ++i) {
					Suggestion2 suggestion = result.getSuggestions().get(i);
					List<Integer> publicationAndNameExisting = null;
					List<Integer> nameExistingSomePublicationDifferent = null;
					List<Set<PubIds>> nameExistingSomePublicationDifferentPubIds = null;
					List<Integer> somePublicationExistingNameDifferent = null;
					List<Set<PubIds>> somePublicationExistingNameDifferentPubIds = null;
					List<Integer> nameExistingPublicationDifferent = null;
					List<Set<PubIds>> nameExistingPublicationDifferentPubIds = null;
					for (int j = 0; j < biotools.size(); ++j) {
						ToolInput biotool = biotools.get(j);
						if (suggestion.getExtracted().equals(biotool.getName())) {
							if (allMatches.get(j)) {
								if (publicationAndNameExisting == null) {
									publicationAndNameExisting = new ArrayList<>();
								}
								publicationAndNameExisting.add(j);
							} else if (oneMatches.get(j)) {
								if (nameExistingSomePublicationDifferent == null) {
									nameExistingSomePublicationDifferent = new ArrayList<>();
								}
								nameExistingSomePublicationDifferent.add(j);
								if (nameExistingSomePublicationDifferentPubIds == null) {
									nameExistingSomePublicationDifferentPubIds = new ArrayList<>();
								}
								nameExistingSomePublicationDifferentPubIds.add(notMatches.get(j));
							} else {
								if (nameExistingPublicationDifferent == null) {
									nameExistingPublicationDifferent = new ArrayList<>();
								}
								nameExistingPublicationDifferent.add(j);
								if (nameExistingPublicationDifferentPubIds == null) {
									nameExistingPublicationDifferentPubIds = new ArrayList<>();
								}
								nameExistingPublicationDifferentPubIds.add(notMatches.get(j));
							}
						} else if (oneMatches.get(j)) {
							if (somePublicationExistingNameDifferent == null) {
								somePublicationExistingNameDifferent = new ArrayList<>();
							}
							somePublicationExistingNameDifferent.add(j);
							if (somePublicationExistingNameDifferentPubIds == null) {
								somePublicationExistingNameDifferentPubIds = new ArrayList<>();
							}
							somePublicationExistingNameDifferentPubIds.add(notMatches.get(j));
						}
					}
					suggestion.setPublicationAndNameExisting(publicationAndNameExisting);
					suggestion.setNameExistingSomePublicationDifferent(nameExistingSomePublicationDifferent);
					suggestion.setNameExistingSomePublicationDifferentPubIds(nameExistingSomePublicationDifferentPubIds);
					suggestion.setSomePublicationExistingNameDifferent(somePublicationExistingNameDifferent);
					suggestion.setSomePublicationExistingNameDifferentPubIds(somePublicationExistingNameDifferentPubIds);
					suggestion.setNameExistingPublicationDifferent(nameExistingPublicationDifferent);
					suggestion.setNameExistingPublicationDifferentPubIds(nameExistingPublicationDifferentPubIds);

					if (i == 0) {
						String suggestionProcessed = Common.BIOTOOLS_PROCESSED_VERSION_TRIM.matcher(result.getSuggestions().get(i).getProcessed()).replaceFirst("");
						if (!suggestionProcessed.isEmpty()) {
							for (int j = 0; j < queryNamesProcessed.size(); ++j) {
								if (suggestionProcessed.equals(queryNamesProcessed.get(j))) {
									if ((publicationAndNameExisting == null || !publicationAndNameExisting.contains(j))
											&& (nameExistingSomePublicationDifferent == null || !nameExistingSomePublicationDifferent.contains(j))
											&& (somePublicationExistingNameDifferent == null || !somePublicationExistingNameDifferent.contains(j))
											&& (nameExistingPublicationDifferent == null || !nameExistingPublicationDifferent.contains(j))) {
										result.addNameMatch(j);
									}
								}
							}
						}
						LinkedHashMap<Integer, List<String>> linkMatchMap = new LinkedHashMap<>();
						for (String suggestionLink : result.getSuggestions().get(i).getLinksAbstract()) {
							linksMatch(linkMatchMap, suggestionLink, suggestion, queryLinks);
						}
						for (String suggestionLink : result.getSuggestions().get(i).getLinksFulltext()) {
							linksMatch(linkMatchMap, suggestionLink, suggestion, queryLinks);
						}
						for (Map.Entry<Integer, List<String>> linkMatchEntry : linkMatchMap.entrySet()) {
							result.addLinkMatch(linkMatchEntry.getKey(), linkMatchEntry.getValue());
						}
						String suggestionExtracted = Common.BIOTOOLS_EXTRACTED_VERSION_TRIM.matcher(result.getSuggestions().get(i).getExtracted()).replaceFirst("");
						if (!suggestionExtracted.isEmpty()) {
							for (String suggestionExtractedWord : suggestionExtracted.split(" ")) {
								List<Integer> nameWordMatchPart = new ArrayList<>();
								for (int j = 0; j < queryNamesExtracted.size(); ++j) {
									if (queryNamesExtracted.get(j).contains(suggestionExtractedWord)) {
										if ((publicationAndNameExisting == null || !publicationAndNameExisting.contains(j))
												&& (nameExistingSomePublicationDifferent == null || !nameExistingSomePublicationDifferent.contains(j))
												&& (somePublicationExistingNameDifferent == null || !somePublicationExistingNameDifferent.contains(j))
												&& (nameExistingPublicationDifferent == null || !nameExistingPublicationDifferent.contains(j))
												&& !result.getNameMatch().contains(j) && !result.getLinkMatch().contains(j) && !result.getNameWordMatch().contains(j)) {
											nameWordMatchPart.add(j);
										}
									}
								}
								if (nameWordMatchPart.size() >= 1 && nameWordMatchPart.size() <= NAME_WORD_MATCH_LIMIT) {
									for (Integer j : nameWordMatchPart) {
										result.addNameWordMatch(j);
									}
								}
							}
						}
					}
				}
			}

			logger.info(mainMarker, "{}Dividing links", logPrefix);
			for (Result2 result : results) {
				String name = (!result.getSuggestions().isEmpty() ? result.getSuggestions().get(0).getExtracted() : "");
				for (Suggestion2 suggestion : result.getSuggestions()) {
					List<BiotoolsLink<LinkType>> linkLinksAbstract = new ArrayList<>();
					List<BiotoolsLink<DownloadType>> downloadLinksAbstract = new ArrayList<>();
					List<BiotoolsLink<DocumentationType>> documentationLinksAbstract = new ArrayList<>();
					Common.makeBiotoolsLinks(suggestion.getLinksAbstract(), linkLinksAbstract, downloadLinksAbstract, documentationLinksAbstract);
					removeBroken(linkLinksAbstract, suggestion.getBrokenLinks(), db, false, name);
					removeBroken(downloadLinksAbstract, suggestion.getBrokenLinks(), db, false, name);
					removeBroken(documentationLinksAbstract, suggestion.getBrokenLinks(), db, true, name);
					String homepage = chooseHomepage(suggestion.getLinksAbstract(), linkLinksAbstract, documentationLinksAbstract, db);
					List<BiotoolsLink<LinkType>> linkLinksFulltext = new ArrayList<>();
					List<BiotoolsLink<DownloadType>> downloadLinksFulltext = new ArrayList<>();
					List<BiotoolsLink<DocumentationType>> documentationLinksFulltext = new ArrayList<>();
					Common.makeBiotoolsLinks(suggestion.getLinksFulltext(), linkLinksFulltext, downloadLinksFulltext, documentationLinksFulltext);
					removeBroken(linkLinksFulltext, suggestion.getBrokenLinks(), db, false, name);
					removeBroken(downloadLinksFulltext, suggestion.getBrokenLinks(), db, false, name);
					removeBroken(documentationLinksFulltext, suggestion.getBrokenLinks(), db, true, name);
					if (homepage == null) {
						homepage = chooseHomepage(suggestion.getLinksFulltext(), linkLinksFulltext, documentationLinksFulltext, db);
					}
					if (homepage == null) {
						for (String link : suggestion.getLinksAbstract()) {
							link = Common.prependHttp(link);
							if (!Common.DOWNLOAD_EXT.matcher(link).find() && Common.BIOTOOLS_SCHEMA_URL_PATTERN.matcher(link).matches()) {
								homepage = link;
								suggestion.setHomepageBroken(true);
								break;
							}
						}
					}
					if (homepage == null) {
						for (String link : suggestion.getLinksFulltext()) {
							link = Common.prependHttp(link);
							if (!Common.DOWNLOAD_EXT.matcher(link).find() && Common.BIOTOOLS_SCHEMA_URL_PATTERN.matcher(link).matches()) {
								homepage = link;
								suggestion.setHomepageBroken(true);
								break;
							}
						}
					}
					if (homepage != null) {
						suggestion.setHomepage(homepage);
					} else {
						for (PubIds pubIds : result.getPubIds()) {
							homepage = PubFetcher.getPmidLink(pubIds.getPmid());
							if (homepage == null) homepage = PubFetcher.getPmcidLink(pubIds.getPmcid());
							if (homepage == null) homepage = PubFetcher.getDoiLink(pubIds.getDoi());
							if (homepage != null) {
								suggestion.setHomepage(homepage);
								break;
							}
						}
						if (homepage == null) {
							suggestion.setHomepage("https://bio.tools");
						}
						suggestion.setHomepageMissing(true);
					}
					suggestion.addLinkLinks(linkLinksAbstract);
					suggestion.addLinkLinks(linkLinksFulltext);
					suggestion.addDownloadLinks(downloadLinksAbstract);
					suggestion.addDownloadLinks(downloadLinksFulltext);
					suggestion.addDocumentationLinks(documentationLinksAbstract);
					suggestion.addDocumentationLinks(documentationLinksFulltext);
					suggestion.removeHomepageFromLinks();
				}
			}

			List<Diff> diffs = new ArrayList<>();
			List<Tool> tools = new ArrayList<>();

			logger.info(mainMarker, "{}Writing {} pass2 results to {}", logPrefix, results.size(), resultsPath.toString());
			resultIndex = 0;
			start = System.currentTimeMillis();
			for (Result2 result : results) {
				++resultIndex;
				System.err.print(PubFetcher.progress(resultIndex, results.size(), start) + "  \r");
				writeResult(result, db, resultsWriter, biotools, licenses, languages, languageKeywords, scrape, preProcessor, diffs, tools);
			}

			logger.info(mainMarker, "{}Writing {} bio.tools diffs to {}", logPrefix, diffs.size(), diffPath.toString());
			for (Diff diff : diffs) {
				if (!diff.include()) {
					continue;
				}
				ToolInput biotool = biotools.get(diff.getExisting());
				writeField(diffWriter, biotool.getBiotoolsID());
				writeField(diffWriter, String.valueOf(diff.getScoreScore2()));
				String publicationBiotools = null;
				if (biotool.getPublication() != null && (!diff.getModifyPublications().isEmpty() || diff.getAddPublications() != null && !diff.getAddPublications().isEmpty() || diff.getModifyName() != null && !diff.getModifyName().isEmpty())) {
					publicationBiotools = biotool.getPublication().stream().map(pubIds -> "[" + PublicationIds.toString(pubIds.getPmid(), pubIds.getPmcid(), pubIds.getDoi(), false) + "]").collect(Collectors.joining(" | "));
				}
				writeField(diffWriter, publicationBiotools);
				writeField(diffWriter, diff.getModifyPublications().stream().map(pubIds -> pubIds.toString()).collect(Collectors.joining(" | ")));
				writeField(diffWriter, diff.getAddPublications() != null ? diff.getAddPublications().stream().map(pubIds -> pubIds.toString()).collect(Collectors.joining(" | ")) : null);
				writeField(diffWriter, diff.getModifyName() != null && !diff.getModifyName().isEmpty() ? biotool.getName() : null);
				writeField(diffWriter, diff.getModifyName());
				writeField(diffWriter, diff.getPossiblyRelated() != null ? diff.getPossiblyRelated().stream().map(e -> biotools.get(e)).map(q -> q.getBiotoolsID() + " (" + q.getName() + ")").collect(Collectors.joining(" | ")) : null);
				writeField(diffWriter, diff.getModifyHomepage() != null && !diff.getModifyHomepage().isEmpty() ? currentHomepage(biotool, db) : null);
				writeField(diffWriter, diff.getModifyHomepage());
				String linkBiotools = null;
				if (biotool.getLink() != null && !diff.getAddLinks().isEmpty()) {
					linkBiotools = biotool.getLink().stream().map(l -> l.getUrl() + " (" + l.getType() + ")").collect(Collectors.joining(" | "));
				}
				writeField(diffWriter, linkBiotools);
				writeField(diffWriter, diff.getAddLinks().stream().map(l -> l.getUrl() + " (" + l.getType() + ")").collect(Collectors.joining(" | ")));
				String downloadBiotools = null;
				if (biotool.getDownload() != null && !diff.getAddDownloads().isEmpty()) {
					downloadBiotools = biotool.getDownload().stream().map(l -> l.getUrl() + " (" + l.getType() + ")").collect(Collectors.joining(" | "));
				}
				writeField(diffWriter, downloadBiotools);
				writeField(diffWriter, diff.getAddDownloads().stream().map(l -> l.getUrl() + " (" + l.getType() + ")").collect(Collectors.joining(" | ")));
				String documentationBiotools = null;
				if (biotool.getDocumentation() != null && !diff.getAddDocumentations().isEmpty()) {
					documentationBiotools = biotool.getDocumentation().stream().map(l -> l.getUrl() + " (" + l.getType() + ")").collect(Collectors.joining(" | "));
				}
				writeField(diffWriter, documentationBiotools);
				writeField(diffWriter, diff.getAddDocumentations().stream().map(l -> l.getUrl() + " (" + l.getType() + ")").collect(Collectors.joining(" | ")));
				writeField(diffWriter, diff.getModifyLicense() != null && !diff.getModifyLicense().isEmpty() ? biotool.getLicense() : null);
				writeField(diffWriter, diff.getModifyLicense() != null ? diff.getModifyLicense().toString() : null);
				String languageBiotools = null;
				if (biotool.getLanguage() != null && !diff.getAddLanguages().isEmpty()) {
					languageBiotools = String.join(" | ", biotool.getLanguage());
				}
				writeField(diffWriter, languageBiotools);
				writeField(diffWriter, diff.getAddLanguages().stream().map(l -> l.toString()).collect(Collectors.joining(" | ")));
				String creditBiotools = null;
				if (biotool.getCredit() != null && (!diff.getModifyCredits().isEmpty() || !diff.getAddCredits().isEmpty())) {
					creditBiotools = biotool.getCredit().stream().map(c -> Arrays.asList(c.getName(), c.getOrcidid(), c.getEmail(), c.getUrl()).stream().filter(e -> e != null && !e.isEmpty()).collect(Collectors.joining(", "))).collect(Collectors.joining(" | "));
				}
				writeField(diffWriter, creditBiotools);
				writeField(diffWriter, diff.getModifyCredits().stream().map(c -> c.toString()).collect(Collectors.joining(" | ")));
				writeField(diffWriter, diff.getAddCredits().stream().map(c -> c.toString()).collect(Collectors.joining(" | ")), true);
			}

			logger.info(mainMarker, "{}Writing {} new bio.tools entries to {}", logPrefix, tools.size(), newPath.toString());
			org.edamontology.edammap.core.output.Json.outputBiotools(newWriter, tools);
		}
	}
}
