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

public class Bioconductor implements InputType {

	@Parsed
	private String name;

	@Parsed
	private String title;

	@Parsed
	private String description;

	@Parsed
	private String biocViews;

	@Parsed
	private String reposFullUrl;

	@Parsed
	private String categories;

	@Parsed
	private String topic;

	@Parsed
	private String topic_URI;

	@Parsed
	private String operation;

	@Parsed
	private String operation_URI;

	@Override
	public void check(int i) throws ParseException {
		if (name == null || name.equals("")) {
			parseException("name", i);
		}
		if (title == null || title.equals("")) {
			parseException("title", i);
		}
		if (description == null || description.equals("")) {
			parseException("description", i);
		}
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getBiocViews() {
		return biocViews;
	}
	public void setBiocViews(String biocViews) {
		this.biocViews = biocViews;
	}

	public String getReposFullUrl() {
		return reposFullUrl;
	}
	public void setReposFullUrl(String reposFullUrl) {
		this.reposFullUrl = reposFullUrl;
	}

	public String getCategories() {
		return categories;
	}
	public void setCategories(String categories) {
		this.categories = categories;
	}

	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getTopic_URI() {
		return topic_URI;
	}
	public void setTopic_URI(String topic_URI) {
		this.topic_URI = topic_URI;
	}

	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getOperation_URI() {
		return operation_URI;
	}
	public void setOperation_URI(String operation_URI) {
		this.operation_URI = operation_URI;
	}
}
