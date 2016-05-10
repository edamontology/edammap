package edammapper.preprocessing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edammapper.cots.Stemmer;

public class PreProcessor {

	// Remove hyphen and all whitespace after a hyphen, if this whitespace contains a line break
	// Hyphen-minus or hyphen, optionally followed by any whitespace, followed by a line break (line feed, vertical tab, form feed, carriage return, next line, line separator or paragraph separator), optionally followed by any whitespace
	private final Pattern HYPHENATION = Pattern.compile("[\\u002D\\u2010][\\p{Z}\\p{Cc}]*[\\u000A\\u000B\\u000C\\u000D\\u0085\\u2028\\u2029][\\p{Z}\\p{Cc}]*");
	// This will do the wrong thing in case of an unfortunate line break after a suspended hyphen (nineteenth-\n and twentieth-century), however assume these are quite rare (compared to legitimate cases anyway).

	// en dash or em dash or slash
	private final Pattern WORD_DIVIDER = Pattern.compile("[\\u2013\\u2014\\u002F]");

	// Characters that can be used to represent an apostrophe, will be changed to ' (\\u0027) 
	private final Pattern APOSTROPHE = Pattern.compile("[\\u0060\\u00B4\\u2018\\u2019\\u02BC\\u201B\\u0091\\u0092]");

	// Punctuation or symbol, except apostrophe
	private final Pattern PUNCTUATION = Pattern.compile("(?=[\\p{P}\\p{S}])[^']");

	// A sequence of separators and control characters
	private final Pattern WHITESPACE = Pattern.compile("[\\p{Z}\\p{Cc}]+");

	// Remove freestanding apostrophe, or apostrophe at beginning or end of word
	private final Pattern APOSTROPHE_REMOVE = Pattern.compile("('+ '+)|((^| )'+)|('+( |$))");

	// Freestanding number (not part of a word)
	private final Pattern NUMBER = Pattern.compile("(^| )[\\p{N}]+( |$)");

	private final Pattern INTERNAL_TRIM = Pattern.compile("  +");

	private final boolean numberRemove;

	private final List<String> stopwords;

	private final Stemmer stemmer;

	private final int shortWord;

	public PreProcessor(PreProcessorArgs args) throws IOException {
		this.numberRemove = args.isNumberRemove();

		if (args.getStopwords() != Stopwords.off) {
			String resourceName = "stopwords/" + args.getStopwords() + ".txt";
			InputStream resource = this.getClass().getResourceAsStream("/" + resourceName);

			if (resource != null) {
				try (BufferedReader br = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
					this.stopwords = br.lines().filter(s -> !s.startsWith("#")).collect(Collectors.toList());
				}
			} else {
				throw new MissingResourceException("Can't find stopword list " + resourceName, this.getClass().getSimpleName(), resourceName);
			}
		} else {
			this.stopwords = null;
		}

		if (args.isNoStemming()) {
			this.stemmer = null;
		} else {
			this.stemmer = new Stemmer();
		}

		this.shortWord = args.getShortWord();
	}

	public List<String> process(String input) {
		input = HYPHENATION.matcher(input).replaceAll("");

		input = WORD_DIVIDER.matcher(input).replaceAll(" ");

		input = APOSTROPHE.matcher(input).replaceAll("'");

		input = PUNCTUATION.matcher(input).replaceAll("");

		input = WHITESPACE.matcher(input).replaceAll(" ");

		input = APOSTROPHE_REMOVE.matcher(input).replaceAll(" ");

		if (numberRemove) {
			input = NUMBER.matcher(input).replaceAll(" ");
		}

		input = INTERNAL_TRIM.matcher(input).replaceAll(" ");

		input = input.trim();

		input = input.toLowerCase(Locale.ROOT);

		List<String> output = Arrays.stream(input.split(" ")).collect(Collectors.toList());

		if (stopwords != null) {
			output.removeIf(s -> stopwords.contains(s));
		}

		if (stemmer != null) {
			for (int i = 0; i < output.size(); ++i) {
				stemmer.add(output.get(i).toCharArray(), output.get(i).length());
				stemmer.stem();
				output.set(i, stemmer.toString());
			}
		}

		output.removeIf(s -> s.length() <= shortWord);

		return output;
	}
}
