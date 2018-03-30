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

	public static final String NUMBERS = "numbers";
	@Parameter(names = { "--" + NUMBERS }, arity = 1, description = "Include/exclude freestanding numbers (i.e., that are not part of a word) in pre-processing")
	private boolean numbers = true;

	public static final String STOPWORDS = "stopwords";
	@Parameter(names = { "--" + STOPWORDS }, description = "Do stopwords removal as part of pre-processing, using the chosen stopwords list")
	private Stopwords stopwords = Stopwords.lucene;

	public static final String STEMMING = "stemming";
	@Parameter(names = { "--" + STEMMING }, arity = 1, description = "Do stemming as part of pre-processing")
	private boolean stemming = true;

	public static final String MIN_LENGTH = "min-length";
	@Parameter(names = { "--" + MIN_LENGTH }, validateWith = PositiveInteger.class, description = "When all pre-processing steps are done, tokens with length less to this length are removed")
	private int minLength = 1;

	public boolean isNumbers() {
		return numbers;
	}
	public void setNumbers(boolean numbers) {
		this.numbers = numbers;
	}

	public Stopwords getStopwords() {
		return stopwords;
	}
	public void setStopwords(Stopwords stopwords) {
		this.stopwords = stopwords;
	}

	public boolean isStemming() {
		return stemming;
	}
	public void setStemming(boolean stemming) {
		this.stemming = stemming;
	}

	public int getMinLength() {
		return minLength;
	}
	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}
}
