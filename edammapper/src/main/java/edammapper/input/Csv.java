package edammapper.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.IterableCSVToBean;

import edammapper.input.csv.BioConductor;
import edammapper.input.csv.Generic;
import edammapper.input.csv.Msutils;
import edammapper.input.csv.SEQwiki;
import edammapper.query.QueryType;

public class Csv {

	private static <T extends Input> IterableCSVToBean<T> getCsvToBean(Class<T> clazz, CSVReader csvReader) {
		HeaderColumnNameMappingStrategy<T> strategy = new HeaderColumnNameMappingStrategy<>();
		strategy.setType(clazz);
		return new IterableCSVToBean<>(csvReader, strategy, null);
	}

	public static List<Input> load(String queryPath, QueryType type) throws IOException, ParseException {
		if (queryPath == null || !(new File(queryPath).canRead())) {
			throw new FileNotFoundException("Query file does not exist or is not readable!");
		}

		List<Input> inputs = new ArrayList<>();

		try (CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(queryPath), StandardCharsets.UTF_8), ',', '\'')) {
			IterableCSVToBean<? extends Input> csvToBean;

			switch (type) {
				case SEQwiki: case SEQwikiTags: case SEQwikiTool: csvToBean = getCsvToBean(SEQwiki.class, csvReader); break;
				case msutils: csvToBean = getCsvToBean(Msutils.class, csvReader); break;
				case BioConductor: csvToBean = getCsvToBean(BioConductor.class, csvReader); break;
				default: csvToBean = getCsvToBean(Generic.class, csvReader); break;
			}

			int i = 1; // header line
			for (Input input : csvToBean) {
				input.check(++i);
				inputs.add(input);
			}
		}

		return inputs;
	}
}
