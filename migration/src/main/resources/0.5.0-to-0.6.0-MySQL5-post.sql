# 0.5.0-to-0.6.0-MySQL5-post.sql
# remove old data
alter table attribute drop column attributename;

# add new constraints
alter table attribute add unique itemid (itemid, namespace, localname);
alter table cal_property_index add index FKBA988E7927801F2 (eventstampid), add constraint FKBA988E7927801F2 foreign key (eventstampid) references event_stamp (stampid);
alter table cal_timerange_index add index FK98D277F227801F2 (eventstampid), add constraint FK98D277F227801F2 foreign key (eventstampid) references event_stamp (stampid);

# update server version
update server_properties SET propertyvalue='0.6-SNAPSHOT' WHERE propertyname='cosmo.schemaVersion';
