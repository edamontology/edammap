package edammapper.query;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import edammapper.edam.EdamUri;
import edammapper.input.Csv;
import edammapper.input.Input;
import edammapper.input.Xml;
import edammapper.input.csv.BioConductor;
import edammapper.input.csv.Generic;
import edammapper.input.csv.Msutils;
import edammapper.input.csv.SEQwiki;
import edammapper.input.xml.Biotools;

public class QueryLoader {

	private static final String EDAM_PREFIX = "http://edamontology.org";
	private static final String SEQWIKI = "http://seqanswers.com/wiki/";
	private static final String BIOC_VIEWS = "http://bioconductor.org/packages/release/BiocViews.html#___";

	private static final Pattern INTERNAL_SEPARATOR_BAR = Pattern.compile("\\|");
	private static final Pattern INTERNAL_SEPARATOR_COMMA = Pattern.compile(",");

	private static List<String> split(String toSplit) {
		if (toSplit == null) return Collections.emptyList();
		return INTERNAL_SEPARATOR_BAR.splitAsStream(toSplit)
			.map(s -> s.trim())
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

	private static List<EdamUri> edamUris(String annotations) {
		if (annotations == null) return Collections.emptyList();
		return INTERNAL_SEPARATOR_BAR.splitAsStream(annotations)
			.filter(s -> !s.trim().isEmpty() && !s.trim().equalsIgnoreCase("NA"))
			.map(s -> (s.contains("/") ? s.trim() : EDAM_PREFIX + "/" + s.trim()))
			.map(s -> new EdamUri(s.toLowerCase(Locale.ROOT), EDAM_PREFIX))
			.collect(Collectors.toList());
	}

	private static List<EdamUri> edamUris(List<String> annotations) {
		if (annotations == null) return Collections.emptyList();
		return annotations.stream()
			.map(s -> new EdamUri(s.trim().toLowerCase(Locale.ROOT), EDAM_PREFIX))
			.collect(Collectors.toList());
	}

	private static Query getGeneric(Generic generic) {
		List<Keyword> keywords = new ArrayList<>();
		keywords.addAll(keywords(generic.getKeywords(), "Keywords", null, INTERNAL_SEPARATOR_BAR));

		Set<EdamUri> annotations = new LinkedHashSet<>();
		annotations.addAll(edamUris(generic.getAnnotations()));

		return new Query(
			generic.getName().trim(),
			split(generic.getWebpageUrls()),
			generic.getDescription() != null ? generic.getDescription().trim() : null,
			keywords,
			split(generic.getPublicationIds()),
			split(generic.getDocUrls()),
			annotations);
	}

	private static Query getSEQwiki(SEQwiki SEQwiki) {
		List<String> webpageUrls = new ArrayList<>();
		webpageUrls.add(SEQWIKI + SEQwiki.getName().trim().replace(" ", "_"));

		List<Keyword> keywords = new ArrayList<>();
		keywords.addAll(keywords(SEQwiki.getDomains(), "Domain", SEQWIKI, INTERNAL_SEPARATOR_COMMA));
		keywords.addAll(keywords(SEQwiki.getMethods(), "Method", SEQWIKI, INTERNAL_SEPARATOR_COMMA));

		return new Query(SEQwiki.getName().trim(), webpageUrls, SEQwiki.getSummary().trim(), keywords, null, null, null);
	}

	private static List<Query> getSEQwikiTags(SEQwiki SEQwiki) {
		List<Query> queries = new ArrayList<>();

		queries.addAll(INTERNAL_SEPARATOR_COMMA.splitAsStream(SEQwiki.getDomains())
			.map(s -> {
				List<Keyword> keywords = new ArrayList<>();
				keywords.add(new Keyword("Domain", s.trim(), SEQWIKI));
				return new Query(null, null, null, keywords, null, null, null);
			}).collect(Collectors.toList()));
		queries.addAll(INTERNAL_SEPARATOR_COMMA.splitAsStream(SEQwiki.getMethods())
			.map(s -> {
				List<Keyword> keywords = new ArrayList<>();
				keywords.add(new Keyword("Method", s.trim(), SEQWIKI));
				return new Query(null, null, null, keywords, null, null, null);
			}).collect(Collectors.toList()));

		return queries;
	}

	private static List<Query> getSEQwikiTool(SEQwiki SEQwiki) {
		List<Query> queries = new ArrayList<>();

		List<String> webpageUrls = new ArrayList<>();
		webpageUrls.add(SEQWIKI + SEQwiki.getName().trim().replace(" ", "_"));

		queries.addAll(INTERNAL_SEPARATOR_COMMA.splitAsStream(SEQwiki.getDomains())
			.map(s -> {
				List<Keyword> keywords = new ArrayList<>();
				keywords.add(new Keyword("Domain", s.trim(), SEQWIKI));
				return new Query(SEQwiki.getName().trim(), webpageUrls, null, keywords, null, null, null);
			}).collect(Collectors.toList()));
		queries.addAll(INTERNAL_SEPARATOR_COMMA.splitAsStream(SEQwiki.getMethods())
			.map(s -> {
				List<Keyword> keywords = new ArrayList<>();
				keywords.add(new Keyword("Method", s.trim(), SEQWIKI));
				return new Query(SEQwiki.getName().trim(), webpageUrls, null, keywords, null, null, null);
			}).collect(Collectors.toList()));

		return queries;
	}

	private static Query getMsutils(Msutils msutils) {
		List<String> webpageUrls = new ArrayList<>();
		if (msutils.getWeblink() != null && !msutils.getWeblink().trim().isEmpty()) {
			webpageUrls.add(msutils.getWeblink().trim());
		}
		if (msutils.getLink() != null && !msutils.getLink().trim().isEmpty()) {
			webpageUrls.add(msutils.getLink().trim());
		}

		Set<EdamUri> annotations = new LinkedHashSet<>();
		annotations.addAll(edamUris(msutils.getTopic()));
		annotations.addAll(edamUris(msutils.getOperation()));
		annotations.addAll(edamUris(msutils.getFormat_in()));
		annotations.addAll(edamUris(msutils.getFormat_out()));

		return new Query(msutils.getName().trim(), webpageUrls, msutils.getDescription().trim(), null, split(msutils.getPaper()), null, annotations);
	}

	private static Query getBiotools(Biotools biotools) {
		List<String> webpageUrls = new ArrayList<>();
		webpageUrls.add(biotools.getHomepage().trim());
		webpageUrls.addAll(biotools.getMirrors().stream().map(s -> s.trim()).collect(Collectors.toList()));

		List<String> publicationIds = new ArrayList<>();
		if (biotools.getPublicationsPrimaryID() != null) {
			publicationIds.add(biotools.getPublicationsPrimaryID().trim());
		}
		publicationIds.addAll(biotools.getPublicationsOtherIDs().stream().map(s -> s.trim()).collect(Collectors.toList()));

		List<String> docUrls = new ArrayList<>();
		if (biotools.getDocsHome() != null) {
			docUrls.add(biotools.getDocsHome().trim());
		}
		if (biotools.getDocsGithub() != null) {
			docUrls.add(biotools.getDocsGithub().trim());
		}

		Set<EdamUri> annotations = new LinkedHashSet<>();
		annotations.addAll(edamUris(biotools.getTopics()));
		annotations.addAll(edamUris(biotools.getFunctionNames()));
		annotations.addAll(edamUris(biotools.getDataTypes()));
		annotations.addAll(edamUris(biotools.getDataFormats()));

		return new Query(biotools.getName().trim(), webpageUrls, biotools.getDescription().trim(), null, publicationIds, docUrls, annotations);
	}

	private static Query getBioConductor(BioConductor bioConductor) {
		String name = bioConductor.getName().trim() + " : " + bioConductor.getTitle().trim().replaceAll("\n", " ");

		List<String> webpageUrls = null;
		if (bioConductor.getReposFullUrl() != null && !bioConductor.getReposFullUrl().trim().isEmpty()) {
			webpageUrls = new ArrayList<>();
			webpageUrls.add(bioConductor.getReposFullUrl().trim());
		}

		List<String> docUrls = null;
		if (bioConductor.getReposFullUrl() != null && !bioConductor.getReposFullUrl().trim().isEmpty()) {
			docUrls = new ArrayList<>();
			docUrls.add(bioConductor.getReposFullUrl().replaceFirst("/bioc/html/(.+)\\.html", "/bioc/manuals/$1/man/$1.pdf").trim());
			docUrls.add(bioConductor.getReposFullUrl().replaceFirst("/bioc/html/(.+)\\.html", "/bioc/vignettes/$1/inst/doc/$1.pdf").trim());
			docUrls.add(bioConductor.getReposFullUrl().replaceFirst("/bioc/html/(.+)\\.html", "/bioc/vignettes/$1/inst/doc/$1.html").trim());
			docUrls.add(bioConductor.getReposFullUrl().replaceFirst("/bioc/html/(.+)\\.html", "/bioc/vignettes/$1/inst/doc/$1-vignette.pdf").trim());
		}

		Set<EdamUri> annotations = new LinkedHashSet<>();
		annotations.addAll(edamUris(bioConductor.getTopic_URI()));
		annotations.addAll(edamUris(bioConductor.getOperation_URI()));

		return new Query(
			name,
			webpageUrls,
			bioConductor.getDescription().trim(),
			keywordsCamelCase(bioConductor.getBiocViews(), "biocViews", BIOC_VIEWS, INTERNAL_SEPARATOR_BAR),
			null,
			docUrls,
			annotations);
	}

	public static List<Query> get(String queryPath, QueryType type) throws IOException, ParseException, XMLStreamException, FactoryConfigurationError {
		List<Input> inputs;
		if (type == QueryType.biotools) {
			inputs = Xml.load(queryPath, type);
		} else {
			inputs = Csv.load(queryPath, type);
		}

		Set<Query> queries = new LinkedHashSet<>();

		for (Input input : inputs) {
			switch (type) {
				case generic: queries.add(getGeneric((Generic) input)); break;
				case SEQwiki: queries.add(getSEQwiki((SEQwiki) input)); break;
				case SEQwikiTags: queries.addAll(getSEQwikiTags((SEQwiki) input)); break;
				case SEQwikiTool: queries.addAll(getSEQwikiTool((SEQwiki) input)); break;
				case msutils: queries.add(getMsutils((Msutils) input)); break;
				case biotools: queries.add(getBiotools((Biotools) input)); break;
				case BioConductor: queries.add(getBioConductor((BioConductor) input)); break;
			}
		}

		return new ArrayList<>(queries);
	}
}
