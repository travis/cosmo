/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.dao.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.osaf.cosmo.calendar.util.CalendarFlattener;
import org.osaf.cosmo.model.CalendarPropertyIndex;
import org.osaf.cosmo.model.CalendarTimeRangeIndex;
import org.osaf.cosmo.model.EventStamp;

public class DefaultCalendarIndexer implements CalendarIndexer {

    private static final Log log = LogFactory
            .getLog(DefaultCalendarIndexer.class);
    
    private int maxPropertyNameLength = 255;
    private int maxPropertyValueLength = 20000;

   
    public void indexCalendarEvent(Session session, EventStamp event) {
        HashMap timeRangeMap = new HashMap();
        Map propertyMap = new HashMap();
        Collection indices = new ArrayList();
        CalendarFlattener flattener = new CalendarFlattener();
        Calendar calendar = event.getCalendar();
        propertyMap = flattener.flattenCalendarObject(calendar);
        flattener.doTimeRange(calendar, timeRangeMap);
        
        // remove previous indexes
        if (event.getId() != -1) {
            session.getNamedQuery("delete.calendarPropertyIndex").setParameter(
                    "eventStamp", event).executeUpdate();
            session.getNamedQuery("delete.calendarTimeRangeIndex")
                    .setParameter("eventStamp", event).executeUpdate();
        }
        
        for (Iterator it = propertyMap.entrySet().iterator(); it.hasNext();) {
            Entry nextEntry = (Entry) it.next();
            CalendarPropertyIndex index = new CalendarPropertyIndex();
            String name = (String) nextEntry.getKey();
            
            // Only index properties that fit within the
            // maximimum index length
            if(name.length() > maxPropertyNameLength) {
                log.warn("calendar property name '" + name + "'"
                        + " too long to index, skipping...");
                continue;
            }
                
            
            index.setName(name);
            index.setValue(getSearchablePropetyValue((String) nextEntry
                    .getValue()));
            event.addPropertyIndex(index);
        }
        
        for (Iterator it = timeRangeMap.entrySet().iterator(); it.hasNext();) {
            Entry entry = (Entry) it.next();
            addIndicesForTerm(indices, (String) entry.getKey(), (String) entry
                    .getValue());
        }

        for (Iterator it = indices.iterator(); it.hasNext();) {
            CalendarTimeRangeIndex index = (CalendarTimeRangeIndex) it.next();
            event.addTimeRangeIndex(index);
        }
    }

   
    /**
     * Return the maximum length of a propety value
     * that will be indexed.  All characters after this
     * maximum length will be truncated in the index and
     * will not be searchable.
     */
    public int getMaxPropertyValueLength() {
        return maxPropertyValueLength;
    }

    /**
     * Set the maximum length of a propety value
     * that will be indexed.  All characters after this
     * maximum length will be truncated in the index and
     * will not be searchable.
     * @param maxPropertyValueLength
     */
    public void setMaxPropertyValueLength(int maxPropertyValueLength) {
        this.maxPropertyValueLength = maxPropertyValueLength;
    }

    
    /**
     * Return the maximum length of a propety name
     * that will be indexed.  A property name that
     * is longer than this maximum length will
     * not be indexed.
     */
    public int getMaxPropertyNameLength() {
        return maxPropertyNameLength;
    }


    /**
     * Set the maximum length of a propety name
     * that will be indexed.  A property name that
     * is longer than this maximum length will
     * not be indexed.
     * @param maxPropertyNameLength
     */
    public void setMaxPropertyNameLength(int maxPropertyNameLength) {
        this.maxPropertyNameLength = maxPropertyNameLength;
    }


    private String getSearchablePropetyValue(String value) {
        if(value.length() > maxPropertyValueLength)
            return value.substring(0, maxPropertyValueLength);
        else
            return value;
    }
    
    private void addIndicesForTerm(Collection indices, String key, String value) {
        StringTokenizer periodTokens = new StringTokenizer(value, ",");
        boolean recurring = value.indexOf(',') > 0;

        while (periodTokens.hasMoreTokens()) {
            // Try to parse term data into start/end period items, or just a
            // start (which may happen if querying a single date property)
            String token = periodTokens.nextToken().toUpperCase();
            int slashPos = token.indexOf('/');
            String testStart = (slashPos != -1) ? token.substring(0, slashPos)
                    : token;
            String testEnd = (slashPos != -1) ? token.substring(slashPos + 1)
                    : null;

            // Check whether floating or fixed test required
            boolean fixed = (testStart.indexOf('Z') != -1);

            indices.add(createIndex(key, testStart, testEnd, !fixed, recurring));
        }
    }

    private CalendarTimeRangeIndex createIndex(String type, String start, String end,
            boolean isFloating, boolean isRecurring) {
        CalendarTimeRangeIndex index = new CalendarTimeRangeIndex();
        index.setType(type);
        index.setEndDate(end);
        index.setStartDate(start);
        index.setIsFloating(isFloating);
        index.setIsRecurring(isRecurring);
        return index;
    }

}
