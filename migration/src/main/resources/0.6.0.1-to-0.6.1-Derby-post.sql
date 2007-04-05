# 0.6.0.1-to-0.6.1-Derby-post.sql

# can't drop parentid column, so atleast drop the constraint
alter table item drop foreign key FK317B137014CFFB

# can't drop the isactive column, so atleast drop the index
drop index idx_itemisactive

# update server version
update server_properties SET propertyvalue='${pom.version}' WHERE propertyname='cosmo.schemaVersion'
