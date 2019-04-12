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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.edamontology.pubfetcher.core.common.PubFetcher;
import org.glassfish.grizzly.http.server.util.HtmlHelper;

public class ParamException extends WebApplicationException {

	private static final long serialVersionUID = 7240254492703489733L;

	private static String toText(String key, String value, String reason) {
		return "Param \"" + key + "=" + value + "\" " + reason;
	}

	private static String toTextJson(String key, String value, String reason) {
		return "Param '" + key + "=" + value + "' " + reason;
	}

	ParamException(String key, String value, String reason, boolean json) {
		super(Response.status(Status.BAD_REQUEST)
			.entity(json ?
				ExceptionCommon.toJson(Status.BAD_REQUEST, toTextJson(key, value, reason)) :
				HtmlHelper.getErrorPage("ParamException", PubFetcher.escapeHtml(toText(key, value, reason)) + "<br>" + ExceptionCommon.time(), Server.version.getName() + " " + Server.version.getVersion()))
			.type(json ? MediaType.APPLICATION_JSON : MediaType.TEXT_HTML + ";charset=utf-8").build());
	}
}
