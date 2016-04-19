package edammapper.edam;

import java.util.ArrayList;
import java.util.List;

public class Concept {

	private String label = "";

	private boolean obsolete = false;

	private List<String> exactSynonyms = new ArrayList<>();
	private List<String> narrowSynonyms = new ArrayList<>();
	private List<String> broadSynonyms = new ArrayList<>();

	private String definition = "";
	private String comment = "";

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isObsolete() {
		return obsolete;
	}
	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
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
}
