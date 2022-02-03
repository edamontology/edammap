FROM openjdk:8u265-jre-buster
COPY target /opt/edammap
WORKDIR /var/lib/edammap
EXPOSE 8080
CMD java -jar /opt/edammap/edammap-server-1.1.1-SNAPSHOT.jar -b http://0.0.0.0:8080 -p edammap --httpsProxy -e EDAM_1.25.owl -f files --fetching true --db server.db --idf biotools.idf --idfStemmed biotools.stemmed.idf --log /var/log/edammap
