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
	@Parameter(names = { "--average-scaling" }, validateWith = PositiveDouble.class, description = "Scaling for the average strategy")
	private double averageScaling = 10;

	@Parameter(names = { "--name-weight" }, validateWith = PositiveDouble.class, description = "Weight of query name in average strategy. Set to 0 to disable matching of names in average strategy.")
	private double nameWeight = 1;

	@Parameter(names = { "--keyword-weight" }, validateWith = PositiveDouble.class, description = "Weight of query keyword in average strategy. Set to 0 to disable matching of keywords in average strategy.")
	private double keywordWeight = 1;

	@Parameter(names = { "--description-weight" }, validateWith = PositiveDouble.class, description = "Weight of query description in average strategy. Set to 0 to disable matching of descriptions in average strategy.")
	private double descriptionWeight = 1;

	@Parameter(names = { "--publication-title-weight" }, validateWith = PositiveDouble.class, description = "Weight of publication title in average strategy. Set to 0 to disable matching of titles in average strategy.")
	private double publicationTitleWeight = 0.25;

	@Parameter(names = { "--publication-keyword-weight" }, validateWith = PositiveDouble.class, description = "Weight of publication keyword in average strategy. Set to 0 to disable matching of keywords in average strategy.")
	private double publicationKeywordWeight = 0.75;

	@Parameter(names = { "--publication-mesh-weight" }, validateWith = PositiveDouble.class, description = "Weight of publication MeSH term in average strategy. Set to 0 to disable matching of MeSH terms in average strategy.")
	private double publicationMeshWeight = 0.25;

	@Parameter(names = { "--publication-mined-term-weight" }, validateWith = PositiveDouble.class, description = "Weight of publication mined term (EFO, GO) in average strategy. Set to 0 to disable matching of mined terms in average strategy.")
	private double publicationMinedWeight = 0.25;

	@Parameter(names = { "--publication-abstract-weight" }, validateWith = PositiveDouble.class, description = "Weight of publication abstract in average strategy. Set to 0 to disable matching of abstracts in average strategy.")
	private double publicationAbstractWeight = 0.75;

	@Parameter(names = { "--publication-fulltext-weight" }, validateWith = PositiveDouble.class, description = "Weight of publication fulltext in average strategy. Set to 0 to disable matching of fulltexts in average strategy.")
	private double publicationFulltextWeight = 0.5;

	@Parameter(names = { "--doc-weight" }, validateWith = PositiveDouble.class, description = "Weight of query doc in average strategy. Set to 0 to disable matching of docs in average strategy.")
	private double docWeight = 0.5;

	@Parameter(names = { "--webpage-weight" }, validateWith = PositiveDouble.class, description = "Weight of query webpage in average strategy. Set to 0 to disable matching of webpages in average strategy.")
	private double webpageWeight = 0.5;

	public double getAverageScaling() {
		return averageScaling;
	}

	public double getNameWeight() {
		return nameWeight;
	}

	public double getKeywordWeight() {
		return keywordWeight;
	}

	public double getDescriptionWeight() {
		return descriptionWeight;
	}

	public double getPublicationTitleWeight() {
		return publicationTitleWeight;
	}

	public double getPublicationKeywordWeight() {
		return publicationKeywordWeight;
	}

	public double getPublicationMeshWeight() {
		return publicationMeshWeight;
	}

	public double getPublicationMinedWeight() {
		return publicationMinedWeight;
	}

	public double getPublicationAbstractWeight() {
		return publicationAbstractWeight;
	}

	public double getPublicationFulltextWeight() {
		return publicationFulltextWeight;
	}

	public double getDocWeight() {
		return docWeight;
	}

	public double getWebpageWeight() {
		return webpageWeight;
	}
}
