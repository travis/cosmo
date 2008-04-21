# 150-to-160-MySQL5-pre.sql

# migrate data
alter table tickets add column createdate bigint
alter table tickets add column modifydate bigint
alter table tickets add column etag varchar(255)

create table event_log (id bigint not null auto_increment, authid bigint not null, authtype varchar(64) not null, entrydate bigint, id1 bigint, id2 bigint, id3 bigint, id4 bigint, strval1 varchar(255), strval2 varchar(255), strval3 varchar(255), strval4 varchar(255), eventtype varchar(64) not null, uid1 varchar(255), uid2 varchar(255), uid3 varchar(255), uid4 varchar(255), primary key (id)) ENGINE=InnoDB

# initialize new ticket fields
update tickets set createdate=1
update tickets set modifydate=1
update tickets set etag=''
