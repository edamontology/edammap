package edammapper.idf;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class IdfMake {

	private Path output;

	private int documentCount;

	private Map<String, Integer> termCounts;

	private Set<String> documentTerms;

	public IdfMake() {
		documentCount = 0;
		termCounts = new TreeMap<>();
		documentTerms = new HashSet<>();
	}

	public IdfMake(String outputPath) throws IOException {
		this();

		if (outputPath == null || outputPath.isEmpty()) {
			throw new FileNotFoundException("Empty path given!");
		}
		Path path = Paths.get(outputPath);
		Path parent = (path.getParent() != null ? path.getParent() : Paths.get("."));
		if (!Files.isDirectory(parent) || !Files.isWritable(parent)) {
			throw new AccessDeniedException(parent.toAbsolutePath().normalize() + " is not a writeable directory!");
		}
		if (Files.isDirectory(path)) {
			throw new FileAlreadyExistsException(path.toAbsolutePath().normalize() + " is an existing directory!");
		}
		this.output = path;
	}

	public void addTerms(List<String> terms) {
		documentTerms.addAll(terms);
	}

	public void addTermsTerms(List<List<String>> terms) {
		terms.forEach(t -> documentTerms.addAll(t));
	}

	public void endDocument() {
		documentTerms.forEach(term -> termCounts.merge(term, 1, Integer::sum));
		documentTerms = new HashSet<>();

		++documentCount;
	}

	// no +1, as for concepts, where all words will be in IDF
	public Map<String, Double> getIdf() {
		Map<String, Double> idfMap = new HashMap<>();
		double idf_max = Math.log10(documentCount);
		for (Map.Entry<String, Integer> termCount : termCounts.entrySet()) {
			double idf = Math.log10(documentCount / (double)(termCount.getValue())) / idf_max;
			idfMap.put(termCount.getKey(), idf);
		}
		return idfMap;
	}

	// +1, as for queries, where unknown words might be queried
	public void writeOutput() throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
			double idf_max = Math.log10(documentCount);
			for (Map.Entry<String, Integer> termCount : termCounts.entrySet()) {
				writer.write(termCount.getKey());
				writer.write("\t");
				writer.write(termCount.getValue().toString());
				writer.write("\t");
				double idf = Math.log10(documentCount / (double)(termCount.getValue() + 1)) / idf_max;
				if (idf < 0) idf = 0;
				writer.write(String.valueOf(idf));
				writer.write("\n");
			}
		}
	}
}
