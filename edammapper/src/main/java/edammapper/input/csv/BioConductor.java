package edammapper.input.csv;

import java.text.ParseException;

import com.opencsv.bean.CsvBind;

import edammapper.input.Input;

public class BioConductor implements Input {

	@CsvBind(required = true)
	private String name;

	@CsvBind
	private String title;

	@CsvBind
	private String description;

	@CsvBind
	private String biocViews;

	@CsvBind
	private String reposFullUrl;

	@CsvBind
	private String categories;

	@CsvBind
	private String topic;

	@CsvBind
	private String topic_URI;

	@CsvBind
	private String operation;

	@CsvBind
	private String operation_URI;

	@Override
	public void check(int i) throws ParseException {
		if (name == null || name.equals("")) {
			throw new ParseException("\"Name\" column missing or some entry in that column missing! (" + i + ")", i);
		}
		if (title == null || title.equals("")) {
			throw new ParseException("\"Title\" column missing or some entry in that column missing! (" + i + ")", i);
		}
		if (description == null || description.equals("")) {
			throw new ParseException("\"Description\" column missing or some entry in that column missing! (" + i + ")", i);
		}
		// some other columns also required?
		// TODO isEdamUri ?
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getBiocViews() {
		return biocViews;
	}
	public void setBiocViews(String biocViews) {
		this.biocViews = biocViews;
	}

	public String getReposFullUrl() {
		return reposFullUrl;
	}
	public void setReposFullUrl(String reposFullUrl) {
		this.reposFullUrl = reposFullUrl;
	}

	public String getCategories() {
		return categories;
	}
	public void setCategories(String categories) {
		this.categories = categories;
	}

	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getTopic_URI() {
		return topic_URI;
	}
	public void setTopic_URI(String topic_URI) {
		this.topic_URI = topic_URI;
	}

	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getOperation_URI() {
		return operation_URI;
	}
	public void setOperation_URI(String operation_URI) {
		this.operation_URI = operation_URI;
	}
}
