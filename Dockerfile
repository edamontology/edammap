FROM debian:bookworm AS build
RUN apt-get update && apt-get install -y default-jdk-headless maven git
WORKDIR /opt/edammap
COPY . .
RUN git clone https://github.com/edamontology/pubfetcher.git && cd pubfetcher/ && git checkout develop && mvn clean install
RUN mvn clean install

FROM debian:bookworm
RUN apt-get update && apt-get install -y default-jre firefox-esr
COPY --from=build /opt/edammap/target /opt/edammap
COPY doc/EDAM_1.25.owl doc/biotools.idf doc/biotools.stemmed.idf /opt/edammap/
WORKDIR /var/lib/edammap
EXPOSE 8080/tcp
CMD ["java", "-jar", "/opt/edammap/edammap-server-1.1.2-SNAPSHOT.jar", "-b", "http://0.0.0.0:8080", "--httpsProxy", "-e", "/opt/edammap/EDAM_1.25.owl", "-f", "files", "--db", "server.db", "--idf", "/opt/edammap/biotools.idf", "--idfStemmed", "/opt/edammap/biotools.stemmed.idf", "--log", "/var/log/edammap"]
