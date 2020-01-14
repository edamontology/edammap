
.. _api:

###
API
###

The EDAMmap API is consumed by sending a JSON request with HTTP POST. The main endpoint is `/api`_, which on the public instance translates to https://biit.cs.ut.ee/edammap/api.

JSON numbers and booleans are converted to strings internally. JSON objects are ignored (except under `bio.tools input`_), meaning there is no hierarchy in the request JSON structure.


.. _api_endpoint:

****
/api
****

The main endpoint is used for performing one mapping. The key-value pairs in the request JSON fall under two categories: `query data`_ and parameters_.

Query data
==========

The query data to be mapped can be supplied in two different ways: as strings or arrays of strings under field names mirroring the usual `EDAMmap input`_ names or as a `bio.tools input`_ JSON object (like a bio.tools entry in JSON format). In case data is specified using both ways, only data under the `bio.tools input`_ is used.

EDAMmap input
-------------

The following data can be given, with only the "name" being mandatory.

==============  ========================  ===========
Key             Type                      Description
==============  ========================  ===========
name            string                    Name of tool or service
keywords        array of strings          Keywords, tags, etc
description     string                    Short description of tool or service
webpageUrls     array of strings          URLs of homepage, etc
docUrls         array of strings          URLs of documentations
publicationIds  array of strings/objects  PMID/PMCID/DOI of journal article

                                          Note: an article ID can be specified as a string ``"<PMID>\t<PMCID>\t<DOI>"`` or as an object (the only place besides `bio.tools input`_ where a JSON object is not ignored), wherein keys ``"pmid"``, ``"pmcid"``, ``"doi"`` can be used
annotations     array of strings          Existing annotations from EDAM
==============  ========================  ===========

bio.tools input
---------------

Under the field name "tool", a JSON object adhering to `biotoolsSchema <https://biotoolsschema.readthedocs.io/>`_ can be specified. All values possible in bio.tools can be specified, but only values relevant to EDAMmap will be used. A few attributes are mandatory: `name <https://biotools.readthedocs.io/en/latest/curators_guide.html#name-tool>`_, `description <https://biotools.readthedocs.io/en/latest/curators_guide.html#description>`_ and `homepage <https://biotools.readthedocs.io/en/latest/curators_guide.html#homepage>`_. The input will be mirrored under tool_ in the response_, but with found EDAM terms added to it.

.. _api_parameters:

Parameters
==========

Main
----

=========  ==========  ===========
Parameter  Default     Description
=========  ==========  ===========
version    ``"1"``     API version. Currently, only one possible value: ``"1"``.
_`type`    ``"core"``  Detail level of the response_. Possible values: ``"core"``, ``"full"``.
txt        ``false``   Also output results to plain text file. The location of the created file can be read from the response_.
html       ``false``   Also output results to HTML file. The location of the created file can be read from the response_.
=========  ==========  ===========

.. _preprocessing:

Preprocessing
-------------

=========  ============  =====  ===========
Parameter  Default       Min    Description
=========  ============  =====  ===========
numbers    ``true``             Include/exclude freestanding numbers (i.e., that are not part of a word) in pre-processing
stopwords  ``"lucene"``         Do stopwords removal as part of pre-processing, using the chosen stopwords list. Possible values: ``"off"``, ``"corenlp"``, ``"lucene"``, ``"mallet"``, ``"smart"``, ``"snowball"``.
stemming   ``true``             Do stemming as part of pre-processing
minLength  ``1``         ``0``  When all pre-processing steps are done, tokens with length less to this length are removed
=========  ============  =====  ===========

.. _fetching:

Fetching
--------

The fetching parameters are implemented in `PubFetcher <https://github.com/edamontology/pubfetcher>`_ and thus are described in its documentation: `Fetching parameters <https://pubfetcher.readthedocs.io/en/latest/cli.html#fetching>`_.

.. _mapping:

Mapping
-------

=======================  ==========================  =======  =======  ===========
Parameter                Default                     Min      Max      Description
=======================  ==========================  =======  =======  ===========
_`branches`              ``["topic", "operation"]``                    Branches to include. Can choose multiple at once from possible values: ``"topic"``, ``"operation"``, ``"data"``, ``"format"``.
_`matches`               ``5``                       ``0``             Number of best matches per branch to output. Output amount can be less than requested if not enough match final scores fulfill `score limits`_ requirement.
obsolete                 ``false``                                     Include matched obsolete concepts
_`replaceObsolete`       ``true``                                      Replace matched obsolete concepts with their best matched replacement defined in EDAM (with "replacedBy" or "consider")
obsoletePenalty          ``0.5``                     ``0.0``  ``1.0``  The fraction of the final score that included or replaced obsolete concepts will get
doneAnnotations          ``true``                                      Suggest concepts already used for annotating query. Parents and children of these concepts are not suggested in any case (unless ``inferiorParentsChildren`` is set to ``true``).
inferiorParentsChildren  ``false``                                     Include parents and children of a better matched concept in suggestion results
=======================  ==========================  =======  =======  ===========

Mapping algorithm
^^^^^^^^^^^^^^^^^

====================  =============  =======  =======  ===========
Parameter             Default        Min      Max      Description
====================  =============  =======  =======  ===========
compoundWords         ``1``          ``0``             Try to match words that have accidentally been made compound (given number is maximum number of words in an accidental compound minus one). Not done for tokens from `fulltext <https://pubfetcher.readthedocs.io/en/latest/fetcher.html#fulltext>`_, `doc <https://pubfetcher.readthedocs.io/en/latest/output.html#content-of-docs>`_ and `webpage <https://pubfetcher.readthedocs.io/en/latest/output.html#content-of-webpages>`_. Set to 0 to disable (for a slight speed increase with only slight changes to the results).
mismatchMultiplier    ``2.0``        ``0.0``           Multiplier for score decrease caused by mismatch
matchMinimum          ``1.0``        ``0.0``  ``1.0``  Minimum score allowed for approximate match. Not done for tokens from fulltext_, doc_ and webpage_. Set to ``1`` to disable approximate matching.
positionOffBy1        ``0.35``       ``0.0``  ``1.0``  Multiplier of a position score component for the case when a word is inserted between matched words or matched words are switched
positionOffBy2        ``0.05``       ``0.0``  ``1.0``  Multiplier of a position score component for the case when two words are inserted between matched words or matched words are switched with an additional word between them
positionMatchScaling  ``0.5``        ``0.0``           Set to ``0`` to not have match score of neighbor influence position score. Setting to ``1`` means linear influence.
positionLoss          ``0.4``        ``0.0``  ``1.0``  Maximum loss caused by wrong positions of matched words
scoreScaling          ``0.2``        ``0.0``           Score is scaled before applying multiplier and weighting with other direction match. Setting to ``0`` or ``1`` means no scaling.
conceptWeight         ``1.0``        ``0.0``           Weight of matching a concept (with a query). Set to ``0`` to disable matching of concepts.
queryWeight           ``1.0``        ``0.0``           Weight of matching a query (with a concept). Set to ``0`` to disable matching of queries.
_`mappingStrategy`    ``"average"``                    Choose the best or take the average of query parts matches. Possible value: ``"best"``, ``"average"``.
_`parentWeight`       ``0.5``        ``0.0``           Weight of concept's parent when computing path enrichment. Weight of grand-parent is ``parentWeight`` times ``parentWeight``, etc. Set to ``0`` to disable path enrichment.
_`pathWeight`         ``0.7``        ``0.0``           Weight of path enrichment. Weight of concept is ``1``. Set to ``0`` to disable path enrichment.
====================  =============  =======  =======  ===========

.. _idf_parameters:

IDF
^^^

=================  =========  =======  ===========
Parameter          Default    Min      Description
=================  =========  =======  ===========
conceptIdfScaling  ``0.5``    ``0.0``  Set to ``0`` to disable concept IDF. Setting to ``1`` means linear IDF weighting.
queryIdfScaling    ``0.5``    ``0.0``  Set to ``0`` to disable query IDF. Setting to ``1`` means linear IDF weighting.
labelSynonymsIdf   ``false``           IDF weighting for concept label and synonyms
nameKeywordsIdf    ``true``            IDF weighting for query name and keywords
descriptionIdf     ``true``            IDF weighting for query description
titleKeywordsIdf   ``true``            IDF weighting for publication title and keywords
abstractIdf        ``true``            IDF weighting for publication abstract
=================  =========  =======  ===========

Concept multipliers
^^^^^^^^^^^^^^^^^^^

============================  =======  =======  =======  ===========
Parameter                     Default  Min      Max      Description
============================  =======  =======  =======  ===========
labelMultiplier               ``1.0``  ``0.0``  ``1.0``  Score multiplier for matching a concept label. Set to ``0`` to disable matching of labels.
exactSynonymMultiplier        ``1.0``  ``0.0``  ``1.0``  Score multiplier for matching a concept exact synonym. Set to ``0`` to disable matching of exact synonyms.
narrowBroadSynonymMultiplier  ``1.0``  ``0.0``  ``1.0``  Score multiplier for matching a concept narrow or broad synonym. Set to ``0`` to disable matching of narrow and broad synonyms.
definitionMultiplier          ``1.0``  ``0.0``  ``1.0``  Score multiplier for matching a concept definition. Set to ``0`` to disable matching of definitions.
commentMultiplier             ``1.0``  ``0.0``  ``1.0``  Score multiplier for matching a concept comment. Set to ``0`` to disable matching of comments.
============================  =======  =======  =======  ===========

.. _query_normalisers:

Query normalisers
^^^^^^^^^^^^^^^^^

==============================  =========  =======  =======  ===========
Parameter                       Default    Min      Max      Description
==============================  =========  =======  =======  ===========
nameNormaliser                  ``0.81``   ``0.0``  ``1.0``  Score normaliser for matching a query name. Set to ``0`` to disable matching of names.
keywordNormaliser               ``0.77``   ``0.0``  ``1.0``  Score normaliser for matching a query keyword. Set to ``0`` to disable matching of keywords.
descriptionNormaliser           ``0.92``   ``0.0``  ``1.0``  Score normaliser for matching a query description. Set to ``0`` to disable matching of descriptions.
publicationTitleNormaliser      ``0.91``   ``0.0``  ``1.0``  Score normaliser for matching a publication `title <https://pubfetcher.readthedocs.io/en/latest/fetcher.html#title>`_. Set to ``0`` to disable matching of titles.
publicationKeywordNormaliser    ``0.77``   ``0.0``  ``1.0``  Score normaliser for matching a publication `keyword <https://pubfetcher.readthedocs.io/en/latest/fetcher.html#keywords>`_. Set to ``0`` to disable matching of keywords.
publicationMeshNormaliser       ``0.75``   ``0.0``  ``1.0``  Score normaliser for matching a publication `MeSH term <https://pubfetcher.readthedocs.io/en/latest/fetcher.html#mesh>`_. Set to ``0`` to disable matching of MeSH terms.
publicationMinedTermNormaliser  ``1.0``    ``0.0``  ``1.0``  Score normaliser for matching a publication mined term (`EFO <https://pubfetcher.readthedocs.io/en/latest/fetcher.html#efo>`_, `GO <https://pubfetcher.readthedocs.io/en/latest/fetcher.html#go>`_). Set to ``0`` to disable matching of mined terms.
publicationAbstractNormaliser   ``0.985``  ``0.0``  ``1.0``  Score normaliser for matching a publication `abstract <https://pubfetcher.readthedocs.io/en/latest/fetcher.html#theabstract>`_. Set to ``0`` to disable matching of abstracts.
publicationFulltextNormaliser   ``1.0``    ``0.0``  ``1.0``  Score normaliser for matching a publication `fulltext <https://pubfetcher.readthedocs.io/en/latest/fetcher.html#fulltext>`_. Set to ``0`` to disable matching of fulltexts.
docNormaliser                   ``1.0``    ``0.0``  ``1.0``  Score normaliser for matching a query `doc <https://pubfetcher.readthedocs.io/en/latest/output.html#content-of-docs>`_. Set to ``0`` to disable matching of docs.
webpageNormaliser               ``1.0``    ``0.0``  ``1.0``  Score normaliser for matching a query `webpage <https://pubfetcher.readthedocs.io/en/latest/output.html#content-of-webpages>`_. Set to ``0`` to disable matching of webpages.
==============================  =========  =======  =======  ===========

.. _query_weights:

Query weights
^^^^^^^^^^^^^

==========================  ========  =======  ===========
Parameter                   Default   Min      Description
==========================  ========  =======  ===========
averageScaling              ``10.0``  ``0.0``  Scaling for the average strategy
nameWeight                  ``1.0``   ``0.0``  Weight of query name in average strategy. Set to ``0`` to disable matching of names in average strategy.
keywordWeight               ``1.0``   ``0.0``  Weight of query keyword in average strategy. Set to ``0`` to disable matching of keywords in average strategy.
descriptionWeight           ``1.0``   ``0.0``  Weight of query description in average strategy. Set to ``0`` to disable matching of descriptions in average strategy.
publicationTitleWeight      ``0.25``  ``0.0``  Weight of publication title_ in average strategy. Set to ``0`` to disable matching of titles in average strategy.
publicationKeywordWeight    ``0.75``  ``0.0``  Weight of publication keyword_ in average strategy. Set to ``0`` to disable matching of keywords in average strategy.
publicationMeshWeight       ``0.25``  ``0.0``  Weight of publication `MeSH term`_ in average strategy. Set to ``0`` to disable matching of MeSH terms in average strategy.
publicationMinedTermWeight  ``0.25``  ``0.0``  Weight of publication mined term (EFO_, GO_) in average strategy. Set to ``0`` to disable matching of mined terms in average strategy.
publicationAbstractWeight   ``0.75``  ``0.0``  Weight of publication abstract_ in average strategy. Set to ``0`` to disable matching of abstracts in average strategy.
publicationFulltextWeight   ``0.5``   ``0.0``  Weight of publication fulltext_ in average strategy. Set to ``0`` to disable matching of fulltexts in average strategy.
docWeight                   ``0.5``   ``0.0``  Weight of query doc_ in average strategy. Set to ``0`` to disable matching of docs in average strategy.
webpageWeight               ``0.5``   ``0.0``  Weight of query webpage_ in average strategy. Set to ``0`` to disable matching of webpages in average strategy.
==========================  ========  =======  ===========

.. _score_limits:

Score limits
^^^^^^^^^^^^

========================  =========  =======  =======  ===========
Parameter                 Default    Min      Max      Description
========================  =========  =======  =======  ===========
goodScoreTopic            ``0.63``   ``0.0``  ``1.0``  Final scores over this are considered good (in topic branch)
goodScoreOperation        ``0.63``   ``0.0``  ``1.0``  Final scores over this are considered good (in operation branch)
goodScoreData             ``0.63``   ``0.0``  ``1.0``  Final scores over this are considered good (in data branch)
goodScoreFormat           ``0.63``   ``0.0``  ``1.0``  Final scores over this are considered good (in format branch)
badScoreTopic             ``0.57``   ``0.0``  ``1.0``  Final scores under this are considered bad (in topic branch)
badScoreOperation         ``0.57``   ``0.0``  ``1.0``  Final scores under this are considered bad (in operation branch)
badScoreData              ``0.57``   ``0.0``  ``1.0``  Final scores under this are considered bad (in data branch)
badScoreFormat            ``0.57``   ``0.0``  ``1.0``  Final scores under this are considered bad (in format branch)
outputGoodScores          ``true``                     Output matches with good scores
outputMediumScores        ``true``                     Output matches with medium scores
outputBadScores           ``false``                    Output matches with bad scores
passableBadScoreInterval  ``0.04``   ``0.0``  ``1.0``  Defines the passable bad scores (the best bad scores) as scores falling inside a score interval of given length, where the upper bound is fixed to the bad score limit
passableBadScoresInTopN   ``3``      ``0``             If a match with passable bad score would be among the top given number of matches, then it is included among the suggested matches (note that matches with any bad score are always included if ``outputBadScores`` is ``true``)
topScorePartOutlier       ``42.0``   ``0.0``           If mappingStrategy_ ``average`` is used, then each non-disabled and non-empty query part will have a corresponding score part. If the score of the top score part is more than the given number of times larger than the score of the next largest score part, then the entire match will be discarded. Only done in topic and operation branches and only when there are at least two score parts and only if the publication fulltext_, doc_ or webpage_ query part is the top score part. Set to a value less than 1 to disable in all cases.
========================  =========  =======  =======  ===========

.. _response:

Response
========

The response output can contain more or less information, depending on the specified type_. The section of most interest is probably results_ in core_.

core
----

success
  ``true`` (if ``false``, then the JSON output of `Error handling`_ applies instead of the one below)
version
  ``"1"``
type
  ``"core"``
api
  URL of endpoint where request was sent
txt
  Location of plain text results file (or ``null`` if not created)
html
  Location of HTML results directory (or ``null`` if not created)
json
  Location of JSON results file
generator
  Information about the application that generated the response

  name
    Name of the application
  url
    Homepage of the application
  version
    Version of the application
time
  start
    Start time of mapping as `UNIX time <https://en.wikipedia.org/wiki/Unix_time>`_ (in milliseconds)
  startHuman
    Start time of mapping as `ISO 8601 <https://en.wikipedia.org/wiki/ISO_8601>`_ combined date and time
  stop
    Stop time of mapping as `UNIX time`_ (in milliseconds)
  stopHuman
    Stop time of mapping as `ISO 8601`_ combined date and time
  duration
    Duration of mapping in seconds
mapping
  _`query`
    id
      Unique ID assigned to the query (and by extension, to this response)
    name
      Name of tool or service (as specified in `query data`_, ``null`` if not specified)
    keywords
      Array of strings representing keywords, tags, etc (as specified in `query data`_, ``null`` if not specified)
    description
      Short description of tool or service (as specified in `query data`_, ``null`` if not specified)
    _`webpageUrls`
      Array of strings representing URLs of homepage, etc (as specified in `query data`_, ``null`` if not specified)
    _`docUrls`
      Array of strings representing URLs of documentations (as specified in `query data`_, ``null`` if not specified)
    _`publicationIds`
      Array of objects representing IDs of journal articles (as specified in `query data`_, ``null`` if not specified)

      pmid
        PMID of article
      pmcid
        PMCID of article
      doi
        DOI of article
    _`annotations`
      Array of EDAM URI strings representing existing annotations from EDAM (as specified in `query data`_, ``null`` if not specified)
  _`results`
    _`topic`
      Array of objects representing a matched term from the topic branch for the given query_, ordered by score. If no results in topic branch, then empty array. If results in topic branch were not asked for in mapping_ parameters, then ``null``.

      _`edamUri`
        EDAM URI of the matched term
      _`edamUriReplaced`
        If replaceObsolete_ is ``true`` and this is a concept replacing a matched obsolete concept, then this contains the EDAM URI of that obsolete concept (that is replaced with the concept specified in edamUri_)
      label
        EDAM label of the matched term in edamUri_
      obsolete
        ``true``, if the term in edamUri_ is obsolete; ``false`` otherwise
      _`childOf`
        Array of objects that are parents of the current matched term in edamUri_ and that test_ ``"fp"``. Absent if there are no such parents.

        edamUri
          EDAM URI of a parent described above
        label
          EDAM label of such parent
      childOfAnnotation
        Array of objects that are parents of the current matched term in edamUri_ and that test_ ``"tp"``. Same structure as in childOf_.
      childOfExcludedAnnotation
        Array of objects that are parents of the current matched term in edamUri_ and that test_ ``"fn"``. Same structure as in childOf_.
      parentOf
        Array of objects that are children of the current matched term in edamUri_ and that test_ ``"fp"``. Same structure as in childOf_.
      parentOfAnnotation
        Array of objects that are children of the current matched term in edamUri_ and that test_ ``"tp"``. Same structure as in childOf_.
      parentOfExcludedAnnotation
        Array of objects that are children of the current matched term in edamUri_ and that test_ ``"fn"``. Same structure as in childOf_.
      _`bestOneQuery`
        Best matched query part. Basis for bestOneScore_ calculation and score class_ determination using `Score limits`_ parameters. Basis for final score_ calculation if mappingStrategy_ is ``"best"``. Otherwise (if mappingStrategy_ is ``"average"``), all query parts will be used for calculating final score (use type_ ``"full"`` to see these partial scores). If replaceObsolete_ is ``true`` and this is a concept replacing a matched obsolete concept, then will contain match information of the obsolete concept specified in edamUriReplaced_ and not the actually suggested concept in edamUri_.

        type
          Name of the type of the query part
        url
          URL of best matched webpage/doc/publication. Absent, if type is not webpage, doc or some publication type.
        value
          Value of best matched keyword or publication keyword. Absent, if type is not keyword or some publication keyword type.
      _`bestOneConcept`
        Term part the best matched query part (bestOneQuery_) matched with

        type
          Name of the type of the term part
        value
          Content of the term part. Absent, if type is ``"none"``.
      score
        _`class`
          One of ``"good"``, ``"medium"``, ``"bad"``. Calculated based on `Score limits`_ parameters and the match score between bestOneQuery_ and bestOneConcept_.
        _`bestOneScore`
          If mappingStrategy_ is ``"average"``, then the match score between bestOneQuery_ and bestOneConcept_ will be stored here. If mappingStrategy_ is not ``"average"``, then will have negative value.
        withoutPathScore
          If parentWeight_ and pathWeight_ are above ``0``, then the non path enriched score will be stored here. Otherwise will have negative value.
        _`score`
          Final score of the match (to edamUriReplaced_, if it exists, or to edamUri_ otherwise)
      _`test`
        ``"tp"``, if term was matched and also specified as existing annotation in the query; ``"fp"``, if term was matched, but not specified as existing annotation in query; ``"fn"``, if term was not matched, but was specified as existing annotation in query
    _`operation`
      Same structure as in topic_, but for terms matched from the operation branch
    _`data`
      Same structure as in topic_, but for terms matched from the data branch
    _`format`
      Same structure as in topic_, but for terms matched from the format branch
_`args`
  The Parameters_

  mainArgs
    Main parameters

    edam
      Filename of the used EDAM ontology OWL file
    txt
      ``true``, if output of plain text results was requested; ``false`` otherwise
    html
      ``true``, if output of HTML results was requested; ``false`` otherwise
    json
      Always ``true``
  processorArgs
    Processing parameters

    fetching
      Always ``true``
    db
      Name of the used `database <https://pubfetcher.readthedocs.io/en/latest/output.html#database>`_ file
    idf
      Name of the used :ref:`IDF <idf>` file
    idfStemmed
      Name of the used stemmed :ref:`IDF <idf>` file
  preProcessorArgs
    Preprocessing_ parameters
  fetcherArgs
    Fetching_ parameters (implemented in PubFetcher_)
  mapperArgs
    Mapping_ parameters

    algorithmArgs
      `Mapping algorithm`_ parameters
    idfArgs
      IDF_ parameters
    multiplierArgs
      `Concept multipliers`_ parameters
    normaliserArgs
      `Query normalisers`_ parameters
    weightArgs
      `Query weights`_ parameters
    scoreArgs
      `Score limits`_ parameters
_`tool`
  Present, if `query data`_ was supplied as `bio.tools input`_. The structure and content of this object is the same as in the object supplied as part of the query, except that ``null`` and empty values are removed. In addition, results_ from the topic_ branch are added to the `topic attribute <https://biotools.readthedocs.io/en/latest/curators_guide.html#topic>`_ and results_ from the operation_ branch are added under a new `function group <https://biotools.readthedocs.io/en/latest/curators_guide.html#function-group>`_ object. Results from the data_ and format_ branches should be added under the ``"input"`` and ``"output"`` attributes of a function group, however EDAMmap can't differentiate between inputs and outputs. Thus, new terms from the data_ and format_ branches will be added as strings (in the form ``"EDAM URI (label)"``, separated by ``" | "``) to the `note <https://biotools.readthedocs.io/en/latest/curators_guide.html#note-function>`_ of the last function group object.

full
----

The type_ ``"full"`` includes everything from core_, plus the following:

mapping
  queryFetched
    _`webpages`
      Array of metadata objects corresponding to webpageUrls_ in query_. Webpages are implemented in PubFetcher_ and thus are described in its documentation: `Content of webpages <https://pubfetcher.readthedocs.io/en/latest/output.html#content-of-webpages>`_. The structure of webpages here will be the same as described in PubFetcher, except for `content <https://pubfetcher.readthedocs.io/en/latest/output.html#webpage-content>`_ which will be missing. The values of `startUrl <https://pubfetcher.readthedocs.io/en/latest/output.html#starturl>`_ of webpages will be the URLs given in webpageUrls_ in query_.
    _`docs`
      Array of metadata objects corresponding to docUrls_ in query_. Structure of objects same as in webpages_.
    _`publications`
      Array of metadata objects corresponding to publicationIds_ in query_. Publications are implemented in PubFetcher_ and thus are described in its documentation: `Content of publications <https://pubfetcher.readthedocs.io/en/latest/output.html#content-of-publications>`_. The structure of publications here will be the same as described in PubFetcher, except for fulltext_ which will be missing.
  results
    topic/operation/data/format
      Array of objects defined in topic_, i.e. the same content as in core_, plus the field parts_ defined below.

      _`parts`
        Array of objects representing scores from each query part that are used in calculating the final score (using weights from `Query weights`_ parameters), in case mappingStrategy_ is ``"average"``. Absent, if mappingStrategy_ is not ``"average"``.

        queryMatch
          type
            Name of the type of the query part
          url
            URL of best matched webpage/doc/publication. Absent, if type is not webpage, doc or some publication type.
          value
            Value of best matched keyword or publication keyword. Absent, if type is not keyword or some publication keyword type.
          score
            Intermediate score of matching to query part from all concept term parts
        conceptMatch
          type
            Name of the type of the term part
          value
            Content of the term part. Absent, if type is ``"definition"``, ``"comment"`` or ``"none"``.
          score
            Intermediate score of matching to concept term part from query part
        score
          Score of the part
counts
  conceptsSize
    Total number of concepts in the used EDAM ontology
  topicSize
    Number of concepts in the topic branch
  operationSize
    Number of concepts in the operation branch
  dataSize
    Number of concepts in the data branch
  formatSize
    Number of concepts in the format branch
  _`queriesSize`
    Number of queries. Always ``1``. Can be bigger in output of :ref:`EDAMmap-CLI <cli>`.
  _`resultsSize`
    Number of results. Always ``1``. Can be bigger in output of :ref:`EDAMmap-CLI <cli>`.
  _`tp`
    topic
      Number of matched terms from the topic branch that test_ ``"tp"``
    operation
      Number of matched terms from the operation branch that test_ ``"tp"``
    data
      Number of matched terms from the data branch that test_ ``"tp"``
    format
      Number of matched terms from the format branch that test_ ``"tp"``
    total
      Total number of matched terms that test_ ``"tp"``
  fp
    Same structure as in tp_, but for matched terms that test_ ``"fp"``
  fn
    Same structure as in tp_, but for matched terms that test_ ``"fn"``
_`measures`
  Measures of EDAMmap performance against existing annotations_ provided in query_. Does not make much sense in case of one query-results pair (if queriesSize_ and resultsSize_ are ``1``), but included for completeness.

  _`precision`
    `The precision <https://en.wikipedia.org/wiki/Precision_and_recall#Precision>`_

    topic
      Precision in the topic branch
    operation
      Precision in the operation branch
    data
      Precision in the data branch
    format
      Precision in the format branch
    total
      Precision over all branches
  recall
    `Recall <https://en.wikipedia.org/wiki/Precision_and_recall#Recall>`_. Same structure as in precision_.
  f1
    `F1 score <https://en.wikipedia.org/wiki/F1_score>`_. Same structure as in precision_.
  f2
    `F2 score <https://en.wikipedia.org/wiki/F1_score>`_. Same structure as in precision_.
  Jaccard
    `Jaccard index <https://en.wikipedia.org/wiki/Jaccard_index>`_. Same structure as in precision_.
  AveP
    `Average precision <https://en.wikipedia.org/wiki/Evaluation_measures_(information_retrieval)#Average_precision>`_. Same structure as in precision_.
  RP
    `R-precision <https://en.wikipedia.org/wiki/Evaluation_measures_(information_retrieval)#R-Precision>`_. Same structure as in precision_.
  DCG
    `Discounted cumulative gain <https://en.wikipedia.org/wiki/Discounted_cumulative_gain>`_. Same structure as in precision_.
  DCGa
    `DCG (alternative) <https://en.wikipedia.org/wiki/Discounted_cumulative_gain>`_. Same structure as in precision_.

.. _api_examples:

Examples
========

One way to test the API is to send JSON data using ``curl``. For example, for sending the input:

.. code-block:: json

  {
    "name": "aTool"
  }

issue the command:

.. code-block:: bash

  $ curl -H "Content-Type: application/json" -X POST -d '{"name":"aTool"}' https://biit.cs.ut.ee/edammap/api

In the output, no results can be seen:

.. code::

  "results" : {
    "topic" : [ ],
    "operation" : [ ],
    "data" : null,
    "format" : null
  }

Which is not surprising, given only the tool name was supplied ("aTool"), which is too little for EDAMmap to work with.

A more meaningful input might look like this:

.. code-block:: json

  {
    "name": "g:Profiler",
    "keywords": [ "gene set enrichment analysis", "Gene Ontology" ],
    "description": "A web server for functional enrichment analysis and conversions of gene lists.",
    "webpageUrls": [ "https://biit.cs.ut.ee/gprofiler/" ],
    "docUrls": [ "https://biit.cs.ut.ee/gprofiler/help.cgi" ],
    "publicationIds": [
      "17478515\t\t10.1093/nar/gkm226",
      {
        "pmcid": "PMC3125778"
      },
      {
        "pmid": "27098042",
        "doi": "10.1093/nar/gkw199"
      }
    ],
    "annotations": [
      "http://edamontology.org/topic_1775",
      "operation_2436",
      "data_3021",
      "http://edamontology.org/format_1964"
    ],
    "branches": [ "topic", "operation", "data", "format" ],
    "matches": 6,
    "obsolete": true
  }

For testing, this input could be saved in a file, e.g. ``input.json``, and then the following command run:

.. code-block:: bash

  $ curl -H "Content-Type: application/json" -X POST -d '@/path/to/input.json' https://biit.cs.ut.ee/edammap/api

To supply the same data (except the "keywords") as `bio.tools input`_, the following could be used:

.. code-block:: json

  {
    "tool": {
      "name": "g:Profiler",
      "description": "A web server for functional enrichment analysis and conversions of gene lists.",
      "homepage": "https://biit.cs.ut.ee/gprofiler/",
      "documentation": [{
        "url": "https://biit.cs.ut.ee/gprofiler/help.cgi",
        "type": "General",
        "note": null
      }],
      "publication": [{
        "pmid": "17478515",
        "pmcid": null,
        "doi": "10.1093/nar/gkm226"
      },{
        "pmcid": "PMC3125778"
      },{
        "pmid": "27098042",
        "pmcid": null,
        "doi": "10.1093/nar/gkw199"
      }],
      "topic": [{
        "term": "Function analysis",
        "uri": "http://edamontology.org/topic_1775"
      }],
      "function": [{
        "operation": [{
          "term": "Gene-set enrichment analysis",
          "uri": "http://edamontology.org/operation_2436"
        }],
        "input": [{
          "data": {
            "uri": "http://edamontology.org/data_3021"
          },
          "format": [{
            "uri": "http://edamontology.org/format_1964"
          }]
        }],
        "output": null
      }]
    },
    "branches": [ "topic", "operation", "data", "format" ],
    "matches": 6,
    "obsolete": true
  }


.. _prefetching:

***********
Prefetching
***********

Once a query has been received by the API, content corresponding to webpageUrls_, docUrls_ and publicationIds_ has to be `fetched <https://pubfetcher.readthedocs.io/en/latest/fetcher.html>`_ (unless it has been fetched and stored in some previous occurrence), before mapping can take place.

This content could be prefetched and prestored in the database_ as a separate step, before the mapping query is sent. This is useful in the web application, where content can be fetched as soon as the user has entered the corresponding query details, and thus mapping time could be less when the entire query form is finally submitted. It might be of less use in the API, but has been included nevertheless.

/api/web
========

Request
-------

Links, whose content is to be prefetched, are specified as an array of strings under the JSON key webpageUrls_.

In addition to webpageUrls_, parameters from Fetching_ can be used, as these can influence the fetching.

Response
--------

The main result of the query is not the content of the response itself, but the fact that the contents of the requested links were stored in the database_ on the server. However, some informational output is still provided.

success
  ``true`` (if ``false``, then the JSON output of `Error handling`_ applies instead of the one below)
webpageUrls
  Array of objects describing the completeness of the content of each link on the server

  id
    A webpage URL specified in the request
  status
    The status of that webpage. One of "`broken <https://pubfetcher.readthedocs.io/en/latest/output.html#broken>`_", "`empty <https://pubfetcher.readthedocs.io/en/latest/output.html#webpage-empty>`_", "non-`usable <https://pubfetcher.readthedocs.io/en/latest/output.html#webpage-usable>`_", "non-`final <https://pubfetcher.readthedocs.io/en/latest/output.html#webpage-final>`_", "`final <https://pubfetcher.readthedocs.io/en/latest/output.html#webpage-final>`_".

/api/doc
========

Analogous to `/api/web`_, except for documentation and that the JSON key docUrls_ has to be used.

/api/pub
========

Request
-------

Journal articles, whose content is to be prefetched, are specified using a PMID and/or PMCID and/or DOI. This is done as an array of strings and objects under the JSON key publicationIds_. If the ID is specified as a string, it has to be in the form ``"<PMID>\t<PMCID>\t<DOI>"``. If it is specified as an object, the keys ``"pmid"``, ``"pmcid"``, ``"doi"`` are to be used.

In addition to publicationIds_, parameters from Fetching_ can be used, as these can influence the fetching.

Response
--------

The main result of the query is not the content of the response itself, but the fact that the contents of the requested articles were stored in the database_ on the server. However, some informational output is still provided.

success
  ``true`` (if ``false``, then the JSON output of `Error handling`_ applies instead of the one below)
publicationIds
  Array of objects describing the completeness of the content of each article on the server

  id
    IDs describing one publication specified in the request

    pmid
      The PMID of the publication
    pmcid
      The PMCID of the publication
    doi
      The DOI of the publication
  status
    The status of that publication. One of `"empty" <https://pubfetcher.readthedocs.io/en/latest/output.html#publication-empty>`_, "non-`usable" <https://pubfetcher.readthedocs.io/en/latest/output.html#publication-usable>`_, "non-`final" <https://pubfetcher.readthedocs.io/en/latest/output.html#publication-final>`_, `"final" <https://pubfetcher.readthedocs.io/en/latest/output.html#publication-final>`_, `"totally final" <https://pubfetcher.readthedocs.io/en/latest/output.html#totallyfinal>`_.

Example
=======

Try to prefetch the publication with PMID "23479348" and PMCID "PMC3654706", increasing connect and read `timeout <https://pubfetcher.readthedocs.io/en/latest/cli.html#timeout>`_ to give the server more time to fetch the whole publication:

.. code-block:: bash

  $ curl -H "Content-Type: application/json" -X POST -d '{"publicationIds":["23479348\tPMC3654706\t"],"timeout":30000}' https://biit.cs.ut.ee/edammap/api/pub

Sample output:

.. code-block:: json

  {
    "success" : true,
    "publicationIds" : [ {
      "id" : {
        "pmid" : "23479348",
        "pmcid" : "PMC3654706",
        "doi" : "10.1093/BIOINFORMATICS/BTT113"
      },
      "status" : "final"
    } ]
  }


.. _error_handling:

**************
Error handling
**************

If ``"success"`` is ``true`` in the JSON response_, then HTTP status code was "200 OK" and the rest of the JSON is in the format described above.

If ``"success"`` is ``false`` in the JSON response_, then something has gone wrong, the HTTP status code is `400 Bad Request`_ or `500 Internal Server Error`_ and the rest of the JSON will be in one of the following formats.

400 Bad Request
===============

Status code 400 means something was done wrong on the client side (syntax error, bad parameter value, etc) and the error should be fixed by the client, before another attempt is made.

The output JSON will have the following format:

success
  ``false``
status
  ``400``
message
  A string describing the error
time
  Timestamp string (as `ISO 8601`_ combined date and time) when the error occurred

500 Internal Server Error
=========================

Status code 500 is a catch all for all other errors. Usually, it should be some problem on the server side. It might be temporary, so another try later might result in success. It might also be an unforeseen problem on the client side. There's a strong chance there is a bug somewhere, so feedback with a timestamp is appreciated (to `GitHub issues <https://github.com/edamontology/edammap/issues/>`_ or by contacting the author).

The output JSON will have the following format:

success
  ``false``
status
  ``500``
time
  Timestamp string (as `ISO 8601`_ combined date and time) when the error occurred

Examples
========

Syntax error in JSON
--------------------

.. code-block:: bash

  $ curl -H "Content-Type: application/json" -X POST -d '{"name"}' https://biit.cs.ut.ee/edammap/api

.. code-block:: json

    {
        "success": false,
        "status": 400,
        "message": "Invalid token=CURLYCLOSE at (line no=1, column no=8, offset=7). Expected tokens are: [COLON]",
        "time": "2018-05-28T12:59:57.389Z"
    }

Bad parameter value
-------------------

.. code-block:: bash

  $ curl -H "Content-Type: application/json" -X POST -d '{"name":"test","goodScoreTopic":2}' https://biit.cs.ut.ee/edammap/api

.. code-block:: json

    {
        "success": false,
        "status": 400,
        "message": "Param 'goodScoreTopic=2.0' is above limit 1.0",
        "time": "2018-05-28T13:02:53.616Z"
    }

Some other illegal requests
---------------------------

.. code-block:: bash

  $ curl -H "Content-Type: application/json" -X POST -d '{"name":"test","annotations":["http://edamontology.org/1775"]}' https://biit.cs.ut.ee/edammap/api

.. code-block:: json

    {
        "success": false,
        "status": 400,
        "message": "Illegal EDAM URI: http://edamontology.org/1775",
        "time": "2018-05-28T14:07:50.164Z"
    }

.. code-block:: bash

  $ curl -H "Content-Type: application/json" -X POST -d '{"name":"test","publicationIds":["23479348\tPMC3654706"]}' https://biit.cs.ut.ee/edammap/api

.. code-block:: json

    {
        "success": false,
        "status": 400,
        "message": "Publication ID has illegal number of parts (2), first part is 23479348",
        "time": "2018-05-28T14:09:04.032Z"
    }

.. code-block:: bash

  $ curl -H "Content-Type: application/json" -X POST -d '{"name":"test","webpageUrls":["biit.cs.ut.ee/gprofiler"]}' https://biit.cs.ut.ee/edammap/api

.. code-block:: json

    {
        "success": false,
        "status": 400,
        "message": "Malformed URL: biit.cs.ut.ee/gprofiler",
        "time": "2018-05-28T14:10:23.651Z"
    }
