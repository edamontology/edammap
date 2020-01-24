# INSTALL

## Compiling from latest source

[git](https://git-scm.com/), [JDK 8](https://openjdk.java.net/projects/jdk8/) (or later) and [Apache Maven](https://maven.apache.org/) are required.

In addition, [installation instructions for PubFetcher](https://github.com/edamontology/pubfetcher/blob/master/INSTALL.md) have to be followed beforehand to ensure PubFetcher dependencies are installed in the local Maven repository.

Execute:

```shell
$ cd ~/foo/bar/
$ git clone https://github.com/edamontology/edammap.git
$ cd edammap/
$ git checkout develop
$ mvn clean install
```

EDAMmap can now be run with:

```shell
$ java -jar ~/foo/bar/edammap/target/edammap-cli-<version>.jar -h
$ java -jar ~/foo/bar/edammap/target/edammap-server-<version>.jar -h
$ java -jar ~/foo/bar/edammap/target/edammap-util-<version>.jar -h
```

A packaged version of EDAMmap can be found as `~/foo/bar/edammap/dist/target/edammap-<version>.zip`.

## Compiling latest release

Same as previous section, except `git checkout develop` must be replaced with `git checkout master`.

## Using a pre-compiled release

Pre-built releases can be found from https://github.com/edamontology/edammap/releases. A downloaded release package can be unzipped in the desired location, where `edammap-cli-<version>.jar`, `edammap-server-<version>.jar` and `edammap-util-<version>.jar` can again be run with `java -jar`.
