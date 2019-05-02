# INSTALL

[Apache Maven](https://maven.apache.org/) is required.

Also, [PubFetcher](https://github.com/edamontology/pubfetcher) must be [installed](https://github.com/edamontology/pubfetcher/blob/master/INSTALL.md) in local Maven repository.

```shell
$ git clone https://github.com/edamontology/edammap.git
$ cd edammap/
$ mvn clean install
$ java -jar target/edammap-cli-0.2-SNAPSHOT.jar -h
$ java -jar target/edammap-server-0.2-SNAPSHOT.jar -h
$ java -jar target/edammap-util-0.2-SNAPSHOT.jar -h
```
