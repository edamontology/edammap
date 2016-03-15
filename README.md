# edamMap
Tool for mapping text to [EDAM](http://bioportal.bioontology.org/ontologies/EDAM?p=classes).  It is designed to assist not replace a curator.

###Summary
The EDAM ontology has the potential to be applied to not only bioinformatics tools and services, but anything related to the usage of these resources.  Hence, teaching and training material can be annotated with EDAM terms allowing their association with relevant software tools and services. Other examples would be protocols for data storage and data exchange.  From experience curating https://bio.tools, whether adding single software tools up to entire collections, the bottleneck is the mapping of software descriptors (tags or free text) to EDAM concepts (terms and synonyms).  This can be time-consuming and error-prone, especially for annotation of for large data sets which lack meaningful tags (annotations) performed by people who are not already familiar with EDAM, resulting in wrong or missing annotations.  
Given this bottleneck, we need fast and precise methods to assist a curator in identifying the right EDAM terms when annotating softwares and teaching materials.  As most of these come with additional information available in textual form (e.g. a publication abstract, or short textual description), mapping of such text to EDAM terms and synonyms provides a shortcut with the potential to become an automatized procedure for annotation of the material.

###Objectives
- Mapping to EDAM concepts (via terms and synonyms) with highest possible confidence. 
- Software tool using arbitrary text input and providing well-defined output (mapping to EDAM concepts)
- Improve annotation of tools in https://bio.tools by mapping paper abstracts
- Integration of the mapping tool into https://bio.tools and other portals to simplify upload of new material

###Usage

_Text input may be_
* key words 
* short phrases (typically in a text file, one word or phrase / line) 
* free text such as paper abstracts, full texts and tutorials

_Mapping to EDAM concepts_ (including deprecated concepts), specifically
* preferred labels 
* exact synonyms
* narror and broad synonyms
* concept definition (maybe)
* concept comments (maybe)

_Output_ includes (provisionally) at least the following information:

* text input 	    	: supplier-provided keyword, short phrase or text
* label_or_synonym 	: EDAM label or synonym that keyword / phrase was matched to
* URI 			: EDAM URI of the matched class
* obsolete 		: one of "yes" or "no"
* match_type 		: one of "Label", Exact_synonym", "Narrow_synonym" or "Broad_synonym"
* match_conf 		: one of "Exact" (for non-case-sensitive exact text matches) or "Inexact" (otherwise)
* branch:  		: one of "Topic", "Operation", "Data" or "Format" 

###Technical and Methodological Roadmap

__Definition of input data__
- Define a document: title, abstract, text, outlines, figures, ..
- Define what can be mapped against EDAM: abstracts of papers defining a tool and/or database in the registry; tool collections with describing terms/keywords
- Continuously adapt tool to manage larger text

__Implement specific implementations_

_Flexible output options_ will be needed on a case-by-case basis, e.g.

* Best n matches for all EDAM branches (Topic, Operation, Data, Format)
* Best n matches for specific branches (Topic and Operation only, say)
* Score for fuzziness of mapping
* Score for more general mapping quality (e.g. number of hits in large texts)

_Different mapping techniques_
- Start from the EDAM concept and create from it a REGEX then use the REGEX as a lookup key 
- Start from text and parse it into vector of words then compare each word to the concept, then aggregate results and compare again with compound words
- Sequence alignment (how to fix the threshold for a positive hit)
- Prediction based on machine learning
- Apply search engines to find EDAM terms in text

__Methodology__
- _Pre-processing:_ 
   * Try without 
   * Find treatment of specific characters like hyphens, points, ...
   * Elimination of promiscous words from the documents (i.e. conjunctives, prepositions, ..) and applying information retrieval techniques to weight the words concerning the significance (look for available software and apply tf-idf)
- _Processing:_ Running the selected technique(s) on the input data to obtain the matches
- _Post-processing:_ Elimination of the matches with low significance from the output (based on a measure of match confidence and/or a defined score)

__Validation and benchmarking__

- Benchmarking on a reference dataset, manually curated mappings between registry sources and EDAM can be used to validate the output quality, start simple by counting validated matches
- Comparison of several mining techniques to select the best performing one.
- Parameter optimization
- Find optimal score to distinguish good matches

__Related concepts__

http://ceur-ws.org/Vol-774/das.pdf

https://www.kcura.com/relativity/Portals/0/Documents/8.0%20Documentation%20Help%20Site/Content/Features/Analytics/Concept%20searching.htm

