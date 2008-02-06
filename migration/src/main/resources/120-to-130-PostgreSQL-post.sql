# 120-to-130-MySQL5-post.sql

# update server version
update server_properties SET propertyvalue='130' WHERE propertyname='cosmo.schemaVersion';
