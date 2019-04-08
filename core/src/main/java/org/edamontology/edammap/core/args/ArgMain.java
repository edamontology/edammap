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

package org.edamontology.edammap.core.args;

import org.edamontology.pubfetcher.core.common.Arg;

public class ArgMain {

	private final Object value;

	private final Arg<?, ?> arg;

	private final boolean input;

	public ArgMain(Object value, Arg<?, ?> arg, boolean input) {
		this.value = value;
		this.arg = arg;
		this.input = input;
	}

	public Object getValue() {
		return value;
	}

	public Arg<?, ?> getArg() {
		return arg;
	}

	public boolean isInput() {
		return input;
	}
}
