package edammapper.utils.seqwikimaker;

import com.opencsv.bean.CsvBind;

import edammapper.fetching.Fetcher;

public class Reference implements Input {

	@CsvBind(required = true)
	private String pubmed;

	@CsvBind(required = true)
	private String describes;

	@Override
	public void check(int i) {
		if (pubmed == null || pubmed.equals("")) {
			System.err.println("\"Pubmed\" column missing or some entry in that column missing! (" + i + ")");
		} else {
			pubmed = pubmed.trim();
			if (!isValid()) {
				System.err.println("Value in \"Pubmed\" column (" + pubmed + ") is not in a known publication ID format! (" + i + ")");
			}
		}
		if (describes == null || describes.equals("")) {
			System.err.println("\"Describes\" column missing or some entry in that column missing! (" + i + ")");
		} else {
			describes = describes.trim();
		}
	}

	public boolean isValid() {
		if (Fetcher.isPmid(pubmed) || Fetcher.isPmcid(pubmed) || Fetcher.isDoi(pubmed)) {
			return true;
		} else {
			return false;
		}
	}

	public String getPubmed() {
		return pubmed;
	}
	public void setPubmed(String pubmed) {
		this.pubmed = pubmed;
	}

	public String getDescribes() {
		return describes;
	}
	public void setDescribes(String describes) {
		this.describes = describes;
	}
}
