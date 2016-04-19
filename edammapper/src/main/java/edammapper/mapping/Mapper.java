package edammapper.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edammapper.args.MapperArgs;
import edammapper.edam.Concept;
import edammapper.edam.ConceptPP;
import edammapper.edam.EdamUri;
import edammapper.preprocessing.PreProcessor;
import edammapper.query.Query;

public class Mapper {

	private class M {
		private M(int position, double score) {
			this.position = position;
			this.score = score;
		}
		private int position;
		private double score;
	}

	private final Map<EdamUri, Concept> concepts;
	private final Map<EdamUri, ConceptPP> conceptsPP;

	private final PreProcessor pp;

	private final MapperArgs args;

	private final double[] matchDists = { 1, 0, 0, 0 };

	public Mapper(Map<EdamUri, Concept> concepts, Map<EdamUri, ConceptPP> conceptsPP, PreProcessor pp, MapperArgs args) {
		// null
		this.concepts = concepts;
		this.conceptsPP = conceptsPP;
		this.pp = pp;
		this.args = args;

		// final?
		this.matchDists[1] = args.algo().getPositionOffBy1();
		this.matchDists[2] = args.algo().getPositionOffBy2();
	}

	private double matchLR(boolean left, List<List<M>> toMatch, int toI, M match, int fromsSize) {
		int matchDist = matchDists.length - 1;
		int sign = (left ? -1 : 1);

		if (toI + sign < 0) {
			if (match.position + sign < 0) {
				matchDist = 0;
			}
		} else if (toI + sign > toMatch.size() - 1) {
			if (match.position + sign > fromsSize - 1) {
				matchDist = 0;
			}
		} else {
			for (M lrmatch : toMatch.get(toI + sign)) {
				if (match.position == lrmatch.position - sign) {
					matchDist = 0;
					break;
				}
				if (match.position == lrmatch.position) {
					matchDist = 1;
					continue;
				}
				for (int j = 1; j < matchDists.length - 1; ++j) {
					if (match.position == lrmatch.position - sign * (1 + j) || match.position == lrmatch.position + sign * j) {
						if (matchDist > j) {
							matchDist = j;
							break;
						}
					}
				}
			}
		}

		return matchDists[matchDist];
	}

	private double match(List<String> froms, List<String> tos) {
		List<List<M>> toMatch = new ArrayList<>(tos.size());
		for (int toI = 0; toI < tos.size(); ++toI) {
			toMatch.add(new ArrayList<>());
		}

		for (int toCW = 0; toCW <= args.algo().getCompoundWords(); ++toCW) {
			for (int toI = 0; toI < tos.size() - toCW; ++toI) {
				String to = tos.get(toI);
				for (int toJ = 1; toJ <= toCW; ++toJ) {
					to += " " + tos.get(toI + toJ);
				}

				for (int fromCW = 0; fromCW <= args.algo().getCompoundWords(); ++fromCW) {
					// Only do one-to-many and many-to-one matches
					if (toCW > 0 && fromCW > 0) break;

					for (int fromI = 0; fromI < froms.size() - fromCW; ++fromI) {
						String from = froms.get(fromI);
						for (int fromJ = 1; fromJ <= fromCW; ++fromJ) {
							from += " " + froms.get(fromI + fromJ);
						}

						int d = Levenshtein.standard(from, to);
						if (toCW > 0 || fromCW > 0) {
							int cw = toCW + fromCW;
							d = d - cw;
							if (d > cw + 1) d = -1;
						}
						if (d > -1) {
							int l = Math.max(from.length(), to.length());
							double score = (l - args.algo().getMismatchMultiplier() * d) / (double)l;
							if (score > 0) {
								for (int toJ = 0; toJ <= toCW; ++toJ) {
									for (int fromJ = 0; fromJ <= fromCW; ++fromJ) {
										toMatch.get(toI + toJ).add(new M(fromI + fromJ, score));
									}
								}
							}
						}
					}
				}
			}
		}

		double scoreSum = 0;
		for (int i = 0; i < toMatch.size(); ++i) {
			double scoreMax = 0;
			for (M match : toMatch.get(i)) {
				double lmatchDist = matchLR(true, toMatch, i, match, froms.size());
				double rmatchDist = matchLR(false, toMatch, i, match, froms.size());

				double score = match.score * (1 - args.algo().getPositionLoss() * (2 - lmatchDist - rmatchDist) / 2);
				if (score > scoreMax) {
					scoreMax = score;
				}
			}
			scoreSum += scoreMax;
		}

		return scoreSum / toMatch.size();
	}

	private double matchScore(List<String> query, List<String> concept) {
		double conceptScore = match(query, concept);
		double queryScore = match(concept, query);
		return (conceptScore * args.algo().getConceptWeight() + queryScore * args.algo().getQueryWeight()) / (args.algo().getConceptWeight() + args.algo().getQueryWeight());
	}

	private Match getMatch(String query, List<String> queryPP, EdamUri edamUri, Concept concept, ConceptPP conceptPP, double lastMatchScore, Match bestMatch, MatchType matchType, int synonymIndex) {
		String matchString;
		List<String> matchStringPP;
		switch (matchType) {
			case label:
				matchString = concept.getLabel();
				matchStringPP = conceptPP.getLabel();
				break;
			case exact_synonym:
				matchString = concept.getExactSynonyms().get(synonymIndex);
				matchStringPP = conceptPP.getExactSynonyms().get(synonymIndex);
				break;
			case narrow_synonym:
				matchString = concept.getNarrowSynonyms().get(synonymIndex);
				matchStringPP = conceptPP.getNarrowSynonyms().get(synonymIndex);
				break;
			case broad_synonym:
				matchString = concept.getBroadSynonyms().get(synonymIndex);
				matchStringPP = conceptPP.getBroadSynonyms().get(synonymIndex);
				break;
			case definition:
				matchString = concept.getDefinition();
				matchStringPP = conceptPP.getDefinition();
				break;
			case comment:
				matchString = concept.getComment();
				matchStringPP = conceptPP.getComment();
				break;
			default:
				// throw
				matchString = "";
				matchStringPP = null;
		}

		MatchConfidence matchConfidence;
		double score;
		if (query.trim().toLowerCase(Locale.ROOT).equals(matchString.trim().toLowerCase(Locale.ROOT))) {
			matchConfidence = MatchConfidence.exact;
			score = 1;
		} else {
			matchConfidence = MatchConfidence.inexact;
			score = matchScore(queryPP, matchStringPP);
		}
		switch (matchType) {
			case label: score *= args.algo().getLabelMultiplier(); break;
			case exact_synonym: score *= args.algo().getExactSynonymMultiplier(); break;
			case narrow_synonym: score *= args.algo().getNarrowBroadMultiplier(); break;
			case broad_synonym: score *= args.algo().getNarrowBroadMultiplier(); break;
			case definition: score *= args.algo().getDefinitionMultiplier(); break;
			case comment: score *= args.algo().getCommentMultiplier(); break;
		}

		if (score >= lastMatchScore && (bestMatch == null || score >= bestMatch.getScore())) {
			Match match = new Match(query.length(), matchString.length(), edamUri, matchType, synonymIndex, matchConfidence, score);
			if (bestMatch == null || match.compareTo(bestMatch) > 0) {
				return match;
			} else {
				return bestMatch;
			}
		} else {
			return bestMatch;
		}
	}

	private Match getMatch(String query, List<String> queryPP, EdamUri edamUri, Concept concept, ConceptPP conceptPP, double lastMatchScore, Match bestMatch, MatchType matchType) {
		return getMatch(query, queryPP, edamUri, concept, conceptPP, lastMatchScore, bestMatch, matchType, -1);
	}

	public List<Mapping> map(List<Query> queries) {
		List<Mapping> mappings = new ArrayList<>(queries.size());
		int tmp = 0;
		for (Query query : queries) {
			System.err.println(++tmp);
			List<String> queryPP = pp.process(query.getQuery());
			Mapping mapping = new Mapping(args.getMatch(), args.getBranches());

			for (Map.Entry<EdamUri, Concept> conceptEntry : concepts.entrySet()) {
				EdamUri edamUri = conceptEntry.getKey();
				Concept concept = conceptEntry.getValue();
				ConceptPP conceptPP = conceptsPP.get(edamUri);

				if (concept.isObsolete() && !args.getObsolete()) continue;
				if (!args.getBranches().contains(edamUri.getBranch())) continue;

				double lastMatchScore = mapping.getLastMatchScore(edamUri.getBranch());
				Match bestMatch = null;

				if (args.algo().getLabelMultiplier() > 0) {
					bestMatch = getMatch(query.getQuery(), queryPP, edamUri, concept, conceptPP, lastMatchScore, bestMatch, MatchType.label);
				}
				if (args.algo().getExactSynonymMultiplier() > 0) {
					for (int i = 0; i < concept.getExactSynonyms().size(); ++i) {
						bestMatch = getMatch(query.getQuery(), queryPP, edamUri, concept, conceptPP, lastMatchScore, bestMatch, MatchType.exact_synonym, i);
					}
				}
				if (args.algo().getNarrowBroadMultiplier() > 0) {
					for (int i = 0; i < concept.getNarrowSynonyms().size(); ++i) {
						bestMatch = getMatch(query.getQuery(), queryPP, edamUri, concept, conceptPP, lastMatchScore, bestMatch, MatchType.narrow_synonym, i);
					}
					for (int i = 0; i < concept.getBroadSynonyms().size(); ++i) {
						bestMatch = getMatch(query.getQuery(), queryPP, edamUri, concept, conceptPP, lastMatchScore, bestMatch, MatchType.broad_synonym, i);
					}
				}
				if (args.algo().getDefinitionMultiplier() > 0) {
					bestMatch = getMatch(query.getQuery(), queryPP, edamUri, concept, conceptPP, lastMatchScore, bestMatch, MatchType.definition);
				}
				if (args.algo().getCommentMultiplier() > 0) {
					bestMatch = getMatch(query.getQuery(), queryPP, edamUri, concept, conceptPP, lastMatchScore, bestMatch, MatchType.comment);
				}

				if (bestMatch != null) {
					mapping.addMatch(edamUri.getBranch(), bestMatch);
				}
			}

			mappings.add(mapping);
		}
		return mappings;
	}
}
