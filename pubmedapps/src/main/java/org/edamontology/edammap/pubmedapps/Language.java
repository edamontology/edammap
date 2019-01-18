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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Language {

	private static final Pattern CPP = Pattern.compile("(?i)(^|[" + License.START_CHARS + "]|[" + License.SEPARATOR_CHARS + "])C [+][+]([" + License.END_CHARS + "]|[" + License.SEPARATOR_CHARS + "]|$)");
	private static final Pattern SQL = Pattern.compile("(?i)(^|[" + License.START_CHARS + "]|[" + License.SEPARATOR_CHARS + "])(MySQL|PostgreSQL|SQLite)([" + License.END_CHARS + "]|[" + License.SEPARATOR_CHARS + "]|$)");
	private static final Pattern HYPHEN_CDR = Pattern.compile("(?i)-(C|D|R)([" + License.END_CHARS + "]|[" + License.SEPARATOR_CHARS + "]|$)");
	private static final Pattern KEYWORD_REQUIRED = Pattern.compile("(?i)^(C|D|R|Forth|PyMOL|Scheme|Shell)$");
	private static final Pattern R = Pattern.compile("(?i)^(bioconductor|bioconductor\\.org|www\\.bioconductor\\.org|shiny|cran|cran\\.r)$");
	private static final Pattern PYTHON = Pattern.compile("(?i)^(pypi|pypi\\.org|pypi\\.python\\.org|ipython|numpy|scipy|django|python2|python3)$");
	private static final Pattern JAVA = Pattern.compile("(?i)^(apps\\.cytoscape\\.org|biojava)$");

	public static class LanguageSearch {

		private List<String> parts = new ArrayList<>();

		public LanguageSearch(String languageSearch) {
			languageSearch = License.WHITESPACE.matcher(languageSearch).replaceAll(" ");
			languageSearch = CPP.matcher(languageSearch).replaceAll("$1C++$2");
			languageSearch = SQL.matcher(languageSearch).replaceAll("$1SQL$3");
			languageSearch = HYPHEN_CDR.matcher(languageSearch).replaceAll("$1$2");
			languageSearch = languageSearch.trim();
			for (String part : License.SEPARATOR.split(languageSearch)) {
				part = License.START.matcher(part).replaceFirst("");
				part = License.END.matcher(part).replaceFirst("");
				parts.add(part);
			}
		}

		public List<String> getMatches(List<Language> languages, boolean definitelyContainsLanguage, List<String> languageKeywords) {
			Map<String, Integer> matches = new LinkedHashMap<>();
			for (Language language : languages) {
				int index = language.match(this, definitelyContainsLanguage, languageKeywords);
				if (index > -1) {
					matches.put(language.original, index);
				}
			}
			return matches.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(e -> e.getKey()).collect(Collectors.toList());
		}
	}

	private final String original;

	private List<String> parts = new ArrayList<>();

	public Language(String language) {
		original = language;
		for (String part : language.split(" ")) {
			parts.add(part);
		}
	}

	private int match(LanguageSearch languageSearch, boolean definitelyContainsLanguage, List<String> languageKeywords) {
		if (languageSearch == null || parts.size() == 0 || parts.get(0).equals("Other")) {
			return -1;
		}
		String languagePart = parts.get(0);
		for (int i = 0; i < languageSearch.parts.size(); ++i) {
			String languageSearchPart = languageSearch.parts.get(i);
			if (languageSearchPart.equalsIgnoreCase(languagePart)) {
				if (languagePart.equals("Scheme")) {
					if (!languageSearchPart.equals(languagePart)) {
						continue;
					}
				}
				boolean equals = true;
				int j = 1;
				for (; j < parts.size(); ++j) {
					if (i + j >= languageSearch.parts.size() || !languageSearch.parts.get(i + j).equalsIgnoreCase(parts.get(j))) {
						equals = false;
						break;
					}
				}
				if (equals) {
					if (!definitelyContainsLanguage && KEYWORD_REQUIRED.matcher(languagePart).matches()) {
						for (int k = (i - 4 < 0 ? 0 : i - 4); k < (i + j + 4 > languageSearch.parts.size() ? languageSearch.parts.size() : i + j + 4); ++k) {
							if (k >= i && k < i + j) continue;
							for (String languageKeyword : languageKeywords) {
								if (languageKeyword.equalsIgnoreCase(languageSearch.parts.get(k))) {
									return i;
								}
							}
						}
					} else {
						return i;
					}
				}
			}
			if (languagePart.equals("R")) {
				if (R.matcher(languageSearchPart).matches()) {
					return i;
				}
			} else if (languagePart.equals("Python")) {
				if (PYTHON.matcher(languageSearchPart).matches()) {
					return i;
				}
			} else if (languagePart.equals("Java")) {
				if (JAVA.matcher(languageSearchPart).matches()) {
					return i;
				}
			}
		}
		return -1;
	}

	public String getOriginal() {
		return original;
	}
}
