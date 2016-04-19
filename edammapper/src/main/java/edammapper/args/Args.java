package edammapper.args;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import edammapper.query.IOType;

public class Args {
	@Parameter(names = { "-e", "--edam" }, required = true, description = "Path of the EDAM ontology file")
	private String edam;

	@Parameter(names = { "-q", "--query" }, required = true, description = "Path of file containing queries")
	private String query;

	@Parameter(names = { "-h", "--help" }, help = true, description = "Print this help")
	private boolean help;

	@Parameter(names = { "-t", "--type" }, description = "Specifies the type of the query and how to output the results")
	private IOType type = IOType.csv;

	@Parameter(names = { "-o", "--output" }, description = "File to write results to. If not specified or invalid, will be written to standard output.")
	private String output = "";

	@Parameter(names = { "-r", "--report" }, description = "File to write a HTML report to. In addition to the usual output, but with formatting in a browser.")
	private String report = "";

	@Parameter(names = { "-k", "--benchmark-report" }, description = "File to write HTML benchmark report to. It will contain metrics and comparisons to the manual mapping specified in the input query file.")
	private String benchmarkReport = "";

	@ParametersDelegate
	private PreProcessorArgs preProcessorArgs = new PreProcessorArgs();

	@ParametersDelegate
	private MapperArgs mapperArgs = new MapperArgs();

	public String getEdam() {
		return edam;
	}

	public String getQuery() {
		return query;
	}

	public boolean isHelp() {
		return help;
	}

	public IOType getType() {
		return type;
	}

	public String getOutput() {
		return output;
	}

	public String getReport() {
		return report;
	}

	public String getBenchmarkReport() {
		return benchmarkReport;
	}

	public PreProcessorArgs getPreProcessorArgs() {
		return preProcessorArgs;
	}

	public MapperArgs getMapperArgs() {
		return mapperArgs;
	}
}
