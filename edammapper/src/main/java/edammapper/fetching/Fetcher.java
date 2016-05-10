package edammapper.fetching;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Fetcher {

	private static final String DOIprefixes = "http://doi.org/|https://doi.org/|http://dx.doi.org/|https://dx.doi.org/|doi:";
	private static final Pattern DOIprefix = Pattern.compile("^(" + DOIprefixes + ")");
	private static final Pattern DOI = Pattern.compile("^(" + DOIprefixes + "|)10\\..+/.*");
	private static final String DOIlink = "http://doi.org/";

	private static final Pattern PMID = Pattern.compile("^[1-9][0-9]*$");
	private static final String PMIDlink = "http://www.ncbi.nlm.nih.gov/pubmed/?term=";

	private static final Pattern PMCID = Pattern.compile("^PMC[1-9][0-9]*$");
	private static final String PMCIDlink = "http://www.ncbi.nlm.nih.gov/pmc/articles/";
	private static final String EUROPEPMClink = "http://europepmc.org/articles/";

	private static final String EUROPEPMC = "http://www.ebi.ac.uk/europepmc/webservices/rest/";
	private static final String EUTILS = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/";

	private static final String MESHlink = "http://www.ncbi.nlm.nih.gov/mesh/?term=";
	private static final String EFOlink = "http://www.ebi.ac.uk/efo/";
	private static final String GOlink = "http://amigo.geneontology.org/amigo/term/GO:";

	private static final Pattern KEYWORDS_BEGIN = Pattern.compile("(?i)^[\\p{Z}\\p{Cc}]*keywords?[\\p{Z}\\p{Cc}]*:*[\\p{Z}\\p{Cc}]*");
	private static final Pattern SEPARATOR_COMMA = Pattern.compile(",");
	private static final Pattern SEPARATOR_SEMICOLON = Pattern.compile(";");

	private static final String USER_AGENT = "Mozilla";
	private static final int TIMEOUT = 10000; // ms

	private final DoiParse doiParse;

	public Fetcher() throws IOException {
		doiParse = new DoiParse();
	}

	public static boolean isPmid(String s) {
		if (s == null) return false;
		return PMID.matcher(s).matches();
	}

	public static boolean isPmcid(String s) {
		if (s == null) return false;
		return PMCID.matcher(s).matches();
	}

	public static String extractPmcid(String s) {
		if (s == null || s.length() < 3) return "";
		return s.substring(3);
	}

	public static boolean isDoi(String s) {
		if (s == null) return false;
		return DOI.matcher(s).matches();
	}

	public static String normalizeDoi(String s) {
		if (!isDoi(s)) return s;
		return DOIprefix.matcher(s).replaceFirst("");

		// http://www.doi.org/doi_handbook/2_Numbering.html#2.4
		// DOI names are case insensitive, using ASCII case folding for comparison of text.
		// (Case insensitivity for DOI names applies only to ASCII characters. DOI names which differ in the case of non-ASCII Unicode characters may be different identifiers.)
		// 10.123/ABC is identical to 10.123/AbC.
		// All DOI names are converted to upper case upon registration, which is a common practice for making any kind of service case insensitive.

		// However, it's not certain that all services respect this.
		// So leave the case as it was originally specified.

		// char[] c = DOIprefix.matcher(s).replaceFirst("").toCharArray();
		// for (int i = 0; i < c.length; ++i) {
		// 	if (c[i] >= 'a' && c[i] <= 'z') {
		// 		c[i] -= 32;
		// 	}
		// }
		// return new String(c);
	}

	public static String getDoiRegistrant(String s) {
		if (s == null) return "";
		String doiRegistrant = "";
		int begin = s.indexOf("10.");
		if (begin != -1) {
			int end = s.indexOf("/", begin + 3);
			if (end != -1) {
				doiRegistrant = s.substring(begin + 3, end);
			}
		}
		return doiRegistrant;
	}

	public static String getPublicationLink(String publicationId) {
		String link = null;
		if (isDoi(publicationId)) link = DOIlink + publicationId;
		else if (isPmid(publicationId)) link = PMIDlink + publicationId;
		else if (isPmcid(publicationId)) link = PMCIDlink + publicationId + "/";
		return link;
	}

	public static String getMeshLink(MeshTerm mesh) {
		String link = null;
		if (mesh.getUniqueId() != null && !mesh.getUniqueId().isEmpty()) {
			link = MESHlink + "%22" + mesh.getUniqueId() + "%22";
		} else if (mesh.getTerm() != null && !mesh.getTerm().isEmpty()) {
			link = MESHlink + "%22" + mesh.getTerm().replaceAll(" ", "+") + "%22";
		}
		return link;
	}

	public static String getMinedLink(MinedTerm mined) {
		String link = null;
		if (mined.getDbIds() != null && !mined.getDbIds().isEmpty()) {
			if (mined.getDbName() != null && mined.getDbName().equalsIgnoreCase("efo")) {
				link = EFOlink + mined.getDbIds().get(0);
			} else if (mined.getDbName() != null && mined.getDbName().equalsIgnoreCase("GO")) {
				link = GOlink + mined.getDbIds().get(0);
			}
		}
		return link;
	}

	private static Document getDoc(String url) {
		return getDoc(url, null, null);
	}

	private static Document getDoc(String url, Publication publication) {
		return getDoc(url, publication, null);
	}

	private static Document getDoc(String url, Publication publication, StringBuilder sb) {
		Document doc = null;

		try {
			doc = Jsoup.connect(url)
				.userAgent(USER_AGENT)
				.timeout(TIMEOUT)
				.validateTLSCertificates(false)
				.get();
		} catch (MalformedURLException e) {
			// if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
			System.err.println(e);
		} catch (HttpStatusException e) {
			// if the response is not OK and HTTP response errors are not ignored
			System.err.println(e);
		} catch (UnsupportedMimeTypeException e) {
			// if the response mime type is not supported and those errors are not ignored
			System.err.println(e);
			// TODO make this nicer
			if (e.getMimeType() != null && e.getMimeType().equalsIgnoreCase("application/pdf")) {
				// doi links can point directly to PDF files
				if (publication != null) {
					fetchPdf(publication, url);
				}
				// webpage and doc urls can point directly to PDF files
				if (sb != null) {
					sb.append(getPdf(url));
				}
			}
		} catch (SocketTimeoutException e) {
			// if the connection times out
			System.err.println(e);
		} catch (IOException e) {
			// on error
			System.err.println(e);
		} catch (Exception e) {
			System.err.println(e);
		}

		if (doc != null && !doc.hasText()) {
			System.err.println("Empty page returned for " + (url != null ? url : ""));
			doc = null;
		}

		if (doc != null) {
			System.out.println("Fetched " + doc.location());
		}

		return doc;
	}

	private static void fetchPdf(Publication publication, String url) {
		// Don't fetch PDF if only keywords are missing
		if (publication == null || (publication.isTitleFinal() && publication.isAbstractFinal() && publication.isFulltextFinal())) return;

		System.out.println("Try PDF " + url);
		try {
			URLConnection con = new URL(url).openConnection();
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setConnectTimeout(TIMEOUT);
			con.setReadTimeout(TIMEOUT);

			try (PDDocument doc = PDDocument.load(con.getInputStream())) {
				System.out.println("Fetched PDF " + con.getURL());

				if (!publication.isTitleFinal() || !publication.isKeywordsFinal() || !publication.isAbstractFinal()) {
					PDDocumentInformation info = doc.getDocumentInformation();
					if (info != null) {
						if (!publication.isTitleFinal()) {
							publication.setTitle(info.getTitle());
						}
						if (!publication.isKeywordsFinal()) {
							String keywords = info.getKeywords();
							if (keywords != null) {
								publication.setKeywords(SEPARATOR_COMMA.splitAsStream(keywords).map(s -> s.trim()).collect(Collectors.toList()));
							}
						}
						if (!publication.isAbstractFinal()) {
							publication.setAbstract(info.getSubject());
						}
					}
				}

				if (!publication.isFulltextFinal()) {
					PDFTextStripper stripper = new PDFTextStripper();
					publication.setFulltext(stripper.getText(doc));
				}

				if (!publication.isTitleFinal() || !publication.isKeywordsFinal() || !publication.isAbstractFinal()) {
					PDMetadata meta = doc.getDocumentCatalog().getMetadata();
					if (meta != null) {
						try (InputStream xmlInputStream = meta.createInputStream()) {
							XMPMetadata xmp = new DomXmpParser().parse(xmlInputStream);

							DublinCoreSchema dc = xmp.getDublinCoreSchema();
							if (dc != null) {
								if (!publication.isTitleFinal()) {
									publication.setTitle(dc.getTitle());
								}
								if (!publication.isKeywordsFinal()) {
									publication.setKeywords(dc.getSubjects());
								}
								if (!publication.isAbstractFinal()) {
									publication.setAbstract(dc.getDescription());
								}
							}

							AdobePDFSchema pdf = xmp.getAdobePDFSchema();
							if (pdf != null) {
								if (!publication.isKeywordsFinal()) {
									String keywords = pdf.getKeywords();
									if (keywords != null) {
										publication.setKeywords(SEPARATOR_COMMA.splitAsStream(keywords).map(s -> s.trim()).collect(Collectors.toList()));
									}
								}
							}
						}
					}
				}
			}
		} catch (MalformedURLException e) {
			// if no protocol is specified, or an unknown protocol is found, or spec is null
			System.err.println(e);
		} catch (SocketTimeoutException e) {
			// if the connection times out
			System.err.println(e);
		} catch (IOException e) {
			// in case of a file reading or parsing error
			System.err.println(e);
		} catch (XmpParsingException e) {
			System.err.println(e);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	private static String getPdf(String url) {
		System.out.println("Try PDF " + url);

		String pdfText = "";
		try {
			URLConnection con = new URL(url).openConnection();
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setConnectTimeout(TIMEOUT);
			con.setReadTimeout(TIMEOUT);

			try (PDDocument doc = PDDocument.load(con.getInputStream())) {
				System.out.println("Fetched PDF " + con.getURL());

				PDFTextStripper stripper = new PDFTextStripper();
				pdfText = stripper.getText(doc);
			}
		} catch (MalformedURLException e) {
			// if no protocol is specified, or an unknown protocol is found, or spec is null
			System.err.println(e);
		} catch (SocketTimeoutException e) {
			// if the connection times out
			System.err.println(e);
		} catch (IOException e) {
			// in case of a file reading or parsing error
			System.err.println(e);
		} catch (Exception e) {
			System.err.println(e);
		}
		return pdfText;
	}

	private static String text(Elements elements) {
		StringBuilder sb = new StringBuilder();
		for (Element element : elements) {
			if (element.hasText()) {
				if (sb.length() != 0) sb.append("\n\n");
				sb.append(element.text());
			}
		}
		return sb.toString();
	}

	private static String getFirstTrimmed(Document doc, String selector) {
		if (doc == null || selector == null) return "";
		Element tag = doc.select(selector.trim()).first();
		if (tag != null) {
			return tag.text();
		}
		return "";
	}

	public static String getDoi(String id) {
		if (isDoi(id)) return normalizeDoi(id);

		String europepmc = EUROPEPMC + "search?resulttype=lite&page=1&pageSize=1&format=xml";
		if (isPmid(id)) {
			europepmc += "&query=ext_id:" + id + " src:med";
		} else if (isPmcid(id)) {
			europepmc += "&query=pmcid:" + id;
		} else {
			// TODO log
			return "";
		}

		Document doc = getDoc(europepmc);
		if (doc != null) {
			String doi = getFirstTrimmed(doc, "doi");
			if (isDoi(doi)) return normalizeDoi(doi);
		}

		if (isPmid(id)) {
			doc = getDoc(EUTILS + "efetch.fcgi?retmode=xml&db=pubmed&id=" + id);
			if (doc != null) {
				String doi = getFirstTrimmed(doc, "ArticleId[IdType=doi]");
				if (isDoi(doi)) return normalizeDoi(doi);
			}
		} else if (isPmcid(id)) {
			doc = getDoc(EUTILS + "efetch.fcgi?retmode=xml&db=pmc&id=" + extractPmcid(id));
			if (doc != null) {
				String doi = getFirstTrimmed(doc, "article-id[pub-id-type=doi]");
				if (isDoi(doi)) return normalizeDoi(doi);
			}
		}

		return "";
	}

	public boolean isKnownDoi(String doi) {
		if (!isDoi(doi)) return false;
		doi = normalizeDoi(doi);

		String site = doiParse.getSite(getDoiRegistrant(doi));
		return site != null;
	}

	private void setIds(Publication publication, Document doc, String pmid, String pmcid, String doi) {
		// nulls
		if (publication.getPmid().isEmpty()) {
			String pmidText = getFirstTrimmed(doc, pmid);
			if (isPmid(pmidText)) {
				publication.setPmid(pmidText);
			}
		}
		if (publication.getPmcid().isEmpty()) {
			String pmcidText = getFirstTrimmed(doc, pmcid);
			if (isPmcid(pmcidText)) {
				publication.setPmcid(pmcidText);
			}
		}
		if (publication.getDoi().isEmpty()) {
			String doiText = getFirstTrimmed(doc, doi);
			if (isDoi(doiText)) {
				publication.setDoi(normalizeDoi(doiText));
			}
		}
	}

	private void setTitle(Publication publication, Document doc, String title) {
		// nulls
		if (!publication.isTitleFinal() && title != null && !title.trim().isEmpty()) {
			Element titleTag = doc.select(title.trim()).first();
			if (titleTag != null) {
				publication.setTitle(titleTag.text());
			}
		}
	}

	private void setKeywords(Publication publication, Document doc, String keywords) {
		// nulls
		if (!publication.isKeywordsFinal() && keywords != null && !keywords.trim().isEmpty()) {
			Elements keywordsTags = doc.select(keywords.trim());
			publication.setKeywords(keywordsTags.stream().map(k -> k.text()).collect(Collectors.toList()));
		}
	}

	private void setKeywordsSplit(Publication publication, Document doc, String keywordsSplit, Pattern separator) {
		// nulls
		if (!publication.isKeywordsFinal() && keywordsSplit != null && !keywordsSplit.trim().isEmpty()) {
			List<String> keywords = new ArrayList<>();
			for (Element e: doc.select(keywordsSplit.trim())) {
				String k = e.text();
				k = KEYWORDS_BEGIN.matcher(k).replaceFirst("");
				if (!k.isEmpty()) {
					keywords.addAll(separator.splitAsStream(k).map(s -> s.trim()).collect(Collectors.toList()));
				}
			}
			publication.setKeywords(keywords);
		}
	}

	private void setAbstract(Publication publication, Document doc, String theAbstract) {
		// nulls
		if (!publication.isAbstractFinal() && theAbstract != null && !theAbstract.trim().isEmpty()) {
			publication.setAbstract(text(doc.select(theAbstract.trim())));
		}
	}

	private void setFulltext(Publication publication, Document doc, String title, String theAbstract, String fulltext) {
		// nulls
		if (!publication.isFulltextFinal() && fulltext != null && !fulltext.trim().isEmpty()) {
			StringBuilder sb = new StringBuilder();

			if (title != null && !title.trim().isEmpty()) {
				sb.append(title.trim());
			}

			if (theAbstract != null && !theAbstract.trim().isEmpty()) {
				if (sb.length() != 0) sb.append(", ");
				sb.append(theAbstract.trim());
			}

			if (sb.length() != 0) sb.append(", ");
			sb.append(fulltext.trim());

			publication.setFulltext(text(doc.select(sb.toString())));
		}
	}

	private List<MinedTerm> getMinedTerms(String url) {
		List<MinedTerm> minedTerms = new ArrayList<>();

		Document doc = getDoc(url);

		if (doc != null) {
			for (Element tmSummary : doc.getElementsByTag("tmSummary")) {
				MinedTerm minedTerm = new MinedTerm();

				Element term = tmSummary.getElementsByTag("term").first();
				if (term != null) {
					minedTerm.setTerm(term.text());
				}

				Element count = tmSummary.getElementsByTag("count").first();
				if (count != null) {
					try {
						minedTerm.setCount(Integer.parseInt(count.text()));
					} catch (NumberFormatException e) {
						// TODO log
					}
				}

				Element altNameList = tmSummary.getElementsByTag("altNameList").first();
				if (altNameList != null) {
					List<String> altNames = new ArrayList<>();
					for (Element altName : altNameList.getElementsByTag("altName")) {
						altNames.add(altName.text());
					}
					minedTerm.setAltNames(altNames);
				}

				Element dbName = tmSummary.getElementsByTag("dbName").first();
				if (dbName != null) {
					minedTerm.setDbName(dbName.text());
				}

				Element dbIdList = tmSummary.getElementsByTag("dbIdList").first();
				if (dbIdList != null) {
					List<String> dbIds = new ArrayList<>();
					for (Element dbId : dbIdList.getElementsByTag("dbId")) {
						dbIds.add(dbId.text());
					}
					minedTerm.setDbIds(dbIds);
				}

				minedTerms.add(minedTerm);
			}
		}

		return minedTerms;
	}

	private void fetchEuropepmc(Publication publication) {
		if (publication == null || publication.isFinal()) return;

		String europepmc = EUROPEPMC + "search?resulttype=core&page=1&pageSize=1&format=xml";
		if (!publication.getPmid().isEmpty()) {
			europepmc += "&query=ext_id:" + publication.getPmid() + " src:med";
		} else if (!publication.getPmcid().isEmpty()) {
			europepmc += "&query=pmcid:" + publication.getPmcid();
		} else if (!publication.getDoi().isEmpty()) {
			europepmc += "&query=doi:" + publication.getDoi();
		} else {
			// TODO log
			return;
		}

		boolean hasFulltextXML = false;
		boolean hasFulltextHTML = false;
		boolean hasMinedTerms = false;

		Document doc = getDoc(europepmc);

		if (doc != null) {
			int count = 0;

			try {
				Element hitCount = doc.getElementsByTag("hitCount").first();
				if (hitCount != null) {
					count = Integer.parseInt(hitCount.text());
				}
			} catch (NumberFormatException e) {
				// TODO log
			}

			if (count > 0) {
				setIds(publication, doc, "pmid", "pmcid", "doi");

				setTitle(publication, doc, "title");

				setKeywords(publication, doc, "keyword");

				if (!publication.isMeshTermsFinal()) {
					List<MeshTerm> meshTerms = new ArrayList<>();
					for (Element meshHeading : doc.getElementsByTag("meshHeading")) {
						MeshTerm meshTerm = new MeshTerm();

						Element majorTopic_YN = meshHeading.getElementsByTag("majorTopic_YN").first();
						if (majorTopic_YN != null) {
							meshTerm.setMajorTopic(majorTopic_YN.text().equalsIgnoreCase("Y"));
						}

						Element descriptorName = meshHeading.getElementsByTag("descriptorName").first();
						if (descriptorName != null) {
							meshTerm.setTerm(descriptorName.text());
						}

						meshTerms.add(meshTerm);
					}
					publication.setMeshTerms(meshTerms);
				}

				setAbstract(publication, doc, "abstractText");

				Element isOpen = doc.getElementsByTag("isOpenAccess").first();
				if (isOpen != null && isOpen.text().equalsIgnoreCase("Y")) {
					hasFulltextXML = true;
				}

				Element inEPMC = doc.getElementsByTag("inEPMC").first();
				if (inEPMC != null && inEPMC.text().equalsIgnoreCase("Y")) {
					hasFulltextHTML = true;
				}

				Element isMined = doc.getElementsByTag("hasTextMinedTerms").first();
				if (isMined != null && isMined.text().equalsIgnoreCase("Y")) {
					hasMinedTerms = true;
				}
			} else {
				// TODO log
			}
		}

		if (!publication.isFulltextFinal() && hasFulltextXML && !publication.getPmcid().isEmpty()) {
			String europepmcFull = EUROPEPMC + publication.getPmcid() + "/fullTextXML";
			Document docFull = getDoc(europepmcFull);

			if (docFull != null) {
				setFulltext(publication, docFull, "front article-title", "front abstract > p",
					"body sec > list list-item, body > p, body > fig, body > table-wrap, " +
					"body sec > title, body sec > p, body sec > fig, body sec > table-wrap"); // supplementary-material
			}
		}

		if (!publication.isFulltextFinal() && hasFulltextHTML && !publication.getPmcid().isEmpty()) {
			String europepmcFull = EUROPEPMClink + publication.getPmcid();
			Document docFull = getDoc(europepmcFull);

			if (docFull != null) {
				setFulltext(publication, docFull, "#article_body .fm-sec > h1", "#article_body h2:matchesOwn((?i)^\\s*abstract\\s*$) + div p",
					"#article_body .boxed-text-box > ul li, #article_body .sec > h2, #article_body .sec > h3, #article_body .sec > h4, #article_body .sec > p, #article_body .sec > .iconblock");
			}
		}

		if ((!publication.isEfoTermsFinal() || !publication.isGoTermsFinal()) && hasMinedTerms && (!publication.getPmcid().isEmpty() || !publication.getPmid().isEmpty())) {
			String ext_id = "";
			if (!publication.getPmcid().isEmpty()) {
				ext_id = "PMC/" + publication.getPmcid() + "/textMinedTerms";
			} else {
				ext_id = "MED/" + publication.getPmid() + "/textMinedTerms";
			}
			if (!publication.isEfoTermsFinal()) {
				publication.setEfoTerms(getMinedTerms(EUROPEPMC + ext_id + "/EFO"));
			}
			if (!publication.isGoTermsFinal()) {
				publication.setGoTerms(getMinedTerms(EUROPEPMC + ext_id + "/GO_TERM"));
			}
		}
	}

	private void fetchPmid(Publication publication) {
		// keywords are usually missing (and if present, fetched by Pmcid),
		// so don't put isKeywordsFinal() here
		if (publication == null || (publication.isIdFinal() && publication.isTitleFinal() &&
			publication.isMeshTermsFinal() &&
			publication.isAbstractFinal())) return;

		String pmid = publication.getPmid();
		if (pmid.isEmpty()) return;

		Document doc = getDoc(EUTILS + "efetch.fcgi?retmode=xml&db=pubmed&id=" + pmid);

		if (doc != null) {
			setIds(publication, doc, "ArticleId[IdType=pubmed]", "ArticleId[IdType=pmc]", "ArticleId[IdType=doi]");

			setTitle(publication, doc, "ArticleTitle");

			setKeywords(publication, doc, "Keyword");

			if (!publication.isMeshTermsFinal()) {
				List<MeshTerm> meshTerms = new ArrayList<>();
				for (Element descriptorName : doc.getElementsByTag("DescriptorName")) {
					MeshTerm meshTerm = new MeshTerm();

					meshTerm.setTerm(descriptorName.text());

					meshTerm.setMajorTopic(descriptorName.attr("MajorTopicYN").trim().equalsIgnoreCase("Y"));

					meshTerm.setUniqueId(descriptorName.attr("UI").trim());

					meshTerms.add(meshTerm);
				}
				publication.setMeshTerms(meshTerms);
			}

			setAbstract(publication, doc, "AbstractText");
		}

		// don't query HTML just because keywords are missing (they usually are),
		// as they will likely be missing in HTML also
		if (!publication.isTitleFinal() || !publication.isMeshTermsFinal() || !publication.isAbstractFinal()) {
			Document docHTML = getDoc(PMIDlink + pmid);

			if (docHTML != null) {
				setTitle(publication, docHTML, ".rprt > h1");

				setKeywordsSplit(publication, docHTML, ".keywords p", SEPARATOR_SEMICOLON);

				// TODO is actually in form DescriptorName/QualifierName instead of just DescriptorName
				publication.setMeshTerms(docHTML.select("a[alsec=mesh]").stream()
					.map(k -> { MeshTerm m = new MeshTerm(); m.setTerm(k.text()); return m; })
					.collect(Collectors.toList()));

				setAbstract(publication, docHTML, "abstracttext");
			}
		}
	}

	private void fetchPmcid(Publication publication) {
		if (publication == null || (publication.isIdFinal() && publication.isTitleFinal() &&
			publication.isKeywordsFinal() &&
			publication.isAbstractFinal() && publication.isFulltextFinal())) return;

		String pmcid = publication.getPmcid();
		if (pmcid.isEmpty()) return;

		Document doc = getDoc(EUTILS + "efetch.fcgi?retmode=xml&db=pmc&id=" + extractPmcid(pmcid));

		if (doc != null) {
			setIds(publication, doc, "article-id[pub-id-type=pmid]", "article-id[pub-id-type=pmc]", "article-id[pub-id-type=doi]");

			setTitle(publication, doc, "front article-title");

			setKeywords(publication, doc, "front kwd");

			setAbstract(publication, doc, "front abstract > p");

			setFulltext(publication, doc, "front article-title", "front abstract > p",
				"body sec > list list-item, body > p, body > fig, body > table-wrap, " +
				"body sec > title, body sec > p, body sec > fig, body sec > table-wrap"); // supplementary-material
		}

		if (!publication.isTitleFinal() || !publication.isKeywordsFinal() || !publication.isAbstractFinal() || !publication.isFulltextFinal()) {
			Document docHTML = getDoc(PMCIDlink + pmcid);

			if (docHTML != null) {
				setTitle(publication, docHTML, "#maincontent .fm-sec > h1");

				setKeywordsSplit(publication, docHTML, ".kwd-text", SEPARATOR_COMMA);

				setAbstract(publication, docHTML, "#maincontent h2:matchesOwn((?i)^\\s*abstract\\s*$) + div p");

				setFulltext(publication, docHTML, "#maincontent .fm-sec > h1", "#maincontent h2:matchesOwn((?i)^\\s*abstract\\s*$) + div p",
					"#maincontent .boxed-text-box > ul li, #maincontent .sec > h2, #maincontent .sec > h3, #maincontent .sec > h4, #maincontent .sec > p, #maincontent .sec > .iconblock");

				if (!publication.isTitleFinal() || !publication.isKeywordsFinal() || !publication.isAbstractFinal() || !publication.isFulltextFinal()) {
					Element a = docHTML.select("a[href*=" + pmcid + "/pdf/]").first();
					if (a != null) {
						String pdfHref = a.attr("abs:href");
						if (pdfHref != null) {
							fetchPdf(publication, pdfHref);
						}
					}
				}
			}
		}
	}

	private String getHref(Document doc, String src, String dst, String a) {
		String href = null;

		if (a != null && !a.trim().isEmpty() && doc != null) {
			Element aTag = doc.select(a.trim()).first();
			if (aTag != null) {
				href = aTag.attr("abs:href");
				if (href == null || href.isEmpty()) {
					// TODO log
				}
			} else {
				// TODO log
			}
		}

		if (src != null && dst != null && doc != null) {
			String url = doc.location();
			if (url != null && !url.isEmpty()) {
				String newUrl = url.replaceFirst(src, dst);
				if (newUrl != null && !newUrl.isEmpty()) {
					href = newUrl;
				} else {
					// TODO log
				}
			}
		}

		return href;
	}

	private void fetchDoi(Publication publication) {
		if (publication == null || (publication.isTitleFinal() && publication.isKeywordsFinal() && publication.isAbstractFinal() && publication.isFulltextFinal())) return;

		String doi = publication.getDoi();
		if (doi.isEmpty()) return;

		String site = doiParse.getSite(getDoiRegistrant(doi));
		if (site == null) {
			// TODO log
			if (!publication.isFulltextFinal()) {
				Document doc = getDoc(DOIlink + doi, publication);
				if (doc != null) {
					publication.setFulltext(doc.text());
				}
			}
			return;
		}

		Document doc = getDoc(DOIlink + doi, publication);

		if (doc != null) {
			setTitle(publication, doc, doiParse.getTitle(site));

			setKeywords(publication, doc, doiParse.getKeywords(site));

			setKeywordsSplit(publication, doc, doiParse.getKeywordsSplit(site), SEPARATOR_COMMA);

			setAbstract(publication, doc, doiParse.getAbstract(site));

			boolean separateFulltext = doiParse.separateFulltext(site);

			if (!separateFulltext) {
				setFulltext(publication, doc, doiParse.getTitle(site), doiParse.getAbstract(site), doiParse.getFulltext(site));
			}

			if (!publication.isFulltextFinal() && separateFulltext) {
				String fulltextHref = getHref(doc, doiParse.getFulltextSrc(site), doiParse.getFulltextDst(site), doiParse.getFulltextA(site));
				if (fulltextHref != null) {
					Document docFull = getDoc(fulltextHref);

					if (docFull != null) {
						setFulltext(publication, docFull, doiParse.getTitle(site), doiParse.getAbstract(site), doiParse.getFulltext(site));
					}
				}
			}

			if (!publication.isTitleFinal() || !publication.isKeywordsFinal() || !publication.isAbstractFinal() || !publication.isFulltextFinal()) {
				String pdfHref = getHref(doc, doiParse.getPdfSrc(site), doiParse.getPdfDst(site), doiParse.getPdfA(site));
				if (pdfHref != null) {
					fetchPdf(publication, pdfHref);
				}
			}
		}
	}

	public Publication getPublication(String id) {
		Publication publication = new Publication();

		if (isPmid(id)) {
			publication.setPmid(id);
		} else if (isPmcid(id)) {
			publication.setPmcid(id);
		} else if (isDoi(id)) {
			publication.setDoi(normalizeDoi(id));
		} else {
			System.err.println("Unknown publication ID: " + id);
			return null;
		}

		fetchEuropepmc(publication);

		if (isPmid(id)) {
			fetchPmid(publication);
			fetchPmcid(publication);
			fetchDoi(publication);
		} else if (isPmcid(id)) {
			fetchPmcid(publication);
			fetchPmid(publication);
			fetchDoi(publication);
		} else if (isDoi(id)) {
			fetchDoi(publication);
			fetchPmid(publication);
			fetchPmcid(publication);
		}

		if (publication.isEmpty()) {
			System.err.println("Empty publication returned for " + id);
			return null;
		}

		return publication;
	}

	// TODO can move site configuration also to a YAML file
	private static String getSelector(String url) {
		if (url == null) return null;
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			return null;
		}
		String host = uri.getHost();
		if (host == null) return null;
		if (host.startsWith("www.")) host = host.substring(4);
		switch (host) {
		case "seqanswers.com": return
			"#mw-content-text > p ~ p:not(:matchesOwn((?i)^\\s*To add a reference for )):not(:matches((?i)^\\s*none specified\\s*$)), " +
			"#mw-content-text > ul li:not(:has(a)), " +
			"#mw-content-text > pre, " +
			"#mw-content-text > .wikitable tr";
		default: return null;
		}
	}

	public String getWebpage(String url) {
		return getWebpage(url, getSelector(url));
	}

	public String getWebpage(String url, String selector) {
		if (url == null) return null;
		StringBuilder sb = new StringBuilder();
		Document doc = getDoc(url, null, sb);
		if (doc != null) {
			if (selector != null) {
				return text(doc.select(selector.trim()));
			} else {
				return doc.text();
			}
		} else if (sb.length() > 0) {
			return sb.toString();
		} else {
			return null;
		}
	}
}
