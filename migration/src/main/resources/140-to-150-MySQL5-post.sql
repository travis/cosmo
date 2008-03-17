# 140-to-150-MySQL5-post.sql

# set createdate to not null
ALTER TABLE collection_item MODIFY COLUMN createdate bigint NOT NULL;

# update server version
update server_properties SET propertyvalue='150' WHERE propertyname='cosmo.schemaVersion';