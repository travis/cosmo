Cosmo MySQL Database Migration README

Overview
--------
To migrate a MySQL Cosmo database to a higher version, you
need to apply the correct sql migration scripts in
order.  For exampe, to migrate from a 0.5 database to
a 0.6.1 database, you must apply 0.5-to-0.6.sql and
then 0.6-to-0.6.1.sql in that order. Apply these scripts
will update the schema and migrate the data from one version
of Cosmo to a later version. Migration is a one-way action.
Migrating data to a previous version is not supported.

NOTE: Make sure your MySQL sever is setup to use the
InnoDB table engine as the default.  Otherwise you will
see errors when running this script.

Instructions
-------------
1. BACKUP YOUR CURRENT DATABASE!!
Things can go wrong, so please backup your current database
so that you do not lose data.

2. Apply sql scripts using mysql command line or the MySQL
Query Browser.  The command line client does not seem to like 
utf8 encoded files, so both an ansi and utf8 encoded version 
are included for each script.

example:  %mysql [dbname] < 0.5-to-0.6-ansi.sql -u [username] -p
          Enter password: ***********

3. If there are no errors, you should have a migrated
database after the script completes and you can start using 
that database with the new version of Cosmo.
