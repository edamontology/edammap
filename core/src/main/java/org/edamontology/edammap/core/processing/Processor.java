/*
 * Copyright © 2016, 2017 Erik Jaaniso
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

package org.edamontology.edammap.core.processing;

import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.idf.Idf;
import org.edamontology.edammap.core.idf.IdfMake;
import org.edamontology.edammap.core.mapping.args.IdfArgs;
import org.edamontology.edammap.core.mapping.args.MultiplierArgs;
import org.edamontology.edammap.core.preprocessing.PreProcessor;
import org.edamontology.edammap.core.query.Keyword;
import org.edamontology.edammap.core.query.Link;
import org.edamontology.edammap.core.query.PublicationIdsQuery;
import org.edamontology.edammap.core.query.Query;
import org.edamontology.edammap.core.query.QueryType;
import org.edamontology.pubfetcher.Database;
import org.edamontology.pubfetcher.Fetcher;
import org.edamontology.pubfetcher.FetcherCommon;
import org.edamontology.pubfetcher.MeshTerm;
import org.edamontology.pubfetcher.MinedTerm;
import org.edamontology.pubfetcher.Publication;
import org.edamontology.pubfetcher.Webpage;

public class Processor {

	private final PreProcessor preProcessor;

	private final Fetcher fetcher;

	private final Database database;

	private final Idf queryIdf;

	public Processor(ProcessorArgs args) throws IOException, ParseException {
		this.preProcessor = new PreProcessor(args.getPreProcessorArgs());

		if (!args.isFetcher()) {
			this.fetcher = null;
		} else {
			this.fetcher = new Fetcher(args.getFetcherArgs());
		}

		if (args.getDatabase() == null || args.getDatabase().isEmpty()) {
			this.database = null;
		} else {
			this.database = new Database(args.getDatabase());
		}

		if (args.getIdf() == null || args.getIdf().isEmpty()) {
			this.queryIdf = null;
		} else {
			this.queryIdf = new Idf(args.getIdf());
		}
	}

	private ConceptProcessed processConcept(Concept concept, IdfMake idfMake) {
		ConceptProcessed processedConcept = new ConceptProcessed();

		processedConcept.setObsolete(concept.isObsolete());

		if (concept.getLabel() != null) {
			List<String> labelTokens = preProcessor.process(concept.getLabel());
			processedConcept.setLabelTokens(labelTokens);
			idfMake.addTerms(labelTokens);
		}

		for (String exactSynonym : concept.getExactSynonyms()) {
			List<String> exactSynonymTokens = preProcessor.process(exactSynonym);
			processedConcept.addExactSynonymTokens(exactSynonymTokens);
			idfMake.addTerms(exactSynonymTokens);
		}

		for (String narrowSynonym : concept.getNarrowSynonyms()) {
			List<String> narrowSynonymTokens = preProcessor.process(narrowSynonym);
			processedConcept.addNarrowSynonymTokens(narrowSynonymTokens);
			idfMake.addTerms(narrowSynonymTokens);
		}

		for (String broadSynonym : concept.getBroadSynonyms()) {
			List<String> broadSynonymTokens = preProcessor.process(broadSynonym);
			processedConcept.addBroadSynonymTokens(broadSynonymTokens);
			idfMake.addTerms(broadSynonymTokens);
		}

		if (concept.getDefinition() != null) {
			List<String> definitionTokens = preProcessor.process(concept.getDefinition());
			processedConcept.setDefinitionTokens(definitionTokens);
			idfMake.addTerms(definitionTokens);
		}

		if (concept.getComment() != null) {
			List<String> commentTokens = preProcessor.process(concept.getComment());
			processedConcept.setCommentTokens(commentTokens);
			idfMake.addTerms(commentTokens);
		}

		idfMake.endDocument();

		processedConcept.setDirectParents(concept.getDirectParents());
		processedConcept.setDirectChildren(concept.getDirectChildren());

		return processedConcept;
	}

	private void processConceptIdf(ConceptProcessed processedConcept, Idf idf) {
		if (processedConcept.getLabelTokens() != null) {
			processedConcept.setLabelIdfs(idf.getIdf(processedConcept.getLabelTokens()));
		}

		for (List<String> exactSynonymTokens : processedConcept.getExactSynonymsTokens()) {
			processedConcept.addExactSynonymIdfs(idf.getIdf(exactSynonymTokens));
		}

		for (List<String> narrowSynonymTokens : processedConcept.getNarrowSynonymsTokens()) {
			processedConcept.addNarrowSynonymIdfs(idf.getIdf(narrowSynonymTokens));
		}

		for (List<String> broadSynonymTokens : processedConcept.getBroadSynonymsTokens()) {
			processedConcept.addBroadSynonymIdfs(idf.getIdf(broadSynonymTokens));
		}

		if (processedConcept.getDefinitionTokens() != null) {
			processedConcept.setDefinitionIdfs(idf.getIdf(processedConcept.getDefinitionTokens()));
		}

		if (processedConcept.getCommentTokens() != null) {
			processedConcept.setCommentIdfs(idf.getIdf(processedConcept.getCommentTokens()));
		}
	}

	private void anonymizeProcessedConcept(ConceptProcessed processedConcept, IdfArgs idfArgs, MultiplierArgs multiplierArgs) {
		if (processedConcept.getLabelTokens() != null && multiplierArgs.getLabelMultiplier() > 0) {
			processedConcept.addTokens(processedConcept.getLabelTokens());
			processedConcept.addIdfs(processedConcept.getLabelIdfs());
			processedConcept.addScaling(idfArgs.isLabelSynonymsIdf() ? idfArgs.getConceptIdfScaling() : 0);
			processedConcept.addMultiplier(multiplierArgs.getLabelMultiplier());
		}
		if (multiplierArgs.getExactSynonymMultiplier() > 0) {
			for (int i = 0; i < processedConcept.getExactSynonymsTokens().size(); ++i) {
				processedConcept.addTokens(processedConcept.getExactSynonymsTokens().get(i));
				processedConcept.addIdfs(processedConcept.getExactSynonymsIdfs().get(i));
				processedConcept.addScaling(idfArgs.isLabelSynonymsIdf() ? idfArgs.getConceptIdfScaling() : 0);
				processedConcept.addMultiplier(multiplierArgs.getExactSynonymMultiplier());
			}
		}
		if (multiplierArgs.getNarrowBroadMultiplier() > 0) {
			for (int i = 0; i < processedConcept.getNarrowSynonymsTokens().size(); ++i) {
				processedConcept.addTokens(processedConcept.getNarrowSynonymsTokens().get(i));
				processedConcept.addIdfs(processedConcept.getNarrowSynonymsIdfs().get(i));
				processedConcept.addScaling(idfArgs.isLabelSynonymsIdf() ? idfArgs.getConceptIdfScaling() : 0);
				processedConcept.addMultiplier(multiplierArgs.getNarrowBroadMultiplier());
			}
		}
		if (multiplierArgs.getNarrowBroadMultiplier() > 0) {
			for (int i = 0; i < processedConcept.getBroadSynonymsTokens().size(); ++i) {
				processedConcept.addTokens(processedConcept.getBroadSynonymsTokens().get(i));
				processedConcept.addIdfs(processedConcept.getBroadSynonymsIdfs().get(i));
				processedConcept.addScaling(idfArgs.isLabelSynonymsIdf() ? idfArgs.getConceptIdfScaling() : 0);
				processedConcept.addMultiplier(multiplierArgs.getNarrowBroadMultiplier());
			}
		}
		if (processedConcept.getDefinitionTokens() != null && multiplierArgs.getDefinitionMultiplier() > 0) {
			processedConcept.addTokens(processedConcept.getDefinitionTokens());
			processedConcept.addIdfs(processedConcept.getDefinitionIdfs());
			processedConcept.addScaling(idfArgs.getConceptIdfScaling());
			processedConcept.addMultiplier(multiplierArgs.getDefinitionMultiplier());
		}
		if (processedConcept.getCommentTokens() != null && multiplierArgs.getCommentMultiplier() > 0) {
			processedConcept.addTokens(processedConcept.getCommentTokens());
			processedConcept.addIdfs(processedConcept.getCommentIdfs());
			processedConcept.addScaling(idfArgs.getConceptIdfScaling());
			processedConcept.addMultiplier(multiplierArgs.getCommentMultiplier());
		}
	}

	public Map<EdamUri, ConceptProcessed> getProcessedConcepts(Map<EdamUri, Concept> concepts, IdfArgs idfArgs, MultiplierArgs multiplierArgs) {
		Map<EdamUri, ConceptProcessed> processedConcepts = new LinkedHashMap<>();

		IdfMake idfMake = new IdfMake();
		for (Map.Entry<EdamUri, Concept> concept : concepts.entrySet()) {
			processedConcepts.put(concept.getKey(), processConcept(concept.getValue(), idfMake));
		}

		Idf idf = new Idf(idfMake.getIdf());
		for (ConceptProcessed processedConcept : processedConcepts.values()) {
			processConceptIdf(processedConcept, idf);
		}

		for (ConceptProcessed processedConcept : processedConcepts.values()) {
			anonymizeProcessedConcept(processedConcept, idfArgs, multiplierArgs);
		}

		return processedConcepts;
	}

	private PublicationProcessed processPublication(Publication publication, PreProcessor pp) {
		PublicationProcessed publicationProcessed = new PublicationProcessed();

		if (!publication.getTitle().isEmpty()) {
			List<String> titleTokens = pp.process(publication.getTitle().getContent());
			if (!titleTokens.isEmpty()) {
				publicationProcessed.setTitleTokens(titleTokens);
				if (queryIdf != null) {
					publicationProcessed.setTitleIdfs(queryIdf.getIdf(titleTokens));
				}
			}
		}

		for (String keyword : publication.getKeywords().getList()) {
			List<String> keywordTokens = pp.process(keyword);
			List<Double> keywordIdfs = null;
			if (keywordTokens.isEmpty()) {
				keywordTokens = null;
			} else if (queryIdf != null) {
				keywordIdfs = queryIdf.getIdf(keywordTokens);
			}
			publicationProcessed.addKeywordTokens(keywordTokens);
			publicationProcessed.addKeywordIdfs(keywordIdfs);
		}

		for (MeshTerm meshTerm : publication.getMeshTerms().getList()) {
			List<String> meshTermTokens = pp.process(meshTerm.getTerm());
			List<Double> meshTermIdfs = null;
			if (meshTermTokens.isEmpty()) {
				meshTermTokens = null;
			} else if (queryIdf != null) {
				meshTermIdfs = queryIdf.getIdf(meshTermTokens);
			}
			publicationProcessed.addMeshTermTokens(meshTermTokens);
			publicationProcessed.addMeshTermIdfs(meshTermIdfs);
		}

		if (!publication.getAbstract().isEmpty()) {
			List<String> abstractTokens = pp.process(publication.getAbstract().getContent());
			if (!abstractTokens.isEmpty()) {
				publicationProcessed.setAbstractTokens(abstractTokens);
				if (queryIdf != null) {
					publicationProcessed.setAbstractIdfs(queryIdf.getIdf(abstractTokens));
				}
			}
		}

		int fulltextWordCount = 0;
		if (!publication.getFulltext().isEmpty()) {
			List<String> fulltextTokens = pp.process(publication.getFulltext().getContent());
			if (!fulltextTokens.isEmpty()) {
				publicationProcessed.setFulltextTokens(fulltextTokens);
				fulltextWordCount = fulltextTokens.size();
				if (queryIdf != null) {
					publicationProcessed.setFulltextIdfs(queryIdf.getIdf(fulltextTokens));
				}
			}
		}

		for (MinedTerm efoTerm : publication.getEfoTerms().getList()) {
			List<String> efoTermTokens = pp.process(efoTerm.getTerm());
			List<Double> efoTermIdfs = null;
			Double efoTermFrequency = null;
			if (efoTermTokens.isEmpty()) {
				efoTermTokens = null;
			} else {
				if (queryIdf != null) {
					efoTermIdfs = queryIdf.getIdf(efoTermTokens);
				}
				efoTermFrequency = efoTerm.getFrequency(fulltextWordCount);
			}
			publicationProcessed.addEfoTermTokens(efoTermTokens);
			publicationProcessed.addEfoTermIdfs(efoTermIdfs);
			publicationProcessed.addEfoTermFrequency(efoTermFrequency);
		}

		for (MinedTerm goTerm : publication.getGoTerms().getList()) {
			List<String> goTermTokens = pp.process(goTerm.getTerm());
			List<Double> goTermIdfs = null;
			Double goTermFrequency = null;
			if (goTermTokens.isEmpty()) {
				goTermTokens = null;
			} else {
				if (queryIdf != null) {
					goTermIdfs = queryIdf.getIdf(goTermTokens);
				}
				goTermFrequency = goTerm.getFrequency(fulltextWordCount);
			}
			publicationProcessed.addGoTermTokens(goTermTokens);
			publicationProcessed.addGoTermIdfs(goTermIdfs);
			publicationProcessed.addGoTermFrequency(goTermFrequency);
		}

		return publicationProcessed;
	}

	public QueryProcessed getProcessedQuery(Query query, PreProcessor pp, QueryType type) {
		QueryProcessed queryProcessed = new QueryProcessed();

		boolean removeBroken = (type == QueryType.BioConductor);

		if (query.getName() != null) {
			List<String> nameTokens = pp.process(query.getName());
			if (!nameTokens.isEmpty()) {
				queryProcessed.setNameTokens(nameTokens);
				if (queryIdf != null) {
					queryProcessed.setNameIdfs(queryIdf.getIdf(nameTokens));
				}
			}
		}

		if (query.getWebpageUrls() != null) {
			for (Iterator<Link> it = query.getWebpageUrls().iterator(); it.hasNext(); ) {
				String webpageUrl = it.next().getUrl();
				Webpage webpage = FetcherCommon.getWebpage(webpageUrl, database, fetcher);
				List<String> webpageTokens = null;
				List<Double> webpageIdfs = null;
				if (webpage != null) {
					webpageTokens = pp.process(webpage.getTitle() + " " + webpage.getContent());
					if (webpageTokens.isEmpty()) {
						webpageTokens = null;
					} else if (queryIdf != null) {
						webpageIdfs = queryIdf.getIdf(webpageTokens);
					}
				}
				if (webpageTokens == null && removeBroken) {
					it.remove();
				} else {
					queryProcessed.addWebpage(webpage);
					queryProcessed.addWebpageTokens(webpageTokens);
					queryProcessed.addWebpageIdfs(webpageIdfs);
				}
			}
		}

		if (query.getDescription() != null) {
			List<String> descriptionTokens = pp.process(query.getDescription());
			if (!descriptionTokens.isEmpty()) {
				queryProcessed.setDescriptionTokens(descriptionTokens);
				if (queryIdf != null) {
					queryProcessed.setDescriptionIdfs(queryIdf.getIdf(descriptionTokens));
				}
			}
		}

		if (query.getKeywords() != null) {
			for (Keyword keyword : query.getKeywords()) {
				String keywordValue = keyword.getValue();
				List<String> keywordTokens = null;
				List<Double> keywordIdfs = null;
				if (keywordValue != null) {
					keywordTokens = pp.process(keywordValue);
					if (keywordTokens.isEmpty()) {
						keywordTokens = null;
					} else if (queryIdf != null) {
						keywordIdfs = queryIdf.getIdf(keywordTokens);
					}
				}
				queryProcessed.addKeywordTokens(keywordTokens);
				queryProcessed.addKeywordIdfs(keywordIdfs);
			}
		}

		if (query.getPublicationIds() != null) {
			for (PublicationIdsQuery publicationIds : query.getPublicationIds()) {
				Publication publication = FetcherCommon.getPublication(publicationIds, database, fetcher, null);
				if (publication != null) {
					queryProcessed.addPublication(publication);
					queryProcessed.addProcessedPublication(processPublication(publication, pp));
				} else {
					queryProcessed.addPublication(null);
					queryProcessed.addProcessedPublication(null);
				}
			}
		}

		if (query.getDocUrls() != null) {
			for (Iterator<Link> it = query.getDocUrls().iterator(); it.hasNext(); ) {
				String docUrl = it.next().getUrl();
				Webpage doc = FetcherCommon.getDoc(docUrl, database, fetcher);
				List<String> docTokens = null;
				List<Double> docIdfs = null;
				if (doc != null) {
					docTokens = pp.process(doc.getTitle() + " " + doc.getContent());
					if (docTokens.isEmpty()) {
						docTokens = null;
					} else if (queryIdf != null) {
						docIdfs = queryIdf.getIdf(docTokens);
					}
				}
				if (docTokens == null && removeBroken) {
					it.remove();
				} else {
					queryProcessed.addDoc(doc);
					queryProcessed.addDocTokens(docTokens);
					queryProcessed.addDocIdfs(docIdfs);
				}
			}
		}

		return queryProcessed;
	}

	private List<QueryProcessed> getProcessedQueries(List<Query> queries, QueryType type) {
		return queries.stream().map(q -> getProcessedQuery(q, preProcessor, type)).collect(Collectors.toList());
	}

	public void makeQueryIdf(List<Query> queries, QueryType type, String outputPath, boolean noWebpagesDocs, boolean noFulltext) throws IOException {
		List<QueryProcessed> processedQueries = getProcessedQueries(queries, type);

		IdfMake idfMake = new IdfMake(outputPath);

		for (QueryProcessed processedQuery : processedQueries) {
			if (processedQuery.getNameTokens() != null) {
				idfMake.addTerms(processedQuery.getNameTokens());
			}
			if (processedQuery.getDescriptionTokens() != null) {
				idfMake.addTerms(processedQuery.getDescriptionTokens());
			}
			for (List<String> keywordTokens : processedQuery.getKeywordsTokens()) {
				if (keywordTokens != null) idfMake.addTerms(keywordTokens);
			}

			if (!noWebpagesDocs) {
				for (List<String> webpageTokens : processedQuery.getWebpagesTokens()) {
					if (webpageTokens != null) idfMake.addTerms(webpageTokens);
				}
				for (List<String> docTokens : processedQuery.getDocsTokens()) {
					if (docTokens != null) idfMake.addTerms(docTokens);
				}
			}

			for (PublicationProcessed processedPublication : processedQuery.getProcessedPublications()) {
				if (processedPublication == null) continue;

				if (processedPublication.getTitleTokens() != null) {
					idfMake.addTerms(processedPublication.getTitleTokens());
				}
				for (List<String> keywordTokens : processedPublication.getKeywordsTokens()) {
					if (keywordTokens != null) idfMake.addTerms(keywordTokens);
				}
				for (List<String> meshTermTokens : processedPublication.getMeshTermsTokens()) {
					if (meshTermTokens != null) idfMake.addTerms(meshTermTokens);
				}
				for (List<String> efoTermTokens : processedPublication.getEfoTermsTokens()) {
					if (efoTermTokens != null) idfMake.addTerms(efoTermTokens);
				}
				for (List<String> goTermTokens : processedPublication.getGoTermsTokens()) {
					if (goTermTokens != null) idfMake.addTerms(goTermTokens);
				}
				if (processedPublication.getAbstractTokens() != null) {
					idfMake.addTerms(processedPublication.getAbstractTokens());
				}

				if (!noFulltext) {
					if (processedPublication.getFulltextTokens() != null) {
						idfMake.addTerms(processedPublication.getFulltextTokens());
					}
				}
			}

			idfMake.endDocument();
		}

		idfMake.writeOutput();
	}
}
