package edammapper.input.xml;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import edammapper.input.InputType;

public class Biotools14 implements InputType {

	private String name;

	private String homepage;

	private List<String> mirrors;

	private String description;

	private List<String> topics;

	private List<String> functionNames;

	private List<String> dataTypes;

	private List<String> dataFormats;

	private String docsHome;

	private String docsGithub;

	private String publicationsPrimaryID;

	private List<String> publicationsOtherIDs;

	public Biotools14() {
		this.mirrors = new ArrayList<>();
		this.topics = new ArrayList<>();
		this.functionNames = new ArrayList<>();
		this.dataTypes = new ArrayList<>();
		this.dataFormats = new ArrayList<>();
		this.publicationsOtherIDs = new ArrayList<>();
	}

	@Override
	public void check(int i) throws ParseException {
		// We could check mandatory attributes here
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getHomepage() {
		return homepage;
	}
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public List<String> getMirrors() {
		return mirrors;
	}
	public void addMirror(String mirror) {
		this.mirrors.add(mirror);
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getTopics() {
		return topics;
	}
	public void addTopic(String topic) {
		this.topics.add(topic);
	}

	public List<String> getFunctionNames() {
		return functionNames;
	}
	public void addFunctionName(String functionName) {
		this.functionNames.add(functionName);
	}

	public List<String> getDataTypes() {
		return dataTypes;
	}
	public void addDataType(String dataType) {
		this.dataTypes.add(dataType);
	}

	public List<String> getDataFormats() {
		return dataFormats;
	}
	public void addDataFormat(String dataFormat) {
		this.dataFormats.add(dataFormat);
	}

	public String getDocsHome() {
		return docsHome;
	}
	public void setDocsHome(String docsHome) {
		this.docsHome = docsHome;
	}

	public String getDocsGithub() {
		return docsGithub;
	}
	public void setDocsGithub(String docsGithub) {
		this.docsGithub = docsGithub;
	}

	public String getPublicationsPrimaryID() {
		return publicationsPrimaryID;
	}
	public void setPublicationsPrimaryID(String publicationsPrimaryID) {
		this.publicationsPrimaryID = publicationsPrimaryID;
	}

	public List<String> getPublicationsOtherIDs() {
		return publicationsOtherIDs;
	}
	public void addPublicationsOtherID(String publicationsOtherID) {
		this.publicationsOtherIDs.add(publicationsOtherID);
	}
}
