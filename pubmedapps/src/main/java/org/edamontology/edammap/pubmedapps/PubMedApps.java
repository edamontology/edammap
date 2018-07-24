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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.beust.jcommander.Parameter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.edamontology.pubfetcher.core.common.BasicArgs;
import org.edamontology.pubfetcher.core.common.FetcherArgs;
import org.edamontology.pubfetcher.core.common.PubFetcher;
import org.edamontology.pubfetcher.core.common.Version;
import org.edamontology.pubfetcher.core.db.publication.Publication;
import org.edamontology.pubfetcher.core.db.publication.PublicationIds;
import org.edamontology.pubfetcher.core.fetching.Fetcher;

import org.edamontology.edammap.core.idf.Idf;
import org.edamontology.edammap.core.preprocessing.PreProcessor;
import org.edamontology.edammap.core.preprocessing.PreProcessorArgs;
import org.edamontology.edammap.core.query.Link;
import org.edamontology.edammap.core.query.Query;
import org.edamontology.edammap.core.query.QueryLoader;
import org.edamontology.edammap.core.query.QueryType;

public final class PubMedApps {

	private static Logger logger;

	// TODO
	//private static final String MESH_QUERY = "(Software[MeSH Terms]) AND (genetics[MeSH Subheading] OR Genetics[MeSH Terms] OR Genomics[MeSH Terms] OR Genetic Phenomena[MeSH Terms] OR Biochemical Phenomena[MeSH Terms] OR Genetic Techniques[MeSH Terms] OR Molecular Probe Techniques[MeSH Terms] OR Nucleic Acids, Nucleotides, and Nucleosides[MeSH Terms] OR Amino Acids, Peptides, and Proteins[MeSH Terms]) AND (\"2013/01/01\"[PDat] : \"2100/01/01\"[PDat])";
	private static final String MESH_QUERY = "(Software[MeSH Terms]) AND (\"2013/01/01\"[PDat] : \"2017/12/31\"[PDat])";

	private static final Pattern USE_IN_PATTERN = Pattern.compile("^[\\p{L}\\p{N}]$");

	private static final String TOOL_TITLE_GENERAL = "database|data|web|server|webserver|web-server|package|toolkit|toolbox|suite|toolsuite|tools|tool|kit|framework|workbench|pipeline|software|program|platform|project|resource|r";
	private static final Pattern TOOL_TITLE_PRUNE = Pattern.compile("(?i)^(update|v|version|(v|version)?\\p{N}+([.-]\\p{N}+)*|" + TOOL_TITLE_GENERAL + ")$");
	private static final double TOOL_TITLE_MULTIPLIER = 24;
	private static final Pattern TOOL_TITLE_TRIM = Pattern.compile("( ?(db|v|version|update))*( ?\\p{N}{0,4})?( ?(db|v|version|update))*$");

	private static final Pattern NOT_ALPHANUMERIC = Pattern.compile("^[^\\p{L}\\p{N}]$");
	private static final Pattern ACRONYM_STOP = Pattern.compile("(?i)(http:|https:|ftp:|;|, |: )");

	private static final double TIER_1_MULTIPLIER = 6;
	private static final double TIER_2_MULTIPLIER = 3;
	private static final double TIER_3_MULTIPLIER = 1.5;
	private static final double BEFORE_AFTER_LIMIT = 72;

	private static final Pattern GOOD_START = Pattern.compile("^(\\p{Lu}|.[^\\p{Ll}-]|.-[^\\p{Ll}]|.[^-]*[^\\p{L}-])[^-]*$");
	private static final Pattern GOOD_END = Pattern.compile("^(.*[^\\p{Ll}]|.*[^\\p{Ll}-].|\\p{Lu}.*|..)$");
	private static final Pattern GOOD_START_MULTI = Pattern.compile("^[^ ]+( \\p{Lu}[^ ]*)*( v| version)?( \\p{Lu}[^ ]*| ([vV](ersion)?)?\\p{N}+([.-]\\p{N}+)*)$");
	private static final Pattern TO_LINK = Pattern.compile("^[^ ]*[^ \\p{Ll}-][^ ]*( [^ ]*[^ \\p{Ll}-][^ ]*)*$");
	private static final Pattern NOT_TO_LINK = Pattern.compile("^[^ ]( [^ ])*$");

	private static final Pattern LINK_TWO_PART = Pattern.compile("^[^./]+\\.[^./]+$");

	private static final Pattern LINK_WWW = Pattern.compile("^[^.]*www[^.]*\\.");
	private static final Pattern LINK_END_REMOVE = Pattern.compile("([.?]\\p{Lu}|--)[\\p{L}\\p{N}'-]*$");
	private static final Pattern LINK_EMAIL_END = Pattern.compile("@[a-zA-Z0-9.-]+\\.[a-z]{2,}$");
	private static final Pattern LINK_EMAIL_REMOVE = Pattern.compile("\\.[^/]+" + LINK_EMAIL_END);

	private static final Pattern PATH_QUERY = Pattern.compile("\\?.*=");
	private static final Pattern PATH_PERIOD = Pattern.compile("(\\.[^\\p{N}][^.]*$)|(\\.$)");
	private static final Pattern PATH_SPLIT = Pattern.compile("[-_]");
	private static final Pattern PATH_NUMBER = Pattern.compile("^[vV-]?\\p{N}+(\\.\\p{N}+)?$");
	private static final Pattern PATH_ONE_UPPERCASE = Pattern.compile("^..*\\p{Lu}.*$");
	private static final Pattern PATH_UNI = Pattern.compile("^uni([\\p{L}\\p{N}]?-.*|[\\p{L}\\p{N}]{0,2})$");

	private static final double PATH_IDF_MIN = 0.5;
	private static final double PATH_IDF_MIN_2 = 0.24;

	private static final Pattern LINK_COMPARE_START = Pattern.compile("(?i)^((http|https|ftp)://)?(www\\.)?");
	private static final Pattern LINK_COMPARE_REST = Pattern.compile("^(\\p{Ll}\\p{Lu}|.[()]?\\.\\p{Lu}|.\\.[()]|.--|./?[^/]+@[^/]+\\.[^/]+)[\\p{L}\\p{N}'-]*$");
	private static final Pattern LINK_COMPARE_SCHEMA = Pattern.compile("(http|https|ftp)://");

	private static final Pattern BIOTOOLS_EXTRACTED_VERSION_TRIM = Pattern.compile(" ?([vV](ersion)?)? ?\\p{N}+([.-]\\p{N}+)*$");
	private static final Pattern BIOTOOLS_PROCESSED_VERSION_TRIM = Pattern.compile(" ?([v](ersion)?)? ?\\p{N}+$");
	private static final Pattern BIOTOOLS_LINK_TRIM_START = LINK_COMPARE_START;
	private static final Pattern BIOTOOLS_LINK_TRIM_END = Pattern.compile("/+$");

	private static final Pattern TITLE_SEPARATOR = Pattern.compile("(?i)(: | - |--|-a |-an |:a |:an )");

	private static final int COMPOUND_WORDS = 5;
	private static final double QUERY_IDF_SCALING = 2;
	private static final double COMPOUND_DIVIDER = 2;

	private static final double LINK_MULTIPLIER_ABSTRACT = 24;
	private static final double LINK_MULTIPLIER_ABSTRACT_MINIMUM = LINK_MULTIPLIER_ABSTRACT / 2;
	private static final double LINK_MULTIPLIER_ABSTRACT_AUGMENTATION = LINK_MULTIPLIER_ABSTRACT / 4;
	private static final double LINK_MULTIPLIER_ABSTRACT_NEW = LINK_MULTIPLIER_ABSTRACT / 2;
	private static final double LINK_MULTIPLIER_FULLTEXT = LINK_MULTIPLIER_ABSTRACT / 2;

	private static final double TOP_SCORE_LIMIT = 24;
	private static final int SUGGESTION_LIMIT = 5;
	private static final int POSSIBLY_EXISTING_VALID_LIMIT = 5;

	private static final Pattern DOC1 = Pattern.compile("(?i)^(https?://)?(www\\.)?(bitbucket\\.org|code\\.google\\.com|github\\.com|sourceforge\\.net).*/wikis?([^\\p{L}]|$)");
	private static final Pattern DOC2 = Pattern.compile("(?i)(^|[^\\p{L}-])(usage|guide)s?([^\\p{L}-]|$)");
	private static final Pattern DOC3 = Pattern.compile("(?i)(tutorial|help|vignette|manual|doc|documentation|faq|about|readme|install|installation|howto|howtouse|intro|introduction|instruction|userguide|usersguide|beginnerguide|beginnersguide|startguide|quickstart|quick_start)s?([^\\p{L}]|$)");
	private static final Pattern SCHEMA_START = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*://");

	private static void meshQuery(FetcherArgs fetcherArgs) throws IOException, ParseException, URISyntaxException {
		String meshQuery = new URI("https", "eutils.ncbi.nlm.nih.gov", "/entrez/eutils/esearch.fcgi", "db=pubmed&term=" + MESH_QUERY + "&retmax=1000000", null).toASCIIString();

		Set<String> pmids = new LinkedHashSet<>();

		Document doc = new Fetcher(fetcherArgs.getPrivateArgs()).getDoc(meshQuery, false, fetcherArgs);
		if (doc != null) {
			for (Element id : doc.getElementsByTag("Id")) {
				pmids.add(id.text());
			}
		}

		for (String pmid : pmids) {
			System.out.println(pmid + "\t\t");
		}
	}

	private static String toolTitlePrune(List<String> toolTitleExtracted) {
		while (!toolTitleExtracted.isEmpty() && TOOL_TITLE_PRUNE.matcher(toolTitleExtracted.get(0)).matches()) {
			toolTitleExtracted.remove(0);
		}
		while (!toolTitleExtracted.isEmpty() && TOOL_TITLE_PRUNE.matcher(toolTitleExtracted.get(toolTitleExtracted.size() - 1)).matches()) {
			toolTitleExtracted.remove(toolTitleExtracted.size() - 1);
		}
		return String.join(" ", toolTitleExtracted);
	}

	private static boolean toolTitleScore(String toolTitle, PreProcessor preProcessor, Map<String, Double> scores, Map<String, String> processedToExtracted, boolean pruned) {
		// toolTitle is already toolTitleExtractedString
		List<String> toolTitleProcessed = preProcessor.process(toolTitle);
		String toolTitleProcessedString = String.join(" ", toolTitleProcessed);

		Double toolTitleExistingScore = scores.get(toolTitleProcessedString);
		if (toolTitleExistingScore != null) {
			toolTitleExistingScore *= TOOL_TITLE_MULTIPLIER;
			if (toolTitleExistingScore > TOOL_TITLE_MULTIPLIER) {
				scores.put(toolTitleProcessedString, toolTitleExistingScore);
			} else {
				scores.put(toolTitleProcessedString, TOOL_TITLE_MULTIPLIER);
			}
		} else if (!pruned) {
			scores.put(toolTitleProcessedString, TOOL_TITLE_MULTIPLIER / toolTitleProcessed.size());
			processedToExtracted.put(toolTitleProcessedString, toolTitle);
		}

		if (toolTitleExistingScore != null) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean toolTitleMatch(String toolTitle, PreProcessor preProcessor, List<String> matchedKeys, boolean linkTwoPart, String linkProcessed, String fromLink, String link, Map<String, List<String>> links) {
		// toolTitle is already toolTitleExtractedString
		List<String> toolTitleProcessed = preProcessor.process(toolTitle);
		String toolTitleProcessedString = String.join(" ", toolTitleProcessed);

		String toolTitleProcessedHyphen = String.join(" ", preProcessor.process(toolTitle.replaceAll("-", " ")));
		String fromLinkProcessedHyphen = String.join(" ", preProcessor.process(fromLink.replaceAll("-", " ")));

		boolean matches = false;

		if (toolTitleProcessed.size() <= COMPOUND_WORDS && !matchedKeys.contains(toolTitleProcessedString) && !(linkTwoPart && linkProcessed.equals(toolTitleProcessedString))) {
			String toolTitleProcessedStringTrimmed = TOOL_TITLE_TRIM.matcher(toolTitleProcessedHyphen).replaceFirst("");
			String fromLinkProcessedTrimmed = TOOL_TITLE_TRIM.matcher(fromLinkProcessedHyphen).replaceFirst("");
			String toolTitleCompare = toolTitleProcessedStringTrimmed.replaceAll(" ", "");
			String fromLinkCompare = fromLinkProcessedTrimmed.replaceAll(" ", "");
			if (toolTitleCompare.length() < 2 || fromLinkCompare.length() < 2) return false;

			if (toolTitleCompare.startsWith(fromLinkCompare) || toolTitleCompare.endsWith(fromLinkCompare)
					|| fromLinkCompare.startsWith(toolTitleCompare) || fromLinkCompare.endsWith(toolTitleCompare)) {
				matches = true;
			}

			if (!matches) {
				String firstLetter = fromLinkCompare.substring(0, 1);
				String pattern = "(^| )" + (USE_IN_PATTERN.matcher(firstLetter).matches() ? firstLetter : ".");
				for (int i = 1; i < fromLinkCompare.length(); ++i) {
					String letter = fromLinkCompare.substring(i, i + 1);
					pattern += "(.* )?" + (USE_IN_PATTERN.matcher(letter).matches() ? letter : ".");
				}
				if (Pattern.compile(pattern).matcher(toolTitleProcessedStringTrimmed).find()) {
					matches = true;
				}
			}
			if (!matches) {
				String firstLetter = toolTitleCompare.substring(0, 1);
				String pattern = "(^| )" + (USE_IN_PATTERN.matcher(firstLetter).matches() ? firstLetter : ".");
				for (int i = 1; i < toolTitleCompare.length(); ++i) {
					String letter = toolTitleCompare.substring(i, i + 1);
					pattern += "(.* )?" + (USE_IN_PATTERN.matcher(letter).matches() ? letter : ".");
				}
				if (Pattern.compile(pattern).matcher(fromLinkProcessedTrimmed).find()) {
					matches = true;
				}
			}
			if (!matches) {
				String firstLetter = fromLinkCompare.substring(0, 1);
				String pattern = "^" + (USE_IN_PATTERN.matcher(firstLetter).matches() ? firstLetter : ".");
				for (int i = 1; i < fromLinkCompare.length(); ++i) {
					String letter = fromLinkCompare.substring(i, i + 1);
					pattern += "([^ ]*|.* )" + (USE_IN_PATTERN.matcher(letter).matches() ? letter : ".");
				}
				pattern += "[^ ]*$";
				if (Pattern.compile(pattern).matcher(toolTitleProcessedStringTrimmed).matches()) {
					matches = true;
				}
			}
			if (!matches) {
				String firstLetter = toolTitleCompare.substring(0, 1);
				String pattern = "^" + (USE_IN_PATTERN.matcher(firstLetter).matches() ? firstLetter : ".");
				for (int i = 1; i < toolTitleCompare.length(); ++i) {
					String letter = toolTitleCompare.substring(i, i + 1);
					pattern += "([^ ]*|.* )" + (USE_IN_PATTERN.matcher(letter).matches() ? letter : ".");
				}
				pattern += "[^ ]*$";
				if (Pattern.compile(pattern).matcher(fromLinkProcessedTrimmed).matches()) {
					matches = true;
				}
			}

			if (matches) {
				List<String> keyLinks = links.get(toolTitleProcessedString);
				if (keyLinks == null) {
					keyLinks = new ArrayList<>();
					links.put(toolTitleProcessedString, keyLinks);
				}
				keyLinks.add(link);
				matchedKeys.add(toolTitleProcessedString);
			}
		}

		return matches;
	}

	private static boolean isAcronym(String acronym, String phrase) {
		if (acronym.isEmpty()) return false;
		acronym = acronym.toUpperCase(Locale.ROOT);
		phrase = phrase.toUpperCase(Locale.ROOT);
		int acronymIndex = 0;
		char acronymChar = acronym.charAt(acronymIndex);
		while (NOT_ALPHANUMERIC.matcher(String.valueOf(acronymChar)).matches()) {
			++acronymIndex;
			if (acronymIndex >= acronym.length()) {
				return false;
			}
			acronymChar = acronym.charAt(acronymIndex);
		}
		for (char phraseChar : phrase.toCharArray()) {
			if (acronymChar == phraseChar) {
				++acronymIndex;
				if (acronymIndex >= acronym.length()) {
					return true;
				}
				acronymChar = acronym.charAt(acronymIndex);
				while (NOT_ALPHANUMERIC.matcher(String.valueOf(acronymChar)).matches()) {
					++acronymIndex;
					if (acronymIndex >= acronym.length()) {
						return true;
					}
					acronymChar = acronym.charAt(acronymIndex);
				}
			}
		}
		return false;
	}

	private static List<Integer> acronyms(String sentence, PreProcessor preProcessor) {
		List<Integer> acronyms = new ArrayList<>();
		int previousEnd = 0;
		int index = 0;
		int begin = 0;
		int end = 0;
		while (begin != -1 && end != -1) {
			begin = sentence.indexOf(" (", previousEnd);
			if (begin != -1) {
				end = sentence.indexOf(")", begin + 2);
				if (end != -1) {
					String before = sentence.substring(previousEnd, begin).trim();
					List<String> beforeExtracted = preProcessor.extract(before);
					preProcessor.process(before, beforeExtracted); // align indexes

					String inside = sentence.substring(begin, end + 1).trim();
					List<String> insideExtracted = preProcessor.extract(inside);
					preProcessor.process(inside, insideExtracted); // align indexes

					Matcher acronymStop = ACRONYM_STOP.matcher(inside);
					if (acronymStop.find()) {
						inside = inside.substring(0, acronymStop.start()).trim();
					}

					if (inside.contains(" ") && beforeExtracted.size() > 0 && insideExtracted.size() > 0) {
						if (isAcronym(beforeExtracted.get(beforeExtracted.size() - 1), inside)) {
							acronyms.add(index + beforeExtracted.size() - 1);
						}
					} else if (!inside.contains(" ") && beforeExtracted.size() > 1 && insideExtracted.size() > 0) {
						if (isAcronym(inside, before)) {
							acronyms.add(-(index + beforeExtracted.size()));
						}
					}

					index += beforeExtracted.size();
					index += insideExtracted.size();
					previousEnd = sentence.indexOf(" ", end + 1);
					if (previousEnd == -1) break;
				}
			}
		}
		return acronyms;
	}

	private static Integer firstAcronymIndex(String sentence, PreProcessor preProcessor) {
		List<Integer> acronyms = acronyms(sentence, preProcessor);
		if (!acronyms.isEmpty()) {
			int acronym = acronyms.get(0); // first acronym
			if (acronym < 0) acronym = -acronym; // index
			return acronym;
		} else {
			return null;
		}
	}

	private static void beforeAfterScore(String key, Map<String, Double> scores, Map<String, Double> beforeAfterAdded, boolean tier1, boolean tier2, boolean tier3, boolean twice) {
		Double newBeforeAfterAdded = beforeAfterAdded.get(key);
		if (newBeforeAfterAdded == null) {
			newBeforeAfterAdded = 1.0;
		}
		double multiplier = 1;
		if (tier1) {
			multiplier = TIER_1_MULTIPLIER;
		} else if (tier2) {
			multiplier = TIER_2_MULTIPLIER;
		} else if (tier3) {
			multiplier = TIER_3_MULTIPLIER;
		}
		if (twice) {
			multiplier *= 2;
		}
		if (newBeforeAfterAdded * multiplier > BEFORE_AFTER_LIMIT) {
			multiplier = BEFORE_AFTER_LIMIT / newBeforeAfterAdded;
		}
		newBeforeAfterAdded *= multiplier;
		beforeAfterAdded.put(key, newBeforeAfterAdded);
		scores.put(key, scores.get(key) * multiplier);
	}

	private static String fromLink(String link, PreProcessor preProcessor, Idf queryIdf, List<String> hostIgnore) {
		int schema = link.indexOf("://");
		if (schema > -1) {
			link = link.substring(schema + 3);
		}
		link = LINK_WWW.matcher(link).replaceFirst("");

		link = LINK_END_REMOVE.matcher(link).replaceFirst("");

		int firstSlash = link.indexOf('/');

		Matcher emailEnd = LINK_EMAIL_END.matcher(link);
		if (emailEnd.find()) {
			if (firstSlash > -1) {
				Matcher emailRemove = LINK_EMAIL_REMOVE.matcher(link);
				if (emailRemove.find()) {
					link = link.substring(0, emailRemove.start());
				} else {
					return "";
				}
			} else {
				link = link.substring(0, emailEnd.start());
			}
		}

		String host = firstSlash > -1 ? link.substring(0, firstSlash) : link;
		if (host.equals("dx.doi.org") || host.equals("doi.org")
				|| host.equals("goo.gl") || host.equals("youtube.com")
				|| host.equals("proteomecentral.proteomexchange.org")) {
			return "";
		}

		int pathCount = 0;

		String bestPath = null;
		double bestPathScore = 0;

		for (int slash = link.lastIndexOf('/'); slash > -1; slash = link.lastIndexOf('/')) {
			String path = link.substring(slash + 1);
			link = link.substring(0, slash);

			if (path.isEmpty()) continue;

			++pathCount;

			if (path.startsWith("~") || path.startsWith("∼")) continue; // different UTF characters
			if (path.startsWith("%")) continue;

			if (pathCount == 1) {
				int equals = path.lastIndexOf('=');
				if (equals > -1) {
					int query = path.indexOf('?');
					if (query > -1) {
						link += "/" + path.substring(0, query);
					}
					path = path.substring(equals + 1);
					int fragment = path.indexOf('#');
					if (fragment > -1) {
						path = path.substring(fragment + 1);
					}
				} else {
					int fragment = path.indexOf('#');
					if (fragment > -1) {
						link += "/" + path.substring(0, fragment);
						path = path.substring(fragment + 1);
					}
				}
			} else {
				Matcher query = PATH_QUERY.matcher(path);
				if (query.find()) {
					continue;
				}
				if (path.startsWith("#")) continue;
			}

			path = PATH_PERIOD.matcher(path).replaceFirst("");
			if (path.endsWith(".tar")) {
				path = path.substring(0, path.length() - 4);
			}

			Matcher pathSplit = PATH_SPLIT.matcher(path);
			if (pathSplit.find() && pathSplit.find()) {
				// don't use pathSplit, as its state has changed
				path = PATH_SPLIT.matcher(path).replaceAll(" ").trim();
			}

			if (path.isEmpty()) continue;
			if (PATH_NUMBER.matcher(path).matches()) continue;

			double score = queryIdf.getIdf(String.join("", preProcessor.process(path)));
			if (score > bestPathScore) {
				bestPath = path;
				bestPathScore = score;
			}

			if (PATH_ONE_UPPERCASE.matcher(path).matches() || score > PATH_IDF_MIN) {
				return path;
			}
		}

		if (pathCount < 3) {
			int period = link.indexOf('.');
			if (period > -1) {
				String rest = link.substring(period + 1);
				link = link.substring(0, period);
				if (link.length() > 1
						&& !link.startsWith("bioinf") && !link.endsWith("lab")
						&& !PATH_UNI.matcher(link).matches() && !PATH_NUMBER.matcher(link).matches()
						&& !rest.equals("edu") && !rest.startsWith("edu.") && !rest.startsWith("ac.")
						&& !hostIgnore.contains(link)) {
					return link;
				}
			}
		}

		if (bestPath != null && bestPathScore > PATH_IDF_MIN_2) {
			return bestPath;
		} else {
			return "";
		}
	}

	private static Map<String, List<String>> links(List<String> partLinks, PreProcessor preProcessor, Idf queryIdf, List<String> hostIgnore,
			Set<String> keys, Map<String, String> processedToExtracted, List<List<String>> processed, String titleWithoutLinks, String abstractWithoutLinks,
			String toolTitle, String toolTitleTwo, String toolTitleAcronym, String toolTitleTwoAcronym, String toolTitlePrune, String toolTitleTwoPrune) {
		Map<String, List<String>> links = new LinkedHashMap<>();

		for (String link : partLinks) {
			String fromLink = fromLink(link, preProcessor, queryIdf, hostIgnore);
			if (fromLink.isEmpty()) {
				continue;
			}
			String fromLinkProcessed = String.join("", preProcessor.process(fromLink));
			if (fromLinkProcessed.isEmpty()) {
				continue;
			}

			List<String> matchedKeys = new ArrayList<>();

			boolean linkTwoPart = false;
			if (LINK_TWO_PART.matcher(link).matches()) {
				linkTwoPart = true;
			}
			String linkProcessed = String.join("", preProcessor.process(link));

			String longestStart = "";
			String longestEnd = "";

			for (String key : keys) {
				if (linkTwoPart && linkProcessed.equals(key)) {
					continue;
				}
				if ((fromLinkProcessed + fromLinkProcessed).equals(key)) {
					continue;
				}

				String keyCompare = key.replaceAll(" ", "");
				boolean found = false;

				if (key.equals(fromLinkProcessed)) {
					found = true;
				}

				if (!found && keyCompare.equals(fromLinkProcessed)) {
					found = true;
				}

				String keyExtracted = processedToExtracted.get(key);
				String keyExtractedLower = keyExtracted.toLowerCase(Locale.ROOT);

				if (!found && !key.contains(" ") && keyCompare.startsWith(fromLinkProcessed)) {
					int keyExtractedIndex = 0;
					for (int i = 0; i < fromLinkProcessed.length(); ++i) {
						while (keyExtractedIndex < keyExtractedLower.length() && fromLinkProcessed.charAt(i) != keyExtractedLower.charAt(keyExtractedIndex)) {
							++keyExtractedIndex;
						}
						if (keyExtractedIndex < keyExtractedLower.length()) {
							++keyExtractedIndex;
						} else {
							break;
						}
					}
					if (keyExtractedIndex > 0) --keyExtractedIndex;

					String goodStart = keyExtracted.substring(keyExtractedIndex);
					if (GOOD_START.matcher(goodStart).matches() && !(goodStart.length() == 2 && goodStart.charAt(1) == 's')) {
						found = true;
					} else if (goodStart.length() >= 2 && goodStart.charAt(1) == '-') {
						int hyphenBased = 0;
						for (List<String> sentenceProcessed : processed) {
							for (String wordProcessed : sentenceProcessed) {
								if (wordProcessed.equals(key)) {
									++hyphenBased;
								}
							}
						}
						if (hyphenBased > 1) {
							found = true;
						}
					}
				}

				if (!found && !key.contains(" ") && keyCompare.endsWith(fromLinkProcessed) && !LINK_TWO_PART.matcher(keyExtracted).matches()) {
					int keyExtractedIndex = keyExtractedLower.length() - 1;
					for (int i = fromLinkProcessed.length() - 1; i >= 0; --i) {
						while (keyExtractedIndex >= 0 && fromLinkProcessed.charAt(i) != keyExtractedLower.charAt(keyExtractedIndex)) {
							--keyExtractedIndex;
						}
						if (keyExtractedIndex >= 0) {
							--keyExtractedIndex;
						} else {
							break;
						}
					}
					if (keyExtractedIndex < keyExtractedLower.length()) ++keyExtractedIndex;
					if (keyExtractedIndex < keyExtractedLower.length()) ++keyExtractedIndex;

					String goodEnd = keyExtracted.substring(0, keyExtractedIndex);
					if (GOOD_END.matcher(goodEnd).matches()) {
						found = true;
					}
				}

				if (!found && key.contains(" ") && keyCompare.startsWith(fromLinkProcessed)) {
					if (GOOD_START_MULTI.matcher(keyExtracted).matches()) {
						String[] words = keyExtracted.split(" ");
						String patternString = "";
						for (int i = 0; i < words.length; ++i) {
							String firstLetter = words[i].substring(0, 1);
							patternString += USE_IN_PATTERN.matcher(firstLetter).matches() ? firstLetter : ".";
							if (words[i].length() > 1) {
								patternString += "[^ ]*";
								String lastLetter = words[i].substring(words[i].length() - 1, words[i].length());
								patternString += USE_IN_PATTERN.matcher(lastLetter).matches() ? lastLetter : ".";
							}
							if (i < words.length - 1) {
								patternString += "([ ./]+)";
							}
						}

						Pattern pattern = Pattern.compile(patternString);
						if (pattern.matcher(titleWithoutLinks).find() || pattern.matcher(abstractWithoutLinks).find()) {
							found = true;
						}
					}
				}

				if (!found && fromLinkProcessed.startsWith(keyCompare)) {
					if (TO_LINK.matcher(keyExtracted).matches() && !NOT_TO_LINK.matcher(keyExtracted).matches()) {
						if (key.length() > longestStart.length()) {
							longestStart = key;
						}
					}
				}

				if (!found && fromLinkProcessed.endsWith(keyCompare)) {
					if (TO_LINK.matcher(keyExtracted).matches() && !NOT_TO_LINK.matcher(keyExtracted).matches()) {
						if (key.length() > longestEnd.length()) {
							longestEnd = key;
						}
					}
				}

				if (!found && key.split(" ").length > 2) {
					String acronym = "^";
					for (int i = 0; i < fromLinkProcessed.length(); ++i) {
						char c = fromLinkProcessed.charAt(i);
						if (USE_IN_PATTERN.matcher(c + "").matches()) acronym += c;
						else acronym += ".";
						if (i < fromLinkProcessed.length() - 1) {
							acronym += "[^ ]* ";
						}
					}
					acronym += "[^ ]*$";

					Pattern acronymPattern = Pattern.compile(acronym);
					if (acronymPattern.matcher(key).matches()) {
						found = true;
					}
				}

				if (found) {
					List<String> keyLinks = links.get(key);
					if (keyLinks == null) {
						keyLinks = new ArrayList<>();
						links.put(key, keyLinks);
					}
					keyLinks.add(link);
					matchedKeys.add(key);
				}
			}

			if (toolTitle != null && toolTitlePrune != null) {
				toolTitleMatch(toolTitle, preProcessor, matchedKeys, linkTwoPart, linkProcessed, fromLink, link, links);
			}
			if (toolTitleTwo != null && toolTitleTwoPrune != null) {
				toolTitleMatch(toolTitleTwo, preProcessor, matchedKeys, linkTwoPart, linkProcessed, fromLink, link, links);
			}
			if (toolTitleAcronym != null) {
				toolTitleMatch(toolTitleAcronym, preProcessor, matchedKeys, linkTwoPart, linkProcessed, fromLink, link, links);
			}
			if (toolTitleTwoAcronym != null) {
				toolTitleMatch(toolTitleTwoAcronym, preProcessor, matchedKeys, linkTwoPart, linkProcessed, fromLink, link, links);
			}

			if (longestStart.length() > 2) {
				boolean present = false;
				for (String key : matchedKeys) {
					if (key.startsWith(longestStart)) {
						present = true;
						break;
					}
				}
				if (!present) {
					List<String> keyLinks = links.get(longestStart);
					if (keyLinks == null) {
						keyLinks = new ArrayList<>();
						links.put(longestStart, keyLinks);
					}
					keyLinks.add(link);
					matchedKeys.add(longestStart);
				}
			}

			if (longestEnd.length() > 2) {
				boolean present = false;
				for (String key : matchedKeys) {
					if (key.endsWith(longestEnd)) {
						present = true;
						break;
					}
				}
				if (!present) {
					List<String> keyLinks = links.get(longestEnd);
					if (keyLinks == null) {
						keyLinks = new ArrayList<>();
						links.put(longestEnd, keyLinks);
					}
					keyLinks.add(link);
					matchedKeys.add(longestEnd);
				}
			}
		}

		return links;
	}

	private static List<String> breakLinks(List<String> resultLinks, List<String> resultAllLinks) {
		for (int i = 0; i < resultLinks.size(); ++i) {
			String resultLink = resultLinks.get(i);
			String resultLinkStart = "";
			Matcher trimStart = BIOTOOLS_LINK_TRIM_START.matcher(resultLink);
			if (trimStart.find()) {
				resultLinkStart = resultLink.substring(0, trimStart.end());
				resultLink = resultLink.substring(trimStart.end());
			}
			int linkMax = 0;
			int schemaStart = 0;
			int schemaEnd = 0;
			for (String link : resultAllLinks) {
				if (!resultLink.equals(link) && resultLink.startsWith(link) && link.length() > linkMax) {
					String rest = resultLink.substring(link.length());
					Matcher schemaMatcher = LINK_COMPARE_SCHEMA.matcher(rest);
					if (schemaMatcher.find()) {
						linkMax = link.length();
						schemaStart = schemaMatcher.start();
						schemaEnd = schemaMatcher.end();
					}
				}
			}
			if (linkMax > 0) {
				resultLinks.set(i, resultLinkStart + resultLink.substring(0, linkMax));
				if (linkMax + schemaEnd < resultLink.length()) {
					resultLinks.add(i + 1, resultLink.substring(linkMax + schemaStart));
				}
			} else {
				Matcher schemaMatcher = LINK_COMPARE_SCHEMA.matcher(resultLink);
				if (schemaMatcher.find()) {
					resultLinks.set(i, resultLinkStart + resultLink.substring(0, schemaMatcher.start()));
					if (schemaMatcher.end() < resultLink.length()) {
						resultLinks.add(i + 1, resultLink.substring(schemaMatcher.start()));
					}
				}
			}
		}
		return resultLinks;
	}

	private static List<Result> getResults(PreProcessorArgs preProcessorArgs, String queryIdf, String queryPath, QueryType queryType, FetcherArgs fetcherArgs, List<Publication> publications) throws IOException, ParseException {
		List<Result> results = new ArrayList<>();

		List<String> hostIgnore = PubFetcher.getResource(PubMedApps.class, "resources/host_ignore.txt");
		List<String> beforeTier1 = PubFetcher.getResource(PubMedApps.class, "resources/before_tier1.txt");
		List<String> beforeTier2 = PubFetcher.getResource(PubMedApps.class, "resources/before_tier2.txt");
		List<String> beforeTier3 = PubFetcher.getResource(PubMedApps.class, "resources/before_tier3.txt");
		List<String> afterTier1 = PubFetcher.getResource(PubMedApps.class, "resources/after_tier1.txt");
		List<String> afterTier2 = PubFetcher.getResource(PubMedApps.class, "resources/after_tier2.txt");
		List<String> afterTier3 = PubFetcher.getResource(PubMedApps.class, "resources/after_tier3.txt");

		PreProcessor preProcessor = new PreProcessor(preProcessorArgs);

		Idf idf = new Idf(queryIdf);

		List<Query> queries = QueryLoader.get(queryPath, queryType, fetcherArgs.getTimeout(), fetcherArgs.getPrivateArgs().getUserAgent());

		List<List<String>> queryNamesExtracted = new ArrayList<>();
		List<String> queryNamesProcessed = new ArrayList<>();
		List<List<String>> queryLinks = new ArrayList<>();
		for (Query query : queries) {
			List<String> queryNameExtracted = preProcessor.extract(query.getName());
			List<String> queryNameProcessed = preProcessor.process(query.getName(), queryNameExtracted);
			queryNamesExtracted.add(Arrays.asList(BIOTOOLS_EXTRACTED_VERSION_TRIM.matcher(String.join(" ", queryNameExtracted)).replaceFirst("").split(" ")));
			queryNamesProcessed.add(BIOTOOLS_PROCESSED_VERSION_TRIM.matcher(String.join(" ", queryNameProcessed)).replaceFirst(""));
			List<Link> links = new ArrayList<>();
			if (query.getWebpageUrls() != null) {
				links.addAll(query.getWebpageUrls());
			}
			if (query.getDocUrls() != null) {
				links.addAll(query.getDocUrls());
			}
			queryLinks.add(links.stream()
				.map(l -> BIOTOOLS_LINK_TRIM_START.matcher(l.getUrl()).replaceFirst(""))
				.map(l -> BIOTOOLS_LINK_TRIM_END.matcher(l).replaceFirst(""))
				.filter(l -> !l.isEmpty())
				.collect(Collectors.toList()));
		}

		for (int publicationIndex = 0; publicationIndex < publications.size(); ++publicationIndex) {
			double percentage = (publicationIndex + 1) / (double) publications.size() * 100;
			percentage = Math.round(percentage * 10) / 10.0;
			System.err.print("\rMaking results: " + percentage + "%"); // TODO

			Publication publication = publications.get(publicationIndex);

			String toolTitle = null;
			String toolTitleTwo = null;
			String toolTitleAcronym = null;
			String toolTitleTwoAcronym = null;
			String toolTitlePruned = null;
			String toolTitleTwoPruned = null;
			long toolTitleWordsTotal = 0;

			String title = publication.getTitle().getContent();
			int from = 0;

			Matcher matcher = TITLE_SEPARATOR.matcher(title);
			while (from < title.length() && matcher.find(from)) {
				String currentToolTitle = title.substring(from, matcher.start()).trim();
				String currentToolTitleTwo = null;
				String currentToolTitleAcronym = null;
				String currentToolTitleTwoAcronym = null;
				String currentToolTitlePruned = null;
				String currentToolTitleTwoPruned = null;

				String separatorString = " and ";
				int separator = currentToolTitle.indexOf(separatorString);
				if (separator < 0) {
					separatorString = " & ";
					separator = currentToolTitle.indexOf(separatorString);
				}
				if (separator > -1) {
					currentToolTitleTwo = currentToolTitle.substring(separator + separatorString.length(), currentToolTitle.length());
					currentToolTitle = currentToolTitle.substring(0, separator);
				}

				List<String> currentToolTitleExtracted = preProcessor.extract(currentToolTitle);
				preProcessor.process(currentToolTitle, currentToolTitleExtracted); // align indexes
				List<String> currentToolTitleTwoExtracted = null;
				if (currentToolTitleTwo != null) {
					currentToolTitleTwoExtracted = preProcessor.extract(currentToolTitleTwo);
					preProcessor.process(currentToolTitleTwo, currentToolTitleTwoExtracted); // align indexes
				}

				Integer firstAcronymIndex = firstAcronymIndex(currentToolTitle, preProcessor);
				if (firstAcronymIndex != null) {
					currentToolTitleAcronym = currentToolTitleExtracted.remove(firstAcronymIndex.intValue());
				}
				if (currentToolTitleTwo != null) {
					Integer firstAcronymIndexTwo = firstAcronymIndex(currentToolTitleTwo, preProcessor);
					if (firstAcronymIndexTwo != null) {
						currentToolTitleTwoAcronym = currentToolTitleTwoExtracted.remove(firstAcronymIndexTwo.intValue());
					}
				}

				currentToolTitle = String.join(" ", currentToolTitleExtracted);
				if (currentToolTitleTwo != null) {
					currentToolTitleTwo = String.join(" ", currentToolTitleTwoExtracted);
				}

				currentToolTitlePruned = toolTitlePrune(currentToolTitleExtracted);
				if (currentToolTitleTwo != null) {
					currentToolTitleTwoPruned = toolTitlePrune(currentToolTitleTwoExtracted);
				}

				if (currentToolTitleTwo != null
						&& (currentToolTitleExtracted.size() > 1 || currentToolTitleTwoExtracted.size() > 1)
						&& (!currentToolTitle.isEmpty() && !currentToolTitleTwo.isEmpty())) {
					currentToolTitle += " " + currentToolTitleTwo;
					currentToolTitleTwo = null;
					if (!currentToolTitlePruned.isEmpty() && !currentToolTitleTwoPruned.isEmpty()) {
						currentToolTitlePruned += " " + currentToolTitleTwoPruned;
						currentToolTitleTwoPruned = null;
					} else if (!currentToolTitleTwoPruned.isEmpty()) {
						currentToolTitlePruned = currentToolTitleTwoPruned;
						currentToolTitleTwoPruned = null;
					}
					currentToolTitleExtracted.addAll(currentToolTitleTwoExtracted);
					currentToolTitleTwoExtracted = null;
				}

				long currentToolTitleWordsTotal = currentToolTitleExtracted.size();
				if (currentToolTitleTwoExtracted != null) {
					currentToolTitleWordsTotal += currentToolTitleTwoExtracted.size();
				}
				if (currentToolTitleWordsTotal < toolTitleWordsTotal || toolTitle == null) {
					toolTitle = currentToolTitle == null || currentToolTitle.isEmpty() ? null : currentToolTitle;
					toolTitleTwo = currentToolTitleTwo == null || currentToolTitleTwo.isEmpty() ? null : currentToolTitleTwo;
					toolTitleAcronym = currentToolTitleAcronym == null || currentToolTitleAcronym.isEmpty() ? null : currentToolTitleAcronym;
					toolTitleTwoAcronym = currentToolTitleTwoAcronym == null || currentToolTitleTwoAcronym.isEmpty() ? null : currentToolTitleTwoAcronym;
					toolTitlePruned = currentToolTitlePruned == null || currentToolTitlePruned.isEmpty() ? null : currentToolTitlePruned;
					toolTitleTwoPruned = currentToolTitleTwoPruned == null || currentToolTitleTwoPruned.isEmpty() ? null : currentToolTitleTwoPruned;
					toolTitleWordsTotal = currentToolTitleWordsTotal;
				}
				from = matcher.end();
			}

			String theAbstract = publication.getAbstract().getContent();

			String titleWithoutLinks = preProcessor.removeLinks(title);
			String abstractWithoutLinks = preProcessor.removeLinks(theAbstract);

			if (from > 0) {
				title = title.substring(from).trim();
			}

			List<String> titleAbstractSentences = preProcessor.sentences(preProcessor.removeLinks(title) + ". " + abstractWithoutLinks);

			List<List<String>> extracted = new ArrayList<>();
			List<List<String>> processed = new ArrayList<>();

			for (String sentence : titleAbstractSentences) {
				List<String> sentenceExtracted = preProcessor.extract(sentence);
				List<String> sentenceProcessed = preProcessor.process(sentence, sentenceExtracted);
				extracted.add(sentenceExtracted);
				processed.add(sentenceProcessed);
			}

			Map<String, Double> scores = new HashMap<>();
			Map<String, String> processedToExtracted = new HashMap<>();

			Map<String, Set<String>> processedToExtractedBegin = new HashMap<>();
			Map<String, List<String>> processedToExtractedWithin = new HashMap<>();

			for (int i = 0; i < processed.size(); ++i) {
				List<String> sentenceExtracted = extracted.get(i);
				List<String> sentenceProcessed = processed.get(i);
				for (int j = 0; j < COMPOUND_WORDS; ++j) {
					for (int k = 0; k < sentenceProcessed.size() - j; ++k) {
						String wordExtracted = sentenceExtracted.get(k);
						String wordProcessed = sentenceProcessed.get(k);
						for (int l = k + 1; l <= k + j; ++l) wordExtracted += " " + sentenceExtracted.get(l);
						for (int l = k + 1; l <= k + j; ++l) wordProcessed += " " + sentenceProcessed.get(l);
						Double value;
						if (j == 0) {
							value = Math.pow(idf.getIdf(sentenceProcessed.get(k)), QUERY_IDF_SCALING);
						} else {
							value = scores.get(sentenceProcessed.get(k));
							for (int l = k + 1; l <= k + j; ++l) value *= scores.get(sentenceProcessed.get(l));
							value /= COMPOUND_DIVIDER;
						}
						scores.merge(wordProcessed, value, Double::sum);
						if (i == 0 || k == 0) {
							Set<String> wordsExtracted = processedToExtractedBegin.get(wordProcessed);
							if (wordsExtracted == null) {
								wordsExtracted = new LinkedHashSet<>();
								processedToExtractedBegin.put(wordProcessed, wordsExtracted);
							}
							wordsExtracted.add(wordExtracted);
						} else {
							List<String> wordsExtracted = processedToExtractedWithin.get(wordProcessed);
							if (wordsExtracted == null) {
								wordsExtracted = new ArrayList<>();
								processedToExtractedWithin.put(wordProcessed, wordsExtracted);
							}
							wordsExtracted.add(wordExtracted);
						}
					}
				}
			}

			// put within before begin so that in case of equality option from within wins (because order-preserving sets)
			Set<String> processedToExtractedKeys = new LinkedHashSet<>();
			processedToExtractedKeys.addAll(processedToExtractedWithin.keySet());
			processedToExtractedKeys.addAll(processedToExtractedBegin.keySet());
			for (String key : processedToExtractedKeys) {
				Map<String, Integer> extractedCount = new LinkedHashMap<>();
				List<String> extractedWithins = processedToExtractedWithin.get(key);
				if (extractedWithins != null) {
					for (String extractedWithin : extractedWithins) {
						extractedCount.merge(extractedWithin, 1, Integer::sum);
					}
				}
				Set<String> extractedBegins = processedToExtractedBegin.get(key);
				if (extractedBegins != null) {
					for (String extractedBegin : extractedBegins) {
						extractedCount.merge(extractedBegin, 1, Integer::sum);
					}
				}
				extractedCount = extractedCount.entrySet().stream()
					.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) -> { throw new AssertionError(); }, LinkedHashMap::new));
				processedToExtracted.put(key, extractedCount.keySet().iterator().next());
			}

			if (toolTitle != null && toolTitlePruned != null) {
				boolean existing = toolTitleScore(toolTitle, preProcessor, scores, processedToExtracted, false);
				if (!existing && !toolTitlePruned.equals(toolTitle)) {
					toolTitleScore(toolTitlePruned, preProcessor, scores, processedToExtracted, true);
				}
			}
			if (toolTitleTwo != null && toolTitleTwoPruned != null) {
				boolean existing = toolTitleScore(toolTitleTwo, preProcessor, scores, processedToExtracted, false);
				if (!existing && !toolTitleTwoPruned.equals(toolTitleTwo)) {
					toolTitleScore(toolTitleTwoPruned, preProcessor, scores, processedToExtracted, true);
				}
			}
			if (toolTitleAcronym != null) {
				toolTitleScore(toolTitleAcronym, preProcessor, scores, processedToExtracted, false);
			}
			if (toolTitleTwoAcronym != null) {
				toolTitleScore(toolTitleTwoAcronym, preProcessor, scores, processedToExtracted, false);
			}

			Map<String, Double> beforeAfterAdded = new HashMap<>();
			for (int i = 0; i < processed.size(); ++i) {
				List<String> sentenceProcessed = processed.get(i);
				boolean acronymsDone = false;
				List<Integer> acronyms = null;
				for (int j = 0; j < sentenceProcessed.size(); ++j) {
					String wordProcessed = sentenceProcessed.get(j);
					boolean inBeforeTier1 = beforeTier1.contains(wordProcessed);
					boolean inBeforeTier2 = beforeTier2.contains(wordProcessed);
					boolean inBeforeTier3 = beforeTier3.contains(wordProcessed);
					if (j + 1 < sentenceProcessed.size() && (inBeforeTier1 || inBeforeTier2 || inBeforeTier3)) {
						if (!acronymsDone) {
							acronyms = acronyms(titleAbstractSentences.get(i), preProcessor);
							acronymsDone = true;
						}
						boolean acronymFound = false;
						String acronym = null;
						if (acronyms.contains(j + 1)) {
							acronym = sentenceProcessed.get(j + 1);
							acronymFound = true;
						} else if (acronyms.contains(-(j + 1))) {
							acronym = sentenceProcessed.get(j + 1);
							acronymFound = true;
						} else if (j + 2 < sentenceProcessed.size()) {
							if (acronyms.contains(j + 2)) {
								acronym = sentenceProcessed.get(j + 2);
								acronymFound = true;
							} else if (acronyms.contains(-(j + 2))) {
								acronym = sentenceProcessed.get(j + 2);
								acronymFound = true;
							}
						}
						if (acronymFound) {
							beforeAfterScore(acronym, scores, beforeAfterAdded, inBeforeTier1, inBeforeTier2, inBeforeTier3, true);
						} else {
							String nextWord = sentenceProcessed.get(j + 1);
							beforeAfterScore(nextWord, scores, beforeAfterAdded, inBeforeTier1, inBeforeTier2, inBeforeTier3, false);
							if (j + 2 < sentenceProcessed.size()) {
								acronymFound = false;
								for (int k = 1; k <= COMPOUND_WORDS && j + 2 + k < sentenceProcessed.size(); ++k) {
									if (acronyms.contains(-(j + 2 + k))) {
										String nextNextWord = sentenceProcessed.get(j + 2 + k);
										beforeAfterScore(nextNextWord, scores, beforeAfterAdded, inBeforeTier1, inBeforeTier2, inBeforeTier3, false);
										acronymFound = true;
										break;
									}
								}
								if (!acronymFound) {
									String nextNextWord = sentenceProcessed.get(j + 2);
									beforeAfterScore(nextNextWord, scores, beforeAfterAdded, inBeforeTier1, inBeforeTier2, inBeforeTier3, false);
									String nextCompoundWord = nextWord + " " + nextNextWord;
									beforeAfterScore(nextCompoundWord, scores, beforeAfterAdded, inBeforeTier1, inBeforeTier2, inBeforeTier3, false);
								}
							}
						}
					}
					boolean inAfterTier1 = afterTier1.contains(wordProcessed);
					boolean inAfterTier2 = afterTier2.contains(wordProcessed);
					boolean inAfterTier3 = afterTier3.contains(wordProcessed);
					if (j - 1 >= 0 && (inAfterTier1 || inAfterTier2 || inAfterTier3)) {
						if (!acronymsDone) {
							acronyms = acronyms(titleAbstractSentences.get(i), preProcessor);
							acronymsDone = true;
						}
						boolean acronymFound = false;
						String acronym = null;
						if (acronyms.contains(j - 1)) {
							acronym = sentenceProcessed.get(j - 1);
							acronymFound = true;
						} else if (acronyms.contains(-(j - 1))) {
							acronym = sentenceProcessed.get(j - 1);
							acronymFound = true;
						} else if (j - 2 >= 0) {
							if (acronyms.contains(j - 2)) {
								acronym = sentenceProcessed.get(j - 2);
								acronymFound = true;
							} else if (acronyms.contains(-(j - 2))) {
								acronym = sentenceProcessed.get(j - 2);
								acronymFound = true;
							}
						}
						if (acronymFound) {
							beforeAfterScore(acronym, scores, beforeAfterAdded, inAfterTier1, inAfterTier2, inAfterTier3, true);
						} else {
							String nextWord = sentenceProcessed.get(j - 1);
							beforeAfterScore(nextWord, scores, beforeAfterAdded, inAfterTier1, inAfterTier2, inAfterTier3, false);
							if (j - 2 >= 0) {
								acronymFound = false;
								for (int k = 1; k <= COMPOUND_WORDS && j - 2 - k >= 0; ++k) {
									if (acronyms.contains(-(j - 2 - k))) {
										String nextNextWord = sentenceProcessed.get(j - 2 - k);
										beforeAfterScore(nextNextWord, scores, beforeAfterAdded, inAfterTier1, inAfterTier2, inAfterTier3, false);
										acronymFound = true;
										break;
									}
								}
								if (!acronymFound) {
									String nextNextWord = sentenceProcessed.get(j - 2);
									beforeAfterScore(nextNextWord, scores, beforeAfterAdded, inAfterTier1, inAfterTier2, inAfterTier3, false);
									String nextCompoundWord = nextNextWord + " " + nextWord;
									beforeAfterScore(nextCompoundWord, scores, beforeAfterAdded, inAfterTier1, inAfterTier2, inAfterTier3, false);
								}
							}
						}
					}
				}
			}

			List<String> titleAbstractLinks = preProcessor.links(title);
			titleAbstractLinks.addAll(preProcessor.links(theAbstract));

			List<String> fulltextLinks = preProcessor.links(publication.getFulltext().getContent());

			for (int i = 0; i < titleAbstractLinks.size(); ++i) {
				String titleAbstractLink = titleAbstractLinks.get(i);

				Iterator<String> it = fulltextLinks.iterator();
				while (it.hasNext()) {
					String fulltextLink = it.next();

					if (fulltextLink.equals(titleAbstractLink)) {
						it.remove();
						break;
					}

					String start = "";
					Matcher startTitleAbstractLink = LINK_COMPARE_START.matcher(titleAbstractLink);
					if (startTitleAbstractLink.find()) {
						start = titleAbstractLink.substring(0, startTitleAbstractLink.end());
						titleAbstractLink = titleAbstractLink.substring(startTitleAbstractLink.end());
					}
					Matcher startFulltextLink = LINK_COMPARE_START.matcher(fulltextLink);
					if (startFulltextLink.find()) {
						String startFulltext = fulltextLink.substring(0, startFulltextLink.end());
						if (startFulltext.length() > start.length()) {
							start = startFulltext;
						}
						fulltextLink = fulltextLink.substring(startFulltextLink.end());
					}

					if (fulltextLink.equals(titleAbstractLink)) {
						titleAbstractLinks.set(i, start + titleAbstractLink);
						it.remove();
						break;
					}

					if (fulltextLink.startsWith(titleAbstractLink)) {
						String rest = fulltextLink.substring(titleAbstractLink.length());
						if (rest.startsWith("/")) {
							titleAbstractLinks.set(i, start + fulltextLink);
						}
						it.remove();
						break;
					}
					if (fulltextLink.contains(titleAbstractLink)) {
						titleAbstractLinks.set(i, start + fulltextLink);
						it.remove();
						break;
					}

					if (titleAbstractLink.startsWith(fulltextLink)) {
						String rest = titleAbstractLink.substring(fulltextLink.length() - 1);
						if (LINK_COMPARE_REST.matcher(rest).matches()) {
							titleAbstractLinks.set(i, start + fulltextLink);
						}
						Matcher schemaMatcher = LINK_COMPARE_SCHEMA.matcher(rest);
						if (schemaMatcher.find()) {
							titleAbstractLinks.set(i, start + fulltextLink);
							titleAbstractLinks.add(i + 1, rest.substring(schemaMatcher.start()));
						}
						it.remove();
						break;
					}
					if (titleAbstractLink.contains(fulltextLink)) {
						it.remove();
						break;
					}
				}
			}

			Map<String, List<String>> linksAbstract = links(titleAbstractLinks, preProcessor, idf, hostIgnore,
				scores.keySet(), processedToExtracted, processed, titleWithoutLinks, abstractWithoutLinks,
				toolTitle, toolTitleTwo, toolTitleAcronym, toolTitleTwoAcronym, toolTitlePruned, toolTitleTwoPruned);

			Map<String, List<String>> linksFulltext = links(fulltextLinks, preProcessor, idf, hostIgnore,
				scores.keySet(), processedToExtracted, processed, titleWithoutLinks, abstractWithoutLinks,
				toolTitle, toolTitleTwo, toolTitleAcronym, toolTitleTwoAcronym, toolTitlePruned, toolTitleTwoPruned);

			for (Map.Entry<String, List<String>> linkEntry : linksAbstract.entrySet()) {
				double score = scores.get(linkEntry.getKey()) * LINK_MULTIPLIER_ABSTRACT * linkEntry.getValue().size();
				if (score > LINK_MULTIPLIER_ABSTRACT_MINIMUM) {
					scores.put(linkEntry.getKey(), score);
				} else {
					scores.put(linkEntry.getKey(), LINK_MULTIPLIER_ABSTRACT_MINIMUM);
				}
			}

			boolean genericLinkAugmentation = linksAbstract.isEmpty();
			for (String link : titleAbstractLinks) {
				boolean present = false;
				for (Map.Entry<String, List<String>> linkEntry : linksAbstract.entrySet()) {
					if (linkEntry.getValue().contains(link)) {
						present = true;
						break;
					}
				}
				if (!present) {
					if (genericLinkAugmentation) {
						for (Map.Entry<String, Double> scoreEntry : scores.entrySet()) {
							scores.put(scoreEntry.getKey(), scoreEntry.getValue() * LINK_MULTIPLIER_ABSTRACT_AUGMENTATION);
						}
						genericLinkAugmentation = false;
					}

					String fromLink = fromLink(link, preProcessor, idf, hostIgnore);
					if (!fromLink.isEmpty()) {
						List<String> fromLinkExtracted = preProcessor.extract(fromLink);
						List<String> fromLinkProcessed = preProcessor.process(fromLink, fromLinkExtracted);
						String fromLinkExtractedString = String.join(" ", fromLinkExtracted);
						String fromLinkProcessedString = String.join(" ", fromLinkProcessed);
						if (!fromLinkProcessedString.isEmpty()) {
							scores.merge(fromLinkProcessedString, LINK_MULTIPLIER_ABSTRACT_NEW / fromLinkProcessed.size(), (d1, d2) -> d1 * d2);
							String wordExtracted = processedToExtracted.get(fromLinkProcessedString);
							if (wordExtracted == null) {
								processedToExtracted.put(fromLinkProcessedString, fromLinkExtractedString);
							}
						}
					}
				}
			}

			for (Map.Entry<String, List<String>> linkEntry : linksFulltext.entrySet()) {
				long multiplier = linkEntry.getValue().stream().filter(link -> !LINK_TWO_PART.matcher(link).matches()).count();
				if (multiplier > 0) {
					if (multiplier > 2) {
						multiplier = 2;
					}
					scores.put(linkEntry.getKey(), scores.get(linkEntry.getKey()) * LINK_MULTIPLIER_FULLTEXT * multiplier);
				}
			}

			Map<String, Double> sortedScores = scores.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) -> { throw new AssertionError(); }, LinkedHashMap::new));

			Result result = new Result();

			result.setPmid(publication.getPmid().getContent());
			result.setPmcid(publication.getPmcid().getContent());
			result.setDoi(publication.getDoi().getContent());

			result.setTitle(publication.getTitle().getContent());
			result.setOa(publication.isOA());
			result.setJournalTitle(publication.getJournalTitle());
			result.setPubDate(publication.getPubDateHuman());
			result.setCitationsCount(publication.getCitationsCount());
			result.setCitationsTimestamp(publication.getCitationsTimestampHuman());
			result.setCorrespAuthor(publication.getCorrespAuthor().toString());

			List<String> resultLinks = new ArrayList<>();
			List<String> suggestionsProcessed = new ArrayList<>();
			Iterator<Map.Entry<String, Double>> sortedScoresIterator = sortedScores.entrySet().iterator();
			if (sortedScoresIterator.hasNext()) {
				Map.Entry<String, Double> topEntry = sortedScoresIterator.next();
				double topScore = topEntry.getValue();
				result.setScore(topScore);
				result.setSuggestion(processedToExtracted.get(topEntry.getKey()));

				List<String> linksFromAbstract = linksAbstract.get(topEntry.getKey());
				if (linksFromAbstract != null) {
					for (String link : linksFromAbstract) {
						resultLinks.add(link);
					}
				}
				List<String> linksFromFulltext = linksFulltext.get(topEntry.getKey());
				if (linksFromFulltext != null) {
					for (String link : linksFromFulltext) {
						resultLinks.add(link);
					}
				}
				suggestionsProcessed.add(topEntry.getKey());

				for (int i = 1; i < SUGGESTION_LIMIT && sortedScoresIterator.hasNext(); ++i) {
					topEntry = sortedScoresIterator.next();
					if (topEntry.getValue() * TOP_SCORE_LIMIT < topScore) {
						break;
					}
					result.addOtherSuggestion(processedToExtracted.get(topEntry.getKey()));
					suggestionsProcessed.add(topEntry.getKey());
				}
			}

			List<String> resultOtherLinks = new ArrayList<>();
			for (List<String> linksFromAbstract : linksAbstract.values()) {
				for (String link : linksFromAbstract) {
					if (!resultLinks.contains(link)) {
						resultOtherLinks.add(link);
					}
				}
			}
			for (List<String> linksFromFulltext : linksFulltext.values()) {
				for (String link : linksFromFulltext) {
					if (!resultLinks.contains(link)) {
						resultOtherLinks.add(link);
					}
				}
			}

			List<String> resultLeftoverLinks = new ArrayList<>();
			for (String link : titleAbstractLinks) {
				if (!resultLinks.contains(link) && !resultOtherLinks.contains(link)) {
					resultLeftoverLinks.add(link);
				}
			}
			for (String link : fulltextLinks) {
				if (!resultLinks.contains(link) && !resultOtherLinks.contains(link)) {
					resultLeftoverLinks.add(link);
				}
			}

			List<String> resultAllLinks = new ArrayList<>();
			resultAllLinks.addAll(resultLinks.stream().map(resultLink -> BIOTOOLS_LINK_TRIM_START.matcher(resultLink).replaceFirst("")).collect(Collectors.toList()));
			resultAllLinks.addAll(resultOtherLinks.stream().map(resultLink -> BIOTOOLS_LINK_TRIM_START.matcher(resultLink).replaceFirst("")).collect(Collectors.toList()));
			resultAllLinks.addAll(resultLeftoverLinks.stream().map(resultLink -> BIOTOOLS_LINK_TRIM_START.matcher(resultLink).replaceFirst("")).collect(Collectors.toList()));

			result.addLinks(breakLinks(resultLinks, resultAllLinks));
			result.addOtherLinks(breakLinks(resultOtherLinks, resultAllLinks));
			result.addLeftoverLinks(breakLinks(resultLeftoverLinks, resultAllLinks));

			for (int i = 0; i < queries.size(); ++i) {
				Query query = queries.get(i);
				if (query.getPublicationIds() != null) {
					for (PublicationIds pubIds : query.getPublicationIds()) {
						if (!pubIds.getPmid().isEmpty() && !result.getPmid().isEmpty() && pubIds.getPmid().equals(result.getPmid())
								|| !pubIds.getPmcid().isEmpty() && !result.getPmcid().isEmpty() && pubIds.getPmcid().equals(result.getPmcid())
								|| !pubIds.getDoi().isEmpty() && !result.getDoi().isEmpty() && pubIds.getDoi().equals(result.getDoi())) {
							result.addExistingName(query.getId(), query.getName());
							for (String link : result.getLinks()) {
								String linkTrimmed = BIOTOOLS_LINK_TRIM_START.matcher(link).replaceFirst("");
								linkTrimmed = BIOTOOLS_LINK_TRIM_END.matcher(linkTrimmed).replaceFirst("");
								boolean found = false;
								for (String queryLink : queryLinks.get(i)) {
									if (linkTrimmed.equalsIgnoreCase(queryLink)) {
										found = true;
										break;
									} else if (linkTrimmed.startsWith(queryLink)) {
										String rest = linkTrimmed.substring(queryLink.length() - 1);
										if (LINK_COMPARE_REST.matcher(rest).matches()) {
											found = true;
											break;
										}
									}
								}
								if (!found) {
									// TODO queryLinks is not complete
									//result.addNewLink(link);
								}
							}
							break;
						}
					}
				}
			}

			for (String suggestionProcessed : suggestionsProcessed) {
				suggestionProcessed = BIOTOOLS_PROCESSED_VERSION_TRIM.matcher(suggestionProcessed).replaceFirst("");
				if (suggestionProcessed.isEmpty()) continue;
				for (int i = 0; i < queryNamesProcessed.size(); ++i) {
					if (suggestionProcessed.equals(queryNamesProcessed.get(i))) {
						String possiblyExistingId = queries.get(i).getId();
						if (!result.getExistingNames().keySet().contains(possiblyExistingId)) {
							result.addPossiblyExisting(possiblyExistingId, queries.get(i).getName());
						}
					}
				}
			}
			List<String> suggestionsExtracted = new ArrayList<>();
			suggestionsExtracted.add(result.getSuggestion());
			suggestionsExtracted.addAll(result.getOtherSuggestions());
			for (String suggestionExtracted : suggestionsExtracted) {
				suggestionExtracted = BIOTOOLS_EXTRACTED_VERSION_TRIM.matcher(suggestionExtracted).replaceFirst("");
				if (suggestionExtracted.isEmpty()) continue;
				for (String suggestionExtractedWord : suggestionExtracted.split(" ")) {
					Map<String, String> possiblyExisting = new LinkedHashMap<>();
					for (int i = 0; i < queryNamesExtracted.size(); ++i) {
						List<String> queryNameExtracted = queryNamesExtracted.get(i);
						if (queryNameExtracted.contains(suggestionExtractedWord)) {
							String possiblyExistingId = queries.get(i).getId();
							if (!result.getExistingNames().keySet().contains(possiblyExistingId)) {
								possiblyExisting.put(possiblyExistingId, queries.get(i).getName());
							}
						}
					}
					if (possiblyExisting.size() >= 1 && possiblyExisting.size() <= POSSIBLY_EXISTING_VALID_LIMIT) {
						for (Map.Entry<String, String> possiblyExistingEntry : possiblyExisting.entrySet()) {
							result.addPossiblyExisting(possiblyExistingEntry.getKey(), possiblyExistingEntry.getValue());
						}
					}
				}
			}
			List<String> resultLinksOtherLinks = new ArrayList<>();
			resultLinksOtherLinks.addAll(result.getLinks());
			resultLinksOtherLinks.addAll(result.getOtherLinks());
			for (int i = 0; i < resultLinksOtherLinks.size(); ++i) {
				String resultLink = resultLinksOtherLinks.get(i);
				resultLink = BIOTOOLS_LINK_TRIM_START.matcher(resultLink).replaceFirst("");
				resultLink = BIOTOOLS_LINK_TRIM_END.matcher(resultLink).replaceFirst("");
				for (int j = 0; j < queryLinks.size(); ++j) {
					String possiblyExistingId = queries.get(j).getId();
					if (!result.getExistingNames().keySet().contains(possiblyExistingId)) {
						List<String> queryLink = queryLinks.get(j);
						for (String link : queryLink) {
							if (resultLink.equalsIgnoreCase(link)) {
								result.addPossiblyExisting(possiblyExistingId, queries.get(j).getName());
							} else if (resultLink.startsWith(link)) {
								String rest = resultLink.substring(link.length() - 1);
								if (LINK_COMPARE_REST.matcher(rest).matches()) {
									result.addPossiblyExisting(possiblyExistingId, queries.get(j).getName());
								}
							}
						}
					}
				}
			}

			if (!(result.getExistingNames().size() == 1 && !result.getSuggestion().isEmpty()
					&& result.getExistingNames().values().iterator().next().equals(result.getSuggestion())
					&& result.getNewLinks().isEmpty())) {
				results.add(result);
			}
		}

		System.err.println(); // TODO

		results = results.stream().sorted(Comparator.comparing(Result::getScore).reversed()).collect(Collectors.toList());

		for (int i = 0; i < results.size() - 1; ++i) {
			Result resultI = results.get(i);
			for (int j = i + 1; j < results.size(); ++j) {
				Result resultJ = results.get(j);
				if (resultI.getSuggestion().equals(resultJ.getSuggestion())) {
					resultI.addSameSuggestion(resultJ.getPmid());
					resultJ.addSameSuggestion(resultI.getPmid());
				}
			}
		}

		return results;
	}

	private static void extractDocs(List<Result> results) {
		for (Result result : results) {
			Set<String> links = new LinkedHashSet<>();
			for (String link : result.getLinks()) {
				if (DOC1.matcher(link).find() || DOC2.matcher(link).find() || DOC3.matcher(link).find()) {
					result.addDoc(link);
				} else {
					links.add(link);
				}
			}
			result.setLinks(links);
		}
	}

	private static void writeLinks(Path txt, List<String> links) throws IOException {
		CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
		encoder.onMalformedInput(CodingErrorAction.REPLACE);
		encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(txt), encoder))) {
			for (String link : links) {
				if (!SCHEMA_START.matcher(link).find()) {
					writer.write("http://");
				}
				writer.write(link);
				writer.write("\n");
			}
		}
	}

	private static void writeWebDoc(PreProcessorArgs preProcessorArgs, String queryIdf, String database, List<String> pubFile, String queryPath, QueryType queryType, String webFile, String docFile, FetcherArgs fetcherArgs) throws IOException, ParseException {
		Path webPath = PubFetcher.outputPath(webFile);
		Path docPath = PubFetcher.outputPath(docFile);

		List<Publication> publications = PubFetcher.getPublications(database, pubFile, PubMedApps.class.getSimpleName());

		List<Result> results = getResults(preProcessorArgs, queryIdf, queryPath, queryType, fetcherArgs, publications);

		extractDocs(results);

		List<String> links = new ArrayList<>();
		List<String> docs = new ArrayList<>();
		for (Result result : results) {
			links.addAll(result.getLinks());
			docs.addAll(result.getDocs());
		}

		writeLinks(webPath, links);
		writeLinks(docPath, docs);
	}

	private static void printResults(PreProcessorArgs preProcessorArgs, String queryIdf, String database, List<String> pubFile, String queryPath, QueryType queryType, FetcherArgs fetcherArgs) throws IOException, ParseException {
		List<Publication> publications = PubFetcher.getPublications(database, pubFile, PubMedApps.class.getSimpleName());

		List<Result> results = getResults(preProcessorArgs, queryIdf, queryPath, queryType, fetcherArgs, publications);

		extractDocs(results);

		logger.info("Removed {} existing", publications.size() - results.size());

		System.out.println("pmid\tpmcid\tdoi\tscore\tsuggestion\tlinks\tdocs\tsame_suggestions\tother_suggestions\tother_links\tleftover_links\texisting_names\tnew_links\tpossibly_existing\ttitle\toa\tjournal_title\tpub_date\tcitations_count\tcitations_timestamp\tcorresp_author");
		for (Result result : results) {
			System.out.print(result.getPmid());
			System.out.print("\t");
			System.out.print(result.getPmcid());
			System.out.print("\t");
			System.out.print(result.getDoi());
			System.out.print("\t");
			System.out.print(result.getScore());
			System.out.print("\t");
			System.out.print(result.getSuggestion());
			System.out.print("\t");
			System.out.print(String.join(" | ", result.getLinks()));
			System.out.print("\t");
			System.out.print(String.join(" | ", result.getDocs()));
			System.out.print("\t");
			System.out.print(String.join(" | ", result.getSameSuggestions()));
			System.out.print("\t");
			System.out.print(String.join(" | ", result.getOtherSuggestions()));
			System.out.print("\t");
			System.out.print(String.join(" | ", result.getOtherLinks()));
			System.out.print("\t");
			System.out.print(String.join(" | ", result.getLeftoverLinks()));
			System.out.print("\t");
			System.out.print(String.join(" | ", result.getExistingNames().entrySet().stream().map(e -> e.getValue() + " (" + e.getKey() + ")").collect(Collectors.toList())));
			System.out.print("\t");
			System.out.print(String.join(" | ", result.getNewLinks()));
			System.out.print("\t");
			System.out.print(String.join(" | ", result.getPossiblyExisting().entrySet().stream().map(e -> e.getValue() + " (" + e.getKey() + ")").collect(Collectors.toList())));
			System.out.print("\t");
			System.out.print(result.getTitle());
			System.out.print("\t");
			System.out.print(result.isOa());
			System.out.print("\t");
			System.out.print(result.getJournalTitle());
			System.out.print("\t");
			System.out.print(result.getPubDate());
			System.out.print("\t");
			System.out.print(result.getCitationsCount());
			System.out.print("\t");
			System.out.print(result.getCitationsTimestamp());
			System.out.print("\t");
			System.out.print(result.getCorrespAuthor());
			System.out.println();
		}
	}

	private static void beforeAfter(PreProcessorArgs preProcessorArgs, String queryIdf, String database, List<String> pubFile) throws IOException {
		PreProcessor preProcessor = new PreProcessor(preProcessorArgs);

		Idf idf = new Idf(queryIdf);

		List<Publication> publications = PubFetcher.getPublications(database, pubFile, PubMedApps.class.getSimpleName());

		Map<String, Integer> before = new HashMap<>();
		Map<String, Integer> after = new HashMap<>();
		Map<String, Integer> all = new HashMap<>();
		Map<String, Double> allBeforeScores = new HashMap<>();
		int allBeforeScoresSum = 0;
		Map<String, Double> allAfterScores = new HashMap<>();
		int allAfterScoresSum = 0;

		for (Publication publication : publications) {
			String toolTitle = publication.getTitle().getContent();
			Matcher titleSeparator = TITLE_SEPARATOR.matcher(toolTitle);
			if (titleSeparator.find()) {
				toolTitle = toolTitle.substring(0, titleSeparator.start()).trim();
			} else {
				continue;
			}

			List<String> toolTitleProcessedWords = preProcessor.process(toolTitle);
			if (toolTitleProcessedWords.size() != 1) continue;
			String toolTitleProcessed = toolTitleProcessedWords.get(0);

			List<String> abstractSentences = preProcessor.sentences(preProcessor.removeLinks(publication.getAbstract().getContent()));
			List<List<String>> processed = new ArrayList<>();
			for (String sentence : abstractSentences) {
				processed.add(preProcessor.process(sentence));
			}

			Map<String, Double> scores = new HashMap<>();
			for (List<String> sentence : processed) {
				for (String word : sentence) {
					scores.merge(word, Math.pow(idf.getIdf(word), QUERY_IDF_SCALING), Double::sum);
				}
			}

			for (List<String> sentenceProcessed : processed) {
				for (int i = 0; i < sentenceProcessed.size(); ++i) {
					if (sentenceProcessed.get(i).equals(toolTitleProcessed)) {
						if (i - 1 >= 0) before.merge(sentenceProcessed.get(i - 1), 1, Integer::sum);
						if (i - 2 >= 0) before.merge(sentenceProcessed.get(i - 2), 1, Integer::sum);
						if (i + 1 < sentenceProcessed.size()) after.merge(sentenceProcessed.get(i +	1), 1, Integer::sum);
						if (i + 2 < sentenceProcessed.size()) after.merge(sentenceProcessed.get(i +	2), 1, Integer::sum);
					}
				}
			}

			for (List<String> sentenceProcessed : processed) {
				for (int i = 0; i < sentenceProcessed.size(); ++i) {
					String wordProcessed = sentenceProcessed.get(i);
					all.merge(wordProcessed, 1, Integer::sum);
					if (i - 1 >= 0) {
						allBeforeScores.merge(wordProcessed, scores.get(sentenceProcessed.get(i - 1)), Double::sum);
						++allBeforeScoresSum;
					}
					if (i - 2 >= 0) {
						allBeforeScores.merge(wordProcessed, scores.get(sentenceProcessed.get(i - 2)), Double::sum);
						++allBeforeScoresSum;
					}
					if (i + 1 < sentenceProcessed.size()) {
						allAfterScores.merge(wordProcessed, scores.get(sentenceProcessed.get(i + 1)), Double::sum);
						++allAfterScoresSum;
					}
					if (i + 2 < sentenceProcessed.size()) {
						allAfterScores.merge(wordProcessed, scores.get(sentenceProcessed.get(i + 2)), Double::sum);
						++allAfterScoresSum;
					}
				}
			}
		}

		Map<String, Integer> beforeSorted = before.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) -> { throw new AssertionError(); }, LinkedHashMap::new));
		System.out.println("BEFORE_TOOL_TITLE\tCOUNT\tTOTAL\tPRECISION\tAVERAGE_SCORE\tPRECISION/AVERAGE_SCORE");
		for (Map.Entry<String, Integer> bs : beforeSorted.entrySet()) {
			String word = bs.getKey();
			int count = bs.getValue();
			int total = all.get(word);
			double precision = count / (double) total;
			Double totalScore = allAfterScores.get(word);
			double averageScore = (totalScore != null ? totalScore / allAfterScoresSum : 0);
			System.out.printf(Locale.ROOT, "%16s\t%d\t%d\t%.6f\t%.6f\t%8.1f\n", word, count, total, precision, averageScore, precision / averageScore);
		}
		System.out.println();
		Map<String, Integer> afterSorted = after.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) -> { throw new AssertionError(); }, LinkedHashMap::new));
		System.out.println("AFTER_TOOL_TITLE\tCOUNT\tTOTAL\tPRECISION\tAVERAGE_SCORE\tPRECISION/AVERAGE_SCORE");
		for (Map.Entry<String, Integer> as : afterSorted.entrySet()) {
			String word = as.getKey();
			int count = as.getValue();
			int total = all.get(word);
			double precision = count / (double) total;
			Double totalScore = allBeforeScores.get(word);
			double averageScore = (totalScore != null ? totalScore / allBeforeScoresSum : 0);
			System.out.printf(Locale.ROOT, "%16s\t%d\t%d\t%.6f\t%.6f\t%8.1f\n", word, count, total, precision, averageScore, precision / averageScore);
		}
	}

	private static boolean requiredArgs(String[] requiredArgs, String arg, PubMedAppsArgs args) throws ReflectiveOperationException {
		for (String requiredArg : requiredArgs) {
			Field requiredArgField = PubMedAppsArgs.class.getDeclaredField(requiredArg);
			if (requiredArgField.get(args) == null) {
				logger.error("{} is required for {}", Arrays.toString(requiredArgField.getAnnotation(Parameter.class).names()),
					Arrays.toString(PubMedAppsArgs.class.getDeclaredField(arg).getAnnotation(Parameter.class).names()));
				return false;
			}
		}
		return true;
	}

	private static void run(PubMedAppsArgs args) throws IOException, ParseException, ReflectiveOperationException, URISyntaxException {
		args.preProcessorArgs.setStemming(false);

		if (args.meshQuery) {
			meshQuery(args.fetcherArgs);
		}

		if (args.writeWebDoc && requiredArgs(new String[] { "idf", "db", "pub", "query", "type", "web", "doc" }, "writeWebDoc", args)) {
			writeWebDoc(args.preProcessorArgs, args.idf, args.db, args.pub, args.query, args.type, args.web, args.doc, args.fetcherArgs);
		}

		if (args.printResults && requiredArgs(new String[] { "idf", "db", "pub", "query", "type" }, "printResults", args)) {
			printResults(args.preProcessorArgs, args.idf, args.db, args.pub, args.query, args.type, args.fetcherArgs);
		}

		if (args.beforeAfter && requiredArgs(new String[] { "idf", "db", "pub" }, "beforeAfter", args)) {
			beforeAfter(args.preProcessorArgs, args.idf, args.db, args.pub);
		}
	}

	public static void main(String[] argv) throws IOException, ReflectiveOperationException {
		Version version = new Version(PubMedApps.class);

		PubMedAppsArgs args = BasicArgs.parseArgs(argv, PubMedAppsArgs.class, version);

		// logger must be called only after configuration changes have been made in BasicArgs.parseArgs()
		// otherwise invalid.log will be created if arg --log is null
		logger = LogManager.getLogger();
		logger.debug(String.join(" ", argv));
		logger.info("This is {} {}", version.getName(), version.getVersion());

		try {
			run(args);
		} catch (Throwable e) {
			logger.error("Exception!", e);
		}
	}
}
