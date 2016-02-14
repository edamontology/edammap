package mapper.io;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rabie Saidi
 */
public class EdamReader {
//    <owl:Class rdf:about="http://edamontology.org/data_0005">
//    <rdfs:label>Resource type</rdfs:label>
//    <rdfs:subClassOf rdf:resource="&oboInOwl;ObsoleteClass"/>
//    <obsolete_since>beta12orEarlier</obsolete_since>
//    <created_in>beta12orEarlier</created_in>
//    <oboInOwl:hasDefinition>A type of computational resource used in bioinformatics.</oboInOwl:hasDefinition>
//    <owl:deprecated>true</owl:deprecated>
//    <oboInOwl:inSubset rdf:resource="&oboOther;edam#obsolete"/>
//    </owl:Class>
    public OntModel getOntologyModel(String filePath){
        try{
            File file = new File(filePath);
            FileReader reader = new FileReader(file);
            OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
            model.read(reader, null);
            return model;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }



}
