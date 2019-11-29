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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.edamontology.pubfetcher.core.common.IllegalRequestException;

import org.edamontology.edammap.core.input.InputType;

public class Tool implements InputType {

	private String biotoolsID;

	private String name;

	private String description;

	private String homepage;

	private List<Function> function = new ArrayList<>();

	private List<Edam> topic = new ArrayList<>();

	private List<String> language = new ArrayList<>();

	private String license;

	private List<Link<LinkType>> link = new ArrayList<>();

	private List<LinkVersion<DownloadType>> download = new ArrayList<>();

	private List<Link<DocumentationType>> documentation = new ArrayList<>();

	private List<Publication> publication = new ArrayList<>();

	private List<Credit> credit = new ArrayList<>();

	private Integer homepage_status;

	private String confidence_flag;

	private Map<String, Object> others = new LinkedHashMap<>();

	@Override
	public void check(int i) throws ParseException {
		// We are not doing any thorough validation, just checking that the required attributes are present
		// TODO add length (accounting for potential whitespace collapse) and regex validations, possibly in a separate class/package
		// if homepage_status is not null, then checked bio.tools content is probably from https://bio.tools, where biotoolsID must be present
		if (homepage_status != null && (biotoolsID == null || biotoolsID.equals(""))) {
			parseException("biotoolsID", i);
		}
		if (name == null || name.equals("")) {
			parseException("name", i);
		}
		if (description == null || description.equals("")) {
			parseException("description", i);
		}
		if (homepage == null || homepage.equals("")) {
			parseException("homepage", i);
		}
		if (function != null) {
			for (int j = 0; j < function.size(); ++j) {
				function.get(j).check(this, i, i + ", function " + j);
			}
		}
		if (topic != null) {
			for (int j = 0; j < topic.size(); ++j) {
				topic.get(j).check(this, i, i + ", topic " + j);
			}
		}
		if (link != null) {
			for (int j = 0; j < link.size(); ++j) {
				link.get(j).check(this, i, i + ", link " + j);
			}
		}
		if (download != null) {
			for (int j = 0; j < download.size(); ++j) {
				download.get(j).check(this, i, i + ", download " + j);
			}
		}
		if (documentation != null) {
			for (int j = 0; j < documentation.size(); ++j) {
				documentation.get(j).check(this, i, i + ", documentation " + j);
			}
		}
		if (publication != null) {
			for (int j = 0; j < publication.size(); ++j) {
				publication.get(j).check(this, i, i + ", publication " + j);
			}
		}
		if (credit != null) {
			for (int j = 0; j < credit.size(); ++j) {
				credit.get(j).check(this, i, i + ", credit " + j);
			}
		}
	}

	@Override
	public void parseException(String attribute, int i, String index) throws ParseException {
		if (name == null || name.equals("")) {
			InputType.super.parseException(attribute, i, index);
		} else {
			throw new ParseException("Attribute \"" + attribute + "\" missing or empty for " + name + "! (record " + index + ")", i);
		}
	}

	public static Tool fromString(String toolString) throws IOException {
		try {
			Tool tool = new ObjectMapper().readValue(toolString, Tool.class);
			tool.check(1);
			return tool;
		} catch (JsonParseException | JsonMappingException | ParseException e) {
			throw new IllegalRequestException(e);
		}
	}

	public String getBiotoolsID() {
		return biotoolsID;
	}
	public void setBiotoolsID(String id) {
		this.biotoolsID = id;
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

	public Integer getHomepage_status() {
		return homepage_status;
	}
	public void setHomepage_status(Integer homepage_status) {
		this.homepage_status = homepage_status;
	}

	public String getConfidence_flag() {
		return confidence_flag;
	}
	public void setConfidence_flag(String confidence_flag) {
		this.confidence_flag = confidence_flag;
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
