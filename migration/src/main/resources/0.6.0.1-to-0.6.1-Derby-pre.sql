# 0.6.0.1-to-0.6.1-Derby-pre.sql
# create new tables
create table collection_item (itemid bigint not null, collectionid bigint not null, primary key (itemid, collectionid))
alter table collection_item add constraint FK3F30F8145361D2A6 foreign key (itemid) references item
alter table collection_item add constraint FK3F30F8148B8DC8EF foreign key (collectionid) references item

create table tombstones (tombstonetype varchar(16) not null, id bigint not null, removedate bigint not null, itemuid varchar(255), stamptype varchar(255), itemid bigint not null, primary key (id))
alter table tombstones add constraint FK40CA41FE5361D2A6 foreign key (itemid) references item

# alter existing tables
alter table item add column clientmodifieddate bigint
alter table item add column lastmodification integer
alter table item add column triagestatuscode integer
alter table item add column triagestatusrank triagestatusrank numeric(12,2)
alter table item add column isautotriage smallint
alter table item add column sent smallint
alter table item add column needsreply smallint
alter table item add column modifiesitemid bigint
alter table item add index FK317B13AB2006A2 (modifiesitemid), add constraint FK317B13AB2006A2 foreign key (modifiesitemid) references item (id)

# migrate data
insert into collection_item (itemid, collectionid) select id, parentid from item where parentid is not null
update item set isautotriage=1 where itemtype='note'
delete from event_stamp where exists (select id from stamp where isactive=0 and id=stampid)
delete from stamp where stamp.isactive=0
delete from item where isactive=0