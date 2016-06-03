package edammapper.preprocessing;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

public class PreProcessorArgs {
	@Parameter(names = { "--remove-numbers" }, description = "Remove free-standing numbers (i.e., that not part of a word) as part of pre-processing")
	private boolean numberRemove = false;

	@Parameter(names = { "-s", "--stopwords" }, description = "Do stopwords removal as part of pre-processing, using the chosen stopwords list")
	private Stopwords stopwords = Stopwords.lucene;

	@Parameter(names = { "--no-stemming" }, description = "Don't do stemming as part of pre-processing")
	private boolean noStemming = false;

	@Parameter(names = { "--short-word" }, validateWith = PositiveInteger.class, description = "When all pre-processing steps are done, tokens with length less or equal to this length are removed")
	private int shortWord = 0;

	public boolean isNumberRemove() {
		return numberRemove;
	}

	public Stopwords getStopwords() {
		return stopwords;
	}

	public boolean isNoStemming() {
		return noStemming;
	}

	public int getShortWord() {
		return shortWord;
	}
}
