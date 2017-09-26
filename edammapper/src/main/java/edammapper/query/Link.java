package edammapper.query;

public class Link {

	private final String url;

	private final String type;

	public Link(String url, String type) {
		this.url = url;
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public String getType() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Link)) return false;
		Link other = (Link) obj;
		if (url == null) {
			if (other.url != null) return false;
		} else if (!url.equals(other.url)) return false;
		if (type == null) {
			if (other.type != null) return false;
		} else if (!type.equals(other.type)) return false;
		return other.canEqual(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	public boolean canEqual(Object other) {
		return (other instanceof Link);
	}
}
