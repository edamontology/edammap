/*
 * Copyright © 2018, 2019 Erik Jaaniso
 *
 * This file is part of PubMedApps.
 *
 * PubMedApps is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PubMedApps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PubMedApps.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.edamontology.edammap.pubmedapps;

import java.util.ArrayList;
import java.util.List;

public class Suggestion1 implements Comparable<Suggestion1> {

	protected double score = 0;

	protected String original = "";

	protected String extracted = "";

	protected String processed = "";

	protected List<String> linksAbstract = new ArrayList<>();

	protected List<String> linksFulltext = new ArrayList<>();

	protected boolean fromAbstractLink = false;

	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}

	public String getOriginal() {
		return original;
	}
	public void setOriginal(String original) {
		this.original = original;
	}

	public String getExtracted() {
		return extracted;
	}
	public void setExtracted(String extracted) {
		this.extracted = extracted;
	}

	public String getProcessed() {
		return processed;
	}
	public void setProcessed(String processed) {
		this.processed = processed;
	}

	public List<String> getLinksAbstract() {
		return linksAbstract;
	}
	public void setLinksAbstract(List<String> linksAbstract) {
		this.linksAbstract = linksAbstract;
	}
	public void addLinkAbstract(String linkAbstract) {
		linksAbstract.add(linkAbstract);
	}
	public void addLinksAbstract(List<String> linksAbstract) {
		this.linksAbstract.addAll(linksAbstract);
	}

	public List<String> getLinksFulltext() {
		return linksFulltext;
	}
	public void setLinksFulltext(List<String> linksFulltext) {
		this.linksFulltext = linksFulltext;
	}
	public void addLinkFulltext(String linkFulltext) {
		linksFulltext.add(linkFulltext);
	}
	public void addLinksFulltext(List<String> linksFulltext) {
		this.linksFulltext.addAll(linksFulltext);
	}

	public boolean isFromAbstractLink() {
		return fromAbstractLink;
	}
	public void setFromAbstractLink(boolean fromAbstractLink) {
		this.fromAbstractLink = fromAbstractLink;
	}

	@Override
	public int compareTo(Suggestion1 o) {
		if (o == null) return -1;
		if (this.score > o.score) return -1;
		if (this.score < o.score) return 1;
		return 0;
	}
}
