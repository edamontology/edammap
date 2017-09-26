package edammapper.input.json;

import java.text.ParseException;

import edammapper.input.InputType;

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
			parseException("topic", i);
		} else {
			for (int j = 0; j < topic.size(); ++j) {
				topic.get(j).check(this, i, i + ", topic " + j);
			}
		}
		if (function == null || function.isEmpty()) {
			parseException("function", i);
		} else {
			for (int j = 0; j < function.size(); ++j) {
				function.get(j).check(this, i, i + ", function " + j);
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
		// We are not checking publication.isEmpty(), as currently `"publication" : [ ]` is valid  
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
