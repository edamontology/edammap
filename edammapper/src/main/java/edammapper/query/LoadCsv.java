package edammapper.query;

import com.opencsv.CSVReader;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.IterableCSVToBean;

import edammapper.edam.EdamUri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LoadCsv {

	private static final String EDAM_PREFIX = "http://edamontology.org";
	private static final String SEQWIKI = "http://seqanswers.com/wiki/";

	private static final Pattern INTERNAL_SEPARATOR_BAR = Pattern.compile("\\|");
	private static final Pattern INTERNAL_SEPARATOR_COMMA = Pattern.compile(",");

	private static <T extends InputRecord> IterableCSVToBean<T> getCsvToBean(Class<T> clazz, CSVReader csvReader) {
		HeaderColumnNameMappingStrategy<T> strategy = new HeaderColumnNameMappingStrategy<>();
		strategy.setType(clazz);
		return new IterableCSVToBean<>(csvReader, strategy, null);
	}

	private static List<EdamUri> edamUris(String concepts) {
		return INTERNAL_SEPARATOR_BAR.splitAsStream(concepts)
			.map(s -> (s.contains("/") ? s.trim() : EDAM_PREFIX + "/" + s.trim()))
			.map(s -> new EdamUri(s.toLowerCase(Locale.ROOT), EDAM_PREFIX))
			.collect(Collectors.toList());
	}

	private static Query csv(CsvRecord csvRecord) {
		Set<EdamUri> matches = new LinkedHashSet<>();
		matches.addAll(edamUris(csvRecord.getMatches()));
		return new Query(csvRecord.getQuery(), csvRecord.getUrl(), matches);
	}

	private static Query SEQwiki(CsvSEQwiki csvSEQwiki) {
		Set<String> domains = INTERNAL_SEPARATOR_COMMA.splitAsStream(csvSEQwiki.getDomains())
			.map(s -> s.trim())
			.collect(Collectors.toCollection(LinkedHashSet::new));
		Set<String> methods = INTERNAL_SEPARATOR_COMMA.splitAsStream(csvSEQwiki.getMethods())
			.map(s -> s.trim())
			.collect(Collectors.toCollection(LinkedHashSet::new));
		return new QuerySEQwiki(csvSEQwiki.getSummary(), SEQWIKI + csvSEQwiki.getName().replace(" ", "_"), csvSEQwiki.getName(), domains, methods);
	}

	private static List<Query> SEQwikiTags(CsvSEQwiki csvSEQwiki) {
		List<Query> queries = new ArrayList<>();
		queries.addAll(INTERNAL_SEPARATOR_COMMA.splitAsStream(csvSEQwiki.getDomains())
			.map(s -> new QuerySEQwikiTags(s.trim(), SEQWIKI + s.trim().replace(" ", "_"), SEQwikiBranch.domain))
			.collect(Collectors.toList()));
		queries.addAll(INTERNAL_SEPARATOR_COMMA.splitAsStream(csvSEQwiki.getMethods())
			.map(s -> new QuerySEQwikiTags(s.trim(), SEQWIKI + s.trim().replace(" ", "_"), SEQwikiBranch.method))
			.collect(Collectors.toList()));
		return queries;
	}

	private static List<Query> SEQwikiTool(CsvSEQwiki csvSEQwiki) {
		List<Query> queries = new ArrayList<>();
		queries.addAll(INTERNAL_SEPARATOR_COMMA.splitAsStream(csvSEQwiki.getDomains())
			.map(s -> new QuerySEQwikiTool(s.trim(), SEQWIKI + s.trim().replace(" ", "_"), SEQwikiBranch.domain, csvSEQwiki.getName()))
			.collect(Collectors.toList()));
		queries.addAll(INTERNAL_SEPARATOR_COMMA.splitAsStream(csvSEQwiki.getMethods())
			.map(s -> new QuerySEQwikiTool(s.trim(), SEQWIKI + s.trim().replace(" ", "_"), SEQwikiBranch.method, csvSEQwiki.getName()))
			.collect(Collectors.toList()));
		return queries;
	}

	private static Query msutils(CsvMsutils csvMsutils) {
		Set<EdamUri> matches = new LinkedHashSet<>();
		matches.addAll(edamUris(csvMsutils.getTopic()));
		matches.addAll(edamUris(csvMsutils.getOperation()));
		matches.addAll(edamUris(csvMsutils.getFormat_in()));
		matches.addAll(edamUris(csvMsutils.getFormat_out()));
		return new QueryMsutils(csvMsutils.getDescription(), csvMsutils.getLink(), matches, csvMsutils.getName());
	}

	public static List<Query> load(String queryPath, IOType type) throws IOException, ParseException {
		if (queryPath == null || !(new File(queryPath).canRead())) {
			throw new FileNotFoundException("Query file does not exist or is not readable!");
		}

		Set<Query> queries = new LinkedHashSet<>();

		try (CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(queryPath), StandardCharsets.UTF_8), ',', '"')) {
			IterableCSVToBean<? extends InputRecord> csvToBean;
			switch (type) {
				case SEQwiki: case SEQwikiTags: case SEQwikiTool: csvToBean = getCsvToBean(CsvSEQwiki.class, csvReader); break;
				case msutils: csvToBean = getCsvToBean(CsvMsutils.class, csvReader); break;
				default: csvToBean = getCsvToBean(CsvRecord.class, csvReader); break;
			}

			int i = 1; // header line
			for (InputRecord inputRecord : csvToBean) {
				inputRecord.check(++i);
				switch (type) {
					case SEQwikiTags: queries.addAll(SEQwikiTags((CsvSEQwiki)inputRecord)); break;
					case SEQwikiTool: queries.addAll(SEQwikiTool((CsvSEQwiki)inputRecord)); break;
				default:
					Query query;
					switch (type) {
						case SEQwiki: query = SEQwiki((CsvSEQwiki)inputRecord); break;
						case msutils: query = msutils((CsvMsutils)inputRecord); break;
						default: query = csv((CsvRecord)inputRecord); break;
					}
					queries.add(query);
					break;
				}
			}
		}

		return new ArrayList<>(queries);
	}
}
