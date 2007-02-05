DELIMITER $$

##############################################
# 0.5-to-0.6.sql
# Migrates Cosmo 0.5 MySQL database
# to Cosmo 0.6 shema
##############################################

##############################################
#        name: migrateAttributes()
# description: Migrate attribute table from
#              Cosmo 0.5 schema to 0.6 schema.
#              Migration involves translating
#              old attributename to namespace,
#              localname.
##############################################
DROP PROCEDURE IF EXISTS migrateAttributes $$
CREATE PROCEDURE migrateAttributes()
BEGIN
   DECLARE ln varchar(255);
   DECLARE ns varchar(255);
   DECLARE attrname varchar(255);
   DECLARE nf, attrid INT;

   # cursor for all attributes
   DECLARE attrcursor CURSOR FOR SELECT id, attributename from attribute;
   DECLARE CONTINUE HANDLER FOR NOT FOUND
       SET nf = 1;
   SET nf = 0;
   OPEN attrcursor;

   # Migrate each attribute using cursor
   FETCH attrcursor into attrid, attrname;
   SET @counter = 0;
   WHILE nf = 0 DO
       SET @counter = @counter + 1;
       # handle special case of calendar:supportedComponentSet
       IF attrname = 'calendar:supportedComponentSet' THEN
           UPDATE attribute SET localname='supportedComponentSet', namespace='org.osaf.cosmo.model.CalendarCollectionStamp' WHERE id=attrid;
       # handle dead properties created using DAV
       ELSEIF INSTR(attrname, '@:@') > 0 THEN
           # namespace is encoded in attributename like  @:@[namespace]@:@[localname]
           SET ns = SUBSTRING(attrname, 4, LOCATE('@:@', attrname, 4) - 4 );
           SET ln = SUBSTRING(attrname, LOCATE('@:@', attrname, 4) + 3);
           UPDATE attribute SET localname=ln, namespace=ns WHERE id=attrid;
       # should not get here, but just in case
       ELSE
           UPDATE attribute SET localname=attrname, namespace='' WHERE id=attrid;
       END IF;
       FETCH attrcursor into attrid, attrname;
   END WHILE;
   CLOSE attrcursor;

   SELECT 'attributes processed', @counter;

END $$

##############################################
#        name: migrateItems()
# description: Migrate item table from
#              Cosmo 0.5 schema to 0.6 schema.
#              Migration involves setting the
#              isactive field to true.
##############################################
DROP PROCEDURE IF EXISTS migrateItems $$
CREATE PROCEDURE migrateItems()
BEGIN
   UPDATE item SET isactive=true;
END $$


##############################################
#        name: migrateCalendarCollections()
# description: Migrate calendar collection items
#              in item table from Cosmo 0.5 schema
#              to 0.6 schema.
#              Migration involves setting the
#              itemtype to 'collection', creating
#              an entry in stamp and calendar_stamp,
#              and removing attributes from the
#              attribute table.
##############################################
DROP PROCEDURE IF EXISTS migrateCalendarCollections $$
CREATE PROCEDURE migrateCalendarCollections()
BEGIN
   DECLARE descr, lang varchar(255);
   DECLARE tz varchar(1024);

   DEClARE nf, calid, currid INT;

   # Migrate each calendar item using cursor
   DECLARE calcolls CURSOR FOR SELECT id FROM item WHERE itemtype='calendar';
   DECLARE CONTINUE HANDLER FOR NOT FOUND
       SET nf = 1;
   SET nf = 0;
   OPEN calcolls;
   FETCH calcolls into calid;
   SET @counter = 0;

   WHILE nf = 0 DO
      SET @counter = @counter + 1;
      # create calendar stamp entry
      INSERT INTO stamp (stamptype, itemid) VALUES ('calendar',calid);
      SELECT id from stamp WHERE itemid=calid AND stamptype='calendar' INTO currid;
      # get attribute values to insert into calendar_stamp table
      SELECT stringvalue from attribute WHERE itemid=calid AND attributename='calendar:description' INTO descr;
      SELECT stringvalue from attribute WHERE itemid=calid AND attributename='calendar:language' INTO lang;
      SELECT stringvalue from attribute WHERE itemid=calid AND attributename='calendar:timezone' INTO tz;
      # one of the above selects may result in not found, so reset flag
      SET nf=0;
      # create calendar_stamp entry
      INSERT INTO calendar_stamp(stampid, language, description, timezone) VALUES (currid, lang, descr, tz);
      # delete unused attrbutes
      DELETE FROM attribute WHERE itemid=calid AND (attributename='calendar:description' OR attributename='calendar:language' OR attributename='calendar:timezone');
      FETCH calcolls into calid;
   END WHILE;
   CLOSE calcolls;

   # change itemtype
   UPDATE item SET itemtype='collection' WHERE itemtype='calendar';

   SELECT 'calendars processed', @counter;

END $$

##############################################
#        name: migrateEvents()
# description: Migrate event items in item table
#              from Cosmo 0.5 schema to 0.6 schema.
#              Migration involves setting the
#              itemtype to 'content', creating
#              an entry in stamp and event_stamp,
#              removing entry from the
#              content_data table, and updating
#              the cal_property_index and
#              cal_timerange_index tables.
##############################################
DROP PROCEDURE IF EXISTS migrateEvents $$
CREATE PROCEDURE migrateEvents()
BEGIN
   DECLARE evblob longblob;

   DEClARE nf, evid, currid, contid INT;

   # Migrate each event item using cursor
   DECLARE event_cursor CURSOR FOR SELECT id, contentdataid FROM item WHERE itemtype='event';
   DECLARE CONTINUE HANDLER FOR NOT FOUND
       SET nf = 1;
   SET nf = 0;
   OPEN event_cursor;
   FETCH event_cursor into evid, contid;
   SET @counter = 0;

   WHILE nf = 0 DO
      SET @counter = @counter + 1;
      # change itemtype, remove old content
      UPDATE item SET itemtype='note', contentdataid=null WHERE id=evid;
      # create event stamp entry
      INSERT INTO stamp (stamptype, itemid) VALUES ('event',evid);
      SELECT id from stamp WHERE itemid=evid AND stamptype='event' INTO currid;
      # get content
      SELECT content from content_data WHERE id=contid INTO evblob;
      # create event_stamp etnry
      INSERT INTO event_stamp (stampid, icaldata) VALUES (currid,CONVERT(evblob USING utf8));
      # update indexes to reflect stampid
      UPDATE cal_property_index SET eventstampid=currid WHERE itemid=evid;
      UPDATE cal_timerange_index SET eventstampid=currid WHERE itemid=evid;
      DELETE FROM content_data where id=contid;
      FETCH event_cursor into evid, contid;
   END WHILE;
   CLOSE event_cursor;

   SELECT 'events processed', @counter;

END $$

DELIMITER ;

START TRANSACTION;

# migrate attribute table
# - add localname, namespace, textvalue columns
# - add not null constraint on itemid
# - drop old index
# - add new indexes
alter table attribute add column localname varchar(255) not null;
alter table attribute add column namespace varchar(255) not null;
alter table attribute add column textvalue text;
alter table attribute add column decvalue numeric(19,6);
alter table attribute modify column itemid BIGINT(20) not null;
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

# migrate data using stored procedures
call migrateItems();
call migrateAttributes();
call migrateCalendarCollections();
call migrateEvents();

# no longer need these
DROP PROCEDURE migrateItems;
DROP PROCEDURE migrateAttributes;
DROP PROCEDURE migrateCalendarCollections;
DROP PROCEDURE migrateEvents;

# remove old data
alter table attribute drop column attributename;

# add new constraints
alter table attribute add unique itemid (itemid, namespace, localname);
alter table cal_property_index add index FKBA988E7927801F2 (eventstampid), add constraint FKBA988E7927801F2 foreign key (eventstampid) references event_stamp (stampid);
alter table cal_timerange_index add index FK98D277F227801F2 (eventstampid), add constraint FK98D277F227801F2 foreign key (eventstampid) references event_stamp (stampid);

# update server version
update server_properties SET propertyvalue='0.6-SNAPSHOT' WHERE propertyname='cosmo.schemaVersion';

COMMIT;