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

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.edamontology.pubfetcher.core.common.IllegalRequestException;
import org.edamontology.pubfetcher.core.common.PubFetcher;
import org.glassfish.grizzly.http.server.util.HtmlHelper;

@Provider
public class IllegalRequestExceptionMapper implements ExceptionMapper<IllegalRequestException> {

	@Context
	private HttpHeaders headers;

	@Override
	public Response toResponse(IllegalRequestException e) {
		boolean json = ExceptionCommon.isJson(headers);
		return Response.status(Status.BAD_REQUEST)
			.entity(json ?
				ExceptionCommon.toJson(Status.BAD_REQUEST, e.getMessage()) :
				HtmlHelper.getErrorPage("IllegalRequestException", PubFetcher.escapeHtml(e.getMessage()) + "<br>" + ExceptionCommon.time(), Server.version.getName() + " " + Server.version.getVersion()))
			.type(json ? MediaType.APPLICATION_JSON : MediaType.TEXT_HTML + ";charset=utf-8").build();
	}
}
