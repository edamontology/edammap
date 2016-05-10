package edammapper.processing;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mapdb.DBException;

import edammapper.edam.Concept;
import edammapper.edam.EdamUri;
import edammapper.fetching.Database;
import edammapper.fetching.Fetcher;
import edammapper.fetching.MeshTerm;
import edammapper.fetching.MinedTerm;
import edammapper.fetching.Publication;
import edammapper.idf.Idf;
import edammapper.idf.IdfMake;
import edammapper.mapping.MapperIdfMultiplierArgs;
import edammapper.preprocessing.PreProcessor;
import edammapper.query.Keyword;
import edammapper.query.Query;
import edammapper.query.QueryType;

public class Processor {

	private final PreProcessor preProcessor;

	private final Fetcher fetcher;

	private final Database database;

	private final Idf queryIdf;

	public Processor(ProcessorArgs args) throws IOException, DBException {
		this.preProcessor = new PreProcessor(args.getPreProcessorArgs());

		if (args.isFetchingDisabled()) {
			this.fetcher = null;
		} else {
			this.fetcher = new Fetcher();
		}

		if (args.getDatabase() == null || args.getDatabase().isEmpty()) {
			this.database = null;
		} else {
			this.database = new Database(args.getDatabase());
		}

		if (args.getQueryIdf() == null || args.getQueryIdf().isEmpty()) {
			this.queryIdf = null;
		} else {
			this.queryIdf = new Idf(args.getQueryIdf());
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

	private void anonymizeProcessedConcept(ConceptProcessed processedConcept, MapperIdfMultiplierArgs args) {
		if (processedConcept.getLabelTokens() != null && args.getLabelMultiplier() > 0) {
			processedConcept.addTokens(processedConcept.getLabelTokens());
			processedConcept.addIdfs(processedConcept.getLabelIdfs());
			processedConcept.addScaling(args.isEnableLabelSynonymsIdf() ? args.getConceptIdfScaling() : 0);
			processedConcept.addMultiplier(args.getLabelMultiplier());
		}
		if (args.getExactSynonymMultiplier() > 0) {
			for (int i = 0; i < processedConcept.getExactSynonymsTokens().size(); ++i) {
				processedConcept.addTokens(processedConcept.getExactSynonymsTokens().get(i));
				processedConcept.addIdfs(processedConcept.getExactSynonymsIdfs().get(i));
				processedConcept.addScaling(args.isEnableLabelSynonymsIdf() ? args.getConceptIdfScaling() : 0);
				processedConcept.addMultiplier(args.getExactSynonymMultiplier());
			}
		}
		if (args.getNarrowBroadMultiplier() > 0) {
			for (int i = 0; i < processedConcept.getNarrowSynonymsTokens().size(); ++i) {
				processedConcept.addTokens(processedConcept.getNarrowSynonymsTokens().get(i));
				processedConcept.addIdfs(processedConcept.getNarrowSynonymsIdfs().get(i));
				processedConcept.addScaling(args.isEnableLabelSynonymsIdf() ? args.getConceptIdfScaling() : 0);
				processedConcept.addMultiplier(args.getNarrowBroadMultiplier());
			}
		}
		if (args.getNarrowBroadMultiplier() > 0) {
			for (int i = 0; i < processedConcept.getBroadSynonymsTokens().size(); ++i) {
				processedConcept.addTokens(processedConcept.getBroadSynonymsTokens().get(i));
				processedConcept.addIdfs(processedConcept.getBroadSynonymsIdfs().get(i));
				processedConcept.addScaling(args.isEnableLabelSynonymsIdf() ? args.getConceptIdfScaling() : 0);
				processedConcept.addMultiplier(args.getNarrowBroadMultiplier());
			}
		}
		if (processedConcept.getDefinitionTokens() != null && args.getDefinitionMultiplier() > 0) {
			processedConcept.addTokens(processedConcept.getDefinitionTokens());
			processedConcept.addIdfs(processedConcept.getDefinitionIdfs());
			processedConcept.addScaling(args.getConceptIdfScaling());
			processedConcept.addMultiplier(args.getDefinitionMultiplier());
		}
		if (processedConcept.getCommentTokens() != null && args.getCommentMultiplier() > 0) {
			processedConcept.addTokens(processedConcept.getCommentTokens());
			processedConcept.addIdfs(processedConcept.getCommentIdfs());
			processedConcept.addScaling(args.getConceptIdfScaling());
			processedConcept.addMultiplier(args.getCommentMultiplier());
		}
	}

	public Map<EdamUri, ConceptProcessed> getProcessedConcepts(Map<EdamUri, Concept> concepts, MapperIdfMultiplierArgs args) {
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
			anonymizeProcessedConcept(processedConcept, args);
		}

		return processedConcepts;
	}

	private PublicationProcessed processPublication(Publication publication, PreProcessor pp) {
		PublicationProcessed publicationProcessed = new PublicationProcessed();

		if (publication.getTitle() != null && !publication.getTitle().isEmpty()) {
			List<String> titleTokens = pp.process(publication.getTitle());
			if (!(titleTokens.size() == 1 && titleTokens.get(0).isEmpty())) {
				publicationProcessed.setTitleTokens(titleTokens);
				if (queryIdf != null) {
					publicationProcessed.setTitleIdfs(queryIdf.getIdf(titleTokens));
				}
			}
		}

		for (String keyword : publication.getKeywords()) {
			List<String> keywordTokens = pp.process(keyword);
			List<Double> keywordIdfs = null;
			if (keywordTokens.size() == 1 && keywordTokens.get(0).isEmpty()) {
				keywordTokens = null;
			} else if (queryIdf != null) {
				keywordIdfs = queryIdf.getIdf(keywordTokens);
			}
			publicationProcessed.addKeywordTokens(keywordTokens);
			publicationProcessed.addKeywordIdfs(keywordIdfs);
		}

		for (MeshTerm meshTerm : publication.getMeshTerms()) {
			List<String> meshTermTokens = null;
			List<Double> meshTermIdfs = null;
			if (meshTerm.getTerm() != null) {
				meshTermTokens = pp.process(meshTerm.getTerm());
				if (meshTermTokens.size() == 1 && meshTermTokens.get(0).isEmpty()) {
					meshTermTokens = null;
				} else if (queryIdf != null) {
					meshTermIdfs = queryIdf.getIdf(meshTermTokens);
				}
			}
			publicationProcessed.addMeshTermTokens(meshTermTokens);
			publicationProcessed.addMeshTermIdfs(meshTermIdfs);
		}

		if (publication.getAbstract() != null && !publication.getAbstract().isEmpty()) {
			List<String> abstractTokens = pp.process(publication.getAbstract());
			if (!(abstractTokens.size() == 1 && abstractTokens.get(0).isEmpty())) {
				publicationProcessed.setAbstractTokens(abstractTokens);
				if (queryIdf != null) {
					publicationProcessed.setAbstractIdfs(queryIdf.getIdf(abstractTokens));
				}
			}
		}

		int fulltextWordCount = 0;
		if (publication.getFulltext() != null && !publication.getFulltext().isEmpty()) {
			List<String> fulltextTokens = pp.process(publication.getFulltext());
			if (!(fulltextTokens.size() == 1 && fulltextTokens.get(0).isEmpty())) {
				publicationProcessed.setFulltextTokens(fulltextTokens);
				fulltextWordCount = fulltextTokens.size();
				if (queryIdf != null) {
					publicationProcessed.setFulltextIdfs(queryIdf.getIdf(fulltextTokens));
				}
			}
		}

		for (MinedTerm efoTerm : publication.getEfoTerms()) {
			List<String> efoTermTokens = null;
			List<Double> efoTermIdfs = null;
			Double efoTermFrequency = null;
			if (efoTerm.getTerm() != null) {
				efoTermTokens = pp.process(efoTerm.getTerm());
				if (efoTermTokens.size() == 1 && efoTermTokens.get(0).isEmpty()) {
					efoTermTokens = null;
				} else {
					efoTermFrequency = efoTerm.getFrequency(fulltextWordCount);
					if (queryIdf != null) {
						efoTermIdfs = queryIdf.getIdf(efoTermTokens);
					}
				}
			}
			publicationProcessed.addEfoTermTokens(efoTermTokens);
			publicationProcessed.addEfoTermIdfs(efoTermIdfs);
			publicationProcessed.addEfoTermFrequency(efoTermFrequency);
		}

		for (MinedTerm goTerm : publication.getGoTerms()) {
			List<String> goTermTokens = null;
			List<Double> goTermIdfs = null;
			Double goTermFrequency = null;
			if (goTerm.getTerm() != null) {
				goTermTokens = pp.process(goTerm.getTerm());
				if (goTermTokens.size() == 1 && goTermTokens.get(0).isEmpty()) {
					goTermTokens = null;
				} else {
					goTermFrequency = goTerm.getFrequency(fulltextWordCount);
					if (queryIdf != null) {
						goTermIdfs = queryIdf.getIdf(goTermTokens);
					}
				}
			}
			publicationProcessed.addGoTermTokens(goTermTokens);
			publicationProcessed.addGoTermIdfs(goTermIdfs);
			publicationProcessed.addGoTermFrequency(goTermFrequency);
		}

		return publicationProcessed;
	}

	public QueryProcessed getProcessedQuery(Query query, PreProcessor pp, QueryType type) {
		QueryProcessed queryProcessed = new QueryProcessed();

		// TODO remove more besides webpageUrl and DocUrl ?
		boolean removeBroken = (type == QueryType.BioConductor);

		boolean databaseUpdated = false;

		if (query.getName() != null) {
			List<String> nameTokens = pp.process(query.getName());
			if (!(nameTokens.size() == 1 && nameTokens.get(0).isEmpty())) {
				queryProcessed.setNameTokens(nameTokens);
				if (queryIdf != null) {
					queryProcessed.setNameIdfs(queryIdf.getIdf(nameTokens));
				}
			}
		}

		if (query.getWebpageUrls() != null) {
			for (Iterator<String> it = query.getWebpageUrls().iterator(); it.hasNext(); ) {
				String webpageUrl = it.next();
				String webpage = null;
				if (database != null) {
					webpage = database.getWebpage(webpageUrl);
				}
				if (webpage == null && fetcher != null) {
					webpage = fetcher.getWebpage(webpageUrl);
					if (webpage != null && database != null) {
						database.putWebpage(webpageUrl, webpage);
						databaseUpdated = true;
					}
				}
				List<String> webpageTokens = null;
				List<Double> webpageIdfs = null;
				if (webpage != null) {
					webpageTokens = pp.process(webpage);
					if (webpageTokens.size() == 1 && webpageTokens.get(0).isEmpty()) {
						webpageTokens = null;
					} else if (queryIdf != null) {
						webpageIdfs = queryIdf.getIdf(webpageTokens);
					}
				}
				if (webpageTokens == null && removeBroken) {
					it.remove();
				} else {
					queryProcessed.addWebpageTokens(webpageTokens);
					queryProcessed.addWebpageIdfs(webpageIdfs);
				}
			}
		}

		if (query.getDescription() != null) {
			List<String> descriptionTokens = pp.process(query.getDescription());
			if (!(descriptionTokens.size() == 1 && descriptionTokens.get(0).isEmpty())) {
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
					if (keywordTokens.size() == 1 && keywordTokens.get(0).isEmpty()) {
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
			for (String publicationId : query.getPublicationIds()) {
				if (Fetcher.isDoi(publicationId)) {
					publicationId = Fetcher.normalizeDoi(publicationId);
				}
				Publication publication = null;
				if (database != null) {
					publication = database.getPublication(publicationId);
				}
				if (publication == null && fetcher != null) {
					publication = fetcher.getPublication(publicationId);
					if (publication != null && database != null) {
						database.putPublication(publicationId, publication);
						databaseUpdated = true;
					}
				}
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
			for (Iterator<String> it = query.getDocUrls().iterator(); it.hasNext(); ) {
				String docUrl = it.next();
				String doc = null;
				if (database != null) {
					doc = database.getDoc(docUrl);
				}
				if (doc == null && fetcher != null) {
					doc = fetcher.getWebpage(docUrl);
					if (doc != null && database != null) {
						database.putDoc(docUrl, doc);
						databaseUpdated = true;
					}
				}
				List<String> docTokens = null;
				List<Double> docIdfs = null;
				if (doc != null) {
					docTokens = pp.process(doc);
					if (docTokens.size() == 1 && docTokens.get(0).isEmpty()) {
						docTokens = null;
					} else if (queryIdf != null) {
						docIdfs = queryIdf.getIdf(docTokens);
					}
				}
				if (docTokens == null && removeBroken) {
					it.remove();
				} else {
					queryProcessed.addDocTokens(docTokens);
					queryProcessed.addDocIdfs(docIdfs);
				}
			}
		}

		if (databaseUpdated) {
			database.commit();
		}

		return queryProcessed;
	}

	private List<QueryProcessed> getProcessedQueries(List<Query> queries, QueryType type) {
		return queries.stream().map(q -> getProcessedQuery(q, preProcessor, type)).collect(Collectors.toList());
	}

	void makeQueryIdf(List<Query> queries, QueryType type, String outputPath, boolean noWebpagesDocs, boolean noFulltext) throws IOException {
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
