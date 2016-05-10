package edammapper.query;

public class Keyword {

	private final String type;

	private final String value;

	private final String url;

	public Keyword(String type, String value, String url) {
		this.type = type;
		this.value = value;
		this.url = url;
	}

	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Keyword)) return false;
		Keyword other = (Keyword) obj;
		if (type == null) {
			if (other.type != null) return false;
		} else if (!type.equals(other.type)) return false;
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equals(other.value)) return false;
		if (url == null) {
			if (other.url != null) return false;
		} else if (!url.equals(other.url)) return false;
		return other.canEqual(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	public boolean canEqual(Object other) {
		return (other instanceof Keyword);
	}
}
