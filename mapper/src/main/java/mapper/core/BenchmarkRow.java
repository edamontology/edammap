package mapper.core;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

public class BenchmarkRow {

	public enum Result {
		TP, FP, FN
	}

	private String keyword;

	private String url;

	private String matchLabel;

	private String matchedString;

	private EdamUri match;

	private Result result;

	private double score;

	public BenchmarkRow(Keyword keyword, ComparisonResult comparisonResult, OntModel model, Result result) {
		this.keyword = keyword.getKeyword();
		this.url = keyword.getUrl();
		this.matchLabel = getLabelFromModel(comparisonResult.getMatch().getUri(), model);
		this.matchedString = comparisonResult.getReference();
		this.match = comparisonResult.getMatch();
		this.result = result;
		this.score = comparisonResult.getGlobalScore();
	}

	public BenchmarkRow(Keyword keyword, OntModel model, EdamUri match) {
		this.keyword = keyword.getKeyword();
		this.url = keyword.getUrl();
		this.matchLabel = getLabelFromModel(match.getUri(), model);
		this.matchedString = "";
		this.match = match;
		this.result = Result.FN;
		this.score = -1;
	}

	private String getLabelFromModel(String uri, OntModel model) {
		String label = "";
		OntClass ontClass = model.getOntClass(uri);
		if (ontClass != null) {
			label = ontClass.getLabel("");
		}
		return label;
	}

	public String getKeyword() {
		return keyword;
	}

	public String getUrl() {
		return url;
	}

	public String getMatchLabel() {
		return matchLabel;
	}

	public String getMatchedString() {
		return matchedString;
	}

	public EdamUri getMatch() {
		return match;
	}

	public Result getResult() {
		return result;
	}

	public double getScore() {
		return score;
	}
}
