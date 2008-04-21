# 150-to-160-MySQL5-post.sql

# update server version
update server_properties SET propertyvalue='160' WHERE propertyname='cosmo.schemaVersion';
