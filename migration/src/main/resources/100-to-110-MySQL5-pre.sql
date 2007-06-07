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

# user_preferences
alter table user_preferences rename to x_user_preferences;
create table user_preferences (id bigint not null auto_increment, createdate bigint, modifydate bigint, preferencename varchar(255) not null, preferencevalue varchar(255) not null, userid bigint not null, primary key (id), unique (userid, preferencename)) ENGINE=InnoDB;

# migrate data
update item set itemtype='file' where itemtype='content';
insert into user_preferences (userid, preferencename, preferencevalue, createdate, modifydate) select userid, preferencename, preferencevalue, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000 from x_user_preferences;
drop table x_user_preferences;
alter table user_preferences add index FK199BD08467D36616 (userid), add constraint FK199BD08467D36616 foreign key (userid) references users (id);