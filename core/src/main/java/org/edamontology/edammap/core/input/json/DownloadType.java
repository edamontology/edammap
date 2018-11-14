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

package org.edamontology.edammap.core.input.json;

public enum DownloadType {
	API_SPECIFICATION("API specification"),
	BIOLOGICAL_DATA("Biological data"),
	BINARIES("Binaries"),
	BINARY_PACKAGE("Binary package"),
	COMMAND_LINE_SPECIFICATION("Command-line specification"),
	CONTAINER_FILE("Container file"),
	CWL_FILE("CWL file"),
	ICON("Icon"),
	ONTOLOGY("Ontology"),
	SCREENSHOT("Screenshot"),
	SOURCE_CODE("Source code"),
	SOURCE_PACKAGE("Source package"),
	TEST_DATE("Test data"),
	TEST_SCRIPT("Test script"),
	TOOL_WRAPPER_GALAXY("Tool wrapper (galaxy)"),
	TOOL_WRAPPER_TAVERNA("Tool wrapper (taverna)"),
	TOOL_WRAPPER_OTHER("Tool wrapper (other)"),
	VM_IMAGE("VM image");

	private String type;

	private DownloadType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}
}
