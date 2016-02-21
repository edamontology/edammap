package mapper.io;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
            InputStream istream = new FileInputStream(file);
            OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
            model.read(istream, null);
            return model;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
