Cosmo Migration Manager README

Instructions for Running
------------------------
1. Shutdown Cosmo 0.5 server
2. Backup your database!!
3. edit sample migration.properties to fit your database
4. run java -jar cosmo-migration-[version]-jar-with-dependencies.jar migration.properties
   - or for verbose output -
   run java -jar target/cosmo-migration-[version]-jar-with-dependencies.jar -v migration.properties
5. If no errors occur, you can configure Cosmo 0.6 to run against
   the migrated 0.5 database and start up normally.
