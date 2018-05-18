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

import org.edamontology.edammap.core.args.PositiveDouble;

import com.beust.jcommander.Parameter;

public class WeightArgs {

	public static final String AVERAGE_SCALING = "averageScaling";
	@Parameter(names = { "--" + AVERAGE_SCALING }, validateWith = PositiveDouble.class, description = "Scaling for the average strategy")
	private double averageScaling = 10;

	public static final String NAME_WEIGHT = "nameWeight";
	@Parameter(names = { "--" + NAME_WEIGHT }, validateWith = PositiveDouble.class, description = "Weight of query name in average strategy. Set to 0 to disable matching of names in average strategy.")
	private double nameWeight = 1;

	public static final String KEYWORD_WEIGHT = "keywordWeight";
	@Parameter(names = { "--" + KEYWORD_WEIGHT }, validateWith = PositiveDouble.class, description = "Weight of query keyword in average strategy. Set to 0 to disable matching of keywords in average strategy.")
	private double keywordWeight = 1;

	public static final String DESCRIPTION_WEIGHT = "descriptionWeight";
	@Parameter(names = { "--" + DESCRIPTION_WEIGHT }, validateWith = PositiveDouble.class, description = "Weight of query description in average strategy. Set to 0 to disable matching of descriptions in average strategy.")
	private double descriptionWeight = 1;

	public static final String PUBLICATION_TITLE_WEIGHT = "publicationTitleWeight";
	@Parameter(names = { "--" + PUBLICATION_TITLE_WEIGHT }, validateWith = PositiveDouble.class, description = "Weight of publication title in average strategy. Set to 0 to disable matching of titles in average strategy.")
	private double publicationTitleWeight = 0.25;

	public static final String PUBLICATION_KEYWORD_WEIGHT = "publicationKeywordWeight";
	@Parameter(names = { "--" + PUBLICATION_KEYWORD_WEIGHT }, validateWith = PositiveDouble.class, description = "Weight of publication keyword in average strategy. Set to 0 to disable matching of keywords in average strategy.")
	private double publicationKeywordWeight = 0.75;

	public static final String PUBLICATION_MESH_WEIGHT = "publicationMeshWeight";
	@Parameter(names = { "--" + PUBLICATION_MESH_WEIGHT }, validateWith = PositiveDouble.class, description = "Weight of publication MeSH term in average strategy. Set to 0 to disable matching of MeSH terms in average strategy.")
	private double publicationMeshWeight = 0.25;

	public static final String PUBLICATION_MINED_TERM_WEIGHT = "publicationMinedTermWeight";
	@Parameter(names = { "--" + PUBLICATION_MINED_TERM_WEIGHT }, validateWith = PositiveDouble.class, description = "Weight of publication mined term (EFO, GO) in average strategy. Set to 0 to disable matching of mined terms in average strategy.")
	private double publicationMinedTermWeight = 0.25;

	public static final String PUBLICATION_ABSTRACT_WEIGHT = "publicationAbstractWeight";
	@Parameter(names = { "--" + PUBLICATION_ABSTRACT_WEIGHT }, validateWith = PositiveDouble.class, description = "Weight of publication abstract in average strategy. Set to 0 to disable matching of abstracts in average strategy.")
	private double publicationAbstractWeight = 0.75;

	public static final String PUBLICATION_FULLTEXT_WEIGHT = "publicationFulltextWeight";
	@Parameter(names = { "--" + PUBLICATION_FULLTEXT_WEIGHT }, validateWith = PositiveDouble.class, description = "Weight of publication fulltext in average strategy. Set to 0 to disable matching of fulltexts in average strategy.")
	private double publicationFulltextWeight = 0.5;

	public static final String DOC_WEIGHT = "docWeight";
	@Parameter(names = { "--" + DOC_WEIGHT }, validateWith = PositiveDouble.class, description = "Weight of query doc in average strategy. Set to 0 to disable matching of docs in average strategy.")
	private double docWeight = 0.5;

	public static final String WEBPAGE_WEIGHT = "webpageWeight";
	@Parameter(names = { "--" + WEBPAGE_WEIGHT }, validateWith = PositiveDouble.class, description = "Weight of query webpage in average strategy. Set to 0 to disable matching of webpages in average strategy.")
	private double webpageWeight = 0.5;

	public double getAverageScaling() {
		return averageScaling;
	}
	public void setAverageScaling(double averageScaling) {
		this.averageScaling = averageScaling;
	}

	public double getNameWeight() {
		return nameWeight;
	}
	public void setNameWeight(double nameWeight) {
		this.nameWeight = nameWeight;
	}

	public double getKeywordWeight() {
		return keywordWeight;
	}
	public void setKeywordWeight(double keywordWeight) {
		this.keywordWeight = keywordWeight;
	}

	public double getDescriptionWeight() {
		return descriptionWeight;
	}
	public void setDescriptionWeight(double descriptionWeight) {
		this.descriptionWeight = descriptionWeight;
	}

	public double getPublicationTitleWeight() {
		return publicationTitleWeight;
	}
	public void setPublicationTitleWeight(double publicationTitleWeight) {
		this.publicationTitleWeight = publicationTitleWeight;
	}

	public double getPublicationKeywordWeight() {
		return publicationKeywordWeight;
	}
	public void setPublicationKeywordWeight(double publicationKeywordWeight) {
		this.publicationKeywordWeight = publicationKeywordWeight;
	}

	public double getPublicationMeshWeight() {
		return publicationMeshWeight;
	}
	public void setPublicationMeshWeight(double publicationMeshWeight) {
		this.publicationMeshWeight = publicationMeshWeight;
	}

	public double getPublicationMinedTermWeight() {
		return publicationMinedTermWeight;
	}
	public void setPublicationMinedTermWeight(double publicationMinedTermWeight) {
		this.publicationMinedTermWeight = publicationMinedTermWeight;
	}

	public double getPublicationAbstractWeight() {
		return publicationAbstractWeight;
	}
	public void setPublicationAbstractWeight(double publicationAbstractWeight) {
		this.publicationAbstractWeight = publicationAbstractWeight;
	}

	public double getPublicationFulltextWeight() {
		return publicationFulltextWeight;
	}
	public void setPublicationFulltextWeight(double publicationFulltextWeight) {
		this.publicationFulltextWeight = publicationFulltextWeight;
	}

	public double getDocWeight() {
		return docWeight;
	}
	public void setDocWeight(double docWeight) {
		this.docWeight = docWeight;
	}

	public double getWebpageWeight() {
		return webpageWeight;
	}
	public void setWebpageWeight(double webpageWeight) {
		this.webpageWeight = webpageWeight;
	}
}
