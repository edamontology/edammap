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

package org.edamontology.edammap.core.output;

import org.edamontology.pubfetcher.core.db.DatabaseEntry;
import org.edamontology.pubfetcher.core.db.DatabaseEntryType;

public class DatabaseEntryEntry {

	private final DatabaseEntry<?> entry;

	private final DatabaseEntryType type;

	public DatabaseEntryEntry(DatabaseEntry<?> entry, DatabaseEntryType type) {
		this.entry = entry;
		this.type = type;
	}

	public DatabaseEntry<?> getEntry() {
		return entry;
	}

	public DatabaseEntryType getType() {
		return type;
	}
}
