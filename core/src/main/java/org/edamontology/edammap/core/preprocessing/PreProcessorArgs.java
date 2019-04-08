/*
 * Copyright © 2016, 2019 Erik Jaaniso
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

import org.edamontology.pubfetcher.core.common.Arg;
import org.edamontology.pubfetcher.core.common.Args;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

public class PreProcessorArgs extends Args {

	private static final String numbersId = "numbers";
	private static final String numbersDescription = "Include/exclude freestanding numbers (i.e., that are not part of a word) in pre-processing";
	private static final Boolean numbersDefault = true;
	@Parameter(names = { "--" + numbersId }, arity = 1, description = numbersDescription)
	private Boolean numbers = numbersDefault;

	private static final String stopwordsId = "stopwords";
	private static final String stopwordsDescription = "Do stopwords removal as part of pre-processing, using the chosen stopwords list";
	private static final Stopwords stopwordsDefault = Stopwords.lucene;
	@Parameter(names = { "--" + stopwordsId }, description = stopwordsDescription)
	private Stopwords stopwords = stopwordsDefault;

	private static final String stemmingId = "stemming";
	private static final String stemmingDescription = "Do stemming as part of pre-processing";
	private static final Boolean stemmingDefault = true;
	@Parameter(names = { "--" + stemmingId }, arity = 1, description = stemmingDescription)
	private Boolean stemming = stemmingDefault;

	private static final String minLengthId = "minLength";
	private static final String minLengthDescription = "When all pre-processing steps are done, tokens with length less to this length are removed";
	private static final Integer minLengthDefault = 1;
	@Parameter(names = { "--" + minLengthId }, validateWith = PositiveInteger.class, description = minLengthDescription)
	private Integer minLength = minLengthDefault;

	@Override
	protected void addArgs() {
		args.add(new Arg<>(this::isNumbers, this::setNumbers, numbersDefault, numbersId, "Freestanding numbers", numbersDescription, null));
		args.add(new Arg<>(this::getStopwords, this::setStopwords, stopwordsDefault, stopwordsId, "Stopword list", stopwordsDescription, Stopwords.class));
		args.add(new Arg<>(this::isStemming, this::setStemming, stemmingDefault, stemmingId, "Stemming", stemmingDescription, null));
		args.add(new Arg<>(this::getMinLength, this::setMinLength, minLengthDefault, 0, null, minLengthId, "Remove shorter than", minLengthDescription, null));
	}

	@Override
	public String getId() {
		return "preProcessorArgs";
	}

	@Override
	public String getLabel() {
		return "Preprocessing";
	}

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
