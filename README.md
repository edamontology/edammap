# edammap
Tool for mapping text (terms, phrases, free text) to the EDAM ontology

# Background
[EDAM](http://bioportal.bioontology.org/ontologies/EDAM?p=classes) includes 4 branches of different concepts (OWL classes): topic, operation, data & format.  A concept is assigned to a branch by its URI (id), thus:

* http://edamontology.org/topic_xxxx
* http://edamontology.org/operation_xxxx 
* http://edamontology.org/data_xxxx 
* http://edamontology.org/format_xxxx 

In the following, text in parenthesis refers to statements in the OWL file.  Each concept has a:

* preferred term (rdfs:label)
* definition (oboInOwl:hasDefinition)
* parent concept (rdfs:subClassOf)
* subset (oboInOwl:inSubset), always one of "topics", "operations", "data", "formats", or "obsolete" (for obsolete classes).  NB: >1 other types of subsets (not listed here) may be be defined for a concept !
* exact synonym (oboInOwl:hasExactSynonym) - standard synonym
* narrow synonym (oboInOwl:hasNarrowSynonym) - specialism of concept
* broad synonym (oboInOwl:hasBroadSynonym) - generalisation of concept 
* comments (rdfs:comment) - some comment

# The problem
The basic problem is to map a list of text, which may be:

* key words 
* short phrases (typically in a text file, one word or phrase / line) 
* free text

to EDAM class labels and synonyms, including obsolete classes, and generate a text output file including (at least) the following information:

* keyword_or_phrase 	: supplier-provided keyword, short phrase or text
* label_or_synonym 	: EDAM label or synonym that keyword / phrase was matched to
* URI 			: EDAM URI of the matched class
* obsolete 		: one of "yes" or "no"
* match_type 		: one of "Label", Exact_synonym", "Narrow_synonym" or "Broad_synonym"
* match_conf 		: one of "Exact" (for non-case-sensitive exact text matches) or "Inexact" (otherwise)
* branch:  		: one of "Topic", "Operation", "Data" or "Format" 

# Output options
Assuming a single line of output, e.g. 

keyword_or_phrase | label_or_synonym | URI | obsolete | match_type | match_conf | branch 

At least one output line is required for every input keyword/phrase, even in cases where no match was found.  

In practice, flexible output options will be needed to suit various applications, e.g.:

* Where matches are identified to more than 1 branch, the best match can be reported for each branch, i.e. a maximum of 4 matches per keyword/phrase
* Reporting of top-n hits in each branch
* etc.

Thus output definition is needed on a case-by-case basis.

# Further developments
There are many lines of further development, e.g. also matching against the description or even the comments, and perhaps supporting "smart" matching that can cope with (ignore) whitespace, do sub-string matching etc.

In terms of "quality" of matches:

labels > exact synonym > narrow or broad synonym > definition > comment

and:

exact full-text matches > "fuzzy" matches

i.e. an exact full-text match to the concept label is best of all.
