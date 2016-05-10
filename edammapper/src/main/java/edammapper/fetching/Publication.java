package edammapper.fetching;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Publication implements Serializable {

	private static final long serialVersionUID = 3201603353790959045L;

	private static final int TITLE_MIN_LENGTH = 10;
	private static final int KEYWORDS_MIN_SIZE = 2;
	private static final int ABSTRACT_MIN_LENGTH = 500;
	private static final int FULLTEXT_MIN_LENGTH = 5000;

	private String pmid = "";
	private String pmcid = "";
	private String doi = "";
	private boolean idFinal = false;

	private String title = "";
	private boolean titleFinal = false;

	private List<String> keywords = new ArrayList<>();
	private boolean keywordsFinal = false;

	private List<MeshTerm> meshTerms = new ArrayList<>();
	private boolean meshTermsFinal = false;

	private List<MinedTerm> efoTerms = new ArrayList<>();
	private boolean efoTermsFinal = false;

	private List<MinedTerm> goTerms = new ArrayList<>();
	private boolean goTermsFinal = false;

	private String theAbstract = "";
	private boolean theAbstractFinal = false;

	private String fulltext = "";
	private boolean fulltextFinal = false;

	public boolean isEmpty() {
		return title.isEmpty() && keywords.isEmpty() &&
			meshTerms.isEmpty() && efoTerms.isEmpty() && goTerms.isEmpty() &&
			theAbstract.isEmpty() && fulltext.isEmpty();
	}

	// TODO
	public boolean isFinal() {
		return idFinal && titleFinal && keywordsFinal &&
			meshTermsFinal && efoTermsFinal && goTermsFinal &&
			theAbstractFinal && fulltextFinal;
	}

	public String getPmid() {
		return pmid;
	}
	public void setPmid(String pmid) {
		if (pmid != null && this.pmid.isEmpty()) {
			this.pmid = pmid;
			System.out.println("set pmid");
			if (!this.pmcid.isEmpty() && !this.doi.isEmpty()) {
				idFinal = true;
				System.out.println("id final");
			}
		}
	}

	public String getPmcid() {
		return pmcid;
	}
	public void setPmcid(String pmcid) {
		if (pmcid != null && this.pmcid.isEmpty()) {
			this.pmcid = pmcid;
			System.out.println("set pmcid");
			if (!this.pmid.isEmpty() && !this.doi.isEmpty()) {
				idFinal = true;
				System.out.println("id final");
			}
		}
	}

	public String getDoi() {
		return doi;
	}
	public void setDoi(String doi) {
		if (doi != null && this.doi.isEmpty()) {
			this.doi = doi;
			System.out.println("set doi");
			if (!this.pmid.isEmpty() && !this.pmcid.isEmpty()) {
				idFinal = true;
				System.out.println("id final");
			}
		}
	}

	public boolean isIdFinal() {
		return idFinal;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		if (!titleFinal && title != null && (this.title == null || this.title.length() < title.length())) {
			this.title = title;
			System.out.println("set title");
			if (this.title.length() >= TITLE_MIN_LENGTH) {
				titleFinal = true;
				System.out.println("title final");
			}
		}
	}
	public boolean isTitleFinal() {
		return titleFinal;
	}

	public List<String> getKeywords() {
		return keywords;
	}
	public void setKeywords(List<String> keywords) {
		if (!keywordsFinal && keywords != null && this.keywords.size() < keywords.size()) {
			List<String> keywordsFull = keywords.stream()
				.filter(k -> k != null && !k.trim().isEmpty())
				.collect(Collectors.toList());
			if (this.keywords.size() < keywordsFull.size()) {
				this.keywords = keywordsFull;
				System.out.println("set keywords");
				if (this.keywords.size() >= KEYWORDS_MIN_SIZE) {
					keywordsFinal = true;
					System.out.println("keywords final");
				}
			}
		}
	}
	public boolean isKeywordsFinal() {
		return keywordsFinal;
	}

	public List<MeshTerm> getMeshTerms() {
		return meshTerms;
	}
	public void setMeshTerms(List<MeshTerm> meshTerms) {
		if (!meshTermsFinal && meshTerms != null && this.meshTerms.size() < meshTerms.size()) {
			List<MeshTerm> meshTermsFull = meshTerms.stream()
				.filter(k -> k != null && k.getTerm() != null && !k.getTerm().isEmpty())
				.collect(Collectors.toList());
			if (this.meshTerms.size() < meshTermsFull.size()) {
				this.meshTerms = meshTermsFull;
				System.out.println("set mesh terms");
				if (this.meshTerms.size() >= KEYWORDS_MIN_SIZE) {
					meshTermsFinal = true;
					System.out.println("mesh terms final");
				}
			}
		}
	}
	public boolean isMeshTermsFinal() {
		return meshTermsFinal;
	}

	public List<MinedTerm> getEfoTerms() {
		return efoTerms;
	}
	public void setEfoTerms(List<MinedTerm> efoTerms) {
		if (!efoTermsFinal && efoTerms != null && this.efoTerms.size() < efoTerms.size()) {
			List<MinedTerm> efoTermsFull = efoTerms.stream()
				.filter(k -> k != null && k.getTerm() != null && !k.getTerm().isEmpty())
				.collect(Collectors.toList());
			if (this.efoTerms.size() < efoTermsFull.size()) {
				this.efoTerms = efoTermsFull;
				System.out.println("set efo terms");
				if (this.efoTerms.size() >= KEYWORDS_MIN_SIZE) {
					efoTermsFinal = true;
					System.out.println("efo terms final");
				}
			}
		}
	}
	public boolean isEfoTermsFinal() {
		return efoTermsFinal;
	}

	public List<MinedTerm> getGoTerms() {
		return goTerms;
	}
	public void setGoTerms(List<MinedTerm> goTerms) {
		if (!goTermsFinal && goTerms != null && this.goTerms.size() < goTerms.size()) {
			List<MinedTerm> goTermsFull = goTerms.stream()
				.filter(k -> k != null && k.getTerm() != null && !k.getTerm().isEmpty())
				.collect(Collectors.toList());
			if (this.goTerms.size() < goTermsFull.size()) {
				this.goTerms = goTermsFull;
				System.out.println("set go terms");
				if (this.goTerms.size() >= KEYWORDS_MIN_SIZE) {
					goTermsFinal = true;
					System.out.println("go terms final");
				}
			}
		}
	}
	public boolean isGoTermsFinal() {
		return goTermsFinal;
	}

	public String getAbstract() {
		return theAbstract;
	}
	public void setAbstract(String theAbstract) {
		if (!theAbstractFinal && theAbstract != null && (this.theAbstract == null || this.theAbstract.length() < theAbstract.length())) {
			this.theAbstract = theAbstract;
			System.out.println("set abstract");
			if (this.theAbstract.length() >= ABSTRACT_MIN_LENGTH) {
				theAbstractFinal = true;
				System.out.println("abstract final");
			}
		}
	}
	public boolean isAbstractFinal() {
		return theAbstractFinal;
	}

	public String getFulltext() {
		return fulltext;
	}
	public void setFulltext(String fulltext) {
		if (!fulltextFinal && fulltext != null && (this.fulltext == null || this.fulltext.length() < fulltext.length())) {
			this.fulltext = fulltext;
			System.out.println("set fulltext");
			if (this.fulltext.length() >= FULLTEXT_MIN_LENGTH) {
				fulltextFinal = true;
				System.out.println("fulltext final");
			}
		}
	}
	public boolean isFulltextFinal() {
		return fulltextFinal;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PMID: ").append(pmid).append("\n");
		builder.append("PMCID: ").append(pmcid).append("\n");
		builder.append("DOI: ").append(doi).append("\n\n");
		builder.append("TITLE: ").append(title).append("\n\n");
		builder.append("KEYWORDS: ").append(keywords).append("\n");
		builder.append("MESH TERMS: ").append(meshTerms).append("\n");
		builder.append("EFO TERMS: ").append(efoTerms).append("\n");
		builder.append("GO TERMS: ").append(goTerms).append("\n\n");
		builder.append("ABSTRACT: ").append(theAbstract).append("\n\n");
		builder.append("FULLTEXT: ").append(fulltext);
		return builder.toString();
	}
}
