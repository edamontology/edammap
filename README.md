# EDAMmap

A tool for mapping various text input to [EDAM ontology](http://edamontology.org/page) concepts. It is designed to assist not replace a curator.

Currently, it is mainly geared towards annotating [bio.tools](https://bio.tools/) content, hence the structure of input parts: _tool name_, _keywords_, _description_, _publication IDs_, _link_ and _documentation URLs_. The content of publications and web pages will be downloaded through the use of the [PubFetcher](https://github.com/edamontology/pubfetcher) library. However, EDAMmap could also be used on arbitrary text inputs of very different lenghts, with results influenceable by a multitude of changeable parameters.

## Use online

EDAMmap can be run on the command line, but also as a web server. For the latter case, a public [web application](https://biit.cs.ut.ee/edammap/) and [API](https://github.com/edamontology/edammap/wiki/api) are available.

## Documentation

Documentation for EDAMmap can be found in the [wiki](https://github.com/edamontology/edammap/wiki).

## Support

Should you need help installing or using EDAMmap, please get in touch with Erik Jaaniso (the lead developer) directly via the [tracker](https://github.com/edamontology/edammap/issues).
