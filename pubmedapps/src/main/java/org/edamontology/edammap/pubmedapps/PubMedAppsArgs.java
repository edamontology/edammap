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

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import org.edamontology.edammap.core.preprocessing.PreProcessorArgs;
import org.edamontology.edammap.core.query.QueryType;
import org.edamontology.pubfetcher.FetcherArgs;
import org.edamontology.pubfetcher.MainArgs;

public class PubMedAppsArgs implements MainArgs {
	@Parameter(names = { "-h", "--help" }, help = true, description = "Print this help")
	boolean help;

	@Parameter(names = { "-db", "-database" }, description = "TODO")
	String db = null;

	@Parameter(names = { "-pub", "-pub-file" }, variableArity = true, description = "TODO")
	List<String> pub = null;

	@Parameter(names = { "-idf", "-query-idf" }, description = "Use the given query IDF file")
	String idf = null;

	@Parameter(names = { "-query", "-query-path" }, description = "TODO")
	String query = null;

	@Parameter(names = { "-type", "-query-type" }, description = "TODO")
	QueryType type = QueryType.biotools;

	@Parameter(names = { "-web", "-web-file" }, description = "TODO")
	String web = null;

	@Parameter(names = { "-doc", "-doc-file" }, description = "TODO")
	String doc = null;

	@Parameter(names = { "--mesh-query" }, description = "TODO")
	boolean meshQuery = false;

	@Parameter(names = { "--write-web-doc" }, description = "TODO")
	boolean writeWebDoc = false;

	@Parameter(names = { "--print-results" }, description = "TODO")
	boolean printResults = false;

	@Parameter(names = { "--before-after" }, description = "TODO")
	boolean beforeAfter = false;

	@ParametersDelegate
	FetcherArgs fetcherArgs = new FetcherArgs();

	@ParametersDelegate
	PreProcessorArgs preProcessorArgs = new PreProcessorArgs();

	@Override
	public boolean isHelp() {
		return help;
	}
}
