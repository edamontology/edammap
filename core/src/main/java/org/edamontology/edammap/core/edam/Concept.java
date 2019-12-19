/*
 * Copyright © 2016, 2017, 2019 Erik Jaaniso
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

package org.edamontology.edammap.core.edam;

import java.util.ArrayList;
import java.util.List;

public class Concept {

	private String label = null;

	private List<String> exactSynonyms = new ArrayList<>();
	private List<String> narrowSynonyms = new ArrayList<>();
	private List<String> broadSynonyms = new ArrayList<>();

	private String definition = "";
	private String comment = "";

	private List<EdamUri> directParents = new ArrayList<>();
	private List<EdamUri> directChildren = new ArrayList<>();

	private boolean obsolete = false;
	private List<EdamUri> replacedBy = new ArrayList<>();

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public List<String> getExactSynonyms() {
		return exactSynonyms;
	}
	public void addExactSynonym(String exactSynonym) {
		exactSynonyms.add(exactSynonym);
	}

	public List<String> getNarrowSynonyms() {
		return narrowSynonyms;
	}
	public void addNarrowSynonym(String narrowSynonym) {
		narrowSynonyms.add(narrowSynonym);
	}

	public List<String> getBroadSynonyms() {
		return broadSynonyms;
	}
	public void addBroadSynonym(String broadSynonym) {
		broadSynonyms.add(broadSynonym);
	}

	public String getDefinition() {
		return definition;
	}
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getComment() {
		return comment;
	}
	public void addComment(String comment) {
		if (!this.comment.isEmpty()) {
			if (this.comment.charAt(this.comment.length() - 1) != '.') {
				this.comment += ".";
			}
			this.comment += " " + comment;
		} else {
			this.comment = comment;
		}
	}

	public List<EdamUri> getDirectParents() {
		return directParents;
	}
	public void setDirectParents(List<EdamUri> directParents) {
		this.directParents = directParents;
	}

	public List<EdamUri> getDirectChildren() {
		return directChildren;
	}
	public void setDirectChildren(List<EdamUri> directChildren) {
		this.directChildren = directChildren;
	}

	public boolean isObsolete() {
		return obsolete;
	}
	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}

	public List<EdamUri> getReplacedBy() {
		return replacedBy;
	}
	public void addReplacedBy(EdamUri replacedBy) {
		this.replacedBy.add(replacedBy);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("LABEL: ").append(label).append("\n");
		sb.append("EXACT SYNONYMS: ").append(exactSynonyms).append("\n");
		sb.append("NARROW SYNONYMS: ").append(narrowSynonyms).append("\n");
		sb.append("BROAD SYNONYMS: ").append(broadSynonyms).append("\n");
		sb.append("DEFINITION: ").append(definition).append("\n");
		sb.append("COMMENT: ").append(comment).append("\n");
		sb.append("PARENTS: ").append(directParents).append("\n");
		sb.append("CHILDREN: ").append(directChildren).append("\n");
		sb.append("OBSOLETE: ").append(obsolete).append("\n");
		sb.append("REPLACED BY: ").append(replacedBy).append("\n");
		return sb.toString();
	}
}
