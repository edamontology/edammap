
.. _future:

################
Ideas for future
################

Sometimes ideas are emerging. These are written down here for future reference. A written down idea is not necessarily a good idea, thus not all points here should be implemented.


*******
General
*******

* In addition to tools, annotate also `training materials <https://tess.elixir-europe.org/>`_.
* Generalise to other ontologies besides `EDAM <http://edamontology.org/page>`_ (`#8 <https://github.com/edamontology/edammap/issues/8>`_). However, optimising specifically for EDAM is one of the goals of EDAMmap.
* Use existing libraries of some other tools, like `Maui <https://github.com/zelandiya/maui>`_ or `Kea <http://www.nzdl.org/Kea/>`_, in addition to the current self-made approach.
* Try to use machine learning. Challenges include a large number of EDAM terms and the quality of manual annotations currently in `bio.tools <https://bio.tools>`_. Also, there will be annotations added by previous versions of EDAMmap in bio.tools. Maybe the ontology needs to be simplified, for example more specific terms removed.


*********
Algorithm
*********

* Currently, scores are not totally comparable across queries. Try to make a score in one query mean the same thing in another query as exactly as possible.
* An extra query part could be tags present in some web pages, like software registries or code repositories. This would require `changes in PubFetcher <https://pubfetcher.readthedocs.io/en/latest/future.html#structure-changes>`_.
* Maybe `WordNet <https://wordnet.princeton.edu/>`_ could be used as part of the mapping algorithm. For example use lemmatisation instead of stemming.
* In results got from running EDAMmap against existing entries of bio.tools, look at FNs and see if anything can be done to increase their score.

Parameters
==========

* Further investigate the effect of different parameter values, like the :ref:`IDF parameters <idf_parameters>`, stop words removal, stemming, bi-directional matching, path enrichment, etc. Not all used methods necessarily improve results.
* Currently, default values for :ref:`query normalisers <query_normalisers>` and :ref:`score limits <score_limits>` have been manually tuned to give good results for the usual input and default values of other parameters. Instead, try to automatically set the values of these normaliser and limit parameters, based on the input queries and parameter values.
* Try to implement automatic parameter tuning to find optimal values for parameters. If parameters of some methods give best results when turned off, then before discarding the methods, one should check that maybe these methods sometimes find correct results that better methods fail to find.
* Some parameters could be changeable on a per EDAM branch basis.

Weights
=======

* In :ref:`query_weights`, maybe the publication should have an overall weight instead of each usable publication part influencing the score independently.
* Different publication full text parts (like image captions) could have different weights. This would require `changes in PubFetcher`_.
* Different `publication types <https://biotools.readthedocs.io/en/latest/curators_guide.html#publication-type>`_ (primary, etc) and `link types <https://biotools.readthedocs.io/en/latest/curators_guide.html#linktype>`_ could have different weights. This would mean categorising links got through other means than bio.tools.

Measures
========

* Add measures about scores, like average maximum score of TPs, etc.
* Maybe also take into account the direct parents and children of an automatically found term when deciding if it matches a manual annotation.
* Plot some measures, for example generate a precision-recall curve by varying the parameter :ref:`matches <matches>`.

Ontology
========

* EDAM is not a tree, but a `DAG <https://en.wikipedia.org/wiki/Directed_acyclic_graph>`_. Does this influence path enrichment? Look more into related terms influencing each other in the results.
* Branch specific tweaks, for example terms from the topic branch could be more specific than terms from the operation branch and maybe more terms could be output for topic than operation.
* Individual concept level tweaks, for example currently some terms are suggested too frequently (many FPs) and others not frequently enough (many FNs, possible because of IDF weighting).
* For more homogeneous results, maybe bias EDAMmap towards some terms, that is some terms could be "more recommended for annotation in bio.tools" than others.
* Look into using inter-branch relations ("has_input", "has_output", "has_topic", etc). Have to be careful, as only some terms have these defined.
* Some concepts have "hasRelatedSynonym" defined, it's currently not read as it's quite rare.
* Results for the data and format branch are not that good currently (thus disabled by default), look into improving them.


******
Server
******

* Give progress information (a progress bar or simply some status information) after the MAP button is pressed in the web app.
* Enable batch queries (more than one result per query is currently only possible on the command-line).
* Option to download the HTML report as a ZIP file.
* Option to choose the EDAM ontology version from a dropdown (or supply own file).
* Make the size of the server worker thread pool configurable.


***********
Maintenance
***********

* Update PubFetcher's `scraping rules <https://pubfetcher.readthedocs.io/en/latest/scraping.html#scraping-rules>`_, by `testing the rules <https://pubfetcher.readthedocs.io/en/latest/scraping.html#testing-of-rules>`_ and modifying outdated rules in `journals.yaml <https://github.com/edamontology/pubfetcher/blob/master/core/src/main/resources/scrape/journals.yaml>`_, `webpages.yaml <https://github.com/edamontology/pubfetcher/blob/master/core/src/main/resources/scrape/webpages.yaml>`_ and most importantly the hardcoded rules for `Europe PMC <https://europepmc.org/>`_ and other built-in `resources <https://pubfetcher.readthedocs.io/en/latest/fetcher.html#resources>`_.
* Update dependencies in `pom.xml <https://github.com/edamontology/edammap/blob/master/pom.xml>`_ (but care should be taken to not cause regressions).
* When a new `biotoolsSchema <https://github.com/bio-tools/biotoolsSchema>`_ is released, some code modifications might be necessary to adhere to it.
* Also, when a new `EDAM ontology <https://github.com/edamontology/edamontology>`_ is released, some modifications might be necessary (for example in `blacklist.txt <https://github.com/edamontology/edammap/blob/master/core/src/main/resources/edam/blacklist.txt>`_ and `blacklist_synonyms.txt <https://github.com/edamontology/edammap/blob/master/core/src/main/resources/edam/blacklist_synonyms.txt>`_; also, any running :ref:`server` instances could be restarted to use the new ontology version).
