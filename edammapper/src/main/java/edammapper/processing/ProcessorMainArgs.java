package edammapper.processing;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import edammapper.preprocessing.PreProcessorArgs;

public class ProcessorMainArgs {
	@Parameter(names = { "-h", "--help" }, help = true, description = "Print this help")
	boolean help;

	@Parameter(names = { "--print-fetch-webpage" }, description = "String webpageUrl")
	String printFetchWebpage = null;

	@Parameter(names = { "--print-fetch-publication" }, description = "String publicationId")
	String printFetchPublication = null;

	@Parameter(names = { "--print-fetch-doc" }, description = "String docUrl")
	String printFetchDoc = null;

	@Parameter(names = { "--print-webpage-urls-from-file" }, arity = 2, description = "String queryPath, QueryType type")
	List<String> printWebpageUrlsFromFile = null;

	@Parameter(names = { "--print-webpage-urls-from-file-html" }, arity = 2, description = "String queryPath, QueryType type")
	List<String> printWebpageUrlsFromFileHtml = null;

	@Parameter(names = { "--print-publication-ids-from-file" }, arity = 2, description = "String queryPath, QueryType type")
	List<String> printPublicationIdsFromFile = null;

	@Parameter(names = { "--print-publication-ids-from-file-html" }, arity = 2, description = "String queryPath, QueryType type")
	List<String> printPublicationIdsFromFileHtml = null;

	@Parameter(names = { "--print-all-doi-from-file" }, arity = 2, description = "String queryPath, QueryType type")
	List<String> printAllDoiFromFile = null;

	@Parameter(names = { "--print-all-doi-from-file-html" }, arity = 2, description = "String queryPath, QueryType type")
	List<String> printAllDoiFromFileHtml = null;

	@Parameter(names = { "--print-unknown-doi-from-file" }, arity = 2, description = "String queryPath, QueryType type")
	List<String> printUnknownDoiFromFile = null;

	@Parameter(names = { "--print-unknown-doi-from-file-html" }, arity = 2, description = "String queryPath, QueryType type")
	List<String> printUnknownDoiFromFileHtml = null;

	@Parameter(names = { "--print-doc-urls-from-file" }, arity = 2, description = "String queryPath, QueryType type")
	List<String> printDocUrlsFromFile = null;

	@Parameter(names = { "--print-doc-urls-from-file-html" }, arity = 2, description = "String queryPath, QueryType type")
	List<String> printDocUrlsFromFileHtml = null;

	@Parameter(names = { "--init-database" }, description = "String database")
	String initDatabase = null;

	@Parameter(names = { "--add-webpages-from-file" }, arity = 3, description = "String database, String queryPath, QueryType type")
	List<String> addWebpagesFromFile = null;

	@Parameter(names = { "--add-publications-from-file" }, arity = 3, description = "String database, String queryPath, QueryType type")
	List<String> addPublicationsFromFile = null;

	@Parameter(names = { "--add-docs-from-file" }, arity = 3, description = "String database, String queryPath, QueryType type")
	List<String> addDocsFromFile = null;

	@Parameter(names = { "--add-all-from-file" }, arity = 3, description = "String database, String queryPath, QueryType type")
	List<String> addAllFromFile = null;

	@Parameter(names = { "--add-missing-webpages-from-file" }, arity = 3, description = "String database, String queryPath, QueryType type")
	List<String> addMissingWebpagesFromFile = null;

	@Parameter(names = { "--add-missing-publications-from-file" }, arity = 3, description = "String database, String queryPath, QueryType type")
	List<String> addMissingPublicationsFromFile = null;

	@Parameter(names = { "--add-missing-docs-from-file" }, arity = 3, description = "String database, String queryPath, QueryType type")
	List<String> addMissingDocsFromFile = null;

	@Parameter(names = { "--add-all-missing-from-file" }, arity = 3, description = "String database, String queryPath, QueryType type")
	List<String> addAllMissingFromFile = null;

	@Parameter(names = { "--put-webpage" }, arity = 2, description = "String database, String webpageUrl")
	List<String> putWebpage = null;

	@Parameter(names = { "--put-publication" }, arity = 2, description = "String database, String publicationId")
	List<String> putPublication = null;

	@Parameter(names = { "--put-doc" }, arity = 2, description = "String database, String docUrl")
	List<String> putDoc = null;

	@Parameter(names = { "--print-webpage-urls" }, description = "String database")
	String printWebpageUrls = null;

	@Parameter(names = { "--print-webpage-urls-html" }, description = "String database")
	String printWebpageUrlsHtml = null;

	@Parameter(names = { "--print-publication-ids" }, description = "String database")
	String printPublicationIds = null;

	@Parameter(names = { "--print-publication-ids-html" }, description = "String database")
	String printPublicationIdsHtml = null;

	@Parameter(names = { "--print-doc-urls" }, description = "String database")
	String printDocUrls = null;

	@Parameter(names = { "--print-doc-urls-html" }, description = "String database")
	String printDocUrlsHtml = null;

	@Parameter(names = { "--print-webpage" }, arity = 2, description = "String database, String webpageUrl")
	List<String> printWebpage = null;

	@Parameter(names = { "--print-publication" }, arity = 2, description = "String database, String publicationId")
	List<String> printPublication = null;

	@Parameter(names = { "--print-doc" }, arity = 2, description = "String database, String docUrl")
	List<String> printDoc = null;

	@Parameter(names = { "--remove-webpage" }, arity = 2, description = "String database, String webpageUrl")
	List<String> removeWebpage = null;

	@Parameter(names = { "--remove-publication" }, arity = 2, description = "String database, String publicationId")
	List<String> removePublication = null;

	@Parameter(names = { "--remove-doc" }, arity = 2, description = "String database, String docUrl")
	List<String> removeDoc = null;

	@Parameter(names = { "--remove-webpages" }, description = "String database")
	String removeWebpages = null;

	@Parameter(names = { "--remove-webpages-regex" }, arity = 2, description = "String database, String regex")
	List<String> removeWebpagesRegex = null;

	@Parameter(names = { "--remove-publications" }, description = "String database")
	String removePublications = null;

	@Parameter(names = { "--remove-publications-regex" }, arity = 2, description = "String database, String regex")
	List<String> removePublicationsRegex = null;

	@Parameter(names = { "--remove-docs" }, description = "String database")
	String removeDocs = null;

	@Parameter(names = { "--remove-docs-regex" }, arity = 2, description = "String database, String regex")
	List<String> removeDocsRegex = null;

	@Parameter(names = { "--refresh-webpage" }, arity = 2, description = "String database, String webpageUrl")
	List<String> refreshWebpage = null;

	@Parameter(names = { "--refresh-publication" }, arity = 2, description = "String database, String publicationId")
	List<String> refreshPublication = null;

	@Parameter(names = { "--refresh-doc" }, arity = 2, description = "String database, String docUrl")
	List<String> refreshDoc = null;

	@Parameter(names = { "--refresh-webpages" }, description = "String database")
	String refreshWebpages = null;

	@Parameter(names = { "--refresh-webpages-regex" }, arity = 2, description = "String database, String regex")
	List<String> refreshWebpagesRegex = null;

	@Parameter(names = { "--refresh-publications" }, description = "String database")
	String refreshPublications = null;

	@Parameter(names = { "--refresh-publications-regex" }, arity = 2, description = "String database, String regex")
	List<String> refreshPublicationsRegex = null;

	@Parameter(names = { "--refresh-all-pmid" }, description = "String database")
	String refreshAllPmid = null;

	@Parameter(names = { "--refresh-all-pmcid" }, description = "String database")
	String refreshAllPmcid = null;

	@Parameter(names = { "--refresh-all-doi" }, description = "String database")
	String refreshAllDoi = null;

	@Parameter(names = { "--refresh-all-doi-of-registrant" }, arity = 2, description = "String database, String registrant")
	List<String> refreshAllDoiOfRegistrant = null;

	@Parameter(names = { "--refresh-docs" }, description = "String database")
	String refreshDocs = null;

	@Parameter(names = { "--refresh-docs-regex" }, arity = 2, description = "String database, String regex")
	List<String> refreshDocsRegex = null;

	@Parameter(names = { "--print-publication-ids-fulltext-not-final" }, description = "String database")
	String printPublicationIdsFulltextNotFinal = null;

	@Parameter(names = { "--print-publication-ids-fulltext-not-final-html" }, description = "String database")
	String printPublicationIdsFulltextNotFinalHtml = null;

	@Parameter(names = { "--refresh-all-fulltext-not-final" }, description = "String database")
	String refreshAllFulltextNotFinal = null;

	@Parameter(names = { "--commit-database" }, description = "String database")
	String commitDatabase = null;

	@Parameter(names = { "--compact-database" }, description = "String database")
	String compactDatabase = null;

	@Parameter(names = { "--make-query-idf" }, arity = 4, description = "String queryPath, QueryType type, String database, String outputPath")
	List<String> makeQueryIdf = null;

	@Parameter(names = { "--make-query-idf-without-database" }, arity = 3, description = "String queryPath, QueryType type, String outputPath")
	List<String> makeQueryIdfWithoutDatabase = null;

	@Parameter(names = { "--make-query-idf-no-webpages-docs" }, description = "")
	boolean makeQueryIdfNoWebpagesDocs = false;

	@Parameter(names = { "--make-query-idf-no-fulltext" }, description = "")
	boolean makeQueryIdfNoFulltext = false;

	@Parameter(names = { "--print-query-idf-top" }, arity = 2, description = "String inputPath, int n")
	List<String> printQueryIdfTop = null;

	@ParametersDelegate
	PreProcessorArgs preProcessorArgs = new PreProcessorArgs();
}
