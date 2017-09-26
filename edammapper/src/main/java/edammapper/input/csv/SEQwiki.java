package edammapper.input.csv;

import java.text.ParseException;

import com.univocity.parsers.annotations.Parsed;

import edammapper.input.InputType;

public class SEQwiki implements InputType {

	@Parsed
	private String name;

	@Parsed
	private String name2;

	@Parsed
	private String summary;

	@Parsed
	private String domains;

	@Parsed
	private String methods;

	@Parsed
	private String features;

	@Parsed
	private String publications;

	@Parsed
	private String webpages;

	@Parsed
	private String docs;

	@Override
	public void check(int i) throws ParseException {
		if (name == null || name.equals("")) {
			parseException("name", i);
		}
		if (name2 == null || name2.equals("")) {
			parseException("name2", i);
		}
		if (!name.equals(name2)) {
			throw new ParseException("Columns \"name\" and \"name2\" must have equal content! (record " + i + ")", i);
		}
		if (summary == null || summary.equals("")) {
			parseException("summary", i);
		}
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getName2() {
		return name2;
	}
	public void setName2(String name2) {
		this.name2 = name2;
	}

	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDomains() {
		return domains;
	}
	public void setDomains(String domains) {
		this.domains = domains;
	}

	public String getMethods() {
		return methods;
	}
	public void setMethods(String methods) {
		this.methods = methods;
	}

	public String getFeatures() {
		return features;
	}
	public void setFeatures(String features) {
		this.features = features;
	}

	public String getPublications() {
		return publications;
	}
	public void setPublications(String publications) {
		this.publications = publications;
	}

	public String getWebpages() {
		return webpages;
	}
	public void setWebpages(String webpages) {
		this.webpages = webpages;
	}

	public String getDocs() {
		return docs;
	}
	public void setDocs(String docs) {
		this.docs = docs;
	}
}
