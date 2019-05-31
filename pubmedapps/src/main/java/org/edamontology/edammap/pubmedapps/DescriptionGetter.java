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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.edamontology.pubfetcher.core.db.Database;
import org.edamontology.pubfetcher.core.db.webpage.Webpage;
import org.edamontology.pubfetcher.core.scrape.Scrape;

import org.edamontology.edammap.core.input.json.DocumentationType;
import org.edamontology.edammap.core.input.json.DownloadType;
import org.edamontology.edammap.core.input.json.LinkType;
import org.edamontology.edammap.core.input.json.ToolInput;
import org.edamontology.edammap.core.preprocessing.PreProcessor;

public final class DescriptionGetter {

	private static final Logger logger = LogManager.getLogger();

	private static final int NAME_DIFFERENT_MESSAGE_LIMIT = 5;

	private static final Pattern FIND_NAME_NOT_ALPHANUM = Pattern.compile("[^\\p{L}\\p{N}]");
	private static final Pattern FIND_NAME_CAMEL = Pattern.compile("(\\p{Ll})(\\p{Lu})");
	private static final Pattern FIND_NAME_TO_NUMBER = Pattern.compile("(\\p{L})(\\p{N})");
	private static final Pattern FIND_NAME_FROM_NUMBER = Pattern.compile("(\\p{N})(\\p{L})");
	private static final Pattern FIND_NAME_NUMBER = Pattern.compile("\\p{N}");

	private static final Pattern CONTENT_TYPE_HTML = Pattern.compile("(?i)/(html|xhtml|xml)");

	private static final int BIOTOOLS_SCHEMA_DESCRIPTION_MIN = 10;
	private static final int BIOTOOLS_SCHEMA_DESCRIPTION_MAX = 1000;

	private static final int BIOTOOLS_DESCRIPTION_MAX_LENGTH = BIOTOOLS_SCHEMA_DESCRIPTION_MAX;
	private static final int BIOTOOLS_DESCRIPTION_MIN_LENGTH = 32;
	private static final int BIOTOOLS_DESCRIPTION_LONG_LENGTH = 160;
	private static final int BIOTOOLS_DESCRIPTION_MINMIN_LENGTH = 24;
	private static final int BIOTOOLS_DESCRIPTION_MESSAGE_MAX_LENGTH = 500;

	private static String descriptionFromTitle(String title, Pattern separator) {
		String description = "";
		int from = 0;
		Matcher matcher = separator.matcher(title);
		while (matcher.find()) {
			String currentDescription = title.substring(from, matcher.start());
			if (currentDescription.length() > description.length()) {
				description = currentDescription;
			}
			from = matcher.end();
		}
		String currentDescription = title.substring(from, title.length());
		if (currentDescription.length() > description.length()) {
			description = currentDescription;
		}
		return description;
	}

	private static boolean addDescription(List<Description> descriptions, String descriptionString, int priority, boolean hasScrape, PreProcessor preProcessor) {
		descriptionString = descriptionString.trim();
		if (descriptionString.isEmpty()) {
			return false;
		}
		if (descriptionString.charAt(descriptionString.length() - 1) == '.') {
			descriptionString = descriptionString.substring(0, descriptionString.length() - 1);
		}
		if (descriptionString.isEmpty()) {
			return false;
		}
		String descriptionSeparated = null;
		List<String> descriptionSentences = preProcessor.sentences(descriptionString);
		if (descriptionSentences.size() == 1) {
			descriptionSeparated = descriptionFromTitle(descriptionString, Common.WEBPAGE_TITLE_SEPARATOR);
			if (descriptionSeparated.equals(descriptionString)) {
				descriptionSeparated = null;
			}
		}
		for (Description description : descriptions) {
			if (description.getDescription().equalsIgnoreCase(descriptionString)) {
				return false;
			}
			if (description.getDescriptionSeparated() != null) {
				if (description.getDescriptionSeparated().equalsIgnoreCase(descriptionString)) {
					return false;
				}
			}
			if (descriptionSeparated != null) {
				if (description.getDescription().equalsIgnoreCase(descriptionSeparated)) {
					return false;
				}
				if (description.getDescriptionSeparated() != null) {
					if (description.getDescriptionSeparated().equalsIgnoreCase(descriptionSeparated)) {
						return false;
					}
				}
			}
		}
		descriptions.add(new Description(descriptionString, descriptionSeparated, priority + (hasScrape ? 0 : 2)));
		return true;
	}

	private static boolean findName(String name, String description) {
		name = name.trim();
		if (name.isEmpty()) {
			return false;
		}
		name = FIND_NAME_NOT_ALPHANUM.matcher(name).replaceAll(".?");
		name = FIND_NAME_CAMEL.matcher(name).replaceAll("$1.?$2");
		name = FIND_NAME_TO_NUMBER.matcher(name).replaceAll("$1.?$2");
		name = FIND_NAME_FROM_NUMBER.matcher(name).replaceAll("$1.?$2");
		name = FIND_NAME_NUMBER.matcher(name).replaceAll(".?");
		return Pattern.compile("(?i)" + name).matcher(description).find();
	}

	private static void descriptionsFromWebpage(List<Description> descriptions, String url, Database db, Scrape scrape, int minLength, String name, Boolean doc, PreProcessor preProcessor) {
		boolean hasScrape = scrape.getWebpage(url) != null;
		Webpage webpage = null;
		if (doc == null || !doc) {
			webpage = db.getWebpage(url, false);
		}
		if (webpage == null && (doc == null || doc)) {
			webpage = db.getDoc(url, false);
		}
		if (webpage != null && !webpage.isBroken() && CONTENT_TYPE_HTML.matcher(webpage.getContentType()).find()) {
			String titleDescription = Common.WHITESPACE.matcher(descriptionFromTitle(webpage.getTitle(), Common.WEBPAGE_TITLE_SEPARATOR)).replaceAll(" ").trim();
			if (titleDescription.length() >= minLength) {
				addDescription(descriptions, titleDescription, 1, hasScrape, preProcessor);
			}
			boolean nameFound = false;
			boolean nameFoundLong = false;
			int sentences = 0;
			int sentencesMinLength = 0;
			for (String sentence : webpage.getContent().split("[\n\r]")) {
				sentence = Common.WHITESPACE.matcher(sentence).replaceAll(" ").trim();
				if (sentence.isEmpty() || sentence.startsWith("<")) {
					continue;
				}
				sentence = sentence.replaceAll("\\|", ":");
				++sentences;
				if (sentences <= 2) {
					if (sentence.length() >= minLength) {
						addDescription(descriptions, sentence, 2, hasScrape, preProcessor);
						if (findName(name, sentence)) {
							nameFound = true;
							if (sentence.length() >= BIOTOOLS_DESCRIPTION_LONG_LENGTH
									&& (sentences != 1 || !sentence.endsWith("..."))) { // sometime HTML <meta> description (first line of webpage content) is truncated
								nameFoundLong = true;
							}
						}
					}
				} else {
					if (nameFoundLong) {
						break;
					}
					if (sentence.length() >= minLength) {
						++sentencesMinLength;
						if (sentence.length() >= BIOTOOLS_DESCRIPTION_LONG_LENGTH && findName(name, sentence)) {
							addDescription(descriptions, sentence, 2 + sentencesMinLength, hasScrape, preProcessor);
							nameFound = true;
							nameFoundLong = true;
						} else if (!nameFound) {
							if (findName(name, sentence)) {
								addDescription(descriptions, sentence, 2 + sentencesMinLength, hasScrape, preProcessor);
								nameFound = true;
							} else if (sentencesMinLength <= 2) {
								addDescription(descriptions, sentence, 2 + sentences, hasScrape, preProcessor);
							}
						}
					}
				}
			}
		}
	}

	private static int descriptionsLength(List<Description> descriptions) {
		int length = 0;
		for (Description description : descriptions) {
			length += description.getDescription().length();
		}
		return length + (descriptions.size() - 1) * 3;
	}

	private static void limitDescriptions(List<Description> descriptions, int maxLength, PreProcessor preProcessor) {
		if (maxLength < 1) {
			for (int i = descriptions.size() - 1; i >= 0; --i) {
				descriptions.remove(i);
			}
			return;
		}
		while (descriptionsLength(descriptions) > maxLength) {
			Description lastDescription = descriptions.get(descriptions.size() - 1);
			List<String> lastDescriptionSentences = preProcessor.sentences(lastDescription.getDescription());
			if (lastDescriptionSentences.size() > 1) {
				lastDescription.setDescription(lastDescriptionSentences.get(0));
			} else if (descriptions.size() > 1) {
				Description lastButOneDescription = descriptions.get(descriptions.size() - 2);
				List<String> lastButOneDescriptionSentences = preProcessor.sentences(lastButOneDescription.getDescription());
				if (lastButOneDescriptionSentences.size() > 1) {
					lastButOneDescription.setDescription(lastButOneDescriptionSentences.get(0));
				} else if (descriptions.size() > 2) {
					Description lastButTwoDescription = descriptions.get(descriptions.size() - 3);
					List<String> lastButTwoDescriptionSentences = preProcessor.sentences(lastButTwoDescription.getDescription());
					if (lastButTwoDescriptionSentences.size() > 1) {
						lastButTwoDescription.setDescription(lastButTwoDescriptionSentences.get(0));
					} else {
						descriptions.remove(descriptions.size() - 1);
					}
				} else {
					descriptions.remove(descriptions.size() - 1);
				}
			} else {
				lastDescription.setDescription(Common.pruneToMax(lastDescription.getDescription(), maxLength));
			}
		}
	}

	private static String getDescription(List<Description> descriptions, String homepage, Set<BiotoolsLink<LinkType>> linkLinks, Set<BiotoolsLink<DocumentationType>> documentationLinks, Set<BiotoolsLink<DownloadType>> downloadLinks, Database db, Scrape scrape, int minLength, int maxLength, String name, PreProcessor preProcessor) {
		if (!homepage.isEmpty()) {
			descriptionsFromWebpage(descriptions, homepage, db, scrape, minLength, name, null, preProcessor);
		}
		for (BiotoolsLink<LinkType> linkLink : linkLinks) {
			if (linkLink.getType() == LinkType.OTHER
					|| linkLink.getType() == LinkType.REPOSITORY
					|| linkLink.getType() == LinkType.REGISTRY) {
				descriptionsFromWebpage(descriptions, linkLink.getUrl(), db, scrape, minLength, name, false, preProcessor);
			}
		}
		for (BiotoolsLink<DocumentationType> documentationLink : documentationLinks) {
			if (documentationLink.getType() == DocumentationType.GENERAL
					|| documentationLink.getType() == DocumentationType.MANUAL
					|| documentationLink.getType() == DocumentationType.INSTALLATION_INSTRUCTIONS
					|| documentationLink.getType() == DocumentationType.TUTORIAL
					|| documentationLink.getType() == DocumentationType.TRAINING_MATERIAL
					|| documentationLink.getType() == DocumentationType.API_DOCUMENTATION) {
				descriptionsFromWebpage(descriptions, documentationLink.getUrl(), db, scrape, minLength, name, true, preProcessor);
			}
		}
		for (BiotoolsLink<DownloadType> downloadLink : downloadLinks) {
			if (downloadLink.getType() == DownloadType.SOURCE_CODE
					|| downloadLink.getType() == DownloadType.CONTAINER_FILE) {
				descriptionsFromWebpage(descriptions, downloadLink.getUrl(), db, scrape, minLength, name, false, preProcessor);
			}
		}
		Collections.sort(descriptions);
		limitDescriptions(descriptions, maxLength, preProcessor);
		return descriptions.stream().map(d -> d.getDescription()).collect(Collectors.joining(" | "));
	}

	static String get(Suggestion2 suggestion, boolean homepageBroken, boolean homepageMissing, List<ToolInput> biotools, Result2 result, String homepage, Set<BiotoolsLink<LinkType>> linkLinks, Set<BiotoolsLink<DocumentationType>> documentationLinks, Set<BiotoolsLink<DownloadType>> downloadLinks, Database db, Scrape scrape, String name, PreProcessor preProcessor) {
		List<String> messages = new ArrayList<>();
		if (suggestion == null || !suggestion.include()) {
			messages.add("NOT INCLUDED!");
		} else if (suggestion.lowConfidence()) {
			messages.add("LOW CONFIDENCE!");
		}
		if (homepageBroken) {
			messages.add("HOMEPAGE BROKEN!");
		} else if (homepageMissing) {
			messages.add("HOMEPAGE MISSING!");
		}
		final String biotoolsPrefix = "bio.tools/";
		if (suggestion != null && suggestion.getPublicationAndNameExisting() != null) {
			messages.add("EXISTING AS " + suggestion.getPublicationAndNameExisting().stream().map(e -> biotools.get(e)).map(q -> biotoolsPrefix + q.getBiotoolsID()).collect(Collectors.joining(", ")));
		}
		if (suggestion != null && suggestion.getNameExistingSomePublicationDifferent() != null) {
			messages.add("EXISTING AS (SOME PUB. MISSING) " + suggestion.getNameExistingSomePublicationDifferent().stream().map(e -> biotools.get(e)).map(q -> biotoolsPrefix + q.getBiotoolsID()).collect(Collectors.joining(", ")));
		}
		if (suggestion != null && suggestion.getSomePublicationExistingNameDifferent() != null) {
			messages.add("TOOL (" + suggestion.getExtracted() + ") EXISTING UNDER DIFFERENT NAME AS " + suggestion.getSomePublicationExistingNameDifferent().stream().limit(NAME_DIFFERENT_MESSAGE_LIMIT).map(e -> biotools.get(e)).map(q -> biotoolsPrefix + q.getBiotoolsID() + " (" + q.getName() + ")").collect(Collectors.joining(", ")));
		}
		if (suggestion != null && suggestion.getNameExistingPublicationDifferent() != null) {
			messages.add("NAME EQUAL TO (PUB. DIFFERENT) " + suggestion.getNameExistingPublicationDifferent().stream().map(e -> biotools.get(e)).map(q -> biotoolsPrefix + q.getBiotoolsID()).collect(Collectors.joining(", ")));
		}
		if (!result.getNameMatch().isEmpty()) {
			messages.add("NAME (" + suggestion.getExtracted() + ") SIMILAR TO (PUB. DIFFERENT) " + result.getNameMatch().stream().map(e -> biotools.get(e)).map(q -> biotoolsPrefix + q.getBiotoolsID() + " (" + q.getName() + ")").collect(Collectors.joining(", ")));
		}
		if (!result.getLinkMatch().isEmpty()) {
			messages.add("COMMON LINK WITH (PUB. & NAME DIFFERENT) " +
				IntStream.range(0, result.getLinkMatch().size())
					.mapToObj(i -> biotoolsPrefix + biotools.get(result.getLinkMatch().get(i)).getBiotoolsID() + " (" +
						String.join(" ; ", new LinkedHashSet<>(result.getLinkMatchLinks().get(i)))
					+ ")").collect(Collectors.joining(", "))
				);
		}
		if (result.getSuggestions().size() > 1) {
			messages.add("CORRECT NAME OF TOOL COULD ALSO BE " +
				result.getSuggestions().stream().skip(1)
					.map(s -> {
						List<Integer> existingIds = new ArrayList<>();
						if (s.getPublicationAndNameExisting() != null) {
							existingIds.addAll(s.getPublicationAndNameExisting());
						}
						if (s.getNameExistingSomePublicationDifferent() != null) {
							existingIds.addAll(s.getNameExistingSomePublicationDifferent());
						}
						if (s.getNameExistingPublicationDifferent() != null) {
							existingIds.addAll(s.getNameExistingPublicationDifferent());
						}
						return "'" + s.getExtracted() + "'" + (!existingIds.isEmpty() ? " (" + existingIds.stream().map(e -> biotools.get(e)).map(q -> biotoolsPrefix + q.getBiotoolsID()).collect(Collectors.joining("; ")) + ")" : "");
					}).collect(Collectors.joining(", "))
				);
		}
		String description = "";
		boolean messagesMaxLengthReached = false;
		for (String message : messages) {
			message = "> " + message + " | ";
			if (description.length() + message.length() <= BIOTOOLS_DESCRIPTION_MESSAGE_MAX_LENGTH && !messagesMaxLengthReached) {
				description += message;
			} else {
				messagesMaxLengthReached = true;
				logger.warn("No room left for description messages: discarded message '{}' (for name '{}')", message, name);
			}
		}

		List<Description> descriptions = new ArrayList<>();
		for (String title : result.getTitle()) {
			String publicationTitleDescription = Common.WHITESPACE.matcher(descriptionFromTitle(title, Common.TITLE_SEPARATOR)).replaceAll(" ").trim();
			if (publicationTitleDescription.length() >= BIOTOOLS_DESCRIPTION_MINMIN_LENGTH) {
				addDescription(descriptions, publicationTitleDescription, 0, true, preProcessor);
			}
		}
		int initialDescriptionsSize = descriptions.size();
		String allDescriptions = getDescription(descriptions, homepage, linkLinks, documentationLinks, downloadLinks, db, scrape, BIOTOOLS_DESCRIPTION_MIN_LENGTH, BIOTOOLS_DESCRIPTION_MAX_LENGTH - description.length(), name, preProcessor);
		if (descriptions.size() <= initialDescriptionsSize) {
			allDescriptions = getDescription(descriptions, homepage, linkLinks, documentationLinks, downloadLinks, db, scrape, BIOTOOLS_DESCRIPTION_MINMIN_LENGTH, BIOTOOLS_DESCRIPTION_MAX_LENGTH - description.length(), name, preProcessor);
		}
		description += allDescriptions;
		if (descriptions.size() <= initialDescriptionsSize && description.length() + 4 <= BIOTOOLS_DESCRIPTION_MAX_LENGTH) {
			if (!allDescriptions.isEmpty()) {
				description += " | ";
			}
			List<String> abstractDescriptions = new ArrayList<>();
			int abstractSentencesLength = 0;
			for (List<String> abstractSentences : result.getAbstractSentences()) {
				if (description.length() + abstractSentencesLength >= BIOTOOLS_DESCRIPTION_MAX_LENGTH) {
					break;
				}
				if (abstractSentences.size() > 0) {
					boolean end = false;
					String abstractDescription = Common.pruneToMax(Common.WHITESPACE.matcher(abstractSentences.get(0).replaceAll("\\|", ":")).replaceAll(" ").trim(), BIOTOOLS_DESCRIPTION_MAX_LENGTH - description.length() - abstractSentencesLength);
					for (int i = 1; i < abstractSentences.size(); ++i) {
						String abstractSentence = Common.WHITESPACE.matcher(abstractSentences.get(i).replaceAll("\\|", ":")).replaceAll(" ").trim();
						if (description.length() + abstractSentencesLength + abstractDescription.length() + 2 + abstractSentence.length() <= BIOTOOLS_DESCRIPTION_MAX_LENGTH) {
							abstractDescription += ". " + abstractSentence;
						} else {
							end = true;
							break;
						}
					}
					abstractDescriptions.add(abstractDescription);
					abstractSentencesLength += abstractDescription.length() + 3;
					if (end) {
						break;
					}
				}
			}
			if (abstractDescriptions.isEmpty()) {
				description += Common.pruneToMax("NO DESCRIPTION FOUND FROM LINKS OR ABSTRACT!", BIOTOOLS_DESCRIPTION_MAX_LENGTH - description.length());
			} else {
				description += String.join(" | ", abstractDescriptions);
			}
		}
		String descriptionOriginal = description;
		if (description.length() < BIOTOOLS_SCHEMA_DESCRIPTION_MIN) {
			description = Common.fillToMin(description, BIOTOOLS_SCHEMA_DESCRIPTION_MIN);
			logger.warn("Description filled to min from '{}' to '{}' (for name '{}')", descriptionOriginal, description, name);
		}
		if (description.length() > BIOTOOLS_SCHEMA_DESCRIPTION_MAX) {
			description = Common.pruneToMax(description, BIOTOOLS_SCHEMA_DESCRIPTION_MAX);
			logger.warn("Description pruned to max from '{}' to '{}' (for name '{}')", descriptionOriginal, description, name);
		}

		return description;
	}
}
