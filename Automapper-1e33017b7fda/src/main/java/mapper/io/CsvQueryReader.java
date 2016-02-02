package mapper.io;

import au.com.bytecode.opencsv.CSVReader;
import mapper.core.Keyword;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rabie Saidi
 */
public class CsvQueryReader {

    public List<Keyword> readKeywords(String fileName) {

        List<Keyword> keywords = new ArrayList<Keyword>();
        CSVReader reader = null;
        if (new File(fileName).exists()) {
            try {
                reader = new CSVReader(new FileReader(fileName));
                List<String[]> entries = reader.readAll();
                if (entries.isEmpty()) {
                    System.out.println("No query !!");
                    return keywords;
                }
                for (String[] entry : entries) {
                    String value = entry[1];
                    String parenValue = entry[2];
                    keywords.add(new Keyword(value, parenValue));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return keywords;
    }
}
