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

	protected String description;

	protected String homepage;

	protected List<Function> function = new ArrayList<>();

	protected List<Edam> topic = new ArrayList<>();

	protected List<String> language = new ArrayList<>();

	protected String license;

	protected List<Link<LinkType>> link = new ArrayList<>();

	protected List<LinkVersion<DownloadType>> download = new ArrayList<>();

	protected List<Link<DocumentationType>> documentation = new ArrayList<>();

	protected List<Publication> publication = new ArrayList<>();

	protected List<Credit> credit = new ArrayList<>();

	protected Map<String, Object> others = new LinkedHashMap<>();

	public Tool trim() {
		Tool tool = new Tool();
		tool.setName(name);
		tool.setDescription(description);
		tool.setHomepage(homepage);
		tool.setFunction(function);
		tool.setTopic(topic);
		tool.setLanguage(language);
		tool.setLicense(license);
		tool.setLink(link);
		tool.setDownload(download);
		tool.setDocumentation(documentation);
		tool.setPublication(publication);
		tool.setCredit(credit);
		return tool;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getHomepage() {
		return homepage;
	}
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public List<Function> getFunction() {
		return function;
	}
	public void setFunction(List<Function> function) {
		this.function = function;
	}

	public List<Edam> getTopic() {
		return topic;
	}
	public void setTopic(List<Edam> topic) {
		this.topic = topic;
	}

	public List<String> getLanguage() {
		return language;
	}
	public void setLanguage(List<String> language) {
		this.language = language;
	}

	public String getLicense() {
		return license;
	}
	public void setLicense(String license) {
		this.license = license;
	}

	public List<Link<LinkType>> getLink() {
		return link;
	}
	public void setLink(List<Link<LinkType>> link) {
		this.link = link;
	}

	public List<LinkVersion<DownloadType>> getDownload() {
		return download;
	}
	public void setDownload(List<LinkVersion<DownloadType>> download) {
		this.download = download;
	}

	public List<Link<DocumentationType>> getDocumentation() {
		return documentation;
	}
	public void setDocumentation(List<Link<DocumentationType>> documentation) {
		this.documentation = documentation;
	}

	public List<Publication> getPublication() {
		return publication;
	}
	public void setPublication(List<Publication> publication) {
		this.publication = publication;
	}

	public List<Credit> getCredit() {
		return credit;
	}
	public void setCredit(List<Credit> credit) {
		this.credit = credit;
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
