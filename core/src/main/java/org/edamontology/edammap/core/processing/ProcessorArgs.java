/*
 * Copyright © 2016 Erik Jaaniso
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

package org.edamontology.edammap.core.processing;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import org.edamontology.edammap.core.preprocessing.PreProcessorArgs;
import org.edamontology.pubfetcher.FetcherArgs;

public class ProcessorArgs {
	@Parameter(names = { "--fetching-disabled" }, description = "Disable fetching of webpages, publications and docs")
	private boolean fetchingDisabled = false;

	@Parameter(names = { "-d", "--db", "--database" }, description = "Use the given database for getting and storing webpages, publications and docs")
	private String database = "";

	@Parameter(names = { "--query-idf" }, description = "Use the given query IDF file; if not specified, weighting of queries with IDF scores will be disabled")
	private String queryIdf = "";

	@ParametersDelegate
	private PreProcessorArgs preProcessorArgs = new PreProcessorArgs();

	@ParametersDelegate
	private FetcherArgs fetcherArgs = new FetcherArgs();

	public boolean isFetchingDisabled() {
		return fetchingDisabled;
	}

	public void setFetchingDisabled(boolean fetchingDisabled) {
		this.fetchingDisabled = fetchingDisabled;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getQueryIdf() {
		return queryIdf;
	}

	public void setQueryIdf(String queryIdf) {
		this.queryIdf = queryIdf;
	}

	public PreProcessorArgs getPreProcessorArgs() {
		return preProcessorArgs;
	}

	public void setPreProcessorArgs(PreProcessorArgs preProcessorArgs) {
		this.preProcessorArgs = preProcessorArgs;
	}

	public FetcherArgs getFetcherArgs() {
		return fetcherArgs;
	}

	public void setFetcherArgs(FetcherArgs fetcherArgs) {
		this.fetcherArgs = fetcherArgs;
	}
}
