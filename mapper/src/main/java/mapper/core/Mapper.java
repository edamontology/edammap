package mapper.core;

import java.util.*;

/**
 * @author Rabie Saidi
 * Date: 09/10/2014
 * Time: 11:51
 */
public class Mapper {

    private final List<Concept> concepts;
    private final List<Keyword> queryTerms;
    private  List<ComparisonResult> results;
    private Map<String, List<ComparisonResult>> map = new LinkedHashMap<>();
    private boolean parents;
    private int match;
    private List<BranchType> includedBranches;



//    public Mapper2(List<OntClass> ontClasses, List<String> queryTerms) {
//        //this.model = model;
//        this.ontClasses = ontClasses;
//        this.queryTerms = queryTerms;
//        this.results = new ArrayList<>();
//    }

    public Mapper(List<Concept> concepts, List<Keyword> queryTerms, boolean parents, int match, List<BranchType> includedBranches) {
        //this.model = model;
        this.concepts = concepts;
        this.queryTerms = queryTerms;
        this.results = new ArrayList<>();
        this.parents = parents;
        this.match = match;
        this.includedBranches = includedBranches;
    }




    public void map() {
        int classCount = 0;
        for(Concept concept : concepts){
            classCount++;
            //System.out.println("Concept " + classCount);
            int termCount = 0;

            for(Keyword term : queryTerms){
                termCount++;
                //System.out.println("Concept " + classCount + " - Term " + termCount);
                List<ComparisonResult> referenceResults = new ArrayList<>();
                List<ComparisonResult> exactSynonymResults = new ArrayList<>();
                List<ComparisonResult> narrowSynonymResults = new ArrayList<>();
                List<ComparisonResult> broadSynonymResults = new ArrayList<>();

                ComparisonResult referenceResult = getResult(term.getValue(), concept.getLabel(), concept, MatchType.LABEL);
                if(referenceResult.getEditScore() >= 0.2)referenceResults.add(referenceResult);
                if (parents) {
                    ComparisonResult referenceParentResult = getResult(term.getParent().getValue(), concept.getLabel(), concept, MatchType.LABEL);
                    referenceParentResult.setQuery(term.getValue());
                    if(referenceParentResult.getEditScore() >= 0.2)referenceResults.add(referenceParentResult);
                }

                //Process exact synonyms
                for(String statement : concept.getExactSynonyms()){
                    ComparisonResult exactSynonymResult = getResult(term.getValue(), statement, concept, MatchType.EXACT_SYNONYM);
                    if(exactSynonymResult.getEditScore() >= 0.2) exactSynonymResults.add(exactSynonymResult);
                    if (parents) {
                        ComparisonResult exactParentResult = getResult(term.getParent().getValue(), statement, concept, MatchType.LABEL);
                        exactParentResult.setQuery(term.getValue());
                        if(exactParentResult.getEditScore() >= 0.2)exactSynonymResults.add(exactParentResult);
                    }
                }

                //Process narrow synonyms
                for(String statement : concept.getNarrowSynonyms()){
                    ComparisonResult narrowSynonymResult = getResult(term.getValue(), statement, concept, MatchType.NARROW_SYNONYM);
                    if(narrowSynonymResult.getEditScore() >= 0.2) narrowSynonymResults.add(narrowSynonymResult);
                    if (parents) {
                        ComparisonResult narrowParentResult = getResult(term.getParent().getValue(), statement, concept, MatchType.LABEL);
                        narrowParentResult.setQuery(term.getValue());
                        if(narrowParentResult.getEditScore() >= 0.2)narrowSynonymResults.add(narrowParentResult);
                    }
                }

                //Process broad synonyms
                for(String statement : concept.getBroadSynonyms()){
                    ComparisonResult broadSynonymResult = getResult(term.getValue(), statement, concept, MatchType.BROAD_SYNONYM);
                    if(broadSynonymResult.getEditScore() >= 0.2) broadSynonymResults.add(broadSynonymResult);
                    if (parents) {
                        ComparisonResult broadParentResult = getResult(term.getParent().getValue(), statement, concept, MatchType.LABEL);
                        broadParentResult.setQuery(term.getValue());
                        if(broadParentResult.getEditScore() >= 0.2)broadSynonymResults.add(broadParentResult);
                    }
                }

                List<ComparisonResult> temp = new ArrayList<>();
                if(map.containsKey(term.getValue())) temp.addAll(map.get(term.getValue()));
                temp.addAll(referenceResults);
                temp.addAll(exactSynonymResults);
                temp.addAll(narrowSynonymResults);
                temp.addAll(broadSynonymResults);
                //Collections.sort(temp);
                map.put(term.getValue(), temp);
            }
        }
        for (Map.Entry<String, List<ComparisonResult>> entry : map.entrySet()) {
            entry.setValue(bestOf(entry.getValue()));
        }
    }

    private List<ComparisonResult> bestOf(List<ComparisonResult> comparisonResults) {
        Collections.sort(comparisonResults);
        List<ComparisonResult> temp = new ArrayList<>();
        Map<BranchType, Integer> branches = new LinkedHashMap<>();
        for (ComparisonResult comparisonResult : comparisonResults) {
            BranchType branch = comparisonResult.getBranch();
            if (includedBranches.isEmpty() || includedBranches.contains(branch)) {
                if (branches.get(branch) == null || branches.get(branch).intValue() < match) {
                    temp.add(comparisonResult);
                    branches.merge(branch, 1, Integer::sum);
                }
            }
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
            if(uri.contains("topic"))branch = BranchType.TOPIC;
            else if(uri.contains("operation"))branch = BranchType.OPERATION;
            else if(uri.contains("data"))branch = BranchType.DATA;
            else if(uri.contains("format"))branch = BranchType.FORMAT;
            else branch = BranchType.OTHER;
            result.setBranch(branch);
        }
        else{
            result.setBranch(BranchType.OTHER);
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
