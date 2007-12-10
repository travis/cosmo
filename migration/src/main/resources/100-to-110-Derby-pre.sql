# 100-to-110-Derby-pre.sql

# alter existing tables
# subscription
alter table subscription add column createdate bigint
alter table subscription add column modifydate bigint

# users
alter table users add column locked smallint

# remove old indexes
drop index idx_startdt
drop index idx_enddt

# event_stamp
alter table event_stamp add column isfloating smallint
alter table event_stamp add column isrecurring smallint
alter table event_stamp add column startdate varchar(16)
alter table event_stamp add column enddate varchar(16)
create index idx_startdt on event_stamp (startdate)
create index idx_enddt on event_stamp (enddate)
create index idx_floating on event_stamp (isfloating)
create index idx_recurring on event_stamp (isrecurring)

# user_preferences
rename table user_preferences to x_user_preferences
create table user_preferences (id bigint not null, createdate bigint, modifydate bigint, preferencename varchar(255) not null, preferencevalue varchar(255) not null, userid bigint not null, primary key (id), unique (userid, preferencename))

# migrate data
update item set itemtype='file' where itemtype='content'
update subscription set createdate=1181239766000, modifydate=1181239766000
update users set locked=0