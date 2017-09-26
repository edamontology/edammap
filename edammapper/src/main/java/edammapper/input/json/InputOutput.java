package edammapper.input.json;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class InputOutput {

	private Edam data;

	private List<Edam> format = new ArrayList<>();

	public void check(ToolInput tool, int i, String index) throws ParseException {
		if (data == null) {
			tool.parseException("data", i, index);
		} else {
			data.check(tool, i, index);
		}
		if (format != null) {
			for (int j = 0; j < format.size(); ++j) {
				format.get(j).check(tool, i, index + ", format " + j);
			}
		}
	}

	public Edam getData() {
		return data;
	}
	public void setData(Edam data) {
		this.data = data;
	}

	public List<Edam> getFormat() {
		return format;
	}
	public void setFormat(List<Edam> format) {
		this.format = format;
	}
}
