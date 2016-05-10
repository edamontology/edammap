package edammapper.input.csv;

import java.text.ParseException;

import com.opencsv.bean.CsvBind;

import edammapper.input.Input;

public class Msutils implements Input {

	@CsvBind(required = true)
	private String name;

	@CsvBind
	private String email;

	@CsvBind
	private String weblink;

	@CsvBind
	private String paper;

	@CsvBind
	private String source;

	@CsvBind
	private String category;

	@CsvBind
	private String link;

	@CsvBind(required = true)
	private String description;

	@CsvBind
	private String lang;

	@CsvBind
	private String interfaces;

	@CsvBind
	private String license;

	@CsvBind
	private String topic;

	@CsvBind
	private String operation;

	@CsvBind
	private String format_in;

	@CsvBind
	private String format_out;

	@CsvBind
	private String comment;

	@Override
	public void check(int i) throws ParseException {
		if (name == null || name.equals("")) {
			throw new ParseException("\"Name\" column missing or some entry in that column missing! (" + i + ")", i);
		}
		if (description == null || description.equals("")) {
			throw new ParseException("\"Description\" column missing or some entry in that column missing! (" + i + ")", i);
		}
		// TODO isEdamUri ?
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
