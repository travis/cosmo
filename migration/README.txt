Cosmo Migration Manager README

Overview
---------
This directory contains the source for the Cosmo
Migration Manager.  The Migration Manager allows
you to migrate a previous version of the Cosmo database
to the most recent version.  It supports Derby and 
MySQL5 databases.

Instructions for Running
------------------------
1. Shutdown Cosmo 0.5 server
2. Backup your database!!
3. edit src/main/resources/applicationContext-migrate.xml
   - Configure your datasource bean to point to your Cosmo 0.5
     database.  Derby and MySQL5 datasource bean examples have 
     been provided.
   - Configure the dialect (either "Derby" or "MySQL5") in the
     migrationManager bean.
4. run mvn integration-test
5. If no errors occur, you can configure Cosmo 0.6 to run against
   the migrated 0.5 database and start up normally.