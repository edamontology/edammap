/*
 * Copyright © 2016 Erik Jaaniso
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

package org.edamontology.edammap.core.edam;

import java.util.Locale;

import org.edamontology.pubfetcher.IllegalRequestException;

public class EdamUri {

	private String uri;

	private Branch branch;

	private int nr;

	public EdamUri(String uri, String prefix) {
		if (!isEdamUri(uri, prefix)) {
			throw new IllegalRequestException("Illegal EDAM URI: " + uri);
		}

		this.uri = uri;

		String[] branch_nr = uri.substring(prefix.length() + 1).split("_", 2);
		branch = Branch.valueOf(branch_nr[0]);
		nr = Integer.parseInt(branch_nr[1]);
	}

	public static boolean isEdamUri(String uri, String prefix) {
		if (uri == null || uri.isEmpty()) return false;
		if (!uri.startsWith(prefix)) return false;
		if (!(uri.length() > prefix.length()) || uri.charAt(prefix.length()) != '/') return false;

		String[] branch_nr = uri.substring(prefix.length() + 1).split("_", 2);
		try {
			Branch.valueOf(branch_nr[0]);
			int nr = Integer.parseInt(branch_nr[1]);
			if (nr < 0) return false;
		} catch (RuntimeException e) {
			return false;
		}

		return true;
	}

	public String getUri() {
		return uri;
	}

	public Branch getBranch() {
		return branch;
	}

	public int getNr() {
		return nr;
	}

	public String getNrString() {
		return String.format(Locale.ROOT, "%04d", nr);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof EdamUri)) return false;
		EdamUri other = (EdamUri)obj;
		if (uri == null) {
			if (other.uri != null) return false;
		} else if (!uri.equals(other.uri)) return false;
		return other.canEqual(this);
	}

	@Override
	public int hashCode() {
		if (uri == null) return 0;
		return uri.hashCode();
	}

	public boolean canEqual(Object other) {
		return (other instanceof EdamUri);
	}

	@Override
	public String toString() {
		if (uri == null) return "";
		return uri;
	}
}
