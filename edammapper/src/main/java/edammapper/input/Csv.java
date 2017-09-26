package edammapper.input;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import edammapper.input.csv.BioConductor;
import edammapper.input.csv.Generic;
import edammapper.input.csv.Msutils;
import edammapper.input.csv.SEQwiki;
import edammapper.query.QueryType;

public class Csv {

	public static List<InputType> load(String queryPath, QueryType type) throws IOException, ParseException {
		List<InputType> inputs = new ArrayList<>();

		BeanListProcessor<? extends InputType> rowProcessor;
		switch (type) {
			case SEQwiki: case SEQwikiTags: case SEQwikiTool: rowProcessor = new BeanListProcessor<SEQwiki>(SEQwiki.class); break;
			case msutils: rowProcessor = new BeanListProcessor<Msutils>(Msutils.class); break;
			case BioConductor: rowProcessor = new BeanListProcessor<BioConductor>(BioConductor.class); break;
			default: rowProcessor = new BeanListProcessor<Generic>(Generic.class); break;
		}
		rowProcessor.setStrictHeaderValidationEnabled(false);

		CsvParserSettings settings = new CsvParserSettings();
		settings.setProcessor(rowProcessor);
		settings.setHeaderExtractionEnabled(true);
		settings.setAutoConfigurationEnabled(true);
		settings.setReadInputOnSeparateThread(false); // disabling is (slightly) more efficient if your input is small
		settings.setSkipEmptyLines(true);
		settings.trimValues(true);
		settings.setMaxCharsPerColumn(65536);
		settings.getFormat().setDelimiter(',');
		settings.getFormat().setQuote('"');
		settings.getFormat().setQuoteEscape('"');
		settings.getFormat().setCharToEscapeQuoteEscaping('"');
		settings.getFormat().setLineSeparator("\n");
		settings.getFormat().setComment('#');

		Input input = new Input(queryPath, true);
		try (InputStreamReader reader = new InputStreamReader(input.newInputStream(), StandardCharsets.UTF_8)) {
			CsvParser parser = new CsvParser(settings);
			parser.parse(reader);
		}

		int i = 0;
		for (InputType inputType : rowProcessor.getBeans()) {
			inputType.check(++i);
			inputs.add(inputType);
		}

		return inputs;
	}
}
