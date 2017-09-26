package edammapper.input.json;

import java.text.ParseException;

public class Link {

	private String url;

	private String type;

	private String comment;

	public void check(ToolInput tool, int i, String index) throws ParseException {
		if (url == null || url.equals("")) {
			tool.parseException("url", i, index);
		}
		if (type == null || type.equals("")) {
			tool.parseException("type", i, index);
		}
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
}
