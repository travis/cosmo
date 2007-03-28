# 0.6.0-to-0.6.0.1-MySQL5-post.sql
# update server version
update server_properties SET propertyvalue='0.6.0.1' WHERE propertyname='cosmo.schemaVersion';
