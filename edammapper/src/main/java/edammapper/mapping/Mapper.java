package edammapper.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edammapper.edam.Branch;
import edammapper.edam.EdamUri;
import edammapper.processing.ConceptProcessed;
import edammapper.processing.PublicationProcessed;
import edammapper.processing.QueryProcessed;
import edammapper.query.Query;

public class Mapper {

	private class M {
		private M(int to, int from, double score) {
			this.to = to;
			this.from = from;
			this.score = score;
		}
		private int to;
		private int from;
		private double score;
	}

	private final Map<EdamUri, ConceptProcessed> processedConcepts;

	private final Levenshtein levenshtein;

	public Mapper(Map<EdamUri, ConceptProcessed> processedConcepts) {
		// null
		this.processedConcepts = processedConcepts;
		this.levenshtein = new Levenshtein();
	}

	private List<M> getTokenMatches(List<String> tos, List<String> froms, int compoundWords, double mismatchMultiplier, double matchMinimum) {
		List<M> matches = new ArrayList<>();

		for (int toCW = 0; toCW <= compoundWords; ++toCW) {
			for (int toI = 0; toI < tos.size() - toCW; ++toI) {
				String to = tos.get(toI);
				for (int toJ = 1; toJ <= toCW; ++toJ) {
					to += " " + tos.get(toI + toJ);
				}

				for (int fromCW = 0; fromCW <= compoundWords; ++fromCW) {
					// Only do one-to-many and many-to-one matches
					if (toCW > 0 && fromCW > 0) break;

					for (int fromI = 0; fromI < froms.size() - fromCW; ++fromI) {
						String from = froms.get(fromI);
						for (int fromJ = 1; fromJ <= fromCW; ++fromJ) {
							from += " " + froms.get(fromI + fromJ);
						}

						double score = 0;
						if (matchMinimum < 1 || toCW > 0 || fromCW > 0) {
							int l = Math.max(from.length(), to.length());
							int d_max = 0;
							if (matchMinimum < 1) {
								d_max += (int) (l * (1 - matchMinimum) / mismatchMultiplier);
							}
							if (toCW > 0 || fromCW > 0) {
								d_max += toCW + fromCW;
							}
							int d = levenshtein.improved(from, to, d_max);
							if (d > -1) {
								score = 1 - (mismatchMultiplier * d) / (double)l;
							} else {
								score = 0;
							}
						} else {
							if (from.equals(to)) score = 1;
							else score = 0;
						}

						if (score > 0) {
							for (int toJ = 0; toJ <= toCW; ++toJ) {
								for (int fromJ = 0; fromJ <= fromCW; ++fromJ) {
									matches.add(new M(toI + toJ, fromI + fromJ, score));
								}
							}
						}
					}
				}
			}
		}

		if (compoundWords > 0) {
			matches.sort((m1, m2) -> (m1.to > m2.to ? 1 : (m1.to < m2.to ? -1 : 0)));
		}

		return matches;
	}

	private void scorePositionIfBest(double[] bestPositionScores, double score) {
		if (bestPositionScores.length == 1) {
			if (score > bestPositionScores[0]) {
				bestPositionScores[0] = score;
			}
		} else if (bestPositionScores.length == 2) {
			if (score > bestPositionScores[1]) {
				if (score > bestPositionScores[0]) {
					bestPositionScores[1] = bestPositionScores[0];
					bestPositionScores[0] = score;
				} else {
					bestPositionScores[1] = score;
				}
			}
		}
	}

	private void scorePosition(double[] bestPositionScores, int to, int from, List<M> matches, int index, double[] positionOffScores, double positionMatchScaling, double matchMinimum) {
		for (int i = index - 1; i >= 0; --i) {
			M matcho = matches.get(i);

			if (matcho.to < to - positionOffScores.length) break;

			int d = to - matcho.to;

			int c = 0;
			if (matcho.to < to) {
				if (matcho.from < from) c = -2;
				else if (matcho.from > from) c = -1;
			}

			int positionOff = Math.abs(matcho.from - from) + d + c;
			if (positionOff < positionOffScores.length) {
				double score = positionOffScores[positionOff];

				if (matchMinimum < 1 && positionMatchScaling > 0) {
					score *= Math.pow(matcho.score, positionMatchScaling);
				}

				scorePositionIfBest(bestPositionScores, score);
			}
		}

		for (int i = index + 1; i < matches.size(); ++i) {
			M matcho = matches.get(i);

			if (matcho.to > to + positionOffScores.length) break;

			int d = matcho.to - to;

			int c = 0;
			if (matcho.to > to) {
				if (matcho.from < from) c = -1;
				else if (matcho.from > from) c = -2;
			}

			int positionOff = Math.abs(matcho.from - from) + d + c;
			if (positionOff < positionOffScores.length) {
				double score = positionOffScores[positionOff];

				if (matchMinimum < 1 && positionMatchScaling > 0) {
					score *= Math.pow(matcho.score, positionMatchScaling);
				}

				scorePositionIfBest(bestPositionScores, score);
			}
		}
	}

	private void calculateScores(double[] bestScores, List<String> tos, List<String> froms, List<Double> fromIdfs, double fromIdfScaling, double fromMultiplier, MapperAlgorithmArgs args) {
		double[] positionOffScores = { 1, args.getPositionOffBy1(), args.getPositionOffBy2() };

		List<M> matches = getTokenMatches(tos, froms, args.getCompoundWords(), args.getMismatchMultiplier(), args.getMatchMinimum());

		double bestScore = 0;

		for (int i = 0; i < matches.size(); ++i) {
			M match = matches.get(i);

			double[] bestPositionScores;
			if ((match.to == 0 && match.from == 0) ||
				(match.to == tos.size() - 1 && match.from == froms.size() - 1) ||
				(tos.size() == 1)) {

				bestPositionScores = new double[1];
				if (tos.size() == 1) {
					bestPositionScores[0] = 1;
				} else {
					bestPositionScores[0] = 0;
				}
			} else {
				bestPositionScores = new double[2];
				bestPositionScores[0] = 0;
				bestPositionScores[1] = 0;
			}

			if (tos.size() > 1 && args.getPositionLoss() > 0) {
				scorePosition(bestPositionScores, match.to, match.from, matches, i, positionOffScores, args.getPositionMatchScaling(), args.getMatchMinimum());
			}

			double bestPositionScore;
			if (bestPositionScores.length == 1) {
				bestPositionScore = bestPositionScores[0];
			} else {
				bestPositionScore = (bestPositionScores[0] + bestPositionScores[1]) / 2;
			}

			double score = match.score - args.getPositionLoss() * (1 - bestPositionScore);
			if (score < 0) {
				score = 0;
			}
			if (fromIdfScaling > 0) {
				score *= Math.pow(fromIdfs.get(match.from), fromIdfScaling);
			}

			if (i > 0 && match.to > matches.get(i - 1).to) {
				bestScore *= fromMultiplier;
				if (bestScore > bestScores[matches.get(i - 1).to]) {
					bestScores[matches.get(i - 1).to] = bestScore;
				}
				bestScore = 0;
			}

			if (score > bestScore) {
				bestScore = score;
			}
		}
		if (matches.size() > 0) {
			bestScore *= fromMultiplier;
			if (bestScore > bestScores[matches.get(matches.size() - 1).to]) {
				bestScores[matches.get(matches.size() - 1).to] = bestScore;
			}
		}
	}

	private double getScore(List<String> toTokens, List<Double> toIdfs, double toIdfScaling, double toMultiplier, List<List<String>> fromsTokens, List<List<Double>> fromsIdfs, List<Double> fromIdfScalings, List<Double> fromMultipliers, MapperAlgorithmArgs args) {
		double[] bestScores = new double[toTokens.size()];
		Arrays.fill(bestScores, 0);

		for (int i = 0; i < fromsTokens.size(); ++i) {
			double multiplier = fromMultipliers.get(i);
			if (args.getScoreScaling() > 0) {
				multiplier = Math.pow(multiplier, 1 / args.getScoreScaling());
			}
			calculateScores(bestScores, toTokens, fromsTokens.get(i), fromsIdfs.get(i), fromIdfScalings.get(i), multiplier, args);
		}

		double bestScoresSum = 0;
		for (int i = 0; i < bestScores.length; ++i) {
			if (toIdfScaling > 0) {
				bestScoresSum += bestScores[i] * Math.pow(toIdfs.get(i), toIdfScaling);
			} else {
				bestScoresSum += bestScores[i];
			}
		}
		double score = bestScoresSum / (double)bestScores.length;

		if (args.getScoreScaling() > 0) {
			score = Math.pow(score, args.getScoreScaling());
		}

		score *= toMultiplier;

		return score;
	}

	// TODO try to make less copy-pasty
	private ConceptMatch toConceptFromQuery(ConceptProcessed processedConcept, QueryProcessed processedQuery, QueryMatchType type, MapperAlgorithmArgs algorithmArgs, MapperIdfMultiplierArgs idfMultiplierArgs, Branch branch) {
		List<List<String>> fromsTokens = new ArrayList<>();
		List<List<Double>> fromsIdfs = new ArrayList<>();
		List<Double> fromIdfScalings = new ArrayList<>();
		List<Double> fromMultipliers = new ArrayList<>();

		boolean queryIdfDisabled = idfMultiplierArgs.getDisableQueryIdfBranches().contains(branch);

		switch (type) {
		case name:
			if (processedQuery.getNameTokens() != null && idfMultiplierArgs.getNameNormalizer() > 0) {
				fromsTokens.add(processedQuery.getNameTokens());
				fromsIdfs.add(processedQuery.getNameIdfs());
				fromIdfScalings.add((queryIdfDisabled || processedQuery.getNameIdfs() == null || idfMultiplierArgs.isDisableNameKeywordsIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
				fromMultipliers.add(Double.valueOf(1));
			}
			break;
		case keyword:
			if (idfMultiplierArgs.getKeywordNormalizer() > 0) {
				for (int i = 0; i < processedQuery.getKeywordsTokens().size(); ++i) {
					if (processedQuery.getKeywordsTokens().get(i) == null) continue;
					fromsTokens.add(processedQuery.getKeywordsTokens().get(i));
					fromsIdfs.add(processedQuery.getKeywordsIdfs().get(i));
					fromIdfScalings.add((queryIdfDisabled || processedQuery.getKeywordsIdfs().get(i) == null || idfMultiplierArgs.isDisableNameKeywordsIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
					fromMultipliers.add(Double.valueOf(1));
				}
			}
			break;
		case description:
			if (processedQuery.getDescriptionTokens() != null && idfMultiplierArgs.getDescriptionNormalizer() > 0) {
				fromsTokens.add(processedQuery.getDescriptionTokens());
				fromsIdfs.add(processedQuery.getDescriptionIdfs());
				fromIdfScalings.add((queryIdfDisabled || processedQuery.getDescriptionIdfs() == null || idfMultiplierArgs.isDisableDescriptionIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
				fromMultipliers.add(Double.valueOf(1));
			}
			break;
		case publication_title:
			for (PublicationProcessed processedPublication : processedQuery.getProcessedPublications()) {
				if (processedPublication == null) continue;
				if (processedPublication.getTitleTokens() != null && idfMultiplierArgs.getPublicationTitleNormalizer() > 0) {
					fromsTokens.add(processedPublication.getTitleTokens());
					fromsIdfs.add(processedPublication.getTitleIdfs());
					fromIdfScalings.add((queryIdfDisabled || processedPublication.getTitleIdfs() == null || idfMultiplierArgs.isDisableTitleKeywordsIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
					fromMultipliers.add(Double.valueOf(1));
				}
			}
			break;
		case publication_keyword:
			for (PublicationProcessed processedPublication : processedQuery.getProcessedPublications()) {
				if (processedPublication == null) continue;
				if (idfMultiplierArgs.getPublicationKeywordNormalizer() > 0) {
					for (int i = 0; i < processedPublication.getKeywordsTokens().size(); ++i) {
						if (processedPublication.getKeywordsTokens().get(i) == null) continue;
						fromsTokens.add(processedPublication.getKeywordsTokens().get(i));
						fromsIdfs.add(processedPublication.getKeywordsIdfs().get(i));
						fromIdfScalings.add((queryIdfDisabled || processedPublication.getKeywordsIdfs().get(i) == null || idfMultiplierArgs.isDisableTitleKeywordsIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
						fromMultipliers.add(Double.valueOf(1));
					}
				}
			}
			break;
		case publication_mesh:
			for (PublicationProcessed processedPublication : processedQuery.getProcessedPublications()) {
				if (processedPublication == null) continue;
				if (idfMultiplierArgs.getPublicationMeshNormalizer() > 0) {
					for (int i = 0; i < processedPublication.getMeshTermsTokens().size(); ++i) {
						if (processedPublication.getMeshTermsTokens().get(i) == null) continue;
						fromsTokens.add(processedPublication.getMeshTermsTokens().get(i));
						fromsIdfs.add(processedPublication.getMeshTermsIdfs().get(i));
						fromIdfScalings.add((queryIdfDisabled || processedPublication.getMeshTermsIdfs().get(i) == null || idfMultiplierArgs.isDisableTitleKeywordsIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
						fromMultipliers.add(Double.valueOf(1));
					}
				}
			}
			break;
		case publication_efo:
		case publication_go:
			for (PublicationProcessed processedPublication : processedQuery.getProcessedPublications()) {
				if (processedPublication == null) continue;
				if (idfMultiplierArgs.getPublicationMinedNormalizer() > 0) {
					for (int i = 0; i < processedPublication.getEfoTermsTokens().size(); ++i) {
						if (processedPublication.getEfoTermsTokens().get(i) == null) continue;
						fromsTokens.add(processedPublication.getEfoTermsTokens().get(i));
						fromsIdfs.add(processedPublication.getEfoTermsIdfs().get(i));
						fromIdfScalings.add((queryIdfDisabled || processedPublication.getEfoTermsIdfs().get(i) == null || idfMultiplierArgs.isDisableTitleKeywordsIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
						fromMultipliers.add(Double.valueOf(1));
					}
				}
				if (idfMultiplierArgs.getPublicationMinedNormalizer() > 0) {
					for (int i = 0; i < processedPublication.getGoTermsTokens().size(); ++i) {
						if (processedPublication.getGoTermsTokens().get(i) == null) continue;
						fromsTokens.add(processedPublication.getGoTermsTokens().get(i));
						fromsIdfs.add(processedPublication.getGoTermsIdfs().get(i));
						fromIdfScalings.add((queryIdfDisabled || processedPublication.getGoTermsIdfs().get(i) == null || idfMultiplierArgs.isDisableTitleKeywordsIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
						fromMultipliers.add(Double.valueOf(1));
					}
				}
			}
			break;
		case publication_abstract:
			for (PublicationProcessed processedPublication : processedQuery.getProcessedPublications()) {
				if (processedPublication == null) continue;
				if (processedPublication.getAbstractTokens() != null && idfMultiplierArgs.getPublicationAbstractNormalizer() > 0) {
					fromsTokens.add(processedPublication.getAbstractTokens());
					fromsIdfs.add(processedPublication.getAbstractIdfs());
					fromIdfScalings.add((queryIdfDisabled || processedPublication.getAbstractIdfs() == null || idfMultiplierArgs.isDisableAbstractIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
					fromMultipliers.add(Double.valueOf(1));
				}
			}
			break;
		case publication_fulltext:
			for (PublicationProcessed processedPublication : processedQuery.getProcessedPublications()) {
				if (processedPublication == null) continue;
				if (processedPublication.getFulltextTokens() != null && idfMultiplierArgs.getPublicationFulltextNormalizer() > 0) {
					fromsTokens.add(processedPublication.getFulltextTokens());
					fromsIdfs.add(processedPublication.getFulltextIdfs());
					fromIdfScalings.add((queryIdfDisabled || processedPublication.getFulltextIdfs() == null) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
					fromMultipliers.add(Double.valueOf(1));
				}
			}
			break;
		case doc:
			if (idfMultiplierArgs.getDocNormalizer() > 0) {
				for (int i = 0; i < processedQuery.getDocsTokens().size(); ++i) {
					if (processedQuery.getDocsTokens().get(i) == null) continue;
					fromsTokens.add(processedQuery.getDocsTokens().get(i));
					fromsIdfs.add(processedQuery.getDocsIdfs().get(i));
					fromIdfScalings.add((queryIdfDisabled || processedQuery.getDocsIdfs().get(i) == null) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
					fromMultipliers.add(Double.valueOf(1));
				}
			}
			break;
		case webpage:
			if (idfMultiplierArgs.getWebpageNormalizer() > 0) {
				for (int i = 0; i < processedQuery.getWebpagesTokens().size(); ++i) {
					if (processedQuery.getWebpagesTokens().get(i) == null) continue;
					fromsTokens.add(processedQuery.getWebpagesTokens().get(i));
					fromsIdfs.add(processedQuery.getWebpagesIdfs().get(i));
					fromIdfScalings.add((queryIdfDisabled || processedQuery.getWebpagesIdfs().get(i) == null) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
					fromMultipliers.add(Double.valueOf(1));
				}
			}
			break;
		default:
			break;
		}

		double bestScore = 0;
		ConceptMatchType matchType = ConceptMatchType.none;
		int synonymIndex = -1;

		if (processedConcept.getLabelTokens() != null && idfMultiplierArgs.getLabelMultiplier() > 0) {
			double idfScaling = idfMultiplierArgs.isEnableLabelSynonymsIdf() ? idfMultiplierArgs.getConceptIdfScaling() : 0;
			double score = getScore(processedConcept.getLabelTokens(), processedConcept.getLabelIdfs(), idfScaling, idfMultiplierArgs.getLabelMultiplier(), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
			if (score > bestScore) {
				bestScore = score;
				matchType = ConceptMatchType.label;
			}
		}
		if (idfMultiplierArgs.getExactSynonymMultiplier() > 0) {
			for (int i = 0; i < processedConcept.getExactSynonymsTokens().size(); ++i) {
				double idfScaling = idfMultiplierArgs.isEnableLabelSynonymsIdf() ? idfMultiplierArgs.getConceptIdfScaling() : 0;
				double score = getScore(processedConcept.getExactSynonymsTokens().get(i), processedConcept.getExactSynonymsIdfs().get(i), idfScaling, idfMultiplierArgs.getExactSynonymMultiplier(), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
				if (score > bestScore) {
					bestScore = score;
					matchType = ConceptMatchType.exact_synonym;
					synonymIndex = i;
				}
			}
		}
		if (idfMultiplierArgs.getNarrowBroadMultiplier() > 0) {
			for (int i = 0; i < processedConcept.getNarrowSynonymsTokens().size(); ++i) {
				double idfScaling = idfMultiplierArgs.isEnableLabelSynonymsIdf() ? idfMultiplierArgs.getConceptIdfScaling() : 0;
				double score = getScore(processedConcept.getNarrowSynonymsTokens().get(i), processedConcept.getNarrowSynonymsIdfs().get(i), idfScaling, idfMultiplierArgs.getNarrowBroadMultiplier(), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
				if (score > bestScore) {
					bestScore = score;
					matchType = ConceptMatchType.narrow_synonym;
					synonymIndex = i;
				}
			}
		}
		if (idfMultiplierArgs.getNarrowBroadMultiplier() > 0) {
			for (int i = 0; i < processedConcept.getBroadSynonymsTokens().size(); ++i) {
				double idfScaling = idfMultiplierArgs.isEnableLabelSynonymsIdf() ? idfMultiplierArgs.getConceptIdfScaling() : 0;
				double score = getScore(processedConcept.getBroadSynonymsTokens().get(i), processedConcept.getBroadSynonymsIdfs().get(i), idfScaling, idfMultiplierArgs.getNarrowBroadMultiplier(), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
				if (score > bestScore) {
					bestScore = score;
					matchType = ConceptMatchType.broad_synonym;
					synonymIndex = i;
				}
			}
		}
		if (processedConcept.getDefinitionTokens() != null && idfMultiplierArgs.getDefinitionMultiplier() > 0) {
			double idfScaling = idfMultiplierArgs.getConceptIdfScaling();
			double score = getScore(processedConcept.getDefinitionTokens(), processedConcept.getDefinitionIdfs(), idfScaling, idfMultiplierArgs.getDefinitionMultiplier(), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
			if (score > bestScore) {
				bestScore = score;
				matchType = ConceptMatchType.definition;
			}
		}
		if (processedConcept.getCommentTokens() != null && idfMultiplierArgs.getCommentMultiplier() > 0) {
			double idfScaling = idfMultiplierArgs.getConceptIdfScaling();
			double score = getScore(processedConcept.getCommentTokens(), processedConcept.getCommentIdfs(), idfScaling, idfMultiplierArgs.getCommentMultiplier(), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
			if (score > bestScore) {
				bestScore = score;
				matchType = ConceptMatchType.comment;
			}
		}

		return new ConceptMatch(bestScore, matchType, synonymIndex);
	}

	// TODO try to make less copy-pasty
	private QueryMatch toQueryFromConcept(QueryProcessed processedQuery, ConceptProcessed processedConcept, QueryMatchType type, MapperAlgorithmArgs algorithmArgs, MapperIdfMultiplierArgs idfMultiplierArgs, Branch branch) {
		List<List<String>> fromsTokens = processedConcept.getTokens();
		List<List<Double>> fromsIdfs = processedConcept.getIdfs();
		List<Double> fromIdfScalings = processedConcept.getIdfScalings();
		List<Double> fromMultipliers = processedConcept.getMultipliers();

		boolean queryIdfDisabled = idfMultiplierArgs.getDisableQueryIdfBranches().contains(branch);

		double bestScore = 0;
		QueryMatchType matchType = QueryMatchType.none;
		int index = -1;
		int indexInPublication = -1;

		switch (type) {
		case name:
			if (processedQuery.getNameTokens() != null && idfMultiplierArgs.getNameNormalizer() > 0) {
				List<Double> idfs = processedQuery.getNameIdfs();
				double idfScaling = ((idfs == null || queryIdfDisabled || idfMultiplierArgs.isDisableNameKeywordsIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
				double score = getScore(processedQuery.getNameTokens(), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
				if (score > bestScore) {
					bestScore = score;
					matchType = QueryMatchType.name;
				}
			}
			break;
		case keyword:
			if (idfMultiplierArgs.getKeywordNormalizer() > 0) {
				for (int i = 0; i < processedQuery.getKeywordsTokens().size(); ++i) {
					if (processedQuery.getKeywordsTokens().get(i) == null) continue;
					List<Double> idfs = processedQuery.getKeywordsIdfs().get(i);
					double idfScaling = ((idfs == null || queryIdfDisabled || idfMultiplierArgs.isDisableNameKeywordsIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
					double score = getScore(processedQuery.getKeywordsTokens().get(i), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
					if (score > bestScore) {
						bestScore = score;
						matchType = QueryMatchType.keyword;
						index = i;
					}
				}
			}
			break;
		case description:
			if (processedQuery.getDescriptionTokens() != null && idfMultiplierArgs.getDescriptionNormalizer() > 0) {
				List<Double> idfs = processedQuery.getDescriptionIdfs();
				double idfScaling = ((idfs == null || queryIdfDisabled || idfMultiplierArgs.isDisableDescriptionIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
				double score = getScore(processedQuery.getDescriptionTokens(), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
				if (score > bestScore) {
					bestScore = score;
					matchType = QueryMatchType.description;
				}
			}
			break;
		case publication_title:
			for (int i = 0; i < processedQuery.getProcessedPublications().size(); ++i) {
				PublicationProcessed processedPublication = processedQuery.getProcessedPublications().get(i);
				if (processedPublication == null) continue;
				if (processedPublication.getTitleTokens() != null && idfMultiplierArgs.getPublicationTitleNormalizer() > 0) {
					List<Double> idfs = processedPublication.getTitleIdfs();
					double idfScaling = ((idfs == null || queryIdfDisabled || idfMultiplierArgs.isDisableTitleKeywordsIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
					double score = getScore(processedPublication.getTitleTokens(), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
					if (score > bestScore) {
						bestScore = score;
						matchType = QueryMatchType.publication_title;
						index = i;
					}
				}
			}
			break;
		case publication_keyword:
			for (int i = 0; i < processedQuery.getProcessedPublications().size(); ++i) {
				PublicationProcessed processedPublication = processedQuery.getProcessedPublications().get(i);
				if (processedPublication == null) continue;
				if (idfMultiplierArgs.getPublicationKeywordNormalizer() > 0) {
					for (int j = 0; j < processedPublication.getKeywordsTokens().size(); ++j) {
						if (processedPublication.getKeywordsTokens().get(j) == null) continue;
						List<Double> idfs = processedPublication.getKeywordsIdfs().get(j);
						double idfScaling = ((idfs == null || queryIdfDisabled || idfMultiplierArgs.isDisableTitleKeywordsIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
						double score = getScore(processedPublication.getKeywordsTokens().get(j), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
						if (score > bestScore) {
							bestScore = score;
							matchType = QueryMatchType.publication_keyword;
							index = i;
							indexInPublication = j;
						}
					}
				}
			}
			break;
		case publication_mesh:
			for (int i = 0; i < processedQuery.getProcessedPublications().size(); ++i) {
				PublicationProcessed processedPublication = processedQuery.getProcessedPublications().get(i);
				if (processedPublication == null) continue;
				if (idfMultiplierArgs.getPublicationMeshNormalizer() > 0) {
					for (int j = 0; j < processedPublication.getMeshTermsTokens().size(); ++j) {
						if (processedPublication.getMeshTermsTokens().get(j) == null) continue;
						List<Double> idfs = processedPublication.getMeshTermsIdfs().get(j);
						double idfScaling = ((idfs == null || queryIdfDisabled || idfMultiplierArgs.isDisableTitleKeywordsIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
						double score = getScore(processedPublication.getMeshTermsTokens().get(j), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
						if (score > bestScore) {
							bestScore = score;
							matchType = QueryMatchType.publication_mesh;
							index = i;
							indexInPublication = j;
						}
					}
				}
			}
			break;
		case publication_efo:
			for (int i = 0; i < processedQuery.getProcessedPublications().size(); ++i) {
				PublicationProcessed processedPublication = processedQuery.getProcessedPublications().get(i);
				if (processedPublication == null) continue;
				if (idfMultiplierArgs.getPublicationMinedNormalizer() > 0) {
					for (int j = 0; j < processedPublication.getEfoTermsTokens().size(); ++j) {
						if (processedPublication.getEfoTermsTokens().get(j) == null) continue;
						List<Double> idfs = processedPublication.getEfoTermsIdfs().get(j);
						double idfScaling = ((idfs == null || queryIdfDisabled || idfMultiplierArgs.isDisableTitleKeywordsIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
						double score = getScore(processedPublication.getEfoTermsTokens().get(j), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
						// simulate fulltext
						score *= Math.pow(processedPublication.getEfoTermFrequencies().get(j), algorithmArgs.getScoreScaling());
						if (score > bestScore) {
							bestScore = score;
							matchType = QueryMatchType.publication_efo;
							index = i;
							indexInPublication = j;
						}
					}
				}
			}
			break;
		case publication_go:
			for (int i = 0; i < processedQuery.getProcessedPublications().size(); ++i) {
				PublicationProcessed processedPublication = processedQuery.getProcessedPublications().get(i);
				if (processedPublication == null) continue;
				if (idfMultiplierArgs.getPublicationMinedNormalizer() > 0) {
					for (int j = 0; j < processedPublication.getGoTermsTokens().size(); ++j) {
						if (processedPublication.getGoTermsTokens().get(j) == null) continue;
						List<Double> idfs = processedPublication.getGoTermsIdfs().get(j);
						double idfScaling = ((idfs == null || queryIdfDisabled || idfMultiplierArgs.isDisableTitleKeywordsIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
						double score = getScore(processedPublication.getGoTermsTokens().get(j), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
						// simulate fulltext
						score *= Math.pow(processedPublication.getGoTermFrequencies().get(j), algorithmArgs.getScoreScaling());
						if (score > bestScore) {
							bestScore = score;
							matchType = QueryMatchType.publication_go;
							index = i;
							indexInPublication = j;
						}
					}
				}
			}
			break;
		case publication_abstract:
			for (int i = 0; i < processedQuery.getProcessedPublications().size(); ++i) {
				PublicationProcessed processedPublication = processedQuery.getProcessedPublications().get(i);
				if (processedPublication == null) continue;
				if (processedPublication.getAbstractTokens() != null && idfMultiplierArgs.getPublicationAbstractNormalizer() > 0) {
					List<Double> idfs = processedPublication.getAbstractIdfs();
					double idfScaling = ((idfs == null || queryIdfDisabled || idfMultiplierArgs.isDisableAbstractIdf()) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
					double score = getScore(processedPublication.getAbstractTokens(), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
					if (score > bestScore) {
						bestScore = score;
						matchType = QueryMatchType.publication_abstract;
						index = i;
					}
				}
			}
			break;
		case publication_fulltext:
			for (int i = 0; i < processedQuery.getProcessedPublications().size(); ++i) {
				PublicationProcessed processedPublication = processedQuery.getProcessedPublications().get(i);
				if (processedPublication == null) continue;
				if (processedPublication.getFulltextTokens() != null && idfMultiplierArgs.getPublicationFulltextNormalizer() > 0) {
					List<Double> idfs = processedPublication.getFulltextIdfs();
					double idfScaling = ((idfs == null || queryIdfDisabled) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
					double score = getScore(processedPublication.getFulltextTokens(), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
					if (score > bestScore) {
						bestScore = score;
						matchType = QueryMatchType.publication_fulltext;
						index = i;
					}
				}
			}
			break;
		case doc:
			if (idfMultiplierArgs.getDocNormalizer() > 0) {
				for (int i = 0; i < processedQuery.getDocsTokens().size(); ++i) {
					if (processedQuery.getDocsTokens().get(i) == null) continue;
					List<Double> idfs = processedQuery.getDocsIdfs().get(i);
					double idfScaling = ((idfs == null || queryIdfDisabled) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
					double score = getScore(processedQuery.getDocsTokens().get(i), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
					if (score > bestScore) {
						bestScore = score;
						matchType = QueryMatchType.doc;
						index = i;
					}
				}
			}
			break;
		case webpage:
			if (idfMultiplierArgs.getWebpageNormalizer() > 0) {
				for (int i = 0; i < processedQuery.getWebpagesTokens().size(); ++i) {
					if (processedQuery.getWebpagesTokens().get(i) == null) continue;
					List<Double> idfs = processedQuery.getWebpagesIdfs().get(i);
					double idfScaling = ((idfs == null || queryIdfDisabled) ? 0 : idfMultiplierArgs.getQueryIdfScaling());
					double score = getScore(processedQuery.getWebpagesTokens().get(i), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
					if (score > bestScore) {
						bestScore = score;
						matchType = QueryMatchType.webpage;
						index = i;
					}
				}
			}
			break;
		default:
			break;
		}

		return new QueryMatch(bestScore, matchType, index, indexInPublication);
	}

	private Match getMatch(ConceptProcessed processedConcept, QueryProcessed processedQuery, QueryMatchType type, MapperAlgorithmArgs algorithmArgs, MapperIdfMultiplierArgs idfMultiplierArgs, Branch branch) {

		ConceptMatch conceptMatch;
		if (algorithmArgs.getConceptWeight() > 0) {
			conceptMatch = toConceptFromQuery(processedConcept, processedQuery, type, algorithmArgs, idfMultiplierArgs, branch);
		} else {
			conceptMatch = new ConceptMatch(0, ConceptMatchType.none, -1);
		}

		QueryMatch queryMatch;
		if (algorithmArgs.getQueryWeight() > 0) {
			queryMatch = toQueryFromConcept(processedQuery, processedConcept, type, algorithmArgs, idfMultiplierArgs, branch);
		} else {
			queryMatch = new QueryMatch(0, QueryMatchType.none, -1, -1);
		}

		double score =
			(algorithmArgs.getConceptWeight() * conceptMatch.getScore() +
			algorithmArgs.getQueryWeight() * queryMatch.getScore()) /
			(algorithmArgs.getConceptWeight() + algorithmArgs.getQueryWeight());

		return new Match(score, conceptMatch, queryMatch);
	}

	private boolean hasTokens(List<String> tokens) {
		return (tokens != null && !tokens.isEmpty());
	}

	private boolean hasListTokens(List<List<String>> tokensList) {
		if (tokensList == null) return false;
		for (List<String> tokens : tokensList) {
			if (hasTokens(tokens)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasPublicationTokens(List<PublicationProcessed> processedPublications, QueryMatchType type) {
		if (processedPublications == null) return false;
		for (PublicationProcessed processedPublication : processedPublications) {
			if (processedPublication != null) {
				switch (type) {
				case publication_title: if (hasTokens(processedPublication.getTitleTokens())) return true; break;
				case publication_keyword: if (hasListTokens(processedPublication.getKeywordsTokens())) return true; break;
				case publication_mesh: if (hasListTokens(processedPublication.getMeshTermsTokens())) return true; break;
				case publication_efo: if (hasListTokens(processedPublication.getEfoTermsTokens())) return true; break;
				case publication_go: if (hasListTokens(processedPublication.getGoTermsTokens())) return true; break;
				case publication_abstract: if (hasTokens(processedPublication.getAbstractTokens())) return true; break;
				case publication_fulltext: if (hasTokens(processedPublication.getFulltextTokens())) return true; break;
				default: break;
				}
			}
		}
		return false;
	}

	private Match getBestMatch(ConceptProcessed processedConcept, QueryProcessed processedQuery, MapperAlgorithmArgs algorithmArgs, MapperIdfMultiplierArgs idfMultiplierArgs, Branch branch) {
		Match bestMatch = new Match(0, new ConceptMatch(0, ConceptMatchType.none, -1), new QueryMatch(0, QueryMatchType.none, -1, -1));
		double numerator = 0;
		double denominator = 0;

		boolean average = (idfMultiplierArgs.getMappingStrategy() == MapperStrategy.average);
		double scaling = idfMultiplierArgs.getAverageScaling();

		if (idfMultiplierArgs.getNameNormalizer() > 0
				&& (!average || idfMultiplierArgs.getNameWeight() > 0)
				&& hasTokens(processedQuery.getNameTokens())) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.name, algorithmArgs, idfMultiplierArgs, branch);
			match.setScore(match.getScore() * idfMultiplierArgs.getNameNormalizer());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				numerator += idfMultiplierArgs.getNameWeight() * Math.pow(match.getScore(), scaling);
				denominator += idfMultiplierArgs.getNameWeight();
			}
		}
		if (idfMultiplierArgs.getKeywordNormalizer() > 0
				&& (!average || idfMultiplierArgs.getKeywordWeight() > 0)
				&& hasListTokens(processedQuery.getKeywordsTokens())) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.keyword, algorithmArgs, idfMultiplierArgs, branch);
			match.setScore(match.getScore() * idfMultiplierArgs.getKeywordNormalizer());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				numerator += idfMultiplierArgs.getKeywordWeight() * Math.pow(match.getScore(), scaling);
				denominator += idfMultiplierArgs.getKeywordWeight();
			}
		}
		if (idfMultiplierArgs.getDescriptionNormalizer() > 0
				&& (!average || idfMultiplierArgs.getDescriptionWeight() > 0)
				&& hasTokens(processedQuery.getDescriptionTokens())) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.description, algorithmArgs, idfMultiplierArgs, branch);
			match.setScore(match.getScore() * idfMultiplierArgs.getDescriptionNormalizer());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				numerator += idfMultiplierArgs.getDescriptionWeight() * Math.pow(match.getScore(), scaling);
				denominator += idfMultiplierArgs.getDescriptionWeight();
			}
		}
		if (idfMultiplierArgs.getPublicationTitleNormalizer() > 0
				&& (!average || idfMultiplierArgs.getPublicationTitleWeight() > 0)
				&& hasPublicationTokens(processedQuery.getProcessedPublications(), QueryMatchType.publication_title)) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.publication_title, algorithmArgs, idfMultiplierArgs, branch);
			match.setScore(match.getScore() * idfMultiplierArgs.getPublicationTitleNormalizer());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				numerator += idfMultiplierArgs.getPublicationTitleWeight() * Math.pow(match.getScore(), scaling);
				denominator += idfMultiplierArgs.getPublicationTitleWeight();
			}
		}
		if (idfMultiplierArgs.getPublicationKeywordNormalizer() > 0
				&& (!average || idfMultiplierArgs.getPublicationKeywordWeight() > 0)
				&& hasPublicationTokens(processedQuery.getProcessedPublications(), QueryMatchType.publication_keyword)) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.publication_keyword, algorithmArgs, idfMultiplierArgs, branch);
			match.setScore(match.getScore() * idfMultiplierArgs.getPublicationKeywordNormalizer());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				numerator += idfMultiplierArgs.getPublicationKeywordWeight() * Math.pow(match.getScore(), scaling);
				denominator += idfMultiplierArgs.getPublicationKeywordWeight();
			}
		}
		if (idfMultiplierArgs.getPublicationMeshNormalizer() > 0
				&& (!average || idfMultiplierArgs.getPublicationMeshWeight() > 0)
				&& hasPublicationTokens(processedQuery.getProcessedPublications(), QueryMatchType.publication_mesh)) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.publication_mesh, algorithmArgs, idfMultiplierArgs, branch);
			match.setScore(match.getScore() * idfMultiplierArgs.getPublicationMeshNormalizer());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				numerator += idfMultiplierArgs.getPublicationMeshWeight() * Math.pow(match.getScore(), scaling);
				denominator += idfMultiplierArgs.getPublicationMeshWeight();
			}
		}
		if (idfMultiplierArgs.getPublicationMinedNormalizer() > 0
				&& (!average || idfMultiplierArgs.getPublicationMinedWeight() > 0)
				&& (hasPublicationTokens(processedQuery.getProcessedPublications(), QueryMatchType.publication_efo) || hasPublicationTokens(processedQuery.getProcessedPublications(), QueryMatchType.publication_go))) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.publication_efo, algorithmArgs, idfMultiplierArgs, branch);
			Match otherMatch = getMatch(processedConcept, processedQuery, QueryMatchType.publication_go, algorithmArgs, idfMultiplierArgs, branch);
			if (otherMatch.compareTo(match) > 0) match = otherMatch;
			match.setScore(match.getScore() * idfMultiplierArgs.getPublicationMinedNormalizer());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				numerator += idfMultiplierArgs.getPublicationMinedWeight() * Math.pow(match.getScore(), scaling);
				denominator += idfMultiplierArgs.getPublicationMinedWeight();
			}
		}
		if (idfMultiplierArgs.getPublicationAbstractNormalizer() > 0
				&& (!average || idfMultiplierArgs.getPublicationAbstractWeight() > 0)
				&& hasPublicationTokens(processedQuery.getProcessedPublications(), QueryMatchType.publication_abstract)) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.publication_abstract, algorithmArgs, idfMultiplierArgs, branch);
			match.setScore(match.getScore() * idfMultiplierArgs.getPublicationAbstractNormalizer());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				numerator += idfMultiplierArgs.getPublicationAbstractWeight() * Math.pow(match.getScore(), scaling);
				denominator += idfMultiplierArgs.getPublicationAbstractWeight();
			}
		}
		if (idfMultiplierArgs.getPublicationFulltextNormalizer() > 0
				&& (!average || idfMultiplierArgs.getPublicationFulltextWeight() > 0)
				&& hasPublicationTokens(processedQuery.getProcessedPublications(), QueryMatchType.publication_fulltext)) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.publication_fulltext, algorithmArgs, idfMultiplierArgs, branch);
			match.setScore(match.getScore() * idfMultiplierArgs.getPublicationFulltextNormalizer());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				numerator += idfMultiplierArgs.getPublicationFulltextWeight() * Math.pow(match.getScore(), scaling);
				denominator += idfMultiplierArgs.getPublicationFulltextWeight();
			}
		}
		if (idfMultiplierArgs.getDocNormalizer() > 0
				&& (!average || idfMultiplierArgs.getDocWeight() > 0)
				&& hasListTokens(processedQuery.getDocsTokens())) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.doc, algorithmArgs, idfMultiplierArgs, branch);
			match.setScore(match.getScore() * idfMultiplierArgs.getDocNormalizer());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				numerator += idfMultiplierArgs.getDocWeight() * Math.pow(match.getScore(), scaling);
				denominator += idfMultiplierArgs.getDocWeight();
			}
		}
		if (idfMultiplierArgs.getWebpageNormalizer() > 0
				&& (!average || idfMultiplierArgs.getWebpageWeight() > 0)
				&& hasListTokens(processedQuery.getWebpagesTokens())) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.webpage, algorithmArgs, idfMultiplierArgs, branch);
			match.setScore(match.getScore() * idfMultiplierArgs.getWebpageNormalizer());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				numerator += idfMultiplierArgs.getWebpageWeight() * Math.pow(match.getScore(), scaling);
				denominator += idfMultiplierArgs.getWebpageWeight();
			}
		}

		if (average && denominator > 0) {
			bestMatch.setBestOneScore(bestMatch.getScore());
			bestMatch.setScore(numerator / denominator);
		}

		return bestMatch;
	}

	private double bestPathScore(EdamUri edamUri, Map<EdamUri, Match> matches, int level, double current, double parentWeight) {
		// matches.get(edamUri) is null if edamUri is from some other branch than its child who called us
		// should not happen if EDAM is consistent
		double score = (matches.get(edamUri) == null ? 0 : matches.get(edamUri).getWithoutPathScore());
		current += Math.pow(parentWeight, level) * score;
		List<EdamUri> parents = processedConcepts.get(edamUri).getDirectParents();
		if (parents.size() == 0) {
			double denominator = 0;
			for (int l = 1; l <= level; ++l) {
				denominator += Math.pow(parentWeight, l);
			}
			return current /= denominator;
		} else {
			double bestPathScore = 0;
			for (EdamUri parent : parents) {
				double best = bestPathScore(parent, matches, level + 1, current, parentWeight);
				if (best > bestPathScore) bestPathScore = best;
			}
			return bestPathScore;
		}
	}

	private void removeParents(EdamUri edamUri, Map<EdamUri, Match> matches) {
		for (EdamUri parent : processedConcepts.get(edamUri).getDirectParents()) {
			if (matches.get(parent) != null && !matches.get(parent).isRemoved()) {
				matches.get(parent).setRemoved(true);
				removeParents(parent, matches);
			}
		}
	}

	private void removeChildren(EdamUri edamUri, Map<EdamUri, Match> matches) {
		for (EdamUri child : processedConcepts.get(edamUri).getDirectChildren()) {
			if (matches.get(child) != null && !matches.get(child).isRemoved()) {
				matches.get(child).setRemoved(true);
				removeChildren(child, matches);
			}
		}
	}

	private boolean isParent(EdamUri child, EdamUri parentSearched) {
		boolean isParent = false;
		for (EdamUri parent : processedConcepts.get(child).getDirectParents()) {
			if (parentSearched.equals(parent)) {
				isParent = true;
				break;
			} else if (isParent(parent, parentSearched)) {
				isParent = true;
				break;
			}
		}
		return isParent;
	}

	private void addParentsChildren(Match match, Mapping mapping, boolean remainingAnnotation) {
		for (Match otherMatch : mapping.getMatches(match.getEdamUri().getBranch())) {
			if (isParent(match.getEdamUri(), otherMatch.getEdamUri())) {

				if (otherMatch.isExistingAnnotation()) match.addParentAnnotation(otherMatch.getEdamUri());
				else match.addParent(otherMatch.getEdamUri());

				if (remainingAnnotation) otherMatch.addChildRemainingAnnotation(match.getEdamUri());
				else if (match.isExistingAnnotation()) otherMatch.addChildAnnotation(match.getEdamUri());
				else otherMatch.addChild(match.getEdamUri());

			} else if (isParent(otherMatch.getEdamUri(), match.getEdamUri())) {

				if (remainingAnnotation) otherMatch.addParentRemainingAnnotation(match.getEdamUri());
				else if (match.isExistingAnnotation()) otherMatch.addParentAnnotation(match.getEdamUri());
				else otherMatch.addParent(match.getEdamUri());

				if (otherMatch.isExistingAnnotation()) match.addChildAnnotation(otherMatch.getEdamUri());
				else match.addChild(otherMatch.getEdamUri());
			}
		}
		if (remainingAnnotation) {
			for (Match otherMatch : mapping.getRemainingAnnotations(match.getEdamUri().getBranch())) {
				if (isParent(match.getEdamUri(), otherMatch.getEdamUri())) {
					match.addParentRemainingAnnotation(otherMatch.getEdamUri());
					otherMatch.addChildRemainingAnnotation(match.getEdamUri());
				} else if (isParent(otherMatch.getEdamUri(), match.getEdamUri())) {
					otherMatch.addParentRemainingAnnotation(match.getEdamUri());
					match.addChildRemainingAnnotation(otherMatch.getEdamUri());
				}
			}
		}
	}

	public Mapping map(Query query, QueryProcessed processedQuery, MapperArgs args) {
		Mapping mapping = new Mapping(args.getMatch(), args.getBranches());

		Map<EdamUri, Match> matches = new HashMap<>();

		for (Map.Entry<EdamUri, ConceptProcessed> conceptEntry : processedConcepts.entrySet()) {
			EdamUri edamUri = conceptEntry.getKey();
			ConceptProcessed processedConcept = conceptEntry.getValue();

			if (!args.getBranches().contains(edamUri.getBranch())) continue;

			if ((processedConcept.isObsolete() && !args.getObsolete())
				|| (processedConcept.getDirectParents().isEmpty() && !args.isNoRemoveTopLevel())) {
				Match zeroMatch = new Match(0, new ConceptMatch(0, ConceptMatchType.none, -1), new QueryMatch(0, QueryMatchType.none, -1, -1));
				zeroMatch.setEdamUri(edamUri);
				matches.put(edamUri, zeroMatch);
				continue;
			}

			Match match = getBestMatch(processedConcept, processedQuery, args.getAlgorithmArgs(), args.getIdfMultiplierArgs(), edamUri.getBranch());
			match.setEdamUri(edamUri);
			matches.put(edamUri, match);
		}

		Set<EdamUri> annotations = new LinkedHashSet<>();
		for (EdamUri annotation : query.getAnnotations()) {
			if (args.getBranches().contains(annotation.getBranch())) {
				annotations.add(annotation);
				matches.get(annotation).setExistingAnnotation(true);
			}
		}

		if (!args.isNoRemoveInferiorParentChild() && args.isExcludeAnnotations()) {
			for (EdamUri annotation : annotations) {
				removeParents(annotation, matches);
				removeChildren(annotation, matches);
				matches.get(annotation).setRemoved(true);
			}
		}

		if (args.getAlgorithmArgs().getPathWeight() > 0 && args.getAlgorithmArgs().getParentWeight() > 0) {
			for (Match match : matches.values()) {
				match.setWithoutPathScore(match.getScore());
			}
			for (Map.Entry<EdamUri, Match> matchEntry : matches.entrySet()) {
				EdamUri edamUri = matchEntry.getKey();
				Match match = matchEntry.getValue();

				if (processedConcepts.get(edamUri).getDirectParents().isEmpty() && !args.isNoRemoveTopLevel()) {
					match.setRemoved(true);
					continue;
				}
				if (processedConcepts.get(edamUri).isObsolete() && !args.getObsolete()) {
					continue;
				}

				double bestPathScore = 0;
				for (EdamUri parent : processedConcepts.get(edamUri).getDirectParents()) {
					double best = bestPathScore(parent, matches, 1, 0, args.getAlgorithmArgs().getParentWeight());
					if (best > bestPathScore) bestPathScore = best;
				}
				match.setScore((match.getScore() + args.getAlgorithmArgs().getPathWeight() * bestPathScore) / (1 + args.getAlgorithmArgs().getPathWeight()));
			}
		}

		List<Match> sortedMatches = new ArrayList<>(matches.values());
		Collections.sort(sortedMatches, Collections.reverseOrder());

		for (Match match : sortedMatches) {
			if (mapping.isFull()) break;
			if (mapping.isFull(match.getEdamUri().getBranch())) continue;

			if (match.isRemoved()) continue;
			if (processedConcepts.get(match.getEdamUri()).isObsolete() && !args.getObsolete()) continue;
			if (args.isExcludeAnnotations() && match.isExistingAnnotation()) continue;

			double goodScore = 0;
			double badScore = 0;
			switch (match.getEdamUri().getBranch()) {
			case topic:
				goodScore = args.getGoodScoreTopic();
				badScore = args.getBadScoreTopic();
				break;
			case operation:
				goodScore = args.getGoodScoreOperation();
				badScore = args.getBadScoreOperation();
				break;
			case data:
				goodScore = args.getGoodScoreData();
				badScore = args.getBadScoreData();
				break;
			case format:
				goodScore = args.getGoodScoreFormat();
				badScore = args.getBadScoreFormat();
				break;
			}

			double score = 0;
			if (args.getIdfMultiplierArgs().getMappingStrategy() == MapperStrategy.average) {
				score = match.getBestOneScore();
			} else if (args.getAlgorithmArgs().getPathWeight() > 0 && args.getAlgorithmArgs().getParentWeight() > 0) {
				score = match.getWithoutPathScore();
			} else {
				score = match.getScore();
			}

			if (score > goodScore) {
				if (args.isNoOutputGoodScores()) continue;
			} else if (score >= badScore && score <= goodScore) {
				if (args.isNoOutputMediumScores()) continue;
			} else if (score < badScore) {
				if (!args.isOutputBadScores()) continue;
			}

			if (!args.isNoRemoveInferiorParentChild()) {
				removeParents(match.getEdamUri(), matches);
				removeChildren(match.getEdamUri(), matches);
			}

			addParentsChildren(match, mapping, false);
			mapping.addMatch(match);
		}

		if (!args.isExcludeAnnotations() && annotations.size() > 0) {
			int annotationsSeen = 0;
			for (Match match : sortedMatches) {
				if (annotations.contains(match.getEdamUri())) {
					++annotationsSeen;
					if (!mapping.getMatches(match.getEdamUri().getBranch()).contains(match)) {
						addParentsChildren(match, mapping, true);
						mapping.addRemainingAnnotation(match);
					}
					if (annotationsSeen >= annotations.size()) break;
				}
			}
		}

		return mapping;
	}
}
