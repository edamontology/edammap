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

package org.edamontology.edammap.core.processing;

import java.util.ArrayList;
import java.util.List;

public class PublicationProcessed {

	private List<String> titleTokens = null;
	private List<Double> titleIdfs = null;

	private List<List<String>> keywordsTokens = new ArrayList<>();
	private List<List<Double>> keywordsIdfs = new ArrayList<>();

	private List<List<String>> meshTermsTokens = new ArrayList<>();
	private List<List<Double>> meshTermsIdfs = new ArrayList<>();

	private List<List<String>> efoTermsTokens = new ArrayList<>();
	private List<List<Double>> efoTermsIdfs = new ArrayList<>();
	private List<Double> efoTermFrequencies = new ArrayList<>();

	private List<List<String>> goTermsTokens = new ArrayList<>();
	private List<List<Double>> goTermsIdfs = new ArrayList<>();
	private List<Double> goTermFrequencies = new ArrayList<>();

	private List<List<String>> abstractTokens = new ArrayList<>();
	private List<List<Double>> abstractIdfs = null;

	private List<List<String>> fulltextTokens = new ArrayList<>();
	private List<List<Double>> fulltextIdfs = null;

	public List<String> getTitleTokens() {
		return titleTokens;
	}
	public void setTitleTokens(List<String> titleTokens) {
		this.titleTokens = titleTokens;
	}
	public List<Double> getTitleIdfs() {
		return titleIdfs;
	}
	public void setTitleIdfs(List<Double> titleIdfs) {
		this.titleIdfs = titleIdfs;
	}

	public List<List<String>> getKeywordsTokens() {
		return keywordsTokens;
	}
	public void addKeywordTokens(List<String> keywordTokens) {
		this.keywordsTokens.add(keywordTokens);
	}
	public List<List<Double>> getKeywordsIdfs() {
		return keywordsIdfs;
	}
	public void addKeywordIdfs(List<Double> keywordIdfs) {
		this.keywordsIdfs.add(keywordIdfs);
	}

	public List<List<String>> getMeshTermsTokens() {
		return meshTermsTokens;
	}
	public void addMeshTermTokens(List<String> meshTermTokens) {
		this.meshTermsTokens.add(meshTermTokens);
	}
	public List<List<Double>> getMeshTermsIdfs() {
		return meshTermsIdfs;
	}
	public void addMeshTermIdfs(List<Double> meshTermIdfs) {
		this.meshTermsIdfs.add(meshTermIdfs);
	}

	public List<List<String>> getEfoTermsTokens() {
		return efoTermsTokens;
	}
	public void addEfoTermTokens(List<String> efoTermTokens) {
		this.efoTermsTokens.add(efoTermTokens);
	}
	public List<List<Double>> getEfoTermsIdfs() {
		return efoTermsIdfs;
	}
	public void addEfoTermIdfs(List<Double> efoTermIdfs) {
		this.efoTermsIdfs.add(efoTermIdfs);
	}
	public List<Double> getEfoTermFrequencies() {
		return efoTermFrequencies;
	}
	public void addEfoTermFrequency(Double efoTermFrequency) {
		this.efoTermFrequencies.add(efoTermFrequency);
	}

	public List<List<String>> getGoTermsTokens() {
		return goTermsTokens;
	}
	public void addGoTermTokens(List<String> goTermTokens) {
		this.goTermsTokens.add(goTermTokens);
	}
	public List<List<Double>> getGoTermsIdfs() {
		return goTermsIdfs;
	}
	public void addGoTermIdfs(List<Double> goTermIdfs) {
		this.goTermsIdfs.add(goTermIdfs);
	}
	public List<Double> getGoTermFrequencies() {
		return goTermFrequencies;
	}
	public void addGoTermFrequency(Double goTermFrequency) {
		this.goTermFrequencies.add(goTermFrequency);
	}

	public List<List<String>> getAbstractTokens() {
		return abstractTokens;
	}
	public void addAbstractTokens(List<String> abstractTokens) {
		this.abstractTokens.add(abstractTokens);
	}
	public List<List<Double>> getAbstractIdfs() {
		return abstractIdfs;
	}
	public void addAbstractIdfs(List<Double> abstractIdfs) {
		if (this.abstractIdfs == null) {
			this.abstractIdfs = new ArrayList<>();
		}
		this.abstractIdfs.add(abstractIdfs);
	}

	public List<List<String>> getFulltextTokens() {
		return fulltextTokens;
	}
	public void addFulltextTokens(List<String> fulltextTokens) {
		this.fulltextTokens.add(fulltextTokens);
	}
	public List<List<Double>> getFulltextIdfs() {
		return fulltextIdfs;
	}
	public void addFulltextIdfs(List<Double> fulltextIdfs) {
		if (this.fulltextIdfs == null) {
			this.fulltextIdfs = new ArrayList<>();
		}
		this.fulltextIdfs.add(fulltextIdfs);
	}
}
