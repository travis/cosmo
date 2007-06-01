# 100-to-110-MySQL5-pre.sql

# alter existing tables
# subscription
alter table subscription add column createdate bigint;
alter table subscription add column modifydate bigint;

# event_stamp
alter table event_stamp add column isfloating bit;
alter table event_stamp add column startdate varchar(16);
alter table event_stamp add column enddate varchar(16);
alter table event_stamp add index idx_startdt (startdate);
alter table event_stamp add index idx_enddt (enddate);

# migrate data
update item set itemtype='file' where itemtype='content';