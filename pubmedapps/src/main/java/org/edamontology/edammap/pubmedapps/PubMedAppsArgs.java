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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import org.edamontology.pubfetcher.core.common.BasicArgs;
import org.edamontology.pubfetcher.core.common.FetcherArgs;
import org.edamontology.edammap.core.mapping.args.MapperArgs;
import org.edamontology.edammap.core.preprocessing.PreProcessorArgs;

public class PubMedAppsArgs extends BasicArgs {

	@Parameter(names = { "-copy-edam" }, description = "TODO")
	String copyEdam = null;

	@Parameter(names = { "-copy-idf" }, description = "TODO")
	String copyIdf = null;

	@Parameter(names = { "-get-biotools" }, description = "TODO")
	String getBiotools = null;

	@Parameter(names = { "-copy-biotools" }, description = "TODO")
	String copyBiotools = null;

	@Parameter(names = { "-select-pub" }, description = "TODO")
	String selectPub = null;

	@Parameter(names = { "-copy-pub" }, description = "TODO")
	String copyPub = null;

	@Parameter(names = { "-init-db" }, description = "TODO")
	String initDb = null;

	@Parameter(names = { "-copy-db" }, description = "TODO")
	String copyDb = null;

	@Parameter(names = { "-fetch-pub" }, description = "TODO")
	String fetchPub = null;

	@Parameter(names = { "-pass1" }, description = "TODO")
	String pass1 = null;

	@Parameter(names = { "-fetch-web" }, description = "TODO")
	String fetchWeb = null;

	@Parameter(names = { "-pass2" }, description = "TODO")
	String pass2 = null;

	@Parameter(names = { "-map", "-edammap" }, description = "TODO")
	String map = null;

	@Parameter(names = { "-all" }, description = "TODO")
	String all = null;

	@Parameter(names = { "-resume" }, description = "TODO")
	String resume = null;

	@Parameter(names = { "--edam", "--edam-owl" }, description = "TODO")
	String edam = null;

	@Parameter(names = { "--idf", "--query-idf" }, description = "Use the given (not stemmed) query IDF file")
	String idf = null;

	@Parameter(names = { "--idf-stemmed", "--query-idf-stemmed" }, description = "Use the given (stemmed) query IDF file")
	String idfStemmed = null;

	@Parameter(names = { "--biotools", "--biotools-json" }, description = "TODO")
	String biotools = null;

	@Parameter(names = { "--pub", "--pub-ids", "--pub-file" }, description = "TODO")
	String pub = null;

	@Parameter(names = { "--db", "--database" }, description = "TODO")
	String db = null;

	@Parameter(names = { "--fetcher-threads", "--fetch-threads" }, description = "TODO")
	int fetcherThreads = 8;

	@Parameter(names = { "--mapper-threads",  "--map-threads" }, description = "TODO")
	int mapperThreads = 4;

	@Parameter(names = { "--verbose" }, description = "TODO")
	LogLevel verbose = LogLevel.OFF;

	// TODO remove
	@Parameter(names = { "-mesh-query" }, description = "TODO")
	boolean meshQuery = false;

	// TODO remove
	@Parameter(names = { "-journal-query" }, description = "TODO")
	boolean journalQuery = false;

	@Parameter(names = { "-before-after" }, description = "TODO")
	boolean beforeAfter = false;

	@Parameter(names = { "-europepmc-abstract" }, description = "TODO")
	Integer europepmcAbstract = null;

	@ParametersDelegate
	PreProcessorArgs preProcessorArgs = new PreProcessorArgs();

	@ParametersDelegate
	FetcherArgs fetcherArgs = new FetcherArgs();

	@ParametersDelegate
	MapperArgs mapperArgs = new MapperArgs();
}
