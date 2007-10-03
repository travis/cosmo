# 110-to-120-MySQL5-pre.sql

# alter existing tables
# subscription
alter table subscription add column etag varchar(255);

# users
alter table users add column etag varchar(255);

# user_preferences
alter table user_preferences add column etag varchar(255);

#item
alter table item add column etag varchar(255);

#stamp
alter table stamp add column etag varchar(255);

#attribute
alter table attribute add column etag varchar(255);

# migrate data
insert into attribute (itemid, attributetype, namespace, localname, stringvalue, createdate, modifydate) select s.itemid, 'string', 'org.osaf.cosmo.model.CalendarCollectionStamp', 'description', cs.description, s.createdate, s.modifydate from stamp s, calendar_stamp cs where s.id=cs.stampid and s.stamptype='calendar';
insert into attribute (itemid, attributetype, namespace, localname, stringvalue, createdate, modifydate) select s.itemid, 'string', 'org.osaf.cosmo.model.CalendarCollectionStamp', 'language', cs.language, s.createdate, s.modifydate from stamp s, calendar_stamp cs where s.id=cs.stampid and s.stamptype='calendar';
insert into attribute (itemid, attributetype, namespace, localname, textvalue, createdate, modifydate) select s.itemid, 'icalendar', 'org.osaf.cosmo.model.CalendarCollectionStamp', 'timezone', cs.timezone, s.createdate, s.modifydate from stamp s, calendar_stamp cs where s.id=cs.stampid and s.stamptype='calendar';

update item set icaluid=null where modifiesitemid is not null;
update attribute set etag='';
update stamp set etag='';