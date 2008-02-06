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
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.RecurrenceId;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * Migration implementation that migrates Cosmo 0.9 (schema ver 130)
 * to Cosmo 0.10 (schema ver 140)
 * 
 * Supports MySQL5 and Derby dialects only.
 *
 */
public class ZeroPointNineToZeroPointTenMigration extends AbstractMigration {
    
    private static final Log log = LogFactory.getLog(ZeroPointNineToZeroPointTenMigration.class);
    
    static {
        // use custom timezone registry
        System.setProperty("net.fortuna.ical4j.timezone.registry", "org.osaf.cosmo.calendar.CosmoTimeZoneRegistryFactory");
        System.setProperty("ical4j.unfolding.relaxed", "true");
        System.setProperty("ical4j.parsing.relaxed", "true");
        System.setProperty("ical4j.validation.relaxed", "true");
    }
    
    @Override
    public String getFromVersion() {
        return "130";
    }

    @Override
    public String getToVersion() {
        // switching to different schema version format
        return "140";
    }

    
    @Override
    public Set<String> getSupportedDialects() {
        HashSet<String> dialects = new HashSet<String>();
        dialects.add("Derby");
        dialects.add("MySQL5");
        dialects.add("PostgreSQL");
        return dialects;
    }

    public void migrateData(Connection conn, String dialect) throws Exception {
        
        log.debug("starting migrateData()");
        migrateEventModifications(conn);
        verifyEventModifications(conn);
    }
    
    
    /**
     * Fix modification items that have an incorrect recurrenceId
     */
    private void migrateEventModifications(Connection conn) throws Exception {
        
        PreparedStatement stmt = null;
        PreparedStatement updateEventStmt = null;
        
        CalendarBuilder calBuilder = new CalendarBuilder();
        
        ResultSet rs = null;
        
        long count = 0;
        long skipCount = 0;
        
        log.debug("starting migrateEventModifications()");
        
        try {
            // get all stamp/item data to migrate
            stmt = conn.prepareStatement("select i.uid, es.icaldata, es.stampid from item i, stamp s, event_stamp es where modifiesitemid is not null and i.id=s.itemid and s.id=es.stampid");
            // update event
            updateEventStmt = conn.prepareStatement("update event_stamp set icaldata=? where stampid=?");
           
            rs = stmt.executeQuery();
           
            // examine each modification and fix if necessary
            while(rs.next()) {
                String uid = rs.getString(1);
                String icaldata = rs.getString(2);
                long stampId = rs.getLong(3);
                Calendar calendar;
                
                try {
                    calendar = calBuilder.build(new StringReader(icaldata));
                } catch (ParserException e) {
                    log.error("unable to parse: " + icaldata);
                    throw e;
                }
                
                ComponentList events = calendar.getComponents().getComponents(
                        Component.VEVENT);
                VEvent event = (VEvent) events.get(0);
                
                RecurrenceId recurrenceId = event.getRecurrenceId();
                
                if(recurrenceId==null)
                    throw new RuntimeException("event " + icaldata + " has no recurrenceId!");
                
                Date recurrenceDate = recurrenceId.getDate();
                ModificationUid modUid = new ModificationUid(uid);
                
                // The problem was the recurrenceId in the .ics didn't match
                // the recurrenceId in the modUid.  We are assuming the
                // recurrenceId in the modUid is the correctOne, since it is
                // from the client.
                Date correctRecurrenceDate = modUid.getRecurrenceId();
                
                // verify we can fix first (there is a bug where we
                // don't store VTIMEZONEs in the exception calendar.
                // Ignore these events, and they are probably OK since they
                // use custom VTIMEZONES, and the affected events are using
                // the cosmo VTIMEZONE defs.
                if(recurrenceDate instanceof DateTime) {
                    DateTime dt = (DateTime) recurrenceDate;
                    Parameter param =  recurrenceId.getParameter(Parameter.TZID);
                    
                    // We don't care about floating date/times
                    if(param==null)
                        continue;
                    
                    // Make sure we have a timezone, otherwise we can't fix
                    if(dt.getTimeZone()==null) {
                        log.debug("ignoring event with custom VTIMEZONE " + icaldata);
                        skipCount++;
                        continue;
                    }
                } else {
                    // Don't care about All Day Events
                    continue;
                }
                
                // adjust time if times don't match
                if(recurrenceDate.getTime()!=correctRecurrenceDate.getTime()) {
                    log.debug("found incorrect recurrenceId for item : " + uid + " : " + icaldata);
                    long adj = correctRecurrenceDate.getTime() - recurrenceDate.getTime();
                    recurrenceDate.setTime(recurrenceDate.getTime() + adj);
                    log.debug("fixing to :" + recurrenceDate.toString());	
                    count++;
                    
                    updateEventStmt.setString(1, calendar.toString());
                    updateEventStmt.setLong(2, stampId);
                    
                    if(updateEventStmt.executeUpdate()!=1)
                        throw new RuntimeException("error updating event_stamp row");
                }
            }
            
            
        } finally {
            close(stmt);
            close(updateEventStmt);
            close(rs);
        }
        
        log.debug("fixed " + count + " modifications");
        log.debug("skipped " + skipCount + " modifications");
    }
    
    /**
     * Verify modification items don't have an incorrect recurrenceId
     */
    private void verifyEventModifications(Connection conn) throws Exception {
        
        PreparedStatement stmt = null;
       
        CalendarBuilder calBuilder = new CalendarBuilder();
        
        ResultSet rs = null;
        
        long count = 0;
        long skipCount = 0;
        
        log.debug("starting verifyEventModifications()");
        
        try {
            // get all stamp/item data to migrate
            stmt = conn.prepareStatement("select i.uid, es.icaldata, es.stampid from item i, stamp s, event_stamp es where modifiesitemid is not null and i.id=s.itemid and s.id=es.stampid");
          
            rs = stmt.executeQuery();
           
            // examine each modification and fix if necessary
            while(rs.next()) {
                String uid = rs.getString(1);
                String icaldata = rs.getString(2);
                
                Calendar calendar;
                
                try {
                    calendar = calBuilder.build(new StringReader(icaldata));
                } catch (ParserException e) {
                    log.error("unable to parse: " + icaldata);
                    throw e;
                }
                
                ComponentList events = calendar.getComponents().getComponents(
                        Component.VEVENT);
                VEvent event = (VEvent) events.get(0);
                
                RecurrenceId recurrenceId = event.getRecurrenceId();
                
                if(recurrenceId==null)
                    throw new RuntimeException("event " + icaldata + " has no recurrenceId!");
                
                Date recurrenceDate = recurrenceId.getDate();
                ModificationUid modUid = new ModificationUid(uid);
                Date correctRecurrenceDate = modUid.getRecurrenceId();
                
                // verify we can validate first (there is a bug where we
                // don't store VTIMEZONEs in the exception calendar.
                if(recurrenceDate instanceof DateTime) {
                    DateTime dt = (DateTime) recurrenceDate;
                    Parameter param =  recurrenceId.getParameter(Parameter.TZID);
                    
                    // We don't care about floating date/times
                    if(param==null)
                        continue;
                    
                    // Make sure we have a timezone, otherwise we can't validate
                    if(dt.getTimeZone()==null) {
                        log.debug("ignoring event with custom VTIMEZONE " + icaldata);
                        skipCount++;
                        continue;
                    }
                } else {
                    // Don't care about All Day Events
                    continue;
                }
                
                count++;
                
                // validate time
                if(recurrenceDate.getTime()!=correctRecurrenceDate.getTime())
                   throw new RuntimeException("found bad recurrenceId after fix");
                
            }
            
            
        } finally {
            close(stmt);
            close(rs);
        }
        
        log.debug("verified " + count + " modifications");
        log.debug("skipped " + skipCount + " modifications");
    }
    
    class ModificationUid {
        
        public static final String RECURRENCEID_DELIMITER = ":";
        
        String parentUid = null;
        Date recurrenceId = null;
        
       
        /**
         * Construct modification uid from String.
         * @param modUid modification uid
         * @throws ModelValidationException
         */
        public ModificationUid(String modUid) {
            String[] split = modUid.split(RECURRENCEID_DELIMITER);
            if(split.length!=2)
                throw new RuntimeException("invalid modification uid");
            parentUid = split[0];
            try {
                recurrenceId = fromStringToDate(split[1]);
            } catch (ParseException e) {
                throw new RuntimeException("invalid modification uid");
            }
        }
        
        public String toString() {
            return parentUid + RECURRENCEID_DELIMITER
                    + fromDateToStringNoTimezone(recurrenceId);
        }
        
        public String getParentUid() {
            return parentUid;
        }

        public void setParentUid(String parentUid) {
            this.parentUid = parentUid;
        }

        public Date getRecurrenceId() {
            return recurrenceId;
        }

        public void setRecurrenceId(Date recurrenceId) {
            this.recurrenceId = recurrenceId;
        }
        
        public boolean equals(Object obj) {
            if(obj instanceof ModificationUid) {
                ModificationUid modUid = (ModificationUid) obj;
                return (ObjectUtils.equals(parentUid, modUid.getParentUid()) &&
                       ObjectUtils.equals(recurrenceId, modUid.getRecurrenceId()));
            }
            return super.equals(obj);
        }
        
        public final int hashCode() {
            return new HashCodeBuilder().append(getParentUid())
                    .append(recurrenceId).toHashCode();
        }

        /**
         * Converts an ical4j Date instance to a string representation.  If the instance
         * is a DateTime and has a timezone, then the string representation will be
         * UTC.
         * @param date date to format
         * @return string representation of date
         */
        public String fromDateToStringNoTimezone(Date date) {
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
        
        /**
         * Returns an ical4j Date instance for the following formats:
         * <p>
         * <code>20070101T073000Z</code><br/>
         * <code>20070101T073000</code><br/>
         * <code>20070101</code>
         * <p>
         * @param dateStr date string to parse
         * @return ical4j Date instance
         * @throws ParseException
         */
        public Date fromStringToDate(String dateStr) throws ParseException {
            if(dateStr==null)
                return null;
            
            // handle date with no time first
            if(dateStr.indexOf("T") ==  -1) 
                return new Date(dateStr);
            // otherwise it must be a date time
            else
                return new DateTime(dateStr);
        }
    }
}
