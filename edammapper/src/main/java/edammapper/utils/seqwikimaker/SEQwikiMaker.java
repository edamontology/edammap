package edammapper.utils.seqwikimaker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.IterableCSVToBean;

public class SEQwikiMaker {

	private static final String TOOLS = "http://seqanswers.com/wiki/Special:Ask/-5B-5BCategory:Bioinformatics-20application-5D-5D/-3F%3D-20Name2/-3FSoftware-20summary%3DSummary/-3FBiological-20domain%3DDomains/-3FBioinformatics-20method%3DMethods/-3FSoftware-20feature%3DFeatures/-3FInput-20format%3DInput/-3FOutput-20format%3DOutput/mainlabel%3DName/limit%3D10000/format%3Dcsv/sep%3D,/headers%3Dshow";
	private static final String REFERENCES = "http://seqanswers.com/wiki/Special:Ask/-5B-5BCategory:Reference-5D-5D/-3FPubmed-20id%3DPubmed/-3FReference-20describes%3DDescribes/limit%3D10000/format%3Dcsv/sep%3D,/headers%3Dshow";
	private static final String URLS = "http://seqanswers.com/wiki/Special:Ask/-5B-5BCategory:URL-5D-5D/-3FURL-20describes%3DDescribes/-3FURL-20type%3DType/-3FURL/limit%3D10000/format%3Dcsv/sep%3D,/headers%3Dshow";

	private static final String USER_AGENT = "Mozilla";
	private static final int TIMEOUT = 10000; // ms

	private static <T extends Input> IterableCSVToBean<T> getCsvToBean(Class<T> clazz, CSVReader csvReader) {
		HeaderColumnNameMappingStrategy<T> strategy = new HeaderColumnNameMappingStrategy<>();
		strategy.setType(clazz);
		return new IterableCSVToBean<>(csvReader, strategy, null);
	}

	private static <T extends Input> List<T> getInputs(String url, Class<T> clazz) throws MalformedURLException, IOException {
		List<T> inputs = new ArrayList<>();

		URLConnection con = new URL(url).openConnection();
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setConnectTimeout(TIMEOUT);
		con.setReadTimeout(TIMEOUT);

		try (CSVReader csvReader = new CSVReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8), ',', '"')) {
			IterableCSVToBean<T> csvToBean = getCsvToBean(clazz, csvReader);

			int i = 1; // header line
			for (T input : csvToBean) {
				input.check(++i);
				inputs.add(input);
			}
		}

		return inputs;
	}

	private static Tool getTool(List<Tool> tools, String describedAs) {
		for (Tool tool : tools) {
			if (tool.getName().equals(describedAs)) {
				return tool;
			}
		}
		return null;
	}

	public static void main(String argv[]) throws MalformedURLException, IOException {
		if (argv.length != 1) {
			System.err.println("Please provide name of output CSV!");
			System.exit(1);
		}

		List<Tool> tools = getInputs(TOOLS, Tool.class);
		List<Reference> references = getInputs(REFERENCES, Reference.class);
		List<Url> urls = getInputs(URLS, Url.class);

		for (Reference reference : references) {
			Tool tool = getTool(tools, reference.getDescribes());
			if (tool != null) {
				if (reference.isValid()) {
					tool.addPublication(reference.getPubmed());
				}
			} else {
				System.err.println("Reference (" + reference.getPubmed() + ") can't be added to non-existent tool (" + reference.getDescribes() + ")!");
			}
		}

		for (Url url : urls) {
			Tool tool = getTool(tools, url.getDescribes());
			if (tool != null) {
				if (url.isValid()) {
					if (url.isWebpage()) {
						tool.addWebpage(url.getUrl());
					} else if (url.isDoc()) {
						tool.addDoc(url.getUrl());
					}
				}
			} else {
				System.err.println("URL (" + url.getUrl() + ") can't be added to non-existent tool (" + url.getDescribes() + ")!");
			}
		}

		CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(argv[0]), StandardCharsets.UTF_8), ',', '"');

		csvWriter.writeNext(new String[]{ "Name", "Name2", "Summary", "Domains", "Methods", "Features", "Input", "Output", "Publications", "Webpages", "Docs" });
		for (Tool tool : tools) {
			csvWriter.writeNext(new String[]{ tool.getName(), tool.getName2(), tool.getSummary(), tool.getDomains(), tool.getMethods(), tool.getFeatures(), tool.getInput(), tool.getOutput(), tool.getPublications(), tool.getWebpages(), tool.getDocs() });
		}

		csvWriter.close();
	}
}
