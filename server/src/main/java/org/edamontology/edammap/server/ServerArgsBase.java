/*
 * Copyright © 2018, 2019 Erik Jaaniso
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

import java.io.File;

import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.BasicArgs;
import org.edamontology.pubfetcher.core.common.FetcherPrivateArgs;

import org.edamontology.edammap.core.processing.ProcessorArgs;

public abstract class ServerArgsBase extends BasicArgs {

	private static final String edamId = "edam";
	private static final String edamDescription = "Path of the EDAM ontology file";
	private static final String edamDefault = null;
	@Parameter(names = { "-e", "--" + edamId }, required = true, description = edamDescription)
	private String edam;

	@ParametersDelegate
	private ProcessorArgs processorArgs = new ProcessorArgs();

	@ParametersDelegate
	private FetcherPrivateArgs fetcherPrivateArgs = new FetcherPrivateArgs();

	@Override
	protected void addArgs() {
		args.add(new Arg<>(this::getEdamFilename, null, edamDefault, edamId, "Ontology file", edamDescription, null, "https://github.com/edamontology/edamontology/tree/master/releases"));
	}

	@Override
	public String getId() {
		return "serverArgs";
	}

	public String getEdam() {
		return edam;
	}
	public String getEdamFilename() {
		return new File(edam).getName();
	}

	public ProcessorArgs getProcessorArgs() {
		return processorArgs;
	}

	public FetcherPrivateArgs getFetcherPrivateArgs() {
		return fetcherPrivateArgs;
	}
}
