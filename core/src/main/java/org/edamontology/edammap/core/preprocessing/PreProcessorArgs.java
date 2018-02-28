/*
 * Copyright © 2016 Erik Jaaniso
 *
 * This file is part of EDAMmap.
 *
 * EDAMmap is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EDAMmap is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EDAMmap.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.edamontology.edammap.core.preprocessing;

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
	public void setNumberRemove(boolean numberRemove) {
		this.numberRemove = numberRemove;
	}

	public Stopwords getStopwords() {
		return stopwords;
	}
	public void setStopwords(Stopwords stopwords) {
		this.stopwords = stopwords;
	}

	public boolean isNoStemming() {
		return noStemming;
	}
	public void setNoStemming(boolean noStemming) {
		this.noStemming = noStemming;
	}

	public int getShortWord() {
		return shortWord;
	}
	public void setShortWord(int shortWord) {
		this.shortWord = shortWord;
	}
}
