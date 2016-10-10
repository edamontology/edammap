package edammapper.utils.seqwikimaker;

import com.opencsv.bean.CsvBind;

public class Tool implements Input {

	private static final String SEP = ",";

	@CsvBind(required = true)
	private String name;

	@CsvBind(required = true)
	private String name2;

	@CsvBind(required = true)
	private String summary;

	@CsvBind
	private String domains;

	@CsvBind
	private String methods;

	@CsvBind
	private String features;

	@CsvBind
	private String input;

	@CsvBind
	private String output;

	private String publications;

	private String webpages;

	private String docs;

	@Override
	public void check(int i) {
		if (name == null || name.equals("")) {
			System.err.println("\"Name\" column missing or some entry in that column missing! (" + i + ")");
		} else {
			name = name.trim();
		}
		if (name2 == null || name2.equals("")) {
			System.err.println("\"Name2\" column missing or some entry in that column missing! (" + i + ")");
		} else {
			name2 = name2.trim();
		}
		if (!name.equals(name2)) {
			System.err.println("\"Name\" and \"Name2\" columns must have equal content! (" + i + ")");
		}
		if (summary == null || summary.equals("")) {
			System.err.println("\"Summary\" column missing or some entry in that column missing! (" + i + ")");
			summary = name;
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

	public String getInput() {
		return input;
	}
	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}

	public String getPublications() {
		return publications;
	}
	public void addPublication(String publication) {
		if (publications == null || publications.isEmpty()) {
			publications = publication;
		} else {
			publications += SEP + publication;
		}
	}

	public String getWebpages() {
		return webpages;
	}
	public void addWebpage(String webpage) {
		if (webpages == null || webpages.isEmpty()) {
			webpages = webpage;
		} else {
			webpages += SEP + webpage;
		}
	}

	public String getDocs() {
		return docs;
	}
	public void addDoc(String doc) {
		if (docs == null || docs.isEmpty()) {
			docs = doc;
		} else {
			docs += SEP + doc;
		}
	}
}
