# 0.5.0-to-0.6.0-MySQL5-pre.sql
# migrate attribute table
# - add localname, namespace, textvalue columns
# - add not null constraint on itemid
# - drop old index
# - add new indexes
alter table attribute add column localname varchar(255) not null;
alter table attribute add column namespace varchar(255) not null;
alter table attribute add column textvalue text;
alter table attribute add column decvalue numeric(19,6);
alter table attribute modify column itemid bigint(20) not null;
alter table attribute drop index attrname_idx;
alter table attribute add index idx_attrns (namespace);
alter table attribute add index idx_attrtype (attributetype);
alter table attribute add index idx_attrname (localname);

# migrate server_properties table
# - increase maximum size of propertyvalue to 2048
alter table server_properties modify column propertyvalue varchar(2048);

# migrate cal_property_index and cal_timerange_index tables
# - add eventstampid field
# - add foreign key constraints on eventstampid
# - drop old index/foreign key constraints
alter table cal_property_index add column eventstampid bigint;
alter table cal_property_index drop foreign key FKBA988E79EA427E04;
alter table cal_property_index drop index FKBA988E79EA427E04;
alter table cal_timerange_index add column eventstampid bigint;
alter table cal_timerange_index drop foreign key FK98D277F2EA427E04;
alter table cal_timerange_index drop index FK98D277F2EA427E04;


# migrate item table
# - add isactive, lastmodifiedby, triagestatus, triagestatusupdated, icaluid
alter table item add column isactive bit;
alter table item add column lastmodifiedby varchar(255);
alter table item add column triagestatus varchar(64);
alter table item add column triagestatusupdated numeric(19,6);
alter table item add column icaluid varchar(255);

# migrate users table
# - add activationid
# - add index on activationid
alter table users add column activationid varchar(255);
create index idx_activationid on users (activationid);

# migrate ticket_privilege table
# - fix typo in ticketid name
# - change primary key to be combination of ticketid, privilege
alter table ticket_privilege change column tickedid ticketid bigint not null, add primary key(ticketid, privilege), drop index FKE492FD3E41A1E708, drop foreign key FKE492FD3E41A1E708;
alter table ticket_privilege add constraint FKE492FD3E41A22318 foreign key FKE492FD3E41A22318 (ticketid) references tickets (id) on delete restrict on update restrict;


# add new tables
create table calendar_stamp (stampid bigint not null, language varchar(255), description varchar(255), timezone mediumtext, primary key (stampid));
create table event_stamp (stampid bigint not null, icaldata longtext not null, primary key (stampid));
create table message_stamp (stampid bigint not null, msgsubject mediumtext, msgto mediumtext, msgcc mediumtext, msgbcc mediumtext, primary key (stampid));

create table stamp (id bigint not null auto_increment, stamptype varchar(16) not null, itemid bigint not null, primary key (id), unique (itemid, stamptype));
create table subscription (id bigint not null auto_increment, displayname varchar(255) not null, collectionuid varchar(255) not null, ticketkey varchar(255) not null, ownerid bigint not null, primary key (id), unique (ownerid, displayname));

alter table calendar_stamp add index FK2B603B8280655080 (stampid), add constraint FK2B603B8280655080 foreign key (stampid) references stamp (id);
alter table event_stamp add index FK1ACFBDDE2F8DB5CC (stampid), add constraint FK1ACFBDDE2F8DB5CC foreign key (stampid) references stamp (id);
alter table message_stamp add index FKB79DC58B8724FF3F (stampid), add constraint FKB79DC58B8724FF3F foreign key (stampid) references stamp (id);

alter table stamp add index FK68AC3C35361D2A6 (itemid), add constraint FK68AC3C35361D2A6 foreign key (itemid) references item (id);
alter table subscription add index FK1456591D5ACA52FE (ownerid), add constraint FK1456591D5ACA52FE foreign key (ownerid) references users (id);

create index idx_stamptype on stamp (stamptype);
