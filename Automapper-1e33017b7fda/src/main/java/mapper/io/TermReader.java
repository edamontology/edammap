package mapper.io;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

/**
 * @author Rabie Saidi
 * Date: 09/10/2014
 * Time: 13:42
 */
public class TermReader {
    public List<String> readTerms(String path) {
        List<String> list = null;
        try {
            list = Files.readAllLines(new File(path).toPath(), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
