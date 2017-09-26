package edammapper.input.json;

import java.text.ParseException;

public class Edam {

	private String uri;

	private String term;

	public void check(ToolInput tool, int i, String index) throws ParseException {
		if ((uri == null || uri.equals("")) && (term == null || term.equals(""))) {
			throw new ParseException("EDAM not present for " + tool.getId() + "! (record " + index + ")", i);
		}
	}

	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
}
