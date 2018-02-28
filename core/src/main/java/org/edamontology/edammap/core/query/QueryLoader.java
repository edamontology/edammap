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

package org.edamontology.edammap.core.query;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.edamontology.edammap.core.edam.Branch;
import org.edamontology.edammap.core.edam.Concept;
import org.edamontology.edammap.core.edam.EdamUri;
import org.edamontology.edammap.core.input.Csv;
import org.edamontology.edammap.core.input.InputType;
import org.edamontology.edammap.core.input.Json;
import org.edamontology.edammap.core.input.Xml;
import org.edamontology.edammap.core.input.csv.BioConductor;
import org.edamontology.edammap.core.input.csv.Generic;
import org.edamontology.edammap.core.input.csv.Msutils;
import org.edamontology.edammap.core.input.csv.SEQwiki;
import org.edamontology.edammap.core.input.json.Edam;
import org.edamontology.edammap.core.input.json.Function;
import org.edamontology.edammap.core.input.json.InputOutput;
import org.edamontology.edammap.core.input.json.Publication;
import org.edamontology.edammap.core.input.json.ToolInput;
import org.edamontology.edammap.core.input.xml.Biotools14;
import org.edamontology.pubfetcher.FetcherArgs;
import org.edamontology.pubfetcher.FetcherCommon;

public class QueryLoader {

	private static final String EDAM_PREFIX = "http://edamontology.org";
	private static final String GENERIC = "https://localhost/";
	private static final String SEQWIKI = "http://seqanswers.com/wiki/";
	private static final String MSUTILS = "http://www.ms-utils.org/";
	private static final String BIOTOOLS = "https://bio.tools/";
	private static final String BIOC_VIEWS = "https://bioconductor.org/packages/release/BiocViews.html#___";

	private static final Pattern INTERNAL_SEPARATOR_BAR = Pattern.compile("\\|");
	private static final Pattern INTERNAL_SEPARATOR_COMMA = Pattern.compile(",");

	private static List<Link> splitLink(String toSplit, Pattern pattern) {
		if (toSplit == null || toSplit.trim().isEmpty()) return Collections.emptyList();
		return pattern.splitAsStream(toSplit)
			.map(s -> s.trim())
			.map(s -> new Link(s, null))
			.collect(Collectors.toList());
	}

	private static PublicationIdsQuery onePublicationId(String publicationId, String url, String type) {
		if (publicationId == null || publicationId.trim().isEmpty()) return null;
		PublicationIdsQuery publicationIds =
			FetcherCommon.isPmid(publicationId) ? new PublicationIdsQuery(publicationId, null, null, url, null, null, type) : (
			FetcherCommon.isPmcid(publicationId) ? new PublicationIdsQuery(null, publicationId, null, null, url, null, type) : (
			FetcherCommon.isDoi(publicationId) ? new PublicationIdsQuery(null, null, publicationId, null, null, url, type) : (
			null)));
		if (publicationIds == null) {
			System.err.println("Unknown publication ID: " + publicationId);
		}
		return publicationIds;
	}

	private static List<PublicationIdsQuery> splitPublicationIds(String toSplit, Pattern pattern, String url) {
		if (toSplit == null || toSplit.trim().isEmpty()) return Collections.emptyList();
		return pattern.splitAsStream(toSplit)
			.map(String::trim)
			.map(s -> onePublicationId(s, url, null))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	private static List<Keyword> keywords(String keywords, String type, String url, Pattern separator) {
		if (keywords == null) return Collections.emptyList();
		return separator.splitAsStream(keywords)
			.map(s -> new Keyword(type, s.trim(), url + s.trim().replaceAll(" ", "_")))
			.collect(Collectors.toList());
	}

	private static List<Keyword> keywordsCamelCase(String keywords, String type, String url, Pattern separator) {
		if (keywords == null) return Collections.emptyList();
		return separator.splitAsStream(keywords)
			.map(s -> s.replaceAll("(?<=[^A-Z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|(?<=[A-Za-z])(?=[^A-Za-z])", " "))
			.map(s -> new Keyword(type, s.trim(), url + s.trim().replaceAll(" ", "")))
			.collect(Collectors.toList());
	}

	private static boolean checkEdamUri(EdamUri edamUri, Map<EdamUri, Concept> concepts) {
		if (concepts != null && concepts.get(edamUri) == null) {
			throw new IllegalArgumentException("Non-existent EDAM URI: " + edamUri);
		}
		return true;
	}

	private static List<EdamUri> edamUris(String annotations, Map<EdamUri, Concept> concepts) {
		if (annotations == null) return Collections.emptyList();
		return INTERNAL_SEPARATOR_BAR.splitAsStream(annotations)
			.filter(s -> !s.trim().isEmpty() && !s.trim().equalsIgnoreCase("NA"))
			.map(s -> (s.contains("/") ? s.trim() : EDAM_PREFIX + "/" + s.trim()))
			.map(s -> new EdamUri(s.toLowerCase(Locale.ROOT), EDAM_PREFIX))
			.filter(e -> checkEdamUri(e, concepts))
			.collect(Collectors.toList());
	}

	private static List<EdamUri> edamUris(List<String> annotations, Map<EdamUri, Concept> concepts) {
		if (annotations == null) return Collections.emptyList();
		return annotations.stream()
			.map(s -> new EdamUri(s.trim().toLowerCase(Locale.ROOT), EDAM_PREFIX))
			.filter(e -> checkEdamUri(e, concepts))
			.collect(Collectors.toList());
	}

	private static List<EdamUri> edamUrisJson(List<Edam> annotations, Map<EdamUri, Concept> concepts) {
		if (annotations == null) return Collections.emptyList();
		return annotations.stream()
			.map(e -> getEdamUri(e, concepts))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	private static EdamUri getEdamUri(Edam edam, Map<EdamUri, Concept> concepts) {
		if (edam.getUri() != null && !edam.getUri().isEmpty()) {
			EdamUri edamUri = new EdamUri(edam.getUri().trim().toLowerCase(Locale.ROOT), EDAM_PREFIX);
			if (!checkEdamUri(edamUri, concepts)) return null;
			return edamUri;
		}
		if (edam.getTerm() != null && !edam.getTerm().isEmpty() && concepts != null) {
			for (Entry<EdamUri, Concept> e : concepts.entrySet()) {
				if (e.getValue().getLabel().equals(edam.getTerm())) return e.getKey();
			}
			for (Entry<EdamUri, Concept> e : concepts.entrySet()) {
				if (e.getValue().getExactSynonyms().contains(edam.getTerm())) return e.getKey();
			}
			for (Entry<EdamUri, Concept> e : concepts.entrySet()) {
				if (e.getValue().getNarrowSynonyms().contains(edam.getTerm())) return e.getKey();
				if (e.getValue().getBroadSynonyms().contains(edam.getTerm())) return e.getKey();
			}
		}
		return null;
	}

	private static Query getGeneric(Generic generic, Map<EdamUri, Concept> concepts) {
		List<Keyword> keywords = new ArrayList<>();
		keywords.addAll(keywords(generic.getKeywords(), "Keywords", null, INTERNAL_SEPARATOR_BAR));

		Set<EdamUri> annotations = new LinkedHashSet<>();
		annotations.addAll(edamUris(generic.getAnnotations(), concepts));

		return new Query(
			generic.getName().trim(),
			null,
			splitLink(generic.getWebpageUrls(), INTERNAL_SEPARATOR_BAR),
			generic.getDescription() != null ? generic.getDescription().trim() : null,
			keywords,
			splitPublicationIds(generic.getPublicationIds(), INTERNAL_SEPARATOR_BAR, GENERIC),
			splitLink(generic.getDocUrls(), INTERNAL_SEPARATOR_BAR),
			annotations);
	}

	private static EdamUri getSEQwikiAnnotation(Keyword keyword, Map<EdamUri, Concept> concepts) {
		if (concepts == null) return null;
		for (Map.Entry<EdamUri, Concept> concept : concepts.entrySet()) {
			if (((keyword.getType().equals("Domain") && concept.getKey().getBranch() == Branch.topic)
				|| (keyword.getType().equals("Method") && concept.getKey().getBranch() == Branch.operation))
				&& keyword.getValue().equalsIgnoreCase(concept.getValue().getLabel())) {
				return concept.getKey();
			}
		}
		return null;
	}

	private static Query getSEQwiki(SEQwiki SEQwiki, Map<EdamUri, Concept> concepts) {
		List<Link> webpageUrls = new ArrayList<>();
		webpageUrls.add(new Link(SEQWIKI + SEQwiki.getName().trim().replace(" ", "_"), null));
		webpageUrls.addAll(splitLink(SEQwiki.getWebpages(), INTERNAL_SEPARATOR_COMMA));

		List<Keyword> keywords = new ArrayList<>();
		keywords.addAll(keywords(SEQwiki.getDomains(), "Domain", SEQWIKI, INTERNAL_SEPARATOR_COMMA));
		keywords.addAll(keywords(SEQwiki.getMethods(), "Method", SEQWIKI, INTERNAL_SEPARATOR_COMMA));

		Set<EdamUri> annotations = new LinkedHashSet<>();
		for (Keyword keyword : keywords) {
			EdamUri annotation = getSEQwikiAnnotation(keyword, concepts);
			if (annotation != null) annotations.add(annotation);
		}

		return new Query(SEQwiki.getName().trim(), null, webpageUrls, SEQwiki.getSummary().trim(), keywords,
			splitPublicationIds(SEQwiki.getPublications(), INTERNAL_SEPARATOR_COMMA, SEQWIKI),
			splitLink(SEQwiki.getDocs(), INTERNAL_SEPARATOR_COMMA),
			annotations);
	}

	private static List<Query> getSEQwikiTags(SEQwiki SEQwiki, Map<EdamUri, Concept> concepts) {
		List<Query> queries = new ArrayList<>();

		queries.addAll(INTERNAL_SEPARATOR_COMMA.splitAsStream(SEQwiki.getDomains())
			.map(s -> {
				List<Keyword> keywords = new ArrayList<>();
				Keyword keyword = new Keyword("Domain", s.trim(), SEQWIKI);
				keywords.add(keyword);
				Set<EdamUri> annotations = new LinkedHashSet<>();
				EdamUri annotation = getSEQwikiAnnotation(keyword, concepts);
				if (annotation != null) annotations.add(annotation);
				return new Query(null, null, null, null, keywords, null, null, annotations);
			}).collect(Collectors.toList()));
		queries.addAll(INTERNAL_SEPARATOR_COMMA.splitAsStream(SEQwiki.getMethods())
			.map(s -> {
				List<Keyword> keywords = new ArrayList<>();
				Keyword keyword = new Keyword("Method", s.trim(), SEQWIKI);
				keywords.add(keyword);
				Set<EdamUri> annotations = new LinkedHashSet<>();
				EdamUri annotation = getSEQwikiAnnotation(keyword, concepts);
				if (annotation != null) annotations.add(annotation);
				return new Query(null, null, null, null, keywords, null, null, annotations);
			}).collect(Collectors.toList()));

		return queries;
	}

	private static List<Query> getSEQwikiTool(SEQwiki SEQwiki, Map<EdamUri, Concept> concepts) {
		List<Query> queries = new ArrayList<>();

		List<Link> webpageUrls = new ArrayList<>();
		webpageUrls.add(new Link(SEQWIKI + SEQwiki.getName().trim().replace(" ", "_"), null));

		queries.addAll(INTERNAL_SEPARATOR_COMMA.splitAsStream(SEQwiki.getDomains())
			.map(s -> {
				List<Keyword> keywords = new ArrayList<>();
				Keyword keyword = new Keyword("Domain", s.trim(), SEQWIKI);
				keywords.add(keyword);
				Set<EdamUri> annotations = new LinkedHashSet<>();
				EdamUri annotation = getSEQwikiAnnotation(keyword, concepts);
				if (annotation != null) annotations.add(annotation);
				return new Query(SEQwiki.getName().trim(), null, webpageUrls, null, keywords, null, null, annotations);
			}).collect(Collectors.toList()));
		queries.addAll(INTERNAL_SEPARATOR_COMMA.splitAsStream(SEQwiki.getMethods())
			.map(s -> {
				List<Keyword> keywords = new ArrayList<>();
				Keyword keyword = new Keyword("Method", s.trim(), SEQWIKI);
				keywords.add(keyword);
				Set<EdamUri> annotations = new LinkedHashSet<>();
				EdamUri annotation = getSEQwikiAnnotation(keyword, concepts);
				if (annotation != null) annotations.add(annotation);
				return new Query(SEQwiki.getName().trim(), null, webpageUrls, null, keywords, null, null, annotations);
			}).collect(Collectors.toList()));

		return queries;
	}

	private static Query getMsutils(Msutils msutils, Map<EdamUri, Concept> concepts) {
		List<Link> webpageUrls = new ArrayList<>();
		if (msutils.getWeblink() != null && !msutils.getWeblink().trim().isEmpty()) {
			webpageUrls.add(new Link(msutils.getWeblink().trim(), "Weblink"));
		}
		if (msutils.getLink() != null && !msutils.getLink().trim().isEmpty()) {
			webpageUrls.add(new Link(msutils.getLink().trim(), "Link"));
		}

		Set<EdamUri> annotations = new LinkedHashSet<>();
		annotations.addAll(edamUris(msutils.getTopic(), concepts));
		annotations.addAll(edamUris(msutils.getOperation(), concepts));
		annotations.addAll(edamUris(msutils.getFormat_in(), concepts));
		annotations.addAll(edamUris(msutils.getFormat_out(), concepts));

		return new Query(msutils.getName().trim(), null, webpageUrls, msutils.getDescription().trim(), null,
			splitPublicationIds(msutils.getPaper(), INTERNAL_SEPARATOR_BAR, MSUTILS), null, annotations);
	}

	private static Query getBiotools14(Biotools14 biotools, Map<EdamUri, Concept> concepts) {
		List<Link> webpageUrls = new ArrayList<>();
		webpageUrls.add(new Link(biotools.getHomepage().trim(), "Homepage"));
		webpageUrls.addAll(biotools.getMirrors().stream().map(s -> new Link(s.trim(), "Mirror")).collect(Collectors.toList()));

		List<PublicationIdsQuery> publicationIds = new ArrayList<>();
		if (biotools.getPublicationsPrimaryID() != null) {
			publicationIds.add(onePublicationId(biotools.getPublicationsPrimaryID().trim(), BIOTOOLS, "Primary"));
		}
		publicationIds.addAll(biotools.getPublicationsOtherIDs().stream().map(s -> onePublicationId(s.trim(), BIOTOOLS, "Other")).collect(Collectors.toList()));

		List<Link> docUrls = new ArrayList<>();
		if (biotools.getDocsHome() != null) {
			docUrls.add(new Link(biotools.getDocsHome().trim(), "Home"));
		}
		if (biotools.getDocsGithub() != null) {
			docUrls.add(new Link(biotools.getDocsGithub().trim(), "Github"));
		}

		Set<EdamUri> annotations = new LinkedHashSet<>();
		annotations.addAll(edamUris(biotools.getTopics(), concepts));
		annotations.addAll(edamUris(biotools.getFunctionNames(), concepts));
		annotations.addAll(edamUris(biotools.getDataTypes(), concepts));
		annotations.addAll(edamUris(biotools.getDataFormats(), concepts));

		return new Query(biotools.getName().trim(), null, webpageUrls, biotools.getDescription().trim(), null,
			publicationIds, docUrls, annotations);
	}

	private static Query getBiotools(ToolInput tool, Map<EdamUri, Concept> concepts) {
		List<Link> webpageUrls = new ArrayList<>();
		webpageUrls.add(new Link(BIOTOOLS + tool.getId().trim(), "ID"));
		webpageUrls.add(new Link(tool.getHomepage().trim(), "Homepage"));
		webpageUrls.addAll(tool.getLink().stream()
			.filter(s -> s.getType().trim().equalsIgnoreCase("Browser")
				|| s.getType().trim().equalsIgnoreCase("Mirror")
				|| s.getType().trim().equalsIgnoreCase("Repository"))
			.map(s -> new Link(s.getUrl().trim(), s.getType().trim())).collect(Collectors.toList()));

		List<PublicationIdsQuery> publicationIds = new ArrayList<>();
		for (Publication publication : tool.getPublication()) {
			String pmid = publication.getPmid();
			if (pmid == null || pmid.trim().isEmpty()) pmid = null;
			else if (!FetcherCommon.isPmid(pmid)) {
				System.err.println("Unknown PMID: " + pmid);
				pmid = null;
			}

			String pmcid = publication.getPmcid();
			if (pmcid == null || pmcid.trim().isEmpty()) pmcid = null;
			else if (!FetcherCommon.isPmcid(pmcid)) {
				System.err.println("Unknown PMCID: " + pmcid);
				pmcid = null;
			}

			String doi = publication.getDoi();
			if (doi == null || doi.trim().isEmpty()) doi = null;
			else if (!FetcherCommon.isDoi(doi)) {
				System.err.println("Unknown DOI: " + doi);
				doi = null;
			}

			String type = publication.getType() != null ? publication.getType().trim() : null;
			publicationIds.add(new PublicationIdsQuery(pmid, pmcid, doi,
				pmid == null ? null : BIOTOOLS, pmcid == null ? null : BIOTOOLS, doi == null ? null : BIOTOOLS, type));
		}

		List<Link> docUrls = tool.getDocumentation().stream()
			.filter(s -> s.getType().trim().equalsIgnoreCase("API documentation")
				|| s.getType().trim().equalsIgnoreCase("General")
				|| s.getType().trim().equalsIgnoreCase("Manual")
				|| s.getType().trim().equalsIgnoreCase("Training material"))
			.map(s -> new Link(s.getUrl().trim(), s.getType().trim())).collect(Collectors.toList());

		Set<EdamUri> annotations = new LinkedHashSet<>();
		annotations.addAll(edamUrisJson(tool.getTopic(), concepts));
		for (Function function : tool.getFunction()) {
			annotations.addAll(edamUrisJson(function.getOperation(), concepts));
			for (InputOutput input : function.getInput()) {
				annotations.addAll(edamUrisJson(Collections.singletonList(input.getData()), concepts));
				if (input.getFormat() != null) {
					annotations.addAll(edamUrisJson(input.getFormat(), concepts));
				}
			}
			for (InputOutput output : function.getOutput()) {
				annotations.addAll(edamUrisJson(Collections.singletonList(output.getData()), concepts));
				if (output.getFormat() != null) {
					annotations.addAll(edamUrisJson(output.getFormat(), concepts));
				}
			}
		}

		return new Query(tool.getName().trim(), tool.getId().trim(), webpageUrls, tool.getDescription().trim(), null,
			publicationIds, docUrls, annotations);
	}

	private static Query getBioConductor(BioConductor bioConductor, Map<EdamUri, Concept> concepts) {
		String name = bioConductor.getName().trim() + " : " + bioConductor.getTitle().trim().replaceAll("\n", " ");

		List<Link> webpageUrls = null;
		if (bioConductor.getReposFullUrl() != null && !bioConductor.getReposFullUrl().trim().isEmpty()) {
			webpageUrls = new ArrayList<>();
			webpageUrls.add(new Link(bioConductor.getReposFullUrl().trim(), null));
		}

		List<Link> docUrls = null;
		if (bioConductor.getReposFullUrl() != null && !bioConductor.getReposFullUrl().trim().isEmpty()) {
			docUrls = new ArrayList<>();
			docUrls.add(new Link(bioConductor.getReposFullUrl().replaceFirst("/bioc/html/(.+)\\.html", "/bioc/manuals/$1/man/$1.pdf").trim(), null));
			docUrls.add(new Link(bioConductor.getReposFullUrl().replaceFirst("/bioc/html/(.+)\\.html", "/bioc/vignettes/$1/inst/doc/$1.pdf").trim(), null));
			docUrls.add(new Link(bioConductor.getReposFullUrl().replaceFirst("/bioc/html/(.+)\\.html", "/bioc/vignettes/$1/inst/doc/$1.html").trim(), null));
			docUrls.add(new Link(bioConductor.getReposFullUrl().replaceFirst("/bioc/html/(.+)\\.html", "/bioc/vignettes/$1/inst/doc/$1-vignette.pdf").trim(), null));
		}

		Set<EdamUri> annotations = new LinkedHashSet<>();
		annotations.addAll(edamUris(bioConductor.getTopic_URI(), concepts));
		annotations.addAll(edamUris(bioConductor.getOperation_URI(), concepts));

		return new Query(
			name,
			null,
			webpageUrls,
			bioConductor.getDescription().trim(),
			keywordsCamelCase(bioConductor.getBiocViews(), "biocViews", BIOC_VIEWS, INTERNAL_SEPARATOR_BAR),
			null,
			docUrls,
			annotations);
	}

	public static List<Query> get(String queryPath, QueryType type, Map<EdamUri, Concept> concepts, FetcherArgs fetcherArgs) throws IOException, ParseException {
		List<? extends InputType> inputs;
		if (type == QueryType.biotools) {
			inputs = Json.load(queryPath, type, fetcherArgs);
		} else if (type == QueryType.biotools14) {
			inputs = Xml.load(queryPath, type, fetcherArgs);
		} else {
			inputs = Csv.load(queryPath, type, fetcherArgs);
		}

		Set<Query> queries = new LinkedHashSet<>();

		for (InputType input : inputs) {
			switch (type) {
				case generic: queries.add(getGeneric((Generic) input, concepts)); break;
				case SEQwiki: queries.add(getSEQwiki((SEQwiki) input, concepts)); break;
				case SEQwikiTags: queries.addAll(getSEQwikiTags((SEQwiki) input, concepts)); break;
				case SEQwikiTool: queries.addAll(getSEQwikiTool((SEQwiki) input, concepts)); break;
				case msutils: queries.add(getMsutils((Msutils) input, concepts)); break;
				case biotools14: queries.add(getBiotools14((Biotools14) input, concepts)); break;
				case biotools: queries.add(getBiotools((ToolInput) input, concepts)); break;
				case BioConductor: queries.add(getBioConductor((BioConductor) input, concepts)); break;
			}
		}

		return new ArrayList<>(queries);
	}


	public static List<Query> get(String queryPath, QueryType type, FetcherArgs fetcherArgs) throws IOException, ParseException {
		return get(queryPath, type, null, fetcherArgs);
	}
}
