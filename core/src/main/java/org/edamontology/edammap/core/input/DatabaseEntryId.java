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

package org.edamontology.edammap.core.input;

import org.edamontology.pubfetcher.core.db.DatabaseEntryType;

public class DatabaseEntryId {

	private final Object id;

	private final DatabaseEntryType type;

	public DatabaseEntryId(Object id, DatabaseEntryType type) {
		this.id = id;
		this.type = type;
	}

	public Object getId() {
		return id;
	}

	public DatabaseEntryType getType() {
		return type;
	}
}
