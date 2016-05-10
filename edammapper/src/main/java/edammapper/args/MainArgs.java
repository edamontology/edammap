package edammapper.args;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import edammapper.mapping.MapperArgs;
import edammapper.processing.ProcessorArgs;
import edammapper.query.QueryType;

public class MainArgs {
	@Parameter(names = { "-e", "--edam" }, required = true, description = "Path of the EDAM ontology file")
	private String edam;

	@Parameter(names = { "-q", "--query" }, required = true, description = "Path of file containing queries")
	private String query;

	@Parameter(names = { "-h", "--help" }, help = true, description = "Print this help")
	private boolean help;

	@Parameter(names = { "-t", "--type" }, description = "Specifies the type of the query and how to output the results")
	private QueryType type = QueryType.generic;

	@Parameter(names = { "-o", "--output" }, description = "File to write results to. If not specified or invalid, will be written to standard output.")
	private String output = "";

	@Parameter(names = { "-r", "--report" }, description = "File to write a HTML report to. In addition to the usual output, but with formatting in a browser.")
	private String report = "";

	@Parameter(names = { "-k", "--benchmark-report" }, description = "File to write HTML benchmark report to. It will contain metrics and comparisons to the manual mapping specified in the input query file.")
	private String benchmarkReport = "";

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

	public boolean isHelp() {
		return help;
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

	public String getBenchmarkReport() {
		return benchmarkReport;
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
