package edammapper.input;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

public class Input {

	public static final String USER_AGENT = "Mozilla";
	public static final int TIMEOUT = 10000; // ms

	private String path;

	private boolean allowFile;

	private InputStream is;

	private String url;

	public Input(String path, boolean allowFile) {
		this.path = path;
		this.allowFile = allowFile;
	}

	public static boolean isProtocol(String path) {
		String pathLower = path.toLowerCase(Locale.ROOT);
		if (pathLower.startsWith("http://") || pathLower.startsWith("https://") || pathLower.startsWith("ftp://")) {
			return true;
		} else {
			return false;
		}
	}

	public InputStream newInputStream() throws IOException {
		close();
		if (isProtocol(path)) {
			URLConnection con = new URL(path).openConnection();
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setConnectTimeout(TIMEOUT);
			con.setReadTimeout(TIMEOUT);
			is = con.getInputStream();
			url = con.getURL().toString();
		} else if (allowFile) {
			is = new FileInputStream(path);
			url = path;
		} else {
			throw new IOException("Unsupported protocol or opening of local files not allowed: " + path);
		}
		return is;
	}

	public void close() throws IOException {
		if (is != null) {
			is.close();
			is = null;
			url = null;
		}
	}

	public String getPath() {
		return path;
	}
	
	public InputStream getInputStream() {
		return is;
	}

	public String getURL() {
		return url;
	}
}
