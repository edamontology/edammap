/*
 * Copyright © 2018 Erik Jaaniso
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

package org.edamontology.edammap.server;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import org.edamontology.edammap.core.processing.ProcessorArgs;
import org.edamontology.pubfetcher.core.common.BasicArgs;
import org.edamontology.pubfetcher.core.common.FetcherPrivateArgs;

public class ServerArgs extends BasicArgs {

	public static final String EDAM = "edam";
	@Parameter(names = { "-e", "--" + EDAM }, required = true, description = "Path of the EDAM ontology file")
	private String edam;

	public static final String TXT = "txt";
	@Parameter(names = { "--" + TXT }, arity = 1, description = "Also output results to text file")
	private boolean txt = true;

	public static final String JSON = "json";
	@Parameter(names = { "--" + JSON }, arity = 1, description = "Also output results to JSON file")
	private boolean json = false;

	public static final String BASE_URI = "baseUri";
	@Parameter(names = { "-b", "--" + BASE_URI }, description = "URI where server will be deployed (as schema://host:port)")
	private String baseUri = "http://localhost:8080";

	public static final String PATH = "path";
	@Parameter(names = { "-p", "--" + PATH }, description = "Path where server will be deployed (only one single path segment supported)")
	private String path = "edammap";

	public static final String HTTPS_PROXY = "httpsProxy";
	@Parameter(names = { "--" + HTTPS_PROXY }, description = "Set if we are behind a HTTPS proxy")
	private boolean httpsProxy = false;

	public static final String FILES = "files";
	@Parameter(names = { "-f", "--" + FILES }, required = true, description = "Directory with HTML resources and output results")
	private String files;

	// TODO
	//public static final String THREADS = "threads";
	//@Parameter(names = { "--" + THREADS }, validateWith = PositiveInteger.class, description = "TODO")
	//private int threads = 4;

	@ParametersDelegate
	private ProcessorArgs processorArgs = new ProcessorArgs();

	@ParametersDelegate
	private FetcherPrivateArgs fetcherPrivateArgs = new FetcherPrivateArgs();

	public String getEdam() {
		return edam;
	}

	public boolean getTxt() {
		return txt;
	}

	public boolean getJson() {
		return json;
	}

	public String getBaseUri() {
		return baseUri;
	}

	public String getPath() {
		return path;
	}

	public boolean getHttpsProxy() {
		return httpsProxy;
	}

	public String getFiles() {
		return files;
	}

	public ProcessorArgs getProcessorArgs() {
		return processorArgs;
	}

	public FetcherPrivateArgs getFetcherPrivateArgs() {
		return fetcherPrivateArgs;
	}
}
