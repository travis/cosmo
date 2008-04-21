# 150-to-160-PostgresSQL-pre.sql

# migrate data
alter table tickets add column createdate bigint
alter table tickets add column modifydate bigint
alter table tickets add column etag varchar(255)

create table event_log (id int8 not null, authid int8 not null, authtype varchar(64) not null, entrydate int8, id1 int8, id2 int8, id3 int8, id4 int8, strval1 varchar(255), strval2 varchar(255), strval3 varchar(255), strval4 varchar(255), eventtype varchar(64) not null, uid1 varchar(255), uid2 varchar(255), uid3 varchar(255), uid4 varchar(255), primary key (id))

# initialize new ticket fields
update tickets set createdate=1
update tickets set modifydate=1
update tickets set etag=''
