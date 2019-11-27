# INSTALL

[git](https://git-scm.com/), [JDK 8](https://openjdk.java.net/projects/jdk8/) (or later) and [Apache Maven](https://maven.apache.org/) are required.

In addition, [installation instructions for PubFetcher](https://github.com/edamontology/pubfetcher/blob/master/INSTALL.md) have to be followed beforehand to ensure PubFetcher dependencies are installed in the local Maven repository.

On the command-line, go to the directory EDAMmap should be installed in and execute:

```shell
$ git clone https://github.com/edamontology/edammap.git
$ cd edammap/
$ mvn clean install
```

EDAMmap can now be run with:

```shell
$ java -jar /path/to/edammap/target/edammap-cli-0.2-SNAPSHOT.jar -h
$ java -jar /path/to/edammap/target/edammap-server-0.2-SNAPSHOT.jar -h
$ java -jar /path/to/edammap/target/edammap-util-0.2-SNAPSHOT.jar -h
```
