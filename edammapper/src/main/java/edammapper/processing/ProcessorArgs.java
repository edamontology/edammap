package edammapper.processing;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import edammapper.preprocessing.PreProcessorArgs;

public class ProcessorArgs {
	@Parameter(names = { "--fetching-disabled" }, description = "Disable fetching of webpages, publications and docs")
	private boolean fetchingDisabled = false;

	@Parameter(names = { "-d", "--database" }, description = "Use the given database for getting and storing webpages, publications and docs")
	private String database = "";

	@Parameter(names = { "--query-idf" }, description = "Use the given query IDF file; if not specified, weighting of queries with IDF scores will be disabled")
	private String queryIdf = "";

	@ParametersDelegate
	private PreProcessorArgs preProcessorArgs = new PreProcessorArgs();

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
}
