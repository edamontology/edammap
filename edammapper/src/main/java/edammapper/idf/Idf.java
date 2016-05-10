package edammapper.idf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Idf {

	private final Map<String, Double> idfMap;

	private final Map<String, Integer> idfTop;

	public Idf(Map<String, Double> idfMap) {
		this.idfMap = idfMap;
		this.idfTop = null;
	}

	public Idf(String inputPath, boolean top) throws IOException {
		try (Stream<String> lines = Files.lines(Paths.get(inputPath), StandardCharsets.UTF_8)) {
			if (top) {
				this.idfTop = lines.map(s -> s.split("\t"))
					.sorted(Collections.reverseOrder(Comparator.comparing(i -> Integer.parseInt(i[1]))))
					.collect(Collectors.toMap(s -> s[0], i -> Integer.parseInt(i[1]),
						(u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
						LinkedHashMap::new));
				this.idfMap = null;
			} else {
				this.idfMap = lines.map(s -> s.split("\t"))
					.collect(Collectors.toMap(s -> s[0], d -> Double.parseDouble(d[2])));
				this.idfTop = null;
			}
		}
	}

	public Idf(String inputPath) throws IOException {
		this(inputPath, false);
	}

	public List<Double> getIdf(List<String> terms) {
		List<Double> idfs = new ArrayList<>();
		for (String term : terms) {
			Double idf = idfMap.get(term);
			if (idf != null) idfs.add(idf);
			else idfs.add(Double.valueOf(1.0));
		}
		return idfs;
	}

	public Map<String, Integer> getTop() {
		return idfTop;
	}
}
