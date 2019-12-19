/*
 * Copyright © 2016, 2018 Erik Jaaniso
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

package org.edamontology.edammap.cli;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.edamontology.pubfetcher.core.common.BasicArgs;
import org.edamontology.pubfetcher.core.common.Version;

public class CliMain {

	private static Logger logger;

	public static void main(String[] argv) throws IOException, ReflectiveOperationException {
		Version version = new Version(CliMain.class);

		CliArgs args = BasicArgs.parseArgs(argv, CliArgs.class, version, false);

		// logger must be called only after configuration changes have been made in BasicArgs.parseArgs()
		// otherwise invalid.log will be created if arg --log is null
		logger = LogManager.getLogger();
		logger.debug(String.join(" ", argv));
		logger.info("This is {} {} ({})", version.getName(), version.getVersion(), version.getUrl());

		int status;
		try {
			Cli.run(args, version, false);
			status = 0;
		} catch (Throwable e) {
			logger.error("Exception!", e);
			status = 1;
		} finally {
			Cli.closeDatabase();
		}

		if (status != 0) {
			System.exit(status);
		}
	}
}
