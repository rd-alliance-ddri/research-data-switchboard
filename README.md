Requirements
===

The software has been created and tests on Ubuntu 14.04.1 LTS 64-bit Linux OS. The software require this packages to be installed: 

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

	jars - backup directory for libraries, used in the project. Currently contains only opencsv-2.3.jar Library.
	v1 - V1 Package, to generate grants collapsible demo 
	v2 - V2 Package, generate RDA, Dryad and CERN database
	
V1 Package
===

V1 Package is designed to import grants and researches data into Neo4J and generate collapsible Grants demo.
 
Package Structure:
---
	v1/data - data, required for V2 Package
	v1/data/arc - ARC grants data
	v1/data/dryad - Data, imported from Dryad and Google
	v1/data/nhmrc - NHMRC grants data
	v1/src - V1 Package sources
	v1/src/Connectors - Software to connect nodes from different data sources
	v1/src/Export - Software to export final graphs into JSON files
	v1/src/Libraries - Additional libraries sources
	v1/src/Loaders - Software to load data into Neo4J database
	v1/src/Web - Collapseble Grants Demo web site skeleton.

Srarting fresh Neo4J instance
---

The package will require a fresh Neo4j instance. The connection to instance will be performed via Neo4J webservice located at localhost:7474 port. To start a fresh Neo4J instance, please locate a Neo4J sources and copy it into folder v1/neo4j. If you have Neo4J archive in the same folder, please execute:

	$ tar -xvf neo4j-community-2.1.5-unix.tar.gz
	$ mkdir research-data-switchboard/v1/neo4j
	$ cp -r neo4j-community-2.1.5/* research-data-switchboard/v1/neo4j/
	$ cd research-data-switchboard/v1/neo4j/
	$ ./bin/neo4j start
	$ cd ..

After that the Neo4J instance should be avaliable and you should be able to access it by the browser via http://localhost:7474/browser/

Installing the libraryes 
---

To install REST and ResearchData libraries, please execute (from v1 folder)

	$ cd src/Libraries/rest
	$ mvn install
	$ cd ../researchdata
	$ mvn install
	$ cd ../../../

Compiling and Running loaders
---

There is three Loader projects, and they all need to be compiled and runned once from project folder. To do that, please execute (from a v1 folder):

	$ mkdir jars
	$ cd src/Loaders/arc_loader
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

To compile and run this appycation, please execute (from v1 folder):

	$ cd src/Connectors/web_researcher_connector
	$ mvn package 
	$ cp target/jars/* ../../../jars/
	$ cp target/web_researcher_connector-1.0.0.jar ../../../
	$ cd ../../../

	$ java -jar web_researcher_connector-1.0.0.jar

Compiling and running exporters and create Demo sites:

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
	$ java -jar grant_export-1.0.0.jar
	$ java -jar institution_export-1.0.0.jar
	$ java -jar researcher_export-1.0.0.jar

After that, you will found four new folders in the v1 folder: datasets, grats, institutions and researchers. Each one will contains json folder with data for this demo. To deploy it on the web server, you will need to copy the sceleton of the site to it. 

	$ cp -r src/web/demo/WebContent/* datasets/
	$ cp -r src/web/demo/WebContent/* grants/
	$ cp -r src/web/demo/WebContent/* institutions/
	$ cp -r src/web/demo/WebContent/* researchers/

The demos could be viewed locally or copied into a web server.

Stoping Neo4J instance
---

To stop running Neo4J instance, please execute  (from v1 folder):
	
	$ cd neo4j
	$ ./bin/neo4j stop	

V2 Package
===

V2 Package is designed to create Neo4J Grant database. This package will require two Neo4J instances. The first instance will be used to load row data and the second one will be used to create a clean graph and add connections between the data sources 
 
Package Structure:
---
	v2/conf - Configuration files
	v2/conf/neo4j1 - First Neo4J instance configuration files
	v2/conf/neo4j2 - Second Neo4J instance configuration files
	v2/data - data, required for V2 
	v2/src - V2 Package sources
	v2/src/Compilers - Software to copy data from the first Neo4J instance to second one, adding required connections.
	v2/src/Harvesters - Software to harvest data from OAI:PMH and JSON webservices
	v2/src/Importers - Software to import harvested data into Neo4J
	v2/src/Libraries - Additional libraries
	v2/src/Libraries/CrossRef - CorssRef Library
	v2/src/Libraries/Google - Google Library
	v2/src/Libraries/Orcid - Orcid Library

Srarting fresh Neo4J instances
---

The package will require two fresh Neo4j instances. The connection to the instances will be performed via Neo4J webservices located at localhost:7474 and localhost:7476 ports. To start a fresh Neo4J instance, please locate a Neo4J sources and copy it into folder v1/neo4j1 and v1/neo4j2. After that, copy the configuration files provided. If you have Neo4J archive in the same folder, please execute:

	$ tar -xvf neo4j-community-2.1.5-unix.tar.gz
	$ mkdir research-data-switchboard/v2/neo4j1
	$ mkdir research-data-switchboard/v2/neo4j2
	$ cp -r neo4j-community-2.1.5/* research-data-switchboard/v2/neo4j1/
	$ cp -r neo4j-community-2.1.5/* research-data-switchboard/v2/neo4j2/
	$ cd research-data-switchboard/v2
	$ cp -r conf/neo4j1/conf/* neo4j1/conf/
	$ cp -r conf/neo4j2/conf/* neo4j2/conf/
	$ cd neo4j1
	$ ./bin/neo4j start
	$ cd ../neo4j2
	$ ./bin/neo4j start
	$ cd ..


After that the Neo4J instances should be avaliable and you should be able to access them by the browser via http://localhost:7474/browser/ and http://localhost:7476/browser/

Installing the libraryes 
---

To install required libraries, please execute (from v2 folder):

	$ cd cd src/Libraries/CrossRef/
	$ mvn install
	$ cd ../google_query/
	$ mvn install
	$ cd ../orcid/
	$ mvn install
	$ cd ../harvester_pmh
	$ mvn install
	$ cd ../import_mets
	$ mvn install
	$ cd ../import_rif
	$ mvn install
	$ cd ../../../

Compiling and running harvesters
---

So far there is four harvesters provided:

	harvester_cern - will harvest data from CERN in OAI:PMH Dublin Core and MARCXML formats.
	harvester_dryad - will harvest data from DRYAD in OAI:PMH Mets format
	harvester_rda - will harvest data from RDA in OAI:PMH Rif format
	harvester_rda2 - will harvest data from RDA by usinh JSON webservice.

All harvesters can work simultainesly, and they will store data on hard disk in XML or JSON files. If any error orccurs, each OAI:PMH harvester will leave an status file on the disk. This file will be used to pick up harvesting from the point there it was terminating. After harvesting will be done, the status file will automatically be deleted. To compile and run all harvesters, please execute (from v2 folder): 

	$ cd src/Harvesters/harvester_cern/
	$ mvn package
	$ cp target/jars/* ../../../jars/
	$ cp target/harvester_cern-1.0.4.jar ../../../
	
	$ cd ../harvester_dryad/
	$ mvn package
	$ cp target/jars/* ../../../jars/
	$ cp target/harvester_dryad-1.0.1.jar ../../../
	
	$ cd ../harvester_rda/
	$ mvn package
	$ cp target/jars/* ../../../jars/
	$ cp target/harvester_rda-1.0.1.jar ../../../

	$ cd ../harvester_rda2/
	$ mvn package
	$ cp target/jars/* ../../../jars/
	$ cp target/harvester_rda2-1.0.0.jar ../../../

	$ cd ../../../
	$ nohup java -jar harvester_cern-1.0.4.jar >/dev/null 2>&1 &
	$ nohup java -jar harvester_dryad-1.0.1.jar >/dev/null 2>&1 &
	$ nohup java -jar harvester_rda-1.0.1.jar >/dev/null 2>&1 &
	$ nohup java -jar harvester_rda2-1.0.0.jar >/dev/null 2>&1 &

To check harvesting processes, please execute 
	
	$ ps aux | grep harvester

Each harvester will create a folder in the working directory, where data and status file will be stored. After harvesting process will be finished, you will be able to run importing process

Compiling and run importers
---

Importers can be run in any order. The RDA and Dryad data will be imported into first Neo4J instance (localhost:7474) while CERN data will be imported straigth into second instance (localhost:7476).

To compile and run all importers, please execute 
	
	$ cd src/Importers/import_cern/
	$ mvn package
	$ cp target/jars/* ../../../jars/
	$ cp target/import_cern-1.0.0.jar ../../../
	
	$ cd ../import_dryad/
	$ mvn package
	$ cp target/jars/* ../../../jars/
	$ cp target/import_dryad-1.0.2.jar ../../../
	
	$ cd ../import_institutions/
	$ mvn package
	$ cp target/jars/* ../../../jars/
	$ cp target/import_institutions-1.0.0.jar ../../../

	$ cd ../import_rda/
	$ mvn package
	$ cp target/jars/* ../../../jars/
	$ cp target/import_rda-1.0.3.jar ../../../

	$ cd ../import_rda2/
	$ mvn package
	$ cp target/jars/* ../../../jars/
	$ cp target/import_rda2-1.0.1.jar ../../../

	$ cd ../../../
	$ nohup java -jar import_cern-1.0.0.jar >/dev/null 2>&1 &
	$ nohup java -jar import_dryad-1.0.2.jar >/dev/null 2>&1 &
	$ nohup java -jar import_institutions-1.0.0.jar >/dev/null 2>&1 &
	$ nohup java -jar import_rda-1.0.3.jar >/dev/null 2>&1 &
	$ nohup java -jar import_rda2-1.0.1.jar >/dev/null 2>&1 &
	

To check importing processes, please execute:
	
	$ ps aux | grep import


Copy data from first instance to the second instance
---

After all data has been imported, tha data, existing in the frist Neo4J instance, need to be compiled and copied into the second Neo4J instance. To do tha, please execute (from v2 folder)

	$ cd src/Comilers/compiler/
	$ mvn package
	$ cp target/jars/* ../../../jars/
	$ cp target/compiler-1.0.0.jar ../../../

	$ java -jar compiler-1.0.0.jar

To stop running Neo4J instances, please execute  (from v1 folder):
	
	$ cd neo4j1
	$ ./bin/neo4j stop
	$ ../neo4j2
	$ ./bin/neo4j stop
	$ cd ..	

Documentation
===

research-data-switchboard project documentation:
http://rd-alliance-ddri.github.io/research-data-switchboard/


