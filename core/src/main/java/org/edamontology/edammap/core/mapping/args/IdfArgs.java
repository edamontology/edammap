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

	public static final String CONCEPT_IDF_SCALING = "conceptIdfScaling";
	@Parameter(names = { "--" + CONCEPT_IDF_SCALING }, validateWith = PositiveDouble.class, description = "Set to 0 to disable concept IDF. Setting to 1 means linear IDF weighting.")
	private double conceptIdfScaling = 0.5;

	public static final String QUERY_IDF_SCALING = "queryIdfScaling";
	@Parameter(names = { "--" + QUERY_IDF_SCALING }, validateWith = PositiveDouble.class, description = "Set to 0 to disable query IDF. Setting to 1 means linear IDF weighting.")
	private double queryIdfScaling = 0.5;

	public static final String LABEL_SYNONYMS_IDF = "labelSynonymsIdf";
	@Parameter(names = { "--" + LABEL_SYNONYMS_IDF }, arity = 1, description = "IDF weighting for concept label and synonyms")
	private boolean labelSynonymsIdf = false;

	public static final String NAME_KEYWORDS_IDF = "nameKeywordsIdf";
	@Parameter(names = { "--" + NAME_KEYWORDS_IDF }, arity = 1, description = "IDF weighting for query name and keywords")
	private boolean nameKeywordsIdf = true;

	public static final String DESCRIPTION_IDF = "descriptionIdf";
	@Parameter(names = { "--" + DESCRIPTION_IDF }, arity = 1, description = "IDF weighting for query description")
	private boolean descriptionIdf = true;

	public static final String TITLE_KEYWORDS_IDF = "titleKeywordsIdf";
	@Parameter(names = { "--" + TITLE_KEYWORDS_IDF }, arity = 1, description = "IDF weighting for publication title and keywords")
	private boolean titleKeywordsIdf = true;

	public static final String ABSTRACT_IDF = "abstractIdf";
	@Parameter(names = { "--" + ABSTRACT_IDF }, arity = 1, description = "IDF weighting for publication abstract")
	private boolean abstractIdf = true;

	public double getConceptIdfScaling() {
		return conceptIdfScaling;
	}
	public void setConceptIdfScaling(double conceptIdfScaling) {
		this.conceptIdfScaling = conceptIdfScaling;
	}

	public double getQueryIdfScaling() {
		return queryIdfScaling;
	}
	public void setQueryIdfScaling(double queryIdfScaling) {
		this.queryIdfScaling = queryIdfScaling;
	}

	public boolean isLabelSynonymsIdf() {
		return labelSynonymsIdf;
	}
	public void setLabelSynonymsIdf(boolean labelSynonymsIdf) {
		this.labelSynonymsIdf = labelSynonymsIdf;
	}

	public boolean isNameKeywordsIdf() {
		return nameKeywordsIdf;
	}
	public void setNameKeywordsIdf(boolean nameKeywordsIdf) {
		this.nameKeywordsIdf = nameKeywordsIdf;
	}

	public boolean isDescriptionIdf() {
		return descriptionIdf;
	}
	public void setDescriptionIdf(boolean descriptionIdf) {
		this.descriptionIdf = descriptionIdf;
	}

	public boolean isTitleKeywordsIdf() {
		return titleKeywordsIdf;
	}
	public void setTitleKeywordsIdf(boolean titleKeywordsIdf) {
		this.titleKeywordsIdf = titleKeywordsIdf;
	}

	public boolean isAbstractIdf() {
		return abstractIdf;
	}
	public void setAbstractIdf(boolean abstractIdf) {
		this.abstractIdf = abstractIdf;
	}
}
