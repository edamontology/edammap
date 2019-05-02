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

import org.edamontology.edammap.core.processing.ProcessorArgs;

import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.BasicArgs;
import org.edamontology.pubfetcher.core.common.FetcherPrivateArgs;

public class ServerArgs extends BasicArgs {

	private static final String edamId = "edam";
	private static final String edamDescription = "Path of the EDAM ontology file";
	private static final String edamDefault = null;
	@Parameter(names = { "-e", "--" + edamId }, required = true, description = edamDescription)
	private String edam;

	static final String txtId = "txt";
	private static final String txtDescription = "Output results to plain text file for queries made through the web application. The value can be changed in the web application itself.";
	private static final Boolean txtDefault = true;
	@Parameter(names = { "--" + txtId }, arity = 1, description = txtDescription)
	private Boolean txt = txtDefault;

	static final String htmlId = "html";
	private static final String htmlDescription = "Output results to HTML file for queries made through the web application. Always true.";
	private static final Boolean htmlDefault = true;
	private Boolean html = htmlDefault;

	static final String jsonId = "json";
	private static final String jsonDescription = "Output results to JSON file for queries made through the web application. The value can be changed in the web application itself.";
	private static final Boolean jsonDefault = false;
	@Parameter(names = { "--" + jsonId }, arity = 1, description = jsonDescription)
	private Boolean json = jsonDefault;

	@ParametersDelegate
	private ServerPrivateArgs serverPrivateArgs = new ServerPrivateArgs();

	@ParametersDelegate
	private ProcessorArgs processorArgs = new ProcessorArgs();

	@ParametersDelegate
	private FetcherPrivateArgs fetcherPrivateArgs = new FetcherPrivateArgs();

	@Override
	protected void addArgs() {
		args.add(new Arg<>(this::getEdamFilename, null, edamDefault, edamId, "Ontology file", edamDescription, null, "https://github.com/edamontology/edamontology/tree/master/releases"));
		args.add(new Arg<>(this::isTxt, null, txtDefault, txtId, "Results to text", txtDescription, null));
		args.add(new Arg<>(this::isHtml, null, htmlDefault, htmlId, "Results to HTML", htmlDescription, null));
		args.add(new Arg<>(this::isJson, null, jsonDefault, jsonId, "Results to JSON", jsonDescription, null));
	}

	public String getEdam() {
		return edam;
	}
	public String getEdamFilename() {
		return new File(edam).getName();
	}

	public Boolean isTxt() {
		return txt;
	}

	public Boolean isHtml() {
		return html;
	}

	public Boolean isJson() {
		return json;
	}

	public ServerPrivateArgs getServerPrivateArgs() {
		return serverPrivateArgs;
	}

	public ProcessorArgs getProcessorArgs() {
		return processorArgs;
	}

	public FetcherPrivateArgs getFetcherPrivateArgs() {
		return fetcherPrivateArgs;
	}
}
