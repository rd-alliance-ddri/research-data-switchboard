Requirements
===

Java
---

All software are created with Java programming lanugage and requires Java SDK 1.7 to be installed on the system. To check your Java version, please execute:

	$ java -version

from a command line. If you Java version is too low or now java could be found, please download and install the latest Java package from 

http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html

Maven
---

The software also require Maven 3.x to be installed on the system. To check your Maven version, please execute:

	$ mvn -version

from a command line. If your Maven version is too low or now maven could be found, please download the latest  and install Maven package from

http://maven.apache.org/download.cgi

Neo4J
---

This software require Neo4J instance to be installed on the system. You can download it from

http://neo4j.com/download/

You can found more information about Neo4J and how to install it in the Neo4J User Manual, located at: 

http://neo4j.com/docs/2.1.5/

opencsv-2.3
---

You can download this library from:

http://sourceforge.net/projects/opencsv/files/opencsv/2.3/opencsv-2.3-src-with-libs.tar.gz/download

Please install locally as a Maven dependancy:

	$ tar -xcf opencsv-2.3-src-with-libs.tar.gz
	$ cd opencsv-2.3/deploy
	$ mvn install:install-file -Dfile=opencsv-2.3.jar -DgroupId=opencsv \
    		-DartifactId=opencsv -Dversion=2.3 -Dpackaging=jar

neo4j-rest-graphdb-2.0.2
---

You can download this library from github. Please install it locally as a Maven dependancy:

	$ git clone -b 2.0-labels-and-indexes https://github.com/neo4j-contrib/java-rest-binding.git
	$ cd java-rest-binding
	$ mvn install

Download and Install
===

To download latest sources, you can use git clone:

	$ git clone https://github.com/rd-alliance-ddri/research-data-switchboard.git

The project structure are:

jar/opencsv - an opencsv-2.3.jar Library
v1 - V1 Package - generate grants collapsible demo
v1/data - data, required for V1 Package
v1/src - V1 Package sources
v2 - V2 Package - generate RDA, Dryad and CERN database
v2/data - data, required for V2 Package
v2/src - V2 Package sources

V1 Package
===

V1 Package is designed to generate collapsible Grants demo. In order to use it, you will need to start a fresh Neo4j instance.

Please locate Neo4J sources and copy it into project folder under v1/neo4j or in any other folder you want. To start an neo4j instance, please execute:

	$ cd v1/neo4j
	$ ./bin/neo4j start
	$ cd ..

form an Neo4j instance folder.

The instance should be avaliable as http://localhost:7474/browser/

Installing the libraryes 
---

To install REST and ResearchData libraries:

$ cd v1
$ mkdir jars
$ cd v1/src
$ cd Libraries/rest
$ mvn install
$ cd ../researchdata
$ mvn install
$ cd ../..

Compiling and Running loaders
---

There is three Loader projects, and they all need to be compiled and runned once from project folder. To do that, please execute:

$ cd v1
$ mkdir jars
$ cd Loaders/arc_loader
$ mvn package 
$ cp target/jars/* ../../../jars/
$ cp target/arc_loader-1.0.0.jar ../../../
$ cd ..

$ cd dryad_loader
$ mvn package 
$ cp target/jars/* ../../../jars/
$ cp target/dryad_loader-1.0.0.jar ../../../
$ cd ..

$ cd nhmrc_loader
$ mvn package 
$ cp target/jars/* ../../../jars/
$ cp target/nhmrc_loader-1.0.0.jar ../../../
$ cd ../../../

To run the programs, please execute (fom v1 folder)

$ java -jar arc_loader-1.0.0.jar
$ java -jar dryad_loader-1.0.0.jar
$ java -jar nhmrc_loader-1.0.0.jar

Documentation
===

research-data-switchboard project documentation:
http://rd-alliance-ddri.github.io/research-data-switchboard/


