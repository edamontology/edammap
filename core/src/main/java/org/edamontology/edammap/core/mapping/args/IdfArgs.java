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

package org.edamontology.edammap.core.mapping.args;

import com.beust.jcommander.Parameter;

import org.edamontology.edammap.core.args.PositiveDouble;

public class IdfArgs {
	@Parameter(names = { "--concept-idf-scaling" }, validateWith = PositiveDouble.class, description = "Set to 0 to disable concept IDF. Setting to 1 means linear IDF weighting.")
	private double conceptIdfScaling = 0.5;

	@Parameter(names = { "--query-idf-scaling" }, validateWith = PositiveDouble.class, description = "Set to 0 to disable query IDF. Setting to 1 means linear IDF weighting.")
	private double queryIdfScaling = 0.5;

	@Parameter(names = { "--label-synonyms-idf" }, arity = 1, description = "IDF weighting for concept label and synonyms")
	private boolean labelSynonymsIdf = false;

	@Parameter(names = { "--name-keywords-idf" }, arity = 1, description = "IDF weighting for query name and keywords")
	private boolean nameKeywordsIdf = true;

	@Parameter(names = { "--description-idf" }, arity = 1, description = "IDF weighting for query description")
	private boolean descriptionIdf = true;

	@Parameter(names = { "--title-keywords-idf" }, arity = 1, description = "IDF weighting for publication title and keywords")
	private boolean titleKeywordsIdf = true;

	@Parameter(names = { "--abstract-idf" }, arity = 1, description = "IDF weighting for publication abstract")
	private boolean abstractIdf = true;

	public double getConceptIdfScaling() {
		return conceptIdfScaling;
	}

	public double getQueryIdfScaling() {
		return queryIdfScaling;
	}

	public boolean isLabelSynonymsIdf() {
		return labelSynonymsIdf;
	}

	public boolean isNameKeywordsIdf() {
		return nameKeywordsIdf;
	}

	public boolean isDescriptionIdf() {
		return descriptionIdf;
	}

	public boolean isTitleKeywordsIdf() {
		return titleKeywordsIdf;
	}

	public boolean isAbstractIdf() {
		return abstractIdf;
	}
}
