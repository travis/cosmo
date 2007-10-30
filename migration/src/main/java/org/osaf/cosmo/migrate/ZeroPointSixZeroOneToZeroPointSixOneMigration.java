/*
 * Copyright 2007 Open Source Applications Foundation
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
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Migration implementation that migrates Cosmo 0.6.0.1
 * database to 0.6.1  
 * 
 * Supports MySQL5 and Derby dialects only.
 *
 */
public class ZeroPointSixZeroOneToZeroPointSixOneMigration extends AbstractMigration {
    
    private static final Log log = LogFactory.getLog(ZeroPointSixZeroOneToZeroPointSixOneMigration.class);
    private HibernateHelper hibernateHelper = new HibernateHelper();
    
    public static final String PRODUCT_ID =
        "-//Open Source Applications Foundation//NONSGML Cosmo Sharing Server//EN";
    
    @Override
    public String getFromVersion() {
        return "0.6.0.1";
    }

    @Override
    public String getToVersion() {
        // switching to different schema version format
        return "100";
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
        
        migrateEvents(conn, dialect);
        migrateAttributes(conn);
    }
     
    private void migrateAttributes(Connection conn) throws Exception {
        
        PreparedStatement updateStmt = null;
        long count = 0;
        log.debug("starting migrateAttributes()");
        
        try {
            updateStmt = conn.prepareStatement("update attribute set createdate=?, modifydate=?");
            long currentTime = System.currentTimeMillis();
            updateStmt.setLong(1, currentTime);
            updateStmt.setLong(2, currentTime);
            count = updateStmt.executeUpdate();
        } finally {
            if(updateStmt!=null)
                updateStmt.close();
        }
        
        log.debug("processed " + count + " attributes");
    }
    
    
    private void migrateEvents(Connection conn, String dialect) throws Exception {
        PreparedStatement stmt = null;
        PreparedStatement insertStampStmt1 = null;
        PreparedStatement insertStampStmt2 = null;
        PreparedStatement insertItemStmt1 = null;
        PreparedStatement insertItemStmt2 = null;
        PreparedStatement insertParentStmt = null;
        PreparedStatement insertEventStmt = null;
        PreparedStatement insertAttributeStmt1 = null;
        PreparedStatement insertAttributeStmt2 = null;
        PreparedStatement updateEventStmt = null;
        PreparedStatement selectParentsStmt = null;
        
        ResultSet rs =  null;
        long count=0;
        long modCount=0;
        long modProblemCount=0;
        long duplicateModCount=0;
        long badEventCount = 0;
        long validationErrorCount = 0;
        
        System.setProperty("ical4j.unfolding.relaxed", "true");
        CalendarBuilder calBuilder = new CalendarBuilder();
        
        log.debug("begin migrateEvents()");
        
        try {
            stmt = conn.prepareStatement("select i.id, i.ownerid, i.icaluid, es.icaldata, i.displayName, i.uid, s.id from item i, stamp s, event_stamp es where i.id=s.itemid and s.id=es.stampid");
            
            insertItemStmt1 = conn.prepareStatement("insert into item (itemtype, ownerid, modifiesitemid, itemname, displayname, version, uid, icaluid, isactive, createdate, modifydate, isautotriage) values (?,?,?,?,?,0,?,?,1,?,?,1)");
            insertItemStmt2 = conn.prepareStatement("insert into item (itemtype, ownerid, modifiesitemid, itemname, displayname, version, uid, icaluid, isactive, createdate, modifydate, id, isautotriage) values (?,?,?,?,?,0,?,?,1,?,?,?,1)");
            insertItemStmt1.setString(1, "note");
            insertItemStmt2.setString(1, "note");
            
            insertItemStmt1.setLong(8, System.currentTimeMillis());
            insertItemStmt2.setLong(8, System.currentTimeMillis());
            
            insertItemStmt1.setLong(9, System.currentTimeMillis());
            insertItemStmt2.setLong(9, System.currentTimeMillis());
            
            insertStampStmt1 = conn.prepareStatement("insert into stamp (stamptype, itemid, createdate, modifydate, isactive) values (?,?,?,?,1)");
            insertStampStmt1.setString(1, "eventexception");
            insertStampStmt1.setLong(3, System.currentTimeMillis());
            insertStampStmt1.setLong(4, System.currentTimeMillis());
            insertStampStmt2 = conn.prepareStatement("insert into stamp (stamptype, itemid, id, createdate, modifydate, isactive) values (?,?,?,?,?,1)");
            insertStampStmt2.setString(1, "eventexception");
            insertStampStmt2.setLong(4, System.currentTimeMillis());
            insertStampStmt2.setLong(5, System.currentTimeMillis());
            
            insertAttributeStmt1 = conn.prepareStatement("insert into attribute (attributetype, namespace, localname, itemid, textvalue) values (?,?,?,?,?)");
            insertAttributeStmt2 = conn.prepareStatement("insert into attribute (attributetype, namespace, localname, itemid, textvalue, id) values (?,?,?,?,?,?)");
            insertAttributeStmt1.setString(1, "text");
            insertAttributeStmt2.setString(1, "text");
            insertAttributeStmt1.setString(2, "org.osaf.cosmo.model.NoteItem");
            insertAttributeStmt2.setString(2, "org.osaf.cosmo.model.NoteItem");
            insertAttributeStmt1.setString(3, "body");
            insertAttributeStmt2.setString(3, "body");
            
            updateEventStmt = conn.prepareStatement("update event_stamp set icaldata=? where stampid=?");
            
            insertEventStmt = conn.prepareStatement("insert into event_stamp (stampid, icaldata) values (?,?)");
            
            insertParentStmt = conn.prepareStatement("insert into collection_item (collectionid, itemid) values (?,?)");
            
            selectParentsStmt = conn.prepareStatement("select collectionid from collection_item where itemid=?");
            
            rs = stmt.executeQuery();
            
            while(rs.next()) {
                count++;
                long itemId = rs.getLong(1);
                long ownerId = rs.getLong(2);
                String icalUid = rs.getString(3);
                String icalData = rs.getString(4);
                String displayName = rs.getString(5);
                String parentUid = rs.getString(6);
                long stampId = rs.getLong(7);
                Calendar calendar = null;
                
                // Because we are using an updated ical4j, there might
                // be cases where parsing will fail.  If this happens,
                // the server will fail when loading the calendar, so
                // we have to fix it.  For now, fix by creating a replacement
                // calendar.
                try {
                    calendar = calBuilder.build(new StringReader(icalData));
                } catch (ParserException e) {
                    badEventCount++;
                    log.debug("cannot parse .ics for item " + itemId);
                    log.debug("parse error for:  " + icalData);
                    log.debug("error: " +  e.getMessage());
                    calendar = createReplacementCalendar(icalUid, e.getMessage());
                    calendar.validate();
                    updateEventStmt.setString(1, calendar.toString());
                    updateEventStmt.setLong(2, stampId);
                    updateEventStmt.executeUpdate();
                    log.debug("replaced with: " + calendar.toString());
                    continue;
                }
                
                try {
                    calendar.validate();
                } catch(ValidationException ve) {
                    validationErrorCount++;
                }
                
                ComponentList comps = calendar.getComponents().getComponents(Component.VEVENT);
                Vector<VEvent> mods = new Vector<VEvent>();
               
                // find event exceptions
                for(Iterator<VEvent> it =comps.iterator(); it.hasNext(); ) {
                    VEvent event = it.next();
                    if(event.getRecurrenceId()!=null && !"".equals(event.getRecurrenceId().getValue()))
                        mods.add(event);
                }
                
                // if no event exceptions, no migration needed
                if(mods.size()==0)
                    continue;
                
                modCount++;
                HashMap<String, VEvent> exceptionMap = new HashMap<String, VEvent>();
                
                boolean hasDuplicateMods = false;
                
                // Add item for each event exception
                for(VEvent mod : mods) {
                    calendar.getComponents().remove(mod);
                    Calendar modCalendar = createBaseCalendar(mod);
                    long newItemId = 0;
                    RecurrenceId recurrenceId = mod.getRecurrenceId();
                    Property summary = mod.getProperties().getProperty(Property.SUMMARY);
                    Property description = mod.getProperties().getProperty(Property.DESCRIPTION);
                    String eventSummary = null;
                    String eventDescription = null;
                    String uid = parentUid + ":" + fromDateToStringNoTimezone(recurrenceId.getDate());
                    
                    if(exceptionMap.containsKey(uid)) {
                        if(!hasDuplicateMods) {
                            hasDuplicateMods=true;
                            modProblemCount++;
                        }
                        duplicateModCount++;
                        log.debug("already processed exception " + uid + " skipping...");
                        continue;
                    }
                    
                    exceptionMap.put(uid, mod);
                    
                    if(summary!=null)
                        eventSummary = summary.getValue();
                    
                    // Make sure we can fit summary in displayname column
                    if(eventSummary!=null && eventSummary.length()>=255)
                        eventSummary = eventSummary.substring(0,254);
                    
                    if(description!=null)
                        eventDescription = description.getValue();
            
                    String itemName = uid;
                    
                    insertItemStmt1.setLong(2, ownerId);
                    insertItemStmt2.setLong(2, ownerId);
                    
                    insertItemStmt1.setLong(3, itemId);
                    insertItemStmt2.setLong(3, itemId);
                    
                    insertItemStmt1.setString(4, itemName);
                    insertItemStmt2.setString(4, itemName);
                    
                    if(eventSummary!=null) {
                        insertItemStmt1.setString(5, eventSummary);
                        insertItemStmt2.setString(5, eventSummary);
                    } else {
                        insertItemStmt1.setNull(5, Types.VARCHAR);
                        insertItemStmt2.setNull(5, Types.VARCHAR);
                    }
                    
                    insertItemStmt1.setString(6, uid);
                    insertItemStmt2.setString(6, uid);
                    
                    insertItemStmt1.setString(7, icalUid);
                    insertItemStmt2.setString(7, icalUid);
                    
                    // insert item
                    if("Derby".equals(dialect)) {
                        newItemId = hibernateHelper.getNexIdUsingHiLoGenerator(conn);
                        insertItemStmt2.setLong(10, newItemId);
                        insertItemStmt2.executeUpdate();
                    } else {
                        insertItemStmt1.executeUpdate();
                        ResultSet generatedKeysRs = insertItemStmt1.getGeneratedKeys();
                        generatedKeysRs.next();
                        newItemId = generatedKeysRs.getLong(1);
                        generatedKeysRs.close();
                    }
                    
                    // insert parents
                    selectParentsStmt.setLong(1, itemId);
                    ResultSet parentRs = selectParentsStmt.executeQuery();
                    while(parentRs.next()) {
                        long parentId = parentRs.getLong(1);
                        insertParentStmt.setLong(1, parentId);
                        insertParentStmt.setLong(2, newItemId);
                        insertParentStmt.executeUpdate();
                    }
                    
                    // insert attribute for Note body
                    if(eventDescription!=null) {
                        if("MySQL5".equals(dialect)) {
                            insertAttributeStmt1.setLong(4, newItemId);
                            insertAttributeStmt1.setString(5, eventDescription);
                            insertAttributeStmt1.executeUpdate();
                        } else {
                            long attributeId = hibernateHelper.getNexIdUsingHiLoGenerator(conn);
                            insertAttributeStmt2.setLong(4, newItemId);
                            insertAttributeStmt2.setString(5, eventDescription);
                            insertAttributeStmt2.setLong(6, attributeId);
                            insertAttributeStmt2.executeUpdate();
                        }
                    }
                    
                    // insert stamp for event exception
                    long newStampId = 0;
                    if("MySQL5".equals(dialect)) {
                        insertStampStmt1.setLong(2, newItemId);
                        insertStampStmt1.executeUpdate();
                        ResultSet generatedKeysRs = insertStampStmt1.getGeneratedKeys();
                        generatedKeysRs.next();
                        newStampId = generatedKeysRs.getLong(1);
                        generatedKeysRs.close();
                    } else {
                        newStampId = hibernateHelper.getNexIdUsingHiLoGenerator(conn);
                        insertStampStmt2.setLong(2, newItemId);
                        insertStampStmt2.setLong(3, newStampId);
                        insertStampStmt2.executeUpdate();
                    }
                    
                    // insert event_stamp
                    insertEventStmt.setLong(1, newStampId);
                    insertEventStmt.setString(2, modCalendar.toString());
                    insertEventStmt.executeUpdate();
                }
                    
                // update event_stamp for master event
                updateEventStmt.setString(1, calendar.toString());
                updateEventStmt.setLong(2, stampId);
                updateEventStmt.executeUpdate();  
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
            
            if(insertItemStmt1!=null)
                insertItemStmt1.close();
            
            if(insertParentStmt!=null)
                insertParentStmt.close();
            
            if(updateEventStmt!=null)
                updateEventStmt.close();
            
            if(insertEventStmt!=null)
                insertEventStmt.close();
            
            if(selectParentsStmt!=null)
                selectParentsStmt.close();
           
        }
    
        log.debug("processed " + count + " events");
        log.debug(modCount + " events had event exceptions");
        log.debug(duplicateModCount + " exceptions ignored due to duplicates");
        log.debug(modProblemCount + " events contain duplicate exceptions");
        log.debug(badEventCount + " bad events encountered");
        log.debug(validationErrorCount + " events failed validation");
    }
    
    private Calendar createBaseCalendar(VEvent event) {
        Calendar cal = new Calendar();
        cal.getProperties().add(new ProdId(PRODUCT_ID));
        cal.getProperties().add(Version.VERSION_2_0);
        cal.getProperties().add(CalScale.GREGORIAN);
        cal.getComponents().add(event);
        return cal;
    }
    
    /**
     * Create a replacement calendar to use when replacing a calendar that
     * failed parsing.
     */
    private Calendar createReplacementCalendar(String uid, String message) {
        VEvent event = new VEvent();
        DtStart dtStart = new DtStart(new Date());
        dtStart.getParameters().add(Value.DATE);
        event.getProperties().add(new Uid(uid));
        event.getProperties().add(dtStart);
        event.getProperties().add(new Description("removed by data migration"));
        event.getProperties().add(new Summary("this event was found to contain " +
                                              "invalid .ics data(" + message + ")" +
                                              " and was removed by data migration on " +
                                              new DateTime().toString()));
        return createBaseCalendar(event);
    }
    
    public static String fromDateToStringNoTimezone(Date date) {
        if(date==null)
            return null;
        
        if(date instanceof DateTime) {
            DateTime dt = (DateTime) date;
            // If DateTime has a timezone, then convert to UTC before
            // serializing as String.
            if(dt.getTimeZone()!=null) {
                // clone instance first to prevent changes to original instance
                DateTime copy = new DateTime(dt);
                copy.setUtc(true);
                return copy.toString();
            } else {
                return dt.toString();
            }
        } else {
            return date.toString();
        }
    }

}
