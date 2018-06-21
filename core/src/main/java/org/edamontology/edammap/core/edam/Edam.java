/*
 * Copyright © 2016, 2017, 2018 Erik Jaaniso
 *
 * This file is part of EDAMmap.
 *
 * EDAMmap is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EDAMmap is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EDAMmap.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.edamontology.edammap.core.edam;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;

import org.edamontology.pubfetcher.core.common.PubFetcher;

public class Edam {
	public static Map<EdamUri, Concept> load(String edamPath) throws IOException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(new File(edamPath));
		} catch (OWLOntologyCreationException e) {
			throw new IOException(e);
		}

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
					if (concept.getLabel() == null)
						throw new IllegalStateException(String.format("Label of concept %s is empty", c.getIRI()));
					return concept;
				},
				(u, v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
				LinkedHashMap::new
			));
	}

	public static Map<Branch, Integer> branchCounts(Map<EdamUri, Concept> concepts) {
		Map<Branch, Integer> branchCounts = new EnumMap<>(Branch.class);
		int topicCount = 0;
		int operationCount = 0;
		int dataCount = 0;
		int formatCount = 0;
		for (EdamUri edamUri : concepts.keySet()) {
			switch (edamUri.getBranch()) {
				case topic: ++topicCount; break;
				case operation: ++operationCount; break;
				case data: ++dataCount; break;
				case format: ++formatCount; break;
			}
		}
		branchCounts.put(Branch.topic, topicCount);
		branchCounts.put(Branch.operation, operationCount);
		branchCounts.put(Branch.data, dataCount);
		branchCounts.put(Branch.format, formatCount);
		return branchCounts;
	}

	public static Set<EdamUri> getBlacklist() throws IOException {
		return PubFetcher.getResource(Edam.class, "edam/blacklist.txt").stream()
			.map(s -> new EdamUri(s, EdamUri.DEFAULT_PREFIX))
			.collect(Collectors.toSet());
	}
}
