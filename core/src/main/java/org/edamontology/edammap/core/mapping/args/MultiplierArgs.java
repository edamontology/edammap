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

	public static final String LABEL_MULTIPLIER = "labelMultiplier";
	@Parameter(names = { "--" + LABEL_MULTIPLIER }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a concept label. Set to 0 to disable matching of labels.")
	private double labelMultiplier = 1.0;

	public static final String EXACT_SYNONYM_MULTIPLIER = "exactSynonymMultiplier";
	@Parameter(names = { "--" + EXACT_SYNONYM_MULTIPLIER }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a concept exact synonym. Set to 0 to disable matching of exact synonyms.")
	private double exactSynonymMultiplier = 1.0;

	public static final String NARROW_BROAD_SYNONYM_MULTIPLIER = "narrowBroadSynonymMultiplier";
	@Parameter(names = { "--" + NARROW_BROAD_SYNONYM_MULTIPLIER }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a concept narrow or broad synonym. Set to 0 to disable matching of narrow and broad synonyms.")
	private double narrowBroadSynonymMultiplier = 1.0;

	public static final String DEFINITION_MULTIPLIER = "definitionMultiplier";
	@Parameter(names = { "--" + DEFINITION_MULTIPLIER }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a concept definition. Set to 0 to disable matching of definitions.")
	private double definitionMultiplier = 1.0;

	public static final String COMMENT_MULTIPLIER = "commentMultiplier";
	@Parameter(names = { "--" + COMMENT_MULTIPLIER }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a concept comment. Set to 0 to disable matching of comments.")
	private double commentMultiplier = 1.0;

	public double getLabelMultiplier() {
		return labelMultiplier;
	}
	public void setLabelMultiplier(double labelMultiplier) {
		this.labelMultiplier = labelMultiplier;
	}

	public double getExactSynonymMultiplier() {
		return exactSynonymMultiplier;
	}
	public void setExactSynonymMultiplier(double exactSynonymMultiplier) {
		this.exactSynonymMultiplier = exactSynonymMultiplier;
	}

	public double getNarrowBroadSynonymMultiplier() {
		return narrowBroadSynonymMultiplier;
	}
	public void setNarrowBroadSynonymMultiplier(double narrowBroadSynonymMultiplier) {
		this.narrowBroadSynonymMultiplier = narrowBroadSynonymMultiplier;
	}

	public double getDefinitionMultiplier() {
		return definitionMultiplier;
	}
	public void setDefinitionMultiplier(double definitionMultiplier) {
		this.definitionMultiplier = definitionMultiplier;
	}

	public double getCommentMultiplier() {
		return commentMultiplier;
	}
	public void setCommentMultiplier(double commentMultiplier) {
		this.commentMultiplier = commentMultiplier;
	}
}
