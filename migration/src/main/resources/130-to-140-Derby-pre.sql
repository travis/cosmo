# 130-to-140-Derby-pre.sql

# migrate data
alter table item alter column displayname set data type varchar(1024)
alter table item add column hasmodifications smallint

# get list of all items with modifications
create table tmp_item (id bigint)
insert into tmp_item (select distinct modifiesitemid from item)

# update hasmodifications for all items
update item set hasmodifications=0
update item set hasmodifications=1 where id in (select id from tmp_item)
drop table tmp_item

# fix bad VALARM TRIGGERS allowed in previous versions
# have to cheat here since derby doesn't support clobs in functions
CREATE FUNCTION REPLACE(STR VARCHAR(20000), OLD VARCHAR(1024), NEW VARCHAR(1024)) RETURNS VARCHAR(20000) PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA EXTERNAL NAME 'org.osaf.cosmo.util.StringUtils.replace'
update event_stamp set icaldata=replace(CAST(icaldata as VARCHAR(20000)),'TRIGGER;VALUE=DATE:','TRIGGER:') where icaldata like '%TRIGGER;VALUE=DATE:%'