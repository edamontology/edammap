package edammapper.fetching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.MissingResourceException;

import org.yaml.snakeyaml.Yaml;

public class DoiParse {

	private final Map<String, String> doi;

	private final Map<String, Map<String, String>> site;

	@SuppressWarnings("unchecked")
	public DoiParse() throws IOException {
		String resourceName = "doi_parse.yaml";
		InputStream resource = this.getClass().getResourceAsStream("/" + resourceName);

		if (resource != null) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
				Yaml yaml = new Yaml();
				Iterable<Object> it = yaml.loadAll(br);

				this.doi = (Map<String, String>) it.iterator().next();
				this.site = (Map<String, Map<String, String>>) it.iterator().next();

				// TODO: validate
				// * doi value has corresponding site key, and vice-versa
				// * site values' keys are from allowed list
			}
		} else {
			throw new MissingResourceException("Can't find doi sites parsing rules " + resourceName, this.getClass().getSimpleName(), resourceName);
		}
	}

	public String getSite(String doiRegistrant) {
		if (doiRegistrant == null) return null;
		return doi.get(doiRegistrant);
	}

	public String getTitle(String site) {
		if (site == null || this.site.get(site) == null) return null;
		return this.site.get(site).get("title");
	}

	public String getKeywords(String site) {
		if (site == null || this.site.get(site) == null) return null;
		return this.site.get(site).get("keywords");
	}

	public String getKeywordsSplit(String site) {
		if (site == null || this.site.get(site) == null) return null;
		return this.site.get(site).get("keywords_split");
	}

	public String getAbstract(String site) {
		if (site == null || this.site.get(site) == null) return null;
		return this.site.get(site).get("abstract");
	}

	public String getFulltext(String site) {
		if (site == null || this.site.get(site) == null) return null;
		return this.site.get(site).get("fulltext");
	}

	public String getFulltextSrc(String site) {
		if (site == null || this.site.get(site) == null) return null;
		return this.site.get(site).get("fulltext_src");
	}

	public String getFulltextDst(String site) {
		if (site == null || this.site.get(site) == null) return null;
		return this.site.get(site).get("fulltext_dst");
	}

	public String getFulltextA(String site) {
		if (site == null || this.site.get(site) == null) return null;
		return this.site.get(site).get("fulltext_a");
	}

	public String getPdfSrc(String site) {
		if (site == null || this.site.get(site) == null) return null;
		return this.site.get(site).get("pdf_src");
	}

	public String getPdfDst(String site) {
		if (site == null || this.site.get(site) == null) return null;
		return this.site.get(site).get("pdf_dst");
	}

	public String getPdfA(String site) {
		if (site == null || this.site.get(site) == null) return null;
		return this.site.get(site).get("pdf_a");
	}

	public boolean separateFulltext(String site) {
		if (site == null || this.site.get(site) == null) return false;
		Map<String, String> map = this.site.get(site);
		return (map.get("fulltext") != null && !map.get("fulltext").isEmpty()) &&
			(
			(map.get("fulltext_src") != null && !map.get("fulltext_src").isEmpty() &&
			map.get("fulltext_dst") != null && !map.get("fulltext_dst").isEmpty())
			||
			(map.get("fulltext_a") != null && !map.get("fulltext_a").isEmpty())
			);
	}
}
