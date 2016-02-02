package mapper.cli;

import mapper.core.Concept;
import mapper.oldcore.OldMapper3;
import mapper.core.StatementFilterFactory;
import mapper.io.EdamReader;
import mapper.io.TermReader;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Statement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Rabie Saidi
 * Date: 09/10/2014
 * Time: 11:36
 */
public class OldApp2
{
    public static void main( String[] args ){
        String queryPath = args[0];
        String referencePath = args[1];
        EdamReader edamReader = new EdamReader();
        OntModel model = edamReader.getOntologyModel(referencePath);
        TermReader termReader = new TermReader();
        List<String> queryTerms = termReader.readTerms(queryPath);
        List<Concept> concepts =  new ArrayList<>();
        Iterator<OntClass> ontClassIterator = model.listClasses();

        long tStart = System.currentTimeMillis();
        System.out.println("Loading concepts..");
        int conceptCount = 0;
       while(ontClassIterator.hasNext()){
        //for(OntClass ontClass : ontClassIterator){
            conceptCount++;
            System.out.println("Concept " + conceptCount);
            OntClass ontClass = ontClassIterator.next();
            Concept concept = new Concept();
            String uri = ontClass.getURI();
            if(uri == null) uri = "##########";
           if(uri.equals("http://edamontology.org/data_0582")){
               uri = uri;
           }
            concept.setUri(uri);
            String label = ontClass.getLabel("");
            if(label == null) label = "##########";
            concept.setLabel(label);
            boolean obsolete = false;
            Predicate<Statement> inSubsetFilter = StatementFilterFactory.getFilter("inSubset");
            Iterator<Statement> inSubsetIterator = ontClass.listProperties().filterKeep(inSubsetFilter);
            if(inSubsetIterator.hasNext()){
                Statement statement = inSubsetIterator.next();
                String value = statement.getObject().toString();
                obsolete = value.equals("http://purl.obolibrary.org/obo/edam#obsolete");
            }
            concept.setObsolete(obsolete);
            Predicate<Statement> exactSynonymFilter = StatementFilterFactory.getFilter("hasExactSynonym");
            Iterator<Statement> exactSynonyms = ontClass.listProperties().filterKeep(exactSynonymFilter);
            List<String> xSynonyms = new ArrayList<>();
            while (exactSynonyms.hasNext()) {
                Statement statement = exactSynonyms.next();
                if(statement.getObject() instanceof Literal) xSynonyms.add(statement.getString());
            }
            concept.setExactSynonyms(xSynonyms);
            Predicate<Statement> narrowSynonymFilter = StatementFilterFactory.getFilter("hasNarrowSynonym");
            Iterator<Statement> narrowSynonyms = ontClass.listProperties().filterKeep(narrowSynonymFilter);
            List<String> nSynonyms = new ArrayList<>();
            while (narrowSynonyms.hasNext()) {
                Statement statement = narrowSynonyms.next();
                if(statement.getObject() instanceof Literal) nSynonyms.add(statement.getString());
            }
            concept.setNarrowSynonyms(nSynonyms);
            Predicate<Statement> broadSynonymFilter = StatementFilterFactory.getFilter("hasBroadSynonym");
            Iterator<Statement> broadSynonyms = ontClass.listProperties().filterKeep(broadSynonymFilter);
            List<String> bSynonyms = new ArrayList<>();
            while (broadSynonyms.hasNext()) {
                Statement statement = broadSynonyms.next();
                if(statement.getObject() instanceof Literal)bSynonyms.add(statement.getString());
            }
            concept.setBroadSynonyms(bSynonyms);
            concepts.add(concept);
        }

        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println("Concepts loaded in " + elapsedSeconds + "s");
        //Mapper2 mapper = new Mapper2(model.listClasses().toList(), queryTerms);
        OldMapper3 mapper = new OldMapper3(concepts, queryTerms);
        mapper.map();
        mapper.print2();

        tStart = tEnd;
        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000.0;
        System.out.println("Mapping done in " + elapsedSeconds + "s");

    }



}

