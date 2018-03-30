/*
 * Copyright © 2016, 2017, 2018 Erik Jaaniso
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

import org.edamontology.edammap.core.query.QueryType;
import org.edamontology.pubfetcher.BasicArgs;
import org.edamontology.pubfetcher.FetcherArgs;
import org.edamontology.pubfetcher.FetcherUtilArgs;

public class UtilArgs extends BasicArgs {

	@Parameter(names = { "-pub-query" }, variableArity = true, description = "TODO")
	List<String> pubQuery = null;

	@Parameter(names = { "-web-query" }, variableArity = true, description = "TODO")
	List<String> webQuery = null;

	@Parameter(names = { "-doc-query" }, variableArity = true, description = "TODO")
	List<String> docQuery = null;

	@Parameter(names = { "-all-query" }, variableArity = true, description = "TODO")
	List<String> allQuery = null;

	@Parameter(names = { "-query-type" }, description = "TODO")
	QueryType queryType = QueryType.generic;

	@Parameter(names = { "--make-idf", "--make-query-idf" }, arity = 3, description = "String queryPath, String database, String idfPath")
	List<String> makeIdf = null;

	@Parameter(names = { "--make-idf-nodb", "--make-query-idf-nodb" }, arity = 2, description = "String queryPath, String idfPath")
	List<String> makeIdfNoDb = null;

	@Parameter(names = { "--make-idf-stemmed", "--make-query-idf-stemmed" }, arity = 3, description = "String queryPath, String database, String idfPath")
	List<String> makeIdfStemmed = null;

	@Parameter(names = { "--make-idf-stemmed-nodb", "--make-query-idf-stemmed-nodb" }, arity = 2, description = "String queryPath, String idfPath")
	List<String> makeIdfStemmedNoDb = null;

	@Parameter(names = { "-make-idf-type", "-make-query-idf-type" }, description = "QueryType type")
	QueryType makeIdfType = QueryType.biotools;

	@Parameter(names = { "-make-idf-webpages-docs", "-make-query-idf-webpages-docs" }, arity = 1, description = "TODO")
	boolean makeIdfWebpagesDocs = true;

	@Parameter(names = { "-make-idf-fulltext", "-make-query-idf-fulltext" }, arity = 1, description = "TODO")
	boolean makeIdfFulltext = true;

	@Parameter(names = { "--print-idf-top", "--print-query-idf-top" }, arity = 2, description = "String inputPath, long n")
	List<String> printIdfTop = null;

	@Parameter(names = { "--print-idf", "--print-query-idf" }, arity = 2, description = "String inputPath, String term")
	List<String> printIdf = null;

	@Parameter(names = { "--biotools-full" }, description = "String outputPath, FetcherArgs for timeout and user agent")
	String biotoolsFull = null;

	@Parameter(names = { "--biotools-dev-full" }, description = "String outputPath, FetcherArgs for timeout and user agent")
	String biotoolsDevFull = null;

	@Parameter(names = { "--make-server-files" }, description = "String outputPath")
	String makeServerFiles = null;

	@ParametersDelegate
	FetcherArgs fetcherArgs = new FetcherArgs();

	@ParametersDelegate
	FetcherUtilArgs fetcherUtilArgs = new FetcherUtilArgs();
}
