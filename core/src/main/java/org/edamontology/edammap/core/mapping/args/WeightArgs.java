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

import com.beust.jcommander.Parameter;

import org.edamontology.edammap.core.args.PositiveDouble;

import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.Args;

public class WeightArgs extends Args {

	private static final String averageScalingId = "averageScaling";
	private static final String averageScalingDescription = "Scaling for the average strategy";
	private static final Double averageScalingDefault = 10.0;
	@Parameter(names = { "--" + averageScalingId }, validateWith = PositiveDouble.class, description = averageScalingDescription)
	private Double averageScaling = averageScalingDefault;

	private static final String nameWeightId = "nameWeight";
	private static final String nameWeightDescription = "Weight of query name in average strategy. Set to 0 to disable matching of names in average strategy.";
	private static final Double nameWeightDefault = 1.0;
	@Parameter(names = { "--" + nameWeightId }, validateWith = PositiveDouble.class, description = nameWeightDescription)
	private Double nameWeight = nameWeightDefault;

	private static final String keywordWeightId = "keywordWeight";
	private static final String keywordWeightDescription = "Weight of query keyword in average strategy. Set to 0 to disable matching of keywords in average strategy.";
	private static final Double keywordWeightDefault = 1.0;
	@Parameter(names = { "--" + keywordWeightId }, validateWith = PositiveDouble.class, description = keywordWeightDescription)
	private Double keywordWeight = keywordWeightDefault;

	private static final String descriptionWeightId = "descriptionWeight";
	private static final String descriptionWeightDescription = "Weight of query description in average strategy. Set to 0 to disable matching of descriptions in average strategy.";
	private static final Double descriptionWeightDefault = 1.0;
	@Parameter(names = { "--" + descriptionWeightId }, validateWith = PositiveDouble.class, description = descriptionWeightDescription)
	private Double descriptionWeight = descriptionWeightDefault;

	private static final String publicationTitleWeightId = "publicationTitleWeight";
	private static final String publicationTitleWeightDescription = "Weight of publication title in average strategy. Set to 0 to disable matching of titles in average strategy.";
	private static final Double publicationTitleWeightDefault = 0.25;
	@Parameter(names = { "--" + publicationTitleWeightId }, validateWith = PositiveDouble.class, description = publicationTitleWeightDescription)
	private Double publicationTitleWeight = publicationTitleWeightDefault;

	private static final String publicationKeywordWeightId = "publicationKeywordWeight";
	private static final String publicationKeywordWeightDescription = "Weight of publication keyword in average strategy. Set to 0 to disable matching of keywords in average strategy.";
	private static final Double publicationKeywordWeightDefault = 0.75;
	@Parameter(names = { "--" + publicationKeywordWeightId }, validateWith = PositiveDouble.class, description = publicationKeywordWeightDescription)
	private Double publicationKeywordWeight = publicationKeywordWeightDefault;

	private static final String publicationMeshWeightId = "publicationMeshWeight";
	private static final String publicationMeshWeightDescription = "Weight of publication MeSH term in average strategy. Set to 0 to disable matching of MeSH terms in average strategy.";
	private static final Double publicationMeshWeightDefault = 0.25;
	@Parameter(names = { "--" + publicationMeshWeightId }, validateWith = PositiveDouble.class, description = publicationMeshWeightDescription)
	private Double publicationMeshWeight = publicationMeshWeightDefault;

	private static final String publicationMinedTermWeightId = "publicationMinedTermWeight";
	private static final String publicationMinedTermWeightDescription = "Weight of publication mined term (EFO, GO) in average strategy. Set to 0 to disable matching of mined terms in average strategy.";
	private static final Double publicationMinedTermWeightDefault = 0.25;
	@Parameter(names = { "--" + publicationMinedTermWeightId }, validateWith = PositiveDouble.class, description = publicationMinedTermWeightDescription)
	private Double publicationMinedTermWeight = publicationMinedTermWeightDefault;

	private static final String publicationAbstractWeightId = "publicationAbstractWeight";
	private static final String publicationAbstractWeightDescription = "Weight of publication abstract in average strategy. Set to 0 to disable matching of abstracts in average strategy.";
	private static final Double publicationAbstractWeightDefault = 0.75;
	@Parameter(names = { "--" + publicationAbstractWeightId }, validateWith = PositiveDouble.class, description = publicationAbstractWeightDescription)
	private Double publicationAbstractWeight = publicationAbstractWeightDefault;

	private static final String publicationFulltextWeightId = "publicationFulltextWeight";
	private static final String publicationFulltextWeightDescription = "Weight of publication fulltext in average strategy. Set to 0 to disable matching of fulltexts in average strategy.";
	private static final Double publicationFulltextWeightDefault = 0.5;
	@Parameter(names = { "--" + publicationFulltextWeightId }, validateWith = PositiveDouble.class, description = publicationFulltextWeightDescription)
	private Double publicationFulltextWeight = publicationFulltextWeightDefault;

	private static final String docWeightId = "docWeight";
	private static final String docWeightDescription = "Weight of query doc in average strategy. Set to 0 to disable matching of docs in average strategy.";
	private static final Double docWeightDefault = 0.5;
	@Parameter(names = { "--" + docWeightId }, validateWith = PositiveDouble.class, description = docWeightDescription)
	private Double docWeight = docWeightDefault;

	private static final String webpageWeightId = "webpageWeight";
	private static final String webpageWeightDescription = "Weight of query webpage in average strategy. Set to 0 to disable matching of webpages in average strategy.";
	private static final Double webpageWeightDefault = 0.5;
	@Parameter(names = { "--" + webpageWeightId }, validateWith = PositiveDouble.class, description = webpageWeightDescription)
	private Double webpageWeight = webpageWeightDefault;

	@Override
	protected void addArgs() {
		args.add(new Arg<>(this::getAverageScaling, this::setAverageScaling, averageScalingDefault, 0.0, null, averageScalingId, "Average strategy scaling", averageScalingDescription, null));
		args.add(new Arg<>(this::getNameWeight, this::setNameWeight, nameWeightDefault, 0.0, null, nameWeightId, "Name weight", nameWeightDescription, null));
		args.add(new Arg<>(this::getKeywordWeight, this::setKeywordWeight, keywordWeightDefault, 0.0, null, keywordWeightId, "Keyword weight",keywordWeightDescription, null));
		args.add(new Arg<>(this::getDescriptionWeight, this::setDescriptionWeight, descriptionWeightDefault, 0.0, null, descriptionWeightId, "Description weight", descriptionWeightDescription, null));
		args.add(new Arg<>(this::getPublicationTitleWeight, this::setPublicationTitleWeight, publicationTitleWeightDefault, 0.0, null, publicationTitleWeightId, "Publication title weight", publicationTitleWeightDescription, null));
		args.add(new Arg<>(this::getPublicationKeywordWeight, this::setPublicationKeywordWeight, publicationKeywordWeightDefault, 0.0, null, publicationKeywordWeightId, "Publication keyword weight", publicationKeywordWeightDescription, null));
		args.add(new Arg<>(this::getPublicationMeshWeight, this::setPublicationMeshWeight, publicationMeshWeightDefault, 0.0, null, publicationMeshWeightId, "Publication MeSH weight", publicationMeshWeightDescription, null));
		args.add(new Arg<>(this::getPublicationMinedTermWeight, this::setPublicationMinedTermWeight, publicationMinedTermWeightDefault, 0.0, null, publicationMinedTermWeightId, "Publication EFO/GO weight", publicationMinedTermWeightDescription, null));
		args.add(new Arg<>(this::getPublicationAbstractWeight, this::setPublicationAbstractWeight, publicationAbstractWeightDefault, 0.0, null, publicationAbstractWeightId, "Publication abstract weight", publicationAbstractWeightDescription, null));
		args.add(new Arg<>(this::getPublicationFulltextWeight, this::setPublicationFulltextWeight, publicationFulltextWeightDefault, 0.0, null, publicationFulltextWeightId, "Publication fulltext weight", publicationFulltextWeightDescription, null));
		args.add(new Arg<>(this::getDocWeight, this::setDocWeight, docWeightDefault, 0.0, null, docWeightId, "Doc weight", docWeightDescription, null));
		args.add(new Arg<>(this::getWebpageWeight, this::setWebpageWeight, webpageWeightDefault, 0.0, null, webpageWeightId, "Webpage weight", webpageWeightDescription, null));
	}

	@Override
	public String getId() {
		return "weightArgs";
	}

	@Override
	public String getLabel() {
		return "Query weights";
	}

	public Double getAverageScaling() {
		return averageScaling;
	}
	public void setAverageScaling(Double averageScaling) {
		this.averageScaling = averageScaling;
	}

	public Double getNameWeight() {
		return nameWeight;
	}
	public void setNameWeight(Double nameWeight) {
		this.nameWeight = nameWeight;
	}

	public Double getKeywordWeight() {
		return keywordWeight;
	}
	public void setKeywordWeight(Double keywordWeight) {
		this.keywordWeight = keywordWeight;
	}

	public Double getDescriptionWeight() {
		return descriptionWeight;
	}
	public void setDescriptionWeight(Double descriptionWeight) {
		this.descriptionWeight = descriptionWeight;
	}

	public Double getPublicationTitleWeight() {
		return publicationTitleWeight;
	}
	public void setPublicationTitleWeight(Double publicationTitleWeight) {
		this.publicationTitleWeight = publicationTitleWeight;
	}

	public Double getPublicationKeywordWeight() {
		return publicationKeywordWeight;
	}
	public void setPublicationKeywordWeight(Double publicationKeywordWeight) {
		this.publicationKeywordWeight = publicationKeywordWeight;
	}

	public Double getPublicationMeshWeight() {
		return publicationMeshWeight;
	}
	public void setPublicationMeshWeight(Double publicationMeshWeight) {
		this.publicationMeshWeight = publicationMeshWeight;
	}

	public Double getPublicationMinedTermWeight() {
		return publicationMinedTermWeight;
	}
	public void setPublicationMinedTermWeight(Double publicationMinedTermWeight) {
		this.publicationMinedTermWeight = publicationMinedTermWeight;
	}

	public Double getPublicationAbstractWeight() {
		return publicationAbstractWeight;
	}
	public void setPublicationAbstractWeight(Double publicationAbstractWeight) {
		this.publicationAbstractWeight = publicationAbstractWeight;
	}

	public Double getPublicationFulltextWeight() {
		return publicationFulltextWeight;
	}
	public void setPublicationFulltextWeight(Double publicationFulltextWeight) {
		this.publicationFulltextWeight = publicationFulltextWeight;
	}

	public Double getDocWeight() {
		return docWeight;
	}
	public void setDocWeight(Double docWeight) {
		this.docWeight = docWeight;
	}

	public Double getWebpageWeight() {
		return webpageWeight;
	}
	public void setWebpageWeight(Double webpageWeight) {
		this.webpageWeight = webpageWeight;
	}
}
