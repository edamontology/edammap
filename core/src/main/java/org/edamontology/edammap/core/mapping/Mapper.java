/*
 * Copyright © 2016, 2017, 2018 Erik Jaaniso
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

package org.edamontology.edammap.core.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.mapping.args.AlgorithmArgs;
import org.edamontology.edammap.core.mapping.args.IdfArgs;
import org.edamontology.edammap.core.mapping.args.MapperArgs;
import org.edamontology.edammap.core.mapping.args.MultiplierArgs;
import org.edamontology.edammap.core.mapping.args.NormaliserArgs;
import org.edamontology.edammap.core.processing.ConceptProcessed;
import org.edamontology.edammap.core.processing.PublicationProcessed;
import org.edamontology.edammap.core.processing.QueryProcessed;
import org.edamontology.edammap.core.query.Query;

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
		if (processedConcepts == null) {
			throw new IllegalArgumentException("Given concepts is null");
		}
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

	private void calculateScores(double[] bestScores, List<String> tos, List<String> froms, List<Double> fromIdfs, double fromIdfScaling, double fromMultiplier, AlgorithmArgs args) {
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

	private double getScore(List<String> toTokens, List<Double> toIdfs, double toIdfScaling, double toMultiplier, List<List<String>> fromsTokens, List<List<Double>> fromsIdfs, List<Double> fromIdfScalings, List<Double> fromMultipliers, AlgorithmArgs args) {
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
	private ConceptMatch toConceptFromQuery(ConceptProcessed processedConcept, QueryProcessed processedQuery, QueryMatchType type, AlgorithmArgs algorithmArgs, IdfArgs idfArgs, MultiplierArgs multiplierArgs, NormaliserArgs normaliserArgs) {
		List<List<String>> fromsTokens = new ArrayList<>();
		List<List<Double>> fromsIdfs = new ArrayList<>();
		List<Double> fromIdfScalings = new ArrayList<>();
		List<Double> fromMultipliers = new ArrayList<>();

		switch (type) {
		case name:
			if (processedQuery.getNameTokens() != null && normaliserArgs.getNameNormaliser() > 0) {
				fromsTokens.add(processedQuery.getNameTokens());
				fromsIdfs.add(processedQuery.getNameIdfs());
				fromIdfScalings.add((processedQuery.getNameIdfs() == null || !idfArgs.isNameKeywordsIdf()) ? 0 : idfArgs.getQueryIdfScaling());
				fromMultipliers.add(Double.valueOf(1));
			}
			break;
		case keyword:
			if (normaliserArgs.getKeywordNormaliser() > 0) {
				for (int i = 0; i < processedQuery.getKeywordsTokens().size(); ++i) {
					if (processedQuery.getKeywordsTokens().get(i) == null) continue;
					fromsTokens.add(processedQuery.getKeywordsTokens().get(i));
					fromsIdfs.add(processedQuery.getKeywordsIdfs().get(i));
					fromIdfScalings.add((processedQuery.getKeywordsIdfs().get(i) == null || !idfArgs.isNameKeywordsIdf()) ? 0 : idfArgs.getQueryIdfScaling());
					fromMultipliers.add(Double.valueOf(1));
				}
			}
			break;
		case description:
			if (processedQuery.getDescriptionTokens() != null && normaliserArgs.getDescriptionNormaliser() > 0) {
				fromsTokens.add(processedQuery.getDescriptionTokens());
				fromsIdfs.add(processedQuery.getDescriptionIdfs());
				fromIdfScalings.add((processedQuery.getDescriptionIdfs() == null || !idfArgs.isDescriptionIdf()) ? 0 : idfArgs.getQueryIdfScaling());
				fromMultipliers.add(Double.valueOf(1));
			}
			break;
		case publication_title:
			for (PublicationProcessed processedPublication : processedQuery.getProcessedPublications()) {
				if (processedPublication == null) continue;
				if (processedPublication.getTitleTokens() != null && normaliserArgs.getPublicationTitleNormaliser() > 0) {
					fromsTokens.add(processedPublication.getTitleTokens());
					fromsIdfs.add(processedPublication.getTitleIdfs());
					fromIdfScalings.add((processedPublication.getTitleIdfs() == null || !idfArgs.isTitleKeywordsIdf()) ? 0 : idfArgs.getQueryIdfScaling());
					fromMultipliers.add(Double.valueOf(1));
				}
			}
			break;
		case publication_keyword:
			for (PublicationProcessed processedPublication : processedQuery.getProcessedPublications()) {
				if (processedPublication == null) continue;
				if (normaliserArgs.getPublicationKeywordNormaliser() > 0) {
					for (int i = 0; i < processedPublication.getKeywordsTokens().size(); ++i) {
						if (processedPublication.getKeywordsTokens().get(i) == null) continue;
						fromsTokens.add(processedPublication.getKeywordsTokens().get(i));
						fromsIdfs.add(processedPublication.getKeywordsIdfs().get(i));
						fromIdfScalings.add((processedPublication.getKeywordsIdfs().get(i) == null || !idfArgs.isTitleKeywordsIdf()) ? 0 : idfArgs.getQueryIdfScaling());
						fromMultipliers.add(Double.valueOf(1));
					}
				}
			}
			break;
		case publication_mesh:
			for (PublicationProcessed processedPublication : processedQuery.getProcessedPublications()) {
				if (processedPublication == null) continue;
				if (normaliserArgs.getPublicationMeshNormaliser() > 0) {
					for (int i = 0; i < processedPublication.getMeshTermsTokens().size(); ++i) {
						if (processedPublication.getMeshTermsTokens().get(i) == null) continue;
						fromsTokens.add(processedPublication.getMeshTermsTokens().get(i));
						fromsIdfs.add(processedPublication.getMeshTermsIdfs().get(i));
						fromIdfScalings.add((processedPublication.getMeshTermsIdfs().get(i) == null || !idfArgs.isTitleKeywordsIdf()) ? 0 : idfArgs.getQueryIdfScaling());
						fromMultipliers.add(Double.valueOf(1));
					}
				}
			}
			break;
		case publication_efo:
		case publication_go:
			for (PublicationProcessed processedPublication : processedQuery.getProcessedPublications()) {
				if (processedPublication == null) continue;
				if (normaliserArgs.getPublicationMinedNormaliser() > 0) {
					for (int i = 0; i < processedPublication.getEfoTermsTokens().size(); ++i) {
						if (processedPublication.getEfoTermsTokens().get(i) == null) continue;
						fromsTokens.add(processedPublication.getEfoTermsTokens().get(i));
						fromsIdfs.add(processedPublication.getEfoTermsIdfs().get(i));
						fromIdfScalings.add((processedPublication.getEfoTermsIdfs().get(i) == null || !idfArgs.isTitleKeywordsIdf()) ? 0 : idfArgs.getQueryIdfScaling());
						fromMultipliers.add(Double.valueOf(1));
					}
				}
				if (normaliserArgs.getPublicationMinedNormaliser() > 0) {
					for (int i = 0; i < processedPublication.getGoTermsTokens().size(); ++i) {
						if (processedPublication.getGoTermsTokens().get(i) == null) continue;
						fromsTokens.add(processedPublication.getGoTermsTokens().get(i));
						fromsIdfs.add(processedPublication.getGoTermsIdfs().get(i));
						fromIdfScalings.add((processedPublication.getGoTermsIdfs().get(i) == null || !idfArgs.isTitleKeywordsIdf()) ? 0 : idfArgs.getQueryIdfScaling());
						fromMultipliers.add(Double.valueOf(1));
					}
				}
			}
			break;
		case publication_abstract:
			for (PublicationProcessed processedPublication : processedQuery.getProcessedPublications()) {
				if (processedPublication == null) continue;
				if (processedPublication.getAbstractTokens() != null && normaliserArgs.getPublicationAbstractNormaliser() > 0) {
					fromsTokens.add(processedPublication.getAbstractTokens());
					fromsIdfs.add(processedPublication.getAbstractIdfs());
					fromIdfScalings.add((processedPublication.getAbstractIdfs() == null || !idfArgs.isAbstractIdf()) ? 0 : idfArgs.getQueryIdfScaling());
					fromMultipliers.add(Double.valueOf(1));
				}
			}
			break;
		case publication_fulltext:
			for (PublicationProcessed processedPublication : processedQuery.getProcessedPublications()) {
				if (processedPublication == null) continue;
				if (processedPublication.getFulltextTokens() != null && normaliserArgs.getPublicationFulltextNormaliser() > 0) {
					fromsTokens.add(processedPublication.getFulltextTokens());
					fromsIdfs.add(processedPublication.getFulltextIdfs());
					fromIdfScalings.add((processedPublication.getFulltextIdfs() == null) ? 0 : idfArgs.getQueryIdfScaling());
					fromMultipliers.add(Double.valueOf(1));
				}
			}
			break;
		case doc:
			if (normaliserArgs.getDocNormaliser() > 0) {
				for (int i = 0; i < processedQuery.getDocsTokens().size(); ++i) {
					if (processedQuery.getDocsTokens().get(i) == null) continue;
					fromsTokens.add(processedQuery.getDocsTokens().get(i));
					fromsIdfs.add(processedQuery.getDocsIdfs().get(i));
					fromIdfScalings.add((processedQuery.getDocsIdfs().get(i) == null) ? 0 : idfArgs.getQueryIdfScaling());
					fromMultipliers.add(Double.valueOf(1));
				}
			}
			break;
		case webpage:
			if (normaliserArgs.getWebpageNormaliser() > 0) {
				for (int i = 0; i < processedQuery.getWebpagesTokens().size(); ++i) {
					if (processedQuery.getWebpagesTokens().get(i) == null) continue;
					fromsTokens.add(processedQuery.getWebpagesTokens().get(i));
					fromsIdfs.add(processedQuery.getWebpagesIdfs().get(i));
					fromIdfScalings.add((processedQuery.getWebpagesIdfs().get(i) == null) ? 0 : idfArgs.getQueryIdfScaling());
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

		if (processedConcept.getLabelTokens() != null && multiplierArgs.getLabelMultiplier() > 0) {
			double idfScaling = idfArgs.isLabelSynonymsIdf() ? idfArgs.getConceptIdfScaling() : 0;
			double score = getScore(processedConcept.getLabelTokens(), processedConcept.getLabelIdfs(), idfScaling, multiplierArgs.getLabelMultiplier(), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
			if (score > bestScore) {
				bestScore = score;
				matchType = ConceptMatchType.label;
			}
		}
		if (multiplierArgs.getExactSynonymMultiplier() > 0) {
			for (int i = 0; i < processedConcept.getExactSynonymsTokens().size(); ++i) {
				double idfScaling = idfArgs.isLabelSynonymsIdf() ? idfArgs.getConceptIdfScaling() : 0;
				double score = getScore(processedConcept.getExactSynonymsTokens().get(i), processedConcept.getExactSynonymsIdfs().get(i), idfScaling, multiplierArgs.getExactSynonymMultiplier(), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
				if (score > bestScore) {
					bestScore = score;
					matchType = ConceptMatchType.exact_synonym;
					synonymIndex = i;
				}
			}
		}
		if (multiplierArgs.getNarrowBroadMultiplier() > 0) {
			for (int i = 0; i < processedConcept.getNarrowSynonymsTokens().size(); ++i) {
				double idfScaling = idfArgs.isLabelSynonymsIdf() ? idfArgs.getConceptIdfScaling() : 0;
				double score = getScore(processedConcept.getNarrowSynonymsTokens().get(i), processedConcept.getNarrowSynonymsIdfs().get(i), idfScaling, multiplierArgs.getNarrowBroadMultiplier(), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
				if (score > bestScore) {
					bestScore = score;
					matchType = ConceptMatchType.narrow_synonym;
					synonymIndex = i;
				}
			}
		}
		if (multiplierArgs.getNarrowBroadMultiplier() > 0) {
			for (int i = 0; i < processedConcept.getBroadSynonymsTokens().size(); ++i) {
				double idfScaling = idfArgs.isLabelSynonymsIdf() ? idfArgs.getConceptIdfScaling() : 0;
				double score = getScore(processedConcept.getBroadSynonymsTokens().get(i), processedConcept.getBroadSynonymsIdfs().get(i), idfScaling, multiplierArgs.getNarrowBroadMultiplier(), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
				if (score > bestScore) {
					bestScore = score;
					matchType = ConceptMatchType.broad_synonym;
					synonymIndex = i;
				}
			}
		}
		if (processedConcept.getDefinitionTokens() != null && multiplierArgs.getDefinitionMultiplier() > 0) {
			double idfScaling = idfArgs.getConceptIdfScaling();
			double score = getScore(processedConcept.getDefinitionTokens(), processedConcept.getDefinitionIdfs(), idfScaling, multiplierArgs.getDefinitionMultiplier(), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
			if (score > bestScore) {
				bestScore = score;
				matchType = ConceptMatchType.definition;
			}
		}
		if (processedConcept.getCommentTokens() != null && multiplierArgs.getCommentMultiplier() > 0) {
			double idfScaling = idfArgs.getConceptIdfScaling();
			double score = getScore(processedConcept.getCommentTokens(), processedConcept.getCommentIdfs(), idfScaling, multiplierArgs.getCommentMultiplier(), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
			if (score > bestScore) {
				bestScore = score;
				matchType = ConceptMatchType.comment;
			}
		}

		return new ConceptMatch(bestScore, matchType, synonymIndex);
	}

	// TODO try to make less copy-pasty
	private QueryMatch toQueryFromConcept(QueryProcessed processedQuery, ConceptProcessed processedConcept, QueryMatchType type, AlgorithmArgs algorithmArgs, IdfArgs idfArgs, NormaliserArgs normaliserArgs) {
		List<List<String>> fromsTokens = processedConcept.getTokens();
		List<List<Double>> fromsIdfs = processedConcept.getIdfs();
		List<Double> fromIdfScalings = processedConcept.getIdfScalings();
		List<Double> fromMultipliers = processedConcept.getMultipliers();

		double bestScore = 0;
		QueryMatchType matchType = QueryMatchType.none;
		int index = -1;
		int indexInPublication = -1;

		switch (type) {
		case name:
			if (processedQuery.getNameTokens() != null && normaliserArgs.getNameNormaliser() > 0) {
				List<Double> idfs = processedQuery.getNameIdfs();
				double idfScaling = ((idfs == null || !idfArgs.isNameKeywordsIdf()) ? 0 : idfArgs.getQueryIdfScaling());
				double score = getScore(processedQuery.getNameTokens(), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
				if (score > bestScore) {
					bestScore = score;
				}
				matchType = QueryMatchType.name;
			}
			break;
		case keyword:
			if (normaliserArgs.getKeywordNormaliser() > 0) {
				for (int i = 0; i < processedQuery.getKeywordsTokens().size(); ++i) {
					if (processedQuery.getKeywordsTokens().get(i) == null) continue;
					List<Double> idfs = processedQuery.getKeywordsIdfs().get(i);
					double idfScaling = ((idfs == null || !idfArgs.isNameKeywordsIdf()) ? 0 : idfArgs.getQueryIdfScaling());
					double score = getScore(processedQuery.getKeywordsTokens().get(i), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
					if (score > bestScore) {
						bestScore = score;
						index = i;
					}
					matchType = QueryMatchType.keyword;
				}
			}
			break;
		case description:
			if (processedQuery.getDescriptionTokens() != null && normaliserArgs.getDescriptionNormaliser() > 0) {
				List<Double> idfs = processedQuery.getDescriptionIdfs();
				double idfScaling = ((idfs == null || !idfArgs.isDescriptionIdf()) ? 0 : idfArgs.getQueryIdfScaling());
				double score = getScore(processedQuery.getDescriptionTokens(), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
				if (score > bestScore) {
					bestScore = score;
				}
				matchType = QueryMatchType.description;
			}
			break;
		case publication_title:
			for (int i = 0; i < processedQuery.getProcessedPublications().size(); ++i) {
				PublicationProcessed processedPublication = processedQuery.getProcessedPublications().get(i);
				if (processedPublication == null) continue;
				if (processedPublication.getTitleTokens() != null && normaliserArgs.getPublicationTitleNormaliser() > 0) {
					List<Double> idfs = processedPublication.getTitleIdfs();
					double idfScaling = ((idfs == null || !idfArgs.isTitleKeywordsIdf()) ? 0 : idfArgs.getQueryIdfScaling());
					double score = getScore(processedPublication.getTitleTokens(), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
					if (score > bestScore) {
						bestScore = score;
						index = i;
					}
					matchType = QueryMatchType.publication_title;
				}
			}
			break;
		case publication_keyword:
			for (int i = 0; i < processedQuery.getProcessedPublications().size(); ++i) {
				PublicationProcessed processedPublication = processedQuery.getProcessedPublications().get(i);
				if (processedPublication == null) continue;
				if (normaliserArgs.getPublicationKeywordNormaliser() > 0) {
					for (int j = 0; j < processedPublication.getKeywordsTokens().size(); ++j) {
						if (processedPublication.getKeywordsTokens().get(j) == null) continue;
						List<Double> idfs = processedPublication.getKeywordsIdfs().get(j);
						double idfScaling = ((idfs == null || !idfArgs.isTitleKeywordsIdf()) ? 0 : idfArgs.getQueryIdfScaling());
						double score = getScore(processedPublication.getKeywordsTokens().get(j), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
						if (score > bestScore) {
							bestScore = score;
							index = i;
							indexInPublication = j;
						}
						matchType = QueryMatchType.publication_keyword;
					}
				}
			}
			break;
		case publication_mesh:
			for (int i = 0; i < processedQuery.getProcessedPublications().size(); ++i) {
				PublicationProcessed processedPublication = processedQuery.getProcessedPublications().get(i);
				if (processedPublication == null) continue;
				if (normaliserArgs.getPublicationMeshNormaliser() > 0) {
					for (int j = 0; j < processedPublication.getMeshTermsTokens().size(); ++j) {
						if (processedPublication.getMeshTermsTokens().get(j) == null) continue;
						List<Double> idfs = processedPublication.getMeshTermsIdfs().get(j);
						double idfScaling = ((idfs == null || !idfArgs.isTitleKeywordsIdf()) ? 0 : idfArgs.getQueryIdfScaling());
						double score = getScore(processedPublication.getMeshTermsTokens().get(j), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
						if (score > bestScore) {
							bestScore = score;
							index = i;
							indexInPublication = j;
						}
						matchType = QueryMatchType.publication_mesh;
					}
				}
			}
			break;
		case publication_efo:
			for (int i = 0; i < processedQuery.getProcessedPublications().size(); ++i) {
				PublicationProcessed processedPublication = processedQuery.getProcessedPublications().get(i);
				if (processedPublication == null) continue;
				if (normaliserArgs.getPublicationMinedNormaliser() > 0) {
					for (int j = 0; j < processedPublication.getEfoTermsTokens().size(); ++j) {
						if (processedPublication.getEfoTermsTokens().get(j) == null) continue;
						List<Double> idfs = processedPublication.getEfoTermsIdfs().get(j);
						double idfScaling = ((idfs == null || !idfArgs.isTitleKeywordsIdf()) ? 0 : idfArgs.getQueryIdfScaling());
						double score = getScore(processedPublication.getEfoTermsTokens().get(j), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
						// simulate fulltext
						score *= Math.pow(processedPublication.getEfoTermFrequencies().get(j), algorithmArgs.getScoreScaling());
						if (score > bestScore) {
							bestScore = score;
							index = i;
							indexInPublication = j;
						}
						matchType = QueryMatchType.publication_efo;
					}
				}
			}
			break;
		case publication_go:
			for (int i = 0; i < processedQuery.getProcessedPublications().size(); ++i) {
				PublicationProcessed processedPublication = processedQuery.getProcessedPublications().get(i);
				if (processedPublication == null) continue;
				if (normaliserArgs.getPublicationMinedNormaliser() > 0) {
					for (int j = 0; j < processedPublication.getGoTermsTokens().size(); ++j) {
						if (processedPublication.getGoTermsTokens().get(j) == null) continue;
						List<Double> idfs = processedPublication.getGoTermsIdfs().get(j);
						double idfScaling = ((idfs == null || !idfArgs.isTitleKeywordsIdf()) ? 0 : idfArgs.getQueryIdfScaling());
						double score = getScore(processedPublication.getGoTermsTokens().get(j), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
						// simulate fulltext
						score *= Math.pow(processedPublication.getGoTermFrequencies().get(j), algorithmArgs.getScoreScaling());
						if (score > bestScore) {
							bestScore = score;
							index = i;
							indexInPublication = j;
						}
						matchType = QueryMatchType.publication_go;
					}
				}
			}
			break;
		case publication_abstract:
			for (int i = 0; i < processedQuery.getProcessedPublications().size(); ++i) {
				PublicationProcessed processedPublication = processedQuery.getProcessedPublications().get(i);
				if (processedPublication == null) continue;
				if (processedPublication.getAbstractTokens() != null && normaliserArgs.getPublicationAbstractNormaliser() > 0) {
					List<Double> idfs = processedPublication.getAbstractIdfs();
					double idfScaling = ((idfs == null || !idfArgs.isAbstractIdf()) ? 0 : idfArgs.getQueryIdfScaling());
					double score = getScore(processedPublication.getAbstractTokens(), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
					if (score > bestScore) {
						bestScore = score;
						index = i;
					}
					matchType = QueryMatchType.publication_abstract;
				}
			}
			break;
		case publication_fulltext:
			for (int i = 0; i < processedQuery.getProcessedPublications().size(); ++i) {
				PublicationProcessed processedPublication = processedQuery.getProcessedPublications().get(i);
				if (processedPublication == null) continue;
				if (processedPublication.getFulltextTokens() != null && normaliserArgs.getPublicationFulltextNormaliser() > 0) {
					List<Double> idfs = processedPublication.getFulltextIdfs();
					double idfScaling = ((idfs == null) ? 0 : idfArgs.getQueryIdfScaling());
					double score = getScore(processedPublication.getFulltextTokens(), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
					if (score > bestScore) {
						bestScore = score;
						index = i;
					}
					matchType = QueryMatchType.publication_fulltext;
				}
			}
			break;
		case doc:
			if (normaliserArgs.getDocNormaliser() > 0) {
				for (int i = 0; i < processedQuery.getDocsTokens().size(); ++i) {
					if (processedQuery.getDocsTokens().get(i) == null) continue;
					List<Double> idfs = processedQuery.getDocsIdfs().get(i);
					double idfScaling = ((idfs == null) ? 0 : idfArgs.getQueryIdfScaling());
					double score = getScore(processedQuery.getDocsTokens().get(i), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
					if (score > bestScore) {
						bestScore = score;
						index = i;
					}
					matchType = QueryMatchType.doc;
				}
			}
			break;
		case webpage:
			if (normaliserArgs.getWebpageNormaliser() > 0) {
				for (int i = 0; i < processedQuery.getWebpagesTokens().size(); ++i) {
					if (processedQuery.getWebpagesTokens().get(i) == null) continue;
					List<Double> idfs = processedQuery.getWebpagesIdfs().get(i);
					double idfScaling = ((idfs == null) ? 0 : idfArgs.getQueryIdfScaling());
					double score = getScore(processedQuery.getWebpagesTokens().get(i), idfs, idfScaling, Double.valueOf(1), fromsTokens, fromsIdfs, fromIdfScalings, fromMultipliers, algorithmArgs);
					if (score > bestScore) {
						bestScore = score;
						index = i;
					}
					matchType = QueryMatchType.webpage;
				}
			}
			break;
		default:
			break;
		}

		return new QueryMatch(bestScore, matchType, index, indexInPublication);
	}

	private Match getMatch(ConceptProcessed processedConcept, QueryProcessed processedQuery, QueryMatchType type, AlgorithmArgs algorithmArgs, IdfArgs idfArgs, MultiplierArgs multiplierArgs, NormaliserArgs normaliserArgs) {

		ConceptMatch conceptMatch;
		if (algorithmArgs.getConceptWeight() > 0) {
			conceptMatch = toConceptFromQuery(processedConcept, processedQuery, type, algorithmArgs, idfArgs, multiplierArgs, normaliserArgs);
		} else {
			conceptMatch = new ConceptMatch(0, ConceptMatchType.none, -1);
		}

		QueryMatch queryMatch;
		if (algorithmArgs.getQueryWeight() > 0) {
			queryMatch = toQueryFromConcept(processedQuery, processedConcept, type, algorithmArgs, idfArgs, normaliserArgs);
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

	private Match getBestMatch(ConceptProcessed processedConcept, QueryProcessed processedQuery, MapperArgs args) {
		Match bestMatch = new Match(0, new ConceptMatch(0, ConceptMatchType.none, -1), new QueryMatch(0, QueryMatchType.none, -1, -1));
		double numerator = 0;
		double denominator = 0;

		boolean average = (args.getAlgorithmArgs().getMappingStrategy() == MapperStrategy.average);
		double scaling = args.getWeightArgs().getAverageScaling();

		List<MatchAverageStats> matchAverageStats = new ArrayList<>();

		if (args.getNormaliserArgs().getNameNormaliser() > 0
				&& (!average || args.getWeightArgs().getNameWeight() > 0)
				&& hasTokens(processedQuery.getNameTokens())) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.name, args.getAlgorithmArgs(), args.getIdfArgs(), args.getMultiplierArgs(), args.getNormaliserArgs());
			match.setScore(match.getScore() * args.getNormaliserArgs().getNameNormaliser());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				double numeratorPart = args.getWeightArgs().getNameWeight() * Math.pow(match.getScore(), scaling);
				numerator += numeratorPart;
				denominator += args.getWeightArgs().getNameWeight();
				matchAverageStats.add(new MatchAverageStats(match.getQueryMatch(), match.getConceptMatch(), numeratorPart));
			}
		}
		if (args.getNormaliserArgs().getKeywordNormaliser() > 0
				&& (!average || args.getWeightArgs().getKeywordWeight() > 0)
				&& hasListTokens(processedQuery.getKeywordsTokens())) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.keyword, args.getAlgorithmArgs(), args.getIdfArgs(), args.getMultiplierArgs(), args.getNormaliserArgs());
			match.setScore(match.getScore() * args.getNormaliserArgs().getKeywordNormaliser());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				double numeratorPart = args.getWeightArgs().getKeywordWeight() * Math.pow(match.getScore(), scaling);
				numerator += numeratorPart;
				denominator += args.getWeightArgs().getKeywordWeight();
				matchAverageStats.add(new MatchAverageStats(match.getQueryMatch(), match.getConceptMatch(), numeratorPart));
			}
		}
		if (args.getNormaliserArgs().getDescriptionNormaliser() > 0
				&& (!average || args.getWeightArgs().getDescriptionWeight() > 0)
				&& hasTokens(processedQuery.getDescriptionTokens())) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.description, args.getAlgorithmArgs(), args.getIdfArgs(), args.getMultiplierArgs(), args.getNormaliserArgs());
			match.setScore(match.getScore() * args.getNormaliserArgs().getDescriptionNormaliser());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				double numeratorPart = args.getWeightArgs().getDescriptionWeight() * Math.pow(match.getScore(), scaling);
				numerator += numeratorPart;
				denominator += args.getWeightArgs().getDescriptionWeight();
				matchAverageStats.add(new MatchAverageStats(match.getQueryMatch(), match.getConceptMatch(), numeratorPart));
			}
		}
		if (args.getNormaliserArgs().getPublicationTitleNormaliser() > 0
				&& (!average || args.getWeightArgs().getPublicationTitleWeight() > 0)
				&& hasPublicationTokens(processedQuery.getProcessedPublications(), QueryMatchType.publication_title)) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.publication_title, args.getAlgorithmArgs(), args.getIdfArgs(), args.getMultiplierArgs(), args.getNormaliserArgs());
			match.setScore(match.getScore() * args.getNormaliserArgs().getPublicationTitleNormaliser());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				double numeratorPart = args.getWeightArgs().getPublicationTitleWeight() * Math.pow(match.getScore(), scaling);
				numerator += numeratorPart;
				denominator += args.getWeightArgs().getPublicationTitleWeight();
				matchAverageStats.add(new MatchAverageStats(match.getQueryMatch(), match.getConceptMatch(), numeratorPart));
			}
		}
		if (args.getNormaliserArgs().getPublicationKeywordNormaliser() > 0
				&& (!average || args.getWeightArgs().getPublicationKeywordWeight() > 0)
				&& hasPublicationTokens(processedQuery.getProcessedPublications(), QueryMatchType.publication_keyword)) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.publication_keyword, args.getAlgorithmArgs(), args.getIdfArgs(), args.getMultiplierArgs(), args.getNormaliserArgs());
			match.setScore(match.getScore() * args.getNormaliserArgs().getPublicationKeywordNormaliser());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				double numeratorPart = args.getWeightArgs().getPublicationKeywordWeight() * Math.pow(match.getScore(), scaling);
				numerator += numeratorPart;
				denominator += args.getWeightArgs().getPublicationKeywordWeight();
				matchAverageStats.add(new MatchAverageStats(match.getQueryMatch(), match.getConceptMatch(), numeratorPart));
			}
		}
		if (args.getNormaliserArgs().getPublicationMeshNormaliser() > 0
				&& (!average || args.getWeightArgs().getPublicationMeshWeight() > 0)
				&& hasPublicationTokens(processedQuery.getProcessedPublications(), QueryMatchType.publication_mesh)) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.publication_mesh, args.getAlgorithmArgs(), args.getIdfArgs(), args.getMultiplierArgs(), args.getNormaliserArgs());
			match.setScore(match.getScore() * args.getNormaliserArgs().getPublicationMeshNormaliser());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				double numeratorPart = args.getWeightArgs().getPublicationMeshWeight() * Math.pow(match.getScore(), scaling);
				numerator += numeratorPart;
				denominator += args.getWeightArgs().getPublicationMeshWeight();
				matchAverageStats.add(new MatchAverageStats(match.getQueryMatch(), match.getConceptMatch(), numeratorPart));
			}
		}
		if (args.getNormaliserArgs().getPublicationMinedNormaliser() > 0
				&& (!average || args.getWeightArgs().getPublicationMinedWeight() > 0)
				&& (hasPublicationTokens(processedQuery.getProcessedPublications(), QueryMatchType.publication_efo) || hasPublicationTokens(processedQuery.getProcessedPublications(), QueryMatchType.publication_go))) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.publication_efo, args.getAlgorithmArgs(), args.getIdfArgs(), args.getMultiplierArgs(), args.getNormaliserArgs());
			Match otherMatch = getMatch(processedConcept, processedQuery, QueryMatchType.publication_go, args.getAlgorithmArgs(), args.getIdfArgs(), args.getMultiplierArgs(), args.getNormaliserArgs());
			if (otherMatch.compareTo(match) > 0) match = otherMatch;
			match.setScore(match.getScore() * args.getNormaliserArgs().getPublicationMinedNormaliser());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				double numeratorPart = args.getWeightArgs().getPublicationMinedWeight() * Math.pow(match.getScore(), scaling);
				numerator += numeratorPart;
				denominator += args.getWeightArgs().getPublicationMinedWeight();
				matchAverageStats.add(new MatchAverageStats(match.getQueryMatch(), match.getConceptMatch(), numeratorPart));
			}
		}
		if (args.getNormaliserArgs().getPublicationAbstractNormaliser() > 0
				&& (!average || args.getWeightArgs().getPublicationAbstractWeight() > 0)
				&& hasPublicationTokens(processedQuery.getProcessedPublications(), QueryMatchType.publication_abstract)) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.publication_abstract, args.getAlgorithmArgs(), args.getIdfArgs(), args.getMultiplierArgs(), args.getNormaliserArgs());
			match.setScore(match.getScore() * args.getNormaliserArgs().getPublicationAbstractNormaliser());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				double numeratorPart = args.getWeightArgs().getPublicationAbstractWeight() * Math.pow(match.getScore(), scaling);
				numerator += numeratorPart;
				denominator += args.getWeightArgs().getPublicationAbstractWeight();
				matchAverageStats.add(new MatchAverageStats(match.getQueryMatch(), match.getConceptMatch(), numeratorPart));
			}
		}
		if (args.getNormaliserArgs().getPublicationFulltextNormaliser() > 0
				&& (!average || args.getWeightArgs().getPublicationFulltextWeight() > 0)
				&& hasPublicationTokens(processedQuery.getProcessedPublications(), QueryMatchType.publication_fulltext)) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.publication_fulltext, args.getAlgorithmArgs(), args.getIdfArgs(), args.getMultiplierArgs(), args.getNormaliserArgs());
			match.setScore(match.getScore() * args.getNormaliserArgs().getPublicationFulltextNormaliser());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				double numeratorPart = args.getWeightArgs().getPublicationFulltextWeight() * Math.pow(match.getScore(), scaling);
				numerator += numeratorPart;
				denominator += args.getWeightArgs().getPublicationFulltextWeight();
				matchAverageStats.add(new MatchAverageStats(match.getQueryMatch(), match.getConceptMatch(), numeratorPart));
			}
		}
		if (args.getNormaliserArgs().getDocNormaliser() > 0
				&& (!average || args.getWeightArgs().getDocWeight() > 0)
				&& hasListTokens(processedQuery.getDocsTokens())) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.doc, args.getAlgorithmArgs(), args.getIdfArgs(), args.getMultiplierArgs(), args.getNormaliserArgs());
			match.setScore(match.getScore() * args.getNormaliserArgs().getDocNormaliser());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				double numeratorPart = args.getWeightArgs().getDocWeight() * Math.pow(match.getScore(), scaling);
				numerator += numeratorPart;
				denominator += args.getWeightArgs().getDocWeight();
				matchAverageStats.add(new MatchAverageStats(match.getQueryMatch(), match.getConceptMatch(), numeratorPart));
			}
		}
		if (args.getNormaliserArgs().getWebpageNormaliser() > 0
				&& (!average || args.getWeightArgs().getWebpageWeight() > 0)
				&& hasListTokens(processedQuery.getWebpagesTokens())) {
			Match match = getMatch(processedConcept, processedQuery, QueryMatchType.webpage, args.getAlgorithmArgs(), args.getIdfArgs(), args.getMultiplierArgs(), args.getNormaliserArgs());
			match.setScore(match.getScore() * args.getNormaliserArgs().getWebpageNormaliser());
			if (match.compareTo(bestMatch) > 0) bestMatch = match;
			if (average) {
				double numeratorPart = args.getWeightArgs().getWebpageWeight() * Math.pow(match.getScore(), scaling);
				numerator += numeratorPart;
				denominator += args.getWeightArgs().getWebpageWeight();
				matchAverageStats.add(new MatchAverageStats(match.getQueryMatch(), match.getConceptMatch(), numeratorPart));
			}
		}

		if (average && denominator > 0) {
			bestMatch.setBestOneScore(bestMatch.getScore());
			bestMatch.setScore(numerator / denominator);

			for (MatchAverageStats mas : matchAverageStats) {
				mas.setScore(mas.getScore() / denominator);
			}
			Collections.sort(matchAverageStats, Collections.reverseOrder());
			bestMatch.setMatchAverageStats(matchAverageStats);
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
		Mapping mapping = new Mapping(args.getMatches(), args.getBranches());

		Map<EdamUri, Match> matches = new HashMap<>();

		for (Map.Entry<EdamUri, ConceptProcessed> conceptEntry : processedConcepts.entrySet()) {
			EdamUri edamUri = conceptEntry.getKey();
			ConceptProcessed processedConcept = conceptEntry.getValue();

			if (!args.getBranches().contains(edamUri.getBranch())) continue;

			if ((processedConcept.isObsolete() && !args.isObsolete())
				|| (processedConcept.getDirectParents().isEmpty() && !args.isTopLevel())) {
				Match zeroMatch = new Match(0, new ConceptMatch(0, ConceptMatchType.none, -1), new QueryMatch(0, QueryMatchType.none, -1, -1));
				zeroMatch.setEdamUri(edamUri);
				matches.put(edamUri, zeroMatch);
				continue;
			}

			Match match = getBestMatch(processedConcept, processedQuery, args);
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

		if (!args.isInferiorParentsChildren() && !args.isAnnotations()) {
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

				if (processedConcepts.get(edamUri).getDirectParents().isEmpty() && !args.isTopLevel()) {
					match.setRemoved(true);
					continue;
				}
				if (processedConcepts.get(edamUri).isObsolete() && !args.isObsolete()) {
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
			if (processedConcepts.get(match.getEdamUri()).isObsolete() && !args.isObsolete()) continue;
			if (!args.isAnnotations() && match.isExistingAnnotation()) continue;

			double goodScore = 0;
			double badScore = 0;
			switch (match.getEdamUri().getBranch()) {
			case topic:
				goodScore = args.getScoreArgs().getGoodScoreTopic();
				badScore = args.getScoreArgs().getBadScoreTopic();
				break;
			case operation:
				goodScore = args.getScoreArgs().getGoodScoreOperation();
				badScore = args.getScoreArgs().getBadScoreOperation();
				break;
			case data:
				goodScore = args.getScoreArgs().getGoodScoreData();
				badScore = args.getScoreArgs().getBadScoreData();
				break;
			case format:
				goodScore = args.getScoreArgs().getGoodScoreFormat();
				badScore = args.getScoreArgs().getBadScoreFormat();
				break;
			}

			double score = 0;
			if (args.getAlgorithmArgs().getMappingStrategy() == MapperStrategy.average) {
				score = match.getBestOneScore();
			} else if (args.getAlgorithmArgs().getPathWeight() > 0 && args.getAlgorithmArgs().getParentWeight() > 0) {
				score = match.getWithoutPathScore();
			} else {
				score = match.getScore();
			}

			if (score > goodScore) {
				if (!args.getScoreArgs().isOutputGoodScores()) continue;
			} else if (score >= badScore && score <= goodScore) {
				if (!args.getScoreArgs().isOutputMediumScores()) continue;
			} else if (score < badScore) {
				if (!args.getScoreArgs().isOutputBadScores()) continue;
			}

			if (!args.isInferiorParentsChildren()) {
				removeParents(match.getEdamUri(), matches);
				removeChildren(match.getEdamUri(), matches);
			}

			addParentsChildren(match, mapping, false);
			mapping.addMatch(match);
		}

		if (args.isAnnotations() && annotations.size() > 0) {
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
