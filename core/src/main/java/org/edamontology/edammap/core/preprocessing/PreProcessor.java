/*
 * Copyright © 2016, 2018 Erik Jaaniso
 *
 * This file is part of EDAMmap.
 *
 * EDAMmap is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EDAMmap is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EDAMmap.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.edamontology.edammap.core.preprocessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.edamontology.edammap.core.cots.Stemmer;

import org.edamontology.pubfetcher.core.common.PubFetcher;

public class PreProcessor {

	// hyphen-minus or hyphen
	private final String HYPHENATION_CODES = "\\u002D\\u2010";
	// separators, control characters or invisible formatting indicators
	private final String WHITESPACE_CODES = "\\p{Z}\\p{Cc}\\p{Cf}";
	// line feed, vertical tab, form feed, carriage return, next line, line separator or paragraph separator
	private final String LINEBREAK_CODES = "\\u000A\\u000B\\u000C\\u000D\\u0085\\u2028\\u2029";
	// en dash or em dash
	private final String DIVIDER_DASH_CODES = "\\u2013\\u2014";
	// except '
	private final String APOSTROPHE_CODES = "\\u0060\\u00B4\\u2018\\u2019\\u02BC\\u201B\\u0091\\u0092";
	// except "
	private final String QUOTATION_CODES = "\\u00AB\\u00BB\\u201A\\u201C\\u201D\\u201E\\u201F\\u2039\\u203A\\u2E42";
	// punctuation to be removed from beginning and end of words (except . and ?)
	private final String SEPARATOR_CODES = ",;:(\\[{<>}\\])'" + APOSTROPHE_CODES + "\"" + QUOTATION_CODES;

	// Remove hyphen and all whitespace after a hyphen, if this whitespace contains a line break
	private final Pattern HYPHENATION = Pattern.compile("[" + HYPHENATION_CODES + "][" + WHITESPACE_CODES + "]*[" + LINEBREAK_CODES + "][" + WHITESPACE_CODES + "]*");
	// This will do the wrong thing in case of an unfortunate line break after a suspended hyphen (nineteenth-\n and twentieth-century),
	// however assume these are quite rare (compared to legitimate cases anyway)

	// Punctuation dividing words without needing whitespace
	private final Pattern WORD_DIVIDER = Pattern.compile("[" + DIVIDER_DASH_CODES + "/]|--|---");

	// Sometimes there is no space after a period at the end of a sentence (also consider ? ending a sentence, but not !)
	private final Pattern PERIOD_FIX_UPPERCASE = Pattern.compile("(.)(\\.|\\?)(\\p{Lu})");
	private final Pattern PERIOD_FIX_NUMBER = Pattern.compile("([^\\p{N}])(\\.|\\?)(\\p{N})");

	// Characters that can be used to represent an apostrophe, will be changed to ' (\\u0027)
	private final Pattern APOSTROPHE = Pattern.compile("[" + APOSTROPHE_CODES + "]");

	// Punctuation or symbol, except apostrophe
	private final Pattern PUNCTUATION = Pattern.compile("(?=[\\p{P}\\p{S}])[^']");

	// Word consisting of only punctuation or symbol
	private final Pattern PUNCTUATION_ONLY = Pattern.compile("^[\\p{P}\\p{S}]+$");

	// Separator character (or . or ?) to be trimmed from beginning or end of word
	private final Pattern SEPARATOR_TRIM = Pattern.compile("^[.?" + SEPARATOR_CODES + "]$");

	// Remove 's from end of extracted words (after separator characters have been trimmed)
	private final Pattern POSSESSIVE = Pattern.compile("['" + APOSTROPHE_CODES + "]s$");

	// A sequence of whitespace
	private final Pattern WHITESPACE = Pattern.compile("[" + WHITESPACE_CODES + "]+");

	// Remove 's from end of processed words (after apostrophes and whitespace have been normalized)
	private final Pattern POSSESSIVE_REMOVE = Pattern.compile("'s( |$)");

	// Remove freestanding apostrophe, or apostrophe at beginning or end of word
	private final Pattern APOSTROPHE_REMOVE = Pattern.compile("('+ '+)|((^| )'+)|('+( |$))");

	// Make words separated by exactly one space
	private final Pattern INTERNAL_TRIM = Pattern.compile("  +");

	// New sentence (usually) begins with uppercase letter
	private final Pattern UPPERCASE_LETTER = Pattern.compile("^\\p{Lu}$");

	// For extracting links
	private final String EMAIL = "[a-zA-Z0-9+._-]+@[a-zA-Z0-9.-]+\\.[a-z]{2,}";
	private final Pattern LINK = Pattern.compile(
		"(^|(?<=[ .?])|--|[" + DIVIDER_DASH_CODES + "]|[" + SEPARATOR_CODES + "])[" + SEPARATOR_CODES + "]*[^ \\p{L}\\p{N}.?" + SEPARATOR_CODES + "]*" +
		"([a-zA-Z0-9][a-zA-Z0-9+._-]*@|[a-zA-Z][a-zA-Z0-9+.-]*://[ ]?)?(((([a-zA-Z0-9])|([a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]))\\.)+[a-z]{2,}|(([0-9]{1,3}\\.){3}[0-9]{1,3}))(:[1-9][0-9]{0,4})?"+
		"([/?#][^ " + DIVIDER_DASH_CODES + "]*|[^ \\p{L}\\p{N}.?" + SEPARATOR_CODES + "]*[" + SEPARATOR_CODES + "]*([.?" + SEPARATOR_CODES + "]|[" + DIVIDER_DASH_CODES + "]|--|(?= )|$)(" + EMAIL + ")?|\\p{Lu}[^ " + DIVIDER_DASH_CODES + ".]*)");
	private final Pattern LINK_END_REMOVE_HACK = Pattern.compile("(([)][.?]?|[(])\\p{Lu}[\\p{L}\\p{N}'-]*|Supplementary|Contact:|Operating)$");
	private final Pattern LINK_NEXT_REMOVE = Pattern.compile("\\p{Lu}[^.]*$");
	private final Pattern EMAIL_ONLY = Pattern.compile("^" + EMAIL + "$");
	private final Pattern LINK_EMAIL_REMOVE = Pattern.compile("(\\)|Contact:|/\\.)" + EMAIL);
	private final Pattern LINK_END_REMOVE = Pattern.compile("[^\\p{L}\\p{N}/]*$");
	private final Pattern LINK_START_REMOVE = Pattern.compile("^[^\\p{L}\\p{N}]*");
	private final Pattern KNOWN_SCHEMA = Pattern.compile("(?i)(http://|https://|ftp://)");
	private final Pattern TILDE = Pattern.compile("[\\u223C\\u02DC]");
	private final Pattern HYPHEN = Pattern.compile("[\\u2010]");

	// Freestanding number (not part of a word)
	private final Pattern NUMBER = Pattern.compile("^[\\p{N}]+$");

	private final boolean numbers;

	private final List<String> stopwords;

	private final Stemmer stemmer;

	private final int minLength;

	public PreProcessor(PreProcessorArgs args) throws IOException {
		this.numbers = args.isNumbers();

		this.stopwords = getStopwords(args.getStopwords());

		this.stemmer = (args.isStemming() ? new Stemmer() : null);

		this.minLength = args.getMinLength();
	}

	public PreProcessor(PreProcessorArgs args, List<String> stopwords) {
		this.numbers = args.isNumbers();

		this.stopwords = stopwords;

		this.stemmer = (args.isStemming() ? new Stemmer() : null);

		this.minLength = args.getMinLength();
	}

	public PreProcessor(boolean stemming) throws IOException {
		this.numbers = true;

		this.stopwords = getStopwords(Stopwords.off);

		this.stemmer = (stemming ? new Stemmer() : null);

		this.minLength = 1;
	}

	public static List<String> getStopwords(Stopwords stopwords) throws IOException {
		if (stopwords == Stopwords.off) return Collections.emptyList();
		else return PubFetcher.getResource(PreProcessor.class, "stopwords/" + stopwords + ".txt");
	}

	private String periodFix(String input) {
		Matcher mU = PERIOD_FIX_UPPERCASE.matcher(input);
		StringBuffer sbU = new StringBuffer();
		while (mU.find()) {
			mU.appendReplacement(sbU, "\\" + mU.group(1) + ". " + mU.group(3)); // mU.group(1) can be special character $ or \
		}
		mU.appendTail(sbU);
		Matcher mN = PERIOD_FIX_NUMBER.matcher(sbU.toString());
		StringBuffer sbN = new StringBuffer();
		while (mN.find()) {
			mN.appendReplacement(sbN, "\\" + mN.group(1) + ". " + mN.group(3)); // mN.group(1) can be special character $ or \
		}
		mN.appendTail(sbN);
		return sbN.toString();
	}

	public List<String> process(String input) {
		return process(input, null);
	}

	public List<String> process(String input, List<String> extracted) {
		input = HYPHENATION.matcher(input).replaceAll("");

		input = WORD_DIVIDER.matcher(input).replaceAll(" ");

		input = periodFix(input);

		input = APOSTROPHE.matcher(input).replaceAll("'");

		input = PUNCTUATION.matcher(input).replaceAll("");

		input = WHITESPACE.matcher(input).replaceAll(" ");

		input = POSSESSIVE_REMOVE.matcher(input).replaceAll(" ");

		input = APOSTROPHE_REMOVE.matcher(input).replaceAll(" ");

		input = INTERNAL_TRIM.matcher(input).replaceAll(" ");

		input = input.trim();

		input = input.toLowerCase(Locale.ROOT);

		List<String> output = Arrays.stream(input.split(" ")).collect(Collectors.toList());

		if (!numbers) {
			if (extracted == null) {
				output.removeIf(s -> NUMBER.matcher(s).matches());
			} else {
				for (int i = 0; i < output.size(); ++i) {
					if (NUMBER.matcher(output.get(i)).matches()) {
						output.remove(i);
						extracted.remove(i);
						--i;
					}
				}
			}
		}

		if (stopwords != null && !stopwords.isEmpty()) {
			if (extracted == null) {
				output.removeIf(s -> stopwords.contains(s));
			} else {
				for (int i = 0; i < output.size(); ++i) {
					if (stopwords.contains(output.get(i))) {
						output.remove(i);
						extracted.remove(i);
						--i;
					}
				}
			}
		}

		if (stemmer != null) {
			for (int i = 0; i < output.size(); ++i) {
				stemmer.add(output.get(i).toCharArray(), output.get(i).length());
				stemmer.stem();
				output.set(i, stemmer.toString());
			}
		}

		if (minLength > 1) {
			if (extracted == null) {
				output.removeIf(s -> s.length() < minLength);
			} else {
				for (int i = 0; i < output.size(); ++i) {
					if (output.get(i).length() < minLength) {
						output.remove(i);
						extracted.remove(i);
						--i;
					}
				}
			}
		}

		if (output.size() == 1 && output.get(0).isEmpty()) {
			output.remove(0);
			if (extracted != null && extracted.size() == 1) {
				extracted.remove(0);
			}
		}

		return output;
	}

	// will result in same size list as process() above, if numberRemove, stopwords and shortWord not used
	public List<String> extract(String input) {
		input = HYPHENATION.matcher(input).replaceAll("");

		input = WORD_DIVIDER.matcher(input).replaceAll(" ");

		input = periodFix(input);

		input = WHITESPACE.matcher(input).replaceAll(" ");

		input = INTERNAL_TRIM.matcher(input).replaceAll(" ");

		input = input.trim();

		List<String> output = Arrays.stream(input.split(" ")).collect(Collectors.toList());

		output.removeIf(s -> PUNCTUATION_ONLY.matcher(s).matches());

		if (output.size() == 1 && output.get(0).isEmpty()) output.remove(0);

		for (int i = 0; i < output.size(); ++i) {
			String word = output.get(i);
			while (SEPARATOR_TRIM.matcher(word.substring(0, 1)).matches()) word = word.substring(1);
			while (SEPARATOR_TRIM.matcher(word.substring(word.length() - 1)).matches()) word = word.substring(0, word.length() - 1);
			output.set(i, word);
		}

		for (int i = 0; i < output.size(); ++i) {
			output.set(i, POSSESSIVE.matcher(output.get(i)).replaceFirst(""));
		}

		return output;
	}

	// not very good, but might be enough for our purposes
	public List<String> sentences(String input) {
		input = HYPHENATION.matcher(input).replaceAll("");

		input = WORD_DIVIDER.matcher(input).replaceAll(" ");

		input = periodFix(input);

		input = WHITESPACE.matcher(input).replaceAll(" ");

		input = INTERNAL_TRIM.matcher(input).replaceAll(" ");

		input = input.trim();

		List<String> output = Arrays.stream(input.split("(\\. )|(\\? )|(\\.$)|(\\?$)")).collect(Collectors.toList());

		for (int i = 0; i < output.size(); ++i) {
			output.set(i, output.get(i).trim());
		}

		output.removeIf(s -> s.isEmpty());

		if (output.size() > 1) {
			if (!output.get(0).contains(" ")) {
				output.set(1, output.get(0) + " " + output.get(1));
				output.remove(0);
			}
		}
		for (int i = 1; i < output.size(); ++i) {
			String current = output.get(i);
			if (!current.contains(" ") || !UPPERCASE_LETTER.matcher(current.substring(0, 1)).matches()) {
				output.set(i - 1, output.get(i - 1) + ". " + current);
				output.remove(i);
				--i;
			}
		}

		return output;
	}

	private boolean notLink(String link, boolean allTwoPart) {
		if (link.indexOf("/") < 0) {
			link = LINK_END_REMOVE.matcher(link).replaceAll("");
			link = LINK_START_REMOVE.matcher(link).replaceAll("");
			int period = link.indexOf(".");
			if (period > -1 && link.substring(0, period).indexOf('@') < 0) {
				String domain = link.substring(period + 1);
				period = domain.indexOf(".");
				if (period < 0 && (domain.length() > 3 && !domain.equals("info") || allTwoPart)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<String> links(String input) {
		input = HYPHENATION.matcher(input).replaceAll("");

		input = WHITESPACE.matcher(input).replaceAll(" ");

		input = INTERNAL_TRIM.matcher(input).replaceAll(" ");

		input = input.trim();

		List<String> output = new ArrayList<>();

		Matcher linkMatcher = LINK.matcher(input);
		while (linkMatcher.find()) {
			String link = input.substring(linkMatcher.start(), linkMatcher.end());

			link = LINK_END_REMOVE_HACK.matcher(link).replaceAll("");

			if (notLink(link, false)) continue;

			if (link.indexOf("/") < 0) {
				link = LINK_NEXT_REMOVE.matcher(link).replaceAll("");
			}

			link = WHITESPACE.matcher(link).replaceAll("");

			link = LINK_EMAIL_REMOVE.matcher(link).replaceAll("");

			link = LINK_END_REMOVE.matcher(link).replaceAll("");

			link = LINK_START_REMOVE.matcher(link).replaceAll("");

			Matcher knownSchema = KNOWN_SCHEMA.matcher(link);
			if (knownSchema.find()) {
				link = link.substring(knownSchema.start());
			}

			if (!EMAIL_ONLY.matcher(link).matches() && !link.isEmpty()) {
				output.add(HYPHEN.matcher(TILDE.matcher(link).replaceAll("~")).replaceAll("-"));
			}
		}

		return output;
	}

	public String removeLinks(String input) {
		input = HYPHENATION.matcher(input).replaceAll("");

		input = WHITESPACE.matcher(input).replaceAll(" ");

		input = INTERNAL_TRIM.matcher(input).replaceAll(" ");

		input = input.trim();

		String output = "";

		int from = 0;
		Matcher linkMatcher = LINK.matcher(input);

		while (linkMatcher.find()) {
			if (from > 0 && (input.charAt(from - 1) == '.' || input.charAt(from - 1) == '?')) {
				output += ". ";
			}
			output += input.substring(from, linkMatcher.start());
			output += " ";

			String link = input.substring(linkMatcher.start(), linkMatcher.end());

			link = LINK_END_REMOVE_HACK.matcher(link).replaceAll("");

			if (notLink(link, true)) {
				output += link;
				output += " ";
			} else {
				Matcher startMatcher = LINK_START_REMOVE.matcher(link);
				if (startMatcher.find() && link.substring(startMatcher.start(), startMatcher.end()).contains("(")) {
					output += "(";
				}
				Matcher endMatcher = LINK_END_REMOVE.matcher(link);
				if (endMatcher.find() && link.substring(endMatcher.start(), endMatcher.end()).contains(")")) {
					output += ")";
				}
			}

			from = linkMatcher.end();
		}

		if (from > 0 && (input.charAt(from - 1) == '.' || input.charAt(from - 1) == '?')) {
			output += ". ";
		}
		output += input.substring(from);

		return output.toString();
	}
}
