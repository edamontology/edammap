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

	public static final String NAME_NORMALISER = "nameNormaliser";
	@Parameter(names = { "--" + NAME_NORMALISER }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a query name. Set to 0 to disable matching of names.")
	private double nameNormaliser = 0.81;

	public static final String KEYWORD_NORMALISER = "keywordNormaliser";
	@Parameter(names = { "--" + KEYWORD_NORMALISER }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a query keyword. Set to 0 to disable matching of keywords.")
	private double keywordNormaliser = 0.77;

	public static final String DESCRIPTION_NORMALISER = "descriptionNormaliser";
	@Parameter(names = { "--" + DESCRIPTION_NORMALISER }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a query description. Set to 0 to disable matching of descriptions.")
	private double descriptionNormaliser = 0.92;

	public static final String PUBLICATION_TITLE_NORMALISER = "publicationTitleNormaliser";
	@Parameter(names = { "--" + PUBLICATION_TITLE_NORMALISER }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a publication title. Set to 0 to disable matching of titles.")
	private double publicationTitleNormaliser = 0.91;

	public static final String PUBLICATION_KEYWORD_NORMALISER = "publicationKeywordNormaliser";
	@Parameter(names = { "--" + PUBLICATION_KEYWORD_NORMALISER }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a publication keyword. Set to 0 to disable matching of keywords.")
	private double publicationKeywordNormaliser = 0.77;

	public static final String PUBLICATION_MESH_NORMALISER = "publicationMeshNormaliser";
	@Parameter(names = { "--" + PUBLICATION_MESH_NORMALISER }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a publication MeSH term. Set to 0 to disable matching of MeSH terms.")
	private double publicationMeshNormaliser = 0.75;

	public static final String PUBLICATION_MINED_TERM_NORMALISER = "publicationMinedTermNormaliser";
	@Parameter(names = { "--" + PUBLICATION_MINED_TERM_NORMALISER }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a publication mined term (EFO, GO). Set to 0 to disable matching of mined terms.")
	private double publicationMinedTermNormaliser = 1.0;

	public static final String PUBLICATION_ABSTRACT_NORMALISER = "publicationAbstractNormaliser";
	@Parameter(names = { "--" + PUBLICATION_ABSTRACT_NORMALISER }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a publication abstract. Set to 0 to disable matching of abstracts.")
	private double publicationAbstractNormaliser = 0.985;

	public static final String PUBLICATION_FULLTEXT_NORMALISER = "publicationFulltextNormaliser";
	@Parameter(names = { "--" + PUBLICATION_FULLTEXT_NORMALISER }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a publication fulltext. Set to 0 to disable matching of fulltexts.")
	private double publicationFulltextNormaliser = 1.0;

	public static final String DOC_NORMALISER = "docNormaliser";
	@Parameter(names = { "--" + DOC_NORMALISER }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a query doc. Set to 0 to disable matching of docs.")
	private double docNormaliser = 1.0;

	public static final String WEBPAGE_NORMALISER = "webpageNormaliser";
	@Parameter(names = { "--" + WEBPAGE_NORMALISER }, validateWith = ZeroToOneDouble.class, description = "Score normaliser for matching a query webpage. Set to 0 to disable matching of webpages.")
	private double webpageNormaliser = 1.0;

	public double getNameNormaliser() {
		return nameNormaliser;
	}
	public void setNameNormaliser(double nameNormaliser) {
		this.nameNormaliser = nameNormaliser;
	}

	public double getKeywordNormaliser() {
		return keywordNormaliser;
	}
	public void setKeywordNormaliser(double keywordNormaliser) {
		this.keywordNormaliser = keywordNormaliser;
	}

	public double getDescriptionNormaliser() {
		return descriptionNormaliser;
	}
	public void setDescriptionNormaliser(double descriptionNormaliser) {
		this.descriptionNormaliser = descriptionNormaliser;
	}

	public double getPublicationTitleNormaliser() {
		return publicationTitleNormaliser;
	}
	public void setPublicationTitleNormaliser(double publicationTitleNormaliser) {
		this.publicationTitleNormaliser = publicationTitleNormaliser;
	}

	public double getPublicationKeywordNormaliser() {
		return publicationKeywordNormaliser;
	}
	public void setPublicationKeywordNormaliser(double publicationKeywordNormaliser) {
		this.publicationKeywordNormaliser = publicationKeywordNormaliser;
	}

	public double getPublicationMeshNormaliser() {
		return publicationMeshNormaliser;
	}
	public void setPublicationMeshNormaliser(double publicationMeshNormaliser) {
		this.publicationMeshNormaliser = publicationMeshNormaliser;
	}

	public double getPublicationMinedTermNormaliser() {
		return publicationMinedTermNormaliser;
	}
	public void setPublicationMinedTermNormaliser(double publicationMinedTermNormaliser) {
		this.publicationMinedTermNormaliser = publicationMinedTermNormaliser;
	}

	public double getPublicationAbstractNormaliser() {
		return publicationAbstractNormaliser;
	}
	public void setPublicationAbstractNormaliser(double publicationAbstractNormaliser) {
		this.publicationAbstractNormaliser = publicationAbstractNormaliser;
	}

	public double getPublicationFulltextNormaliser() {
		return publicationFulltextNormaliser;
	}
	public void setPublicationFulltextNormaliser(double publicationFulltextNormaliser) {
		this.publicationFulltextNormaliser = publicationFulltextNormaliser;
	}

	public double getDocNormaliser() {
		return docNormaliser;
	}
	public void setDocNormaliser(double docNormaliser) {
		this.docNormaliser = docNormaliser;
	}

	public double getWebpageNormaliser() {
		return webpageNormaliser;
	}
	public void setWebpageNormaliser(double webpageNormaliser) {
		this.webpageNormaliser = webpageNormaliser;
	}
}
