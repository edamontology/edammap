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

package org.edamontology.edammap.core.input.csv;

import java.text.ParseException;

import org.edamontology.edammap.core.input.InputType;

import com.univocity.parsers.annotations.Parsed;

public class Generic implements InputType {

	@Parsed
	private String id;

	@Parsed
	private String name;

	@Parsed
	private String keywords;

	@Parsed
	private String description;

	@Parsed
	private String webpageUrls;

	@Parsed
	private String docUrls;

	@Parsed
	private String publicationIds;

	@Parsed
	private String annotations;

	@Override
	public void check(int i) throws ParseException {
		// TODO add more validation (https://www.univocity.com/pages/java_beans.html#validation)
		if (name == null || name.equals("")) {
			parseException("name", i);
		}
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getWebpageUrls() {
		return webpageUrls;
	}
	public void setWebpageUrls(String webpageUrls) {
		this.webpageUrls = webpageUrls;
	}

	public String getDocUrls() {
		return docUrls;
	}
	public void setDocUrls(String docUrls) {
		this.docUrls = docUrls;
	}

	public String getPublicationIds() {
		return publicationIds;
	}
	public void setPublicationIds(String publicationIds) {
		this.publicationIds = publicationIds;
	}

	public String getAnnotations() {
		return annotations;
	}
	public void setAnnotations(String annotations) {
		this.annotations = annotations;
	}
}
