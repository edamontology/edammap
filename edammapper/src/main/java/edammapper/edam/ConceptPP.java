package edammapper.edam;

import java.util.ArrayList;
import java.util.List;

import edammapper.preprocessing.PreProcessor;

public class ConceptPP {

	private List<String> label = null;

	private boolean obsolete = false;

	private List<List<String>> exactSynonyms = new ArrayList<>();
	private List<List<String>> narrowSynonyms = new ArrayList<>();
	private List<List<String>> broadSynonyms = new ArrayList<>();

	private List<String> definition = null;
	private List<String> comment = null;

	public ConceptPP(Concept concept, PreProcessor pp) {
		this.label = pp.process(concept.getLabel());
		this.obsolete = concept.isObsolete();
		concept.getExactSynonyms().forEach(s -> this.exactSynonyms.add(pp.process(s)));
		concept.getNarrowSynonyms().forEach(s -> this.narrowSynonyms.add(pp.process(s)));
		concept.getBroadSynonyms().forEach(s -> this.broadSynonyms.add(pp.process(s)));
		this.definition = pp.process(concept.getDefinition());
		this.comment = pp.process(concept.getComment());
	}

	public List<String> getLabel() {
		return label;
	}

	public boolean isObsolete() {
		return obsolete;
	}

	public List<List<String>> getExactSynonyms() {
		return exactSynonyms;
	}

	public List<List<String>> getNarrowSynonyms() {
		return narrowSynonyms;
	}

	public List<List<String>> getBroadSynonyms() {
		return broadSynonyms;
	}

	public List<String> getDefinition() {
		return definition;
	}

	public List<String> getComment() {
		return comment;
	}
}
