package edammapper.edam;

import java.util.ArrayList;
import java.util.List;

public class Concept {

	private boolean obsolete = false;

	private String label = ""; // TODO set to null ?

	private List<String> exactSynonyms = new ArrayList<>();
	private List<String> narrowSynonyms = new ArrayList<>();
	private List<String> broadSynonyms = new ArrayList<>();

	private String definition = "";
	private String comment = "";

	private List<EdamUri> directParents = new ArrayList<>();
	private List<EdamUri> directChildren = new ArrayList<>();

	public boolean isObsolete() {
		return obsolete;
	}
	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public List<String> getExactSynonyms() {
		return exactSynonyms;
	}
	public void addExactSynonym(String exactSynonym) {
		exactSynonyms.add(exactSynonym);
	}

	public List<String> getNarrowSynonyms() {
		return narrowSynonyms;
	}
	public void addNarrowSynonym(String narrowSynonym) {
		narrowSynonyms.add(narrowSynonym);
	}

	public List<String> getBroadSynonyms() {
		return broadSynonyms;
	}
	public void addBroadSynonym(String broadSynonym) {
		broadSynonyms.add(broadSynonym);
	}

	public String getDefinition() {
		return definition;
	}
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	public List<EdamUri> getDirectParents() {
		return directParents;
	}
	public void setDirectParents(List<EdamUri> directParents) {
		this.directParents = directParents;
	}

	public List<EdamUri> getDirectChildren() {
		return directChildren;
	}
	public void setDirectChildren(List<EdamUri> directChildren) {
		this.directChildren = directChildren;
	}
}
