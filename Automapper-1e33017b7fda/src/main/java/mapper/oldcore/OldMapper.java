package mapper.oldcore;

import mapper.core.Matcher;

import java.util.*;

/**
 * @author Rabie Saidi
 * Date: 09/10/2014
 * Time: 11:51
 */
public class OldMapper {
    private List<String> referenceTerms;
    private List<String> queryTerms;
    private List<String> synonymTerms;
    private Map<String, List<OldComparisonResult>> map = new LinkedHashMap<>();


    public OldMapper(List<String> referenceTerms, List<String> queryTerms) {
        this.referenceTerms = referenceTerms;
        this.queryTerms = queryTerms;
    }

    public OldMapper(List<String> referenceTerms, List<String> queryTerms, List<String> synonymTerms) {
        this.referenceTerms = referenceTerms;
        this.queryTerms = queryTerms;
        this.synonymTerms = synonymTerms;
    }

    public void print() {
        for (Map.Entry<String, List<OldComparisonResult>> entry : map.entrySet()) {
            System.out.print(entry.getKey() + " | ");
            for(OldComparisonResult result : entry.getValue()){
                System.out.print(result.getQuery() + " | ");
            }
            System.out.println("");
            //System.out.println(entry.getValue());
            //System.out.println("_____________________");
        }
        System.out.println("");
        System.out.println("#######################");
        System.out.println("List of terms to green ");
        System.out.println("#######################");
        for(String query : queryTerms){
            boolean toGreen = false;
            for(List<OldComparisonResult> results : map.values()){
                for(OldComparisonResult result : results){
                    if(result.getQuery().equals(query) && result.getEditScore() == 1 && result.getSubstringScore() == 1){
                        toGreen = true;
                        break;
                    }
                }
                if(toGreen)break;
            }
            if(toGreen) System.out.println(query);
        }

        System.out.println("");
        System.out.println("#####################");
        System.out.println("List of terms to bar ");
        System.out.println("#####################");
        for(String query : queryTerms){
            boolean toBar = true;
            for(List<OldComparisonResult> results : map.values()){
                for(OldComparisonResult result : results){
                    if(result.getQuery().equals(query)){
                        toBar = false;
                        break;
                    }
                }
                if(!toBar)break;
            }
            if(toBar) System.out.println(query);
        }

    }

    public void map() {
        int counter =0;
        for(String reference : referenceTerms){
            map.put(reference, new ArrayList<OldComparisonResult>());
            List<OldComparisonResult> results = new ArrayList<OldComparisonResult>();
            String synonyms = synonymTerms.get(counter);
            for(String query : queryTerms){
                OldComparisonResult result = new OldComparisonResult(query, reference);
                Matcher matcher = new Matcher();
                List<OldComparisonResult> temp = new ArrayList<OldComparisonResult>();
                //Process the reference term
                result.setEditScore(matcher.editScore(query,reference));
                result.setSubstringScore(matcher.substringScore(query,reference));
                temp.add(result);

                //Process the synonyms
                String[] synonymList = synonyms.split("\\|");
                for(String synonym : synonymList){
                    OldComparisonResult synonymResult = new OldComparisonResult(query, synonym);
                    synonymResult.setEditScore(matcher.editScore(query,synonym));
                    synonymResult.setSubstringScore(matcher.substringScore(query,synonym));
                    temp.add(synonymResult);
                }
                Collections.sort(temp);
                result = temp.get(0);
                if(result.getEditScore() >= 0.4 && results.size() < 5)
                    results.add(result);
            }
            Collections.sort(results);
            map.put(reference, results);
            counter++;
        }
    }
}
