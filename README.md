# EDAMmap

Tool for mapping text to [EDAM](http://edamontology.org/page). It is designed to assist not replace a curator.

## INSTALL

[Apache Maven](https://maven.apache.org/) is required.

Also, [PubFetcher](https://github.com/edamontology/pubfetcher/) must be installed in local Maven repository.

```shell
$ git clone https://github.com/edamontology/edammap.git
$ cd edammap/
$ mvn clean install
$ java -jar target/edammap-cli-0.2-SNAPSHOT.jar -h
$ java -jar target/edammap-util-0.2-SNAPSHOT.jar -h
$ java -jar target/pubmedapps-0.2-SNAPSHOT.jar -h
```
