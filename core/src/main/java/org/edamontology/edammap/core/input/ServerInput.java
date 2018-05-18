/*
 * Copyright © 2018 Erik Jaaniso
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

package org.edamontology.edammap.core.input;

import java.text.ParseException;
import java.util.regex.Pattern;

public class ServerInput implements InputType {

	private static final Pattern CRLF = Pattern.compile("\r\n");

	private String id = null;

	private final String name;

	private final String keywords;

	private final String description;

	private final String webpageUrls;

	private final String docUrls;

	private final String publicationIds;

	private final String annotations;

	public ServerInput(String name, String keywords, String description,
			String webpageUrls,	String docUrls,	String publicationIds, String annotations) {
		this.name = crlf(name);
		this.keywords = crlf(keywords);
		this.description = crlf(description);
		this.webpageUrls = crlf(webpageUrls);
		this.docUrls = crlf(docUrls);
		this.publicationIds = crlf(publicationIds);
		this.annotations = crlf(annotations);
	}

	@Override
	public void check(int i) throws ParseException {
		if (name == null || name.equals("")) {
			parseException("name", i);
		}
	}

	private String crlf(String input) {
		if (input == null) return null;
		return CRLF.matcher(input).replaceAll("\n");
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

	public String getKeywords() {
		return keywords;
	}

	public String getDescription() {
		return description;
	}

	public String getWebpageUrls() {
		return webpageUrls;
	}

	public String getDocUrls() {
		return docUrls;
	}

	public String getPublicationIds() {
		return publicationIds;
	}

	public String getAnnotations() {
		return annotations;
	}
}
