# EDAMmap

Tool for mapping text to [EDAM](http://edamontology.org/page). It is designed to assist not replace a curator.

## Compile

[Apache Maven](https://maven.apache.org/) is required.

Also, [PubFetcher](https://github.com/edamontology/pubfetcher/) must be installed in local Maven repository.

```shell
$ git clone https://github.com/edamontology/edammap.git
$ cd edammap/
$ mvn clean install
$ java -jar target/edammap-util-0.2-SNAPSHOT.jar -h
$ java -jar target/edammap-cli-0.2-SNAPSHOT.jar -h
$ java -jar target/edammap-server-0.2-SNAPSHOT.jar -h
$ java -jar target/pubmedapps-0.2-SNAPSHOT.jar -h
```

## Setup

Generate queries (here, content of [bio.tools](https://bio.tools/)) and empty database.

```shell
java -jar edammap-util-0.2-SNAPSHOT.jar --biotools-full biotools.json
java -jar edammap-util-0.2-SNAPSHOT.jar --init-db biotools.db
```

Pre-fetch all publications, webpages and docs (this can be skipped).

```shell
java -jar edammap-util-0.2-SNAPSHOT.jar -pub-query biotools.json -query-type biotools -db-fetch biotools.db --log pub.log
java -jar edammap-util-0.2-SNAPSHOT.jar -web-query biotools.json -query-type biotools -db-fetch biotools.db --log web.log
java -jar edammap-util-0.2-SNAPSHOT.jar -doc-query biotools.json -query-type biotools -db-fetch biotools.db --log doc.log
# or
java -jar edammap-util-0.2-SNAPSHOT.jar -all-query biotools.json -query-type biotools -db-fetch biotools.db --log all.log
```

Generate IDF files (if the last step was not skipped and IDF is turned on for mapping).

```shell
java -jar edammap-util-0.2-SNAPSHOT.jar --make-idf biotools.json biotools.db biotools.idf
java -jar edammap-util-0.2-SNAPSHOT.jar --make-idf-stemmed biotools.json biotools.db biotools.stemmed.idf
```

Download [EDAM Ontology](http://edamontology.org/page) in OWL format.

## Run

```shell
java -jar edammap-cli-0.2-SNAPSHOT.jar -e EDAM_1.20.owl -q biotools.json -t biotools -o results.txt -r results --threads 4 --fetching false --db biotools.db --idfStemmed biotools.stemmed.idf --log edammap.log
```

Text results will be in "results.txt" and HTML results in directory "results".

If biotools.db was not pre-fetched, then --fetching should be set to true.

By default, --stemming is true, so --idfStemmed with the stemmed IDF file must be used (if IDF weighting is desired). If --stemming is set to false, then --idf with the regular IDF file must be used.

Instead of adding arguments on the command line, the file [options.conf](core/options.conf) can be copied and used for managing program parameters.

```shell
java -jar edammap-cli-0.2-SNAPSHOT.jar @options.conf
```
