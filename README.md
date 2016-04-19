# edamMap
Tool for mapping text to [EDAM](http://bioportal.bioontology.org/ontologies/EDAM?p=classes).  It is designed to assist not replace a curator.


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

