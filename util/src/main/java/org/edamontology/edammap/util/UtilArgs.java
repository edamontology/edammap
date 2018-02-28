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

import org.edamontology.edammap.core.preprocessing.PreProcessorArgs;
import org.edamontology.edammap.core.query.QueryType;
import org.edamontology.pubfetcher.FetcherArgs;
import org.edamontology.pubfetcher.FetcherUtilArgs;
import org.edamontology.pubfetcher.MainArgs;

public class UtilArgs implements MainArgs {
	@Parameter(names = { "-h", "--help" }, help = true, description = "Print this help")
	boolean help;

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

	@Parameter(names = { "--make-query-idf" }, arity = 4, description = "String queryPath, QueryType type, String outputPath, String database")
	List<String> makeQueryIdf = null;

	@Parameter(names = { "--make-query-idf-without-database" }, arity = 3, description = "String queryPath, QueryType type, String outputPath")
	List<String> makeQueryIdfWithoutDatabase = null;

	@Parameter(names = { "--make-query-idf-no-webpages-docs" }, description = "TODO")
	boolean makeQueryIdfNoWebpagesDocs = false;

	@Parameter(names = { "--make-query-idf-no-fulltext" }, description = "TODO")
	boolean makeQueryIdfNoFulltext = false;

	@Parameter(names = { "--print-query-idf-top" }, arity = 2, description = "String inputPath, long n")
	List<String> printQueryIdfTop = null;

	@Parameter(names = { "--biotools-full" }, description = "String outputPath")
	String biotoolsFull = null;

	@Parameter(names = { "--biotools-dev-full" }, description = "String outputPath")
	String biotoolsDevFull = null;

	@ParametersDelegate
	FetcherArgs fetcherArgs = new FetcherArgs();

	@ParametersDelegate
	FetcherUtilArgs fetcherUtilArgs = new FetcherUtilArgs();

	@ParametersDelegate
	PreProcessorArgs preProcessorArgs = new PreProcessorArgs();

	@Override
	public boolean isHelp() {
		return help;
	}
}
