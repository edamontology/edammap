/*
 * Copyright © 2017 Erik Jaaniso
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

package org.edamontology.edammap.core.input.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class Tool {

	protected String name;

	protected List<Edam> topic = new ArrayList<>();

	protected List<Function> function = new ArrayList<>();

	protected String homepage;

	protected String description;

	protected List<Link> link = new ArrayList<>();

	protected List<Link> documentation = new ArrayList<>();

	protected List<String> toolType = new ArrayList<>();

	protected List<Publication> publication = new ArrayList<>();

	protected Map<String, Object> others = new LinkedHashMap<>();

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public List<Edam> getTopic() {
		return topic;
	}
	public void setTopic(List<Edam> topic) {
		this.topic = topic;
	}

	public List<Function> getFunction() {
		return function;
	}
	public void setFunction(List<Function> function) {
		this.function = function;
	}

	public String getHomepage() {
		return homepage;
	}
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public List<Link> getLink() {
		return link;
	}
	public void setLink(List<Link> link) {
		this.link = link;
	}

	public List<Link> getDocumentation() {
		return documentation;
	}
	public void setDocumentation(List<Link> documentation) {
		this.documentation = documentation;
	}

	public List<String> getToolType() {
		return toolType;
	}
	public void setToolType(List<String> toolType) {
		this.toolType = toolType;
	}

	public List<Publication> getPublication() {
		return publication;
	}
	public void setPublication(List<Publication> publication) {
		this.publication = publication;
	}

	@JsonAnyGetter
	public Map<String, Object> getOthers() {
		return others;
	}

	@JsonAnySetter
	public void addOther(String key, Object value) {
		others.put(key, value);
	}
}
