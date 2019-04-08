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

public class NormaliserArgs extends Args {

	private static final String nameNormaliserId = "nameNormaliser";
	private static final String nameNormaliserDescription = "Score normaliser for matching a query name. Set to 0 to disable matching of names.";
	private static final Double nameNormaliserDefault = 0.81;
	@Parameter(names = { "--" + nameNormaliserId }, validateWith = ZeroToOneDouble.class, description = nameNormaliserDescription)
	private Double nameNormaliser = nameNormaliserDefault;

	private static final String keywordNormaliserId = "keywordNormaliser";
	private static final String keywordNormaliserDescription = "Score normaliser for matching a query keyword. Set to 0 to disable matching of keywords.";
	private static final Double keywordNormaliserDefault = 0.77;
	@Parameter(names = { "--" + keywordNormaliserId }, validateWith = ZeroToOneDouble.class, description = keywordNormaliserDescription)
	private Double keywordNormaliser = keywordNormaliserDefault;

	private static final String descriptionNormaliserId = "descriptionNormaliser";
	private static final String descriptionNormaliserDescription = "Score normaliser for matching a query description. Set to 0 to disable matching of descriptions.";
	private static final Double descriptionNormaliserDefault = 0.92;
	@Parameter(names = { "--" + descriptionNormaliserId }, validateWith = ZeroToOneDouble.class, description = descriptionNormaliserDescription)
	private Double descriptionNormaliser = descriptionNormaliserDefault;

	private static final String publicationTitleNormaliserId = "publicationTitleNormaliser";
	private static final String publicationTitleNormaliserDescription = "Score normaliser for matching a publication title. Set to 0 to disable matching of titles.";
	private static final Double publicationTitleNormaliserDefault = 0.91;
	@Parameter(names = { "--" + publicationTitleNormaliserId }, validateWith = ZeroToOneDouble.class, description = publicationTitleNormaliserDescription)
	private Double publicationTitleNormaliser = publicationTitleNormaliserDefault;

	private static final String publicationKeywordNormaliserId = "publicationKeywordNormaliser";
	private static final String publicationKeywordNormaliserDescription = "Score normaliser for matching a publication keyword. Set to 0 to disable matching of keywords.";
	private static final Double publicationKeywordNormaliserDefault = 0.77;
	@Parameter(names = { "--" + publicationKeywordNormaliserId }, validateWith = ZeroToOneDouble.class, description = publicationKeywordNormaliserDescription)
	private Double publicationKeywordNormaliser = publicationKeywordNormaliserDefault;

	private static final String publicationMeshNormaliserId = "publicationMeshNormaliser";
	private static final String publicationMeshNormaliserDescription = "Score normaliser for matching a publication MeSH term. Set to 0 to disable matching of MeSH terms.";
	private static final Double publicationMeshNormaliserDefault = 0.75;
	@Parameter(names = { "--" + publicationMeshNormaliserId }, validateWith = ZeroToOneDouble.class, description = publicationMeshNormaliserDescription)
	private Double publicationMeshNormaliser = publicationMeshNormaliserDefault;

	private static final String publicationMinedTermNormaliserId = "publicationMinedTermNormaliser";
	private static final String publicationMinedTermNormaliserDescription = "Score normaliser for matching a publication mined term (EFO, GO). Set to 0 to disable matching of mined terms.";
	private static final Double publicationMinedTermNormaliserDefault = 1.0;
	@Parameter(names = { "--" + publicationMinedTermNormaliserId }, validateWith = ZeroToOneDouble.class, description = publicationMinedTermNormaliserDescription)
	private Double publicationMinedTermNormaliser = publicationMinedTermNormaliserDefault;

	private static final String publicationAbstractNormaliserId = "publicationAbstractNormaliser";
	private static final String publicationAbstractNormaliserDescription = "Score normaliser for matching a publication abstract. Set to 0 to disable matching of abstracts.";
	private static final Double publicationAbstractNormaliserDefault = 0.985;
	@Parameter(names = { "--" + publicationAbstractNormaliserId }, validateWith = ZeroToOneDouble.class, description = publicationAbstractNormaliserDescription)
	private Double publicationAbstractNormaliser = publicationAbstractNormaliserDefault;

	private static final String publicationFulltextNormaliserId = "publicationFulltextNormaliser";
	private static final String publicationFulltextNormaliserDescription = "Score normaliser for matching a publication fulltext. Set to 0 to disable matching of fulltexts.";
	private static final Double publicationFulltextNormaliserDefault = 1.0;
	@Parameter(names = { "--" + publicationFulltextNormaliserId }, validateWith = ZeroToOneDouble.class, description = publicationFulltextNormaliserDescription)
	private Double publicationFulltextNormaliser = publicationFulltextNormaliserDefault;

	private static final String docNormaliserId = "docNormaliser";
	private static final String docNormaliserDescription = "Score normaliser for matching a query doc. Set to 0 to disable matching of docs.";
	private static final Double docNormaliserDefault = 1.0;
	@Parameter(names = { "--" + docNormaliserId }, validateWith = ZeroToOneDouble.class, description = docNormaliserDescription)
	private Double docNormaliser = docNormaliserDefault;

	private static final String webpageNormaliserId = "webpageNormaliser";
	private static final String webpageNormaliserDescription = "Score normaliser for matching a query webpage. Set to 0 to disable matching of webpages.";
	private static final Double webpageNormaliserDefault = 1.0;
	@Parameter(names = { "--" + webpageNormaliserId }, validateWith = ZeroToOneDouble.class, description = webpageNormaliserDescription)
	private Double webpageNormaliser = webpageNormaliserDefault;

	@Override
	protected void addArgs() {
		args.add(new Arg<>(this::getNameNormaliser, this::setNameNormaliser, nameNormaliserDefault, 0.0, 1.0, nameNormaliserId, "Name norm.", nameNormaliserDescription, null));
		args.add(new Arg<>(this::getKeywordNormaliser, this::setKeywordNormaliser, keywordNormaliserDefault, 0.0, 1.0, keywordNormaliserId, "Keyword norm.", keywordNormaliserDescription, null));
		args.add(new Arg<>(this::getDescriptionNormaliser, this::setDescriptionNormaliser, descriptionNormaliserDefault, 0.0, 1.0, descriptionNormaliserId, "Description norm.", descriptionNormaliserDescription, null));
		args.add(new Arg<>(this::getPublicationTitleNormaliser, this::setPublicationTitleNormaliser, publicationTitleNormaliserDefault, 0.0, 1.0, publicationTitleNormaliserId, "Publication title norm.", publicationTitleNormaliserDescription, null));
		args.add(new Arg<>(this::getPublicationKeywordNormaliser, this::setPublicationKeywordNormaliser, publicationKeywordNormaliserDefault, 0.0, 1.0, publicationKeywordNormaliserId, "Publication keyword norm.", publicationKeywordNormaliserDescription, null));
		args.add(new Arg<>(this::getPublicationMeshNormaliser, this::setPublicationMeshNormaliser, publicationMeshNormaliserDefault, 0.0, 1.0, publicationMeshNormaliserId, "Publication MeSH norm.", publicationMeshNormaliserDescription, null));
		args.add(new Arg<>(this::getPublicationMinedTermNormaliser, this::setPublicationMinedTermNormaliser, publicationMinedTermNormaliserDefault, 0.0, 1.0, publicationMinedTermNormaliserId, "Publication EFO/GO norm.", publicationMinedTermNormaliserDescription, null));
		args.add(new Arg<>(this::getPublicationAbstractNormaliser, this::setPublicationAbstractNormaliser, publicationAbstractNormaliserDefault, 0.0, 1.0, publicationAbstractNormaliserId, "Publication abstract norm.", publicationAbstractNormaliserDescription, null));
		args.add(new Arg<>(this::getPublicationFulltextNormaliser, this::setPublicationFulltextNormaliser, publicationFulltextNormaliserDefault, 0.0, 1.0, publicationFulltextNormaliserId, "Publication fulltext norm.", publicationFulltextNormaliserDescription, null));
		args.add(new Arg<>(this::getDocNormaliser, this::setDocNormaliser, docNormaliserDefault, 0.0, 1.0, docNormaliserId, "Doc norm.", docNormaliserDescription, null));
		args.add(new Arg<>(this::getWebpageNormaliser, this::setWebpageNormaliser, webpageNormaliserDefault, 0.0, 1.0, webpageNormaliserId, "Webpage norm.", webpageNormaliserDescription, null));
	}

	@Override
	public String getId() {
		return "normaliserArgs";
	}

	@Override
	public String getLabel() {
		return "Query normalisers";
	}

	public Double getNameNormaliser() {
		return nameNormaliser;
	}
	public void setNameNormaliser(Double nameNormaliser) {
		this.nameNormaliser = nameNormaliser;
	}

	public Double getKeywordNormaliser() {
		return keywordNormaliser;
	}
	public void setKeywordNormaliser(Double keywordNormaliser) {
		this.keywordNormaliser = keywordNormaliser;
	}

	public Double getDescriptionNormaliser() {
		return descriptionNormaliser;
	}
	public void setDescriptionNormaliser(Double descriptionNormaliser) {
		this.descriptionNormaliser = descriptionNormaliser;
	}

	public Double getPublicationTitleNormaliser() {
		return publicationTitleNormaliser;
	}
	public void setPublicationTitleNormaliser(Double publicationTitleNormaliser) {
		this.publicationTitleNormaliser = publicationTitleNormaliser;
	}

	public Double getPublicationKeywordNormaliser() {
		return publicationKeywordNormaliser;
	}
	public void setPublicationKeywordNormaliser(Double publicationKeywordNormaliser) {
		this.publicationKeywordNormaliser = publicationKeywordNormaliser;
	}

	public Double getPublicationMeshNormaliser() {
		return publicationMeshNormaliser;
	}
	public void setPublicationMeshNormaliser(Double publicationMeshNormaliser) {
		this.publicationMeshNormaliser = publicationMeshNormaliser;
	}

	public Double getPublicationMinedTermNormaliser() {
		return publicationMinedTermNormaliser;
	}
	public void setPublicationMinedTermNormaliser(Double publicationMinedTermNormaliser) {
		this.publicationMinedTermNormaliser = publicationMinedTermNormaliser;
	}

	public Double getPublicationAbstractNormaliser() {
		return publicationAbstractNormaliser;
	}
	public void setPublicationAbstractNormaliser(Double publicationAbstractNormaliser) {
		this.publicationAbstractNormaliser = publicationAbstractNormaliser;
	}

	public Double getPublicationFulltextNormaliser() {
		return publicationFulltextNormaliser;
	}
	public void setPublicationFulltextNormaliser(Double publicationFulltextNormaliser) {
		this.publicationFulltextNormaliser = publicationFulltextNormaliser;
	}

	public Double getDocNormaliser() {
		return docNormaliser;
	}
	public void setDocNormaliser(Double docNormaliser) {
		this.docNormaliser = docNormaliser;
	}

	public Double getWebpageNormaliser() {
		return webpageNormaliser;
	}
	public void setWebpageNormaliser(Double webpageNormaliser) {
		this.webpageNormaliser = webpageNormaliser;
	}
}
