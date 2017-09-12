package edammapper.processing;

import java.util.ArrayList;
import java.util.List;

import edammapper.edam.EdamUri;

public class ConceptProcessed {

	private boolean obsolete = false;

	private List<String> labelTokens = null;
	private List<Double> labelIdfs = null;

	private List<List<String>> exactSynonymsTokens = new ArrayList<>();
	private List<List<Double>> exactSynonymsIdfs = new ArrayList<>();

	private List<List<String>> narrowSynonymsTokens = new ArrayList<>();
	private List<List<Double>> narrowSynonymsIdfs = new ArrayList<>();

	private List<List<String>> broadSynonymsTokens = new ArrayList<>();
	private List<List<Double>> broadSynonymsIdfs = new ArrayList<>();

	private List<String> definitionTokens = null;
	private List<Double> definitionIdfs = null;

	private List<String> commentTokens = null;
	private List<Double> commentIdfs = null;

	private List<List<String>> tokens = new ArrayList<>();
	private List<List<Double>> idfs = new ArrayList<>();
	private List<Double> idfScalings = new ArrayList<>();
	private List<Double> multipliers = new ArrayList<>();

	private List<EdamUri> directParents = new ArrayList<>();
	private List<EdamUri> directChildren = new ArrayList<>();

	public boolean isObsolete() {
		return obsolete;
	}
	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}

	public List<String> getLabelTokens() {
		return labelTokens;
	}
	public void setLabelTokens(List<String> labelTokens) {
		this.labelTokens = labelTokens;
	}
	public List<Double> getLabelIdfs() {
		return labelIdfs;
	}
	public void setLabelIdfs(List<Double> labelIdfs) {
		this.labelIdfs = labelIdfs;
	}

	public List<List<String>> getExactSynonymsTokens() {
		return exactSynonymsTokens;
	}
	public void addExactSynonymTokens(List<String> exactSynonymTokens) {
		this.exactSynonymsTokens.add(exactSynonymTokens);
	}
	public List<List<Double>> getExactSynonymsIdfs() {
		return exactSynonymsIdfs;
	}
	public void addExactSynonymIdfs(List<Double> exactSynonymIdfs) {
		this.exactSynonymsIdfs.add(exactSynonymIdfs);
	}

	public List<List<String>> getNarrowSynonymsTokens() {
		return narrowSynonymsTokens;
	}
	public void addNarrowSynonymTokens(List<String> narrowSynonymTokens) {
		this.narrowSynonymsTokens.add(narrowSynonymTokens);
	}
	public List<List<Double>> getNarrowSynonymsIdfs() {
		return narrowSynonymsIdfs;
	}
	public void addNarrowSynonymIdfs(List<Double> narrowSynonymIdfs) {
		this.narrowSynonymsIdfs.add(narrowSynonymIdfs);
	}

	public List<List<String>> getBroadSynonymsTokens() {
		return broadSynonymsTokens;
	}
	public void addBroadSynonymTokens(List<String> broadSynonymTokens) {
		this.broadSynonymsTokens.add(broadSynonymTokens);
	}
	public List<List<Double>> getBroadSynonymsIdfs() {
		return broadSynonymsIdfs;
	}
	public void addBroadSynonymIdfs(List<Double> broadSynonymIdfs) {
		this.broadSynonymsIdfs.add(broadSynonymIdfs);
	}

	public List<String> getDefinitionTokens() {
		return definitionTokens;
	}
	public void setDefinitionTokens(List<String> definitionTokens) {
		this.definitionTokens = definitionTokens;
	}
	public List<Double> getDefinitionIdfs() {
		return definitionIdfs;
	}
	public void setDefinitionIdfs(List<Double> definitionIdfs) {
		this.definitionIdfs = definitionIdfs;
	}

	public List<String> getCommentTokens() {
		return commentTokens;
	}
	public void setCommentTokens(List<String> commentTokens) {
		this.commentTokens = commentTokens;
	}
	public List<Double> getCommentIdfs() {
		return commentIdfs;
	}
	public void setCommentIdfs(List<Double> commentIdfs) {
		this.commentIdfs = commentIdfs;
	}

	public List<List<String>> getTokens() {
		return tokens;
	}
	public void addTokens(List<String> tokens) {
		this.tokens.add(tokens);
	}
	public List<List<Double>> getIdfs() {
		return idfs;
	}
	public void addIdfs(List<Double> idfs) {
		this.idfs.add(idfs);
	}

	public List<Double> getIdfScalings() {
		return idfScalings;
	}
	public void addScaling(Double scaling) {
		this.idfScalings.add(scaling);
	}
	public List<Double> getMultipliers() {
		return multipliers;
	}
	public void addMultiplier(Double multiplier) {
		this.multipliers.add(multiplier);
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
