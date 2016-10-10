package edammapper.utils.seqwikimaker;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.opencsv.bean.CsvBind;

public class Url implements Input {

	@CsvBind(required = true)
	private String describes;

	@CsvBind(required = true)
	private String type;

	@CsvBind(required = true)
	private String url;

	@Override
	public void check(int i) {
		if (describes == null || describes.equals("")) {
			System.err.println("\"Describes\" column missing or some entry in that column missing! (" + i + ")");
		} else {
			describes = describes.trim();
		}
		if (type == null || type.equals("")) {
			System.err.println("\"Type\" column missing or some entry in that column missing! (" + i + ")");
		} else {
			type = type.trim();
			if (!isWebpage() && !isDoc() && !isOther()) {
				System.err.println("Value in \"Type\" column (" + type + ") is not a known URL type! (" + i + ")");
			}
		}
		if (url == null || url.equals("")) {
			System.err.println("\"URL\" column missing or some entry in that column missing! (" + i + ")");
		} else {
			url = url.trim();
			if (!isValid()) {
				System.err.println("Value in \"URL\" column (" + url + ") is an invalid URL! (" + i + ")");
			}
		}
	}

	public boolean isWebpage() {
		if (type.equals("Homepage")
				|| type.equals("Analysis server")
				|| type.equals("")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isDoc() {
		if (type.equals("Manual")
				|| type.equals("Binaries")
				|| type.equals("Source code")
				|| type.equals("HOWTO")
				|| type.equals("Publication full text")
				|| type.equals("Description")
				|| type.equals("White Paper")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isOther() {
		if (type.equals("Mailing list")
				|| type.equals("Related")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isValid() {
		try {
			URL u = new URL(url);
			u.toURI();
		} catch (MalformedURLException | URISyntaxException e) {
			return false;
		}
		return true;
	}

	public String getDescribes() {
		return describes;
	}
	public void setDescribes(String describes) {
		this.describes = describes;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
