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

	jars/opencsv - an opencsv-2.3.jar Library
	v1 - V1 Package - generate grants collapsible demo
	v1/data - data, required for V1 Package
	v1/src - V1 Package sources
	v2 - V2 Package - generate RDA, Dryad and CERN database
	v2/data - data, required for V2 Package
	v2/src - V2 Package sources

V1 Package
===

V1 Package is designed to generate collapsible Grants demo. In order to use it, you will need to start a fresh Neo4j instance.

Please locate Neo4J sources and copy it into folder v1/neo4j under procect root folder or in any other folder you want. To start an neo4j instance, please execute:
	
	$ cd v1/neo4j
	$ ./bin/neo4j start
	$ cd ..

form an Neo4j instance folder.

The instance should be avaliable as http://localhost:7474/browser/

Installing the libraryes 
---

To install REST and ResearchData libraries, please execute (from a v1 folder)

	$ mkdir jars
	$ cd v1/src
	$ cd Libraries/rest
	$ mvn install
	$ cd ../researchdata
	$ mvn install
	$ cd ../..

Compiling and Running loaders
---

There is three Loader projects, and they all need to be compiled and runned once from project folder. To do that, please execute (from a v1 folder):

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

To run the programs, please execute (fom a v1 folder):

	$ java -jar arc_loader-1.0.0.jar
	$ java -jar dryad_loader-1.0.0.jar
	$ java -jar nhmrc_loader-1.0.0.jar

Compiling and running connector
---

After data has been loaded, there are missing connections between Dryad, ARC and NHMRC data. web_researcher_connector application is used to crerate them.

To compile and run this appycation, please execute (from a v1 folder):

	$ cd src/Connectors/web_researcher_connector
	$ mvn package 
	$ cp target/jars/* ../../../jars/
	$ cp target/web_researcher_connector-1.0.0.jar ../../../
	$ cd ../../../

	$ java -jar web_researcher_connector-1.0.0.jar

Compiling and running exporters:

To create grants collapsible demo, the data need to be exported as JSON files. The package contains four exporters, each one will generate different type of JSON file.

Te compile and run all exporters, please execute (from a v1 folder):

	$ cd src/Export/dataset_export
	$ mvn package
	$ cp target/jars/* ../../../jars/
	$ cp target/dataset_export-1.0.0.jar ../../../

	$ cd ../grant_export
	$ mvn package
	$ cp target/jars/* ../../../jars/
	$ cp target/grant_export-1.0.0.jar ../../../

	$ cd ../institution_export
	$ mvn package
	$ cp target/jars/* ../../../jars/
	$ cp target/institution_export-1.0.0.jar ../../../

	$ cd ../researcher_export
	$ mvn package
	$ cp target/jars/* ../../../jars/
	$ cp target/researcher_export-1.0.0.jar ../../../
	$ cd ../../../

	$ java -jar dataset_export-1.0.0.jar
	$ java -jar dataset_export-1.0.0.jar
	$ java -jar dataset_export-1.0.0.jar
	$ java -jar dataset_export-1.0.0.jar

Documentation
===

research-data-switchboard project documentation:
http://rd-alliance-ddri.github.io/research-data-switchboard/


