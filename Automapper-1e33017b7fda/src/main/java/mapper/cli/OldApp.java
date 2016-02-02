package mapper.cli;

import mapper.oldcore.OldMapper;
import mapper.io.TermReader;

import java.util.List;

/**
 * @author Rabie Saidi
 * Date: 09/10/2014
 * Time: 11:36
 */
public class OldApp
{
    public static void main( String[] args ){
        String queryPath = args[0];
        String referencePath = args[1];
        String synonymPath = args[2];
        TermReader reader = new TermReader();
        List<String> referenceTerms = reader.readTerms(referencePath);
        List<String> queryTerms = reader.readTerms(queryPath);
        List<String> synonymTerms = reader.readTerms(synonymPath);
        OldMapper mapper = new OldMapper(referenceTerms, queryTerms, synonymTerms);
        mapper.map();
        mapper.print();
    }



}

