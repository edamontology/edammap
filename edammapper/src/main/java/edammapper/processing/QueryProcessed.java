package edammapper.processing;

import java.util.ArrayList;
import java.util.List;

import edammapper.fetching.Publication;

public class QueryProcessed {

	private List<String> nameTokens = null;
	private List<Double> nameIdfs = null;

	private List<List<String>> webpagesTokens = new ArrayList<>();
	private List<List<Double>> webpagesIdfs = new ArrayList<>();

	private List<String> descriptionTokens = null;
	private List<Double> descriptionIdfs = null;

	private List<List<String>> keywordsTokens = new ArrayList<>();
	private List<List<Double>> keywordsIdfs = new ArrayList<>();

	private List<Publication> publications = new ArrayList<>();
	private List<PublicationProcessed> processedPublications = new ArrayList<>();

	private List<List<String>> docsTokens = new ArrayList<>();
	private List<List<Double>> docsIdfs = new ArrayList<>();

	public List<String> getNameTokens() {
		return nameTokens;
	}
	public void setNameTokens(List<String> nameTokens) {
		this.nameTokens = nameTokens;
	}
	public List<Double> getNameIdfs() {
		return nameIdfs;
	}
	public void setNameIdfs(List<Double> nameIdfs) {
		this.nameIdfs = nameIdfs;
	}

	public List<List<String>> getWebpagesTokens() {
		return webpagesTokens;
	}
	public void addWebpageTokens(List<String> webpageTokens) {
		this.webpagesTokens.add(webpageTokens);
	}
	public List<List<Double>> getWebpagesIdfs() {
		return webpagesIdfs;
	}
	public void addWebpageIdfs(List<Double> webpageIdfs) {
		this.webpagesIdfs.add(webpageIdfs);
	}

	public List<String> getDescriptionTokens() {
		return descriptionTokens;
	}
	public void setDescriptionTokens(List<String> descriptionTokens) {
		this.descriptionTokens = descriptionTokens;
	}
	public List<Double> getDescriptionIdfs() {
		return descriptionIdfs;
	}
	public void setDescriptionIdfs(List<Double> descriptionIdfs) {
		this.descriptionIdfs = descriptionIdfs;
	}

	public List<List<String>> getKeywordsTokens() {
		return keywordsTokens;
	}
	public void addKeywordTokens(List<String> keywordTokens) {
		this.keywordsTokens.add(keywordTokens);
	}
	public List<List<Double>> getKeywordsIdfs() {
		return keywordsIdfs;
	}
	public void addKeywordIdfs(List<Double> keywordIdfs) {
		this.keywordsIdfs.add(keywordIdfs);
	}

	public List<Publication> getPublications() {
		return publications;
	}
	public void addPublication(Publication publication) {
		this.publications.add(publication);
	}
	public List<PublicationProcessed> getProcessedPublications() {
		return processedPublications;
	}
	public void addProcessedPublication(PublicationProcessed processedPublication) {
		this.processedPublications.add(processedPublication);
	}

	public List<List<String>> getDocsTokens() {
		return docsTokens;
	}
	public void addDocTokens(List<String> docTokens) {
		this.docsTokens.add(docTokens);
	}
	public List<List<Double>> getDocsIdfs() {
		return docsIdfs;
	}
	public void addDocIdfs(List<Double> docIdfs) {
		this.docsIdfs.add(docIdfs);
	}
}
