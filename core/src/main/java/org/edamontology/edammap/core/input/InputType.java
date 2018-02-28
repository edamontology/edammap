/*
 * Copyright © 2016, 2017 Erik Jaaniso
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

import java.text.ParseException;

public interface InputType {
	public void check(int i) throws ParseException;

	default void parseException(String attribute, int i, String index) throws ParseException {
		throw new ParseException("Attribute \"" + attribute + "\" missing or empty! (record " + index + ")", i);
	}

	default void parseException(String attribute, int i) throws ParseException {
		parseException(attribute, i, String.valueOf(i));
	}
}
