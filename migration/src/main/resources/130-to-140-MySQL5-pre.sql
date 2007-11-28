# 130-to-140-MySQL5-pre.sql

# migrate data
alter table item modify column displayname varchar(1024);
alter table item add column hasmodifications bit;

# get list of all items with modifications
create table tmp_item (id bigint);
insert into tmp_item (select distinct modifiesitemid from item);

# update hasmodifications for all items
update item set hasmodifications=0;
update item set hasmodifications=1 where id in (select id from tmp_item);
drop table tmp_item;


# fix bad VALARM TRIGGERS allowed in previous versions
update event_stamp set icaldata=replace(icaldata,'TRIGGER;VALUE=DATE:','TRIGGER:') where icaldata like '%TRIGGER;VALUE=DATE:%';

# fix bad calendar-color attribute added by ical 3
update attribute set textvalue=concat(substring(textvalue,1,18),' xmlns:',substring(textvalue,2,2),'="http://apple.com/ns/ical/"',substring(textvalue,19)) where attributetype='xml' and textvalue like '%calendar-color%' and textvalue not like '%xmlns:%';