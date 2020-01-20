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

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DownloadType {
	@JsonProperty("API specification")
	API_SPECIFICATION("API specification"),
	@JsonProperty("Biological data")
	BIOLOGICAL_DATA("Biological data"),
	@JsonProperty("Binaries")
	BINARIES("Binaries"),
	@JsonProperty("Command-line specification")
	COMMAND_LINE_SPECIFICATION("Command-line specification"),
	@JsonProperty("Container file")
	CONTAINER_FILE("Container file"),
	@JsonProperty("Icon")
	ICON("Icon"),
	@JsonProperty("Software package")
	SOFTWARE_PACKAGE("Software package"),
	@JsonProperty("Screenshot")
	SCREENSHOT("Screenshot"),
	@JsonProperty("Source code")
	SOURCE_CODE("Source code"),
	@JsonProperty("Test data")
	TEST_DATE("Test data"),
	@JsonProperty("Test script")
	TEST_SCRIPT("Test script"),
	@JsonProperty("Tool wrapper (CWL)")
	TOOL_WRAPPER_CWL("Tool wrapper (CWL)"),
	@JsonProperty("Tool wrapper (Galaxy)")
	TOOL_WRAPPER_GALAXY("Tool wrapper (Galaxy)"),
	@JsonProperty("Tool wrapper (Taverna)")
	TOOL_WRAPPER_TAVERNA("Tool wrapper (Taverna)"),
	@JsonProperty("Tool wrapper (Other)")
	TOOL_WRAPPER_OTHER("Tool wrapper (Other)"),
	@JsonProperty("VM image")
	VM_IMAGE("VM image"),
	@JsonProperty("Downloads page")
	DOWNLOADS_PAGE("Downloads page"),
	@JsonProperty("Other")
	OTHER("Other");

	private String type;

	private DownloadType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}
}
