package mapper.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Rabie Saidi
 * Date: 09/10/2014
 * Time: 11:51
 */
public class Mapper {

    private final List<Concept> concepts;
    private final Map<String, Keyword> queryTerms;
    private  List<ComparisonResult> results;
    private Map<String, List<ComparisonResult>> map = new LinkedHashMap<>();
    private int match;
    private List<BranchType> includedBranches;



//    public Mapper2(List<OntClass> ontClasses, List<String> queryTerms) {
//        //this.model = model;
//        this.ontClasses = ontClasses;
//        this.queryTerms = queryTerms;
//        this.results = new ArrayList<>();
//    }

    public Mapper(List<Concept> concepts, Map<String, Keyword> queryTerms, int match, List<BranchType> includedBranches) {
        //this.model = model;
        this.concepts = concepts;
        this.queryTerms = queryTerms;
        this.results = new ArrayList<>();
        this.match = match;
        this.includedBranches = includedBranches;
    }




    public void map() {
        System.out.println("Mapping...");
        int classCount = 0;
        double conceptSize = concepts.size();
        long progressPrevious = 0;
        for(Concept concept : concepts){
            long progress = Math.round(classCount / conceptSize * 100);
            if (progressPrevious != progress) {
                System.out.print("Mapping " + progress + "%" + "\r");
                progressPrevious = progress;
            }
            classCount++;
            //System.out.println("Concept " + classCount);
            int termCount = 0;

            for (Keyword term : queryTerms.values()) {
                termCount++;
                //System.out.println("Concept " + classCount + " - Term " + termCount);
                List<ComparisonResult> referenceResults = new ArrayList<>();
                List<ComparisonResult> exactSynonymResults = new ArrayList<>();
                List<ComparisonResult> narrowSynonymResults = new ArrayList<>();
                List<ComparisonResult> broadSynonymResults = new ArrayList<>();

                ComparisonResult referenceResult = getResult(term.getKeyword(), concept.getLabel(), concept, MatchType.LABEL);
                if(referenceResult.getEditScore() >= 0.2)referenceResults.add(referenceResult);
                for (String parent : term.getParents()) {
                    ComparisonResult referenceParentResult = getResult(parent, concept.getLabel(), concept, MatchType.LABEL);
                    referenceParentResult.setQuery(term.getKeyword());
                    if(referenceParentResult.getEditScore() >= 0.2)referenceResults.add(referenceParentResult);
                }

                //Process exact synonyms
                for(String statement : concept.getExactSynonyms()){
                    ComparisonResult exactSynonymResult = getResult(term.getKeyword(), statement, concept, MatchType.EXACT_SYNONYM);
                    if(exactSynonymResult.getEditScore() >= 0.2) exactSynonymResults.add(exactSynonymResult);
                    for (String parent : term.getParents()) {
                        ComparisonResult exactParentResult = getResult(parent, statement, concept, MatchType.LABEL);
                        exactParentResult.setQuery(term.getKeyword());
                        if(exactParentResult.getEditScore() >= 0.2)exactSynonymResults.add(exactParentResult);
                    }
                }

                //Process narrow synonyms
                for(String statement : concept.getNarrowSynonyms()){
                    ComparisonResult narrowSynonymResult = getResult(term.getKeyword(), statement, concept, MatchType.NARROW_SYNONYM);
                    if(narrowSynonymResult.getEditScore() >= 0.2) narrowSynonymResults.add(narrowSynonymResult);
                    for (String parent : term.getParents()) {
                        ComparisonResult narrowParentResult = getResult(parent, statement, concept, MatchType.LABEL);
                        narrowParentResult.setQuery(term.getKeyword());
                        if(narrowParentResult.getEditScore() >= 0.2)narrowSynonymResults.add(narrowParentResult);
                    }
                }

                //Process broad synonyms
                for(String statement : concept.getBroadSynonyms()){
                    ComparisonResult broadSynonymResult = getResult(term.getKeyword(), statement, concept, MatchType.BROAD_SYNONYM);
                    if(broadSynonymResult.getEditScore() >= 0.2) broadSynonymResults.add(broadSynonymResult);
                    for (String parent : term.getParents()) {
                        ComparisonResult broadParentResult = getResult(parent, statement, concept, MatchType.LABEL);
                        broadParentResult.setQuery(term.getKeyword());
                        if(broadParentResult.getEditScore() >= 0.2)broadSynonymResults.add(broadParentResult);
                    }
                }

                List<ComparisonResult> temp = new ArrayList<>();
                if(map.containsKey(term.getKeyword())) temp.addAll(map.get(term.getKeyword()));
                temp.addAll(referenceResults);
                temp.addAll(exactSynonymResults);
                temp.addAll(narrowSynonymResults);
                temp.addAll(broadSynonymResults);
                //Collections.sort(temp);
                map.put(term.getKeyword(), temp);
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

    public void print2(String path) {
        boolean stdout = path.isEmpty();
        if (!stdout) {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8)) {
                for (Map.Entry<String, List<ComparisonResult>> entry : map.entrySet()) {
                    for (ComparisonResult result : entry.getValue()) {
                        writer.write(result.toString() + "\n");
                    }
                }
            } catch (IOException x) {
                System.err.println("Failed to write results to fail, dumping to standard output!");
                stdout = true;
            }
        }
        if (stdout) {
            for (Map.Entry<String, List<ComparisonResult>> entry : map.entrySet()) {
                for (ComparisonResult result : entry.getValue()) {
                    System.out.print(result + "\n");
                }
            }
        }
    }
}
