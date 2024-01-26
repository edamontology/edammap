FROM debian:bookworm
RUN apt-get update && apt-get install -y default-jre firefox-esr
COPY target /opt/edammap
WORKDIR /var/lib/edammap
EXPOSE 8080
CMD java -jar /opt/edammap/edammap-server-1.1.2-SNAPSHOT.jar -b http://0.0.0.0:8080 --httpsProxy -e EDAM_1.25.owl -f files --db server.db --idf biotools.idf --idfStemmed biotools.stemmed.idf --log /var/log/edammap
