# 0.6.0.1-to-0.6.1-MySQL5-pre.sql

# create new tables
create table collection_item (itemid bigint not null, collectionid bigint not null, primary key (itemid, collectionid)) ENGINE=InnoDB;
alter table collection_item add index FK3F30F8145361D2A6 (itemid), add constraint FK3F30F8145361D2A6 foreign key (itemid) references item (id);
alter table collection_item add index FK3F30F8148B8DC8EF (collectionid), add constraint FK3F30F8148B8DC8EF foreign key (collectionid) references item (id);

create table tombstones (tombstonetype varchar(16) not null, id bigint not null auto_increment, removedate bigint not null, namespace varchar(255), localname varchar(255), itemuid varchar(255), stamptype varchar(255), itemid bigint not null, primary key (id)) ENGINE=InnoDB;
alter table tombstones add index FK40CA41FE5361D2A6 (itemid), add constraint FK40CA41FE5361D2A6 foreign key (itemid) references item (id);

create table pwrecovery (id bigint not null auto_increment, creationdate datetime, pwrecoverykey varchar(255) not null unique, timeout bigint, userid bigint, primary key (id)) ENGINE=InnoDB;
create index idx_pwrecoverykey on pwrecovery (pwrecoverykey);
alter table pwrecovery add index FK9C4F969C67D36616 (userid), add constraint FK9C4F969C67D36616 foreign key (userid) references users (id);

# alter existing tables
alter table item add column clientmodifieddate bigint;
alter table item add column lastmodification integer;
alter table item add column triagestatuscode integer;
alter table item add column triagestatusrank numeric(12,2);
alter table item add column isautotriage bit;
alter table item add column sent bit;
alter table item add column needsreply bit;
alter table item add column modifiesitemid bigint;
alter table item add index FK317B13AB2006A2 (modifiesitemid), add constraint FK317B13AB2006A2 foreign key (modifiesitemid) references item (id);

alter table attribute add column createdate bigint;
alter table attribute add column modifydate bigint;

alter table item drop column triagestatus;
alter table item drop column triagestatusupdated;

drop table message_stamp;

# migrate data
update item set isautotriage=1 where itemtype='note';
delete from event_stamp where exists (select id from stamp where isactive=0 and id=stampid);
delete from stamp where stamp.isactive=0;
delete from ticket_privilege where exists (select t.id from tickets t, item i where t.id=ticketid and i.id=t.itemid and i.isactive=0);
delete from tickets where exists (select i.id from item i where i.id=itemid and i.isactive=0);
delete from item where isactive=0;
insert into collection_item (itemid, collectionid) select id, parentid from item where parentid is not null;