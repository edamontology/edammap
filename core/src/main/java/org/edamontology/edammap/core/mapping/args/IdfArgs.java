/*
 * Copyright © 2016, 2018, 2019 Erik Jaaniso
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

import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.Args;

public class IdfArgs extends Args {

	private static final String conceptIdfScalingId = "conceptIdfScaling";
	private static final String conceptIdfScalingDescription = "Set to 0 to disable concept IDF. Setting to 1 means linear IDF weighting.";
	private static final Double conceptIdfScalingDefault = 0.5;
	@Parameter(names = { "--" + conceptIdfScalingId }, validateWith = PositiveDouble.class, description = conceptIdfScalingDescription)
	private Double conceptIdfScaling = conceptIdfScalingDefault;

	private static final String queryIdfScalingId = "queryIdfScaling";
	private static final String queryIdfScalingDescription = "Set to 0 to disable query IDF. Setting to 1 means linear IDF weighting.";
	private static final Double queryIdfScalingDefault = 0.5;
	@Parameter(names = { "--" + queryIdfScalingId }, validateWith = PositiveDouble.class, description = queryIdfScalingDescription)
	private Double queryIdfScaling = queryIdfScalingDefault;

	private static final String labelSynonymsIdfId = "labelSynonymsIdf";
	private static final String labelSynonymsIdfDescription = "IDF weighting for concept label and synonyms";
	private static final Boolean labelSynonymsIdfDefault = false;
	@Parameter(names = { "--" + labelSynonymsIdfId }, arity = 1, description = labelSynonymsIdfDescription)
	private Boolean labelSynonymsIdf = labelSynonymsIdfDefault;

	private static final String nameKeywordsIdfId = "nameKeywordsIdf";
	private static final String nameKeywordsIdfDescription = "IDF weighting for query name and keywords";
	private static final Boolean nameKeywordsIdfDefault = true;
	@Parameter(names = { "--" + nameKeywordsIdfId }, arity = 1, description = nameKeywordsIdfDescription)
	private Boolean nameKeywordsIdf = nameKeywordsIdfDefault;

	private static final String descriptionIdfId = "descriptionIdf";
	private static final String descriptionIdfDescription = "IDF weighting for query description";
	private static final Boolean descriptionIdfDefault = true;
	@Parameter(names = { "--" + descriptionIdfId }, arity = 1, description = descriptionIdfDescription)
	private Boolean descriptionIdf = descriptionIdfDefault;

	private static final String titleKeywordsIdfId = "titleKeywordsIdf";
	private static final String titleKeywordsIdfDescription = "IDF weighting for publication title and keywords";
	private static final Boolean titleKeywordsIdfDefault = true;
	@Parameter(names = { "--" + titleKeywordsIdfId }, arity = 1, description = titleKeywordsIdfDescription)
	private Boolean titleKeywordsIdf = titleKeywordsIdfDefault;

	private static final String abstractIdfId = "abstractIdf";
	private static final String abstractIdfDescription = "IDF weighting for publication abstract";
	private static final Boolean abstractIdfDefault = true;
	@Parameter(names = { "--" + abstractIdfId }, arity = 1, description = abstractIdfDescription)
	private Boolean abstractIdf = abstractIdfDefault;

	@Override
	protected void addArgs() {
		args.add(new Arg<>(this::getConceptIdfScaling, this::setConceptIdfScaling, conceptIdfScalingDefault, 0.0, null, conceptIdfScalingId, "Concept IDF scaling", conceptIdfScalingDescription, null));
		args.add(new Arg<>(this::getQueryIdfScaling, this::setQueryIdfScaling, queryIdfScalingDefault, 0.0, null, queryIdfScalingId, "Query IDF scaling", queryIdfScalingDescription, null));
		args.add(new Arg<>(this::isLabelSynonymsIdf, this::setLabelSynonymsIdf, labelSynonymsIdfDefault, labelSynonymsIdfId, "Label/Synonyms IDF", labelSynonymsIdfDescription, null));
		args.add(new Arg<>(this::isNameKeywordsIdf, this::setNameKeywordsIdf, nameKeywordsIdfDefault, nameKeywordsIdfId, "Name/Keywords IDF", nameKeywordsIdfDescription, null));
		args.add(new Arg<>(this::isDescriptionIdf, this::setDescriptionIdf, descriptionIdfDefault, descriptionIdfId, "Description IDF", descriptionIdfDescription, null));
		args.add(new Arg<>(this::isTitleKeywordsIdf, this::setTitleKeywordsIdf, titleKeywordsIdfDefault, titleKeywordsIdfId, "Title/Keywords IDF", titleKeywordsIdfDescription, null));
		args.add(new Arg<>(this::isAbstractIdf, this::setAbstractIdf, abstractIdfDefault, abstractIdfId, "Abstract IDF", abstractIdfDescription, null));
	}

	@Override
	public String getId() {
		return "idfArgs";
	}

	@Override
	public String getLabel() {
		return "IDF";
	}

	public Double getConceptIdfScaling() {
		return conceptIdfScaling;
	}
	public void setConceptIdfScaling(Double conceptIdfScaling) {
		this.conceptIdfScaling = conceptIdfScaling;
	}

	public Double getQueryIdfScaling() {
		return queryIdfScaling;
	}
	public void setQueryIdfScaling(Double queryIdfScaling) {
		this.queryIdfScaling = queryIdfScaling;
	}

	public Boolean isLabelSynonymsIdf() {
		return labelSynonymsIdf;
	}
	public void setLabelSynonymsIdf(Boolean labelSynonymsIdf) {
		this.labelSynonymsIdf = labelSynonymsIdf;
	}

	public Boolean isNameKeywordsIdf() {
		return nameKeywordsIdf;
	}
	public void setNameKeywordsIdf(Boolean nameKeywordsIdf) {
		this.nameKeywordsIdf = nameKeywordsIdf;
	}

	public Boolean isDescriptionIdf() {
		return descriptionIdf;
	}
	public void setDescriptionIdf(Boolean descriptionIdf) {
		this.descriptionIdf = descriptionIdf;
	}

	public Boolean isTitleKeywordsIdf() {
		return titleKeywordsIdf;
	}
	public void setTitleKeywordsIdf(Boolean titleKeywordsIdf) {
		this.titleKeywordsIdf = titleKeywordsIdf;
	}

	public Boolean isAbstractIdf() {
		return abstractIdf;
	}
	public void setAbstractIdf(Boolean abstractIdf) {
		this.abstractIdf = abstractIdf;
	}
}
