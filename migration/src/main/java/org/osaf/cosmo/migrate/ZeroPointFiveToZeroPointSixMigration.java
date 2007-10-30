/*
 * Copyright 2005-2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.migrate;
import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Uid;

import org.apache.commons.id.uuid.VersionFourGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Migration implementation that migrates Cosmo 0.5
 * database to 0.6.  
 * 
 * Supports MySQL5 and Derby dialects only.
 *
 */
public class ZeroPointFiveToZeroPointSixMigration extends AbstractMigration {
    
    private static final Log log = LogFactory.getLog(ZeroPointFiveToZeroPointSixMigration.class);
    private HibernateHelper hibernateHelper = new HibernateHelper();
    
    @Override
    public String getFromVersion() {
        return "0.5.0";
    }

    @Override
    public String getToVersion() {
        return "0.6.0";
    }
    
    @Override
    public Set<String> getSupportedDialects() {
        HashSet<String> dialects = new HashSet<String>();
        dialects.add("Derby");
        dialects.add("MySQL5");
        return dialects;
    }

    public void migrateData(Connection conn, String dialect) throws Exception {
        
        log.debug("starting migrateData()");
         
        // set isActive=true for all items
        PreparedStatement stmt = conn.prepareStatement("update item set isactive=?");
        stmt.setBoolean(1, true);
        int updated = stmt.executeUpdate();
        
        log.debug("updated " + updated + " item rows");
        
        migrateUsers(conn);
        migrateItems(conn);
        migrateAttributes(conn);
        migrateCalendarCollections(conn, dialect);
        migrateEvents(conn, dialect);
        migrateStamps(conn, dialect);
    }
    
    private void migrateUsers(Connection conn) throws Exception {
        PreparedStatement stmt = null;
        PreparedStatement updateStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet rs =  null;
        long count = 0;
        log.debug("starting migrateUsers()");
        
        try {
            stmt = conn.prepareStatement("select id, datecreated, datemodified, admin from users");
            updateStmt = conn.prepareStatement("update users set createdate=?, modifydate=? where id=?");
            insertStmt = conn.prepareStatement("insert into user_preferences (userid, preferencename, preferencevalue) values (?, ?, ?)");
            insertStmt.setString(2, "Login.Url");
            
            rs = stmt.executeQuery();
            
            while(rs.next()) {
                count++;
                long userId = rs.getLong(1);
                Timestamp createTs = rs.getTimestamp(2);
                Timestamp modifyTs = rs.getTimestamp(3);
                boolean isAdmin = rs.getBoolean(4);
                
                // update user timestamps
                updateStmt.setLong(1, createTs.getTime());
                updateStmt.setLong(2, modifyTs.getTime());
                updateStmt.setLong(3, userId);

                updateStmt.executeUpdate();
                
                // insert account preference
                insertStmt.setLong(1, userId);
                insertStmt.setString(3, isAdmin ? "/account/view" : "/pim");
                insertStmt.executeUpdate();    
            }
        } finally {
            if(rs!=null)
                rs.close();
            if(stmt!=null)
                stmt.close();
            if(updateStmt!=null)
                updateStmt.close();
            if(insertStmt!=null)
                insertStmt.close();
        }
        
        log.debug("processed " + count + " users");
    }
    
    private void migrateItems(Connection conn) throws Exception {
        PreparedStatement stmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs =  null;
        long count = 0;
        log.debug("starting migrateItems()");
        
        try {
            stmt = conn.prepareStatement("select id, datecreated, datemodified from item");
            updateStmt = conn.prepareStatement("update item set createdate=?, modifydate=? where id=?");
            rs = stmt.executeQuery();
            
            while(rs.next()) {
                count++;
                long itemId = rs.getLong(1);
                Timestamp createTs = rs.getTimestamp(2);
                Timestamp modifyTs = rs.getTimestamp(3);
                
                updateStmt.setLong(1, createTs.getTime());
                updateStmt.setLong(2, modifyTs.getTime());
                updateStmt.setLong(3, itemId);

                updateStmt.executeUpdate();
            }
        } finally {
            if(rs!=null)
                rs.close();
            if(stmt!=null)
                stmt.close();
            if(updateStmt!=null)
                updateStmt.close();
        }
        
        log.debug("processed " + count + " items");
    }
    
    private void migrateAttributes(Connection conn) throws Exception {
        
        PreparedStatement stmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs =  null;
        long count = 0;
        log.debug("starting migrateAttributes()");
        
        try {
            stmt = conn.prepareStatement("select id, attributename from attribute");
            updateStmt = conn.prepareStatement("update attribute set namespace=?, localname=? where id=?");
            rs = stmt.executeQuery();
            
            while(rs.next()) {
                count++;
                long attributeId = rs.getLong(1);
                String attributeName = rs.getString(2);
                
                if("calendar:supportedComponentSet".equals(attributeName)) {
                    updateStmt.setString(1, "org.osaf.cosmo.model.CalendarCollectionStamp");
                    updateStmt.setString(2, "supportedComponentSet" );
                } else if("cosmo:excludeFreeBusyRollup".equals(attributeName)) {
                    updateStmt.setString(1, "org.osaf.cosmo.model.CollectionItem");
                    updateStmt.setString(2, "excludeFreeBusyRollup" );
                } else if(attributeName.indexOf("@:@") >= 0) {
                    String namespace = null;
                    String localname = null;
                    String[] chunks = attributeName.split("@:@",3);
                    
                    // no namespace
                    if(chunks.length==1) {
                        namespace = "org.osaf.cosmo.default";
                        localname = chunks[0];
                    } 
                    // no prefix
                    else if(chunks.length==2) {
                        namespace = chunks[0];
                        localname = chunks[1];
                    } 
                    // all three present, just need namespace and localname
                    else {
                        namespace = chunks[1];
                        localname = chunks[2];
                    }
                    
                    updateStmt.setString(1, namespace);
                    updateStmt.setString(2, localname);
                } else {
                    updateStmt.setString(1, "org.osaf.cosmo.default" );
                    updateStmt.setString(2, attributeName);
                }
                
                updateStmt.setLong(3, attributeId);
                updateStmt.executeUpdate();
            }
        } finally {
            if(rs!=null)
                rs.close();
            if(stmt!=null)
                stmt.close();
            if(updateStmt!=null)
                updateStmt.close();
        }
        
        log.debug("processed " + count + " attributes");
        
    }
    
    private void migrateCalendarCollections(Connection conn, String dialect) throws Exception {
        PreparedStatement stmt = null;
        PreparedStatement insertStampStmt1 = null;
        PreparedStatement insertStampStmt2 = null;
        PreparedStatement insertCalendarStmt = null;
        PreparedStatement deleteAttributeStmt = null;
        PreparedStatement selectAttributeStmt = null;
        PreparedStatement updateCollectionStmt = null;
        
        ResultSet rs =  null;
        long count = 0;
        
        log.debug("begin migrateCalendarCollections()");
        
        try {
            stmt = conn.prepareStatement("select id from item where itemtype=?");
            stmt.setString(1, "calendar");
            
            insertStampStmt1 = conn.prepareStatement("insert into stamp (stamptype, itemid, isactive) values (?,?,1)");
            insertStampStmt1.setString(1, "calendar");
            insertStampStmt2 = conn.prepareStatement("insert into stamp (stamptype, itemid, id, isactive) values (?,?,?,1)");
            insertStampStmt2.setString(1, "calendar");
            
            insertCalendarStmt = conn.prepareStatement("insert into calendar_stamp (stampid, language, description, timezone) values (?,?,?,?)");
            
            selectAttributeStmt = conn.prepareStatement("select stringvalue from attribute where itemid=? and attributename=?");
            updateCollectionStmt = conn.prepareStatement("update item set itemtype=? where itemtype=?");
            updateCollectionStmt.setString(1, "collection");
            updateCollectionStmt.setString(2, "calendar");
            
            deleteAttributeStmt = conn.prepareStatement("delete from attribute where itemid=? and (attributename=? or attributename=? or attributename=?)");
            
            deleteAttributeStmt.setString(2, "calendar:description");
            deleteAttributeStmt.setString(3, "calendar:language");
            deleteAttributeStmt.setString(4, "calendar:timezone");
            
            rs = stmt.executeQuery();
            
            while(rs.next()) {
                count++;
                long itemId = rs.getLong(1);
                long stampId = 0;
                
                if("MySQL5".equals(dialect)) {
                    insertStampStmt1.setLong(2, itemId);
                    insertStampStmt1.executeUpdate();
                } else {
                    stampId = hibernateHelper.getNexIdUsingHiLoGenerator(conn);
                    insertStampStmt2.setLong(2, itemId);
                    insertStampStmt2.setLong(3, stampId);
                    insertStampStmt2.executeUpdate();
                }
                
                if("MySQL5".equals(dialect)) {
                    ResultSet generatedKeysRs = insertStampStmt1.getGeneratedKeys();
                    generatedKeysRs.next();
                    stampId = generatedKeysRs.getLong(1);
                    generatedKeysRs.close();
                }
                
                String description = null;
                String timezone = null;
                String language = null;
                
                selectAttributeStmt.setLong(1, itemId);
                selectAttributeStmt.setString(2,"calendar:description");
                ResultSet attrRs = selectAttributeStmt.executeQuery();
                if(attrRs.next())
                    description = attrRs.getString(1);

                attrRs.close();
                selectAttributeStmt.setString(2,"calendar:language");
                attrRs = selectAttributeStmt.executeQuery();
                if(attrRs.next())
                    language = attrRs.getString(1);
                
                attrRs.close();
                selectAttributeStmt.setString(2,"calendar:timezone");
                attrRs = selectAttributeStmt.executeQuery();
                if(attrRs.next())
                    timezone = attrRs.getString(1);
                
               
                insertCalendarStmt.setLong(1, stampId);
            
                if(language!=null)
                    insertCalendarStmt.setString(2, language);
                else
                    insertCalendarStmt.setNull(2, Types.VARCHAR);
                
                if(description != null)
                    insertCalendarStmt.setString(3, description);
                else
                    insertCalendarStmt.setNull(3, Types.VARCHAR);
                
                if(timezone!=null)
                    insertCalendarStmt.setString(4,timezone);
                else
                    insertCalendarStmt.setNull(4, Types.CLOB);
                
                insertCalendarStmt.executeUpdate();
               
                
                deleteAttributeStmt.setLong(1, itemId);
                deleteAttributeStmt.executeUpdate();
            }
            
            updateCollectionStmt.executeUpdate();
            
        } finally {
            if(rs!=null)
                rs.close();
            if(stmt!=null)
                stmt.close();
            if(insertStampStmt1!=null)
                insertStampStmt1.close();
            if(insertStampStmt2!=null)
                insertStampStmt2.close();
            if(insertCalendarStmt!=null)
                insertCalendarStmt.close();
            if(deleteAttributeStmt!=null)
                deleteAttributeStmt.close();
            if(selectAttributeStmt!=null)
                selectAttributeStmt.close();
            if(updateCollectionStmt!=null)
                updateCollectionStmt.close();
        }
        
        log.debug("processed " + count + " calendars");
    }
    
    private void migrateEvents(Connection conn, String dialect) throws Exception {
        PreparedStatement stmt = null;
        PreparedStatement insertStampStmt1 = null;
        PreparedStatement insertStampStmt2 = null;
        PreparedStatement insertEventStmt = null;
        PreparedStatement insertAttributeStmt1 = null;
        PreparedStatement insertAttributeStmt2 = null;
        PreparedStatement deleteContentDataStmt = null;
        PreparedStatement selectContentDataStmt = null;
        PreparedStatement updateEventStmt = null;
        PreparedStatement updatePropsStmt = null;
        PreparedStatement updateTimerangesStmt = null;
        
        ResultSet rs =  null;
        long count=0;
        
        System.setProperty("ical4j.unfolding.relaxed", "true");
        CalendarBuilder calBuilder = new CalendarBuilder();
        
        VersionFourGenerator uidGenerator = new VersionFourGenerator();
        
        log.debug("begin migrateEvents()");
        
        try {
            stmt = conn.prepareStatement("select id, contentdataid from item where itemtype=?");
            stmt.setString(1, "event");
            
            insertStampStmt1 = conn.prepareStatement("insert into stamp (stamptype, itemid, isactive) values (?,?,1)");
            insertStampStmt1.setString(1, "event");
            insertStampStmt2 = conn.prepareStatement("insert into stamp (stamptype, itemid, id, isactive) values (?,?,?,1)");
            insertStampStmt2.setString(1, "event");
            
            insertAttributeStmt1 = conn.prepareStatement("insert into attribute (attributetype, namespace, localname, itemid, textvalue, attributename) values (?,?,?,?,?,'a')");
            insertAttributeStmt2 = conn.prepareStatement("insert into attribute (attributetype, namespace, localname, itemid, textvalue, id, attributename) values (?,?,?,?,?,?,'a')");
            insertAttributeStmt1.setString(1, "text");
            insertAttributeStmt2.setString(1, "text");
            insertAttributeStmt1.setString(2, "org.osaf.cosmo.model.NoteItem");
            insertAttributeStmt2.setString(2, "org.osaf.cosmo.model.NoteItem");
            insertAttributeStmt1.setString(3, "body");
            insertAttributeStmt2.setString(3, "body");
            
            deleteContentDataStmt = conn.prepareStatement("delete from content_data where id=?");
            selectContentDataStmt = conn.prepareStatement("select content from content_data where id=?");
            
            updateEventStmt = conn.prepareStatement("update item set itemtype=?, contentdataid=?, contentlength=?, icaluid=?, displayname=? where id=?");
            updateEventStmt.setString(1, "note");
            updateEventStmt.setNull(2, Types.BIGINT);
            
            insertEventStmt = conn.prepareStatement("insert into event_stamp (stampid, icaldata) values (?,?)");
            updatePropsStmt = conn.prepareStatement("update cal_property_index set eventstampid=? where itemid=?");
            updateTimerangesStmt = conn.prepareStatement("update cal_timerange_index set eventstampid=? where itemid=?");
            
            rs = stmt.executeQuery();
            
            while(rs.next()) {
                count++;
                long itemId = rs.getLong(1);
                long contentDataId = rs.getLong(2);
                long stampId = 0;
                
                // Add record to stamp
                if("MySQL5".equals(dialect)) {
                    insertStampStmt1.setLong(2, itemId);
                    insertStampStmt1.executeUpdate();
                } else {
                    stampId = hibernateHelper.getNexIdUsingHiLoGenerator(conn);
                    insertStampStmt2.setLong(2, itemId);
                    insertStampStmt2.setLong(3, stampId);
                    insertStampStmt2.executeUpdate();
                }
                
                // MySQL uses autogenerated id
                if("MySQL5".equals(dialect)) {
                    ResultSet generatedKeysRs = insertStampStmt1.getGeneratedKeys();
                    generatedKeysRs.next();
                    stampId = generatedKeysRs.getLong(1);
                    generatedKeysRs.close();
                }
          
                // Get binary content data
                selectContentDataStmt.setLong(1, contentDataId);
                
                Calendar calendar = null;
                long icalLength = 0;
                String icalUid = null;
                String eventDesc = null;
                String eventSummary = null;
                ResultSet contentDataRs = selectContentDataStmt.executeQuery();
                if(contentDataRs.next()) {
                    log.debug("itemid=" + itemId);
                    Blob icalBlob = contentDataRs.getBlob(1);
                    byte[] icalBytes = icalBlob.getBytes(1, (int) icalBlob.length());
                    // have to parse data into Calendar to get right contentlength
                    calendar = calBuilder.build(new ByteArrayInputStream(icalBytes));
                    VEvent event = (VEvent) calendar.getComponents().getComponents(
                            Component.VEVENT).get(0);
                    
                    // Now that we parsed, lets get the UID, DESCRIPTION, and
                    // SUMMARY so we can update NoteItem, ContentItem
                    Uid uid = event.getUid();
                    
                    // Handle the case where events don't have a UID (should be rare)
                    if(uid!=null)
                        icalUid = event.getUid().getValue();
                    
                    if(icalUid==null || "".equals(icalUid))
                        icalUid = null;
                    
                    // If there is no UID, create a new one
                    if(icalUid==null) {
                        icalUid = uidGenerator.nextIdentifier().toString();
                        if(uid!=null)
                            uid.setValue(icalUid);
                        else
                            event.getProperties().add(new Uid(icalUid));
                    }
                    
                    Property p = event.getProperties().getProperty(Property.DESCRIPTION);
                    if(p!=null)
                        eventDesc = p.getValue();
                    
                    if("".equals(eventDesc))
                        eventDesc = null;
                    
                    p = event.getProperties().getProperty(Property.SUMMARY);
                    if(p!=null)
                        eventSummary = p.getValue();
                    
                    if("".equals(eventSummary))
                        eventSummary = null;
                    
                    // Make sure we can fit summary in displayname column
                    if(eventSummary!=null && eventSummary.length()>=255)
                        eventSummary = eventSummary.substring(0,254);
                    
                    // Calculate new length
                    icalLength = calendar.toString().getBytes("UTF-8").length;
                }
                
                contentDataRs.close();
                
                // update item record with new contentLength, itemtype,
                // icaluid, and displayname
                updateEventStmt.setLong(3, icalLength);
                updateEventStmt.setString(4, icalUid);
                if(eventSummary!=null)
                    updateEventStmt.setString(5, eventSummary);
                else
                    updateEventStmt.setNull(5, Types.VARCHAR);
                updateEventStmt.setLong(6, itemId);
                updateEventStmt.executeUpdate();
                
                // add event_stamp record
                insertEventStmt.setLong(1, stampId);
                insertEventStmt.setString(2, calendar.toString());
                
                insertEventStmt.executeUpdate();
                
                // If there is a DESCRIPTION, add a text attribute
                if(eventDesc!=null) {
                    if("MySQL5".equals(dialect)) {
                        insertAttributeStmt1.setLong(4, itemId);
                        insertAttributeStmt1.setString(5, eventDesc);
                        insertAttributeStmt1.executeUpdate();
                    } else {
                        long attributeId = hibernateHelper.getNexIdUsingHiLoGenerator(conn);
                        insertAttributeStmt2.setLong(4, itemId);
                        insertAttributeStmt2.setString(5, eventDesc);
                        insertAttributeStmt2.setLong(6, attributeId);
                        insertAttributeStmt2.executeUpdate();
                    }
                }
                
                // Update calendar indexes to reflect item and stamp
                updatePropsStmt.setLong(1, stampId);
                updatePropsStmt.setLong(2, itemId);
                updatePropsStmt.executeUpdate();
                
                updateTimerangesStmt.setLong(1, stampId);
                updateTimerangesStmt.setLong(2, itemId);
                updateTimerangesStmt.executeUpdate();
                
                // no longer need content for events
                deleteContentDataStmt.setLong(1, contentDataId);
                deleteContentDataStmt.executeUpdate();
            }
              
        } finally {
            if(rs != null)
                rs.close();
            
            if(stmt!=null)
                stmt.close();
            
            if(insertStampStmt1!=null)
                insertStampStmt1.close();
            
            if(insertStampStmt2!=null)
                insertStampStmt2.close();
            
            if(insertAttributeStmt1!=null)
                insertAttributeStmt1.close();
            
            if(insertAttributeStmt2!=null)
                insertAttributeStmt2.close();
            
            if(deleteContentDataStmt!=null)
                deleteContentDataStmt.close();
            
            if(selectContentDataStmt!=null)
                selectContentDataStmt.close();
            
            if(updateEventStmt!=null)
                updateEventStmt.close();
            
            if(insertEventStmt!=null)
                insertEventStmt.close();
            
            if(updatePropsStmt!=null)
                updatePropsStmt.close();
            
            if(updateTimerangesStmt!=null)
                updateTimerangesStmt.close();
        }
    
        log.debug("processed " + count + " events");
    }
    
    private void migrateStamps(Connection conn, String dialect) throws Exception {
        
        PreparedStatement updateStmt = null;

        long count = 0;
        log.debug("starting migrateStamps()");
        
        try {
            
            updateStmt = conn.prepareStatement("update stamp set createdate=?, modifydate=?, isactive=1");
            long currTime = System.currentTimeMillis();
            updateStmt.setLong(1, currTime);
            updateStmt.setLong(2, currTime);
            
            count = updateStmt.executeUpdate();
            
        } finally {
            if(updateStmt!=null)
                updateStmt.close();
        }
        
        log.debug("processed " + count + " stamps");
    }

}
