# 100-to-110-Derby-pre.sql

# alter existing tables
# subscription
alter table subscription add column createdate bigint
alter table subscription add column modifydate bigint

# event_stamp
alter table event_stamp add column isfloating smallint
alter table event_stamp add column startdate varchar(16)
alter table event_stamp add column enddate varchar(16)
create index idx_startdt on event_stamp (startdate)
create index idx_enddt on event_stamp (enddate)

# migrate data
update item set itemtype='file' where itemtype='content'