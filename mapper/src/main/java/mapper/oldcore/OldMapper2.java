package mapper.oldcore;

import mapper.core.*;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Statement;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author Rabie Saidi
 * Date: 09/10/2014
 * Time: 11:51
 */
public class OldMapper2 {

    private  List<OntClass> ontClasses;
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

    public OldMapper2(List<Concept> concepts, List<String> queryTerms) {
        //this.model = model;
        this.concepts = concepts;
        this.queryTerms = queryTerms;
        this.results = new ArrayList<>();
    }


    public void map() {
        int termCount = 0;
        for(String term : queryTerms){
            termCount++;
            System.out.println("Term " + termCount);
            //Iterator classIterator = model.listClasses();
            List<ComparisonResult> referenceResults = new ArrayList<ComparisonResult>();
            List<ComparisonResult> exactSynonymResults = new ArrayList<ComparisonResult>();
            List<ComparisonResult> narrowSynonymResults = new ArrayList<ComparisonResult>();
            List<ComparisonResult> broadSynonymResults = new ArrayList<ComparisonResult>();

            int classCount = 0;
            for(OntClass ontClass : ontClasses){
            //while(classIterator.hasNext()){
                classCount++;
                //System.out.println("Term " + termCount + " - Class " + classCount);

                //OntClass ontClass = (OntClass) classIterator.next();

                //Process the reference term... use listLabels instead of getLabel
                String label = ontClass.getLabel("");
                ComparisonResult referenceResult = getResult(term, label, ontClass, MatchType.LABEL);
                if(referenceResult.getEditScore() >= 0.2)referenceResults.add(referenceResult);

                //Process exact synonyms
                Predicate<Statement> exactSynonymFilter = StatementFilterFactory.getFilter("hasExactSynonym");
                Iterator<Statement> exactSynonyms = ontClass.listProperties().filterKeep(exactSynonymFilter);
                while(exactSynonyms.hasNext()){
                    Statement statement = exactSynonyms.next();
                    if (statement instanceof Literal) {
                        ComparisonResult exactSynonymResult = getResult(term, statement.getLiteral().getString(), ontClass, MatchType.EXACT_SYNONYM);
                        if(exactSynonymResult.getEditScore() >= 0.2) exactSynonymResults.add(exactSynonymResult);
                    }
                }

                //Process narrow synonyms
                Predicate<Statement> narrowSynonymFilter = StatementFilterFactory.getFilter("hasNarrowSynonym");
                Iterator<Statement> narrowSynonyms = ontClass.listProperties().filterKeep(narrowSynonymFilter);
                while(narrowSynonyms.hasNext()) {
                    Statement statement = narrowSynonyms.next();
                    if (statement instanceof Literal){
                        ComparisonResult narrowSynonymResult = getResult(term, statement.getLiteral().getString(), ontClass, MatchType.NARROW_SYNONYM);
                        if(narrowSynonymResult.getEditScore() >= 0.2) narrowSynonymResults.add(narrowSynonymResult);
                    }
                }

                //Process broad synonyms
                Predicate<Statement> broadSynonymFilter = StatementFilterFactory.getFilter("hasBroadSynonym");
                Iterator<Statement> broadSynonyms = ontClass.listProperties().filterKeep(broadSynonymFilter);
                while(broadSynonyms.hasNext()){
                    Statement statement = broadSynonyms.next();
                    if (statement instanceof Literal) {
                        ComparisonResult broadSynonymResult = getResult(term, statement.getLiteral().getString(), ontClass, MatchType.BROAD_SYNONYM);
                        if(broadSynonymResult.getEditScore() >= 0.2) broadSynonymResults.add(broadSynonymResult);
                    }
                }

//                if (ontClass.getURI() != null) {
//                    if (ontClass.getURI().equals("http://edamontology.org/data_0005")) {
//                        //ontClass.listProperties()
//                        int i = 0;
//                        Predicate<Statement> filter = new Predicate<Statement>() {
//                            @Override
//                            public boolean test(Statement statement) {
//                                return statement.getPredicate().getLocalName().equals("hasExactSynonym");
//                            }
//                        };
//                        List list = ontClass.listProperties().filterKeep(filter).toList();
//                        i++;
//                    }
//                }
            }
            List<ComparisonResult> temp = new ArrayList<ComparisonResult>();
            temp.addAll(bestOf(referenceResults));
            temp.addAll(bestOf(exactSynonymResults));
            temp.addAll(bestOf(narrowSynonymResults));
            temp.addAll(bestOf(broadSynonymResults));
            Collections.sort(temp);
            results.addAll(bestOf(temp));

        }

    }

    public void map2() {
        int classCount = 0;
        for(OntClass ontClass : ontClasses){
            classCount++;
            System.out.println("Concept " + classCount);
            int termCount = 0;
            String label = ontClass.getLabel("");
            Predicate<Statement> exactSynonymFilter = StatementFilterFactory.getFilter("hasExactSynonym");
            List<Statement> exactSynonyms = ontClass.listProperties().filterKeep(exactSynonymFilter).toList();
            Predicate<Statement> narrowSynonymFilter = StatementFilterFactory.getFilter("hasNarrowSynonym");
            List<Statement> narrowSynonyms = ontClass.listProperties().filterKeep(narrowSynonymFilter).toList();
            Predicate<Statement> broadSynonymFilter = StatementFilterFactory.getFilter("hasBroadSynonym");
            List<Statement> broadSynonyms = ontClass.listProperties().filterKeep(broadSynonymFilter).toList();

            for(String term : queryTerms){
                termCount++;
                System.out.println("Concept " + classCount + " - Term " + termCount);
                List<ComparisonResult> referenceResults = new ArrayList<>();
                List<ComparisonResult> exactSynonymResults = new ArrayList<>();
                List<ComparisonResult> narrowSynonymResults = new ArrayList<>();
                List<ComparisonResult> broadSynonymResults = new ArrayList<>();

                ComparisonResult referenceResult = getResult(term, label, ontClass, MatchType.LABEL);
                if(referenceResult.getEditScore() >= 0.2)referenceResults.add(referenceResult);

                //Process exact synonyms
                for(Statement statement : exactSynonyms){
                    if (statement instanceof Literal) {
                        ComparisonResult exactSynonymResult = getResult(term, statement.getLiteral().getString(), ontClass, MatchType.EXACT_SYNONYM);
                        if(exactSynonymResult.getEditScore() >= 0.2) exactSynonymResults.add(exactSynonymResult);
                    }
                }

                //Process narrow synonyms
                for(Statement statement : narrowSynonyms){
                    if (statement instanceof Literal){
                        ComparisonResult narrowSynonymResult = getResult(term, statement.getLiteral().getString(), ontClass, MatchType.NARROW_SYNONYM);
                        if(narrowSynonymResult.getEditScore() >= 0.2) narrowSynonymResults.add(narrowSynonymResult);
                    }
                }

                //Process broad synonyms
                for(Statement statement : broadSynonyms){
                    if (statement instanceof Literal) {
                        ComparisonResult broadSynonymResult = getResult(term, statement.getLiteral().getString(), ontClass, MatchType.BROAD_SYNONYM);
                        if(broadSynonymResult.getEditScore() >= 0.2) broadSynonymResults.add(broadSynonymResult);
                    }
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

    public void map3() {
        int classCount = 0;
        for(OntClass ontClass : ontClasses){
            classCount++;
            System.out.println("Concept " + classCount);
            int termCount = 0;
            String label = ontClass.getLabel("");
            Predicate<Statement> exactSynonymFilter = StatementFilterFactory.getFilter("hasExactSynonym");
            List<Statement> exactSynonyms = ontClass.listProperties().filterKeep(exactSynonymFilter).toList();
            Predicate<Statement> narrowSynonymFilter = StatementFilterFactory.getFilter("hasNarrowSynonym");
            List<Statement> narrowSynonyms = ontClass.listProperties().filterKeep(narrowSynonymFilter).toList();
            Predicate<Statement> broadSynonymFilter = StatementFilterFactory.getFilter("hasBroadSynonym");
            List<Statement> broadSynonyms = ontClass.listProperties().filterKeep(broadSynonymFilter).toList();

            for(String term : queryTerms){
                termCount++;
                System.out.println("Concept " + classCount + " - Term " + termCount);
                List<ComparisonResult> referenceResults = new ArrayList<>();
                List<ComparisonResult> exactSynonymResults = new ArrayList<>();
                List<ComparisonResult> narrowSynonymResults = new ArrayList<>();
                List<ComparisonResult> broadSynonymResults = new ArrayList<>();

                ComparisonResult referenceResult = getResult(term, label, ontClass, MatchType.LABEL);
                if(referenceResult.getEditScore() >= 0.2)referenceResults.add(referenceResult);

                //Process exact synonyms
                for(Statement statement : exactSynonyms){
                    if (statement instanceof Literal) {
                        ComparisonResult exactSynonymResult = getResult(term, statement.getLiteral().getString(), ontClass, MatchType.EXACT_SYNONYM);
                        if(exactSynonymResult.getEditScore() >= 0.2) exactSynonymResults.add(exactSynonymResult);
                    }
                }

                //Process narrow synonyms
                for(Statement statement : narrowSynonyms){
                    if (statement instanceof Literal){
                        ComparisonResult narrowSynonymResult = getResult(term, statement.getLiteral().getString(), ontClass, MatchType.NARROW_SYNONYM);
                        if(narrowSynonymResult.getEditScore() >= 0.2) narrowSynonymResults.add(narrowSynonymResult);
                    }
                }

                //Process broad synonyms
                for(Statement statement : broadSynonyms){
                    if (statement instanceof Literal) {
                        ComparisonResult broadSynonymResult = getResult(term, statement.getLiteral().getString(), ontClass, MatchType.BROAD_SYNONYM);
                        if(broadSynonymResult.getEditScore() >= 0.2) broadSynonymResults.add(broadSynonymResult);
                    }
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


        return temp;
    }

    private ComparisonResult getResult(String query, String reference, OntClass ontClass, MatchType matchType){
        ComparisonResult result = new ComparisonResult(query, reference);
        Matcher matcher = new Matcher();
        result.setEditScore(matcher.editScore(query,reference));
        result.setSubstringScore(matcher.substringScore(query,reference));

        //uri
        if (ontClass.getURI() != null) {
            String uri = ontClass.getURI();
            result.setUri(uri);
            //String branch = uri.substring(uri.indexOf("org/")+4, uri.indexOf("_"));
//            BranchType branch;
//            if(uri.contains("topic"))branch = BranchType.TOPIC;
//            else if(uri.contains("operation"))branch = BranchType.OPERATION;
//            else if(uri.contains("data"))branch = BranchType.DATA;
//            else if(uri.contains("format"))branch = BranchType.FORMAT;
//            else branch = BranchType.OTHER;
//            result.setBranch(branch);
        }
        else{
            result.setUri("NO URI DEFINED");
//            result.setBranch(BranchType.OTHER);
        }
        //obsolete
        boolean obsolete = false;
        Predicate<Statement> inSubsetFilter = StatementFilterFactory.getFilter("inSubset");
        Iterator<Statement> inSubsetIterator = ontClass.listProperties().filterKeep(inSubsetFilter);
        if(inSubsetIterator.hasNext()){
            Statement statement = inSubsetIterator.next();
            String value = statement.getObject().toString();
            obsolete = value.equals("http://purl.obolibrary.org/obo/edam#obsolete");
        }
        //result.setObselete(obsolete);
        //match_type
        result.setMatchType(matchType);
        if(result.getEditScore() == 1) result.setMatchConfidence(MatchConfidence.EXACT);
        result.setMatchConfidence(MatchConfidence.INEXACT);
        return result;
    }

    public void print() {
        for(ComparisonResult result : results){
            System.out.println(result);
        }
    }

    public void print2() {
        for (Map.Entry<String, List<ComparisonResult>> entry : map.entrySet()) {
            for (ComparisonResult result : entry.getValue()){
                System.out.println(result);
            }
        }

    }

}
