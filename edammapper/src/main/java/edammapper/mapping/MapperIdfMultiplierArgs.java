package edammapper.mapping;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

import edammapper.args.PositiveDouble;
import edammapper.args.ZeroToOneDouble;
import edammapper.edam.Branch;

public class MapperIdfMultiplierArgs {
	@Parameter(names = { "--concept-idf-scaling" }, validateWith = PositiveDouble.class, description = "Set to 0 to disable concept IDF. Setting to 1 means linear IDF weighting.")
	private double conceptIdfScaling = 0.5;

	@Parameter(names = { "--query-idf-scaling" }, validateWith = PositiveDouble.class, description = "Set to 0 to disable query IDF. Setting to 1 means linear IDF weighting.")
	private double queryIdfScaling = 0.5;

	@Parameter(names = { "--enable-label-synonyms-idf" }, description = "Enable IDF weighting for concept label and synonyms")
	private boolean enableLabelSynonymsIdf = false;

	@Parameter(names = { "--disable-name-keywords-idf" }, description = "Disable IDF weighting for query name and keywords")
	private boolean disableNameKeywordsIdf = false;

	@Parameter(names = { "--disable-description-idf" }, description = "Disable IDF weighting for query description")
	private boolean disableDescriptionIdf = false;

	@Parameter(names = { "--disable-title-keywords-idf" }, description = "Disable IDF weighting for publication title and keywords")
	private boolean disableTitleKeywordsIdf = false;

	@Parameter(names = { "--disable-abstract-idf" }, description = "Disable IDF weighting for publication abstract")
	private boolean disableAbstractIdf = false;

	// TODO a more general solution to have all parameters specifiable on branch bases
	@Parameter(names = { "--disable-query-idf-branches" }, variableArity = true, description = "Branches to disable query IDF in. Space separated from list [topic, operation, data, format].")
	private List<Branch> disableQueryIdfBranches = new ArrayList<>();

	@Parameter(names = { "--label-multiplier" }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a concept label. Set to 0 to disable matching of labels.")
	private double labelMultiplier = 1;

	@Parameter(names = { "--exact-synonym-multiplier" }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a concept exact synonym. Set to 0 to disable matching of exact synonyms.")
	private double exactSynonymMultiplier = 1;

	@Parameter(names = { "--narrow-broad-synonym-multiplier" }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a concept narrow or broad synonym. Set to 0 to disable matching of narrow and broad synonyms.")
	private double narrowBroadMultiplier = 1;

	@Parameter(names = { "--definition-multiplier" }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a concept definition. Set to 0 to disable matching of definitions.")
	private double definitionMultiplier = 1;

	@Parameter(names = { "--comment-multiplier" }, validateWith = ZeroToOneDouble.class, description = "Score multiplier for matching a concept comment. Set to 0 to disable matching of comments.")
	private double commentMultiplier = 1;

	@Parameter(names = { "--mapping-strategy" }, description = "Choose the best or take the average of query parts matches")
	private MapperStrategy mappingStrategy = MapperStrategy.average;

	@Parameter(names = { "--name-normalizer" }, validateWith = ZeroToOneDouble.class, description = "Score normalizer for matching a query name. Set to 0 to disable matching of names.")
	private double nameNormalizer = 0.81;

	@Parameter(names = { "--keyword-normalizer" }, validateWith = ZeroToOneDouble.class, description = "Score normalizer for matching a query keyword. Set to 0 to disable matching of keywords.")
	private double keywordNormalizer = 0.77;

	@Parameter(names = { "--description-normalizer" }, validateWith = ZeroToOneDouble.class, description = "Score normalizer for matching a query description. Set to 0 to disable matching of descriptions.")
	private double descriptionNormalizer = 0.92;

	@Parameter(names = { "--publication-title-normalizer" }, validateWith = ZeroToOneDouble.class, description = "Score normalizer for matching a publication title. Set to 0 to disable matching of titles.")
	private double publicationTitleNormalizer = 0.91;

	@Parameter(names = { "--publication-keyword-normalizer" }, validateWith = ZeroToOneDouble.class, description = "Score normalizer for matching a publication keyword. Set to 0 to disable matching of keywords.")
	private double publicationKeywordNormalizer = 0.77;

	@Parameter(names = { "--publication-mesh-normalizer" }, validateWith = ZeroToOneDouble.class, description = "Score normalizer for matching a publication MeSH term. Set to 0 to disable matching of MeSH terms.")
	private double publicationMeshNormalizer = 0.75;

	@Parameter(names = { "--publication-mined-term-normalizer" }, validateWith = ZeroToOneDouble.class, description = "Score normalizer for matching a publication mined term (EFO, GO). Set to 0 to disable matching of mined terms.")
	private double publicationMinedNormalizer = 1;

	@Parameter(names = { "--publication-abstract-normalizer" }, validateWith = ZeroToOneDouble.class, description = "Score normalizer for matching a publication abstract. Set to 0 to disable matching of abstracts.")
	private double publicationAbstractNormalizer = 0.985;

	@Parameter(names = { "--publication-fulltext-normalizer" }, validateWith = ZeroToOneDouble.class, description = "Score normalizer for matching a publication fulltext. Set to 0 to disable matching of fulltexts.")
	private double publicationFulltextNormalizer = 1;

	@Parameter(names = { "--doc-normalizer" }, validateWith = ZeroToOneDouble.class, description = "Score normalizer for matching a query doc. Set to 0 to disable matching of docs.")
	private double docNormalizer = 1;

	@Parameter(names = { "--webpage-normalizer" }, validateWith = ZeroToOneDouble.class, description = "Score normalizer for matching a query webpage. Set to 0 to disable matching of webpages.")
	private double webpageNormalizer = 1;

	@Parameter(names = { "--average-scaling" }, validateWith = PositiveDouble.class, description = "Scaling for the average strategy")
	private double averageScaling = 10;

	@Parameter(names = { "--name-weight" }, validateWith = PositiveDouble.class, description = "Weight of query name in average strategy. Set to 0 to disable matching of names in average strategy.")
	private double nameWeight = 1;

	@Parameter(names = { "--keyword-weight" }, validateWith = PositiveDouble.class, description = "Weight of query keyword in average strategy. Set to 0 to disable matching of keywords in average strategy.")
	private double keywordWeight = 1;

	@Parameter(names = { "--description-weight" }, validateWith = PositiveDouble.class, description = "Weight of query description in average strategy. Set to 0 to disable matching of descriptions in average strategy.")
	private double descriptionWeight = 1;

	@Parameter(names = { "--publication-title-weight" }, validateWith = PositiveDouble.class, description = "Weight of publication title in average strategy. Set to 0 to disable matching of titles in average strategy.")
	private double publicationTitleWeight = 0.25;

	@Parameter(names = { "--publication-keyword-weight" }, validateWith = PositiveDouble.class, description = "Weight of publication keyword in average strategy. Set to 0 to disable matching of keywords in average strategy.")
	private double publicationKeywordWeight = 0.75;

	@Parameter(names = { "--publication-mesh-weight" }, validateWith = PositiveDouble.class, description = "Weight of publication MeSH term in average strategy. Set to 0 to disable matching of MeSH terms in average strategy.")
	private double publicationMeshWeight = 0.25;

	@Parameter(names = { "--publication-mined-term-weight" }, validateWith = PositiveDouble.class, description = "Weight of publication mined term (EFO, GO) in average strategy. Set to 0 to disable matching of mined terms in average strategy.")
	private double publicationMinedWeight = 0.25;

	@Parameter(names = { "--publication-abstract-weight" }, validateWith = PositiveDouble.class, description = "Weight of publication abstract in average strategy. Set to 0 to disable matching of abstracts in average strategy.")
	private double publicationAbstractWeight = 0.75;

	@Parameter(names = { "--publication-fulltext-weight" }, validateWith = PositiveDouble.class, description = "Weight of publication fulltext in average strategy. Set to 0 to disable matching of fulltexts in average strategy.")
	private double publicationFulltextWeight = 0.5;

	@Parameter(names = { "--doc-weight" }, validateWith = PositiveDouble.class, description = "Weight of query doc in average strategy. Set to 0 to disable matching of docs in average strategy.")
	private double docWeight = 0.5;

	@Parameter(names = { "--webpage-weight" }, validateWith = PositiveDouble.class, description = "Weight of query webpage in average strategy. Set to 0 to disable matching of webpages in average strategy.")
	private double webpageWeight = 0.5;

	public double getConceptIdfScaling() {
		return conceptIdfScaling;
	}

	public double getQueryIdfScaling() {
		return queryIdfScaling;
	}

	public boolean isEnableLabelSynonymsIdf() {
		return enableLabelSynonymsIdf;
	}

	public boolean isDisableNameKeywordsIdf() {
		return disableNameKeywordsIdf;
	}

	public boolean isDisableDescriptionIdf() {
		return disableDescriptionIdf;
	}

	public boolean isDisableTitleKeywordsIdf() {
		return disableTitleKeywordsIdf;
	}

	public boolean isDisableAbstractIdf() {
		return disableAbstractIdf;
	}

	public List<Branch> getDisableQueryIdfBranches() {
		return disableQueryIdfBranches;
	}

	public double getLabelMultiplier() {
		return labelMultiplier;
	}

	public double getExactSynonymMultiplier() {
		return exactSynonymMultiplier;
	}

	public double getNarrowBroadMultiplier() {
		return narrowBroadMultiplier;
	}

	public double getDefinitionMultiplier() {
		return definitionMultiplier;
	}

	public double getCommentMultiplier() {
		return commentMultiplier;
	}

	public MapperStrategy getMappingStrategy() {
		return mappingStrategy;
	}

	public double getNameNormalizer() {
		return nameNormalizer;
	}

	public double getKeywordNormalizer() {
		return keywordNormalizer;
	}

	public double getDescriptionNormalizer() {
		return descriptionNormalizer;
	}

	public double getPublicationTitleNormalizer() {
		return publicationTitleNormalizer;
	}

	public double getPublicationKeywordNormalizer() {
		return publicationKeywordNormalizer;
	}

	public double getPublicationMeshNormalizer() {
		return publicationMeshNormalizer;
	}

	public double getPublicationMinedNormalizer() {
		return publicationMinedNormalizer;
	}

	public double getPublicationAbstractNormalizer() {
		return publicationAbstractNormalizer;
	}

	public double getPublicationFulltextNormalizer() {
		return publicationFulltextNormalizer;
	}

	public double getDocNormalizer() {
		return docNormalizer;
	}

	public double getWebpageNormalizer() {
		return webpageNormalizer;
	}

	public double getAverageScaling() {
		return averageScaling;
	}

	public double getNameWeight() {
		return nameWeight;
	}

	public double getKeywordWeight() {
		return keywordWeight;
	}

	public double getDescriptionWeight() {
		return descriptionWeight;
	}

	public double getPublicationTitleWeight() {
		return publicationTitleWeight;
	}

	public double getPublicationKeywordWeight() {
		return publicationKeywordWeight;
	}

	public double getPublicationMeshWeight() {
		return publicationMeshWeight;
	}

	public double getPublicationMinedWeight() {
		return publicationMinedWeight;
	}

	public double getPublicationAbstractWeight() {
		return publicationAbstractWeight;
	}

	public double getPublicationFulltextWeight() {
		return publicationFulltextWeight;
	}

	public double getDocWeight() {
		return docWeight;
	}

	public double getWebpageWeight() {
		return webpageWeight;
	}
}
