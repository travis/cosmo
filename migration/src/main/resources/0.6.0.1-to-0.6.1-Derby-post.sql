# 0.6.0.1-to-0.6.1-Derby-post.sql

# can't drop parentid column, so atleast drop the constraint
alter table item drop foreign key FK317B137014CFFB

# create updated index
create unique index itemmid on stamp (itemid, stamptype, isactive)

# update server version
update server_properties SET propertyvalue='${pom.version}' WHERE propertyname='cosmo.schemaVersion'
