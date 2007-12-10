# 100-to-110-Derby-post.sql
# remove old data
drop table cal_property_index
drop table cal_timerange_index

drop table x_user_preferences
alter table user_preferences add constraint FK199BD08467D36616 foreign key (userid) references users

# update server version
update server_properties SET propertyvalue='110' WHERE propertyname='cosmo.schemaVersion'