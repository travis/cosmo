# 130-to-140-MySQL5-post.sql

# update server version
update server_properties SET propertyvalue='140' WHERE propertyname='cosmo.schemaVersion';
