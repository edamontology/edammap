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
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.beust.jcommander.Parameter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.edamontology.pubfetcher.core.common.BasicArgs;
import org.edamontology.pubfetcher.core.common.FetcherArgs;
import org.edamontology.pubfetcher.core.common.PubFetcher;
import org.edamontology.pubfetcher.core.common.Version;
import org.edamontology.pubfetcher.core.db.Database;
import org.edamontology.pubfetcher.core.db.publication.CorrespAuthor;
import org.edamontology.pubfetcher.core.db.publication.Publication;
import org.edamontology.pubfetcher.core.db.publication.PublicationIds;
import org.edamontology.pubfetcher.core.db.webpage.Webpage;
import org.edamontology.pubfetcher.core.fetching.Fetcher;
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
import org.edamontology.edammap.core.preprocessing.PreProcessorArgs;
import org.edamontology.edammap.core.query.QueryType;
import org.edamontology.edammap.pubmedapps.Language.LanguageSearch;
import org.edamontology.edammap.pubmedapps.License.LicenseSearch;

public final class PubMedApps {

	private static Logger logger;

	// TODO meshQuery
	//private static final String MESH_QUERY = "(Software[MeSH Terms]) AND (genetics[MeSH Subheading] OR Genetics[MeSH Terms] OR Genomics[MeSH Terms] OR Genetic Phenomena[MeSH Terms] OR Biochemical Phenomena[MeSH Terms] OR Genetic Techniques[MeSH Terms] OR Molecular Probe Techniques[MeSH Terms] OR Nucleic Acids, Nucleotides, and Nucleosides[MeSH Terms] OR Amino Acids, Peptides, and Proteins[MeSH Terms]) AND (\"2013/01/01\"[PDat] : \"2100/01/01\"[PDat])";
	private static final String MESH_QUERY = "(Software[MeSH Terms]) AND (\"2013/01/01\"[PDat] : \"2017/12/31\"[PDat])";

	private static final Pattern USE_IN_PATTERN = Pattern.compile("^[\\p{L}\\p{N}]$");

	private static final String TOOL_TITLE_GENERAL = "database|data|web|server|webserver|web-server|package|toolkit|toolbox|suite|toolsuite|tools|tool|kit|framework|workbench|pipeline|software|program|platform|project|resource|r";
	private static final Pattern TOOL_TITLE_PRUNE = Pattern.compile("(?i)^(update|v|ver|version|(v|ver|version)?\\p{N}+([.-]\\p{N}+)*|" + TOOL_TITLE_GENERAL + ")$");
	private static final double TOOL_TITLE_MULTIPLIER = 24;
	private static final Pattern TOOL_TITLE_TRIM = Pattern.compile("( ?(db|v|ver|version|update))*( ?\\p{N}{0,4})?( ?(db|v|ver|version|update))*$");
	private static final Pattern TOOL_TITLE_INVALID = Pattern.compile("(?i)^(correction|erratum)( to)?$");

	private static final Pattern TOOL_TITLE_SEPARATOR = Pattern.compile("(?i),? (and|&) ");
	private static final Pattern TOOL_TITLE_SEPARATOR_ALL = Pattern.compile("(?i)(,? (and|&) )|(, )");
	private static final int TOOL_TITLE_SEPARATOR_MAX_WORDS = 5;
	private static final int TOOL_TITLE_STANDALONE_MAX_CHARS = 18;
	private static final Pattern TOOL_TITLE_NOT_ALPHANUM = Pattern.compile("[^\\p{L}\\p{N}]");

	private static final Pattern ACRONYM_STOP = Pattern.compile("(?i)(http:|https:|ftp:|;|, |: )");

	private static final double TIER_1_MULTIPLIER = 6;
	private static final double TIER_2_MULTIPLIER = 3;
	private static final double TIER_3_MULTIPLIER = 1.5;
	private static final double BEFORE_AFTER_LIMIT = 72;

	private static final Pattern GOOD_START = Pattern.compile("^(\\p{Lu}|.[^\\p{Ll}-]|.-[^\\p{Ll}]|.[^-]*[^\\p{L}-])[^-]*$");
	private static final Pattern GOOD_END = Pattern.compile("^(.*[^\\p{Ll}]|.*[^\\p{Ll}-].|\\p{Lu}.*|..)$");
	private static final Pattern GOOD_START_MULTI = Pattern.compile("^[^ ]+( \\p{Lu}[^ ]*)*( v| ver| version)?( \\p{Lu}[^ ]*| ([vV](er(sion)?)?)?\\p{N}+([.-]\\p{N}+)*)$");
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
	private static final Pattern LINK_COMPARE_END = Pattern.compile("/+$");
	private static final Pattern LINK_COMPARE_INDEX = Pattern.compile("/+index\\.[\\p{L}\\p{N}]+$");
	private static final Pattern LINK_COMPARE_REST = Pattern.compile("^(\\p{Ll}\\p{Lu}|./*[.]?[\"(\\[{<>}\\])]+[.]?|.\\.\\p{Lu}[\\p{L}\\p{N}'-]|//*\\.|./+\\.|./*--|./*[^/]+@[^/]+\\.[^/]+)[\\p{L}\\p{N}'-]*$");
	private static final Pattern LINK_COMPARE_SCHEMA = Pattern.compile("(http|https|ftp)://");

	private static final Pattern FIX_LINK = Pattern.compile("([.]?[\"(\\[{<>}\\])]+[.]?|\\.\\p{Lu}|--)[\\p{L}\\p{N}'-]+$");
	private static final Pattern FIX_LINK_KEEP1 = Pattern.compile("(\\.[\\p{Ll}\\p{N}]+)\\p{Lu}[\\p{L}\\p{N}'-]*$");
	private static final Pattern FIX_LINK_KEEP2 = Pattern.compile("(/)\\.[\\p{L}\\p{N}'-]*$");
	private static final Pattern FIX_LINK_EMAIL1 = Pattern.compile("[.]?[^/.]+@[^/]+\\.[^/]+$");
	private static final Pattern FIX_LINK_EMAIL2 = Pattern.compile("[.]?[^/.]+\\.[^/.]+@[^/]+\\.[^/]+$");
	private static final Pattern FIX_LINK_EMAIL3 = Pattern.compile("[.]?[^/.]+\\.[^/.]+\\.[^/.]+@[^/]+\\.[^/]+$");

	private static final Pattern BIOTOOLS_EXTRACTED_VERSION_TRIM = Pattern.compile(" ?([vV](er(sion)?)?)? ?\\p{N}+([.-]\\p{N}+)*$");
	private static final Pattern BIOTOOLS_PROCESSED_VERSION_TRIM = Pattern.compile(" ?([v](er(sion)?)?)? ?\\p{N}+$");

	private static final Pattern TITLE_SEPARATOR = Pattern.compile("(?i)(: | - |--a |--an |--|-a |-an |:a |:an |, a |, an |\n|\r|\\|)");
	private static final Pattern WEBPAGE_TITLE_SEPARATOR = Pattern.compile("(?i)(\\||---|--| - |::|: | // | / | @ | \\< | \\> |«|»|·|•|\n|\r|, a |, an )");

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
	private static final int NAME_WORD_MATCH_LIMIT = 5;
	private static final int NAME_DIFFERENT_MESSAGE_LIMIT = 5;

	private static final Pattern LINK_HELPDESK = Pattern.compile("(?i)(^|[^\\p{L}-])(contact|contactus|help[-_]?desk)s?([^\\p{L}-]|$)");
	private static final Pattern LINK_ISSUES = Pattern.compile("(?i)^(https?://)?(www\\.)?(github\\.com/+[^/]+/+[^/]+/+issues|sourceforge\\.net/+p/+[^/]+/+tickets|code\\.google\\.com/+(archive/+)?p/+[^/]+/+issues|bitbucket\\.org/+[^/]+/+[^/]+/+issues)([^\\p{L}]|$)");
	private static final Pattern LINK_LIST_ADDR = Pattern.compile("(?i)^(https?://)?(www\\.)?(groups\\.google\\.com|gitter\\.im|sourceforge\\.net/+p/+[^/]+/+discussion|sourceforge\\.net/+projects/+[^/]+/+lists)([^\\p{L}]|$)");
	private static final Pattern LINK_LIST_BOTH = Pattern.compile("(?i)(^|[^\\p{L}-])(mailman|listinfo|mailing[-_]?lists?)([^\\p{L}-]|$)");
	private static final Pattern LINK_REGISTRY = Pattern.compile("(?i)^(https?://)?(www\\.)?(mybiosoftware\\.com|biocatalogue\\.org)([^\\p{L}]|$)");
	private static final Pattern LINK_REPOSITORY = Pattern.compile("(?i)^(https?://)?(www\\.)?(bioconductor\\.org|github\\.com|sourceforge\\.net|code\\.google\\.com|cran\\.r-project\\.org|bitbucket\\.org|gitlab\\.com|pypi\\.(python\\.)?org|apps\\.cytoscape\\.org)([^\\p{L}]|$)");
	private static final Pattern LINK_SOCIAL = Pattern.compile("(?i)^(https?://)?(www\\.)?(twitter\\.com|facebook\\.com)([^\\p{L}]|$)");

	private static final Pattern DOWNLOAD_SRC_CODE = Pattern.compile("(?i)^(https?://)?(www\\.)?(git\\.bioconductor\\.org|github\\.com/+[^/]+/+[^/]+/+tree|sourceforge\\.net/+projects/+[^/]+/+files|code\\.google\\.com/+(archive/+)?p/+[^/]+/+source|bitbucket\\.org/+[^/]+/+[^/]+/+src)([^\\p{L}]|$)");
	private static final Pattern DOWNLOAD_SRC_PKG = Pattern.compile("(?i)^(https?://)?(www\\.)?(github\\.com/+[^/]+/+[^/]+/+releases|sourceforge\\.net/+projects/+[^/]+/+files/+.+/+download|code\\.google\\.com/+(archive/+)?p/+[^/]+/+downloads|bitbucket\\.org/+[^/]+/+[^/]+/+downloads|apps\\.cytoscape\\.org/+download)([^\\p{L}]|$)");
	private static final Pattern DOWNLOAD_EXT = Pattern.compile("(?i)\\.(gz|zip|bz2|tar|tgz|7z|rar|xz|jar|exe)([^\\p{L}-]|$)");
	private static final Pattern DOWNLOAD_FTP = Pattern.compile("(?i)^ftp://");
	private static final Pattern DOWNLOAD_API = Pattern.compile("(?i)\\.(wsdl)([^\\p{L}-]|$)");
	private static final Pattern DOWNLOAD_CONTAINER = Pattern.compile("(?i)(^|[^\\p{L}-])(docker)([^\\p{L}-]|$)");
	private static final Pattern DOWNLOAD_CWL = Pattern.compile("(?i)\\.(cwl)([^\\p{L}-]|$)");
	private static final Pattern DOWNLOAD_PAGE = Pattern.compile("(?i)(^|[^\\p{Ll}])download(s|ing)?([^\\p{Ll}]|$)");

	private static final Pattern DOCUMENTATION_API = Pattern.compile("(?i)(^|[^\\p{L}-])(api|apidoc)s?([^\\p{L}-]|$)");
	private static final String DOCUMENTATION_CITE_EITHER = "citing";
	private static final Pattern DOCUMENTATION_CITE = Pattern.compile("(?i)((^|[^\\p{L}-])(references|cite|citation)s?([^\\p{L}-]|$))|((" + DOCUMENTATION_CITE_EITHER + ")s?([^\\p{L}-]|$))|((^|[^\\p{L}-])(" + DOCUMENTATION_CITE_EITHER + "))");
	private static final String DOCUMENTATION_GENERAL_EITHER = "faq|about|read[-_]?me|information|overview|description|features";
	private static final Pattern DOCUMENTATION_GENERAL = Pattern.compile("(?i)((" + DOCUMENTATION_GENERAL_EITHER + ")s?([^\\p{L}-]|$))|((^|[^\\p{L}-])(" + DOCUMENTATION_GENERAL_EITHER + "))");
	private static final String DOCUMENTATION_INSTALL_EITHER = "install|installation|installing";
	private static final Pattern DOCUMENTATION_INSTALL = Pattern.compile("(?i)((" + DOCUMENTATION_INSTALL_EITHER + ")s?([^\\p{L}-]|$))|((^|[^\\p{L}-])(" + DOCUMENTATION_INSTALL_EITHER + "))");
	private static final String DOCUMENTATION_TERMS_EITHER = "terms[-_]?of[-_]?use|conditions[-_]?of[-_]?use";
	private static final Pattern DOCUMENTATION_TERMS = Pattern.compile("(?i)((^|[^\\p{L}-])(terms|conditions|legal|license|copyright|copying)s?([^\\p{L}-]|$))|((" + DOCUMENTATION_TERMS_EITHER + ")s?([^\\p{L}-]|$))|((^|[^\\p{L}-])(" + DOCUMENTATION_TERMS_EITHER + "))");
	private static final String DOCUMENTATION_TRAINING_EITHER = "training|exercise";
	private static final Pattern DOCUMENTATION_TRAINING = Pattern.compile("(?i)((" + DOCUMENTATION_TRAINING_EITHER + ")s?([^\\p{L}-]|$))|((^|[^\\p{L}-])(" + DOCUMENTATION_TRAINING_EITHER + "))");
	private static final String DOCUMENTATION_TUTORIAL_EITHER = "tutorial|example|(guided|quick)[-_]?tour|getting[-_]?started";
	private static final Pattern DOCUMENTATION_TUTORIAL = Pattern.compile("(?i)((^|[^\\p{L}-])(demo|tour)s?([^\\p{L}-]|$))|((" + DOCUMENTATION_TUTORIAL_EITHER + ")s?([^\\p{L}-]|$))|((^|[^\\p{L}-])(" + DOCUMENTATION_TUTORIAL_EITHER + "))");

	private static final String DOCUMENTATION_EITHER = "vignette|manual|documentation|how[-_]?to|introduction|instruction|users?[-_]?guide|beginners?[-_]?guide|start[-_]?guide|quick[-_]?(start|guide)";
	private static final Pattern DOCUMENTATION = Pattern.compile("(?i)((^|[^\\p{L}-])(usage|guide|how|use)s?([^\\p{L}-]|$))|((help|doc|intro|" + DOCUMENTATION_EITHER + ")s?([^\\p{L}-]|$))|((^|[^\\p{L}-])(" + DOCUMENTATION_EITHER + "))");
	private static final Pattern DOCUMENTATION_WIKI = Pattern.compile("(?i)^(https?://)?(www\\.)?(github\\.com/+[^/]+/+[^/]+/+wiki|sourceforge\\.net/+p/+[^/]+/wiki|sourceforge\\.net/+p/+[^/]+/+home|code\\.google\\.com/+(archive/+)?p/+[^/]+/+wikis?|bitbucket\\.org/+[^/]+/+[^/]+/+wiki)([^\\p{L}]|$)");
	private static final Pattern DOCUMENTATION_EXT = Pattern.compile("(?i)\\.(pdf|ps|doc|docx|ppt|pptx)([^\\p{L}-]|$)");

	private static final Pattern SCHEMA_START = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*://");
	private static final Pattern KNOWN_SCHEMA_START = Pattern.compile("(?i)^(http|https|ftp)://");

	private static final Pattern FIND_NAME_NOT_ALPHANUM = Pattern.compile("[^\\p{L}\\p{N}]");
	private static final Pattern FIND_NAME_CAMEL = Pattern.compile("(\\p{Ll})(\\p{Lu})");
	private static final Pattern FIND_NAME_TO_NUMBER = Pattern.compile("(\\p{L})(\\p{N})");
	private static final Pattern FIND_NAME_FROM_NUMBER = Pattern.compile("(\\p{N})(\\p{L})");
	private static final Pattern FIND_NAME_NUMBER = Pattern.compile("\\p{N}");

	private static final Pattern CONTENT_TYPE_HTML = Pattern.compile("(?i)/(html|xhtml|xml)");

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

	// TODO move BIOTOOLS_SCHEMA stuff to org.edamontology.edammap.core.input.json
	private static final int BIOTOOLS_SCHEMA_NAME_MIN = 1;
	private static final int BIOTOOLS_SCHEMA_NAME_MAX = 100;
	private static final String BIOTOOLS_SCHEMA_NAME_CHARS = " A-Za-z0-9+.,\\-_:;()";
	private static final Pattern BIOTOOLS_SCHEMA_NAME_PATTERN = Pattern.compile("^[" + BIOTOOLS_SCHEMA_NAME_CHARS + "]*$");
	private static final Pattern BIOTOOLS_SCHEMA_NAME_INVALID_CHAR = Pattern.compile("[^" + BIOTOOLS_SCHEMA_NAME_CHARS + "]");
	private static final String[][] BIOTOOLS_SCHEMA_NAME_REPLACEMENTS = {
			{ "\u2010", "-" },
			{ "&", " and " },
			{ "@", "a" },
			{ "α", "a" }, { "β", "b" }, { "μ", "u" }, { "µ", "u" },
			{ "²", "2" }
	};
	private static final Pattern BIOTOOLS_SCHEMA_NAME_APOSTROPHE_QUOTATION_SPACE = Pattern.compile("([\\p{L}\\p{N}])['\"\\u0060\\u00B4\\u2018\\u2019\\u02BC\\u201B\\u0091\\u0092\\u00AB\\u00BB\\u201A\\u201C\\u201D\\u201E\\u201F\\u2039\\u203A\\u2E42]+([\\p{L}\\p{N}])");
	private static final int BIOTOOLS_SCHEMA_DESCRIPTION_MIN = 10;
	private static final int BIOTOOLS_SCHEMA_DESCRIPTION_MAX = 1000;
	// TODO unescaped dot metacharacter https://github.com/bio-tools/biotoolsSchema/issues/124
	private static final Pattern BIOTOOLS_SCHEMA_URL_PATTERN = Pattern.compile("^https?://[^\\s/$.?#].[^\\s]*$");
	private static final Pattern BIOTOOLS_SCHEMA_URLFTP_PATTERN = Pattern.compile("^(https?|s?ftp)://[^\\s/$.?#].[^\\s]*$");
	private static final Pattern BIOTOOLS_SCHEMA_PMID_PATTERN = Pattern.compile("^[1-9][0-9]{0,8}$");
	private static final Pattern BIOTOOLS_SCHEMA_PMCID_PATTERN = Pattern.compile("^(PMC)[1-9][0-9]{0,8}$");
	// TODO too restrictive https://github.com/bio-tools/biotoolsSchema/issues/8
	private static final Pattern BIOTOOLS_SCHEMA_DOI_PATTERN = Pattern.compile("^10.[0-9]{4,9}[A-Za-z0-9:;)(_/.-]+$");
	private static final int BIOTOOLS_SCHEMA_CREDIT_NAME_MIN = 1;
	private static final int BIOTOOLS_SCHEMA_CREDIT_NAME_MAX = 100;
	// TODO too restrictive (last character can be 'X') https://github.com/bio-tools/biotoolsSchema/issues/142
	private static final Pattern BIOTOOLS_SCHEMA_CREDIT_ORCIDID_PATTERN = Pattern.compile("^https?://orcid.org/[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}$");
	private static final Pattern BIOTOOLS_SCHEMA_CREDIT_EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9_]+([-+.'][A-Za-z0-9_]+)*@[A-Za-z0-9_]+([-.][A-Za-z0-9_]+)*\\.[A-Za-z0-9_]+([-.][A-Za-z0-9_]+)*$");

	private static final int BIOTOOLS_DESCRIPTION_MAX_LENGTH = BIOTOOLS_SCHEMA_DESCRIPTION_MAX;
	private static final int BIOTOOLS_DESCRIPTION_MIN_LENGTH = 32;
	private static final int BIOTOOLS_DESCRIPTION_LONG_LENGTH = 160;
	private static final int BIOTOOLS_DESCRIPTION_MINMIN_LENGTH = 24;
	private static final int BIOTOOLS_DESCRIPTION_MESSAGE_MAX_LENGTH = 500;

	private static final Pattern WHITESPACE = Pattern.compile("[\\p{Z}\\p{Cc}\\p{Cf}]+");
	private static final Pattern PUNCTUATION_NUMBERS = Pattern.compile("[\\p{P}\\p{S}\\p{N}]+");
	private static final Pattern NAME_SEPARATOR = Pattern.compile("[ \\u002D\\u2010]+");
	private static final Pattern PERIOD = Pattern.compile("[.]");
	private static final Pattern UPPERCASE = Pattern.compile("^\\{Lu}+$");

	static String prependHttp(String url) {
		if (!SCHEMA_START.matcher(url).find()) {
			return "http://" + url;
		} else {
			return url;
		}
	}

	static String trimUrl(String url) {
		if (url == null) return "";
		url = LINK_COMPARE_INDEX.matcher(LINK_COMPARE_END.matcher(LINK_COMPARE_START.matcher(url).replaceFirst("")).replaceFirst("")).replaceFirst("");
		int slash = url.indexOf('/');
		if (slash < 0) {
			return url.toUpperCase(Locale.ROOT);
		} else {
			return url.substring(0, slash).toUpperCase(Locale.ROOT) + url.substring(slash);
		}
	}

	// TODO meshQuery
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
		if (toolTitle.isEmpty()) {
			return false;
		}
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
		if (toolTitle.isEmpty()) {
			return false;
		}
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

	private static boolean isAcronym(String acronym, String phrase, boolean allWords) {
		acronym = acronym.trim();
		phrase = phrase.trim().replaceAll("-", " ");
		boolean patternValid = false;
		String acronymPattern = "(?i)";
		if (allWords) {
			acronymPattern += "^";
		} else {
			acronymPattern += "(^| )";
		}
		if (phrase.indexOf(" ") > -1 && acronym.indexOf(" ") < 0) {
			for (int i = 0; i < acronym.length(); ++i) {
				String c = String.valueOf(acronym.charAt(i));
				if (USE_IN_PATTERN.matcher(c).matches()) {
					if (patternValid) {
						if (allWords) {
							acronymPattern += "[^ ]* *";
						} else {
							acronymPattern += "([^ ]*|.* )";
						}
					}
					patternValid = true;
					acronymPattern += "[^ \\p{L}\\p{N}]*(\\p{L}+(?=\\p{Lu}))?" + c;
				}
			}
		}
		if (allWords) {
			acronymPattern += "[^ ]*$";
		}
		if (!patternValid) {
			return false;
		}
		Matcher acronymMatcher = Pattern.compile(acronymPattern).matcher(phrase);
		if (acronymMatcher.find()) {
			if (phrase.substring(acronymMatcher.start(), acronymMatcher.end()).trim().indexOf(" ") > -1) {
				return true;
			} else {
				if (phrase.substring(acronymMatcher.end() - 1, acronymMatcher.end()).equals(" ")) {
					if (acronymMatcher.end() - 2 >= 0) {
						return isAcronym(acronym, phrase.substring(0, acronymMatcher.end() - 2) + phrase.substring(acronymMatcher.end() - 1), allWords);
					} else {
						return false;
					}
				} else {
					return isAcronym(acronym, phrase.substring(0, acronymMatcher.end() - 1) + phrase.substring(acronymMatcher.end()), allWords);
				}
			}
		} else {
			return false;
		}
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
						if (isAcronym(beforeExtracted.get(beforeExtracted.size() - 1), inside, false)) {
							acronyms.add(index + beforeExtracted.size() - 1);
						}
					} else if (!inside.contains(" ") && beforeExtracted.size() > 1 && insideExtracted.size() > 0) {
						if (isAcronym(inside, before, false)) {
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
			String toolTitle, String toolTitlePruned, String toolTitleAcronym) {
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

				if (!found && isAcronym(fromLinkProcessed, key, true)) {
					found = true;
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

			if (toolTitle != null && !toolTitle.isEmpty() && toolTitlePruned != null && !toolTitlePruned.isEmpty()) {
				toolTitleMatch(toolTitle, preProcessor, matchedKeys, linkTwoPart, linkProcessed, fromLink, link, links);
			}
			if (toolTitleAcronym != null && !toolTitleAcronym.isEmpty()) {
				toolTitleMatch(toolTitleAcronym, preProcessor, matchedKeys, linkTwoPart, linkProcessed, fromLink, link, links);
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

	private static List<String> breakLinks(List<String> links, List<String> allLinks) {
		for (int i = 0; i < links.size(); ++i) {
			String link = links.get(i);
			String linkStart = "";
			Matcher trimStart = LINK_COMPARE_START.matcher(link);
			if (trimStart.find()) {
				linkStart = link.substring(0, trimStart.end());
				link = link.substring(trimStart.end());
			}
			int linkMax = 0;
			int schemaStart = 0;
			int schemaEnd = 0;
			for (String otherLink : allLinks) {
				if (!link.equals(otherLink) && link.startsWith(otherLink) && otherLink.length() > linkMax) {
					String rest = link.substring(otherLink.length());
					Matcher schemaMatcher = LINK_COMPARE_SCHEMA.matcher(rest);
					if (schemaMatcher.find()) {
						linkMax = otherLink.length();
						schemaStart = schemaMatcher.start();
						schemaEnd = schemaMatcher.end();
					}
				}
			}
			if (linkMax > 0) {
				links.set(i, linkStart + link.substring(0, linkMax));
				if (linkMax + schemaEnd < link.length()) {
					links.add(i + 1, link.substring(linkMax + schemaStart));
				}
			} else {
				Matcher schemaMatcher = LINK_COMPARE_SCHEMA.matcher(link);
				if (schemaMatcher.find()) {
					links.set(i, linkStart + link.substring(0, schemaMatcher.start()));
					if (schemaMatcher.end() < link.length()) {
						links.add(i + 1, link.substring(schemaMatcher.start()));
					}
				}
			}
		}
		return links;
	}

	private static List<String> makeFixLinks(List<String> links) {
		for (int i = 0; i < links.size(); ++i) {
			String link = links.get(i);
			Matcher fixLink = FIX_LINK.matcher(link);
			if (fixLink.find()) {
				links.add(++i, link.substring(0, fixLink.start()));
				continue;
			}
			Matcher fixLinkKeep1 = FIX_LINK_KEEP1.matcher(link);
			if (fixLinkKeep1.find()) {
				links.add(++i, link.substring(0, fixLinkKeep1.start()) + fixLinkKeep1.group(1));
				continue;
			}
			Matcher fixLinkKeep2 = FIX_LINK_KEEP2.matcher(link);
			if (fixLinkKeep2.find()) {
				links.add(++i, link.substring(0, fixLinkKeep2.start()) + fixLinkKeep2.group(1));
				continue;
			}
			Matcher fixLinkEmail1 = FIX_LINK_EMAIL1.matcher(link);
			Matcher fixLinkEmail2 = FIX_LINK_EMAIL2.matcher(link);
			Matcher fixLinkEmail3 = FIX_LINK_EMAIL3.matcher(link);
			if (fixLinkEmail1.find()) {
				links.add(++i, link.substring(0, fixLinkEmail1.start()));
			}
			if (fixLinkEmail2.find()) {
				links.add(++i, link.substring(0, fixLinkEmail2.start()));
			}
			if (fixLinkEmail3.find()) {
				links.add(++i, link.substring(0, fixLinkEmail3.start()));
			}
		}
		for (int i = 0; i < links.size(); ++i) {
			String link = links.get(i);
			Matcher schemaStart = SCHEMA_START.matcher(link);
			if (schemaStart.find() && !KNOWN_SCHEMA_START.matcher(link).find()) {
				links.add(++i, "http://" + link.substring(schemaStart.end()));
			}
		}
		return links;
	}

	private static BiotoolsLink<DocumentationType> getDocumentationLink(String link) {
		if (DOCUMENTATION_API.matcher(link).find()) {
			return new BiotoolsLink<DocumentationType>(link, DocumentationType.API_DOCUMENTATION);
		} else if (DOCUMENTATION_TRAINING.matcher(link).find()) {
			return new BiotoolsLink<DocumentationType>(link, DocumentationType.TRAINING_MATERIAL);
		} else if (DOCUMENTATION_TUTORIAL.matcher(link).find()) {
			return new BiotoolsLink<DocumentationType>(link, DocumentationType.TUTORIAL);
		} else if (DOCUMENTATION_INSTALL.matcher(link).find()) {
			return new BiotoolsLink<DocumentationType>(link, DocumentationType.INSTALLATION_INSTRUCTIONS);
		} else if (DOCUMENTATION.matcher(link).find()) {
			return new BiotoolsLink<DocumentationType>(link, DocumentationType.MANUAL);
		} else if (DOCUMENTATION_GENERAL.matcher(link).find()) {
			return new BiotoolsLink<DocumentationType>(link, DocumentationType.GENERAL);
		} else if (DOCUMENTATION_CITE.matcher(link).find()) {
			return new BiotoolsLink<DocumentationType>(link, DocumentationType.CITATION_INSTRUCTIONS);
		} else if (DOCUMENTATION_TERMS.matcher(link).find()) {
			return new BiotoolsLink<DocumentationType>(link, DocumentationType.TERMS_OF_USE);
		} else if (DOCUMENTATION_WIKI.matcher(link).find()) {
			return new BiotoolsLink<DocumentationType>(link, DocumentationType.MANUAL);
		} else {
			return null;
		}
	}

	private static void makeBiotoolsLinks(List<String> links, List<BiotoolsLink<LinkType>> linkLinks, List<BiotoolsLink<DownloadType>> downloadLinks, List<BiotoolsLink<DocumentationType>> documentationLinks) {
		for (String link : links) {
			BiotoolsLink<DocumentationType> documentationLink = null;
			if (LINK_REGISTRY.matcher(link).find()) {
				linkLinks.add(new BiotoolsLink<LinkType>(link, LinkType.REGISTRY));
			} else if (DOCUMENTATION_EXT.matcher(link).find()) {
				documentationLink = getDocumentationLink(link);
				if (documentationLink != null) {
					documentationLinks.add(documentationLink);
				} else {
					documentationLinks.add(new BiotoolsLink<DocumentationType>(link, DocumentationType.MANUAL));
				}
			} else if (DOWNLOAD_EXT.matcher(link).find()) {
				downloadLinks.add(new BiotoolsLink<DownloadType>(link, DownloadType.BINARY_PACKAGE));
			} else if (DOWNLOAD_API.matcher(link).find()) {
				downloadLinks.add(new BiotoolsLink<DownloadType>(link, DownloadType.API_SPECIFICATION));
			} else if (DOWNLOAD_CWL.matcher(link).find()) {
				downloadLinks.add(new BiotoolsLink<DownloadType>(link, DownloadType.CWL_FILE));
			} else if (DOWNLOAD_CONTAINER.matcher(link).find()) {
				downloadLinks.add(new BiotoolsLink<DownloadType>(link, DownloadType.CONTAINER_FILE));
			} else if (DOWNLOAD_FTP.matcher(link).find()) {
				downloadLinks.add(new BiotoolsLink<DownloadType>(link, DownloadType.BINARIES));
			} else if (DOWNLOAD_SRC_PKG.matcher(link).find()) {
				downloadLinks.add(new BiotoolsLink<DownloadType>(link, DownloadType.SOURCE_PACKAGE));
			} else if (DOWNLOAD_SRC_CODE.matcher(link).find()) {
				downloadLinks.add(new BiotoolsLink<DownloadType>(link, DownloadType.SOURCE_CODE));
			} else if ((documentationLink = getDocumentationLink(link)) != null) {
				documentationLinks.add(documentationLink);
			} else if (LINK_ISSUES.matcher(link).find()) {
				linkLinks.add(new BiotoolsLink<LinkType>(link, LinkType.ISSUE_TRACKER));
			} else if (LINK_LIST_ADDR.matcher(link).find()) {
				linkLinks.add(new BiotoolsLink<LinkType>(link, LinkType.MAILING_LIST));
			} else if (LINK_REPOSITORY.matcher(link).find()) {
				linkLinks.add(new BiotoolsLink<LinkType>(link, LinkType.REPOSITORY));
			} else if (LINK_LIST_BOTH.matcher(link).find()) {
				linkLinks.add(new BiotoolsLink<LinkType>(link, LinkType.MAILING_LIST));
			} else if (LINK_HELPDESK.matcher(link).find()) {
				linkLinks.add(new BiotoolsLink<LinkType>(link, LinkType.HELPDESK));
			} else if (LINK_SOCIAL.matcher(link).find()) {
				linkLinks.add(new BiotoolsLink<LinkType>(link, LinkType.SOCIAL_MEDIA));
			} else if (DOWNLOAD_PAGE.matcher(link).find()) {
				downloadLinks.add(new BiotoolsLink<DownloadType>(link, DownloadType.DOWNLOADS_PAGE));
			} else {
				linkLinks.add(new BiotoolsLink<LinkType>(link, LinkType.OTHER));
			}
		}
	}

	private static <T> void removeBroken(List<BiotoolsLink<T>> links, Set<BiotoolsLink<?>> broken, Database db, boolean doc, String name) {
		for (Iterator<BiotoolsLink<T>> it = links.iterator(); it.hasNext(); ) {
			BiotoolsLink<T> link = it.next();
			boolean removed = false;
			if (!doc) {
				if (db.getWebpage(link.getUrl()) == null || db.getWebpage(link.getUrl()).isBroken()) {
					broken.add(link);
					it.remove();
					removed = true;
				}
			} else {
				if (db.getDoc(link.getUrl()) == null || db.getDoc(link.getUrl()).isBroken()) {
					broken.add(link);
					it.remove();
					removed = true;
				}
			}
			if (!removed) {
				if (!BIOTOOLS_SCHEMA_URLFTP_PATTERN.matcher(link.getUrl()).matches()) {
					logger.warn("Discarded invalid link url '{}' (for name '{}')", link.getUrl(), name);
					it.remove();
				}
			}
		}
	}

	private static boolean isBroken(String url, Database db) {
		if (db.getWebpage(url) != null && !db.getWebpage(url).isBroken()) {
			return false;
		}
		if (db.getDoc(url) != null && !db.getDoc(url).isBroken()) {
			return false;
		}
		return true;
	}

	private static String chooseHomepage(List<String> links, List<BiotoolsLink<LinkType>> linkLinks, List<BiotoolsLink<DocumentationType>> documentationLinks, Database db) {
		for (Iterator<BiotoolsLink<LinkType>> it =  linkLinks.iterator(); it.hasNext(); ) {
			BiotoolsLink<LinkType> linkLink = it.next();
			if (linkLink.getType() == LinkType.OTHER && BIOTOOLS_SCHEMA_URL_PATTERN.matcher(linkLink.getUrl()).matches()) {
				it.remove();
				return linkLink.getUrl();
			}
		}
		for (Iterator<BiotoolsLink<LinkType>> it =  linkLinks.iterator(); it.hasNext(); ) {
			BiotoolsLink<LinkType> linkLink = it.next();
			if (linkLink.getType() == LinkType.REPOSITORY && BIOTOOLS_SCHEMA_URL_PATTERN.matcher(linkLink.getUrl()).matches()) {
				it.remove();
				return linkLink.getUrl();
			}
		}
		for (Iterator<BiotoolsLink<DocumentationType>> it =  documentationLinks.iterator(); it.hasNext(); ) {
			BiotoolsLink<DocumentationType> documentationLink = it.next();
			if (documentationLink.getType() == DocumentationType.GENERAL && BIOTOOLS_SCHEMA_URL_PATTERN.matcher(documentationLink.getUrl()).matches()) {
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
					&& BIOTOOLS_SCHEMA_URL_PATTERN.matcher(documentationLink.getUrl()).matches()) {
				it.remove();
				return documentationLink.getUrl();
			}
		}
		for (String link : links) {
			link = prependHttp(link);
			if (db.getWebpage(link) != null && !db.getWebpage(link).isBroken() || db.getDoc(link) != null && !db.getDoc(link).isBroken()) {
				if (!DOWNLOAD_EXT.matcher(link).find() && BIOTOOLS_SCHEMA_URL_PATTERN.matcher(link).matches()) {
					return link;
				}
			}
		}
		return null;
	}

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
			descriptionSeparated = descriptionFromTitle(descriptionString, WEBPAGE_TITLE_SEPARATOR);
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

	private static void descriptionsFromWebpage(List<Description> descriptions, String url, Database db, Scrape scrape, int minLength, String name, Boolean doc, PreProcessor preProcessor) {
		boolean hasScrape = scrape.getWebpage(url) != null;
		Webpage webpage = null;
		if (doc == null || !doc) {
			webpage = db.getWebpage(url);
		}
		if (webpage == null && (doc == null || doc)) {
			webpage = db.getDoc(url);
		}
		if (webpage != null && !webpage.isBroken() && CONTENT_TYPE_HTML.matcher(webpage.getContentType()).find()) {
			String titleDescription = License.WHITESPACE.matcher(descriptionFromTitle(webpage.getTitle(), WEBPAGE_TITLE_SEPARATOR)).replaceAll(" ").trim();
			if (titleDescription.length() >= minLength) {
				addDescription(descriptions, titleDescription, 1, hasScrape, preProcessor);
			}
			boolean nameFound = false;
			boolean nameFoundLong = false;
			int sentences = 0;
			int sentencesMinLength = 0;
			for (String sentence : webpage.getContent().split("[\n\r]")) {
				sentence = License.WHITESPACE.matcher(sentence).replaceAll(" ").trim();
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

	private static String pruneToMax(String string, int maxLength) {
		if (string.length() <= maxLength) {
			return string;
		} else if (maxLength < 1) {
			return "";
		} else if (maxLength - 4 < 1) {
			return string.substring(0, maxLength);
		} else {
			return string.substring(0, maxLength - 4) + " ...";
		}
	}

	private static String fillToMin(String string, int minLength) {
		if (string.length() >= minLength) {
			return string;
		} else if (string.length() + 1 == minLength) {
			return string + "+";
		} else {
			return string + " " + String.join("", Collections.nCopies(minLength - string.length() - 1, "+"));
		}
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
				lastDescription.setDescription(pruneToMax(lastDescription.getDescription(), maxLength));
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

	private static List<String> normaliseCreditName(String name) {
		List<String> normalisedName = new ArrayList<>();
		name = PERIOD.matcher(name).replaceAll(". ");
		name = WHITESPACE.matcher(name).replaceAll(" ").trim();
		String[] nameParts = NAME_SEPARATOR.split(name);
		for (int i = 0; i < nameParts.length; ++i) {
			String namePart = nameParts[i];
			namePart = PUNCTUATION_NUMBERS.matcher(namePart).replaceAll("");
			if (namePart.length() <= 1 || UPPERCASE.matcher(namePart).matches() && i < nameParts.length - 1 || namePart.equalsIgnoreCase("dr") || namePart.equalsIgnoreCase("prof")) {
				continue;
			}
			namePart = namePart.toLowerCase(Locale.ROOT);
			namePart = Normalizer.normalize(namePart, Normalizer.Form.NFKD);
			normalisedName.add(namePart);
		}
		return normalisedName;
	}

	private static boolean creditNameEqual(String name1, String name2) {
		if (name1.isEmpty() || name2.isEmpty()) {
			return false;
		}
		List<String> normalisedName1 = normaliseCreditName(name1);
		List<String> normalisedName2 = normaliseCreditName(name2);
		if (normalisedName1.isEmpty() || normalisedName2.isEmpty()) {
			return false;
		}
		if (normalisedName1.size() >= 2 && normalisedName2.size() >= 2) {
			return normalisedName1.get(0).equals(normalisedName2.get(0)) && normalisedName1.get(normalisedName1.size() - 1).equals(normalisedName2.get(normalisedName2.size() - 1));
		} else if (normalisedName1.size() < 2) {
			return normalisedName1.get(0).equals(normalisedName2.get(normalisedName2.size() - 1));
		} else {
			return normalisedName2.get(0).equals(normalisedName1.get(normalisedName1.size() - 1));
		}
	}

	private static boolean creditOrcidEqual(String orcid1, String orcid2) {
		if (orcid1.isEmpty() || orcid2.isEmpty()) {
			return false;
		}
		return trimUrl(orcid1).equals(trimUrl(orcid2));
	}

	private static boolean creditEmailEqual(String email1, String email2) {
		if (email1.isEmpty() || email2.isEmpty()) {
			return false;
		}
		int email1At = email1.indexOf("@");
		if (email1At > -1) {
			int email2At = email2.indexOf("@");
			if (email2At > -1) {
				String email1User = email1.substring(0, email1At);
				String email1Domain = email1.substring(email1At + 1);
				String email2User = email2.substring(0, email2At);
				String email2Domain = email2.substring(email2At + 1);
				if (email1User.isEmpty() || email1Domain.isEmpty() || email2User.isEmpty() || email2Domain.isEmpty()) {
					return false;
				}
				return PERIOD.matcher(email1User).replaceAll("").equalsIgnoreCase(PERIOD.matcher(email2User).replaceAll("")) && email1Domain.equalsIgnoreCase(email2Domain);
			}
		}
		return false;
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

	private static void makeResult(List<Result> results, Publication publication,
			String title, String titleRest,	int toolTitleSize, String toolTitleExtractedOriginal, String toolTitle, String toolTitlePruned, String toolTitleAcronym, List<String> toolTitleOthers, List<String> toolTitleProcessedOthers,
			List<String> hostIgnore, List<String> beforeTier1, List<String> beforeTier2, List<String> beforeTier3, List<String> afterTier1, List<String> afterTier2, List<String> afterTier3,
			PreProcessor preProcessor, Idf idf) {
		String theAbstract = publication.getAbstract().getContent();

		String titleWithoutLinks = preProcessor.removeLinks(title);
		String abstractWithoutLinks = preProcessor.removeLinks(theAbstract);

		String titleRestWithoutLinks = preProcessor.removeLinks(titleRest).trim();
		List<String> titleAbstractSentences = preProcessor.sentences(titleRestWithoutLinks + (titleRestWithoutLinks.isEmpty() ? "" : ". ") + abstractWithoutLinks);

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

		if (toolTitle != null && !toolTitle.isEmpty() && toolTitlePruned != null && !toolTitlePruned.isEmpty()) {
			boolean existing = toolTitleScore(toolTitle, preProcessor, scores, processedToExtracted, false);
			if (!existing && !toolTitlePruned.equals(toolTitle)) {
				toolTitleScore(toolTitlePruned, preProcessor, scores, processedToExtracted, true);
			}
		}
		if (toolTitleAcronym != null && !toolTitleAcronym.isEmpty()) {
			toolTitleScore(toolTitleAcronym, preProcessor, scores, processedToExtracted, false);
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

		List<String> allLinks = new ArrayList<>();
		allLinks.addAll(titleAbstractLinks.stream().map(link -> LINK_COMPARE_START.matcher(link).replaceFirst("")).collect(Collectors.toList()));
		allLinks.addAll(fulltextLinks.stream().map(link -> LINK_COMPARE_START.matcher(link).replaceFirst("")).collect(Collectors.toList()));

		breakLinks(titleAbstractLinks, allLinks);
		breakLinks(fulltextLinks, allLinks);

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

				if (titleAbstractLink.startsWith(fulltextLink)) {
					String rest = titleAbstractLink.substring(fulltextLink.length() - 1);
					if (LINK_COMPARE_REST.matcher(rest).matches()) {
						titleAbstractLinks.set(i, start + fulltextLink);
						it.remove();
						break;
					}
				}
			}
		}

		Map<String, List<String>> linksAbstract = links(titleAbstractLinks, preProcessor, idf, hostIgnore,
			scores.keySet(), processedToExtracted, processed, titleWithoutLinks, abstractWithoutLinks,
			toolTitle, toolTitlePruned, toolTitleAcronym);

		Map<String, List<String>> linksFulltext = links(fulltextLinks, preProcessor, idf, hostIgnore,
			scores.keySet(), processedToExtracted, processed, titleWithoutLinks, abstractWithoutLinks,
			toolTitle, toolTitlePruned, toolTitleAcronym);

		for (Map.Entry<String, List<String>> linkEntry : linksAbstract.entrySet()) {
			double score = scores.get(linkEntry.getKey()) * LINK_MULTIPLIER_ABSTRACT * linkEntry.getValue().size();
			if (score > LINK_MULTIPLIER_ABSTRACT_MINIMUM) {
				scores.put(linkEntry.getKey(), score);
			} else {
				scores.put(linkEntry.getKey(), LINK_MULTIPLIER_ABSTRACT_MINIMUM);
			}
		}

		List<String> fromAbstractLinks = new ArrayList<>();
		boolean genericLinkAugmentation = linksAbstract.isEmpty();
		for (String link : titleAbstractLinks) {
			if (LINK_TWO_PART.matcher(link).matches()) continue;
			boolean present = false;
			for (Map.Entry<String, List<String>> linkEntry : linksAbstract.entrySet()) {
				if (linkEntry.getValue().contains(link) && !fromAbstractLinks.contains(linkEntry.getKey())) {
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
						if (!scores.containsKey(fromLinkProcessedString)) {
							fromAbstractLinks.add(fromLinkProcessedString);
							processedToExtracted.put(fromLinkProcessedString, fromLinkExtractedString);
						}
						scores.merge(fromLinkProcessedString, LINK_MULTIPLIER_ABSTRACT_NEW / fromLinkProcessed.size(), (d1, d2) -> d1 * d2);
						List<String> linkAbstract = linksAbstract.get(fromLinkProcessedString);
						if (linkAbstract == null) {
							linkAbstract = new ArrayList<>();
							linksAbstract.put(fromLinkProcessedString, linkAbstract);
						}
						linkAbstract.add(link);
					}
				}
			}
		}
		if (!fromAbstractLinks.isEmpty()) {
			for (String link : fulltextLinks) {
				String fromLink = fromLink(link, preProcessor, idf, hostIgnore);
				if (!fromLink.isEmpty()) {
					List<String> fromLinkProcessed = preProcessor.process(fromLink);
					String fromLinkProcessedString = String.join(" ", fromLinkProcessed);
					if (!fromLinkProcessedString.isEmpty()) {
						for (String fromAbstractLink : fromAbstractLinks) {
							if (fromAbstractLink.equals(fromLinkProcessedString)) {
								List<String> linkFulltext = linksFulltext.get(fromLinkProcessedString);
								if (linkFulltext == null) {
									linkFulltext = new ArrayList<>();
									linksFulltext.put(fromLinkProcessedString, linkFulltext);
								}
								linkFulltext.add(link);
							}
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

		String pmid = publication.getPmid().getContent();
		String pmcid = publication.getPmcid().getContent();
		String doi = publication.getDoi().getContent();
		if (!pmid.isEmpty() && !BIOTOOLS_SCHEMA_PMID_PATTERN.matcher(pmid).matches()) {
			logger.warn("Discarded invalid publication PMID '{}' (PMCID is '{}', DOI is '{}')", pmid, pmcid, doi);
			pmid = "";
		}
		if (!pmcid.isEmpty() && !BIOTOOLS_SCHEMA_PMCID_PATTERN.matcher(pmcid).matches()) {
			logger.warn("Discarded invalid publication PMCID '{}' (PMID is '{}', DOI is '{}')", pmcid, pmid, doi);
			pmcid = "";
		}
		if (!doi.isEmpty() && !BIOTOOLS_SCHEMA_DOI_PATTERN.matcher(doi).matches()) {
			logger.warn("Discarded invalid publication DOI '{}' (PMID is '{}', PMCID is '{}')", doi, pmid, pmcid);
			doi = "";
		}

		Result result = new Result(new PublicationIds(pmid, pmcid, doi, publication.getPmid().getUrl(), publication.getPmcid().getUrl(), publication.getDoi().getUrl()));

		result.addTitle(title);
		result.addToolTitleOthers(toolTitleOthers != null ? toolTitleOthers : new ArrayList<>());
		result.addToolTitleExtractedOriginal(toolTitleExtractedOriginal != null ? toolTitleExtractedOriginal : "");
		result.addToolTitle(toolTitle != null ? toolTitle : "");
		result.addToolTitlePruned(toolTitlePruned != null ? toolTitlePruned : "");
		result.addToolTitleAcronym(toolTitleAcronym != null ? toolTitleAcronym : "");
		result.addAbstractSentences(preProcessor.sentences(publication.getAbstract().getContent()));
		result.addOa(publication.isOA());
		result.addJournalTitle(publication.getJournalTitle());
		result.addPubDate(publication.getPubDate());
		result.addPubDateHuman(publication.getPubDateHuman());
		result.addCitationsCount(publication.getCitationsCount());
		result.addCitationsTimestamp(publication.getCitationsTimestamp());
		result.addCitationsTimestampHuman(publication.getCitationsTimestampHuman());

		List<CorrespAuthor> correspAuthor = publication.getCorrespAuthor();
		for (CorrespAuthor ca : correspAuthor) {
			String name = License.WHITESPACE.matcher(ca.getName()).replaceAll(" ").trim();
			if (name.length() < BIOTOOLS_SCHEMA_CREDIT_NAME_MIN && !name.isEmpty()) {
				name = fillToMin(name, BIOTOOLS_SCHEMA_CREDIT_NAME_MIN);
				logger.warn("Credit name filled to min from '{}' to '{}' (from pub {})", ca.getName(), name, result.getPublicationIds().get(0).toString());
			}
			if (name.length() > BIOTOOLS_SCHEMA_CREDIT_NAME_MAX) {
				name = pruneToMax(name, BIOTOOLS_SCHEMA_CREDIT_NAME_MAX);
				logger.warn("Credit name pruned to max from '{}' to '{}' (from pub {})", ca.getName(), name, result.getPublicationIds().get(0).toString());
			}
			ca.setName(name);
			if (!ca.getOrcid().isEmpty() && !BIOTOOLS_SCHEMA_CREDIT_ORCIDID_PATTERN.matcher(ca.getOrcid()).matches()) {
				logger.warn("Discarded invalid credit orcidid '{}' (from pub {})", ca.getOrcid(), result.getPublicationIds().get(0).toString());
				ca.setOrcid("");
			}
			if (!ca.getEmail().isEmpty() && !BIOTOOLS_SCHEMA_CREDIT_EMAIL_PATTERN.matcher(ca.getEmail()).matches()) {
				String email = ca.getEmail();
				if (email.charAt(email.length() - 1) == '.') {
					email = email.substring(0, email.length() - 1);
				}
				boolean valid = false;
				for (String emailPart : email.split(" ")) {
					if (BIOTOOLS_SCHEMA_CREDIT_EMAIL_PATTERN.matcher(emailPart).matches()) {
						email = emailPart;
						valid = true;
						break;
					}
				}
				if (valid) {
					logger.warn("Credit email changed from '{}' to '{}' (from pub {})", ca.getEmail(), email, result.getPublicationIds().get(0).toString());
					ca.setEmail(email);
				} else {
					logger.warn("Discarded invalid credit email '{}' (from pub {})", ca.getEmail(), result.getPublicationIds().get(0).toString());
					ca.setEmail("");
				}
			}
			if (!ca.getUri().isEmpty() && !BIOTOOLS_SCHEMA_URL_PATTERN.matcher(ca.getUri()).matches()) {
				logger.warn("Discarded invalid credit url '{}' (from pub {})", ca.getUri(), result.getPublicationIds().get(0).toString());
				ca.setUri("");
			}
		}
		for (Iterator<CorrespAuthor> it = correspAuthor.iterator(); it.hasNext(); ) {
			CorrespAuthor ca = it.next();
			if (ca.getName().isEmpty() && ca.getEmail().isEmpty() && ca.getUri().isEmpty()) {
				logger.warn("Discarded empty credit (from pub {})", result.getPublicationIds().get(0).toString());
				it.remove();
			}
		}
		result.addCorrespAuthor(correspAuthor);

		Iterator<Map.Entry<String, Double>> sortedScoresIterator = sortedScores.entrySet().iterator();
		double topScore = 0;
		for (int i = 0; i < SUGGESTION_LIMIT && sortedScoresIterator.hasNext(); ++i) {
			Map.Entry<String, Double> entry = sortedScoresIterator.next();
			if (toolTitleProcessedOthers != null && toolTitleProcessedOthers.contains(entry.getKey())) {
				--i;
				continue;
			}
			if (i == 0) {
				topScore = entry.getValue();
			} else {
				if (entry.getValue() * TOP_SCORE_LIMIT < topScore) break;
			}
			Suggestion suggestion = new Suggestion();
			suggestion.setScore(entry.getValue());
			String suggestionExtractedOriginal = processedToExtracted.get(entry.getKey());
			String suggestionExtracted = suggestionExtractedOriginal;
			if (!BIOTOOLS_SCHEMA_NAME_PATTERN.matcher(suggestionExtracted).matches() && !suggestionExtracted.isEmpty()) {
				for (int j = 0; j < BIOTOOLS_SCHEMA_NAME_REPLACEMENTS.length; ++j) {
					suggestionExtracted = suggestionExtracted.replace(BIOTOOLS_SCHEMA_NAME_REPLACEMENTS[j][0], BIOTOOLS_SCHEMA_NAME_REPLACEMENTS[j][1]);
				}
				suggestionExtracted = BIOTOOLS_SCHEMA_NAME_APOSTROPHE_QUOTATION_SPACE.matcher(suggestionExtracted).replaceAll("$1 $2");
				suggestionExtracted = Normalizer.normalize(suggestionExtracted, Normalizer.Form.NFKD);
				suggestionExtracted = License.WHITESPACE.matcher(suggestionExtracted).replaceAll(" ").trim();
				suggestionExtracted = BIOTOOLS_SCHEMA_NAME_INVALID_CHAR.matcher(suggestionExtracted).replaceAll("");
				logger.info("Name changed from '{}' to '{}' (from pub {})", suggestionExtractedOriginal, suggestionExtracted, result.getPublicationIds().get(0).toString());
			}
			if (suggestionExtracted.length() < BIOTOOLS_SCHEMA_NAME_MIN) {
				suggestionExtracted = fillToMin(suggestionExtracted, BIOTOOLS_SCHEMA_NAME_MIN);
				logger.info("Name filled to min from '{}' to '{}' (from pub {})", suggestionExtractedOriginal, suggestionExtracted, result.getPublicationIds().get(0).toString());
			}
			if (suggestionExtracted.length() > BIOTOOLS_SCHEMA_NAME_MAX) {
				suggestionExtracted = pruneToMax(suggestionExtracted, BIOTOOLS_SCHEMA_NAME_MAX);
				logger.info("Name pruned to max from '{}' to '{}' (from pub {})", suggestionExtractedOriginal, suggestionExtracted, result.getPublicationIds().get(0).toString());
			}
			suggestion.setExtracted(suggestionExtracted);
			if (suggestionExtracted.equals(suggestionExtractedOriginal)) {
				suggestion.setProcessed(entry.getKey());
			} else {
				suggestion.setProcessed(String.join(" ", preProcessor.process(suggestionExtracted)));
				suggestion.setOriginal(suggestionExtractedOriginal);
			}
			if (linksAbstract.get(entry.getKey()) != null) {
				suggestion.setLinksAbstract(linksAbstract.get(entry.getKey()));
			}
			if (linksFulltext.get(entry.getKey()) != null) {
				suggestion.setLinksFulltext(linksFulltext.get(entry.getKey()));
			}
			suggestion.setFromAbstractLink(fromAbstractLinks.contains(entry.getKey()));
			result.addSuggestion(suggestion);
		}

		result.addLeftoverLinksAbstract(titleAbstractLinks);
		result.addLeftoverLinksFulltext(fulltextLinks);

		List<String> leftoverLinksAbstractCompare = new ArrayList<>();
		TreeSet<Integer> leftoverLinksAbstractRemove = new TreeSet<>();
		for (String leftoverLinkAbstract : result.getLeftoverLinksAbstract().get(0)) {
			leftoverLinksAbstractCompare.add(String.join("", preProcessor.process(leftoverLinkAbstract)));
		}
		List<String> titleAbstractLinksCompare = new ArrayList<>();
		for (String titleAbstractLink : titleAbstractLinks) {
			titleAbstractLinksCompare.add(String.join("", preProcessor.process(titleAbstractLink)));
		}
		for (Suggestion suggestion : result.getSuggestions()) {
			String suggestionCompare = BIOTOOLS_PROCESSED_VERSION_TRIM.matcher(suggestion.getProcessed()).replaceFirst("").replaceAll(" ", "");
			if (suggestionCompare.length() < 2) continue;
			for (int i = 0; i < result.getLeftoverLinksAbstract().get(0).size(); ++i) {
				String leftoverLinkAbstractCompare = leftoverLinksAbstractCompare.get(i);
				if (leftoverLinkAbstractCompare.contains(suggestionCompare)) {
					suggestion.addLinkAbstract(result.getLeftoverLinksAbstract().get(0).get(i));
					leftoverLinksAbstractRemove.add(i);
				}
			}
			for (int i = 0; i < titleAbstractLinks.size(); ++i) {
				String link = titleAbstractLinks.get(i);
				if (!suggestion.getLinksAbstract().contains(link) && !result.getLeftoverLinksAbstract().get(0).contains(link)) {
					String linkCompare = titleAbstractLinksCompare.get(i);
					if (linkCompare.contains(suggestionCompare)) {
						suggestion.addLinkAbstract(link);
					}
				}
			}
		}
		for (Iterator<Integer> it = leftoverLinksAbstractRemove.descendingIterator(); it.hasNext(); ) {
			result.getLeftoverLinksAbstract().get(0).remove(it.next().intValue());
		}

		for (Suggestion suggestion : result.getSuggestions()) {
			makeFixLinks(suggestion.getLinksAbstract());
			makeFixLinks(suggestion.getLinksFulltext());
		}

		results.add(result);
	}

	private static void linksMatch(Map<Integer, List<String>> linkMatchMap, String suggestionLink, Suggestion suggestion, List<List<String>> queryLinks) {
		suggestionLink = trimUrl(suggestionLink);
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
					if (LINK_COMPARE_REST.matcher(rest).matches()) {
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

	private static String currentHomepage(ToolInput biotool, Database db) {
		String homepage = biotool.getHomepage();
		if (biotool.getHomepage_status() != 0) {
			homepage += " (homepage_status: " + biotool.getHomepage_status() + ")";
		}
		Webpage webpage = db.getWebpage(biotool.getHomepage());
		if (webpage != null && webpage.isBroken()) {
			homepage += " (broken)";
		}
		return homepage;
	}

	private static boolean linksEqual(String addLink, String addLinkTrimmed, String biotoolsLink, Database db, boolean addLinkDoc, boolean biotoolsLinkDoc) {
		String biotoolsLinkTrimmed = trimUrl(biotoolsLink);
		if (addLinkTrimmed.equals(biotoolsLinkTrimmed)) {
			return true;
		}
		String addLinkFinalTrimmed = null;
		Webpage addLinkWebpage = null;
		if (addLinkDoc) {
			addLinkWebpage = db.getDoc(addLink);
		} else {
			addLinkWebpage = db.getWebpage(addLink);
		}
		if (addLinkWebpage != null) {
			addLinkFinalTrimmed = trimUrl(addLinkWebpage.getFinalUrl());
		}
		String biotoolsLinkFinalTrimmed = null;
		Webpage biotoolsLinkWebpage = null;
		if (biotoolsLinkDoc) {
			biotoolsLinkWebpage = db.getDoc(biotoolsLink);
		} else {
			biotoolsLinkWebpage = db.getWebpage(biotoolsLink);
		}
		if (biotoolsLinkWebpage != null) {
			biotoolsLinkFinalTrimmed = trimUrl(biotoolsLinkWebpage.getFinalUrl());
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

	private static Diff makeDiff(double scoreScore2, Set<Integer> possiblyRelated, List<ToolInput> biotools, int existing, List<PublicationIds> publications, Set<PublicationIds> addPublications, String modifyName, String homepage, Set<BiotoolsLink<LinkType>> links, Set<BiotoolsLink<DownloadType>> downloads, Set<BiotoolsLink<DocumentationType>> documentations, Provenance license, List<Provenance> languages, List<CorrespAuthor> credits, Database db) {
		Diff diff = new Diff();

		diff.setScoreScore2(scoreScore2);
		diff.setPossiblyRelated(possiblyRelated);

		ToolInput biotool = biotools.get(existing);

		diff.setExisting(existing);
		diff.setAddPublications(addPublications);
		diff.setModifyName(modifyName);

		for (PublicationIds publicationIds : publications) {
			if (biotool.getPublication() != null) {
				for (org.edamontology.edammap.core.input.json.Publication pubIds : biotool.getPublication()) {
					if ((!publicationIds.getPmid().isEmpty() && pubIds.getPmid() != null && pubIds.getPmid().trim().equals(publicationIds.getPmid())
							|| !publicationIds.getPmcid().isEmpty() && pubIds.getPmcid() != null && pubIds.getPmcid().trim().equals(publicationIds.getPmcid())
							|| !publicationIds.getDoi().isEmpty() && pubIds.getDoi() != null && PubFetcher.normaliseDoi(pubIds.getDoi().trim()).equals(publicationIds.getDoi()))
						&& (!publicationIds.getPmid().isEmpty() && pubIds.getPmid() != null && !pubIds.getPmid().isEmpty() && !pubIds.getPmid().trim().equals(publicationIds.getPmid())
							|| !publicationIds.getPmcid().isEmpty() && pubIds.getPmcid() != null && !pubIds.getPmcid().isEmpty() && !pubIds.getPmcid().trim().equals(publicationIds.getPmcid())
							|| !publicationIds.getDoi().isEmpty() && pubIds.getDoi() != null && !pubIds.getDoi().isEmpty() && !PubFetcher.normaliseDoi(pubIds.getDoi().trim()).equals(publicationIds.getDoi()))) {
						diff.addModifyPublication(publicationIds);
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
			String homepageTrimmed = trimUrl(homepage);
			if (!linksEqual(homepage, homepageTrimmed, biotool.getHomepage(), db, false, false)
					&& !linksEqual(homepage, homepageTrimmed, biotool.getHomepage(), db, true, false)) {
				Webpage webpage = db.getWebpage(biotool.getHomepage());
				List<String> homepageLinks = new ArrayList<>();
				homepageLinks.add(homepage);
				List<BiotoolsLink<LinkType>> linkLinks = new ArrayList<>();
				List<BiotoolsLink<DownloadType>> downloadLinks = new ArrayList<>();
				List<BiotoolsLink<DocumentationType>> documentationLinks = new ArrayList<>();
				makeBiotoolsLinks(homepageLinks, linkLinks, downloadLinks, documentationLinks);
				if (biotool.getHomepage_status() != 0 && webpage != null && webpage.isBroken()) {
					diff.setModifyHomepage(homepage);
				} else if (!linkLinks.isEmpty() && linkLinks.get(0).getType() == LinkType.OTHER) {
					diff.setModifyHomepage(homepage);
					List<String> biotoolsHomepageLinks = new ArrayList<>();
					biotoolsHomepageLinks.add(biotool.getHomepage());
					List<BiotoolsLink<LinkType>> biotoolsLinkLinks = new ArrayList<>();
					List<BiotoolsLink<DownloadType>> biotoolsDownloadLinks = new ArrayList<>();
					List<BiotoolsLink<DocumentationType>> biotoolsDocumentationLinks = new ArrayList<>();
					makeBiotoolsLinks(biotoolsHomepageLinks, biotoolsLinkLinks, biotoolsDownloadLinks, biotoolsDocumentationLinks);
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
					if (creditBiotools.getName() != null && creditNameEqual(credit.getName(), creditBiotools.getName())
							|| creditBiotools.getOrcidid() != null && creditOrcidEqual(credit.getOrcid(), creditBiotools.getOrcidid())
							|| creditBiotools.getEmail() != null && creditEmailEqual(credit.getEmail(), creditBiotools.getEmail())) {
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

	private static void addDiff(List<Diff> diffs, Diff diff) {
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

	private static void run(PreProcessorArgs preProcessorArgs, String queryIdf, String database, List<String> pubFile, String queryPath, QueryType queryType, String webFile, String docFile, String outputDir, FetcherArgs fetcherArgs, boolean pass1) throws IOException, ParseException {
		Path outputPath = null;
		if (outputDir != null) {
			outputPath = PubFetcher.outputPath(outputDir, true, false);
			Files.createDirectory(outputPath);
		}

		List<String> hostIgnore = PubFetcher.getResource(PubMedApps.class, "resources/host_ignore.txt");
		List<String> beforeTier1 = PubFetcher.getResource(PubMedApps.class, "resources/before_tier1.txt");
		List<String> beforeTier2 = PubFetcher.getResource(PubMedApps.class, "resources/before_tier2.txt");
		List<String> beforeTier3 = PubFetcher.getResource(PubMedApps.class, "resources/before_tier3.txt");
		List<String> afterTier1 = PubFetcher.getResource(PubMedApps.class, "resources/after_tier1.txt");
		List<String> afterTier2 = PubFetcher.getResource(PubMedApps.class, "resources/after_tier2.txt");
		List<String> afterTier3 = PubFetcher.getResource(PubMedApps.class, "resources/after_tier3.txt");

		List<String> license = PubFetcher.getResource(PubMedApps.class, "resources/license.txt");
		List<License> licenses = license.stream().map(l -> new License(l)).collect(Collectors.toList());
		List<String> language = PubFetcher.getResource(PubMedApps.class, "resources/language.txt");
		List<String> languageKeywords = PubFetcher.getResource(PubMedApps.class, "resources/language_keywords.txt");
		List<Language> languages = language.stream().map(l -> new Language(l)).collect(Collectors.toList());

		Scrape scrape = new Scrape(fetcherArgs.getPrivateArgs().getJournalsYaml(), fetcherArgs.getPrivateArgs().getWebpagesYaml());

		PreProcessor preProcessor = new PreProcessor(preProcessorArgs);

		Idf idf = new Idf(queryIdf);

		@SuppressWarnings("unchecked")
		List<ToolInput> biotools = (List<ToolInput>) Json.load(queryPath, queryType, fetcherArgs.getTimeout(), fetcherArgs.getPrivateArgs().getUserAgent());

		Set<Publication> publications = new LinkedHashSet<>(PubFetcher.getPublications(database, pubFile, PubMedApps.class.getSimpleName()));

		List<Result> results = new ArrayList<>();

		int publicationIndex = 0;
		for (Publication publication : publications) {
			++publicationIndex;
			double percentage = publicationIndex / (double) publications.size() * 100;
			percentage = Math.round(percentage * 10) / 10.0;
			System.err.print("\rMaking results: " + percentage + "%"); // TODO use progress bar from PubFetcher

			List<String> toolTitleExtractedOriginal = new ArrayList<>();
			List<String> toolTitle = new ArrayList<>();
			List<String> toolTitlePruned = new ArrayList<>();
			String toolTitleAcronym = null;
			long toolTitleWordsTotal = 0;

			String title = publication.getTitle().getContent();
			String titleRest = title;

			int from = 0;
			Matcher matcher = TITLE_SEPARATOR.matcher(title);

			while (from < title.length() && matcher.find(from)) {
				String currentToolTitleString = title.substring(from, matcher.start()).trim();
				if (TOOL_TITLE_INVALID.matcher(currentToolTitleString).matches()) {
					from = matcher.end();
					continue;
				}

				List<String> currentToolTitleExtractedOriginal = new ArrayList<>();
				List<String> currentToolTitle = new ArrayList<>();
				List<String> currentToolTitlePruned = new ArrayList<>();
				String currentToolTitleAcronym = null;
				long currentToolTitleWordsTotal = 0;

				Integer firstAcronymIndex = firstAcronymIndex(currentToolTitleString, preProcessor);
				if (!TOOL_TITLE_SEPARATOR.matcher(currentToolTitleString).find() || firstAcronymIndex != null || currentToolTitleString.split(" ").length > TOOL_TITLE_SEPARATOR_MAX_WORDS) {
					List<String> currentToolTitleExtracted = preProcessor.extract(currentToolTitleString);
					currentToolTitleExtractedOriginal.add(String.join(" ", currentToolTitleExtracted));
					preProcessor.process(currentToolTitleString, currentToolTitleExtracted); // align indexes
					if (firstAcronymIndex != null) {
						currentToolTitleAcronym = currentToolTitleExtracted.remove(firstAcronymIndex.intValue());
					}
					currentToolTitle.add(String.join(" ", currentToolTitleExtracted));
					currentToolTitlePruned.add(toolTitlePrune(currentToolTitleExtracted));
					currentToolTitleWordsTotal += currentToolTitleExtracted.size();
				} else {
					for (String currentToolTitleStringPart : TOOL_TITLE_SEPARATOR_ALL.split(currentToolTitleString)) {
						List<String> currentToolTitleExtracted = preProcessor.extract(currentToolTitleStringPart);
						currentToolTitleExtractedOriginal.add(String.join(" ", currentToolTitleExtracted));
						preProcessor.process(currentToolTitleStringPart, currentToolTitleExtracted); // align indexes
						currentToolTitle.add(String.join(" ", currentToolTitleExtracted));
						currentToolTitlePruned.add(toolTitlePrune(currentToolTitleExtracted));
						currentToolTitleWordsTotal += currentToolTitleExtracted.size();
					}
				}

				if (currentToolTitleWordsTotal < toolTitleWordsTotal || toolTitle.isEmpty()) {
					toolTitleExtractedOriginal = currentToolTitleExtractedOriginal;
					toolTitle = currentToolTitle;
					toolTitlePruned = currentToolTitlePruned;
					toolTitleAcronym = currentToolTitleAcronym;
					toolTitleWordsTotal = currentToolTitleWordsTotal;
					titleRest = title.substring(0, from).trim() + " " + title.substring(matcher.start()).trim();
				}

				from = matcher.end();
			}

			if (from == 0) {
				List<String> toolTitleExtractedStandalone = preProcessor.extract(title);
				String toolTitleExtractedOriginalStandalone = String.join(" ", toolTitleExtractedStandalone);
				preProcessor.process(title, toolTitleExtractedStandalone); // align indexes
				String toolTitlePrunedStandalone = toolTitlePrune(toolTitleExtractedStandalone);
				if (toolTitlePrunedStandalone.length() <= TOOL_TITLE_STANDALONE_MAX_CHARS) {
					toolTitleExtractedOriginal.add(toolTitleExtractedOriginalStandalone);
					toolTitle.add(String.join(" ", toolTitleExtractedStandalone));
					toolTitlePruned.add(toolTitlePrunedStandalone);
					titleRest = "";
				}
			}

			if (!toolTitle.isEmpty()) {
				List<String> toolTitleProcessed = new ArrayList<>();
				for (int i = 0; i < toolTitle.size(); ++i) {
					toolTitleProcessed.add(String.join(" ", preProcessor.process(toolTitle.get(i))));
				}
				List<String> toolTitleProcessedDone = new ArrayList<>();
				for (int i = 0; i < toolTitleProcessed.size(); ++i) {
					if (!toolTitleProcessedDone.contains(toolTitleProcessed.get(i))) {
						List<String> toolTitleOthers = new ArrayList<>();
						for (int j = 0; j < toolTitle.size(); ++j) {
							if (!toolTitle.get(i).equals(toolTitle.get(j))) {
								toolTitleOthers.add(toolTitle.get(j));
							}
						}
						List<String> toolTitleProcessedOthers = new ArrayList<>();
						for (int j = 0; j < toolTitleProcessed.size(); ++j) {
							if (!toolTitleProcessed.get(i).equals(toolTitleProcessed.get(j))) {
								toolTitleProcessedOthers.add(toolTitleProcessed.get(j));
							}
						}
						makeResult(results, publication,
							title, titleRest, toolTitle.size(), toolTitleExtractedOriginal.get(i), toolTitle.get(i), toolTitlePruned.get(i), toolTitleAcronym, toolTitleOthers, toolTitleProcessedOthers,
							hostIgnore, beforeTier1, beforeTier2, beforeTier3, afterTier1, afterTier2, afterTier3,
							preProcessor, idf);
						toolTitleProcessedDone.add(toolTitleProcessed.get(i));
					}
				}
			} else {
				makeResult(results, publication,
					title, titleRest, 0, null, null, null, null, null, null,
					hostIgnore, beforeTier1, beforeTier2, beforeTier3, afterTier1, afterTier2, afterTier3,
					preProcessor, idf);
			}
		}

		System.err.println(); // TODO this changes line after the progress bar

		// TODO Removed -89 existing
		logger.info("Removed {} existing", publications.size() - results.size());

		if (pass1) {
			Collections.sort(results);

			List<String> webpages = new ArrayList<>();
			List<String> docs = new ArrayList<>();

			for (Result result : results) {
				for (Suggestion suggestion : result.getSuggestions()) {
					List<BiotoolsLink<LinkType>> linkLinks = new ArrayList<>();
					List<BiotoolsLink<DownloadType>> downloadLinks = new ArrayList<>();
					List<BiotoolsLink<DocumentationType>> documentationLinks = new ArrayList<>();
					makeBiotoolsLinks(suggestion.getLinksAbstract(), linkLinks, downloadLinks, documentationLinks);
					makeBiotoolsLinks(suggestion.getLinksFulltext(), linkLinks, downloadLinks, documentationLinks);
					linkLinks.forEach(link -> webpages.add(link.getUrl()));
					downloadLinks.forEach(link -> webpages.add(link.getUrl()));
					documentationLinks.forEach(link -> docs.add(link.getUrl()));
				}
			}

			// TODO can throw IOException, do in the beginning
			Path webPath = PubFetcher.outputPath(webFile);
			Path docPath = PubFetcher.outputPath(docFile);

			// TODO can throw IOException, maybe open writer in the beginning
			writeLinks(webPath, webpages);
			writeLinks(docPath, docs);

			System.out.println("pmid\tpmcid\tdoi\tscore\tsuggestion_original\tsuggestion\tsuggestion_processed\tlinks_abstract\tlinks_fulltext\tfrom_abstract_link\tother_scores\tother_suggestions_original\tother_suggestions\tother_suggestions_processed\tother_links_abstract\tother_links_fulltext\tleftover_links_abstract\tleftover_links_fulltext\ttitle\ttool_title_multiple\ttool_title_extracted_original\ttool_title\ttool_title_pruned\ttool_title_acronym\toa\tjournal_title\tpub_date\tcitations_count\tcitations_timestamp\tcorresp_author_name\tcorresp_author_orcid\tcorresp_author_email\tcorresp_author_phone\tcorresp_author_uri");
			for (Result result : results) {
				System.out.print(result.getPublicationIds().get(0).getPmid());
				System.out.print("\t");
				System.out.print(result.getPublicationIds().get(0).getPmcid());
				System.out.print("\t");
				System.out.print(result.getPublicationIds().get(0).getDoi());
				System.out.print("\t");
				if (!result.getSuggestions().isEmpty()) {
					System.out.print(result.getSuggestions().get(0).getScore());
				}
				System.out.print("\t");
				if (!result.getSuggestions().isEmpty()) {
					System.out.print(result.getSuggestions().get(0).getOriginal());
				}
				System.out.print("\t");
				if (!result.getSuggestions().isEmpty()) {
					System.out.print(result.getSuggestions().get(0).getExtracted());
				}
				System.out.print("\t");
				if (!result.getSuggestions().isEmpty()) {
					System.out.print(result.getSuggestions().get(0).getProcessed());
				}
				System.out.print("\t");
				if (!result.getSuggestions().isEmpty()) {
					System.out.print(String.join(" | ", result.getSuggestions().get(0).getLinksAbstract()));
				}
				System.out.print("\t");
				if (!result.getSuggestions().isEmpty()) {
					System.out.print(String.join(" | ", result.getSuggestions().get(0).getLinksFulltext()));
				}
				System.out.print("\t");
				System.out.print(result.getSuggestions().stream().map(s -> String.valueOf(s.isFromAbstractLink())).collect(Collectors.joining(" | ")));
				System.out.print("\t");
				System.out.print(result.getSuggestions().stream().skip(1).map(suggestion -> String.format(Locale.ROOT, "%.1f", suggestion.getScore())).collect(Collectors.joining(" | ")));
				System.out.print("\t");
				System.out.print(result.getSuggestions().stream().skip(1).map(suggestion -> suggestion.getOriginal()).collect(Collectors.joining(" | ")));
				System.out.print("\t");
				System.out.print(result.getSuggestions().stream().skip(1).map(suggestion -> suggestion.getExtracted()).collect(Collectors.joining(" | ")));
				System.out.print("\t");
				System.out.print(result.getSuggestions().stream().skip(1).map(suggestion -> suggestion.getProcessed()).collect(Collectors.joining(" | ")));
				System.out.print("\t");
				List<String> otherLinksAbstract = new ArrayList<>();
				boolean otherLinksAbstractEmpty = true;
				for (int i = 1; i < result.getSuggestions().size(); ++i) {
					otherLinksAbstract.add(result.getSuggestions().get(i).getLinksAbstract().toString());
					if (!result.getSuggestions().get(i).getLinksAbstract().isEmpty()) {
						otherLinksAbstractEmpty = false;
					}
				}
				if (!otherLinksAbstractEmpty) {
					System.out.print(String.join(" | ", otherLinksAbstract));
				}
				System.out.print("\t");
				List<String> otherLinksFulltext = new ArrayList<>();
				boolean otherLinksFulltextEmpty = true;
				for (int i = 1; i < result.getSuggestions().size(); ++i) {
					otherLinksFulltext.add(result.getSuggestions().get(i).getLinksFulltext().toString());
					if (!result.getSuggestions().get(i).getLinksFulltext().isEmpty()) {
						otherLinksFulltextEmpty = false;
					}
				}
				if (!otherLinksFulltextEmpty) {
					System.out.print(String.join(" | ", otherLinksFulltext));
				}
				System.out.print("\t");
				System.out.print(String.join(" | ", result.getLeftoverLinksAbstract().get(0)));
				System.out.print("\t");
				System.out.print(String.join(" | ", result.getLeftoverLinksFulltext().get(0)));
				System.out.print("\t");
				System.out.print(result.getTitle().get(0));
				System.out.print("\t");
				System.out.print(result.getToolTitleOthers().get(0));
				System.out.print("\t");
				System.out.print(result.getToolTitleExtractedOriginal().get(0));
				System.out.print("\t");
				System.out.print(result.getToolTitle().get(0));
				System.out.print("\t");
				System.out.print(result.getToolTitlePruned().get(0));
				System.out.print("\t");
				System.out.print(result.getToolTitleAcronym().get(0));
				System.out.print("\t");
				System.out.print(result.isOa().get(0));
				System.out.print("\t");
				System.out.print(result.getJournalTitle().get(0));
				System.out.print("\t");
				System.out.print(result.getPubDateHuman().get(0) + " (" + result.getPubDate().get(0) + ")");
				System.out.print("\t");
				System.out.print(result.getCitationsCount().get(0));
				System.out.print("\t");
				System.out.print(result.getCitationsTimestampHuman().get(0) + " (" + result.getCitationsTimestamp().get(0) + ")");
				System.out.print("\t");
				System.out.print(result.getCorrespAuthor().get(0).stream().map(ca -> ca.getName()).collect(Collectors.joining(" | ")));
				System.out.print("\t");
				System.out.print(result.getCorrespAuthor().get(0).stream().map(ca -> ca.getOrcid()).collect(Collectors.joining(" | ")));
				System.out.print("\t");
				System.out.print(result.getCorrespAuthor().get(0).stream().map(ca -> ca.getEmail()).collect(Collectors.joining(" | ")));
				System.out.print("\t");
				System.out.print(result.getCorrespAuthor().get(0).stream().map(ca -> ca.getPhone()).collect(Collectors.joining(" | ")));
				System.out.print("\t");
				System.out.print(result.getCorrespAuthor().get(0).stream().map(ca -> ca.getUri()).collect(Collectors.joining(" | ")));
				System.out.println();
			}

		} else {

			try (Database db = new Database(database);
					BufferedWriter resultsWriter = Files.newBufferedWriter(outputPath.resolve("results.csv"), StandardCharsets.UTF_8);
					BufferedWriter diffWriter = Files.newBufferedWriter(outputPath.resolve("diff.csv"), StandardCharsets.UTF_8);
					BufferedWriter newWriter = Files.newBufferedWriter(outputPath.resolve("new.json"), StandardCharsets.UTF_8)) {
				resultsWriter.write("pmid\tpmcid\tdoi\tsame_suggestions\tscore\tscore2\tscore2_parts\tsuggestion_original\tsuggestion\tsuggestion_processed\tpublication_and_name_existing\tname_existing_some_publication_different\tsome_publication_existing_name_different\tname_existing_publication_different\tname_match\tlink_match\tname_word_match\tlinks_abstract\tlinks_fulltext\tfrom_abstract_link\thomepage\thomepage_biotools\tlink\tlink_biotools\tdownload\tdownload_biotools\tdocumentation\tdocumentation_biotools\tbroken_links\tother_scores\tother_scores2\tother_scores2_parts\tother_suggestions_original\tother_suggestions\tother_suggestions_processed\tother_publication_and_name_existing\tother_name_existing_some_publication_different\tother_some_publication_existing_name_different\tother_name_existing_publication_different\tother_links_abstract\tother_links_fulltext\tleftover_links_abstract\tleftover_links_fulltext\ttitle\ttool_title_others\ttool_title_extracted_original\ttool_title\ttool_title_pruned\ttool_title_acronym\tdescription\tdescription_biotools\tlicense_homepage\tlicense_link\tlicense_download\tlicense_documentation\tlicense_abstract\tlicense\tlicense_biotools\tlanguage_homepage\tlanguage_link\tlanguage_download\tlanguage_documentation\tlanguage_abstract\tlanguage\tlanguage_biotools\toa\tjournal_title\tpub_date\tcitations_count\tcitations_timestamp\tcitations_count_normalised\tcorresp_author_name\tcredit_name_biotools\tcorresp_author_orcid\tcredit_orcidid_biotools\tcorresp_author_email\tcredit_email_biotools\tcorresp_author_phone\tcorresp_author_uri\tcredit_url_biotools\tcredit\n");
				diffWriter.write("biotools_id\tscore_score2\tcurrent_publications\tmodify_publications\tadd_publications\tcurrent_name\tmodify_name\tpossibly_related\tcurrent_homepage\tmodify_homepage\tcurrent_links\tadd_links\tcurrent_downloads\tadd_downloads\tcurrent_documentations\tadd_documentations\tcurrent_license\tmodify_license\tcurrent_languages\tadd_languages\tcurrent_credits\tmodify_credits\tadd_credits\n");

				for (Result result : results) {
					if (!result.getSuggestions().isEmpty() && result.getSuggestions().get(0).calculateScore2()) {
						result.getSuggestions().get(0).setScore2(result.getSuggestions().get(0).getScore());

						double firstScore = result.getSuggestions().get(0).getScore();
						for (int i = 0; i < result.getSuggestions().size(); ++i) {
							Suggestion suggestion = result.getSuggestions().get(i);
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
							for (Suggestion suggestion : result.getSuggestions()) {
								List<String> suggestionExtracted = new ArrayList<>(Arrays.asList(suggestion.getExtracted().split(" ")));
								String suggestionPrunedProcessed = String.join(" ", preProcessor.process(toolTitlePrune(suggestionExtracted)));
								int match = 0;
								if (suggestionPrunedProcessed.length() > 2) {
									if (toolTitlePrunedProcessedString.equals(suggestionPrunedProcessed)) {
										match = toolTitlePrunedProcessed.size();
									} else if (toolTitleAcronymProcessedString.equals(suggestionPrunedProcessed)) {
										match = 1;
									} else if (!toolTitlePrunedProcessedString.isEmpty() && isAcronym(toolTitlePrunedProcessedString, suggestion.getExtracted(), false)
											|| !toolTitleAcronymProcessedString.isEmpty() && isAcronym(toolTitleAcronymProcessedString, suggestion.getExtracted(), false)
											|| !toolTitleExtractedOriginal.isEmpty() && toolTitleExtractedOriginalSize <= TOOL_TITLE_ORIGINAL_MAX_SIZE_FOR_ACRONYM && isAcronym(suggestionPrunedProcessed, toolTitleExtractedOriginal, false)) {
										match = 2;
									} else if (toolTitlePrunedProcessedString.contains(suggestionPrunedProcessed)) {
										match = toolTitlePrunedProcessed.size() + 1;
									}
								} else if (suggestionPrunedProcessed.length() > 0) {
									if (toolTitlePrunedProcessedString.equals(suggestionPrunedProcessed)
											|| toolTitleAcronymProcessedString.equals(suggestionPrunedProcessed)) {
										match = 1;
									} else if (!toolTitleExtractedOriginal.isEmpty() && toolTitleExtractedOriginalSize <= TOOL_TITLE_ORIGINAL_MAX_SIZE_FOR_ACRONYM && isAcronym(suggestionPrunedProcessed, toolTitleExtractedOriginal, false)) {
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

						for (Suggestion suggestion : result.getSuggestions()) {
							if (suggestion.getScore2() < 0) {
								continue;
							}
							String suggestionExtractedString = suggestion.getExtracted();
							if (suggestion.isFromAbstractLink()) {
								suggestionExtractedString = PATH_SPLIT.matcher(suggestionExtractedString).replaceAll(" ").trim();
							}
							List<String> suggestionExtracted = new ArrayList<>(Arrays.asList(suggestionExtractedString.split(" ")));
							String[] suggestionPruned = toolTitlePrune(suggestionExtracted).split(" ");
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

						for (Suggestion suggestion : result.getSuggestions()) {
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

						for (Suggestion suggestion : result.getSuggestions()) {
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

				Collections.sort(results);

				TreeSet<Integer> removeResult = new TreeSet<>();
				for (int i = 0; i < results.size() - 1; ++i) {
					if (removeResult.contains(i)) {
						continue;
					}
					Result resultI = results.get(i);
					if (!resultI.getSuggestions().isEmpty()) {
						if (!resultI.getSuggestions().get(0).include()) {
							break;
						}
						for (int j = i + 1; j < results.size(); ++j) {
							if (removeResult.contains(j)) {
								continue;
							}
							Result resultJ = results.get(j);
							if (!resultJ.getSuggestions().isEmpty()) {
								if (!resultJ.getSuggestions().get(0).include()) {
									break;
								}
								if (resultI.getSuggestions().get(0).getExtracted().equals(resultJ.getSuggestions().get(0).getExtracted())) {
									resultI.addPublicationIds(resultJ.getPublicationIds().get(0));

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

									for (Iterator<Suggestion> iterI = resultI.getSuggestions().iterator(); iterI.hasNext(); ) {
										Suggestion suggestionI = iterI.next();
										for (Iterator<Suggestion> iterJ = resultJ.getSuggestions().iterator(); iterJ.hasNext(); ) {
											Suggestion suggestionJ = iterJ.next();
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
									for (Suggestion suggestionJ : resultJ.getSuggestions()) {
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

				for (int i = 0; i < results.size() - 1; ++i) {
					Result resultI = results.get(i);
					if (!resultI.getSuggestions().isEmpty()) {
						for (int j = i + 1; j < results.size(); ++j) {
							Result resultJ = results.get(j);
							if (!resultJ.getSuggestions().isEmpty()) {
								if (resultI.getSuggestions().get(0).getExtracted().equals(resultJ.getSuggestions().get(0).getExtracted())) {
									resultI.addSameSuggestion(resultJ.getPublicationIds().get(0));
									resultJ.addSameSuggestion(resultI.getPublicationIds().get(0));
								}
							}
						}
					}
				}

				List<List<String>> queryNamesExtracted = new ArrayList<>();
				List<String> queryNamesProcessed = new ArrayList<>();
				List<List<String>> queryLinks = new ArrayList<>();
				for (ToolInput biotool : biotools) {
					List<String> queryNameExtracted = preProcessor.extract(biotool.getName());
					List<String> queryNameProcessed = preProcessor.process(biotool.getName(), queryNameExtracted);
					queryNamesExtracted.add(Arrays.asList(BIOTOOLS_EXTRACTED_VERSION_TRIM.matcher(String.join(" ", queryNameExtracted)).replaceFirst("").split(" ")));
					queryNamesProcessed.add(BIOTOOLS_PROCESSED_VERSION_TRIM.matcher(String.join(" ", queryNameProcessed)).replaceFirst(""));
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
						.map(l -> trimUrl(l.trim()))
						.filter(l -> !l.isEmpty())
						.collect(Collectors.toList()));
				}

				for (Result result : results) {
					List<Boolean> oneMatches = new ArrayList<>();
					List<Boolean> allMatches = new ArrayList<>();
					List<Set<PublicationIds>> notMatches = new ArrayList<>();
					for (int i = 0; i < biotools.size(); ++i) {
						ToolInput biotool = biotools.get(i);
						boolean oneMatch = false;
						boolean allMatch = true;
						Set<PublicationIds> notMatch = null;
						for (PublicationIds publicationIds : result.getPublicationIds()) {
							boolean match = false;
							if (biotool.getPublication() != null) {
								for (org.edamontology.edammap.core.input.json.Publication pubIds : biotool.getPublication()) {
									if (!publicationIds.getPmid().isEmpty() && pubIds.getPmid() != null && pubIds.getPmid().trim().equals(publicationIds.getPmid())
											|| !publicationIds.getPmcid().isEmpty() && pubIds.getPmcid() != null && pubIds.getPmcid().trim().equals(publicationIds.getPmcid())
											|| !publicationIds.getDoi().isEmpty() && pubIds.getDoi() != null && PubFetcher.normaliseDoi(pubIds.getDoi().trim()).equals(publicationIds.getDoi())) {
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
								notMatch.add(publicationIds);
							}
						}
						oneMatches.add(oneMatch);
						allMatches.add(allMatch);
						notMatches.add(notMatch);
					}
					for (int i = 0; i < result.getSuggestions().size(); ++i) {
						Suggestion suggestion = result.getSuggestions().get(i);
						List<Integer> publicationAndNameExisting = null;
						List<Integer> nameExistingSomePublicationDifferent = null;
						List<Set<PublicationIds>> nameExistingSomePublicationDifferentPublicationIds = null;
						List<Integer> somePublicationExistingNameDifferent = null;
						List<Set<PublicationIds>> somePublicationExistingNameDifferentPublicationIds = null;
						List<Integer> nameExistingPublicationDifferent = null;
						List<Set<PublicationIds>> nameExistingPublicationDifferentPublicationIds = null;
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
									if (nameExistingSomePublicationDifferentPublicationIds == null) {
										nameExistingSomePublicationDifferentPublicationIds = new ArrayList<>();
									}
									nameExistingSomePublicationDifferentPublicationIds.add(notMatches.get(j));
								} else {
									if (nameExistingPublicationDifferent == null) {
										nameExistingPublicationDifferent = new ArrayList<>();
									}
									nameExistingPublicationDifferent.add(j);
									if (nameExistingPublicationDifferentPublicationIds == null) {
										nameExistingPublicationDifferentPublicationIds = new ArrayList<>();
									}
									nameExistingPublicationDifferentPublicationIds.add(notMatches.get(j));
								}
							} else if (oneMatches.get(j)) {
								if (somePublicationExistingNameDifferent == null) {
									somePublicationExistingNameDifferent = new ArrayList<>();
								}
								somePublicationExistingNameDifferent.add(j);
								if (somePublicationExistingNameDifferentPublicationIds == null) {
									somePublicationExistingNameDifferentPublicationIds = new ArrayList<>();
								}
								somePublicationExistingNameDifferentPublicationIds.add(notMatches.get(j));
							}
						}
						suggestion.setPublicationAndNameExisting(publicationAndNameExisting);
						suggestion.setNameExistingSomePublicationDifferent(nameExistingSomePublicationDifferent);
						suggestion.setNameExistingSomePublicationDifferentPublicationIds(nameExistingSomePublicationDifferentPublicationIds);
						suggestion.setSomePublicationExistingNameDifferent(somePublicationExistingNameDifferent);
						suggestion.setSomePublicationExistingNameDifferentPublicationIds(somePublicationExistingNameDifferentPublicationIds);
						suggestion.setNameExistingPublicationDifferent(nameExistingPublicationDifferent);
						suggestion.setNameExistingPublicationDifferentPublicationIds(nameExistingPublicationDifferentPublicationIds);

						if (i == 0) {
							String suggestionProcessed = BIOTOOLS_PROCESSED_VERSION_TRIM.matcher(result.getSuggestions().get(i).getProcessed()).replaceFirst("");
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
							String suggestionExtracted = BIOTOOLS_EXTRACTED_VERSION_TRIM.matcher(result.getSuggestions().get(i).getExtracted()).replaceFirst("");
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

				List<Diff> diffs = new ArrayList<>();
				List<Tool> tools = new ArrayList<>();

				for (Result result : results) {
					final String name;
					if (!result.getSuggestions().isEmpty()) {
						name = result.getSuggestions().get(0).getExtracted();
					} else {
						name = "";
					}

					for (Suggestion suggestion : result.getSuggestions()) {
						List<BiotoolsLink<LinkType>> linkLinksAbstract = new ArrayList<>();
						List<BiotoolsLink<DownloadType>> downloadLinksAbstract = new ArrayList<>();
						List<BiotoolsLink<DocumentationType>> documentationLinksAbstract = new ArrayList<>();
						makeBiotoolsLinks(suggestion.getLinksAbstract(), linkLinksAbstract, downloadLinksAbstract, documentationLinksAbstract);
						removeBroken(linkLinksAbstract, suggestion.getBrokenLinks(), db, false, name);
						removeBroken(downloadLinksAbstract, suggestion.getBrokenLinks(), db, false, name);
						removeBroken(documentationLinksAbstract, suggestion.getBrokenLinks(), db, true, name);
						String homepage = chooseHomepage(suggestion.getLinksAbstract(), linkLinksAbstract, documentationLinksAbstract, db);
						List<BiotoolsLink<LinkType>> linkLinksFulltext = new ArrayList<>();
						List<BiotoolsLink<DownloadType>> downloadLinksFulltext = new ArrayList<>();
						List<BiotoolsLink<DocumentationType>> documentationLinksFulltext = new ArrayList<>();
						makeBiotoolsLinks(suggestion.getLinksFulltext(), linkLinksFulltext, downloadLinksFulltext, documentationLinksFulltext);
						removeBroken(linkLinksFulltext, suggestion.getBrokenLinks(), db, false, name);
						removeBroken(downloadLinksFulltext, suggestion.getBrokenLinks(), db, false, name);
						removeBroken(documentationLinksFulltext, suggestion.getBrokenLinks(), db, true, name);
						if (homepage == null) {
							homepage = chooseHomepage(suggestion.getLinksFulltext(), linkLinksFulltext, documentationLinksFulltext, db);
						}
						if (homepage == null) {
							for (String link : suggestion.getLinksAbstract()) {
								link = prependHttp(link);
								if (!DOWNLOAD_EXT.matcher(link).find() && BIOTOOLS_SCHEMA_URL_PATTERN.matcher(link).matches()) {
									homepage = link;
									suggestion.setHomepageBroken(true);
									break;
								}
							}
						}
						if (homepage == null) {
							for (String link : suggestion.getLinksFulltext()) {
								link = prependHttp(link);
								if (!DOWNLOAD_EXT.matcher(link).find() && BIOTOOLS_SCHEMA_URL_PATTERN.matcher(link).matches()) {
									homepage = link;
									suggestion.setHomepageBroken(true);
									break;
								}
							}
						}
						if (homepage != null) {
							suggestion.setHomepage(homepage);
						} else {
							for (PublicationIds publicationIds : result.getPublicationIds()) {
								homepage = PubFetcher.getPmidLink(publicationIds.getPmid());
								if (homepage == null) homepage = PubFetcher.getPmcidLink(publicationIds.getPmcid());
								if (homepage == null) homepage = PubFetcher.getDoiLink(publicationIds.getDoi());
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

					writeField(resultsWriter, result.getPublicationIds().stream().map(p -> p.getPmid()).collect(Collectors.joining(" | ")));
					writeField(resultsWriter, result.getPublicationIds().stream().map(p -> p.getPmcid()).collect(Collectors.joining(" | ")));
					writeField(resultsWriter, result.getPublicationIds().stream().map(p -> p.getDoi()).collect(Collectors.joining(" | ")));

					writeField(resultsWriter, result.getSameSuggestions().stream().map(pubIds -> pubIds.toString()).collect(Collectors.joining(" | ")));

					final Suggestion suggestion;
					if (!result.getSuggestions().isEmpty()) {
						suggestion = result.getSuggestions().get(0);
					} else {
						suggestion = null;
					}

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
								((suggestion.getNameExistingSomePublicationDifferentPublicationIds() != null && suggestion.getNameExistingSomePublicationDifferentPublicationIds().get(i) != null) ? (" (" +
									String.join(" ; ", suggestion.getNameExistingSomePublicationDifferentPublicationIds().get(i).stream().map(p -> p.toString()).collect(Collectors.toList()))
								+ ")") : "")).collect(Collectors.toList());
						writeField(resultsWriter, String.join(" | ", nameExistingSomePublicationDifferent));
					} else {
						writeField(resultsWriter, null);
					}

					List<String> somePublicationExistingNameDifferent = null;
					if (suggestion != null && suggestion.getSomePublicationExistingNameDifferent() != null) {
						somePublicationExistingNameDifferent = IntStream.range(0, suggestion.getSomePublicationExistingNameDifferent().size())
							.mapToObj(i -> biotools.get(suggestion.getSomePublicationExistingNameDifferent().get(i)).getBiotoolsID() + " (" + biotools.get(suggestion.getSomePublicationExistingNameDifferent().get(i)).getName() + ")" +
								((suggestion.getSomePublicationExistingNameDifferentPublicationIds() != null && suggestion.getSomePublicationExistingNameDifferentPublicationIds().get(i) != null && !suggestion.getSomePublicationExistingNameDifferentPublicationIds().get(i).isEmpty()) ? (" (" +
									String.join(" ; ", suggestion.getSomePublicationExistingNameDifferentPublicationIds().get(i).stream().map(p -> p.toString()).collect(Collectors.toList()))
								+ ")") : "")).collect(Collectors.toList());
						writeField(resultsWriter, String.join(" | ", somePublicationExistingNameDifferent));
					} else {
						writeField(resultsWriter, null);
					}

					List<String> nameExistingPublicationDifferent = null;
					if (suggestion != null && suggestion.getNameExistingPublicationDifferent() != null) {
						nameExistingPublicationDifferent = IntStream.range(0, suggestion.getNameExistingPublicationDifferent().size())
							.mapToObj(i -> biotools.get(suggestion.getNameExistingPublicationDifferent().get(i)).getBiotoolsID() +
								((suggestion.getNameExistingPublicationDifferentPublicationIds() != null && suggestion.getNameExistingPublicationDifferentPublicationIds().get(i) != null) ? (" (" +
									String.join(" ; ", suggestion.getNameExistingPublicationDifferentPublicationIds().get(i).stream().map(p -> p.toString()).collect(Collectors.toList()))
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
								((s.getNameExistingSomePublicationDifferentPublicationIds() != null && s.getNameExistingSomePublicationDifferentPublicationIds().get(i) != null) ? (" (" +
									String.join(" ; ", s.getNameExistingSomePublicationDifferentPublicationIds().get(i).stream().map(p -> p.toString()).collect(Collectors.toList()))
								+ ")") : "")).collect(Collectors.toList())
						: null).collect(Collectors.toList());
					writeField(resultsWriter, otherNameExistingSomePublicationDifferent.stream().map(e -> e != null ? String.join(" ; ", e) : "").collect(Collectors.joining(" | ")));

					List<List<String>> otherSomePublicationExistingNameDifferent = result.getSuggestions().stream().skip(1)
						.map(s -> s.getSomePublicationExistingNameDifferent() != null ? IntStream.range(0, s.getSomePublicationExistingNameDifferent().size())
							.mapToObj(i -> biotools.get(s.getSomePublicationExistingNameDifferent().get(i)).getBiotoolsID() + " (" + biotools.get(s.getSomePublicationExistingNameDifferent().get(i)).getName() + ")" +
								((s.getSomePublicationExistingNameDifferentPublicationIds() != null && s.getSomePublicationExistingNameDifferentPublicationIds().get(i) != null && !s.getSomePublicationExistingNameDifferentPublicationIds().get(i).isEmpty()) ? (" (" +
									String.join(" ; ", s.getSomePublicationExistingNameDifferentPublicationIds().get(i).stream().map(p -> p.toString()).collect(Collectors.toList()))
								+ ")") : "")).collect(Collectors.toList())
						: null).collect(Collectors.toList());
					writeField(resultsWriter, otherSomePublicationExistingNameDifferent.stream().map(e -> e != null ? String.join(" ; ", e) : "").collect(Collectors.joining(" | ")));

					List<List<String>> otherNameExistingPublicationDifferent = result.getSuggestions().stream().skip(1)
						.map(s -> s.getNameExistingPublicationDifferent() != null ? IntStream.range(0, s.getNameExistingPublicationDifferent().size())
							.mapToObj(i -> biotools.get(s.getNameExistingPublicationDifferent().get(i)).getBiotoolsID() +
								((s.getNameExistingPublicationDifferentPublicationIds() != null && s.getNameExistingPublicationDifferentPublicationIds().get(i) != null) ? (" (" +
									String.join(" ; ", s.getNameExistingPublicationDifferentPublicationIds().get(i).stream().map(p -> p.toString()).collect(Collectors.toList()))
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
					for (String message : messages) {
						message = "> " + message + " | ";
						if (description.length() + message.length() <= BIOTOOLS_DESCRIPTION_MESSAGE_MAX_LENGTH) {
							description += message;
						} else {
							// TODO log
							break;
						}
					}

					List<Description> descriptions = new ArrayList<>();
					for (String title : result.getTitle()) {
						String publicationTitleDescription = License.WHITESPACE.matcher(descriptionFromTitle(title, TITLE_SEPARATOR)).replaceAll(" ").trim();
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
								String abstractDescription = pruneToMax(License.WHITESPACE.matcher(abstractSentences.get(0).replaceAll("\\|", ":")).replaceAll(" ").trim(), BIOTOOLS_DESCRIPTION_MAX_LENGTH - description.length() - abstractSentencesLength);
								for (int i = 1; i < abstractSentences.size(); ++i) {
									String abstractSentence = License.WHITESPACE.matcher(abstractSentences.get(i).replaceAll("\\|", ":")).replaceAll(" ").trim();
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
							description += pruneToMax("NO DESCRIPTION FOUND FROM LINKS OR ABSTRACT!", BIOTOOLS_DESCRIPTION_MAX_LENGTH - description.length());
						} else {
							description += String.join(" | ", abstractDescriptions);
						}
					}
					String descriptionOriginal = description;
					if (description.length() < BIOTOOLS_SCHEMA_DESCRIPTION_MIN) {
						description = fillToMin(description, BIOTOOLS_SCHEMA_DESCRIPTION_MIN);
						logger.warn("Description filled to min from '{}' to '{}' (for name '{}')", descriptionOriginal, description, name);
					}
					if (description.length() > BIOTOOLS_SCHEMA_DESCRIPTION_MAX) {
						description = pruneToMax(description, BIOTOOLS_SCHEMA_DESCRIPTION_MAX);
						logger.warn("Description pruned to max from '{}' to '{}' (for name '{}')", descriptionOriginal, description, name);
					}
					writeField(resultsWriter, description);
					writeField(resultsWriter, existing.stream().map(e -> biotools.get(e)).map(q -> q.getDescription().replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r").replaceAll("\t", "\\\\t")).collect(Collectors.joining(" | ")));

					List<Provenance> webpageLicenses = new ArrayList<>();
					if (!homepage.isEmpty()) {
						String homepageLicense = null;
						if (db.getWebpage(homepage) != null) {
							homepageLicense = db.getWebpage(homepage).getLicense();
						} else if (db.getDoc(homepage) != null) {
							homepageLicense = db.getDoc(homepage).getLicense();
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

					List<Provenance> linkLicenses = linkLinks.stream().map(l -> db.getWebpage(l.getUrl()) != null ? new Provenance(db.getWebpage(l.getUrl()).getLicense(), l.getUrl()) : new Provenance()).collect(Collectors.toList());
					writeField(resultsWriter, linkLicenses.stream().map(p -> p.toString()).collect(Collectors.joining(" | ")));
					linkLicenses.removeIf(p -> p.isEmpty());
					webpageLicenses.addAll(linkLicenses);

					List<Provenance> downloadLicenses = downloadLinks.stream().map(l -> db.getWebpage(l.getUrl()) != null ? new Provenance(db.getWebpage(l.getUrl()).getLicense(), l.getUrl()) : new Provenance()).collect(Collectors.toList());
					writeField(resultsWriter, downloadLicenses.stream().map(p -> p.toString()).collect(Collectors.joining(" | ")));
					downloadLicenses.removeIf(p -> p.isEmpty());
					webpageLicenses.addAll(downloadLicenses);

					List<Provenance> documentationLicenses = documentationLinks.stream().map(l -> db.getDoc(l.getUrl()) != null ? new Provenance(db.getDoc(l.getUrl()).getLicense(), l.getUrl()) : new Provenance()).collect(Collectors.toList());
					writeField(resultsWriter, documentationLicenses.stream().map(p -> p.toString()).collect(Collectors.joining(" | ")));
					documentationLicenses.removeIf(p -> p.isEmpty());
					webpageLicenses.addAll(documentationLicenses);

					List<List<Provenance>> abstractLicenses = new ArrayList<>();
					for (int i = 0; i < result.getPublicationIds().size(); ++i) {
						List<String> abstractSentences = result.getAbstractSentences().get(i);
						String provenance = result.getPublicationIds().get(i).toString();
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
						if (db.getWebpage(homepage) != null) {
							homepageLanguage = db.getWebpage(homepage).getLanguage();
						} else if (db.getDoc(homepage) != null) {
							homepageLanguage = db.getDoc(homepage).getLanguage();
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

					List<Provenance> linkLanguages = linkLinks.stream().map(l -> db.getWebpage(l.getUrl()) != null ? new Provenance(db.getWebpage(l.getUrl()).getLanguage(), l.getUrl()) : new Provenance()).collect(Collectors.toList());
					writeField(resultsWriter, linkLanguages.stream().map(p -> p.toString()).collect(Collectors.joining(" | ")));
					linkLanguages.removeIf(p -> p.isEmpty());
					webpageLanguages.addAll(linkLanguages);

					List<Provenance> downloadLanguages = downloadLinks.stream().map(l -> db.getWebpage(l.getUrl()) != null ? new Provenance(db.getWebpage(l.getUrl()).getLanguage(), l.getUrl()) : new Provenance()).collect(Collectors.toList());
					writeField(resultsWriter, downloadLanguages.stream().map(p -> p.toString()).collect(Collectors.joining(" | ")));
					downloadLanguages.removeIf(p -> p.isEmpty());
					webpageLanguages.addAll(downloadLanguages);

					List<Provenance> documentationLanguages = documentationLinks.stream().map(l -> db.getDoc(l.getUrl()) != null ? new Provenance(db.getDoc(l.getUrl()).getLanguage(), l.getUrl()) : new Provenance()).collect(Collectors.toList());
					writeField(resultsWriter, documentationLanguages.stream().map(p -> p.toString()).collect(Collectors.joining(" | ")));
					documentationLanguages.removeIf(p -> p.isEmpty());
					webpageLanguages.addAll(documentationLanguages);

					List<List<Provenance>> abstractLanguages = new ArrayList<>();
					for (int i = 0; i < result.getPublicationIds().size(); ++i) {
						List<String> abstractSentences = result.getAbstractSentences().get(i);
						String provenance = result.getPublicationIds().get(i).toString();
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
								if (creditNameEqual(ca.getName(), credit.getName()) || creditOrcidEqual(ca.getOrcid(), credit.getOrcid()) || creditEmailEqual(ca.getEmail(), credit.getEmail())) {
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
									otherPart = TOOL_TITLE_TRIM.matcher(String.join("", preProcessor.process(otherPart))).replaceFirst("");
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
									addDiff(diffs, makeDiff(scoreScore2, possiblyRelated, biotools, suggestion.getPublicationAndNameExisting().get(i), result.getPublicationIds(), null, null, homepage, linkLinks, downloadLinks, documentationLinks, bestLicense, allLanguages, credits, db));
								} else {
									addDiff(diffs, makeDiff(scoreScore2, possiblyRelated, biotools, suggestion.getPublicationAndNameExisting().get(i), result.getPublicationIds(), null, null, null, null, null, null, bestAbstractLicense, abstractLanguagesUnique, credits, db));
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
									addDiff(diffs, makeDiff(scoreScore2, possiblyRelated, biotools, suggestion.getNameExistingSomePublicationDifferent().get(i), result.getPublicationIds(), suggestion.getNameExistingSomePublicationDifferentPublicationIds().get(i), null, homepage, linkLinks, downloadLinks, documentationLinks, bestLicense, allLanguages, credits, db));
								} else {
									addDiff(diffs, makeDiff(scoreScore2, possiblyRelated, biotools, suggestion.getNameExistingSomePublicationDifferent().get(i), result.getPublicationIds(), suggestion.getNameExistingSomePublicationDifferentPublicationIds().get(i), null, null, null, null, null, bestAbstractLicense, abstractLanguagesUnique, credits, db));
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
									addDiff(diffs, makeDiff(scoreScore2, possiblyRelated, biotools, suggestion.getSomePublicationExistingNameDifferent().get(i), result.getPublicationIds(), suggestion.getSomePublicationExistingNameDifferentPublicationIds().get(i), name, homepage, linkLinks, downloadLinks, documentationLinks, bestLicense, allLanguages, credits, db));
								} else {
									addDiff(diffs, makeDiff(scoreScore2, possiblyRelated, biotools, suggestion.getSomePublicationExistingNameDifferent().get(i), result.getPublicationIds(), suggestion.getSomePublicationExistingNameDifferentPublicationIds().get(i), null, null, null, null, null, bestAbstractLicense, abstractLanguagesUnique, credits, db));
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
							for (PublicationIds publicationIds : result.getPublicationIds().stream().filter(id -> !id.isEmpty()).collect(Collectors.toCollection(LinkedHashSet::new))) {
								org.edamontology.edammap.core.input.json.Publication newPublication = new org.edamontology.edammap.core.input.json.Publication();
								if (!publicationIds.getDoi().isEmpty()) {
									newPublication.setDoi(publicationIds.getDoi());
								}
								if (!publicationIds.getPmid().isEmpty()) {
									newPublication.setPmid(publicationIds.getPmid());
								}
								if (!publicationIds.getPmcid().isEmpty()) {
									newPublication.setPmcid(publicationIds.getPmcid());
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
				}

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

				org.edamontology.edammap.core.output.Json.outputBiotools(tools, newWriter);
			}
		}
	}

	private static void beforeAfter(PreProcessorArgs preProcessorArgs, String queryIdf, String database, List<String> pubFile) throws IOException {
		PreProcessor preProcessor = new PreProcessor(preProcessorArgs);

		Idf idf = new Idf(queryIdf);

		Set<Publication> publications = new LinkedHashSet<>(PubFetcher.getPublications(database, pubFile, PubMedApps.class.getSimpleName()));

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

		if (args.pass1 && requiredArgs(new String[] { "idf", "db", "pub", "query", "type", "web", "doc" }, "pass1", args)) {
			run(args.preProcessorArgs, args.idf, args.db, args.pub, args.query, args.type, args.web, args.doc, null, args.fetcherArgs, true);
		}

		if (args.pass2 && requiredArgs(new String[] { "idf", "db", "pub", "query", "type", "output" }, "pass2", args)) {
			run(args.preProcessorArgs, args.idf, args.db, args.pub, args.query, args.type, null, null, args.output, args.fetcherArgs, false);
		}

		if (args.beforeAfter && requiredArgs(new String[] { "idf", "db", "pub" }, "beforeAfter", args)) {
			beforeAfter(args.preProcessorArgs, args.idf, args.db, args.pub);
		}

		// TODO log
		logger.info("Ready");
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
