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

package org.edamontology.edammap.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import java.io.File;

import org.edamontology.edammap.core.args.CoreArgs;
import org.edamontology.edammap.core.input.Input;
import org.edamontology.edammap.core.query.QueryType;

import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.BasicArgs;
import org.edamontology.pubfetcher.core.common.PositiveInteger;

public class CliArgs extends BasicArgs {

	private static final String edamId = "edam";
	private static final String edamDescription = "Path of the EDAM ontology file";
	private static final String edamDefault = null;
	@Parameter(names = { "-e", "--" + edamId }, required = true, description = edamDescription)
	private String edam;

	private static final String queryId = "query";
	private static final String queryDescription = "Path or URL of file containing input queries of QueryType --type";
	private static final String queryDefault = null;
	@Parameter(names = { "-q", "--" + queryId }, required = true, description = queryDescription)
	private String query;

	private static final String typeId = "type";
	private static final String typeDescription = "Specifies the type of the query and how to output the results";
	private static final QueryType typeDefault = QueryType.generic;
	@Parameter(names = { "-t", "--" + typeId, "--queryType" }, description = typeDescription)
	private QueryType type = typeDefault;

	private static final String outputId = "output";
	private static final String outputDescription = "File to write results to, one per line. If missing (and HTML report also not specified), then results will be written to standard output.";
	private static final String outputDefault = "";
	@Parameter(names = { "-o", "--" + outputId }, description = outputDescription)
	private String output = outputDefault;

	private static final String reportId = "report";
	private static final String reportDescription = "Directory to write a HTML report to. In addition to detailed results, it will contain used parameters, metrics, comparisons to manual mapping, extended information about queries and nice formatting. The specified directory will be created and must not be an existing directory.";
	private static final String reportDefault = "";
	@Parameter(names = { "-r", "--" + reportId, "--results" }, description = reportDescription)
	private String report = reportDefault;

	private static final String jsonId = "json";
	private static final String jsonDescription = "File to write results to, in JSON format. Will include same info as HTML report.";
	private static final String jsonDefault = "";
	@Parameter(names = { "-j", "--" + jsonId }, description = jsonDescription)
	private String json = jsonDefault;

	private static final String biotoolsId = "biotools";
	private static final String biotoolsDescription = "File to write results to, in bio.tools JSON format, confirming to biotoolsSchema. Available only for '--type biotools', where the input JSON is copied to the output, but with new annotations found by EDAMmap added. New annotations from the topic branch will be added to the 'topic' and new annotations from the operation branch will all be added under a new 'function' of a bio.tools entry. New annotations from the data and format branches will be added as strings in the form 'EDAM URI (label)' separated by ' | ' to the 'note' of the latest 'function', because EDAMmap can't differentiate inputs and outputs."; // TODO to documentation
	private static final String biotoolsDefault = "";
	@Parameter(names = { "-b", "--" + biotoolsId }, description = biotoolsDescription)
	private String biotools = biotoolsDefault;

	private static final String reportPageSizeId = "reportPageSize";
	private static final String reportPageSizeDescription = "Number of results in a HTML report page. Setting to 0 will output all results to a single HTML page.";
	private static final Integer reportPageSizeDefault = 100;
	@Parameter(names = { "--" + reportPageSizeId }, validateWith = PositiveInteger.class, description = reportPageSizeDescription)
	private Integer reportPageSize = reportPageSizeDefault;

	private static final String reportPaginationSizeId = "reportPaginationSize";
	private static final String reportPaginationSizeDescription = "Number of pagination links visible before/after the current page link in a HTML report page. Setting to 0 will make all pagination links visible.";
	private static final Integer reportPaginationSizeDefault = 11;
	@Parameter(names = { "--" + reportPaginationSizeId }, validateWith = PositiveInteger.class, description = reportPaginationSizeDescription)
	private Integer reportPaginationSize = reportPaginationSizeDefault;

	private static final String threadsId = "threads";
	private static final String threadsDescription = "How many threads to use for mapping (one thread processes one query at a time)";
	private static final Integer threadsDefault = 4;
	@Parameter(names = { "--" + threadsId }, validateWith = PositiveInteger.class, description = threadsDescription)
	private Integer threads = threadsDefault;

	@ParametersDelegate
	private CoreArgs coreArgs = new CoreArgs();

	@Override
	protected void addArgs() {
		args.add(new Arg<>(this::getEdamFilename, null, edamDefault, edamId, "Ontology file", edamDescription, null, "https://github.com/edamontology/edamontology/tree/master/releases"));
		args.add(new Arg<>(this::getQueryFilename, null, queryDefault, queryId, "Query file", queryDescription, null));
		args.add(new Arg<>(this::getType, null, typeDefault, typeId, "Type", typeDescription, QueryType.class));
		args.add(new Arg<>(this::getOutputFilename, null, outputDefault, outputId, "Output file", outputDescription, null));
		args.add(new Arg<>(this::getReportFilename, null, reportDefault, reportId, "Report file", reportDescription, null));
		args.add(new Arg<>(this::getJsonFilename, null, jsonDefault, jsonId, "JSON file", jsonDescription, null));
		args.add(new Arg<>(this::getBiotoolsFilename, null, biotoolsDefault, biotoolsId, "bio.tools file", biotoolsDescription, null));
		args.add(new Arg<>(this::getReportPageSize, null, reportPageSizeDefault, 0, null, reportPageSizeId, "Report page size", reportPageSizeDescription, null));
		args.add(new Arg<>(this::getReportPaginationSize, null, reportPaginationSizeDefault, 0, null, reportPaginationSizeId, "Report pagination size", reportPaginationSizeDescription, null));
		args.add(new Arg<>(this::getThreads, null, threadsDefault, 0, null, threadsId, "Number of threads", threadsDescription, null));
	}

	public String getEdam() {
		return edam;
	}
	public String getEdamFilename() {
		return new File(edam).getName();
	}
	public void setEdam(String edam) {
		this.edam = edam;
	}

	public String getQuery() {
		return query;
	}
	public String getQueryFilename() {
		if (Input.isProtocol(query)) {
			return query;
		} else {
			return new File(query).getName();
		}
	}
	public void setQuery(String query) {
		this.query = query;
	}

	public QueryType getType() {
		return type;
	}
	public void setType(QueryType type) {
		this.type = type;
	}

	public String getOutput() {
		return output;
	}
	public String getOutputFilename() {
		return new File(output).getName();
	}
	public void setOutput(String output) {
		this.output = output;
	}

	public String getReport() {
		return report;
	}
	public String getReportFilename() {
		return new File(report).getName();
	}
	public void setReport(String report) {
		this.report = report;
	}

	public String getJson() {
		return json;
	}
	public String getJsonFilename() {
		return new File(json).getName();
	}
	public void setJson(String json) {
		this.json = json;
	}

	public String getBiotools() {
		return biotools;
	}
	public String getBiotoolsFilename() {
		return new File(biotools).getName();
	}
	public void setBiotools(String biotools) {
		this.biotools = biotools;
	}

	public Integer getReportPageSize() {
		return reportPageSize;
	}
	public void setReportPageSize(Integer reportPageSize) {
		this.reportPageSize = reportPageSize;
	}

	public Integer getReportPaginationSize() {
		return reportPaginationSize;
	}
	public void setReportPaginationSize(Integer reportPaginationSize) {
		this.reportPaginationSize = reportPaginationSize;
	}

	public Integer getThreads() {
		return threads;
	}
	public void setThreads(Integer threads) {
		this.threads = threads;
	}

	public CoreArgs getCoreArgs() {
		return coreArgs;
	}
	public void setCoreArgs(CoreArgs coreArgs) {
		this.coreArgs = coreArgs;
	}
}
