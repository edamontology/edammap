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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class License {

	private static final Logger logger = LogManager.getLogger();

	private static final String VERSION = "(\\p{N}+)([^\\p{N}]+(\\p{N}+)([^\\p{N}]+(\\p{N}+))?)?";
	private static final Pattern VERSION_SPDX = Pattern.compile("^" + VERSION);
	private static final Pattern VERSION_SEARCH = Pattern.compile("[vV(>=≥]*" + VERSION);
	private static final Pattern LGPL = Pattern.compile("(?i) *(Lesser|Library) (General Public Licen[sc]e|GPL)");
	private static final Pattern AGPL = Pattern.compile("(?i) *Affero (General Public Licen[sc]e|GPL|Licen[sc]e)");
	private static final Pattern GPL = Pattern.compile("(?i) *(General|GNU) Public Licen[sc]e");
	private static final Pattern LICENSE_BEGIN = Pattern.compile("(?i)^Licen[sc]e: *");
	private static final Pattern GNU_BEGIN = Pattern.compile("(?i)^GNU +");
	static final String SEPARATOR_CHARS = " _/\\-";
	static final Pattern SEPARATOR = Pattern.compile("[" + SEPARATOR_CHARS + "]+");
	static final String START_CHARS = "\"'(\\[>=≥";
	static final String END_CHARS = "\\]),;:'\"®™";
	static final Pattern START = Pattern.compile("^[" + START_CHARS + "]+");
	static final Pattern END = Pattern.compile("[" + END_CHARS + "]+$");
	private static final Pattern ACRONYM = Pattern.compile("^(\\p{Lu}+)[^\\p{N}]*$");
	private static final Pattern LICENSE = Pattern.compile("(?i)Licen[sc]");
	private static final Pattern NUMBER = Pattern.compile("\\p{N}");

	public static class LicenseSearch {

		private List<String> parts = new ArrayList<>();

		private List<List<Integer>> versions = new ArrayList<>();

		public LicenseSearch(String licenseSearch) {
			licenseSearch = Common.WHITESPACE.matcher(licenseSearch).replaceAll(" ");
			licenseSearch = LGPL.matcher(licenseSearch).replaceAll(" LGPL");
			licenseSearch = AGPL.matcher(licenseSearch).replaceAll(" AGPL");
			licenseSearch = GPL.matcher(licenseSearch).replaceAll(" GPL");
			licenseSearch = licenseSearch.trim();
			licenseSearch = LICENSE_BEGIN.matcher(licenseSearch).replaceFirst("");
			licenseSearch = GNU_BEGIN.matcher(licenseSearch).replaceFirst("");
			for (String part : SEPARATOR.split(licenseSearch)) {
				part = START.matcher(part).replaceFirst("");
				part = END.matcher(part).replaceFirst("");
				if (!getVersion(part, versions)) {
					parts.add(part);
				}
			}
		}

		private boolean getVersion(String part, List<List<Integer>> versions) {
			Matcher versionMatcher = VERSION_SEARCH.matcher(part);
			if (versionMatcher.find()) {
				try {
					int version1 = Integer.parseInt(versionMatcher.group(1));
					int version2 = -1;
					if (versionMatcher.group(3) != null) {
						version2 = Integer.parseInt(versionMatcher.group(3));
					}
					int version3 = -1;
					if (versionMatcher.group(5) != null) {
						version3 = Integer.parseInt(versionMatcher.group(5));
					}
					if (versionMatcher.start() > 0) {
						parts.add(part.substring(0, versionMatcher.start()));
					}
					List<Integer> version = new ArrayList<>();
					version.add(parts.size());
					version.add(version1);
					version.add(version2);
					version.add(version3);
					versions.add(version);
					parts.add(part);
					if (versionMatcher.end() < part.length()) {
						parts.add(part.substring(versionMatcher.end()));
					}
					return true;
				} catch (NumberFormatException e) {
					logger.debug("License part {} has version number format exception: {}", part, e.getMessage());
				}
			}
			return false;
		}

		public License bestMatch(List<License> licenses, boolean definitelyContainsLicense) {
			License bestLicense = null;
			Match bestMatch = new Match();
			for (License license : licenses) {
				Match match = license.match(this, definitelyContainsLicense);
				if (match.matches > 0) {
					if (match.matches > bestMatch.matches) {
						bestLicense = license;
						bestMatch = match;
					} else if (match.matches == bestMatch.matches) {
						if (match.versionMatches > bestMatch.versionMatches || match.acronymMatch && !bestMatch.acronymMatch) {
							bestLicense = license;
							bestMatch = match;
						} else if (match.versionMatches == bestMatch.versionMatches) {
							if (bestMatch.versionMatches == 0) {
								if (license.parts.get(0).equals("BSD")) {
									if (license.version[0] == 3 && bestLicense.version[0] != 3 || license.version[0] == 2 && bestLicense.version[0] != 2 && bestLicense.version[0] != 3) {
										bestLicense = license;
									}
								} else if (license.version[0] > bestLicense.version[0]
										|| license.version[0] == bestLicense.version[0] && license.version[1] > bestLicense.version[1]
										|| license.version[0] == bestLicense.version[0] && license.version[1] == bestLicense.version[1] && license.version[2] > bestLicense.version[2]
										|| license.version[0] == bestLicense.version[0] && license.version[1] == bestLicense.version[1] && license.version[2] == bestLicense.version[2] && license.parts.size() < bestLicense.parts.size()) {
									bestLicense = license;
								}
							} else if (bestMatch.versionMatches == 1) {
								if (license.version[1] < bestLicense.version[1]
										|| license.version[1] == bestLicense.version[1] && license.version[2] < bestLicense.version[2]
										|| license.version[1] == bestLicense.version[1] && license.version[2] == bestLicense.version[2] && license.parts.size() < bestLicense.parts.size()) {
									bestLicense = license;
								}
							} else if (bestMatch.versionMatches == 2) {
								if (license.version[2] < bestLicense.version[2]
										|| license.version[2] == bestLicense.version[2] && license.parts.size() < bestLicense.parts.size()) {
									bestLicense = license;
								}
							}
						}
					}
				}
			}
			return bestLicense;
		}
	}

	private static class Match {
		private int matches = 0;
		private int versionMatches = 0;
		private boolean acronymMatch = false;
	}

	private final String original;

	private List<String> parts = new ArrayList<>();

	private int[] version = { -1, -1, -1 };

	public License(String license) {
		original = license;
		for (String part : license.split("-")) {
			if (version[0] < 0) {
				getVersion(part, version);
				if (version[0] < 0) {
					parts.add(part);
				}
			} else {
				parts.add(part);
			}
		}
	}

	private void getVersion(String part, int[] version) {
		Matcher versionMatcher = VERSION_SPDX.matcher(part);
		if (versionMatcher.find()) {
			try {
				version[0] = Integer.parseInt(versionMatcher.group(1));
				if (versionMatcher.group(3) != null) {
					version[1] = Integer.parseInt(versionMatcher.group(3));
				}
				if (versionMatcher.group(5) != null) {
					version[2] = Integer.parseInt(versionMatcher.group(5));
				}
				if (versionMatcher.end() < part.length()) {
					parts.add(part.substring(versionMatcher.end()));
				}
			} catch (NumberFormatException e) {
				logger.debug("License part {} has version number format exception: {}", part, e.getMessage());
				version[0] = -1;
				version[1] = -1;
				version[2] = -1;
			}
		}
	}

	private Match match(LicenseSearch licenseSearch, boolean definitelyContainsLicense) {
		Match match = new Match();
		if (licenseSearch == null || parts.size() == 0) {
			return match;
		}
		int search = 0;
		int matched = 0;
		while (search < licenseSearch.parts.size()) {
			int matches = 0;
			int firstIndex = -1;
			int lastIndex = -1;
			String acronym = "";
			if (search == 0 && definitelyContainsLicense) {
				for (;; ++search) {
					if (search >= licenseSearch.parts.size()) {
						search = 0;
						break;
					}
					String part = licenseSearch.parts.get(search);
					Matcher acronymMatcher = ACRONYM.matcher(part);
					if (acronymMatcher.find()) {
						acronym += acronymMatcher.group(1);
						if (acronym.length() >= 3 && acronym.length() <= 7) {
							if (acronym.equals(parts.get(0))) {
								firstIndex = 0;
								lastIndex = search;
								++matches;
								match.acronymMatch = true;
								++search;
								break;
							}
						} else if (acronym.length() > 7) {
							search = 0;
							break;
						}
					} else {
						search = 0;
						break;
					}
				}
			}
			for (; search < licenseSearch.parts.size() && matches + matched < parts.size(); ++search) {
				if (lastIndex > -1 && search - lastIndex > 4) {
					break;
				}
				String part = licenseSearch.parts.get(search);
				if (part.equalsIgnoreCase(parts.get(matches + matched))) {
					if (firstIndex < 0) {
						firstIndex = search;
					}
					lastIndex = search;
					++matches;
				}
			}
			if (matches > match.matches) {
				boolean isLicense = false;
				if (!definitelyContainsLicense && !original.contains("GPL")) {
					for (int i = (firstIndex - 2 < 0 ? 0 : firstIndex - 2); i <= lastIndex + 4 && i < licenseSearch.parts.size(); ++i) {
						if (LICENSE.matcher(licenseSearch.parts.get(i)).find()) {
							isLicense = true;
							break;
						}
					}
				} else {
					isLicense = true;
				}
				if (isLicense) {
					match.matches = matches;
					match.versionMatches = 0;
					if (version[0] > -1) {
						Boolean mismatch = null;
						for (List<Integer> versionSearch : licenseSearch.versions) {
							int versionMatches = 0;
							if (versionSearch.get(0) >= firstIndex - 2 && versionSearch.get(0) <= lastIndex + 3) {
								if (versionSearch.get(1) >= 0 && versionSearch.get(1) == version[0]) {
									mismatch = false;
									++versionMatches;
									if (versionSearch.get(2) >= 0 && versionSearch.get(2) == version[1]) {
										++versionMatches;
										if (versionSearch.get(3) >= 0 && versionSearch.get(3) == version[2]) {
											++versionMatches;
										}
									}
								} else {
									if (mismatch == null && !NUMBER.matcher(parts.get(0)).find()) {
										mismatch = true;
									}
								}
							}
							if (versionMatches > match.versionMatches) {
								match.versionMatches = versionMatches;
							}
						}
						if (mismatch != null && mismatch.booleanValue()) {
							return new Match();
						}
					}
				}
			}
			matched += matches;
			if (matched >= parts.size()) {
				break;
			}
		}
		return match;
	}

	public String getOriginal() {
		return original;
	}
}
