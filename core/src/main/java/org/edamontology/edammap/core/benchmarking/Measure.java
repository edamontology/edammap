/*
 * Copyright © 2016, 2018 Erik Jaaniso
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

package org.edamontology.edammap.core.benchmarking;

public enum Measure {
	precision("Precision", "https://en.wikipedia.org/wiki/Precision_and_recall#Precision"),
	recall("Recall", "https://en.wikipedia.org/wiki/Precision_and_recall#Recall"),
	f1("F1 score", "https://en.wikipedia.org/wiki/F1_score"),
	f2("F2 score", "https://en.wikipedia.org/wiki/F1_score"),
	Jaccard("Jaccard index", "https://en.wikipedia.org/wiki/Jaccard_index"),
	AveP("Average precision", "https://en.wikipedia.org/wiki/Information_retrieval#Average_precision"),
	RP("R-precision", "https://en.wikipedia.org/wiki/Information_retrieval#R-Precision"),
	DCG("Discounted cumulative gain", "https://en.wikipedia.org/wiki/Discounted_cumulative_gain"),
	DCGa("DCG (alternative)", "https://en.wikipedia.org/wiki/Discounted_cumulative_gain");

	private String name;

	private String url;

	private Measure(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return name;
	}
}
