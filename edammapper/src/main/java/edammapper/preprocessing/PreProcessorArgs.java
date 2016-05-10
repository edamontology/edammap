package edammapper.preprocessing;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

public class PreProcessorArgs {
	@Parameter(names = { "--remove-numbers" }, description = "Remove freestanding numbers (i.e., that not part of a word) as part of preprocessing")
	private boolean numberRemove = false;

	@Parameter(names = { "-s", "--stopwords" }, description = "Do stopword removal as part of preprocessing, using the chosen stopword list. Add word count in lists.")
	private Stopwords stopwords = Stopwords.lucene;

	@Parameter(names = { "--no-stemming" }, description = "Don't do stemming as part of preprocessing")
	private boolean noStemming = false;

	@Parameter(names = { "--short-word" }, validateWith = PositiveInteger.class, description = "When all preprocessing steps are done, tokens with length equal to or less of this length are removed")
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
