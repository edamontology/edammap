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

package org.edamontology.edammap.core.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.edamontology.pubfetcher.core.common.FetcherArgs;
import org.edamontology.pubfetcher.core.common.PubFetcher;
import org.edamontology.pubfetcher.core.common.Version;
import org.edamontology.pubfetcher.core.db.publication.Publication;
import org.edamontology.pubfetcher.core.db.publication.PublicationPart;
import org.edamontology.pubfetcher.core.db.publication.PublicationPartName;
import org.edamontology.pubfetcher.core.db.webpage.Webpage;

import org.edamontology.edammap.core.args.CoreArgs;
import org.edamontology.edammap.core.benchmarking.MappingTest;
import org.edamontology.edammap.core.benchmarking.MatchTest;
import org.edamontology.edammap.core.benchmarking.Results;
import org.edamontology.edammap.core.edam.Branch;
import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.mapping.ConceptMatch;
import org.edamontology.edammap.core.mapping.ConceptMatchType;
import org.edamontology.edammap.core.mapping.Match;
import org.edamontology.edammap.core.mapping.MatchAverageStats;
import org.edamontology.edammap.core.mapping.QueryMatch;
import org.edamontology.edammap.core.mapping.QueryMatchType;
import org.edamontology.edammap.core.mapping.args.ScoreArgs;
import org.edamontology.edammap.core.query.Keyword;
import org.edamontology.edammap.core.query.Link;
import org.edamontology.edammap.core.query.PublicationIdsQuery;
import org.edamontology.edammap.core.query.Query;
import org.edamontology.edammap.core.query.QueryLoader;
import org.edamontology.edammap.core.query.QueryType;

public class Report {

	private static final String[] FONT_RESOURCES = new String[] {
		"Spectral-Regular-ext.woff2",
		"Spectral-Regular.woff2",
		"Spectral-Bold-ext.woff2",
		"Spectral-Bold.woff2",
		"SpectralSC-Regular-ext.woff2",
		"SpectralSC-Regular.woff2",
		"SpectralSC-Bold-ext.woff2",
		"SpectralSC-Bold.woff2",
		"ofl.txt",
		"fa-regular-400.woff2",
		"fa-solid-900.woff2",
		"LICENSE.txt"
	};

	private static String percent(double val) {
		double percent = val * 100;
		if (percent < 0.1) {
			return String.format(Locale.ROOT, "%.2g%%", percent);
		} else {
			return String.format(Locale.ROOT, "%.2f%%", percent);
		}
	}

	private static void writeLinks(FetcherArgs fetcherArgs, Writer writer, List<Link> webpageUrls, List<Webpage> webpages) throws IOException {
		for (int i = 0; i < webpageUrls.size(); ++i) {
			Link webpageUrl = webpageUrls.get(i);
			Webpage webpage = webpages.get(i);
			if (webpageUrl == null || webpageUrl.getUrl() == null || webpageUrl.getUrl().isEmpty()) continue;
			String status = "";
			if (webpage == null) {
				status = "broken";
			} else if (webpage.isBroken()) {
				status = "broken";
			} else if (webpage.isEmpty()) {
				status = "empty";
			} else if (!webpage.isFinal(fetcherArgs)) {
				status = "non-final";
			}
			writer.write("\t\t\t\t\t<div class=\"with-meta\"><span" + (status.isEmpty() ? "" : " class=\"" + status + "\"") + ">");
			writer.write(PubFetcher.getLinkHtml(webpageUrl.getUrl()));
			if (webpageUrl.getType() != null && !webpageUrl.getType().isEmpty()) {
				writer.write(" <span class=\"link-type\">(" + PubFetcher.escapeHtml(webpageUrl.getType()) + ")</span>");
			}
			if (!status.isEmpty()) {
				writer.write(" (" + status + ")");
			}
			writer.write("</span><span class=\"spacer\"></span>");
			if (webpage != null) {
				writer.write("<span class=\"" + (status.isEmpty() ? "info" : "warning") + "\" tabindex=\"0\"></span>");
			}
			writer.write("\n");
			if (webpage != null) {
				writer.write("\t\t\t\t\t\t<div class=\"" + (status.isEmpty() ? "info" : "warning") + "-box\" tabindex=\"0\">\n");
				writer.write("\t\t\t\t\t\t\t<h4><span" + (status.isEmpty() ? "" : " class=\"" + status + "\"") + ">");
				writer.write(PubFetcher.getLinkHtml(webpageUrl.getUrl()));
				if (webpageUrl.getType() != null && !webpageUrl.getType().isEmpty()) {
					writer.write(" <span class=\"link-type\">(" + PubFetcher.escapeHtml(webpageUrl.getType()) + ")</span>");
				}
				if (!status.isEmpty()) {
					writer.write(" (" + status + ")");
				}
				writer.write("</span></h4>\n");
				writer.write(webpage.toStringMetaHtml("\t\t\t\t\t\t\t"));
				writer.write("\n");
				writer.write("\t\t\t\t\t\t</div>\n");
			}
			writer.write("\t\t\t\t\t</div>\n");
		}
	}

	private static void writePublicationId(Writer writer, String label, String id, String idLink, String from) throws IOException {
		if (!id.isEmpty()) {
			writer.write("\t\t\t\t\t<div><span>" + label + ":</span> <span>" + idLink);
			if (!from.isEmpty()) {
				writer.write(" (from " + PubFetcher.getLinkHtml(from) + ")");
			}
			writer.write("</span></div>\n");
		}
	}

	private static void writePublicationPartMeta(FetcherArgs fetcherArgs, Writer writer, Publication publication, PublicationPartName name) throws IOException {
		PublicationPart part = publication.getPart(name);
		String partStatusIcon = "info";
		if (!part.isUsable(fetcherArgs)) {
			partStatusIcon = "warning";
		} else if (!part.isFinal(fetcherArgs)) {
			partStatusIcon = "info-warning";
		}
		writer.write("\t\t\t\t\t<div class=\"pub-part\"><div class=\"pub-" + partStatusIcon + "\" tabindex=\"0\"><div class=\"pub-" + (partStatusIcon.equals("info-warning") ? "info" : partStatusIcon) + "-box\" tabindex=\"0\">\n");
		writer.write(part.toStringMetaHtml("\t\t\t\t\t\t"));
		writer.write("\n");
		writer.write("\t\t\t\t\t</div></div>");
	}

	private static void writePublications(FetcherArgs fetcherArgs, Writer writer, List<PublicationIdsQuery> publicationIds, List<Publication> publications) throws IOException {
		if (publicationIds == null) return;
		for (int i = 0; i < publicationIds.size(); ++i) {
			PublicationIdsQuery publicationId = publicationIds.get(i);
			Publication publication = publications.get(i);
			if (publicationId == null || publicationId.isEmpty()) continue;
			String status = "";
			String statusIcon = "pub-info";
			if (publication == null) {
				status = "missing";
				statusIcon = "pub-warning";
			} else if (publication.isEmpty()) {
				status = "empty";
				statusIcon = "pub-warning";
			} else if (!publication.isUsable(fetcherArgs)) {
				status = "non-usable";
				statusIcon = "pub-warning";
			} else if (!publication.isFinal(fetcherArgs)) {
				status = "non-final";
				statusIcon = "pub-info-warning";
			}
			writer.write("\t\t\t<div class=\"publication\">\n");
			writer.write("\t\t\t\t<div class=\"pub-head");
			if (!status.isEmpty()) {
				writer.write(" pub-" + status);
			}
			writer.write("\">");
			if (publication != null || !publicationId.isEmpty()) {
				writer.write("<div class=\"" + statusIcon + "\" tabindex=\"0\"><div class=\"" + (statusIcon.equals("pub-info-warning") ? "pub-info" : statusIcon)  + "-box\" tabindex=\"0\">\n");
				if (!publicationId.isEmpty()) {
					writePublicationId(writer, "pmid", publicationId.getPmid(), PubFetcher.getPmidLinkHtml(publicationId.getPmid()), publicationId.getPmidUrl());
					writePublicationId(writer, "pmcid", publicationId.getPmcid(), PubFetcher.getPmcidLinkHtml(publicationId.getPmcid()), publicationId.getPmcidUrl());
					writePublicationId(writer, "doi", publicationId.getDoi(), PubFetcher.getDoiLinkHtml(publicationId.getDoi()), publicationId.getDoiUrl());
					if (publication != null) {
						writer.write("\t\t\t\t\t<hr>\n");
					}
				}
				if (publication != null) {
					writer.write(publication.toStringMetaHtml("\t\t\t\t\t"));
					writer.write("\n");
				}
				writer.write("\t\t\t\t</div></div>");
			}
			if (!status.isEmpty()) {
				writer.write("<span>" + status.toUpperCase(Locale.ROOT) + "</span> ");
			}
			writer.write("Publication");
			if (publicationId.getType() != null && !publicationId.getType().isEmpty()) {
				writer.write(" (" + PubFetcher.escapeHtml(publicationId.getType()) + ")");
			}
			if (publication != null) {
				writer.write("&nbsp; <span class=\"" + (publication.isOA() ? "oa" : "not-oa") + "\">" + (publication.isOA() ? "" : "Not ") + "Open Access</span>");
			}
			writer.write("</div>\n");
			writer.write("\t\t\t\t<div class=\"pub-body\">\n");
			if (publication != null && (publication.getIdCount() > 0 || !publication.isEmpty())) {
				if (!publication.getPmid().isEmpty()) {
					writePublicationPartMeta(fetcherArgs, writer, publication, PublicationPartName.pmid);
					writer.write("<span class=\"dt\">pmid:</span> " + PubFetcher.getPmidLinkHtml(publication.getPmid().getContent()) + "</div>\n");
				}
				if (!publication.getPmcid().isEmpty()) {
					writePublicationPartMeta(fetcherArgs, writer, publication, PublicationPartName.pmcid);
					writer.write("<span class=\"dt\">pmcid:</span> " + PubFetcher.getPmcidLinkHtml(publication.getPmcid().getContent()) + "</div>\n");
				}
				if (!publication.getDoi().isEmpty()) {
					writePublicationPartMeta(fetcherArgs, writer, publication, PublicationPartName.doi);
					writer.write("<span class=\"dt\">doi:</span> " + PubFetcher.getDoiLinkHtml(publication.getDoi().getContent()) + "</div>\n");
				}
				if (publication.getIdCount() > 0 && !publication.isEmpty()) {
					writer.write("\t\t\t\t\t<hr>\n");
				}
				if (!publication.getTitle().isEmpty()) {
					writePublicationPartMeta(fetcherArgs, writer, publication, PublicationPartName.title);
					writer.write("<span class=\"dt\">Title:</span> " + publication.getTitle().toStringPlainHtml() + "</div>\n");
				}
				if (!publication.getKeywords().isEmpty()) {
					writePublicationPartMeta(fetcherArgs, writer, publication, PublicationPartName.keywords);
					writer.write("<span class=\"dt\">Keywords:</span> " + publication.getKeywords().toStringPlainHtml() + "</div>\n");
				}
				if (!publication.getMeshTerms().isEmpty()) {
					writePublicationPartMeta(fetcherArgs, writer, publication, PublicationPartName.mesh);
					writer.write("<span class=\"dt\">MeSH terms:</span> " + publication.getMeshTerms().toStringPlainHtml() + "</div>\n");
				}
				if (!publication.getEfoTerms().isEmpty()) {
					writePublicationPartMeta(fetcherArgs, writer, publication, PublicationPartName.efo);
					writer.write("<span class=\"dt\">EFO terms:</span> " + publication.getEfoTerms().toStringPlainHtml() + "</div>\n");
				}
				if (!publication.getGoTerms().isEmpty()) {
					writePublicationPartMeta(fetcherArgs, writer, publication, PublicationPartName.go);
					writer.write("<span class=\"dt\">GO terms:</span> " + publication.getGoTerms().toStringPlainHtml() + "</div>\n");
				}
				if (!publication.getAbstract().isEmpty()) {
					writePublicationPartMeta(fetcherArgs, writer, publication, PublicationPartName.theAbstract);
					writer.write("<span class=\"dt\">Abstract:</span> " + publication.getAbstract().toStringPlainHtml() + "</div>\n");
				}
				if (!publication.getFulltext().isEmpty()) {
					writePublicationPartMeta(fetcherArgs, writer, publication, PublicationPartName.fulltext);
					writer.write("<span class=\"dt\">Full text present:</span> [" + publication.getFulltext().getSize() + " characters]</div>\n");
				}
			}
			writer.write("\t\t\t\t</div>\n");
			writer.write("\t\t\t</div>\n");
		}
	}

	private static String conceptMatchString(Concept concept, ConceptMatch conceptMatch) {
		switch (conceptMatch.getType()) {
			case label: return PubFetcher.escapeHtml(concept.getLabel());
			case exact_synonym: return PubFetcher.escapeHtml(concept.getExactSynonyms().get(conceptMatch.getSynonymIndex()));
			case narrow_synonym: return PubFetcher.escapeHtml(concept.getNarrowSynonyms().get(conceptMatch.getSynonymIndex()));
			case broad_synonym: return PubFetcher.escapeHtml(concept.getBroadSynonyms().get(conceptMatch.getSynonymIndex()));
			case definition: return PubFetcher.escapeHtml(concept.getDefinition());
			case comment: return PubFetcher.escapeHtml(concept.getComment());
			default: return "";
		}
	}

	private static void writeParentsChildren(Writer writer, Map<EdamUri, Concept> concepts, List<EdamUri> pc, String desc) throws IOException {
		if (!pc.isEmpty()) {
			writer.write("<br><span class=\"pc\">[" + desc + " ");
			writer.write(pc.stream()
				.map(a -> PubFetcher.getLinkHtml(a.toString(), concepts.get(a).getLabel()))
				.collect(Collectors.joining("; ")));
			writer.write("]</span>");
		}
	}

	private static void writeConcept(Writer writer, Map<EdamUri, Concept> concepts, Match match) throws IOException {
		EdamUri edamUri = match.getEdamUri();
		Concept concept = concepts.get(edamUri); // TODO
		writer.write("\t\t\t\t\t<div class=\"concept\">");
		if (concept.isObsolete()) {
			writer.write("<span class=\"obsolete\">");
		}
		writer.write("<strong>" + PubFetcher.getLinkHtml(edamUri.toString(), concept.getLabel() + " (" + edamUri.getNrString() + ")") + "</strong>");
		if (match.getConceptMatch().getType() != ConceptMatchType.label && match.getConceptMatch().getType() != ConceptMatchType.none) {
			writer.write(" (" + conceptMatchString(concept, match.getConceptMatch()) + ")");
		}
		if (concept.isObsolete()) {
			writer.write("</span>");
		}
		writeParentsChildren(writer, concepts, match.getParents(), "Child of");
		writeParentsChildren(writer, concepts, match.getParentsAnnotation(), "Child of annotation");
		writeParentsChildren(writer, concepts, match.getParentsRemainingAnnotation(), "Child of excluded annotation");
		writeParentsChildren(writer, concepts, match.getChildren(), "Parent of");
		writeParentsChildren(writer, concepts, match.getChildrenAnnotation(), "Parent of annotation");
		writeParentsChildren(writer, concepts, match.getChildrenRemainingAnnotation(), "Parent of excluded annotation");
		writer.write("</div>\n");
	}

	private static String queryMatchString(Query query, QueryMatch queryMatch, boolean text) {
		QueryMatchType type = queryMatch.getType();
		int index = queryMatch.getIndex();
		if (type == QueryMatchType.webpage && index >= 0 && query.getWebpageUrls().get(index) != null) {
			if (text) return PubFetcher.getLinkHtml(query.getWebpageUrls().get(index).getUrl(), type.toString());
			else return PubFetcher.getLinkHtml(query.getWebpageUrls().get(index).getUrl());
		} else if (type == QueryMatchType.doc && index >= 0 && query.getDocUrls().get(index) != null) {
			if (text) return PubFetcher.getLinkHtml(query.getDocUrls().get(index).getUrl(), type.toString());
			else return PubFetcher.getLinkHtml(query.getDocUrls().get(index).getUrl());
		} else if (type.isPublication() && index >= 0) {
			if (text) return PubFetcher.getIdLinkHtml(query.getPublicationIds().get(index), type.toString());
			else return PubFetcher.getIdLinkHtml(query.getPublicationIds().get(index));
		} else {
			if (text) return type.toString();
			else return "";
		}
	}

	private static String publicationMatchString(QueryMatch queryMatch, List<Publication> publications) {
		int index = queryMatch.getIndex();
		int indexInPublication = queryMatch.getIndexInPublication();
		if (index >= 0) {
			switch (queryMatch.getType()) {
				case publication_keyword: return PubFetcher.escapeHtml(publications.get(index).getKeywords().getList().get(indexInPublication));
				case publication_mesh: return publications.get(index).getMeshTerms().getList().get(indexInPublication).toStringHtml();
				case publication_efo: return publications.get(index).getEfoTerms().getList().get(indexInPublication).toStringHtml();
				case publication_go: return publications.get(index).getGoTerms().getList().get(indexInPublication).toStringHtml();
				default: return "";
			}
		} else {
			return "";
		}
	}

	private static void writeQueryMatch(Writer writer, Query query, List<Publication> publications, QueryMatch queryMatch, boolean main) throws IOException {
		QueryMatchType type = queryMatch.getType();
		int index = queryMatch.getIndex();
		if (main) {
			writer.write("\t\t\t\t\t<div class=\"type\">");
		} else {
			writer.write("<span>" + type + "</span>");
		}
		String queryMatchString = queryMatchString(query, queryMatch, main);
		if (!queryMatchString.isEmpty()) {
			if (!main) writer.write("<br>");
			writer.write(queryMatchString);
		}
		String publicationMatchString = publicationMatchString(queryMatch, publications);
		if (!publicationMatchString.isEmpty()) {
			writer.write("<br>");
			writer.write(publicationMatchString);
		}
		if (type == QueryMatchType.keyword && index >= 0 && query.getKeywords().get(index) != null) {
			writer.write("<br>");
			writer.write(PubFetcher.getLinkHtml(query.getKeywords().get(index).getUrl(), query.getKeywords().get(index).getValue()));
		}
		if (main) {
			writer.write("</div>\n");
		}
	}

	static String getScoreClass(ScoreArgs scoreArgs, Branch branch, Match match) {
		double bestOneScore;
		if (match.getBestOneScore() > -1) {
			bestOneScore = match.getBestOneScore();
		} else if (match.getWithoutPathScore() > -1) {
			bestOneScore = match.getWithoutPathScore();
		} else {
			bestOneScore = match.getScore();
		}
		double goodScore = 0;
		double badScore = 0;
		switch (branch) {
		case topic:
			goodScore = scoreArgs.getGoodScoreTopic();
			badScore = scoreArgs.getBadScoreTopic();
			break;
		case operation:
			goodScore = scoreArgs.getGoodScoreOperation();
			badScore = scoreArgs.getBadScoreOperation();
			break;
		case data:
			goodScore = scoreArgs.getGoodScoreData();
			badScore = scoreArgs.getBadScoreData();
			break;
		case format:
			goodScore = scoreArgs.getGoodScoreFormat();
			badScore = scoreArgs.getBadScoreFormat();
			break;
		}
		String scoreClass;
		if (bestOneScore > goodScore) scoreClass = "good";
		else if (bestOneScore < badScore) scoreClass = "bad";
		else scoreClass = "medium";
		return scoreClass;
	}

	private static void writeScore(ScoreArgs scoreArgs, Writer writer, Branch branch, Match match) throws IOException {
		writer.write("\t\t\t\t\t<div class=\"score " + getScoreClass(scoreArgs, branch, match) + "\">");
		if (match.getBestOneScore() > -1) {
			writer.write("<span class=\"best-one\">" + percent(match.getBestOneScore()) + "</span> ");
		}
		if (match.getWithoutPathScore() > -1) {
			writer.write("<span class=\"without-path\">" + percent(match.getWithoutPathScore()) + "</span> ");
		}
		writer.write("<span>" + percent(match.getScore()) + "</span></div>\n");
	}

	private static void writeMatches(ScoreArgs scoreArgs, Writer writer, Map<EdamUri, Concept> concepts, Query query, List<Publication> publications, MappingTest mapping) throws IOException {
		for (Branch branch : Branch.values()) {
			List<MatchTest> matches = mapping.getMatches(branch);
			if (matches.isEmpty()) continue;
			writer.write("\t\t<div class=\"branch " + branch + "\">\n");
			writer.write("\t\t\t<h3>" + branch + "</h3>\n");
			for (MatchTest matchTest : matches) {
				Match match = matchTest.getMatch();
				writer.write("\t\t\t<div class=\"match " + matchTest.getTest().name() + "\">\n");
				writer.write("\t\t\t\t<div class=\"match-div\">\n");
				writeConcept(writer, concepts, match);
				writeQueryMatch(writer, query, publications, match.getQueryMatch(), true);
				writer.write("\t\t\t\t\t<div class=\"type\">" + match.getConceptMatch().getType() + "</div>\n");
				writeScore(scoreArgs, writer, branch, match);
				writer.write("\t\t\t\t</div>\n");
				List<MatchAverageStats> matchAverageStats = match.getMatchAverageStats();
				if (matchAverageStats != null && !matchAverageStats.isEmpty()) {
					Concept concept = concepts.get(match.getEdamUri()); // TODO
					writer.write("\t\t\t\t<div class=\"details-div\">\n");
					writer.write("\t\t\t\t\t<div class=\"details\" tabindex=\"0\"></div>\n");
					writer.write("\t\t\t\t\t<div class=\"details-box\" tabindex=\"0\">\n");
					for (MatchAverageStats mas : matchAverageStats) {
						writer.write("\t\t\t\t\t\t<div><div>");
						writeQueryMatch(writer, query, publications, mas.getQueryMatch(), false);
						writer.write("<br>" + percent(mas.getQueryMatch().getScore()) + "</div><div>");
						ConceptMatchType conceptType = mas.getConceptMatch().getType();
						writer.write("<span>" + conceptType + "</span>");
						if (conceptType != ConceptMatchType.definition && conceptType != ConceptMatchType.comment && conceptType != ConceptMatchType.none) {
							writer.write("<br>" + conceptMatchString(concept, mas.getConceptMatch()));
						}
						writer.write("<br>" + percent(mas.getConceptMatch().getScore()) + "</div><div>");
						writer.write("<span>" + percent(mas.getScore()) + "</span></div></div>\n");
					}
					writer.write("\t\t\t\t\t</div>\n");
					writer.write("\t\t\t\t</div>\n");
				}
				writer.write("\t\t\t</div>\n");
			}
			writer.write("\t\t</div>\n");
		}
	}

	private static void writeArticle(CoreArgs args, QueryType type, Writer writer, Map<EdamUri, Concept> concepts, Query query, int queriesSize, List<Publication> publications, List<Webpage> webpages, List<Webpage> docs, MappingTest mapping, int page, int nr, int nrMin, int nrMax) throws IOException {
		FetcherArgs fetcherArgs = args.getFetcherArgs();

		writer.write("<article>\n");

		writer.write("\t<h2 id=\"" + nr + "\"><span>");
		if (queriesSize > 1) {
			writer.write("<span class=\"rank\">" + nr + ". </span>");
		}
		writer.write("<span>" + (query.getName() != null ? PubFetcher.escapeHtml(query.getName()) : "") + "</span>");
		if (query.getId() != null) {
			if (type == QueryType.biotools) {
				writer.write("<a href=\"" + PubFetcher.escapeHtmlAttribute(QueryLoader.BIOTOOLS + query.getId()) + "\" class=\"biotools-link\"></a>");
			} else {
				writer.write("<span> (" + PubFetcher.escapeHtml(query.getId()) + ")</span>");
			}
		}
		writer.write("</span><span>");
		String next = "";
		if (nr == nrMax && nr < queriesSize) {
			next = "index" + (page + 1) + ".html#" + (nr + 1);
		} else if (nr < nrMax) {
			next = "#" + (nr + 1);
		}
		if (next.isEmpty()) {
			writer.write("<span class=\"next\"></span>");
		} else {
			writer.write("<a href=\"" + next + "\" class=\"next\"></a>");
		}
		String previous = "";
		if (nr == nrMin && nr > 1) {
			previous = "index" + (page - 1 == 1 ? "" : page - 1) + ".html#" + (nr - 1);
		} else if (nr > nrMin) {
			previous = "#" + (nr - 1);
		}
		if (previous.isEmpty()) {
			writer.write("<span class=\"previous\"></span>");
		} else {
			writer.write("<a href=\"" + previous + "\" class=\"previous\"></a>");
		}
		writer.write("<a href=\"#" + nr + "\" class=\"current\"></a>");
		writer.write("</span></h2>\n");

		boolean webpagesPresent = false;
		if (query.getWebpageUrls() != null) {
			for (Link link : query.getWebpageUrls()) {
				if (link != null && link.getUrl() != null && !link.getUrl().isEmpty()) {
					webpagesPresent = true;
					break;
				}
			}
		}
		boolean docsPresent = false;
		if (query.getDocUrls() != null) {
			for (Link link : query.getDocUrls()) {
				if (link != null && link.getUrl() != null && !link.getUrl().isEmpty()) {
					docsPresent = true;
					break;
				}
			}
		}
		boolean miscPresent = (query.getKeywords() != null && !query.getKeywords().isEmpty())
			|| (query.getDescription() != null && !query.getDescription().isEmpty())
			|| webpagesPresent || docsPresent;
		boolean publicationsPresent = false;
		if (query.getPublicationIds() != null) {
			for (PublicationIdsQuery publicationIds : query.getPublicationIds()) {
				if (publicationIds != null && !publicationIds.isEmpty()) {
					publicationsPresent = true;
					break;
				}
			}
		}

		if (miscPresent || publicationsPresent) {
			if (publicationsPresent) {
				writer.write("\t<section class=\"query\">\n");
			} else {
				writer.write("\t<section class=\"query query-no-publications\">\n");
			}

			if (miscPresent) {
				writer.write("\t\t<section class=\"misc\">\n");

				if (query.getKeywords() != null && !query.getKeywords().isEmpty()) {
					Map<String, List<Keyword>> keywords = new LinkedHashMap<>();
					for (Keyword keyword : query.getKeywords()) {
						if (keywords.get(keyword.getType()) == null) {
							keywords.put(keyword.getType(), new ArrayList<>());
						}
						keywords.get(keyword.getType()).add(keyword);
					}
					for (Map.Entry<String, List<Keyword>> entry : keywords.entrySet()) {
						writer.write("\t\t\t<div class=\"generic\">\n");
						writer.write("\t\t\t\t<h3>" + PubFetcher.escapeHtml(entry.getKey()) + "</h3><br>\n");
						writer.write("\t\t\t\t<div>");
						writer.write(entry.getValue().stream()
							.map(k -> PubFetcher.getLinkHtml(k.getUrl(), k.getValue()))
							.collect(Collectors.joining("; "))
						);
						writer.write("</div>\n");
						writer.write("\t\t\t</div>\n");
					}
				}

				if (query.getDescription() != null && !query.getDescription().isEmpty()) {
					writer.write("\t\t\t<div class=\"generic\">\n");
					writer.write("\t\t\t\t<h3>Description</h3><br>\n");
					writer.write("\t\t\t\t<div>" + PubFetcher.getParagraphsHtml(query.getDescription()) + "</div>\n");
					writer.write("\t\t\t</div>\n");
				}

				if (webpagesPresent) {
					writer.write("\t\t\t<div class=\"links\">\n");
					writer.write("\t\t\t\t<h3>Links</h3><br>\n");
					writer.write("\t\t\t\t<div>\n");
					writeLinks(fetcherArgs, writer, query.getWebpageUrls(), webpages);
					writer.write("\t\t\t\t</div>\n");
					writer.write("\t\t\t</div>\n");
				}

				if (docsPresent) {
					writer.write("\t\t\t<div class=\"links\">\n");
					writer.write("\t\t\t\t<h3>Documentation</h3><br>\n");
					writer.write("\t\t\t\t<div>\n");
					writeLinks(fetcherArgs, writer, query.getDocUrls(), docs);
					writer.write("\t\t\t\t</div>\n");
					writer.write("\t\t\t</div>\n");
				}

				writer.write("\t\t</section>\n");
			}

			if (publicationsPresent) {
				writer.write("\t\t<section class=\"publications\">\n");
				writePublications(fetcherArgs, writer, query.getPublicationIds(), publications);
				writer.write("\t\t</section>\n");
			}

			writer.write("\t</section>\n");
		}

		writer.write("\t<section class=\"mapping\">\n");
		writeMatches(args.getMapperArgs().getScoreArgs(), writer, concepts, query, publications, mapping);
		writer.write("\t</section>\n");

		writer.write("</article>\n\n");
	}

	private static void writeParams(CoreArgs args, List<ParamMain> paramsMain, QueryType type, Writer writer, Map<EdamUri, Concept> concepts, List<Query> queries, Results results) throws IOException {
		Params.writeMain(paramsMain, writer);
		Params.writeProcessing(args.getProcessorArgs(), writer);
		Params.writePreProcessing(args.getPreProcessorArgs(), writer, false);
		Params.writeFetching(args.getFetcherArgs(), writer, type != QueryType.server, false);
		Params.writeMapping(args.getMapperArgs(), writer, false);
		Params.writeBenchmarking(writer, concepts, queries, results);
	}

	private static void writePagination(CoreArgs args, int reportPageSize, int reportPaginationSize, Writer writer, int queriesSize, int page) throws IOException {
		if (queriesSize <= reportPageSize || reportPageSize == 0) return;

		int pageMax = (queriesSize - 1) / reportPageSize + 1;
		int paginationSize = reportPaginationSize;
		if (paginationSize > pageMax) paginationSize = pageMax;

		writer.write("<ul class=\"pagination\">\n");

		writer.write("\t<li><a ");
		if (page == 1) writer.write("class=\"disabled-page\"");
		else writer.write("href=\"index.html\"");
		writer.write(">«</a></li>\n");
		writer.write("\t<li><a ");
		if (page == 1) writer.write("class=\"disabled-page\"");
		else writer.write("href=\"index" + (page - 1 == 1 ? "" : page - 1) + ".html\"");
		writer.write(">‹</a></li>");

		int first = page - (paginationSize - 1) / 2;
		int last = page + paginationSize / 2;
		if (first < 1) {
			last += 1 - first;
			first = 1;
		}
		if (last > pageMax) {
			first -= last - pageMax;
			last = pageMax;
		}
		if (first < 1) first = 1;
		for (int i = first; i <= last; ++i) {
			writer.write("\t<li><a ");
			if (i == page) writer.write("class=\"current-page\"");
			else writer.write("href=\"index" + (i == 1 ? "" : i) + ".html\"");
			writer.write(">" + i + "</a></li>");
		}

		writer.write("\t<li><a ");
		if (page == pageMax) writer.write("class=\"disabled-page\"");
		else writer.write("href=\"index" + (page + 1) + ".html\"");
		writer.write(">›</a></li>\n");
		writer.write("\t<li><a ");
		if (page == pageMax) writer.write("class=\"disabled-page\"");
		else writer.write("href=\"index" + pageMax + ".html\"");
		writer.write(">»</a></li>");

		writer.write("</ul>\n\n");
	}

	private static void out(CoreArgs args, List<ParamMain> paramsMain, QueryType type, int reportPageSize, int reportPaginationSize, Writer writer, Map<EdamUri, Concept> concepts, List<Query> queries, List<List<Publication>> publications, List<List<Webpage>> webpages, List<List<Webpage>> docs, Results results, long start, long stop, Version version, int page, boolean txt, boolean json) throws IOException {
		writer.write("<!DOCTYPE html>\n");
		writer.write("<html lang=\"en\">\n\n");

		writer.write("<head>\n");
		writer.write("\t<meta charset=\"utf-8\">\n");
		writer.write("\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
		writer.write("\t<meta name=\"generator\" content=\"" + version.getName() + " " + version.getVersion() + "\">\n");
		writer.write("\t<title>" + version.getName() + " " + version.getVersion() + " Report</title>\n");
		if (type != QueryType.server) {
			writer.write("\t<link rel=\"stylesheet\" href=\"edammap-" + version.getVersion() + ".css\">\n");
		} else {
			writer.write("\t<link rel=\"stylesheet\" href=\"../../edammap-" + version.getVersion() + ".css\">\n");
		}
		writer.write("</head>\n\n");

		writer.write("<body>\n\n");

		writer.write("<header>\n\n");

		int resultMin = (page - 1) * reportPageSize + 1;
		int resultMax = page * reportPageSize;
		if (resultMax > queries.size()) resultMax = queries.size();
		writer.write("<h1>" + version.getName() + " Results");
		if (queries.size() > 1) {
			writer.write(" <span>(");
			if (queries.size() > reportPageSize && reportPageSize > 0) {
				writer.write(resultMin + "–" + resultMax + " ∕ ");
			}
			writer.write(queries.size() + ")</span>");
		}
		writer.write("</h1>\n\n");

		String startInstant = Instant.ofEpochMilli(start).toString();
		writer.write("<p>Generated by " + PubFetcher.getLinkHtml(version.getUrl(), version.getName()) + " " + version.getVersion()
			+ " in " + ((stop - start) / 1000.0) + " seconds <span>(start <time datetime=\"" + startInstant + "\">" + startInstant + "</time>)</span></p>\n\n");
		if (type == QueryType.server) {
			if (txt) {
				writer.write("<p>Results as <a href=\"results.txt\">plain text</a></p>\n\n");
			}
			if (json) {
				writer.write("<p>Results as <a href=\"results.json\">JSON</a></p>\n\n");
			}
		}

		writePagination(args, reportPageSize, reportPaginationSize, writer, queries.size(), page);

		writer.write("</header>\n\n");

		writer.write("<main>\n\n");

		for (int i = resultMin; i <= resultMax; ++i) {
			writeArticle(args, type, writer, concepts, queries.get(i - 1), queries.size(), publications.get(i - 1), webpages.get(i - 1), docs.get(i - 1), results.getMappings().get(i - 1), page, i, resultMin, resultMax);
		}

		writer.write("</main>\n\n");

		writer.write("<footer>\n\n");

		writer.write("<h2>Parameters</h2>\n\n");

		writer.write("<section id=\"tabs\">\n");
		writer.write("\n");
		writeParams(args, paramsMain, type, writer, concepts, queries, results);
		writer.write("</section>\n\n");

		writePagination(args, reportPageSize, reportPaginationSize, writer, queries.size(), page);

		writer.write("</footer>\n\n");

		writer.write("</body>\n\n");

		writer.write("</html>\n");
	}

	public static void copyHtmlResources(Path path, Version version) throws IOException {
		Files.copy(Report.class.getResourceAsStream("/html/style.css"), path.resolve("edammap-" + version.getVersion() + ".css"));
	}

	public static void copyFontResources(Path path) throws IOException {
		for (String resource : FONT_RESOURCES) {
			Files.copy(Report.class.getResourceAsStream("/html/" + resource), path.resolve(resource));
		}
	}

	static void output(CoreArgs args, List<ParamMain> paramsMain, QueryType type, int reportPageSize, int reportPaginationSize, Path report, boolean existingDirectory, Map<EdamUri, Concept> concepts, List<Query> queries, List<List<Publication>> publications, List<List<Webpage>> webpages, List<List<Webpage>> docs, Results results, long start, long stop, Version version, boolean txt, boolean json) throws IOException {
		if (report != null) {
			if (!existingDirectory) {
				Files.createDirectory(report);
			}
			if (type != QueryType.server) {
				copyHtmlResources(report, version);
				copyFontResources(report);
			}
			int pageMax = (queries.size() - 1) / reportPageSize + 1;
			for (int page = 1; page <= pageMax; ++page) {
				try (BufferedWriter writer = Files.newBufferedWriter(report.resolve("index" + (page == 1 ? "" : page) + ".html"), StandardCharsets.UTF_8)) {
					out(args, paramsMain, type, reportPageSize, reportPaginationSize, writer, concepts, queries, publications, webpages, docs, results, start, stop, version, page, txt, json);
				} catch (IOException e) {
					try {
						Txt.out(type, System.out, concepts, queries, results.getMappings());
					} catch (Exception e2) {
						throw e;
					}
					throw e;
				}
			}
		}
	}
}
