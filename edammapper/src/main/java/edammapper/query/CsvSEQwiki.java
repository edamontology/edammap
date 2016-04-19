package edammapper.query;

import java.text.ParseException;

import com.opencsv.bean.CsvBind;

public class CsvSEQwiki implements InputRecord {

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
	private String language;

	@CsvBind
	private String license;

	@CsvBind
	private String os;

	public void check(int i) throws ParseException {
		if (name == null || name.equals("")) {
			throw new ParseException("\"Name\" column missing or some entry in that column missing! (" + i + ")", i);
		}
		if (name2 == null || name2.equals("")) {
			throw new ParseException("\"Name2\" column missing or some entry in that column missing! (" + i + ")", i);
		}
		if (!name.equals(name2)) {
			throw new ParseException("\"Name\" and \"Name2\" columns must have equal content! (" + i + ")", i);
		}
		if (summary == null || summary.equals("")) {
			throw new ParseException("\"Summary\" column missing or some entry in that column missing! (" + i + ")", i);
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

	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLicense() {
		return license;
	}
	public void setLicense(String license) {
		this.license = license;
	}

	public String getOs() {
		return os;
	}
	public void setOs(String os) {
		this.os = os;
	}
}
