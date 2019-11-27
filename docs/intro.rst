
################
What is EDAMmap?
################

A tool for mapping various text input to `EDAM ontology <http://edamontology.org/page>`_ concepts. It is designed to assist not replace a curator.

Currently, it is mainly geared towards annotating `bio.tools <https://bio.tools/>`_ content, hence the structure of input parts: *tool name*, *keywords*, *description*, *publication IDs*, *link* and *documentation URLs*. The content of publications and web pages will be downloaded through the use of the `PubFetcher <https://github.com/edamontology/pubfetcher>`_ library. However, EDAMmap could also be used on arbitrary text inputs of very different lengths, with results influenceable by a multitude of changeable parameters.

EDAMmap can be run on the command line, but also as a web server. For the latter case, a public `web application <https://biit.cs.ut.ee/edammap/>`_ and :ref:`API <api>` are available.


**********
Background
**********

Longer (but somewhat outdated) background information can be found in the `thesis <https://github.com/edamontology/edammap/blob/master/doc/Automatic%20mapping%20of%20free%20texts%20to%20bioinformatics%20ontology%20terms.pdf>`_ where EDAMmap was initially developed (and the corresponding `talk <https://github.com/edamontology/edammap/blob/master/doc/Automatic%20mapping%20of%20free%20texts%20to%20bioinformatics%20ontology%20terms%20-%20Talk.pdf>`_).

bio.tools
=========

In the field of bioinformatics, there are numerous tools, databases and services for solving various biological problems. To provide a common portal for biologists that need to look for a certain tool, several projects gathering a vast amount of tools' metadata available under one web site have been launched. One such project is the ELIXIR tools and services registry `bio.tools`_.

EDAM
====

Collecting the descriptions of tools into one common place is not enough -- to be useful the entries need clear and accurate curation. In addition, to semantically connect meta-data, simplifying the organisation and merging of resources and providing better browse and search capabilities, the tools should be annotated in a standardised way, e.g. using an ontology. In the case of bio.tools, we use the `EDAM ontology`_, which is a simple ontology of well-established concepts that are prevalent in the field of bioinformatics organised into an intuitive hierarchy. EDAM is divided into 4 branches (topic, operation, data and format) and each term has a preferred label, synonyms, longer definition, etc.

EDAMmap
=======

So far, the process of annotating bio.tools entries with EDAM terms had been a manual affair, being both time-consuming and prone to mistakes due to unfamiliarity with the annotated tools or EDAM. The process can partly be automatised by EDAMmap: as input it will take tool description parts (for example name, description, publication IDs, link and documentation URLs from bio.tools), fetch content corresponding to publication IDs and web page URLs, tokenise all parts and find EDAM terms whose parts (label, synonyms, definition, etc) best match with the input parts. Parameters influencing various aspects of the process (like influence of different mapping algorithm parts to the final score or how many terms to suggest to the user) have been tuned for usage with bio.tools content and EDAM ontology, but these can be changed by the user at will. EDAMmap is flexible -- different input and ontology parts can be omitted and have very different lengths. But in the end, accurate annotation relies heavily on expert domain knowledge, so EDAMmap is intended to only enhance curation, not replace the curator.


*******
Outline
*******

In the :ref:`manual`, instructions are given on how to obtain or generate :ref:`setup` files required by EDAMmap, including the :ref:`idf` files. In the :ref:`input` section, the structure of the input submitted as the query is discussed, with the most common file types being :ref:`csv` or `biotoolsSchema <https://biotoolsschema.readthedocs.io/>`_ compatible JSON. The many changeable parameters are discussed in :ref:`parameters` and output results and result formats in :ref:`results_section`. EDAMmap consists of 3 tools: :ref:`cli` to run mapping of multiple queries in parallel on the command-line, :ref:`server` enabling mapping of one query in a web application or through an API, and :ref:`util` for running many utility operations.

The :ref:`EDAMmap API <api>` can consumed through the :ref:`/api <api_endpoint>` endpoint, either by sending requests to the public instance https://biit.cs.ut.ee/edammap/api or by sending requests to a local instance set up by following the instructions under :ref:`server`. :ref:`prefetching` can be used to pre-store the content of webpages, docs and publications for a quicker final mapping call. Possible error situations when using the API are described in :ref:`error_handling`.

Lastly, some :ref:`future` are discussed.


*******
Install
*******

Installation instructions can be found in the project's GitHub repo at `INSTALL <https://github.com/edamontology/edammap/blob/master/INSTALL.md>`_.


**********
Quickstart
**********

Use the public web application at https://biit.cs.ut.ee/edammap/ by filling in the "name" and some other fields, e.g. some "links" and "publications", and by clicking on "MAP".

For command-line usage, some simple examples can be found under :ref:`cli`.


****
Repo
****

EDAMmap is hosted at https://github.com/edamontology/edammap.


*******
Support
*******

Should you need help installing or using EDAMmap, please get in touch with Erik Jaaniso (the lead developer) directly via the `tracker <https://github.com/edamontology/edammap/issues>`_.


*******
License
*******

EDAMmap is free and open-source software licensed under the GNU General Public License v3.0, as seen in `COPYING <https://github.com/edamontology/edammap/blob/master/COPYING>`_.
