# 150-to-160-PostgresSQL-post.sql

# update server version
update server_properties SET propertyvalue='160' WHERE propertyname='cosmo.schemaVersion';
