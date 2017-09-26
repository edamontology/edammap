package edammapper.input.csv;

import java.text.ParseException;

import com.univocity.parsers.annotations.Parsed;

import edammapper.input.InputType;

public class Generic implements InputType {

	@Parsed
	private String name;

	@Parsed
	private String webpageUrls;

	@Parsed
	private String description;

	@Parsed
	private String keywords;

	@Parsed
	private String publicationIds;

	@Parsed
	private String docUrls;

	@Parsed
	private String annotations;

	@Override
	public void check(int i) throws ParseException {
		if (name == null || name.equals("")) {
			parseException("name", i);
		}
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
