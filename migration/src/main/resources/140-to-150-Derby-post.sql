# 140-to-150-Derby-post.sql

# set createdate to not null
alter table collection_item alter column createdate null

# update server version
update server_properties SET propertyvalue='150' WHERE propertyname='cosmo.schemaVersion'