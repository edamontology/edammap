/*
 * Copyright © 2016, 2017 Erik Jaaniso
 *
 * This file is part of EDAMmap.
 *
 * EDAMmap is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EDAMmap is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EDAMmap.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.edamontology.edammap.core.input;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.edamontology.edammap.core.input.csv.Bioconductor;
import org.edamontology.edammap.core.input.csv.Generic;
import org.edamontology.edammap.core.input.csv.Msutils;
import org.edamontology.edammap.core.input.csv.SEQwiki;
import org.edamontology.edammap.core.query.QueryType;

public class Csv {

	public static List<InputType> load(String queryPath, QueryType type, int timeout, String userAgent) throws IOException, ParseException {
		List<InputType> inputs = new ArrayList<>();

		BeanListProcessor<? extends InputType> rowProcessor;
		switch (type) {
			case SEQwiki: rowProcessor = new BeanListProcessor<SEQwiki>(SEQwiki.class); break;
			case msutils: rowProcessor = new BeanListProcessor<Msutils>(Msutils.class); break;
			case Bioconductor: rowProcessor = new BeanListProcessor<Bioconductor>(Bioconductor.class); break;
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
		settings.setMaxCharsPerColumn(100000);
		settings.getFormat().setDelimiter(',');
		settings.getFormat().setQuote('"');
		settings.getFormat().setQuoteEscape('"');
		settings.getFormat().setCharToEscapeQuoteEscaping('"');
		settings.getFormat().setLineSeparator("\n");
		settings.getFormat().setComment('#');

		try (InputStreamReader reader = new InputStreamReader(Input.newInputStream(queryPath, true, timeout, userAgent), StandardCharsets.UTF_8)) {
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
