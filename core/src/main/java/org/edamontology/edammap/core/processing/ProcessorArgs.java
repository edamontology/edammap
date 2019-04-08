/*
 * Copyright © 2016, 2018, 2019 Erik Jaaniso
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

import java.io.File;

import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.Args;

import com.beust.jcommander.Parameter;

public class ProcessorArgs extends Args {

	private static final String fetchingId = "fetching";
	private static final String fetchingDescription = "Fetch publications, webpages and docs";
	private static final Boolean fetchingDefault = true;
	@Parameter(names = { "--fetch", "--fetcher", "--" + fetchingId }, arity = 1, description = fetchingDescription)
	private Boolean fetching = fetchingDefault;

	private static final String dbId = "db";
	private static final String dbDescription = "Use the given database for getting and storing publications, webpages and docs";
	private static final String dbDefault = "";
	@Parameter(names = { "--" + dbId, "--database" }, description = dbDescription)
	private String db = dbDefault;

	private static final String idfId = "idf";
	private static final String idfDescription = "Use the given query IDF file (when stemming is not enabled); if not specified, weighting of queries with IDF scores will be disabled (when stemming is not enabled)";
	private static final String idfDefault = "";
	@Parameter(names = { "--" + idfId, "--query-" + idfId }, description = idfDescription)
	private String idf = idfDefault;

	private static final String idfStemmedId = "idfStemmed";
	private static final String idfStemmedDescription = "Use the given query IDF file (when stemming is enabled); if not specified, weighting of queries with IDF scores will be disabled (when stemming is enabled)";
	private static final String idfStemmedDefault = "";
	@Parameter(names = { "--" + idfStemmedId, "--query-" + idfStemmedId }, description = idfStemmedDescription)
	private String idfStemmed = idfStemmedDefault;

	@Override
	protected void addArgs() {
		args.add(new Arg<>(this::isFetching, this::setFetching, fetchingDefault, fetchingId, "Fetching", fetchingDescription, null));
		args.add(new Arg<>(this::getDbFilename, this::setDb, dbDefault, dbId, "Database file", dbDescription, null));
		args.add(new Arg<>(this::getIdfFilename, this::setIdf, idfDefault, idfId, "Query IDF file", idfDescription, null));
		args.add(new Arg<>(this::getIdfStemmedFilename, this::setIdfStemmed, idfStemmedDefault, idfStemmedId, "Stemmed query IDF file", idfStemmedDescription, null));
	}

	@Override
	public String getId() {
		return "processorArgs";
	}

	@Override
	public String getLabel() {
		return "Processing";
	}

	public Boolean isFetching() {
		return fetching;
	}
	public void setFetching(Boolean fetching) {
		this.fetching = fetching;
	}

	public String getDb() {
		return db;
	}
	public String getDbFilename() {
		return new File(db).getName();
	}
	public void setDb(String db) {
		this.db = db;
	}

	public String getIdf() {
		return idf;
	}
	public String getIdfFilename() {
		return new File(idf).getName();
	}
	public void setIdf(String idf) {
		this.idf = idf;
	}

	public String getIdfStemmed() {
		return idfStemmed;
	}
	public String getIdfStemmedFilename() {
		return new File(idfStemmed).getName();
	}
	public void setIdfStemmed(String idfStemmed) {
		this.idfStemmed = idfStemmed;
	}
}
