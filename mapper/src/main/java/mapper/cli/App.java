package mapper.cli;

import mapper.core.*;
import mapper.io.CsvQueryReader;
import mapper.io.EdamReader;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Statement;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Rabie Saidi
 * Date: 09/10/2014
 * Time: 11:36
 */
public class App
{
    /**
     * Usage: just write in command line the query path followed by the ontology path
     * Example: allViews.csv edam.owl
     * @param args
     */
    public static void main( String[] argv ){
        Args args = new Args();
        JCommander jcommander = new JCommander(args);
        try {
            jcommander.parse(argv);
        } catch (ParameterException e) {
            System.err.println(e.getLocalizedMessage());
            jcommander.usage();
            System.exit(1);
        }
        if (args.files.size() != 2) {
            jcommander.usage();
            System.exit(1);
        }
        if (args.help) {
            jcommander.usage();
            System.exit(0);
        }
        String queryPath = args.files.get(0);
        String referencePath = args.files.get(1);
        EdamReader edamReader = new EdamReader();
        OntModel model = edamReader.getOntologyModel(referencePath);
        CsvQueryReader termReader = new CsvQueryReader(args.parents);
        List<Keyword> queryTerms = termReader.readKeywords(queryPath);
        List<Concept> concepts =  new ArrayList<>();
        Iterator<OntClass> ontClassIterator = model.listClasses();
        //Query q;

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
        Mapper mapper = new Mapper(concepts, queryTerms, args.parents, args.match, args.branches);
        mapper.map();
        mapper.print2();

        tStart = tEnd;
        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000.0;
        System.out.println("Mapping done in " + elapsedSeconds + "s");

    }



}

