# 0.5.0-to-0.6.0-Derby-post.sql
# remove old data
#alter table attribute drop column attributename

# add new constraints
alter table attribute add constraint itemid unique (itemid, namespace, localname)
alter table cal_property_index add constraint FKBA988E7927801F2 foreign key (eventstampid) references stamp on delete cascade
alter table cal_timerange_index add constraint FK98D277F227801F2 foreign key (eventstampid) references stamp on delete cascade
# update server version
update server_properties SET propertyvalue='0.6-SNAPSHOT' WHERE propertyname='cosmo.schemaVersion'
