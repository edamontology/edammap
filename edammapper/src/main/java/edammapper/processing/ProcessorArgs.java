package edammapper.processing;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import edammapper.preprocessing.PreProcessorArgs;

public class ProcessorArgs {
	@Parameter(names = { "--fetching-disabled" }, description = "")
	private boolean fetchingDisabled = false;

	@Parameter(names = { "-d", "--database" }, description = "")
	private String database = "";

	@Parameter(names = { "--query-idf" }, description = "")
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
