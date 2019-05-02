/*
 * Copyright © 2016, 2017, 2018, 2019 Erik Jaaniso
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

package org.edamontology.edammap.util;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import org.edamontology.pubfetcher.cli.PubFetcherArgs;
import org.edamontology.pubfetcher.core.common.BasicArgs;
import org.edamontology.pubfetcher.core.common.FetcherArgs;

import org.edamontology.edammap.core.query.QueryType;

public class UtilArgs extends BasicArgs {

	@Parameter(names = { "-pub-query" }, variableArity = true, description = "Load all publication IDs found in the specified files of QueryType specified with --query-type. A file can either be local or a URL, in which case --timeout and --userAgent can be used to change parameters used to fetch it.")
	List<String> pubQuery = null;

	@Parameter(names = { "-web-query" }, variableArity = true, description = "Load all webpage URLs found in the specified files of QueryType specified with --query-type. A file can either be local or a URL, in which case --timeout and --userAgent can be used to change parameters used to fetch it.")
	List<String> webQuery = null;

	@Parameter(names = { "-doc-query" }, variableArity = true, description = "Load all doc URLs found in the specified files of QueryType specified with --query-type. A file can either be local or a URL, in which case --timeout and --userAgent can be used to change parameters used to fetch it.")
	List<String> docQuery = null;

	@Parameter(names = { "-all-query" }, variableArity = true, description = "Load all publication IDs, webpage URLs and doc URLs found in the specified files of QueryType specified with --query-type. A file can either be local or a URL, in which case --timeout and --userAgent can be used to change parameters used to fetch it.")
	List<String> allQuery = null;

	@Parameter(names = { "--query-type" }, description = "Specifies the type of the query files loaded using -pub-query, -web-query, -doc-query and -all-query")
	QueryType queryType = QueryType.generic;

	@Parameter(names = { "--make-idf", "--make-query-idf" }, arity = 3, description = "Argument order: queryPath, database, idfPath. Make the specified IDF file from tokens parsed from queries of type -make-idf-type loaded from the specified query file. The tokens are not stemmed. Contents for publication IDs, webpage URLs and doc URLs found in queries are loaded from the specified database file. If -make-idf-webpages-docs is true (the default), then tokens from webpage and doc content will also be used to make the IDF file and if -make-idf-fulltext is true (the default), then tokens from publication fulltext will also be used to make the IDF file. If the specified query file is a URL, then --timeout and --userAgent can be used to change parameters used to fetch it. The fetching parameters --titleMinLength, --keywordsMinSize, --minedTermsMinSize, --abstractMinLength, --fulltextMinLength and --webpageMinLength can be used to change the minimum length of a usable corresponding part (parts below that length will not be tokenised, thus will not used to make the specified IDF file).")
	List<String> makeIdf = null;

	@Parameter(names = { "--make-idf-nodb", "--make-query-idf-nodb" }, arity = 2, description = "Argument order: queryPath, idfPath. Make the specified IDF file from tokens parsed from queries of type -make-idf-type loaded from the specified query file. The tokens are not stemmed. Contents for publication IDs, webpage URLs and doc URLs found in queries are are not loaded and thus are not used to make the specified IDF file. If the specified query file is a URL, then --timeout and --userAgent can be used to change parameters used to fetch it.")
	List<String> makeIdfNoDb = null;

	@Parameter(names = { "--make-idf-stemmed", "--make-query-idf-stemmed" }, arity = 3, description = "Argument order: queryPath, database, idfPath. Make the specified IDF file from tokens parsed from queries of type -make-idf-type loaded from the specified query file. The tokens are stemmed. Contents for publication IDs, webpage URLs and doc URLs found in queries are loaded from the specified database file. If -make-idf-webpages-docs is true (the default), then tokens from webpage and doc content will also be used to make the IDF file and if -make-idf-fulltext is true (the default), then tokens from publication fulltext will also be used to make the IDF file. If the specified query file is a URL, then --timeout and --userAgent can be used to change parameters used to fetch it. The fetching parameters --titleMinLength, --keywordsMinSize, --minedTermsMinSize, --abstractMinLength, --fulltextMinLength and --webpageMinLength can be used to change the minimum length of a usable corresponding part (parts below that length will not be tokenised, thus will not used to make the specified IDF file).")
	List<String> makeIdfStemmed = null;

	@Parameter(names = { "--make-idf-stemmed-nodb", "--make-query-idf-stemmed-nodb" }, arity = 2, description = "Argument order: queryPath, idfPath. Make the specified IDF file from tokens parsed from queries of type -make-idf-type loaded from the specified query file. The tokens are stemmed. Contents for publication IDs, webpage URLs and doc URLs found in queries are are not loaded and thus are not used to make the specified IDF file. If the specified query file is a URL, then --timeout and --userAgent can be used to change parameters used to fetch it.")
	List<String> makeIdfStemmedNoDb = null;

	@Parameter(names = { "-make-idf-type", "-make-query-idf-type" }, description = "The QueryType of the query file loaded to make the IDF file with --make-idf, --make-idf-nodb, --make-idf-stemmed or --make-idf-stemmed-nodb")
	QueryType makeIdfType = QueryType.biotools;

	@Parameter(names = { "-make-idf-webpages-docs", "-make-query-idf-webpages-docs" }, arity = 1, description = "Whether tokens from webpage and doc content will also be used to make the IDF file with --make-idf or --make-idf-stemmed")
	boolean makeIdfWebpagesDocs = true;

	@Parameter(names = { "-make-idf-fulltext", "-make-query-idf-fulltext" }, arity = 1, description = "Whether tokens from publication fulltext will also be used to make the IDF file with --make-idf or --make-idf-stemmed")
	boolean makeIdfFulltext = true;

	@Parameter(names = { "--print-idf-top", "--print-query-idf-top" }, arity = 2, description = "Argument order: idfPath, n. Print top n most frequent terms from the specified IDF file along with their counts (that show in how many documents a term occurs)")
	List<String> printIdfTop = null;

	@Parameter(names = { "--print-idf", "--print-query-idf" }, variableArity = true, description = "Argument order: idfPath, term, term, ... Print given terms along with their IDF scores (between 0 and 1) read from the given IDF file. Given terms are preprocessed, but stemming is not done, thus terms in the given IDF file must not be stemmed either.")
	List<String> printIdf = null;

	@Parameter(names = { "--print-idf-stemmed", "--print-query-idf-stemmed" }, variableArity = true, description = "Argument order: idfPath, term, term, ... Print given terms along with their IDF scores (between 0 and 1) read from the given IDF file. Given terms are preprocessed, with stemming being done, thus terms in the given IDF file must also be stemmed.")
	List<String> printIdfStemmed = null;

	@Parameter(names = { "--biotools-full" }, description = "Fetch all content (by following \"next\" until the last page) from https://bio.tools/api/tool to the specified JSON file. Fetching parameters --timeout and --userAgent can be used.")
	String biotoolsFull = null;

	@Parameter(names = { "--biotools-dev-full" }, description = "Fetch all content (by following \"next\" until the last page) from https://dev.bio.tools/api/tool to the specified JSON file. Fetching parameters --timeout and --userAgent can be used.")
	String biotoolsDevFull = null;

	@Parameter(names = { "--make-server-files" }, description = "Create new directory with CSS, JavaScript and font files required by EDAMmap-Server. The version of EDAMmap-Server the files are created for must match the version of EDAMmap-Util running the command.")
	String makeServerFiles = null;

	@Parameter(names = { "--make-options-conf" }, description = "Create new options configuration file")
	String makeOptionsConf = null;

	@ParametersDelegate
	FetcherArgs fetcherArgs = new FetcherArgs();

	@ParametersDelegate
	PubFetcherArgs pubFetcherArgs = new PubFetcherArgs();
}
