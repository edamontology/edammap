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

package org.edamontology.edammap.core.mapping.args;

import org.edamontology.edammap.core.args.ZeroToOneDouble;

import com.beust.jcommander.Parameter;

public class MultiplierArgs {
	@Parameter(names = { "--label-multiplier" }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a concept label. Set to 0 to disable matching of labels.")
	private double labelMultiplier = 1;

	@Parameter(names = { "--exact-synonym-multiplier" }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a concept exact synonym. Set to 0 to disable matching of exact synonyms.")
	private double exactSynonymMultiplier = 1;

	@Parameter(names = { "--narrow-broad-synonym-multiplier" }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a concept narrow or broad synonym. Set to 0 to disable matching of narrow and broad synonyms.")
	private double narrowBroadMultiplier = 1;

	@Parameter(names = { "--definition-multiplier" }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a concept definition. Set to 0 to disable matching of definitions.")
	private double definitionMultiplier = 1;

	@Parameter(names = { "--comment-multiplier" }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a concept comment. Set to 0 to disable matching of comments.")
	private double commentMultiplier = 1;

	public double getLabelMultiplier() {
		return labelMultiplier;
	}

	public double getExactSynonymMultiplier() {
		return exactSynonymMultiplier;
	}

	public double getNarrowBroadMultiplier() {
		return narrowBroadMultiplier;
	}

	public double getDefinitionMultiplier() {
		return definitionMultiplier;
	}

	public double getCommentMultiplier() {
		return commentMultiplier;
	}
}
