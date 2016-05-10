package edammapper.input.csv;

import java.text.ParseException;

import com.opencsv.bean.CsvBind;

import edammapper.input.Input;

public class Generic implements Input {

	@CsvBind(required = true)
	private String name;

	@CsvBind
	private String webpageUrls;

	@CsvBind
	private String description;

	@CsvBind
	private String keywords;

	@CsvBind
	private String publicationIds;

	@CsvBind
	private String docUrls;

	@CsvBind
	private String annotations;

	@Override
	public void check(int i) throws ParseException {
		if (name == null || name.equals("")) {
			throw new ParseException("\"Name\" column missing or some entry in that column missing! (" + i + ")", i);
		}
		// TODO isEdamUri ?
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getWebpageUrls() {
		return webpageUrls;
	}
	public void setWebpageUrls(String webpageUrls) {
		this.webpageUrls = webpageUrls;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getPublicationIds() {
		return publicationIds;
	}
	public void setPublicationIds(String publicationIds) {
		this.publicationIds = publicationIds;
	}

	public String getDocUrls() {
		return docUrls;
	}
	public void setDocUrls(String docUrls) {
		this.docUrls = docUrls;
	}

	public String getAnnotations() {
		return annotations;
	}
	public void setAnnotations(String annotations) {
		this.annotations = annotations;
	}
}
