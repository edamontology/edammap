package edammapper.input.csv;

import java.text.ParseException;

import com.univocity.parsers.annotations.Parsed;

import edammapper.input.InputType;

public class BioConductor implements InputType {

	@Parsed
	private String name;

	@Parsed
	private String title;

	@Parsed
	private String description;

	@Parsed
	private String biocViews;

	@Parsed
	private String reposFullUrl;

	@Parsed
	private String categories;

	@Parsed
	private String topic;

	@Parsed
	private String topic_URI;

	@Parsed
	private String operation;

	@Parsed
	private String operation_URI;

	@Override
	public void check(int i) throws ParseException {
		if (name == null || name.equals("")) {
			parseException("name", i);
		}
		if (title == null || title.equals("")) {
			parseException("title", i);
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
