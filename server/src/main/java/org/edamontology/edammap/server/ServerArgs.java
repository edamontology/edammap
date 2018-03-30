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
import org.edamontology.pubfetcher.BasicArgs;
import org.edamontology.pubfetcher.FetcherPrivateArgs;

public class ServerArgs extends BasicArgs {

	public static final String EDAM = "edam";
	@Parameter(names = { "-e", "--" + EDAM }, required = true, description = "Path of the EDAM ontology file")
	private String edam;

	public static final String BASE_URI = "base-uri";
	@Parameter(names = { "-b", "--" + BASE_URI }, description = "Base URI where server will be deployed")
	private String baseUri = "http://localhost:8080";

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

	public String getBaseUri() {
		return baseUri;
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
