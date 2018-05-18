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

package org.edamontology.edammap.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

import org.edamontology.edammap.core.args.CoreArgs;
import org.edamontology.edammap.core.query.QueryType;

public class CliArgs extends CoreArgs {

	public static final String EDAM = "edam";
	@Parameter(names = { "-e", "--" + EDAM }, required = true, description = "Path of the EDAM ontology file")
	private String edam;

	public static final String QUERY = "query";
	@Parameter(names = { "-q", "--" + QUERY }, required = true, description = "Path of file containing queries")
	private String query;

	public static final String TYPE = "type";
	@Parameter(names = { "-t", "--" + TYPE, "--queryType" }, description = "Specifies the type of the query and how to output the results")
	private QueryType type = QueryType.generic;

	public static final String OUTPUT = "output";
	@Parameter(names = { "-o", "--" + OUTPUT }, description = "File to write results to, one per line. If missing (and HTML report also not specified), then results will be written to standard output.")
	private String output = "";

	public static final String REPORT = "report";
	@Parameter(names = { "-r", "--" + REPORT, "--results" }, description = "Directory to write a HTML report to. In addition to detailed results, it will contain used parameters, metrics, comparisons to manual mapping, extended information about queries and nice formatting.")
	private String report = "";

	public static final String JSON = "json";
	@Parameter(names = { "-j", "--" + JSON }, description = "File to write results to, in JSON format. Will include same info as HTML report.")
	private String json = "";

	public static final String REPORT_PAGE_SIZE = "reportPageSize";
	@Parameter(names = { "--" + REPORT_PAGE_SIZE }, validateWith = PositiveInteger.class, description = "Number of results in a HTML report page. Setting to 0 will output all results to a single HTML page.")
	private int reportPageSize = 100;

	public static final String REPORT_PAGINATION_SIZE = "reportPaginationSize";
	@Parameter(names = { "--" + REPORT_PAGINATION_SIZE }, validateWith = PositiveInteger.class, description = "Number of pagination links visible before/after the current page link in a HTML report page. Setting to 0 will make all pagination links visible.")
	private int reportPaginationSize = 11;

	public static final String THREADS = "threads";
	@Parameter(names = { "--" + THREADS }, validateWith = PositiveInteger.class, description = "How many threads to use for mapping (one query is processed by one thread)")
	private int threads = 4;

	public String getEdam() {
		return edam;
	}

	public String getQuery() {
		return query;
	}

	public QueryType getType() {
		return type;
	}

	public String getOutput() {
		return output;
	}

	public String getReport() {
		return report;
	}

	public String getJson() {
		return json;
	}

	public int getReportPageSize() {
		return reportPageSize;
	}

	public int getReportPaginationSize() {
		return reportPaginationSize;
	}

	public int getThreads() {
		return threads;
	}
}
