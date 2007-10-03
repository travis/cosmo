Chandler Server Migration Manager README

Overview
---------
This directory contains the source for the Chandler Server
Migration Manager.  The Migration Manager allows a previous
version of the Chandler Server database to be migragted
to the most recent version.  It supports Derby and 
MySQL5 databases.

Instructions for Building
--------------------------
If you checked out the server source, you must build the
migration manager before running. To build:

1. run mvn package
2. copy target/cosmo-migration-[version]-jar-with-dependencies.jar
   to the top level migration directory
3. copy src/main/config/migration.properties to the top level
   migration directory

Instructions for Running
---------------------------
If you downloaded the all-in-one server bundle, then the 
migration manager is already built. To run:
 
1. Shutdown Chandler Server (ver 0.5 or later)
2. Backup your database.
   For the default embedded Derby database:

   Copy the $OSAFSRV_HOME/db directory to db_backup or some other name.  
   In the event of a failed migration, you can then revert to the previous 
   version by removing the $OSAFSRV_HOME/db directory and copying the 
   db_backup directory to $OSAFSRV_HOME/db.

   For MySQL you can use something like:
       mysqldump -u [username] -p [password] [databasename] > backupfile.sql
   To restore you can use something like:
       mysql -u [root] -p [password] < backupfile.sql [databasename]
3. Edit migration.properties to match your database.
4. run 
   java -jar cosmo-migration-[ver]-jar-with-dependencies.jar migration.properties
   or for verbose output
   run 
   java -jar cosmo-migration-[ver]-jar-with-dependencies.jar -v migration.properties
5. If no errors occur, you can configure the new version of Chandler Server
   to run against the migrated database and start up normally.  If using Derby, copy
   $OSAFSRV_HOME/db from the old version of Cosmo to $OSAFSRV_HOME/db of the
   new version.
