package edammapper.edam;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;

public class Edam {
	public static Map<EdamUri, Concept> load(String edamPath) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(edamPath));

		String prefix = ontology.getOntologyID().getOntologyIRI().get().toString();

		return ontology.classesInSignature()
			.filter(c -> EdamUri.isEdamUri(c.getIRI().toString(), prefix))
			.collect(Collectors.toMap(
				c -> new EdamUri(c.getIRI().toString(), prefix),
				c -> {
					Concept concept = new Concept();
					EntitySearcher.getAnnotations(c, ontology).forEachOrdered(a -> {
						if (a.getProperty().isLabel())
							concept.setLabel(a.getValue().asLiteral().get().getLiteral());
						else if (a.getProperty().isDeprecated())
							concept.setObsolete(true);
						else if (a.getProperty().toStringID().equals("http://www.geneontology.org/formats/oboInOwl#hasExactSynonym") && a.getValue().asLiteral().isPresent())
							concept.addExactSynonym(a.getValue().asLiteral().get().getLiteral());
						else if (a.getProperty().toStringID().equals("http://www.geneontology.org/formats/oboInOwl#hasNarrowSynonym") && a.getValue().asLiteral().isPresent())
							concept.addNarrowSynonym(a.getValue().asLiteral().get().getLiteral());
						else if (a.getProperty().toStringID().equals("http://www.geneontology.org/formats/oboInOwl#hasBroadSynonym") && a.getValue().asLiteral().isPresent())
							concept.addBroadSynonym(a.getValue().asLiteral().get().getLiteral());
						else if (a.getProperty().toStringID().equals("http://www.geneontology.org/formats/oboInOwl#hasDefinition") && a.getValue().asLiteral().isPresent())
							concept.setDefinition(a.getValue().asLiteral().get().getLiteral());
						else if (a.getProperty().isComment() && a.getValue().asLiteral().isPresent())
							concept.setComment(a.getValue().asLiteral().get().getLiteral());
					});
					concept.setDirectParents(EntitySearcher.getSuperClasses(c, ontology)
						.filter(a -> a.isOWLClass() && !a.asOWLClass().getIRI().toString().equals("http://www.w3.org/2002/07/owl#DeprecatedClass"))
						.map(a -> new EdamUri(a.asOWLClass().getIRI().toString(), prefix))
						.collect(Collectors.toList()));
					concept.setDirectChildren(EntitySearcher.getSubClasses(c, ontology)
						.filter(a -> a.isOWLClass())
						.map(a -> new EdamUri(a.asOWLClass().getIRI().toString(), prefix))
						.collect(Collectors.toList()));
					if (concept.getLabel().isEmpty())
						throw new IllegalStateException(String.format("Label of concept %s is empty", c.getIRI()));
					return concept;
				},
				(u, v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
				LinkedHashMap::new
			));
	}
}
