package mapper.oldcore;

import mapper.core.*;

import java.util.*;

/**
 * @author Rabie Saidi
 * Date: 09/10/2014
 * Time: 11:51
 */
public class OldMapper3 {

    private final List<Concept> concepts;
    private final List<String> queryTerms;
    private  List<ComparisonResult> results;
    private Map<String, List<ComparisonResult>> map = new LinkedHashMap<>();



//    public Mapper2(List<OntClass> ontClasses, List<String> queryTerms) {
//        //this.model = model;
//        this.ontClasses = ontClasses;
//        this.queryTerms = queryTerms;
//        this.results = new ArrayList<>();
//    }

    public OldMapper3(List<Concept> concepts, List<String> queryTerms) {
        //this.model = model;
        this.concepts = concepts;
        this.queryTerms = queryTerms;
        this.results = new ArrayList<>();
    }




    public void map() {
        int classCount = 0;
        for(Concept concept : concepts){
            classCount++;
            //System.out.println("Concept " + classCount);
            int termCount = 0;

            for(String term : queryTerms){
                termCount++;
                //System.out.println("Concept " + classCount + " - Term " + termCount);
                List<ComparisonResult> referenceResults = new ArrayList<>();
                List<ComparisonResult> exactSynonymResults = new ArrayList<>();
                List<ComparisonResult> narrowSynonymResults = new ArrayList<>();
                List<ComparisonResult> broadSynonymResults = new ArrayList<>();

                ComparisonResult referenceResult = getResult(term, concept.getLabel(), concept, MatchType.LABEL);
                if(referenceResult.getEditScore() >= 0.2)referenceResults.add(referenceResult);

                //Process exact synonyms
                for(String statement : concept.getExactSynonyms()){
                    ComparisonResult exactSynonymResult = getResult(term, statement, concept, MatchType.EXACT_SYNONYM);
                    if(exactSynonymResult.getEditScore() >= 0.2) exactSynonymResults.add(exactSynonymResult);
                }

                //Process narrow synonyms
                for(String statement : concept.getNarrowSynonyms()){
                    ComparisonResult narrowSynonymResult = getResult(term, statement, concept, MatchType.NARROW_SYNONYM);
                    if(narrowSynonymResult.getEditScore() >= 0.2) narrowSynonymResults.add(narrowSynonymResult);

                }

                //Process broad synonyms
                for(String statement : concept.getBroadSynonyms()){
                    ComparisonResult broadSynonymResult = getResult(term, statement, concept, MatchType.BROAD_SYNONYM);
                    if(broadSynonymResult.getEditScore() >= 0.2) broadSynonymResults.add(broadSynonymResult);
                }

                List<ComparisonResult> temp = new ArrayList<>();
                if(map.containsKey(term)) temp.addAll(map.get(term));
                temp.addAll(referenceResults);
                temp.addAll(exactSynonymResults);
                temp.addAll(narrowSynonymResults);
                temp.addAll(broadSynonymResults);
                //Collections.sort(temp);
                map.put(term, temp);
            }
        }
        for (Map.Entry<String, List<ComparisonResult>> entry : map.entrySet()) {
            map.put(entry.getKey(), bestOf(entry.getValue()));
        }
    }

    private List<ComparisonResult> bestOf(List<ComparisonResult> comparisonResults) {
        Collections.sort(comparisonResults);
        List<ComparisonResult> temp = new ArrayList<>();
        List<BranchType> branches = new ArrayList<>();
        int count = 0;
        int i = 0;
        while(count < 5 && count < comparisonResults.size() && i < comparisonResults.size()){
            if(i == 0){
                temp.add(comparisonResults.get(i));
                branches.add(temp.get(temp.size() - 1).getBranch());
                count++;
            }
            else{
                if(comparisonResults.get(i).compareTo(comparisonResults.get(i-1)) == 0
                        && !branches.contains(comparisonResults.get(i).getBranch())){
                    temp.add(comparisonResults.get(i));
                    if(!branches.contains(temp.get(temp.size() - 1).getBranch())){
                        branches.add(temp.get(temp.size() - 1).getBranch());
                    }
                    count++;
                }
            }
            i++;
        }

        //At least one output line is required for every input keyword/phrase, even in cases where no match was found.
        //Where matches are identified to more than 1 branch, the best match can be reported for each branch,
        //i.e. a maximum of 4 matches per keyword/phrase
        Iterator<ComparisonResult> iterator = temp.iterator();
        int index =0;
        while(iterator.hasNext()){
            ComparisonResult result = iterator.next();
            if(result.getGlobalScore() < 0.3 && index != 0){
                iterator.remove();
            }
            index++;
        }

        return temp;
    }

    private ComparisonResult getResult(String query, String reference, Concept ontClass, MatchType matchType){
        ComparisonResult result = new ComparisonResult(query, reference);
        Matcher matcher = new Matcher();
        result.setEditScore(matcher.editScore(query.toLowerCase().trim(),reference.toLowerCase().trim()));
        result.setSubstringScore(matcher.substringScore(query.toLowerCase().trim(),reference.toLowerCase().trim()));

        //uri
        result.setUri(ontClass.getUri());
        //branch
        if (!ontClass.getUri().equals("##########")) {
            String uri = ontClass.getUri();
            BranchType branch;
//            if(uri.contains("topic"))branch = BranchType.TOPIC;
//            else if(uri.contains("operation"))branch = BranchType.OPERATION;
//            else if(uri.contains("data"))branch = BranchType.DATA;
//            else if(uri.contains("format"))branch = BranchType.FORMAT;
//            else branch = BranchType.OTHER;
//            result.setBranch(branch);
        }
        else{
//            result.setBranch(BranchType.OTHER);
        }
        //obsolete
        if(ontClass.isObsolete()) result.setObselete("YES");
        else result.setObselete("NO");
        //match_type
        result.setMatchType(matchType);
        if(result.getEditScore() == 1) result.setMatchConfidence(MatchConfidence.EXACT);
        else result.setMatchConfidence(MatchConfidence.INEXACT);
        return result;
    }

    public void print2() {
        for (Map.Entry<String, List<ComparisonResult>> entry : map.entrySet()) {
            for (ComparisonResult result : entry.getValue()){
                System.out.println(result);
            }
        }

    }

}
