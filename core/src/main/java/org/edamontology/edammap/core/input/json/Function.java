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
import java.util.ArrayList;
import java.util.List;

public class Function {

	private List<Edam> operation = new ArrayList<>();

	private List<InputOutput> input = new ArrayList<>();

	private List<InputOutput> output = new ArrayList<>();

	private String comment;

	public void check(ToolInput tool, int i, String index) throws ParseException {
		if (operation == null || operation.isEmpty()) {
			tool.parseException("operation", i, index);
		} else {
			for (int j = 0; j < operation.size(); ++j) {
				operation.get(j).check(tool, i, index + ", operation " + j);
			}
		}
		if (input != null) {
			for (int j = 0; j < input.size(); ++j) {
				input.get(j).check(tool, i, index + ", input " + j);
			}
		}
		if (output != null) {
			for (int j = 0; j < output.size(); ++j) {
				output.get(j).check(tool, i, index + ", output " + j);
			}
		}
	}

	public List<Edam> getOperation() {
		return operation;
	}
	public void setOperation(List<Edam> operation) {
		this.operation = operation;
	}

	public List<InputOutput> getInput() {
		return input;
	}
	public void setInput(List<InputOutput> input) {
		this.input = input;
	}

	public List<InputOutput> getOutput() {
		return output;
	}
	public void setOutput(List<InputOutput> output) {
		this.output = output;
	}

	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
}
