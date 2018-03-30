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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.edamontology.pubfetcher.FetcherCommon;

public final class Input {

	private static Logger logger = LogManager.getLogger();

	public static boolean isProtocol(String path) {
		String pathLower = path.toLowerCase(Locale.ROOT);
		if (pathLower.startsWith("http://") || pathLower.startsWith("https://") || pathLower.startsWith("ftp://")) {
			return true;
		} else {
			return false;
		}
	}

	public static InputStream newInputStream(String path, boolean allowFile, int timeout, String userAgent) throws IOException {
		InputStream is;
		if (isProtocol(path)) {
			URLConnection con = FetcherCommon.newConnection(path, timeout, userAgent);
			is = con.getInputStream();
			logger.info("Opened URL {}", con.getURL().toString());
		} else if (allowFile) {
			is = new FileInputStream(path);
			logger.info("Opened file {}", path); // TODO new File(path).getName()
		} else {
			throw new IOException("Unsupported protocol or opening of local files not allowed: " + path); // TODO new File(path).getName()
		}
		return is;
	}
}
