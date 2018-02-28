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

import java.text.ParseException;

import org.edamontology.edammap.core.input.InputType;

public class ToolInput extends Tool implements InputType {

	private String id;

	@Override
	public void check(int i) throws ParseException {
		// We are not doing any thorough validation, just checking that the required attributes are present
		if (id == null || id.equals("")) {
			parseException("id", i);
		}
		if (name == null || name.equals("")) {
			parseException("name", i);
		}
		if (topic == null || topic.isEmpty()) {
			//parseException("topic", i); // TODO
		} else {
			for (int j = 0; j < topic.size(); ++j) {
				topic.get(j).check(this, i, i + ", topic " + j);
			}
		}
		if (function == null || function.isEmpty()) {
			parseException("function", i);
		} else {
			for (int j = 0; j < function.size(); ++j) {
				//function.get(j).check(this, i, i + ", function " + j); // TODO
			}
		}
		if (homepage == null || homepage.equals("")) {
			parseException("homepage", i);
		}
		if (description == null || description.equals("")) {
			parseException("description", i);
		}
		if (link != null) {
			for (int j = 0; j < link.size(); ++j) {
				link.get(j).check(this, i, i + ", link " + j);
			}
		}
		if (documentation != null) {
			for (int j = 0; j < documentation.size(); ++j) {
				documentation.get(j).check(this, i, i + ", documentation " + j);
			}
		}
		if (toolType == null || toolType.isEmpty()) {
			parseException("toolType", i);
		} else {
			for (int j = 0; j < toolType.size(); ++j) {
				if (toolType.get(j) == null || toolType.get(j).equals("")) {
					throw new ParseException("A \"toolType\" is null or empty for " + id + "! (record " + i + ", toolType " + j + ")", i);
				}
			}
		}
		// We are not checking publication.isEmpty(), as currently `"publication" : [ ]` is valid // TODO
		if (publication == null) {
			throw new ParseException("Attribute \"publication\" missing for " + id + "! (record " + i + ")", i);
		}
	}

	@Override
	public void parseException(String attribute, int i, String index) throws ParseException {
		if (id == null || id.equals("")) {
			InputType.super.parseException(attribute, i, index);
		} else {
			throw new ParseException("Attribute \"" + attribute + "\" missing or empty for " + id + "! (record " + index + ")", i);
		}
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
