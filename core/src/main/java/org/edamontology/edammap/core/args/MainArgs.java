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

package org.edamontology.edammap.core.args;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.beust.jcommander.validators.PositiveInteger;

import org.edamontology.edammap.core.mapping.args.MapperArgs;
import org.edamontology.edammap.core.processing.ProcessorArgs;
import org.edamontology.edammap.core.query.QueryType;
import org.edamontology.pubfetcher.BasicArgs;

public class MainArgs extends BasicArgs {
	@Parameter(names = { "-e", "--edam" }, required = true, description = "Path of the EDAM ontology file")
	private String edam;

	@Parameter(names = { "-q", "--query" }, required = true, description = "Path of file containing queries")
	private String query;

	@Parameter(names = { "-t", "--type", "--query-type" }, description = "Specifies the type of the query and how to output the results")
	private QueryType type = QueryType.generic;

	@Parameter(names = { "-o", "--output" }, description = "File to write results to, one per line. If missing (and HTML report also not specified), then results will be written to standard output.")
	private String output = "";

	@Parameter(names = { "-r", "--report", "--results" }, description = "File to write a HTML report to. In addition to detailed results, it will contain used parameters, metrics, comparisons to manual mapping, extended information about queries and nice formatting.")
	private String report = "";

	@Parameter(names = { "--report-page-size" }, validateWith = PositiveInteger.class, description = "Number of results in a HTML report page. Setting to 0 will output all results to a single HTML page.")
	private int reportPageSize = 100;

	@Parameter(names = { "--report-pagination-size" }, validateWith = PositiveInteger.class, description = "Number of pagination links visible before/after the current page link in a HTML report page. Setting to 0 will make all pagination links visible.")
	private int reportPaginationSize = 11;

	@Parameter(names = { "--threads" }, description = "How many threads to use for mapping (one query is processed by one thread)")
	private int threads = 4;

	@ParametersDelegate
	private ProcessorArgs processorArgs = new ProcessorArgs();

	@ParametersDelegate
	private MapperArgs mapperArgs = new MapperArgs();

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

	public int getReportPageSize() {
		return reportPageSize;
	}

	public int getReportPaginationSize() {
		return reportPaginationSize;
	}

	public int getThreads() {
		return threads;
	}

	public ProcessorArgs getProcessorArgs() {
		return processorArgs;
	}

	public MapperArgs getMapperArgs() {
		return mapperArgs;
	}
}
