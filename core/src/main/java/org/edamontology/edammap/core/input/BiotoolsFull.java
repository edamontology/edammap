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
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.edamontology.pubfetcher.core.common.FetcherArgs;
import org.edamontology.pubfetcher.core.common.PubFetcher;

import org.edamontology.edammap.core.input.json.Biotools;

public final class BiotoolsFull {

	private static final Logger logger = LogManager.getLogger();

	public static int get(String outputPath, FetcherArgs fetcherArgs, boolean dev, boolean stderr) throws IOException {
		logger.info("Make full {}bio.tools JSON to {}", dev ? "dev." : "", outputPath);

		String api = "https://" + (dev ? "dev." : "") + "bio.tools/api/tool";

		Path output = PubFetcher.outputPath(outputPath);

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);

		Biotools biotoolsFull = new Biotools();
		int count = 0;

		boolean error = false;

		String next = "?page=1";
		int page = 0;
		long start = System.currentTimeMillis();
		while (next != null) {
			++page;
			try (InputStream is = Input.newInputStream(api + next + "&format=json", false, fetcherArgs.getTimeout(), fetcherArgs.getPrivateArgs().getUserAgent())) {
				Biotools biotools = mapper.readValue(is, Biotools.class);

				if (stderr) {
					System.err.print(PubFetcher.progress(page, biotools.getCount() / 10 + 1, start) + "  \r");
				}

				biotoolsFull.addTools(biotools.getList());
				biotoolsFull.setCount(biotools.getCount());
				count += biotools.getList().size();

				next = biotools.getNext();
			} catch (Exception e) {
				logger.error("Exception at " + next + "!", e);
				error = true;
				break;
			}
		}

		mapper.writeValue(output.toFile(), biotoolsFull);

		if (count != biotoolsFull.getCount()) {
			logger.error("Got {} {}bio.tools entries instead of advertised {}", count, dev ? "dev." : "", biotoolsFull.getCount());
		}
		logger.info("Made {}bio.tools JSON with {} entries", dev ? "dev." : "", count);

		if (count != biotoolsFull.getCount()) {
			throw new RuntimeException("Got " + count + " " + (dev ? "dev." : "") + "bio.tools entries instead of advertised " + biotoolsFull.getCount());
		} else if (error) {
			throw new RuntimeException("Error getting full " + (dev ? "dev." : "") + "bio.tools content");
		}

		return count;
	}
}
