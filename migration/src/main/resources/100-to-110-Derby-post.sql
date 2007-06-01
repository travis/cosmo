# 100-to-110-Derby-post.sql
# remove old data
drop table cal_property_index
drop table cal_timerange_index

# update server version
update server_properties SET propertyvalue='110' WHERE propertyname='cosmo.schemaVersion'