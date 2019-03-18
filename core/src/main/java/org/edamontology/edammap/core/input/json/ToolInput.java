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

	private String biotoolsID;

	@Override
	public void check(int i) throws ParseException {
		// We are not doing any thorough validation, just checking that the required attributes are present
		// TODO add length (accounting for potential whitespace collapse) and regex validations, possibly in a separate class/package
		if (biotoolsID == null || biotoolsID.equals("")) {
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
		if (biotoolsID == null || biotoolsID.equals("")) {
			InputType.super.parseException(attribute, i, index);
		} else {
			throw new ParseException("Attribute \"" + attribute + "\" missing or empty for " + biotoolsID + "! (record " + index + ")", i);
		}
	}

	public String getBiotoolsID() {
		return biotoolsID;
	}
	public void setBiotoolsID(String id) {
		this.biotoolsID = id;
	}
}
