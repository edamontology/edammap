
.. _manual:

######
Manual
######


.. _setup:

*****
Setup
*****

Compilation instructions can be found in `INSTALL <https://github.com/edamontology/edammap/blob/master/INSTALL.md>`_.

For running the mapping, the latest EDAM ontology is required (in OWL format) -- it can be downloaded from http://edamontology.org/page.

The query input to EDAMmap can contain publication IDs and web page URLs, but not the actual content of publications and web pages -- this will have to be fetched. This fetched content could be saved in a file for potential later reuse. More information about that file (an on-disk key-value store) can be found in `PubFetcher <https://github.com/edamontology/pubfetcher>`_'s documentation: `Database <https://pubfetcher.readthedocs.io/en/latest/output.html#database>`_. To generate an initial empty database:

.. code-block:: bash

  $ java -jar edammap-util-<version>.jar -db-init db.db

.. _idf:

IDF
===

For potentially better mapping results, `tf–idf <https://en.wikipedia.org/wiki/Tf%E2%80%93idf>`_ weighting could be applied to raise the importance of more meaningful words. To use tf–idf, a file with normalised IDF scores of words is required. This file should be generated based on a large number of entries that are similar (or from a similar domain) to those later input to EDAMmap. As an example, IDF files generated based on all entries of `bio.tools <https://bio.tools/>`_ (at some point in time) are provided: `biotools.idf <https://github.com/edamontology/edammap/blob/master/doc/biotools.idf>`_ (stemming has not been applied to words) and `biotools.stemmed.idf <https://github.com/edamontology/edammap/blob/master/doc/biotools.stemmed.idf>`_ (stemming has been applied).

If so wished, the IDF files based on bio.tools content can be generated from scratch instead of using the ones provided. This will take several hours.

First, all content from bio.tools can be downloaded with:

.. code-block:: bash

  $ java -jar edammap-util-<version>.jar -biotools-full biotools.json

Next, content for publications, webpages and docs is fetched (``-db-fetch`` is documented in PubFetcher's documentation at `Get content <https://pubfetcher.readthedocs.io/en/latest/cli.html#get-content>`_):

.. code-block:: bash

  $ java -jar edammap-util-<version>.jar -pub-query biotools.json --query-type biotools -db-fetch db.db --log pub.log
  $ java -jar edammap-util-<version>.jar -web-query biotools.json --query-type biotools -db-fetch db.db --log web.log
  $ java -jar edammap-util-<version>.jar -doc-query biotools.json --query-type biotools -db-fetch db.db --log doc.log

or alternatively, with a single command:

.. code-block:: bash

  $ java -jar edammap-util-<version>.jar -all-query biotools.json --query-type biotools -db-fetch db.db --log all.log

And as last step, the wanted IDF files are generated:

.. code-block:: bash

  $ java -jar edammap-util-<version>.jar -make-idf biotools.json db.db biotools.idf
  $ java -jar edammap-util-<version>.jar -make-idf-stemmed biotools.json db.db biotools.stemmed.idf

Another reason to generated own IDF files might be, that the inputs to be annotated with EDAMmap are from a different field and not meant for bio.tools. Then, the queries input from ``biotools.json`` should be replaced with the different collection of entries from that different domain.


.. _input:

*****
Input
*****

An input query fed to EDAMmap can have the following parts: *id*, *name*, *list of keywords*, *description*, *webpage URLs*, *documentation URLs*, *publication IDs*, *existing EDAM annotations*. The *name*, *keywords* and *description* are strings describing the tool to be annotated. The *id* can be used as an optional identificator for the tool and (unlike *name*) its content will not be fed to the mapping algorithm. Content corresponding to *URLs* and *IDs* will need to be fetched by leveraging the PubFetcher_ library. *Existing manual annotations* can be specified to do benchmarking of EDAMmap or for example to exclude already existing annotations from results.

.. _QueryType:

How query parts are read from an input file depends on the type of the input file (specified with ``--query-type``).

For example, using ``--query-type biotools`` means that the input file is a JSON file containing entries adhering to the `biotoolsSchema <https://biotoolsschema.readthedocs.io/>`_ (as returned by https://bio.tools/api/tool?format=json). The tool *name* is found from ``"name"``, *publication IDs* are picked from ``"publication"``, *existing annotation* are found in ``"topic"`` and ``"function"``, etc. Other, non-relevant fields in the JSON are ignored.

.. _csv:

CSV
===

For self-generated input, using a generic CSV file should be easier. This can be specified with ``--query-type generic`` (or it can also be omitted, as it is the default).

The field delimiter character in the CSV file is ``,``, the character used for escaping values where the field delimiter is part of the value is ``"`` and the character used for escaping quotes inside an already quoted value is also ``"``. Lines are separated with ``\n`` (Unix end-of-line) and empty lines and lines beginning with ``#`` are skipped. The maximum number of characters allowed for any given value is 100000. Within fields, multiple *keywords*, *webpage URLs*, *documentation URLs*, *publication IDs* and *existing annotation* can be separated with ``|`` (which means this character can't be used as part of the values of these query parts).

The first line of the CSV file must be the header line describing the columns, with the following content: ``id,name,keywords,description,webpageUrls,docUrls,publicationIds,annotations``. But columns, along with their corresponding header entries, can be omitted, as not all query parts have to be be used for mapping. For example, to do simple one input string to EDAM term matching, only the *name* part could be filled. Or if only short descriptions are available about tools, only *description* and *name* could be filled (filling the *name* is mandatory). Also, the order of the fields in the header line can be changed (as long as column data matches with its header).

An example generic input CSV file ``example.csv``, with only one tool called "g:Profiler", is the following::

  name,keywords,description,webpageUrls,docUrls,publicationIds,annotations
  g:Profiler,gene set enrichment analysis|Gene Ontology,"A web server for functional enrichment analysis, and conversions of gene lists.",https://biit.cs.ut.ee/gprofiler/,https://biit.cs.ut.ee/gprofiler/help.cgi,17478515|PMC3125778|10.1093/nar/gkw199,http://edamontology.org/topic_1775|operation_2436|data_3021|http://edamontology.org/format_1964

.. note::
  Specifying the prefix ``http://edamontology.org/`` is optional for existing annotations.
.. note::
  Only one ID can be specified for one publication, either a PubMed ID, a PubMed Central ID or a DOI (in the example, ``17478515|PMC3125778|10.1093/nar/gkw199`` are three different publications).


.. _parameters:

**********
Parameters
**********

Mapping can be influenced by various changeable parameters, which on the command line can be specified as ``--parameter value``. Most of these parameters are documented under :ref:`EDAMmap API Parameters <api_parameters>`.

:ref:`Preprocessing <preprocessing>` parameters influence the tokenisation of the input. :ref:`Fetching <fetching>` parameters influence the fetching of publications, webpages and docs. And :ref:`Mapping <mapping>` parameters influence the mapping algorithm and outputting of the results (more about the mapping algorithm can be found in section 3.10 of the `thesis <https://github.com/edamontology/edammap/blob/master/doc/Automatic%20mapping%20of%20free%20texts%20to%20bioinformatics%20ontology%20terms.pdf>`_).

In addition, there are some parameters that can't be changed through the API, but can be changed on the command line. These are the `Fetching private <https://pubfetcher.readthedocs.io/en/latest/cli.html#fetching-private>`_ parameters (from PubFetcher_) and the Processing parameters, documented in the table below.

Processing
==========

================  ========  ===========
Parameter         Default   Description
================  ========  ===========
``--fetching``    ``true``  Fetch `publications <https://pubfetcher.readthedocs.io/en/latest/output.html#content-of-publications>`_, `webpages <https://pubfetcher.readthedocs.io/en/latest/output.html#content-of-webpages>`_ and `docs <https://pubfetcher.readthedocs.io/en/latest/output.html#content-of-docs>`_ (corresponding to given publication IDs, webpage URLs and doc URLs); if ``false``, then only the `database <https://pubfetcher.readthedocs.io/en/latest/output.html#database>`_ is used for getting them (if a database is given with ``--db``)
``--db``                    Use the given `database <https://pubfetcher.readthedocs.io/en/latest/output.html#database>`_ for getting and storing `publications <https://pubfetcher.readthedocs.io/en/latest/output.html#content-of-publications>`_, `webpages <https://pubfetcher.readthedocs.io/en/latest/output.html#content-of-webpages>`_ and `docs <https://pubfetcher.readthedocs.io/en/latest/output.html#content-of-docs>`_ (corresponding to given publication IDs, webpage URLs and doc URLs); if a database is given, then it is queried first even if fetching is enabled with ``--fetching`` (and fetching is done only if `required and possible <https://pubfetcher.readthedocs.io/en/latest/fetcher.html#can-fetch>`_ for found database entry)
``--idf``                   Use the given query IDF_ file (when stemming is not enabled); if not specified, weighting of queries with IDF scores will be disabled (when stemming is not enabled)
``--idfStemmed``            Use the given query IDF_ file (when stemming is enabled); if not specified, weighting of queries with IDF scores will be disabled (when stemming is enabled)
================  ========  ===========


.. _results:

*******
Results
*******

The output results will contain the requested :ref:`matches <matches>` number (or less, if scores are too low) of best terms (described by their EDAM URI and label) from the requested :ref:`branches <branches>` ordered by :ref:`score <score>` within each branch, output along with intermediate match scores. Depending on the output type, results can additionally contain extra information about :ref:`query part to concept part matches <parts>` that form the final score and contain also matched parent and child terms, the supplied :ref:`query <query>`, the used :ref:`parameters <args>` and information about the fetched :ref:`webpages <webpages>`, :ref:`docs <docs>` and :ref:`publications <publications>`. Results can also contain benchmarking :ref:`measures <measures>` which might be helpful in evaluating the performance of EDAMmap and in choosing optimal parameter values (benchmarking results can only make sense if any existing manually added :ref:`annotations <annotations>` were supplied with the query).

Results can be output into a JSON file, a directory containing HTML files and/or a plain text file. The content and structure of the JSON output is documented under the :ref:`Response <response>` section of the EDAMmap API documentation. If the JSON output is obtained through running EDAMmap on the command-line (instead of querying through the API), then the *type* in the JSON output will be ``"cli"`` instead of ``"core"`` or ``"full"`` and the *api*, *txt*, *html* and *json* fields will be missing, but otherwise the output structure will be the same as for the ``"full"`` API response. The HTML output will contain the same information as the ``"full"`` JSON output, but rendered in a nice way in a web browser with clickable links to outside resources.

The plain text output will contain minimal information besides the matched terms. After the initial header line labelling the columns it will contain one line for each matched term with the following tab-separated values:

query_id
  The *id* of the query
query_name
  The *name* of the tool in the query
edam_branch
  The EDAM branch the matched term is from (one of ``topic``, ``operation``, ``data``, ``format``)
edam_uri
  The EDAM URI of the matched term
edam_label
  The EDAM label of the matched term
edam_obsolete
  ``true``, if the term is obsolete; ``false`` otherwise
_`best_one_query`
  Name of the type of the best matched query part
_`best_one_concept`
  Name of the type of the EDAM term part the best matched query part matched with
best_one_score
  If :ref:`mappingStrategy <mappingstrategy>` is "average", then the match score of best_one_query_ and best_one_concept_ will be stored here. If :ref:`mappingStrategy <mappingstrategy>` is not "average", then it will have a negative value.
without_path_score
  If :ref:`parentWeight <parentweight>` and :ref:`pathWeight <pathweight>` are above 0, then the non-path-enriched score will be stored here. Otherwise it will have a negative value.
score
  The final score of the match
test
  ``tp``, if term was matched and also specified as existing annotation in the query; ``fp``, if term was matched, but not specified as existing annotation in query; ``fn``, if term was not matched, but was specified as existing annotation in query


.. _cli:

***********
EDAMmap-CLI
***********

EDAMmap can be run as a command-line tool with the input being a JSON or CSV local file or URL resource (with the file contents described in the Input_ section) and with the results_ being output to the specified JSON, HTML and/or plain text files. The query can consist of many tools and the mapping process will be multi-threaded.

All command-line arguments suppliable to EDAMmap can be seen with:

.. code-block:: bash

  $ java -jar edammap-cli-<version>.jar -h

The output will be rather long, as it contains all parameters described in the `Parameters`_ section. In addition to these parameters, EDAMmap-CLI accepts arguments described in the following table (entries marked with * are mandatory).

==========================  ==========================  ===========  ===========
Parameter                   Parameter args              Default      Description
==========================  ==========================  ===========  ===========
``--edam`` or ``-e`` *      *<file path>*                            Path of the EDAM ontology file
``--query`` or ``-q`` *     *<file path or URL>*                     Path or URL of file containing input queries of QueryType ``--type``
``--type`` or ``-t``        *<QueryType>*               ``generic``  Specifies the type of the query and how to output the results. Possible values: ``generic``, ``SEQwiki``, ``msutils``, ``Bioconductor``, ``biotools14``, ``biotools``, ``server``.
``--output`` or ``-o``      *<file path>*                            Text file to write results to, one per line. If missing (and HTML report also not specified), then results will be written to standard output.
``--report`` or ``-r``      *<directory path>*                       Directory to write a HTML report to. In addition to detailed results, it will contain used parameters, metrics, comparisons to manual mapping, extended information about queries and nice formatting. The specified directory will be created and must not be an existing directory.
``--json`` or ``-j``        *<file path>*                            File to write results to, in JSON format. Will include the same info as the HTML report.
``--reportPageSize``        *<positive integer>*        ``100``      Number of results in a HTML report page. Setting to 0 will output all results to a single HTML page.
``--reportPaginationSize``  *<positive integer>*        ``11``       Number of pagination links visible before/after the current page link in a HTML report page. Setting to 0 will make all pagination links visible.
``--threads``               *<positive integer>*        ``4``        How many threads to use for mapping (one thread processes one query at a time)
==========================  ==========================  ===========  ===========

So, for example, to map the example tool ("g:Profiler") defined in the `Input`_ section (in ``example.csv``), the following command could be run:

.. code-block:: bash

  $ java -jar edammap-cli-<version>.jar -e EDAM_1.21.owl -q example.csv -r gprofiler --idfStemmed biotools.stemmed.idf -l gprofiler.log

Contents for webpages_, docs_ and publications_ described in the query ``example.csv`` will be `fetched <https://pubfetcher.readthedocs.io/en/latest/fetcher.html>`_ (but not stored for potential later reuse, as no database_ file is specified), the IDF file ``biotools.stemmed.idf`` obtained in the `Setup`_ section (where words are stemmed as by default ``--stemming`` is ``true``) will be used as an input to the mapping algorithm and results_ will be output to the HTML file ``gprofiler/index.html``, with `log lines <https://pubfetcher.readthedocs.io/en/latest/output.html#log-file>`_ of the whole process appended to ``gprofiler.log``.

Another example is the mapping of the whole content of bio.tools:

.. code-block:: bash

  $ java -jar edammap-cli-<version>.jar -e EDAM_1.21.owl -q biotools.json -t biotools -o results.txt -r results -j results.json --threads 8 --fetching false --db db.db --idfStemmed biotools.stemmed.idf --branches topic operation data format --matches 5 --obsolete true --log biotools.log

The query ``biotools.json`` is the whole content of bio.tools as obtained with the ``-biotools-full`` command of `EDAMmap-Util`_. Contents of webpages, docs and publications has been pre-fetched to the database file ``db.db`` (as described under IDF_), thus ``--fetching`` is disabled. Results will be output as plain text to ``results.txt``, as HTML files to the directory ``results`` and as JSON to ``results.json``. Results will contain 5 term matches from each EDAM branch and can include obsolete concepts. As EDAMmap was run on the whole content of bio.tools, then the benchmarking results can be consulted to assess the performance and as webpages, docs and publications have been stored on disk, then EDAMmap can easily be re-run while varying the parameters to tune these results.

Instead of specifying the parameters as part of the command line, they could be stored in a configuration file. An initial configuration file, with all parameters commented out, can be generated with:

.. code-block:: bash

  $ java -jar edammap-util-<version>.jar -make-options-conf options.conf

In the ensuing file, ``#`` should be removed from the front of all mandatory parameters and all parameters whose default value should be changed. In the configuration file, parameters and parameter values are separated by newline characters (instead of spaces). Now, EDAMmap can be run as:

.. code-block:: bash

  $ java -jar edammap-cli-<version>.jar @options.conf


.. _server:

**************
EDAMmap-Server
**************

EDAMmap can also be run as a web server. A query can then be input with a HTML form in a web application or posted as JSON to an :ref:`API <api>`. However, in contrast to `EDAMmap-CLI`_, only one query at a time can be submitted this way.

All command-line arguments suppliable to an EDAMmap server can be seen with:

.. code-block:: bash

  $ java -jar edammap-server-<version>.jar -h

In addition to Processing_ and `Fetching private`_ parameters, EDAMmap Server accepts arguments described in the following table (entries marked with * are mandatory).

=======================  ==========================  =========================  ===========
Parameter                Parameter args              Default                    Description
=======================  ==========================  =========================  ===========
``--edam`` or ``-e`` *   *<file path>*                                          Path of the EDAM ontology file
``--txt``                *<boolean>*                 ``true``                   Output results to a plain text file for queries made through the web application. The value can be changed in the web application itself.
``--json``               *<boolean>*                 ``false``                  Output results to a JSON file for queries made through the web application. The value can be changed in the web application itself.
``--baseUri`` or ``-b``  *<string>*                  ``http://localhost:8080``  URI where the server will be deployed (as schema://host:port)
``--path`` or ``-p``     *<string>*                  ``edammap``                Path where the server will be deployed (only one single path segment supported)
``--httpsProxy``                                                                Use if we are behind a HTTPS proxy
``--files`` or ``-f`` *  *<directory path>*                                     An existing directory where the results will be output. It must contain required CSS, JavaScript and font resources pre-generated with `EDAMmap-Util`_.
``--fetchingThreads``    *<positive integer>*        ``8``                      How many threads to create (maximum) for fetching individual database entries of one query
=======================  ==========================  =========================  ===========

To setup the server version of EDAMmap, a new directory with required CSS, JavaScript and font resources must be created:

.. code-block:: bash

  $ java -jar edammap-util-<version>.jar -make-server-files files

If wanted (i.e. if ``--db`` will be used when running the server), an initial empty database_ for storing fetched_ webpages_, docs_ and publications_ can also be created:

.. code-block:: bash

  $ java -jar edammap-util-<version>.jar -db-init server.db

EDAMmap-Server can now be run with:

.. code-block:: bash

  $ java -jar edammap-server-<version>.jar -b http://127.0.0.1:8080 -p edammap -e EDAM_1.21.owl -f files --fetching true --db server.db --idf biotools.idf --idfStemmed biotools.stemmed.idf --log serverlogs

The web application can now be accessed locally at http://127.0.0.1:8080/edammap and the :ref:`API <api>` is at http://127.0.0.1:8080/edammap/api. How to obtain the IDF files ``biotools.idf`` and ``biotools.stemmed.idf`` is described in the `Setup`_ section. In contrast to the other EDAMmap tools, the server will not log to a single `log file <https://pubfetcher.readthedocs.io/en/latest/output.html#log-file>`_, but with ``-l`` or ``--log`` a directory can be defined where log files, that are rotated daily, will be stored. The log directory will also contain daily rotated access logs compatible with Apache's combined format.

A public instance of EDAMmap-Server is accessible at https://biit.cs.ut.ee/edammap, with the :ref:`API <api>` at https://biit.cs.ut.ee/edammap/api.


.. _util:

************
EDAMmap-Util
************

EDAMmap includes a utility program to manage and fill database files with fetched content or otherwise setup prerequisites for other tools, etc. Many of its operations have already been used above, but this section is still included for completeness.

All command-line arguments suppliable to the utility program can be seen with:

.. code-block:: bash

  $ java -jar edammap-util-<version>.jar -h

The list of options is very long, as EDAMmap-Util extends the `CLI of PubFetcher <https://pubfetcher.readthedocs.io/en/latest/cli.html>`_, which means that the utility program can run all the same operations as PubFetcher-CLI can. In addition to functionality inherited from PubFetcher-CLI, operations described in the following table can be executed.

============================  =============================================  ============  ===========
Parameter                     Parameter args                                 Default       Description
============================  =============================================  ============  ===========
``-pub-query``                *<file path/URL> <file path/URL> ...*                        Load all `publication IDs <https://pubfetcher.readthedocs.io/en/latest/output.html#ids-of-publications>`_ found in the specified files of QueryType_ specified with ``--query-type``. A file can either be local or a URL, in which case `-\\-timeout <https://pubfetcher.readthedocs.io/en/latest/cli.html#timeout>`_ and `-\\-userAgent <https://pubfetcher.readthedocs.io/en/latest/cli.html#useragent>`_ can be used to change parameters used to fetch it.
``-web-query``                *<file path/URL> <file path/URL> ...*                        Load all `webpage URLs <https://pubfetcher.readthedocs.io/en/latest/output.html#urls-of-webpages>`_ found in the specified files of QueryType_ specified with ``--query-type``. A file can either be local or a URL, in which case `-\\-timeout`_ and `-\\-userAgent`_ can be used to change parameters used to fetch it.
``-doc-query``                *<file path/URL> <file path/URL> ...*                        Load all `doc URLs <https://pubfetcher.readthedocs.io/en/latest/output.html#urls-of-docs>`_ found in the specified files of QueryType_ specified with ``--query-type``. A file can either be local or a URL, in which case `-\\-timeout`_ and `-\\-userAgent`_ can be used to change parameters used to fetch it.
``-all-query``                *<file path/URL> <file path/URL> ...*                        Load all `publication IDs`_, `webpage URLs`_ and `doc URLs`_ found in the specified files of QueryType_ specified with ``--query-type``. A file can either be local or a URL, in which case `-\\-timeout`_ and `-\\-userAgent`_ can be used to change parameters used to fetch it.
``--query-type``              <QueryType_>                                   ``generic``   Specifies the type of the query files loaded using ``-pub-query``, ``-web-query``, ``-doc-query`` and ``-all-query``. Possible values: ``generic``, ``SEQwiki``, ``msutils``, ``Bioconductor``, ``biotools14``, ``biotools``, ``server``.
``-make-idf``                 *<query path/URL> <database path> <IDF path>*                Make the specified IDF file from tokens parsed from queries of type ``--make-idf-type`` loaded from the specified query file. The tokens are not stemmed. Contents for publication IDs, webpage URLs and doc URLs found in queries are loaded from the specified database file. If ``--make-idf-webpages-docs`` is ``true`` (the default), then tokens from webpage and doc content will also be used to make the IDF file and if ``--make-idf-fulltext`` is ``true`` (the default), then tokens from publication fulltext will also be used to make the IDF file. If the specified query file is a URL, then ``--timeout`` and ``--userAgent`` can be used to change parameters used to fetch it. The fetching parameters ``--titleMinLength``, ``--keywordsMinSize``, ``--minedTermsMinSize``, ``--abstractMinLength``, ``--fulltextMinLength`` and ``--webpageMinLength`` can be used to change the minimum length of a usable corresponding part (parts below that length will not be tokenised, thus will not used to make the specified IDF file).
``-make-idf-nodb``            *<query path/URL> <IDF path>*                                Make the specified IDF file from tokens parsed from queries of type ``--make-idf-type`` loaded from the specified query file. The tokens are not stemmed. Contents for publication IDs, webpage URLs and doc URLs found in queries are are not loaded and thus are not used to make the specified IDF file. If the specified query file is a URL, then ``--timeout`` and ``--userAgent`` can be used to change parameters used to fetch it.
``-make-idf-stemmed``         *<query path/URL> <database path> <IDF path>*                Make the specified IDF file from tokens parsed from queries of type ``--make-idf-type`` loaded from the specified query file. The tokens are stemmed. Contents for publication IDs, webpage URLs and doc URLs found in queries are loaded from the specified database file. If ``--make-idf-webpages-docs`` is true (the default), then tokens from webpage and doc content will also be used to make the IDF file and if ``--make-idf-fulltext`` is ``true`` (the default), then tokens from publication fulltext will also be used to make the IDF file. If the specified query file is a URL, then ``--timeout`` and ``--userAgent`` can be used to change parameters used to fetch it. The fetching parameters ``--titleMinLength``, ``--keywordsMinSize``, ``--minedTermsMinSize``, ``--abstractMinLength``, ``--fulltextMinLength`` and ``--webpageMinLength`` can be used to change the minimum length of a usable corresponding part (parts below that length will not be tokenised, thus will not used to make the specified IDF file).
``-make-idf-stemmed-nodb``    *<query path/URL> <IDF path>*                                Make the specified IDF file from tokens parsed from queries of type ``--make-idf-type`` loaded from the specified query file. The tokens are stemmed. Contents for publication IDs, webpage URLs and doc URLs found in queries are are not loaded and thus are not used to make the specified IDF file. If the specified query file is a URL, then ``--timeout`` and ``--userAgent`` can be used to change parameters used to fetch it.
``--make-idf-type``           <QueryType_>                                   ``biotools``  The QueryType_ of the query file loaded to make the IDF file with ``-make-idf``, ``-make-idf-nodb``, ``-make-idf-stemmed`` or ``-make-idf-stemmed-nodb``. Possible values: ``generic``, ``SEQwiki``, ``msutils``, ``Bioconductor``, ``biotools14``, ``biotools``, ``server``.
``--make-idf-webpages-docs``  *<boolean>*                                    ``true``      Whether tokens from webpage and doc content will also be used to make the IDF file with ``-make-idf`` or ``-make-idf-stemmed``
``--make-idf-fulltext``       *<boolean>*                                    ``true``      Whether tokens from publication fulltext will also be used to make the IDF file with ``-make-idf`` or ``-make-idf-stemmed``
``-print-idf-top``            *<IDF path> <positive integer n>*                            Print top *n* most frequent terms from the specified IDF file along with their counts (that show in how many documents a term occurs)
``-print-idf``                *<IDF path> <term> <term> ...*                               Print given terms along with their IDF scores (between 0 and 1) read from the given IDF file. Given terms are preprocessed, but stemming is not done, thus terms in the given IDF file must not be stemmed either.
``-print-idf-stemmed``        *<IDF path> <term> <term> ...*                               Print given terms along with their IDF scores (between 0 and 1) read from the given IDF file. Given terms are preprocessed, with stemming being done, thus terms in the given IDF file must also be stemmed.
``-biotools-full``            *<file path>*                                                Fetch all content (by following ``"next"`` until the last page) from https://bio.tools/api/tool to the specified JSON file. Fetching parameters `-\\-timeout`_ and `-\\-userAgent`_ can be used.
``-biotools-dev-full``        *<file path>*                                                Fetch all content (by following ``"next"`` until the last page) from https://dev.bio.tools/api/tool to the specified JSON file. Fetching parameters `-\\-timeout`_ and `-\\-userAgent`_ can be used.
``-make-server-files``        *<directory path>*                                           Create new directory with CSS, JavaScript and font files required by `EDAMmap-Server`_. The version of EDAMmap-Server the files are created for must match the version of EDAMmap-Util running the command.
``-make-options-conf``        *<file path>*                                                Create new options configuration file
============================  =============================================  ============  ===========

.. note::
  ``-pub-query``, ``-web-query``, ``-doc-query``, ``-all-query`` and ``--query-type`` are not standalone operations, but are meant to be used as part of the `Pipeline of operations <https://pubfetcher.readthedocs.io/en/latest/cli.html#pipeline-of-operations>`_ inherited from PubFetcher, allowing to inject IDs read from formats not supported by PubFetcher itself.
