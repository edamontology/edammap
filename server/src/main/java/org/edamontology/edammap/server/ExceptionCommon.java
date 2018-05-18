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

import java.io.StringWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

public final class ExceptionCommon {

	static String time() {
		return Instant.ofEpochMilli(System.currentTimeMillis()).toString();
	}

	static String toJson(Status status, String message) {
		StringWriter result = new StringWriter();
		Map<String, Object> config = new HashMap<>();
		config.put(JsonGenerator.PRETTY_PRINTING, true);
		JsonGenerator generator = Json.createGeneratorFactory(config).createGenerator(result);
		generator.writeStartObject();
		generator.write("success", false);
		if (status != null) {
			generator.write("status", status.getStatusCode());
		}
		if (message != null) {
			generator.write("message", message);
		}
		generator.write("time", time());
		generator.writeEnd();
		generator.close();
		return result.toString();
	}

	static boolean isJson(HttpHeaders headers) {
		boolean json = headers.getMediaType().getType().equalsIgnoreCase(MediaType.APPLICATION_JSON_TYPE.getType())
			&& headers.getMediaType().getSubtype().equalsIgnoreCase(MediaType.APPLICATION_JSON_TYPE.getSubtype());
		if (!json) {
			for (MediaType mediaType : headers.getAcceptableMediaTypes()) {
				if (mediaType.getType().equalsIgnoreCase(MediaType.APPLICATION_JSON_TYPE.getType())
						&& mediaType.getSubtype().equalsIgnoreCase(MediaType.APPLICATION_JSON_TYPE.getSubtype())) {
					json = true;
					break;
				}
			}
		}
		return json;
	}
}
