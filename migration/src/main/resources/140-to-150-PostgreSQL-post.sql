# 140-to-150-PostgresSQL-post.sql

# add not null constraint
ALTER TABLE collection_item ALTER COLUMN createdate SET NOT NULL;

# update server version
update server_properties SET propertyvalue='150' WHERE propertyname='cosmo.schemaVersion';
