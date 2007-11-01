# 130-to-140-Derby-pre.sql

# migrate data
alter table item alter column displayname set data type varchar(1024)
alter table item add column hasmodifications smallint

update item set hasmodifications=0
update item set hasmodifications=1 where id in (select distinct modifiesitemid from item)