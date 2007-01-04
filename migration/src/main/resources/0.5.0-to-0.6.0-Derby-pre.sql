# 0.5.0-to-0.6.0-Derby-pre.sql
# migrate attribute table
# - add localname, namespace, textvalue columns
# - add not null constraint on itemid
# - drop old index
# - add new indexes
alter table attribute add column localname varchar(255) not null default ''
alter table attribute add column namespace varchar(255) not null default ''
alter table attribute add column textvalue clob(102400000)
alter table attribute add column decvalue numeric(19,6)
alter table attribute add column tzvalue varchar(32)
alter table attribute alter column itemid not null
# do this because in Derby we can't drop a column
alter table attribute alter column attributename null


drop index attrname_idx
create index idx_attrns on attribute (namespace)
create index idx_attrtype on attribute (attributetype)
create index idx_attrname on attribute (localname)

# migrate server_properties table
# - increase maximum size of propertyvalue to 2048
alter table server_properties alter column propertyvalue set data type varchar(2048)

# migrate cal_property_index and cal_timerange_index tables
# - add eventstampid field
# - add foreign key constraints on eventstampid
# - drop old index/foreign key constraints
alter table cal_property_index add column eventstampid bigint
alter table cal_property_index drop foreign key FKBA988E79EA427E04
alter table cal_timerange_index add column eventstampid bigint
alter table cal_timerange_index drop foreign key FK98D277F2EA427E04


# migrate item table
# - add isactive, lastmodifiedby, triagestatus, triagestatusupdated, icaluid
alter table item add column isactive smallint not null default 1
alter table item add column lastmodifiedby varchar(255)
alter table item add column triagestatus varchar(64)
alter table item add column triagestatusupdated numeric(19,6)
alter table item add column icaluid varchar(255)
alter table item add column createdate bigint
alter table item add column modifydate bigint
alter table item add column clientcreatedate bigint

# migrate users table
# - add createdate, modifydate
# - add activationid
# - add index on activationid
alter table users add column createdate bigint
alter table users add column modifydate bigint
alter table users add column activationid varchar(255)
create index idx_activationid on users (activationid)

# migrate ticket_privilege table
# - fix typo in ticketid name
# - change primary key to be combination of ticketid, privilege
alter table ticket_privilege add column ticketid bigint not null default 0
update ticket_privilege set ticketid=tickedid
alter table ticket_privilege alter column privilege not null
alter table ticket_privilege add primary key(ticketid, privilege) 
alter table ticket_privilege drop foreign key FKE492FD3E41A1E708
alter table ticket_privilege add constraint FKE492FD3E41A22318 foreign key (ticketid) references tickets


# add new tables
create table calendar_stamp (language varchar(255), description varchar(255), timezone clob(100000), stampid bigint not null, primary key (stampid))
create table event_stamp (icaldata clob(102400000) not null, stampid bigint not null, primary key (stampid))
create table message_stamp (msgsubject clob(262144), msgto clob(262144), msgcc clob(262144), msgbcc clob(262144), stampid bigint not null, primary key (stampid))

create table stamp (stamptype varchar(16) not null, id bigint not null, createdate bigint, modifydate bigint, isactive smallint not null, itemid bigint not null, primary key (id), unique (itemid, stamptype))
create table subscription (id bigint not null, displayname varchar(255) not null, collectionuid varchar(255) not null, ticketkey varchar(255) not null, ownerid bigint not null, primary key (id), unique (ownerid, displayname))

alter table calendar_stamp add constraint FK2B603B8280655080 foreign key (stampid) references stamp
alter table event_stamp add constraint FK1ACFBDDE2F8DB5CC foreign key (stampid) references stamp
alter table message_stamp add constraint FKB79DC58B8724FF3F foreign key (stampid) references stamp

alter table stamp add constraint FK68AC3C35361D2A6 foreign key (itemid) references item
alter table subscription add constraint FK1456591D5ACA52FE foreign key (ownerid) references users

create index idx_stamptype on stamp (stamptype)
