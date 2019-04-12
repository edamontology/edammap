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

import java.io.File;

import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.Args;

public class ServerPrivateArgs extends Args {

	private static final String baseUriId = "baseUri";
	private static final String baseUriDescription = "URI where server will be deployed (as schema://host:port)";
	private static final String baseUriDefault = "http://localhost:8080";
	@Parameter(names = { "-b", "--" + baseUriId }, description = baseUriDescription)
	private String baseUri = baseUriDefault;

	private static final String pathId = "path";
	private static final String pathDescription = "Path where server will be deployed (only one single path segment supported)";
	private static final String pathDefault = "edammap";
	@Parameter(names = { "-p", "--" + pathId }, description = pathDescription)
	private String path = pathDefault;

	private static final String httpsProxyId = "httpsProxy";
	private static final String httpsProxyDescription = "Set if we are behind a HTTPS proxy";
	private static final Boolean httpsProxyDefault = false;
	@Parameter(names = { "--" + httpsProxyId }, description = httpsProxyDescription)
	private Boolean httpsProxy = httpsProxyDefault;

	private static final String filesId = "files";
	private static final String filesDescription = "Directory with HTML resources and output results";
	private static final String filesDefault = null;
	@Parameter(names = { "-f", "--" + filesId }, required = true, description = filesDescription)
	private String files;

	@Override
	protected void addArgs() {
		args.add(new Arg<>(this::getBaseUri, null, baseUriDefault, baseUriId, "", baseUriDescription, null));
		args.add(new Arg<>(this::getPath, null, pathDefault, pathId, "", pathDescription, null));
		args.add(new Arg<>(this::isHttpsProxy, null, httpsProxyDefault, httpsProxyId, "", httpsProxyDescription, null));
		args.add(new Arg<>(this::getFilesFilename, null, filesDefault, filesId, "", filesDescription, null));
	}

	@Override
	public String getId() {
		return "serverPrivateArgs";
	}

	@Override
	public String getLabel() {
		return "EDAMmap-Server (private)";
	}

	public String getBaseUri() {
		return baseUri;
	}

	public String getPath() {
		return path;
	}

	public Boolean isHttpsProxy() {
		return httpsProxy;
	}

	public String getFiles() {
		return files;
	}
	public String getFilesFilename() {
		return new File(files).getName();
	}
}
