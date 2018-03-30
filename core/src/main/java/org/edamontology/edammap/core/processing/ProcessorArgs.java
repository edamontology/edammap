/*
 * Copyright © 2016, 2018 Erik Jaaniso
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

public class ProcessorArgs {
	public static final String FETCHING = "fetching";
	@Parameter(names = { "--fetch", "--fetcher", "--" + FETCHING }, arity = 1, description = "Fetch publications, webpages and docs")
	private boolean fetching = true;

	public static final String DB = "db";
	@Parameter(names = { "--" + DB, "--database" }, description = "Use the given database for getting and storing publications, webpages and docs")
	private String db = "";

	public static final String IDF = "idf";
	@Parameter(names = { "--" + IDF, "--query-" + IDF }, description = "Use the given query IDF file (when stemming is not enabled); if not specified, weighting of queries with IDF scores will be disabled (when stemming is not enabled)")
	private String idf = "";

	public static final String IDF_STEMMED = "idf-stemmed";
	@Parameter(names = { "--" + IDF_STEMMED, "--query-" + IDF_STEMMED }, description = "Use the given query IDF file (when stemming is enabled); if not specified, weighting of queries with IDF scores will be disabled (when stemming is enabled)")
	private String idfStemmed = "";

	public boolean isFetching() {
		return fetching;
	}
	public void setFetching(boolean fetching) {
		this.fetching = fetching;
	}

	public String getDb() {
		return db;
	}
	public void setDb(String db) {
		this.db = db;
	}

	public String getIdf() {
		return idf;
	}
	public void setIdf(String idf) {
		this.idf = idf;
	}

	public String getIdfStemmed() {
		return idfStemmed;
	}
	public void setIdfStemmed(String idfStemmed) {
		this.idfStemmed = idfStemmed;
	}
}
