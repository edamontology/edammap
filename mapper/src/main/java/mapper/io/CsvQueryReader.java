package mapper.io;

import com.opencsv.CSVReader;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.IterableCSVToBean;

import mapper.core.Keyword;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CsvQueryReader {
	public Map<String, Keyword> readKeywords(String file) {
		Map<String, Keyword> keywords = new LinkedHashMap<>();
		try {
			if (file == null || !(new File(file).canRead())) {
				throw new FileNotFoundException("Query file does not exist or is not readable!");
			}
			CSVReader csvReader = new CSVReader(new FileReader(file), ',', '"');
			HeaderColumnNameMappingStrategy<CsvRecord> strategy = new HeaderColumnNameMappingStrategy<>();
			strategy.setType(CsvRecord.class);
			IterableCSVToBean<CsvRecord> csvToBean = new IterableCSVToBean<>(csvReader, strategy, null);
			for (CsvRecord csvRecord : csvToBean) {
				if (csvRecord.getKeyword() == null || csvRecord.getKeyword().equals("")) {
					throw new ParseException("\"keyword\" column missing or some entry in that column missing!", 0);
				}
				Keyword keyword = new Keyword(csvRecord.getKeyword(), csvRecord.getMatch(), csvRecord.getParent());
				keywords.merge(csvRecord.getKeyword(), keyword, Keyword::merge);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return keywords;
	}
}
