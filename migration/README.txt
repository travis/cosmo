Chandler Server Migration Manager README

Overview
---------
This directory contains the source for the Chandler Server
Migration Manager.  The Migration Manager allows
you to migrate a previous version of the Chandler Server database
to the most recent version.  It supports Derby and 
MySQL5 databases.

Instructions for Building
--------------------------
1. run mvn package
2. copy target/cosmo-migration-[version]-jar-with-dependencies.jar
   to the top level migration directory
3. copy src/main/config/migration.properties to the top level
   migration directory

Instructions for Running
------------------------------
1. Shutdown Chandler Server (ver 0.5 or later)
2. Backup your database.  If using the default embedded
   Derby database, then this involves copying the %OSAFSRV_HOME%\db
   to db_backup or some other name.
   For MySQL you can use something like:
       mysqldump -u [username] -p [password] [databasename] > [backupfile.sql]
3. Edit migration.properties to match your database.
4. run 
   java -jar cosmo-migration-[ver]-jar-with-dependencies.jar migration.properties
   or for verbose output
   run 
   java -jar cosmo-migration-[ver]-jar-with-dependencies.jar -v migration.properties
5. If no errors occur, you can configure the new version of Chandler Server
   to run against the migrated database and start up normally.  If using Derby, copy
   %OSAFSRV_HOME%\db from the old version of Cosmo to %OSAFSRV_HOME%\db of the
   new version.
