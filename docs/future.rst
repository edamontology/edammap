
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
* Try to use machine learning. Challenges include a large number of EDAM terms and the quality of manual annotations currently in `bio.tools <https://bio.tools>`_. Also, there will be annotations added by previous versions of EDAMmap in bio.tools.


*********
Algorithm
*********

* Currently, scores are not totally comparable across queries. Try to make a score in one query mean the same thing in another query as exactly as possible.
* An extra query part could be tags present in some web pages, like software registries or code repositories. This would require `changes in PubFetcher <https://pubfetcher.readthedocs.io/en/latest/future.html#structure-changes>`_.
* Maybe `WordNet <https://wordnet.princeton.edu/>`_ could be used as part of the mapping algorithm.

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


******
Server
******

* Give progress information (a progress bar or simply some status information) after the MAP button is pressed in the web app.
* Enable batch queries (more than one result per query is currently only possible on the command-line).
* Option to download the HTML report as a ZIP file.
* Option to choose the EDAM ontology version from a dropdown (or supply own file).
* Make the size of the server worker thread pool configurable.
