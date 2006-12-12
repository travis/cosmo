Cosmo Migration Manager README

Overview
---------
This directory contains the source for the Cosmo
Migration Manager.  The Migration Manager allows
you to migrate a previous version of the Cosmo database
to the most recent version.  It supports Derby and 
MySQL5 databases.

Instructions for Building and Running
-------------------------------------
1. Shutdown Cosmo 0.5 server
2. Backup your database!!
3. run mvn package
4. copy src/main/config/migration.properties .
5. edit migration.properties to fit your database
6. run java -jar target/cosmo-migration-0.6-SNAPSHOT-jar-with-dependencies.jar migration.properties
   - or for verbose output -
   run java -jar target/cosmo-migration-0.6-SNAPSHOT-jar-with-dependencies.jar -v migration.properties
7. If no errors occur, you can configure Cosmo 0.6 to run against
   the migrated 0.5 database and start up normally.
