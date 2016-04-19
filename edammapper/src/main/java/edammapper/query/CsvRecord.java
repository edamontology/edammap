package edammapper.query;

import java.text.ParseException;

import com.opencsv.bean.CsvBind;

public class CsvRecord implements InputRecord {

	@CsvBind(required = true)
	private String query;

	@CsvBind
	private String url;

	@CsvBind
	private String matches;

	public void check(int i) throws ParseException {
		if (query == null || query.equals("")) {
			throw new ParseException("\"query\" column missing or some entry in that column missing! (" + i + ")", i);
		}
	}

	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String getMatches() {
		return matches;
	}
	public void setMatches(String matches) {
		this.matches = matches;
	}
}
