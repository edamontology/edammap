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

public class NormaliserArgs {
	@Parameter(names = { "--name-normaliser" }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a query name. Set to 0 to disable matching of names.")
	private double nameNormaliser = 0.81;

	@Parameter(names = { "--keyword-normaliser" }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a query keyword. Set to 0 to disable matching of keywords.")
	private double keywordNormaliser = 0.77;

	@Parameter(names = { "--description-normaliser" }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a query description. Set to 0 to disable matching of descriptions.")
	private double descriptionNormaliser = 0.92;

	@Parameter(names = { "--publication-title-normaliser" }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a publication title. Set to 0 to disable matching of titles.")
	private double publicationTitleNormaliser = 0.91;

	@Parameter(names = { "--publication-keyword-normaliser" }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a publication keyword. Set to 0 to disable matching of keywords.")
	private double publicationKeywordNormaliser = 0.77;

	@Parameter(names = { "--publication-mesh-normaliser" }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a publication MeSH term. Set to 0 to disable matching of MeSH terms.")
	private double publicationMeshNormaliser = 0.75;

	@Parameter(names = { "--publication-mined-term-normaliser" }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a publication mined term (EFO, GO). Set to 0 to disable matching of mined terms.")
	private double publicationMinedNormaliser = 1;

	@Parameter(names = { "--publication-abstract-normaliser" }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a publication abstract. Set to 0 to disable matching of abstracts.")
	private double publicationAbstractNormaliser = 0.985;

	@Parameter(names = { "--publication-fulltext-normaliser" }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a publication fulltext. Set to 0 to disable matching of fulltexts.")
	private double publicationFulltextNormaliser = 1;

	@Parameter(names = { "--doc-normaliser" }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a query doc. Set to 0 to disable matching of docs.")
	private double docNormaliser = 1;

	@Parameter(names = { "--webpage-normaliser" }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a query webpage. Set to 0 to disable matching of webpages.")
	private double webpageNormaliser = 1;

	public double getNameNormaliser() {
		return nameNormaliser;
	}

	public double getKeywordNormaliser() {
		return keywordNormaliser;
	}

	public double getDescriptionNormaliser() {
		return descriptionNormaliser;
	}

	public double getPublicationTitleNormaliser() {
		return publicationTitleNormaliser;
	}

	public double getPublicationKeywordNormaliser() {
		return publicationKeywordNormaliser;
	}

	public double getPublicationMeshNormaliser() {
		return publicationMeshNormaliser;
	}

	public double getPublicationMinedNormaliser() {
		return publicationMinedNormaliser;
	}

	public double getPublicationAbstractNormaliser() {
		return publicationAbstractNormaliser;
	}

	public double getPublicationFulltextNormaliser() {
		return publicationFulltextNormaliser;
	}

	public double getDocNormaliser() {
		return docNormaliser;
	}

	public double getWebpageNormaliser() {
		return webpageNormaliser;
	}
}
