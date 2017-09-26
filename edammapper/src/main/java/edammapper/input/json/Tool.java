package edammapper.input.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class Tool {

	protected String name;

	protected List<Edam> topic = new ArrayList<>();

	protected List<Function> function = new ArrayList<>();

	protected String homepage;

	protected String description;

	protected List<Link> link = new ArrayList<>();

	protected List<Link> documentation = new ArrayList<>();

	protected List<String> toolType = new ArrayList<>();

	protected List<Publication> publication = new ArrayList<>();

	protected Map<String, Object> others = new LinkedHashMap<>();

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public List<Edam> getTopic() {
		return topic;
	}
	public void setTopic(List<Edam> topic) {
		this.topic = topic;
	}

	public List<Function> getFunction() {
		return function;
	}
	public void setFunction(List<Function> function) {
		this.function = function;
	}

	public String getHomepage() {
		return homepage;
	}
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public List<Link> getLink() {
		return link;
	}
	public void setLink(List<Link> link) {
		this.link = link;
	}

	public List<Link> getDocumentation() {
		return documentation;
	}
	public void setDocumentation(List<Link> documentation) {
		this.documentation = documentation;
	}

	public List<String> getToolType() {
		return toolType;
	}
	public void setToolType(List<String> toolType) {
		this.toolType = toolType;
	}

	public List<Publication> getPublication() {
		return publication;
	}
	public void setPublication(List<Publication> publication) {
		this.publication = publication;
	}

	@JsonAnyGetter
	public Map<String, Object> getOthers() {
		return others;
	}

	@JsonAnySetter
	public void addOther(String key, Object value) {
		others.put(key, value);
	}
}
