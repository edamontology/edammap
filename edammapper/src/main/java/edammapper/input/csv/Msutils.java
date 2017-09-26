package edammapper.input.csv;

import java.text.ParseException;

import com.univocity.parsers.annotations.Parsed;

import edammapper.input.InputType;

public class Msutils implements InputType {

	@Parsed
	private String name;

	@Parsed
	private String email;

	@Parsed
	private String weblink;

	@Parsed
	private String paper;

	@Parsed
	private String source;

	@Parsed
	private String category;

	@Parsed
	private String link;

	@Parsed
	private String description;

	@Parsed
	private String lang;

	@Parsed
	private String interfaces;

	@Parsed
	private String license;

	@Parsed
	private String topic;

	@Parsed
	private String operation;

	@Parsed
	private String format_in;

	@Parsed
	private String format_out;

	@Parsed
	private String comment;

	@Override
	public void check(int i) throws ParseException {
		if (name == null || name.equals("")) {
			parseException("name", i);
		}
		if (description == null || description.equals("")) {
			parseException("description", i);
		}
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getWeblink() {
		return weblink;
	}
	public void setWeblink(String weblink) {
		this.weblink = weblink;
	}

	public String getPaper() {
		return paper;
	}
	public void setPaper(String paper) {
		this.paper = paper;
	}

	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}

	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}

	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getInterfaces() {
		return interfaces;
	}
	public void setInterfaces(String interfaces) {
		this.interfaces = interfaces;
	}

	public String getLicense() {
		return license;
	}
	public void setLicense(String license) {
		this.license = license;
	}

	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getFormat_in() {
		return format_in;
	}
	public void setFormat_in(String format_in) {
		this.format_in = format_in;
	}

	public String getFormat_out() {
		return format_out;
	}
	public void setFormat_out(String format_out) {
		this.format_out = format_out;
	}

	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
}
