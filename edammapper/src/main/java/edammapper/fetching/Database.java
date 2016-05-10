package edammapper.fetching;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Set;

import org.mapdb.DB;
import org.mapdb.DBException;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

public class Database {

	private final DB db;

	private final HTreeMap<String, String> webpages;

	private final HTreeMap<String, Publication> publications;

	private final HTreeMap<String, String> docs;

	@SuppressWarnings("unchecked")
	public Database(String database) throws FileNotFoundException, DBException {
		if (database == null || !(new File(database).canRead())) {
			throw new FileNotFoundException("Database file does not exist or is not readable!");
		}

		this.db = DBMaker.fileDB(database).closeOnJvmShutdown().transactionEnable().make();

		this.webpages = db.hashMap("webpages", Serializer.STRING, Serializer.STRING).open();
		this.publications = db.hashMap("publications", Serializer.STRING, Serializer.JAVA).open();
		this.docs = db.hashMap("docs", Serializer.STRING, Serializer.STRING).open();
	}

	@SuppressWarnings("unchecked")
	public static void init(String database) throws FileAlreadyExistsException, DBException {
		if (database == null || new File(database).exists()) {
			throw new FileAlreadyExistsException(database);
		}

		DB db = DBMaker.fileDB(database).closeOnJvmShutdown().transactionEnable().make();

		db.hashMap("webpages", Serializer.STRING, Serializer.STRING).create();
		db.hashMap("publications", Serializer.STRING, Serializer.JAVA).create();
		db.hashMap("docs", Serializer.STRING, Serializer.STRING).create();

		db.commit();
	}

	public String putWebpage(String webpageUrl, String webpage) {
		return webpages.put(webpageUrl, webpage);
	}

	public boolean putWebpageCommit(String webpageUrl, String webpage) {
		if (webpage == null) return false;
		webpages.put(webpageUrl, webpage);
		db.commit();
		return true;
	}

	public Publication putPublication(String publicationId, Publication publication) {
		return publications.put(publicationId, publication);
	}

	public boolean putPublicationCommit(String publicationId, Publication publication) {
		if (publication == null) return false;
		publications.put(publicationId, publication);
		db.commit();
		return true;
	}

	public String putDoc(String docUrl, String doc) {
		return docs.put(docUrl, doc);
	}

	public boolean putDocCommit(String docUrl, String doc) {
		if (doc == null) return false;
		docs.put(docUrl, doc);
		db.commit();
		return true;
	}

	public String removeWebpage(String webpageUrl) {
		return webpages.remove(webpageUrl);
	}

	public Publication removePublication(String publicationId) {
		return publications.remove(publicationId);
	}

	public String removeDoc(String docUrl) {
		return docs.remove(docUrl);
	}

	public boolean containsWebpage(String webpageUrl) {
		return webpages.containsKey(webpageUrl);
	}

	public boolean containsPublication(String publicationId) {
		return publications.containsKey(publicationId);
	}

	public boolean containsDoc(String docUrl) {
		return docs.containsKey(docUrl);
	}

	@SuppressWarnings("unchecked")
	public Set<String> getWebpageUrls() {
		return webpages.keySet();
	}

	@SuppressWarnings("unchecked")
	public Set<String> getPublicationIds() {
		return publications.keySet();
	}

	@SuppressWarnings("unchecked")
	public Set<String> getDocUrls() {
		return docs.keySet();
	}

	public String getWebpage(String webpageUrl) {
		return webpages.get(webpageUrl);
	}

	public Publication getPublication(String publicationId) {
		return publications.get(publicationId);
	}

	public String getDoc(String docUrl) {
		return docs.get(docUrl);
	}

	public void commit() {
		db.commit();
	}

	public void compact() {
		db.getStore().compact();
	}
}
