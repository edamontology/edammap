package mapper.io;

import com.opencsv.bean.CsvBind;

public class CsvRecord {
	@CsvBind(required = true)
	private String keyword;

	@CsvBind
	private String match;

	@CsvBind
	private String parent;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getMatch() {
		return match;
	}

	public void setMatch(String match) {
		this.match = match;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}
}
