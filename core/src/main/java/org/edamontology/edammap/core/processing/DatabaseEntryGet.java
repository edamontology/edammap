/*
 * Copyright © 2019 Erik Jaaniso
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

package org.edamontology.edammap.core.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.edamontology.pubfetcher.core.common.FetcherArgs;
import org.edamontology.pubfetcher.core.common.PubFetcher;
import org.edamontology.pubfetcher.core.db.Database;
import org.edamontology.pubfetcher.core.db.DatabaseEntry;
import org.edamontology.pubfetcher.core.db.publication.PublicationIds;
import org.edamontology.pubfetcher.core.fetching.Fetcher;

import org.edamontology.edammap.core.input.DatabaseEntryId;
import org.edamontology.edammap.core.output.DatabaseEntryEntry;

public class DatabaseEntryGet {

	private static final Logger logger = LogManager.getLogger();

	private Object lock = new Object();

	private Database db;
	private Fetcher fetcher;
	private FetcherArgs fetcherArgs;

	private boolean lockDone;
	private int numThreads;
	private int index;

	private List<DatabaseEntryId> databaseEntryIds;
	private long startMillis;
	private List<DatabaseEntryEntry> databaseEntries;

	public List<DatabaseEntryEntry> getDatabaseEntries(Database db, Fetcher fetcher, FetcherArgs fetcherArgs, List<DatabaseEntryId> ids, long start, int threads) {
		if (ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}

		this.db = db;
		this.fetcher = fetcher;
		this.fetcherArgs = fetcherArgs;

		lockDone = false;
		numThreads = 0;
		index = 0;

		databaseEntryIds = ids;
		startMillis = start;
		databaseEntries = new ArrayList<>(ids.size());
		for (int i = 0; i < ids.size(); ++i) {
			databaseEntries.add(null);
		}

		logger.info("Get {} database entries", ids.size());

		for (int i = 0; i < threads && i < ids.size(); ++i) {
			Thread t = new Thread(new DatabaseEntryThread());
			t.setDaemon(true);
			t.start();
		}

		synchronized(lock) {
			while (!lockDone || numThreads > 0) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					logger.error("Exception!", e);
					break;
				}
			}
		}

		long nullCount = databaseEntries.stream().filter(e -> e.getEntry() == null).count();
		if (nullCount != 0) {
			logger.warn("Got {} database entries (instead of {})", databaseEntries.size() - nullCount, ids.size());
		} else {
			logger.info("Got {} database entries", databaseEntries.size());
		}

		return databaseEntries;
	}

	public class DatabaseEntryThread implements Runnable {

		@Override
		public void run() {
			synchronized(lock) {
				++numThreads;
				lockDone = true;
			}
			try {
				while (true) {
					DatabaseEntryId id;
					int localIndex;
					synchronized(databaseEntryIds) {
						if (index >= databaseEntryIds.size()) {
							break;
						}
						id = databaseEntryIds.get(index);
						localIndex = index;
						++index;
					}

					logger.info("Fetch {} {}", id.getType(), PubFetcher.progress(localIndex + 1, databaseEntryIds.size(), startMillis));

					DatabaseEntry<? extends DatabaseEntry<?>> databaseEntry = null;
					switch (id.getType()) {
						case publication: databaseEntry = PubFetcher.getPublication((PublicationIds) id.getId(), db, fetcher, null, fetcherArgs); break;
						case webpage: databaseEntry = PubFetcher.getWebpage((String) id.getId(), db, fetcher, fetcherArgs); break;
						case doc: databaseEntry = PubFetcher.getDoc((String) id.getId(), db, fetcher, fetcherArgs); break;
					}

					synchronized(databaseEntries) {
						databaseEntries.set(localIndex, new DatabaseEntryEntry(databaseEntry, id.getType()));
					}
				}
			} finally {
				synchronized(lock) {
					--numThreads;
					lock.notifyAll();
				}
			}
		}
	}
}
