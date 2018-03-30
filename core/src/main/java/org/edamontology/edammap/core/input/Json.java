/*
 * Copyright © 2017 Erik Jaaniso
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

package org.edamontology.edammap.core.input;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.edamontology.edammap.core.input.json.Biotools;
import org.edamontology.edammap.core.query.QueryType;

public class Json {

	public static List<? extends InputType> load(String queryPath, QueryType type, int timeout, String userAgent) throws IOException, ParseException {
		List<? extends InputType> inputs = new ArrayList<>();

		try (InputStream is = Input.newInputStream(queryPath, true, timeout, userAgent)) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);

			switch (type) {
				case biotools: inputs = mapper.readValue(is, Biotools.class).getList(); break;
				default: break;
			}
		}

		int i = 0;
		for (InputType inputType : inputs) {
			inputType.check(++i);
		}

		return inputs;
	}
}
