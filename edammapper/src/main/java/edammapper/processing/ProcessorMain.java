package edammapper.processing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.mapdb.DBException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import edammapper.fetching.Database;
import edammapper.fetching.Fetcher;
import edammapper.fetching.Publication;
import edammapper.idf.Idf;
import edammapper.input.Input;
import edammapper.input.json.Biotools;
import edammapper.output.Output;
import edammapper.query.PublicationIds;
import edammapper.query.QueryLoader;
import edammapper.query.QueryType;

public class ProcessorMain {

	private static void printFetchWebpage(String webpageUrl) throws IOException {
		String webpage = new Fetcher().getWebpage(webpageUrl);
		if (webpage != null) System.out.println(webpage);
	}

	private static void printFetchPublication(String publicationId) throws IOException {
		Publication publication = new Fetcher().getPublication(QueryLoader.onePublicationId(publicationId));
		if (publication != null) System.out.println(publication);
	}

	private static void printFetchDoc(String docUrl) throws IOException {
		String doc = new Fetcher().getWebpage(docUrl);
		if (doc != null) System.out.println(doc);
	}

	private static void printUrlsHtml(Set<String> set) {
		System.out.println("<ul>");
		for (String url : set) {
			System.out.println("<li><a href=\"" + url + "\">" + url + "</a></li>");
		}
		System.out.println("</ul>");
	}

	private static String getPublicationLinkHtml(String publicationId) {
		String link = Fetcher.getPublicationLink(publicationId);
		if (link == null) {
			return publicationId;
		} else {
			return "<a href=\"" + link + "\">" + publicationId + "</a>";
		}
	}

	private static String getPublicationLinkHtml(PublicationIds publicationIds) {
		if (publicationIds == null) return "";
		String s = "";
		if (publicationIds.getPmid() != null && !publicationIds.getPmid().isEmpty()) {
			s += getPublicationLinkHtml(publicationIds.getPmid());
		}
		if (publicationIds.getPmcid() != null && !publicationIds.getPmcid().isEmpty()) {
			if (!s.isEmpty()) s += ", ";
			s += getPublicationLinkHtml(publicationIds.getPmcid());
		}
		if (publicationIds.getDoi() != null && !publicationIds.getDoi().isEmpty()) {
			if (!s.isEmpty()) s += ", ";
			s += getPublicationLinkHtml(publicationIds.getDoi());
		}
		return s;
	}

	private static void printPublicationIdHtml(Set<String> set) {
		System.out.println("<ul>");
		for (String id : set) {
			System.out.println("<li>" + getPublicationLinkHtml(id) + "</li>");
		}
		System.out.println("</ul>");
	}

	private static void printPublicationIdsHtml(Set<PublicationIds> set) {
		System.out.println("<ul>");
		for (PublicationIds ids : set) {
			System.out.println("<li>" + getPublicationLinkHtml(ids) + "</li>");
		}
		System.out.println("</ul>");
	}

	private static Set<String> getWebpageUrlsFromFile(String queryPath, QueryType type) throws IOException, ParseException {
		return QueryLoader.get(queryPath, type).stream()
			.flatMap(q -> q.getWebpageUrls().stream().map(l -> l.getUrl()))
			.collect(Collectors.toCollection(TreeSet::new));
	}

	private static Set<PublicationIds> getPublicationIdsFromFile(String queryPath, QueryType type) throws IOException, ParseException {
		return QueryLoader.get(queryPath, type).stream()
			.flatMap(q -> q.getPublicationIds().stream())
			.collect(Collectors.toCollection(TreeSet::new));
	}

	private static Set<String> getDocUrlsFromFile(String queryPath, QueryType type) throws IOException, ParseException {
		return QueryLoader.get(queryPath, type).stream()
			.flatMap(q -> q.getDocUrls().stream().map(l -> l.getUrl()))
			.collect(Collectors.toCollection(TreeSet::new));
	}

	private static void printWebpageUrlsFromFile(String queryPath, QueryType type) throws IOException, ParseException {
		getWebpageUrlsFromFile(queryPath, type).forEach(System.out::println);
	}

	private static void printWebpageUrlsFromFileHtml(String queryPath, QueryType type) throws IOException, ParseException {
		printUrlsHtml(getWebpageUrlsFromFile(queryPath, type));
	}

	private static void printPublicationIdsFromFile(String queryPath, QueryType type) throws IOException, ParseException {
		getPublicationIdsFromFile(queryPath, type).forEach(System.out::println);
	}

	private static void printPublicationIdsFromFileHtml(String queryPath, QueryType type) throws IOException, ParseException {
		printPublicationIdsHtml(getPublicationIdsFromFile(queryPath, type));
	}

	private static void printAllDoiFromFile(String queryPath, QueryType type, boolean html) throws IOException, ParseException {
		Set<String> set = getPublicationIdsFromFile(queryPath, type).stream()
			.map(p -> Fetcher.getDoi(p.getDoi()))
			.filter(Fetcher::isDoi)
			.collect(Collectors.toCollection(TreeSet::new));
		if (html) printPublicationIdHtml(set);
		else set.forEach(System.out::println);
	}

	private static void printUnknownDoiFromFile(String queryPath, QueryType type, boolean html) throws IOException, ParseException {
		Fetcher fetcher = new Fetcher();
		Set<String> set = getPublicationIdsFromFile(queryPath, type).stream()
			.map(p -> Fetcher.getDoi(p.getDoi()))
			.filter(id -> Fetcher.isDoi(id) && !fetcher.isKnownDoi(id))
			.collect(Collectors.toCollection(TreeSet::new));
		if (html) printPublicationIdHtml(set);
		else set.forEach(System.out::println);
	}

	private static void printDocUrlsFromFile(String queryPath, QueryType type) throws IOException, ParseException {
		getDocUrlsFromFile(queryPath, type).forEach(System.out::println);
	}

	private static void printDocUrlsFromFileHtml(String queryPath, QueryType type) throws IOException, ParseException {
		printUrlsHtml(getDocUrlsFromFile(queryPath, type));
	}

	private static void initDatabase(String database) throws FileAlreadyExistsException, DBException {
		Database.init(database);
	}

	private static void addWebpagesFromFile(String database, String queryPath, QueryType type) throws IOException, ParseException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		getWebpageUrlsFromFile(queryPath, type).stream()
			.forEach(url -> db.putWebpageCommit(url, fetcher.getWebpage(url)));
	}

	private static void addPublicationsFromFile(String database, String queryPath, QueryType type) throws IOException, ParseException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		getPublicationIdsFromFile(queryPath, type).stream()
			.forEach(ids -> {
				String id = Processor.choosePublicationId(ids);
				if (id != null) {
					db.putPublicationCommit(id, fetcher.getPublication(ids));
				}
			});
	}

	private static void addDocsFromFile(String database, String queryPath, QueryType type) throws IOException, ParseException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		getDocUrlsFromFile(queryPath, type).stream()
			.forEach(url -> db.putDocCommit(url, fetcher.getWebpage(url)));
	}

	private static void addAllFromFile(String database, String queryPath, QueryType type) throws IOException, ParseException {
		addWebpagesFromFile(database, queryPath, type);
		addPublicationsFromFile(database, queryPath, type);
		addDocsFromFile(database, queryPath, type);
	}

	private static void addMissingWebpagesFromFile(String database, String queryPath, QueryType type) throws IOException, ParseException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		getWebpageUrlsFromFile(queryPath, type).stream()
			.filter(url -> !db.containsWebpage(url))
			.forEach(url -> db.putWebpageCommit(url, fetcher.getWebpage(url)));
	}

	private static void addMissingPublicationsFromFile(String database, String queryPath, QueryType type) throws IOException, ParseException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		getPublicationIdsFromFile(queryPath, type).stream()
			.forEach(ids -> {
				String id = Processor.choosePublicationId(ids);
				if (id != null && !db.containsPublication(id)) {
					db.putPublicationCommit(id, fetcher.getPublication(ids));
				}
			});
	}

	private static void addMissingDocsFromFile(String database, String queryPath, QueryType type) throws IOException, ParseException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		getDocUrlsFromFile(queryPath, type).stream()
			.filter(url -> !db.containsDoc(url))
			.forEach(url -> db.putDocCommit(url, fetcher.getWebpage(url)));
	}

	private static void addAllMissingFromFile(String database, String queryPath, QueryType type) throws IOException, ParseException {
		addMissingWebpagesFromFile(database, queryPath, type);
		addMissingPublicationsFromFile(database, queryPath, type);
		addMissingDocsFromFile(database, queryPath, type);
	}

	private static boolean putWebpage(String database, String webpageUrl) throws IOException, DBException {
		return new Database(database).putWebpageCommit(webpageUrl, new Fetcher().getWebpage(webpageUrl));
	}

	private static boolean putPublication(String database, String publicationId) throws IOException, DBException {
		return new Database(database).putPublicationCommit(publicationId, new Fetcher().getPublication(QueryLoader.onePublicationId(publicationId)));
	}

	private static boolean putDoc(String database, String docUrl) throws IOException, DBException {
		return new Database(database).putDocCommit(docUrl, new Fetcher().getWebpage(docUrl));
	}

	private static void printWebpageUrls(String database) throws FileNotFoundException, DBException {
		new TreeSet<String>(new Database(database).getWebpageUrls()).forEach(System.out::println);
	}

	private static void printWebpageUrlsHtml(String database) throws FileNotFoundException, DBException {
		printUrlsHtml(new TreeSet<String>(new Database(database).getWebpageUrls()));
	}

	private static void printPublicationIds(String database) throws FileNotFoundException, DBException {
		new TreeSet<String>(new Database(database).getPublicationIds()).forEach(System.out::println);
	}

	private static void printPublicationIdsHtml(String database) throws FileNotFoundException, DBException {
		printPublicationIdHtml(new TreeSet<String>(new Database(database).getPublicationIds()));
	}

	private static void printDocUrls(String database) throws FileNotFoundException, DBException {
		new TreeSet<String>(new Database(database).getDocUrls()).forEach(System.out::println);
	}

	private static void printDocUrlsHtml(String database) throws FileNotFoundException, DBException {
		printUrlsHtml(new TreeSet<String>(new Database(database).getDocUrls()));
	}

	private static void printWebpage(String database, String webpageUrl) throws FileNotFoundException, DBException {
		System.out.println(new Database(database).getWebpage(webpageUrl));
	}

	private static void printPublication(String database, String publicationId) throws FileNotFoundException, DBException {
		System.out.println(new Database(database).getPublication(publicationId));
	}

	private static void printDoc(String database, String docUrl) throws FileNotFoundException, DBException {
		System.out.println(new Database(database).getDoc(docUrl));
	}

	private static String removeWebpage(String database, String webpageUrl) throws FileNotFoundException, DBException {
		Database db = new Database(database);
		String previous = db.removeWebpage(webpageUrl);
		db.commit();
		return previous;
	}

	private static Publication removePublication(String database, String publicationId) throws FileNotFoundException, DBException {
		Database db = new Database(database);
		Publication previous = db.removePublication(publicationId);
		db.commit();
		return previous;
	}

	private static String removeDoc(String database, String docUrl) throws FileNotFoundException, DBException {
		Database db = new Database(database);
		String previous = db.removeDoc(docUrl);
		db.commit();
		return previous;
	}

	private static void removeWebpages(String database) throws FileNotFoundException, DBException {
		Database db = new Database(database);
		db.getWebpageUrls().stream()
			.forEach(db::removeWebpage);
		db.commit();
	}

	private static void removeWebpagesRegex(String database, String regex) throws FileNotFoundException, DBException {
		Database db = new Database(database);
		db.getWebpageUrls().stream()
			.filter(Pattern.compile(regex).asPredicate())
			.forEach(db::removeWebpage);
		db.commit();
	}

	private static void removePublications(String database) throws FileNotFoundException, DBException {
		Database db = new Database(database);
		db.getPublicationIds().stream()
			.forEach(db::removePublication);
		db.commit();
	}

	private static void removePublicationsRegex(String database, String regex) throws FileNotFoundException, DBException {
		Database db = new Database(database);
		db.getPublicationIds().stream()
			.filter(Pattern.compile(regex).asPredicate())
			.forEach(db::removePublication);
		db.commit();
	}

	private static void removeDocs(String database) throws FileNotFoundException, DBException {
		Database db = new Database(database);
		db.getDocUrls().stream()
			.forEach(db::removeDoc);
		db.commit();
	}

	private static void removeDocsRegex(String database, String regex) throws FileNotFoundException, DBException {
		Database db = new Database(database);
		db.getDocUrls().stream()
			.filter(Pattern.compile(regex).asPredicate())
			.forEach(db::removeDoc);
		db.commit();
	}

	private static boolean refreshWebpage(String database, String webpageUrl) throws IOException, DBException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		if (db.containsWebpage(webpageUrl)) {
			return db.putWebpageCommit(webpageUrl, fetcher.getWebpage(webpageUrl));
		} else {
			return false;
		}
	}

	private static boolean refreshPublication(String database, String publicationId) throws IOException, DBException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		if (db.containsPublication(publicationId)) {
			return db.putPublicationCommit(publicationId, fetcher.getPublication(QueryLoader.onePublicationId(publicationId)));
		} else {
			return false;
		}
	}

	private static boolean refreshDoc(String database, String docUrl) throws IOException, DBException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		if (db.containsDoc(docUrl)) {
			return db.putDocCommit(docUrl, fetcher.getWebpage(docUrl));
		} else {
			return false;
		}
	}

	private static void refreshWebpages(String database) throws IOException, DBException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		db.getWebpageUrls().stream()
			.forEach(url -> db.putWebpageCommit(url, fetcher.getWebpage(url)));
	}

	private static void refreshWebpagesRegex(String database, String regex) throws IOException, DBException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		db.getWebpageUrls().stream()
			.filter(Pattern.compile(regex).asPredicate())
			.forEach(url -> db.putWebpageCommit(url, fetcher.getWebpage(url)));
	}

	private static void refreshPublications(String database) throws IOException, DBException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		db.getPublicationIds().stream()
			.forEach(id -> db.putPublicationCommit(id, fetcher.getPublication(QueryLoader.onePublicationId(id))));
	}

	private static void refreshPublicationsRegex(String database, String regex) throws IOException, DBException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		db.getPublicationIds().stream()
			.filter(Pattern.compile(regex).asPredicate())
			.forEach(id -> db.putPublicationCommit(id, fetcher.getPublication(QueryLoader.onePublicationId(id))));
	}

	private static void refreshPublications(String database, Predicate<String> filter) throws IOException, DBException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		db.getPublicationIds().stream()
			.filter(filter)
			.forEach(id -> db.putPublicationCommit(id, fetcher.getPublication(QueryLoader.onePublicationId(id))));
	}

	private static void refreshAllPmid(String database) throws IOException, DBException {
		refreshPublications(database, Fetcher::isPmid);
	}

	private static void refreshAllPmcid(String database) throws IOException, DBException {
		refreshPublications(database, Fetcher::isPmcid);
	}

	private static void refreshAllDoi(String database) throws IOException, DBException {
		refreshPublications(database, Fetcher::isDoi);
	}

	private static void refreshAllDoiOfRegistrant(String database, String registrant) throws IOException, DBException {
		refreshPublications(database, id -> Fetcher.isDoi(id) && Fetcher.getDoiRegistrant(id).equals(registrant));
	}

	private static void refreshDocs(String database) throws IOException, DBException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		db.getDocUrls().stream()
			.forEach(url -> db.putDocCommit(url, fetcher.getWebpage(url)));
	}

	private static void refreshDocsRegex(String database, String regex) throws IOException, DBException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		db.getDocUrls().stream()
			.filter(Pattern.compile(regex).asPredicate())
			.forEach(url -> db.putDocCommit(url, fetcher.getWebpage(url)));
	}

	private static void printPublicationIdsFulltextNotFinal(String database, boolean html) throws FileNotFoundException, DBException {
		Database db = new Database(database);
		Set<String> set = db.getPublicationIds().stream()
			.filter(id -> !db.getPublication(id).isFulltextFinal())
			.collect(Collectors.toCollection(TreeSet::new));
		if (html) printPublicationIdHtml(set);
		else set.forEach(System.out::println);
	}

	private static void refreshAllFulltextNotFinal(String database) throws IOException, DBException {
		Database db = new Database(database);
		Fetcher fetcher = new Fetcher();
		db.getPublicationIds().stream()
			.filter(id -> !db.getPublication(id).isFulltextFinal())
			.forEach(id -> db.putPublicationCommit(id, fetcher.getPublication(QueryLoader.onePublicationId(id))));
	}

	private static void commitDatabase(String database) throws FileNotFoundException, DBException {
		new Database(database).commit();
	}
	private static void compactDatabase(String database) throws FileNotFoundException, DBException {
		new Database(database).compact();
	}

	private static void makeQueryIdf(String queryPath, QueryType type, String database, String outputPath, ProcessorMainArgs args) throws IOException, ParseException {
		ProcessorArgs processorArgs = new ProcessorArgs();
		processorArgs.setFetchingDisabled(true);
		processorArgs.setDatabase(database);
		processorArgs.setQueryIdf(null);
		processorArgs.setPreProcessorArgs(args.preProcessorArgs);

		Processor processor = new Processor(processorArgs);
		processor.makeQueryIdf(QueryLoader.get(queryPath, type), type, outputPath, args.makeQueryIdfNoWebpagesDocs, args.makeQueryIdfNoFulltext);
	}

	private static void printQueryIdfTop(String inputPath, long n) throws IOException {
		new Idf(inputPath, true).getTop().entrySet().stream()
			.limit(n).forEach(e -> System.out.println(e.getKey() + "\t" + e.getValue()));
	}

	private static void biotoolsFull(String outputPath) throws IOException {
		Path output = Output.check(outputPath, false);

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);

		Biotools biotoolsFull = new Biotools();

		String next = "?page=1";
		while (next != null) {
			Input input = new Input("https://bio.tools/api/tool" + next + "&format=json", false);

			try (InputStream is = input.newInputStream()) {
				Biotools biotools = mapper.readValue(is, Biotools.class);
				System.err.println(input.getURL());

				biotoolsFull.addTools(biotools.getList());
				biotoolsFull.setCount(biotools.getCount());

				next = biotools.getNext();
			}
		}

		mapper.writeValue(output.toFile(), biotoolsFull);
	}

	private static ProcessorMainArgs parseArgs(String[] argv) {
		ProcessorMainArgs args = new ProcessorMainArgs();
		JCommander jcommander = new JCommander(args);
		try {
			jcommander.parse(argv);
		} catch (ParameterException e) {
			System.err.println(e);
			System.err.println("Use -h or --help for listing valid options");
			System.exit(1);
		}
		if (args.help) {
			jcommander.usage();
			System.exit(0);
		}
		return args;
	}

	public static void main(String argv[]) throws IOException, ParseException, DBException, OWLOntologyCreationException {
		ProcessorMainArgs args = parseArgs(argv);

		if (args.printFetchWebpage != null) {
			printFetchWebpage(args.printFetchWebpage);
		}
		if (args.printFetchPublication != null) {
			printFetchPublication(args.printFetchPublication);
		}
		if (args.printFetchDoc != null) {
			printFetchDoc(args.printFetchDoc);
		}

		if (args.printWebpageUrlsFromFile != null) {
			printWebpageUrlsFromFile(args.printWebpageUrlsFromFile.get(0), QueryType.valueOf(args.printWebpageUrlsFromFile.get(1)));
		}
		if (args.printWebpageUrlsFromFileHtml != null) {
			printWebpageUrlsFromFileHtml(args.printWebpageUrlsFromFileHtml.get(0), QueryType.valueOf(args.printWebpageUrlsFromFileHtml.get(1)));
		}

		if (args.printPublicationIdsFromFile != null) {
			printPublicationIdsFromFile(args.printPublicationIdsFromFile.get(0), QueryType.valueOf(args.printPublicationIdsFromFile.get(1)));
		}
		if (args.printPublicationIdsFromFileHtml != null) {
			printPublicationIdsFromFileHtml(args.printPublicationIdsFromFileHtml.get(0), QueryType.valueOf(args.printPublicationIdsFromFileHtml.get(1)));
		}

		if (args.printAllDoiFromFile != null) {
			printAllDoiFromFile(args.printAllDoiFromFile.get(0), QueryType.valueOf(args.printAllDoiFromFile.get(1)), false);
		}
		if (args.printAllDoiFromFileHtml != null) {
			printAllDoiFromFile(args.printAllDoiFromFileHtml.get(0), QueryType.valueOf(args.printAllDoiFromFileHtml.get(1)), true);
		}
		if (args.printUnknownDoiFromFile != null) {
			printUnknownDoiFromFile(args.printUnknownDoiFromFile.get(0), QueryType.valueOf(args.printUnknownDoiFromFile.get(1)), false);
		}
		if (args.printUnknownDoiFromFileHtml != null) {
			printUnknownDoiFromFile(args.printUnknownDoiFromFileHtml.get(0), QueryType.valueOf(args.printUnknownDoiFromFileHtml.get(1)), true);
		}

		if (args.printDocUrlsFromFile != null) {
			printDocUrlsFromFile(args.printDocUrlsFromFile.get(0), QueryType.valueOf(args.printDocUrlsFromFile.get(1)));
		}
		if (args.printDocUrlsFromFileHtml != null) {
			printDocUrlsFromFileHtml(args.printDocUrlsFromFileHtml.get(0), QueryType.valueOf(args.printDocUrlsFromFileHtml.get(1)));
		}

		if (args.initDatabase != null) {
			initDatabase(args.initDatabase);
		}

		if (args.addWebpagesFromFile != null) {
			addWebpagesFromFile(args.addWebpagesFromFile.get(0), args.addWebpagesFromFile.get(1), QueryType.valueOf(args.addWebpagesFromFile.get(2)));
		}
		if (args.addPublicationsFromFile != null) {
			addPublicationsFromFile(args.addPublicationsFromFile.get(0), args.addPublicationsFromFile.get(1), QueryType.valueOf(args.addPublicationsFromFile.get(2)));
		}
		if (args.addDocsFromFile != null) {
			addDocsFromFile(args.addDocsFromFile.get(0), args.addDocsFromFile.get(1), QueryType.valueOf(args.addDocsFromFile.get(2)));
		}
		if (args.addAllFromFile != null) {
			addAllFromFile(args.addAllFromFile.get(0), args.addAllFromFile.get(1), QueryType.valueOf(args.addAllFromFile.get(2)));
		}

		if (args.addMissingWebpagesFromFile != null) {
			addMissingWebpagesFromFile(args.addMissingWebpagesFromFile.get(0), args.addMissingWebpagesFromFile.get(1), QueryType.valueOf(args.addMissingWebpagesFromFile.get(2)));
		}
		if (args.addMissingPublicationsFromFile != null) {
			addMissingPublicationsFromFile(args.addMissingPublicationsFromFile.get(0), args.addMissingPublicationsFromFile.get(1), QueryType.valueOf(args.addMissingPublicationsFromFile.get(2)));
		}
		if (args.addMissingDocsFromFile != null) {
			addMissingDocsFromFile(args.addMissingDocsFromFile.get(0), args.addMissingDocsFromFile.get(1), QueryType.valueOf(args.addMissingDocsFromFile.get(2)));
		}
		if (args.addAllMissingFromFile != null) {
			addAllMissingFromFile(args.addAllMissingFromFile.get(0), args.addAllMissingFromFile.get(1), QueryType.valueOf(args.addAllMissingFromFile.get(2)));
		}

		if (args.putWebpage != null) {
			putWebpage(args.putWebpage.get(0), args.putWebpage.get(1));
		}
		if (args.putPublication != null) {
			putPublication(args.putPublication.get(0), args.putPublication.get(1));
		}
		if (args.putDoc != null) {
			putDoc(args.putDoc.get(0), args.putDoc.get(1));
		}

		if (args.printWebpageUrls != null) {
			printWebpageUrls(args.printWebpageUrls);
		}
		if (args.printWebpageUrlsHtml != null) {
			printWebpageUrlsHtml(args.printWebpageUrlsHtml);
		}
		if (args.printPublicationIds != null) {
			printPublicationIds(args.printPublicationIds);
		}
		if (args.printPublicationIdsHtml != null) {
			printPublicationIdsHtml(args.printPublicationIdsHtml);
		}
		if (args.printDocUrls != null) {
			printDocUrls(args.printDocUrls);
		}
		if (args.printDocUrlsHtml != null) {
			printDocUrlsHtml(args.printDocUrlsHtml);
		}

		if (args.printWebpage != null) {
			printWebpage(args.printWebpage.get(0), args.printWebpage.get(1));
		}
		if (args.printPublication != null) {
			printPublication(args.printPublication.get(0), args.printPublication.get(1));
		}
		if (args.printDoc != null) {
			printDoc(args.printDoc.get(0), args.printDoc.get(1));
		}

		if (args.removeWebpage != null) {
			removeWebpage(args.removeWebpage.get(0), args.removeWebpage.get(1));
		}
		if (args.removePublication != null) {
			removePublication(args.removePublication.get(0), args.removePublication.get(1));
		}
		if (args.removeDoc != null) {
			removeDoc(args.removeDoc.get(0), args.removeDoc.get(1));
		}

		if (args.removeWebpages != null) {
			removeWebpages(args.removeWebpages);
		}
		if (args.removeWebpagesRegex != null) {
			removeWebpagesRegex(args.removeWebpagesRegex.get(0), args.removeWebpagesRegex.get(1));
		}

		if (args.removePublications != null) {
			removePublications(args.removePublications);
		}
		if (args.removePublicationsRegex != null) {
			removePublicationsRegex(args.removePublicationsRegex.get(0), args.removePublicationsRegex.get(1));
		}

		if (args.removeDocs != null) {
			removeDocs(args.removeDocs);
		}
		if (args.removeDocsRegex != null) {
			removeDocsRegex(args.removeDocsRegex.get(0), args.removeDocsRegex.get(1));
		}

		if (args.refreshWebpage != null) {
			refreshWebpage(args.refreshWebpage.get(0), args.refreshWebpage.get(1));
		}
		if (args.refreshPublication != null) {
			refreshPublication(args.refreshPublication.get(0), args.refreshPublication.get(1));
		}
		if (args.refreshDoc != null) {
			refreshDoc(args.refreshDoc.get(0), args.refreshDoc.get(1));
		}

		if (args.refreshWebpages != null) {
			refreshWebpages(args.refreshWebpages);
		}
		if (args.refreshWebpagesRegex != null) {
			refreshWebpagesRegex(args.refreshWebpagesRegex.get(0), args.refreshWebpagesRegex.get(1));
		}

		if (args.refreshPublications != null) {
			refreshPublications(args.refreshPublications);
		}
		if (args.refreshPublicationsRegex != null) {
			refreshPublicationsRegex(args.refreshPublicationsRegex.get(0), args.refreshPublicationsRegex.get(1));
		}

		if (args.refreshAllPmid != null) {
			refreshAllPmid(args.refreshAllPmid);
		}
		if (args.refreshAllPmcid != null) {
			refreshAllPmcid(args.refreshAllPmcid);
		}
		if (args.refreshAllDoi != null) {
			refreshAllDoi(args.refreshAllDoi);
		}
		if (args.refreshAllDoiOfRegistrant != null) {
			refreshAllDoiOfRegistrant(args.refreshAllDoiOfRegistrant.get(0), args.refreshAllDoiOfRegistrant.get(1));
		}

		if (args.refreshDocs != null) {
			refreshDocs(args.refreshDocs);
		}
		if (args.refreshDocsRegex != null) {
			refreshDocsRegex(args.refreshDocsRegex.get(0), args.refreshDocsRegex.get(1));
		}

		if (args.printPublicationIdsFulltextNotFinal != null) {
			printPublicationIdsFulltextNotFinal(args.printPublicationIdsFulltextNotFinal, false);
		}
		if (args.printPublicationIdsFulltextNotFinalHtml != null) {
			printPublicationIdsFulltextNotFinal(args.printPublicationIdsFulltextNotFinalHtml, true);
		}
		if (args.refreshAllFulltextNotFinal != null) {
			refreshAllFulltextNotFinal(args.refreshAllFulltextNotFinal);
		}

		if (args.commitDatabase != null) {
			commitDatabase(args.commitDatabase);
		}
		if (args.compactDatabase != null) {
			compactDatabase(args.compactDatabase);
		}

		if (args.makeQueryIdf != null) {
			makeQueryIdf(args.makeQueryIdf.get(0), QueryType.valueOf(args.makeQueryIdf.get(1)), args.makeQueryIdf.get(2), args.makeQueryIdf.get(3), args);
		}
		if (args.makeQueryIdfWithoutDatabase != null) {
			makeQueryIdf(args.makeQueryIdfWithoutDatabase.get(0), QueryType.valueOf(args.makeQueryIdfWithoutDatabase.get(1)), null, args.makeQueryIdfWithoutDatabase.get(2), args);
		}

		if (args.printQueryIdfTop != null) {
			printQueryIdfTop(args.printQueryIdfTop.get(0), Long.parseLong(args.printQueryIdfTop.get(1)));
		}

		if (args.biotoolsFull != null) {
			biotoolsFull(args.biotoolsFull);
		}
	}
}
