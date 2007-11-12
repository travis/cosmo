# 130-to-140-MySQL5-pre.sql

# migrate data
alter table item modify column displayname varchar(1024);
alter table item add column hasmodifications bit;

update item set hasmodifications=0;
update item set hasmodifications=1 where id in (select distinct modifiesitemid from item);

# fix bad VALARM TRIGGERS allowed in previous versions
update event_stamp set icaldata=replace(icaldata,'TRIGGER;VALUE=DATE:','TRIGGER:') where icaldata like '%TRIGGER;VALUE=DATE:%';