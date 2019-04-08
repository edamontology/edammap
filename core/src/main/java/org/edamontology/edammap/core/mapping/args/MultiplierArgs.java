/*
 * Copyright © 2016, 2019 Erik Jaaniso
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

import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.Args;

import com.beust.jcommander.Parameter;

public class MultiplierArgs extends Args {

	private static final String labelMultiplierId = "labelMultiplier";
	private static final String labelMultiplierDescription = "Score multiplier for matching a concept label. Set to 0 to disable matching of labels.";
	private static final Double labelMultiplierDefault = 1.0;
	@Parameter(names = { "--" + labelMultiplierId }, validateWith = ZeroToOneDouble.class, description = labelMultiplierDescription)
	private Double labelMultiplier = labelMultiplierDefault;

	private static final String exactSynonymMultiplierId = "exactSynonymMultiplier";
	private static final String exactSynonymMultiplierDescription = "Score multiplier for matching a concept exact synonym. Set to 0 to disable matching of exact synonyms.";
	private static final Double exactSynonymMultiplierDefault = 1.0;
	@Parameter(names = { "--" + exactSynonymMultiplierId }, validateWith = ZeroToOneDouble.class, description = exactSynonymMultiplierDescription)
	private Double exactSynonymMultiplier = exactSynonymMultiplierDefault;

	private static final String narrowBroadSynonymMultiplierId = "narrowBroadSynonymMultiplier";
	private static final String narrowBroadSynonymMultiplierDescription = "Score multiplier for matching a concept narrow or broad synonym. Set to 0 to disable matching of narrow and broad synonyms.";
	private static final Double narrowBroadSynonymMultiplierDefault = 1.0;
	@Parameter(names = { "--" + narrowBroadSynonymMultiplierId }, validateWith = ZeroToOneDouble.class, description = narrowBroadSynonymMultiplierDescription)
	private Double narrowBroadSynonymMultiplier = narrowBroadSynonymMultiplierDefault;

	private static final String definitionMultiplierId = "definitionMultiplier";
	private static final String definitionMultiplierDescription = "Score multiplier for matching a concept definition. Set to 0 to disable matching of definitions.";
	private static final Double definitionMultiplierDefault = 1.0;
	@Parameter(names = { "--" + definitionMultiplierId }, validateWith = ZeroToOneDouble.class, description = definitionMultiplierDescription)
	private Double definitionMultiplier = definitionMultiplierDefault;

	private static final String commentMultiplierId = "commentMultiplier";
	private static final String commentMultiplierDescription = "Score multiplier for matching a concept comment. Set to 0 to disable matching of comments.";
	private static final Double commentMultiplierDefault = 1.0;
	@Parameter(names = { "--" + commentMultiplierId }, validateWith = ZeroToOneDouble.class, description = commentMultiplierDescription)
	private Double commentMultiplier = commentMultiplierDefault;

	@Override
	protected void addArgs() {
		args.add(new Arg<>(this::getLabelMultiplier, this::setLabelMultiplier, labelMultiplierDefault, 0.0, 1.0, labelMultiplierId, "Label multiplier", labelMultiplierDescription, null));
		args.add(new Arg<>(this::getExactSynonymMultiplier, this::setExactSynonymMultiplier, exactSynonymMultiplierDefault, 0.0, 1.0, exactSynonymMultiplierId, "Exact synonym multiplier", exactSynonymMultiplierDescription, null));
		args.add(new Arg<>(this::getNarrowBroadSynonymMultiplier, this::setNarrowBroadSynonymMultiplier, narrowBroadSynonymMultiplierDefault, 0.0, 1.0, narrowBroadSynonymMultiplierId, "Narrow/Broad multiplier", narrowBroadSynonymMultiplierDescription, null));
		args.add(new Arg<>(this::getDefinitionMultiplier, this::setDefinitionMultiplier, definitionMultiplierDefault, 0.0, 1.0, definitionMultiplierId, "Definition multiplier", definitionMultiplierDescription, null));
		args.add(new Arg<>(this::getCommentMultiplier, this::setCommentMultiplier, commentMultiplierDefault, 0.0, 1.0, commentMultiplierId, "Comment multiplier", commentMultiplierDescription, null));
	}

	@Override
	public String getId() {
		return "multiplierArgs";
	}

	@Override
	public String getLabel() {
		return "Concept multipliers";
	}

	public Double getLabelMultiplier() {
		return labelMultiplier;
	}
	public void setLabelMultiplier(Double labelMultiplier) {
		this.labelMultiplier = labelMultiplier;
	}

	public Double getExactSynonymMultiplier() {
		return exactSynonymMultiplier;
	}
	public void setExactSynonymMultiplier(Double exactSynonymMultiplier) {
		this.exactSynonymMultiplier = exactSynonymMultiplier;
	}

	public Double getNarrowBroadSynonymMultiplier() {
		return narrowBroadSynonymMultiplier;
	}
	public void setNarrowBroadSynonymMultiplier(Double narrowBroadSynonymMultiplier) {
		this.narrowBroadSynonymMultiplier = narrowBroadSynonymMultiplier;
	}

	public Double getDefinitionMultiplier() {
		return definitionMultiplier;
	}
	public void setDefinitionMultiplier(Double definitionMultiplier) {
		this.definitionMultiplier = definitionMultiplier;
	}

	public Double getCommentMultiplier() {
		return commentMultiplier;
	}
	public void setCommentMultiplier(Double commentMultiplier) {
		this.commentMultiplier = commentMultiplier;
	}
}
