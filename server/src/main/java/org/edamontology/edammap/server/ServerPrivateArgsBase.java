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
import org.edamontology.pubfetcher.core.common.PositiveInteger;

public abstract class ServerPrivateArgsBase extends Args {

	private static final String baseUriId = "baseUri";
	private static final String baseUriDescription = "URI where the server will be deployed (as schema://host:port)";
	private static final String baseUriDefault = "http://localhost:8080";
	@Parameter(names = { "-b", "--" + baseUriId }, description = baseUriDescription)
	private String baseUri = baseUriDefault;

	private static final String httpsProxyId = "httpsProxy";
	private static final String httpsProxyDescription = "Use if we are behind a HTTPS proxy";
	private static final Boolean httpsProxyDefault = false;
	@Parameter(names = { "--" + httpsProxyId }, description = httpsProxyDescription)
	private Boolean httpsProxy = httpsProxyDefault;

	private static final String filesId = "files";
	private static final String filesDescription = "An existing directory where the results will be output. It must contain required CSS, JavaScript and font resources pre-generated with EDAMmap-Util.";
	private static final String filesDefault = null;
	@Parameter(names = { "-f", "--" + filesId }, required = true, description = filesDescription)
	private String files;

	private static final String fetchingThreadsId = "fetchingThreads";
	private static final String fetchingThreadsDescription = "How many threads to create (maximum) for fetching individual database entries of one query";
	private static final Integer fetchingThreadsDefault = 8;
	@Parameter(names = { "--" + fetchingThreadsId }, validateWith = PositiveInteger.class, description = fetchingThreadsDescription)
	private Integer fetchingThreads = fetchingThreadsDefault;

	@Override
	protected void addArgs() {
		args.add(new Arg<>(this::getBaseUri, null, baseUriDefault, baseUriId, "", baseUriDescription, null));
		args.add(new Arg<>(this::isHttpsProxy, null, httpsProxyDefault, httpsProxyId, "", httpsProxyDescription, null));
		args.add(new Arg<>(this::getFilesFilename, null, filesDefault, filesId, "", filesDescription, null));
		args.add(new Arg<>(this::getFetchingThreads, null, 0, null, fetchingThreadsDefault, fetchingThreadsId, "", fetchingThreadsDescription, null));
	}

	@Override
	public String getId() {
		return "serverPrivateArgs";
	}

	public String getBaseUri() {
		return baseUri;
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

	public Integer getFetchingThreads() {
		return fetchingThreads;
	}
}
