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
package org.osaf.cosmo.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.query.ComponentFilter;
import org.osaf.cosmo.calendar.query.TimeRangeFilter;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.DuplicateItemNameException;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.rpc.model.Calendar;
import org.osaf.cosmo.rpc.model.CosmoDate;
import org.osaf.cosmo.rpc.model.CosmoToICalendarConverter;
import org.osaf.cosmo.rpc.model.Event;
import org.osaf.cosmo.rpc.model.ICalendarToCosmoConverter;
import org.osaf.cosmo.rpc.model.RecurrenceRule;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.util.PathUtil;

public class RPCServiceImpl implements RPCService {
    private static final Log log =
        LogFactory.getLog(RPCServiceImpl.class);

    private UserService userService = null;
    private ContentService contentService = null;
    private CosmoSecurityManager cosmoSecurityManager = null;
    private ICalendarToCosmoConverter icalendarToCosmoConverter = new ICalendarToCosmoConverter();
    private CosmoToICalendarConverter cosmoToICalendarConverter = new CosmoToICalendarConverter();

    public CosmoSecurityManager getCosmoSecurityManager() {
        return cosmoSecurityManager;
    }

    public void setCosmoSecurityManager(CosmoSecurityManager cosmoSecurityManager) {
        this.cosmoSecurityManager = cosmoSecurityManager;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public String createCalendar(String displayName)
        throws RPCException {

        User user = getUser();
        if (user == null){
            throw new RPCException("You must be logged in to create a collection");
        }
                
        CollectionItem collection = contentService.getRootItem(getUser());
        CollectionItem calendar = new CollectionItem();
        calendar.setName(displayName);
        calendar.setOwner(user);
        calendar.setDisplayName(displayName);
        CalendarCollectionStamp ccs = new CalendarCollectionStamp(calendar);
        calendar.addStamp(ccs);

        try {
            this.createCalendarHandleDuplicateName(collection, calendar, 0);
        } catch (Exception e) {
            throw new RPCException("Cannot create calendar: " + e.getMessage(), e);
        }
        return calendar.getUid();
    }
    
    private void createCalendarHandleDuplicateName(CollectionItem collection, 
        CollectionItem calendar, int i) throws RPCException {
        try {
            contentService.createCollection(collection, calendar);
        } catch (DuplicateItemNameException e) {
            calendar.setName(calendar.getName() + Integer.toString(i));
            createCalendarHandleDuplicateName(collection, calendar, i+1);
        }
    }

    public Calendar[] getCalendars() throws RPCException {
        if (log.isDebugEnabled())
            log.debug("Getting calendars in home collection of " +
                      getUser().getUsername());
        // XXX no ContentService.findCalendars yet
        HomeCollectionItem home = contentService.getRootItem(getUser());
        List<Calendar> cals = new ArrayList<Calendar>();
        for (Iterator<Item> i=home.getChildren().iterator(); i.hasNext();) {
            Item child = i.next();
            if (child.getStamp(CalendarCollectionStamp.class)==null)
                continue;
            Calendar calendar = new Calendar();
            calendar.setName(child.getDisplayName());
            calendar.setUid(child.getUid());
            
            cals.add(calendar);
        }
        //TODO sort these
        return (Calendar[]) cals.toArray(new Calendar[cals.size()]);
    }

    public Event getEvent(String collectionUid, String id) throws RPCException {
        return this.doGetEvent(collectionUid, id);
    }
    
    public Event getEvent(String collectionUid, String id, String ticket) throws RPCException {
        return doGetEvent(collectionUid, id);
    }

    private Event doGetEvent(String collectionUid, String id) throws RPCException{
        
        if (log.isDebugEnabled())
            log.debug("Getting event " + id + " in calendar with uid " +
                      collectionUid);
        ContentItem calItem = (ContentItem) contentService.findItemByUid(id);
        if (calItem == null){
            return null;
        }
    
        EventStamp event = EventStamp.getStamp(calItem);
        if (event == null){
            return null;
        }
    
        try {
            return icalendarToCosmoConverter.createEvent(calItem.getUid(),
                    event.getMasterEvent(), event.getCalendar());
        } catch (Exception e) {
            log.error("Problem getting event: userName: " + getUsername()
                    + " calendarUid: " + collectionUid
                    + " id: " +id, e);
            throw new RPCException("Problem getting event", e);
        }
    }

    public Event[] getEvents(String collectionUid, long utcStartTime,
            long utcEndTime) throws RPCException {
        
        return doGetEvents(collectionUid, utcStartTime, utcEndTime);
    }
    public Event[] getEvents(String collectionUid, long utcStartTime, long utcEndTime, String ticket) throws RPCException {
        return doGetEvents(collectionUid, utcStartTime, utcEndTime);
    }

    private Event[] doGetEvents(String collectionUid, long utcStartTime,
                long utcEndTime) throws RPCException {
        DateTime start = new DateTime(utcStartTime);
        start.setUtc(true);

        DateTime end = new DateTime(utcEndTime);
        end.setUtc(true);

        ComponentFilter eventFilter = new ComponentFilter(Component.VEVENT);
        eventFilter.setTimeRangeFilter(new TimeRangeFilter(start, end));

        ComponentFilter calFilter =
            new ComponentFilter(net.fortuna.ical4j.model.Calendar.VCALENDAR);
        calFilter.getComponentFilters().add(eventFilter);

        CalendarFilter filter = new CalendarFilter();
        filter.setFilter(calFilter);

        
        if (log.isDebugEnabled())
            log.debug("Getting events between " + start + " and " + end +
                      " in calendar with uid " + collectionUid);

        // XXX: need ContentService.findEvents(path, filter)
        CollectionItem collection = (CollectionItem)
            contentService.findItemByUid(collectionUid);
        if (collection == null)
            throw new RPCException("Collection with uid " + collectionUid + " does not exist");

        Set<ContentItem> calendarItems = null;
        try {
            calendarItems = contentService.findEvents(collection, filter);
        } catch (Exception e) {
            log.error("cannot find events for calendar with uid " + collectionUid, e);
            throw new RPCException("Cannot find events for calendar with uid " + collectionUid + ": " + e.getMessage(), e);
        }

        Event[] events = null;
        try {
            DateTime beginDate = new DateTime();
            beginDate.setUtc(true);
            beginDate.setTime(utcStartTime);
            DateTime endDate = new DateTime();
            endDate.setUtc(true);
            endDate.setTime(utcEndTime);

            events = icalendarToCosmoConverter.
                createEventsFromCalendars(calendarItems, beginDate, endDate);
        } catch (Exception e) {
            log.error("Problem getting events: userName: " + getUsername()
                      + " calendarUid: " + collectionUid
                      + " beginDate: " + utcStartTime
                      + " endDate: " + utcStartTime, e);
            throw new RPCException("Problem getting events", e);
        }
        return events;
    }

    public String getPreference(String preferenceName) throws RPCException {
        if (log.isDebugEnabled())
            log.debug("Getting preference " + preferenceName);
       return userService.getPreference(getUsername(), preferenceName);
    }

    public String getVersion() {
        return CosmoConstants.PRODUCT_VERSION;
    }

    public void removeCalendar(String collectionUid) throws RPCException {
        
        if (log.isDebugEnabled())
            log.debug("Removing collection with uid " + collectionUid);
        
        contentService.removeItem(
                contentService.findItemByUid(collectionUid));
        
    }
    
    public void removeEvent(String collectionUid, String id) 
        throws RPCException {
        doRemoveEvent(collectionUid, id);
    }
    
    public void removeEvent(String collectionUid, String id, String ticket) throws RPCException {
        doRemoveEvent(collectionUid, id);
        
    }

    private void doRemoveEvent(String collectionUid, String id)
        throws RPCException {
        
        if (log.isDebugEnabled())
            log.debug("Removing event " + id + " from calendar with uid" +
                      collectionUid);
        ContentItem calItem = (ContentItem) contentService.findItemByUid(id);
        contentService.removeContent(calItem);
    }

    public void removePreference(String preferenceName) throws RPCException {
        if (log.isDebugEnabled())
            log.debug("Removing preference " + preferenceName);
        userService.removePreference(getUsername(), preferenceName);
    }

    public String saveEvent(String collectionUid, Event event)
        throws RPCException {
        return doSaveEvent(collectionUid, event);
    }
    
    public String saveEvent(String collectionUid, Event event, String ticket) throws RPCException {
        return doSaveEvent(collectionUid, event);
    }

    private String doSaveEvent(String collectionUid, Event event)
        throws RPCException {

        ContentItem calendarEventItem = null;
        
        CollectionItem calendarItem = getCalendarCollectionItem(collectionUid);
        calendarItem = (CollectionItem) contentService.findItemByUid(collectionUid);
        
        //Check to see if this is a new event
        if (StringUtils.isEmpty(event.getId())) {
            calendarEventItem = saveNewEvent(event, calendarItem);

        } else {
            calendarEventItem = (ContentItem) contentService.findItemByUid(event.getId());
            calendarEventItem.setDisplayName(event.getTitle());

            EventStamp eventStamp = EventStamp.getStamp(calendarEventItem);
            net.fortuna.ical4j.model.Calendar calendar = eventStamp.getCalendar();
            cosmoToICalendarConverter.updateEvent(event, calendar);
            cosmoToICalendarConverter.updateVTimeZones(calendar);
            eventStamp.setCalendar(calendar);

            // update NoteItem attributes
            if(calendarEventItem instanceof NoteItem) {
                ((NoteItem) calendarEventItem).setIcalUid(eventStamp.getIcalUid());
                ((NoteItem) calendarEventItem).setBody(event.getDescription());
            }
            contentService.updateContent(calendarEventItem);
        }

        return calendarEventItem.getUid();
    }

    public void setPreference(String preferenceName, String value)
            throws RPCException {
        if (log.isDebugEnabled())
            log.debug("Setting preference " + preferenceName + " to " + value);
        userService.setPreference(getUsername(),preferenceName);
    }

    
    public Map<String, RecurrenceRule> getRecurrenceRules(String collectionUid,
            String[] eventIds) throws RPCException {
        return doGetRecurrenceRules(collectionUid, eventIds); 
    }
    public Map<String, RecurrenceRule> getRecurrenceRules(String collectionUid, String[] eventIds, String ticket) throws RPCException {

        return doGetRecurrenceRules(collectionUid, eventIds);
    }

    private Map<String, RecurrenceRule> doGetRecurrenceRules(String collectionUid,
            String[] eventIds) throws RPCException {
        Map<String, RecurrenceRule> map = new HashMap<String, RecurrenceRule>();
        for (int x = 0; x < eventIds.length; x++) {
            String eventId = eventIds[x];
            ContentItem calItem = (ContentItem) contentService.findItemByUid(eventId);
            EventStamp eventStamp = EventStamp.getStamp(calItem);
            Event e = icalendarToCosmoConverter.createEvent(calItem.getUid(),
                    eventStamp.getMasterEvent(), eventStamp.getCalendar());
            map.put(eventId, e.getRecurrenceRule());
        }
        return map;
    }

    public void saveRecurrenceRule(String collectionUid, String eventId,
            RecurrenceRule recurrenceRule) throws RPCException {
        doSaveRecurrenceRule(collectionUid, eventId, recurrenceRule);
    }
    
    public void saveRecurrenceRule(String collectionUid, String eventId, RecurrenceRule recurrenceRule, String ticket) throws RPCException {
        doSaveRecurrenceRule(collectionUid, eventId, recurrenceRule);
        
    }

    private void doSaveRecurrenceRule(String collectionUid, String eventId,
            RecurrenceRule recurrenceRule) throws RPCException {

        ContentItem calItem = (ContentItem) contentService.findItemByUid(eventId);
        EventStamp eventStamp = EventStamp.getStamp(calItem);
        net.fortuna.ical4j.model.Calendar calendar = eventStamp.getCalendar();

        Event event = icalendarToCosmoConverter.createEvent(eventId, eventStamp
                .getMasterEvent(), calendar);
        event.setRecurrenceRule(recurrenceRule);
        cosmoToICalendarConverter.updateEvent(event, calendar);
        cosmoToICalendarConverter.updateVTimeZones(calendar);
        eventStamp.setCalendar(calendar);
        contentService.updateContent(calItem);
    }

    public Map<String, Event[]> expandEvents(String collectionUid, String[] eventIds,
            long utcStartTime, long utcEndTime) throws RPCException{
        return doExpandEvents(collectionUid, eventIds, utcStartTime, utcEndTime);
    }
    
    public Map<String, Event[]> expandEvents(String collectionUid, String[] eventIds, long utcStartTime, long utcEndTime, String ticket) throws RPCException {
        return doExpandEvents(collectionUid, eventIds, utcStartTime, utcEndTime);
    }

    private Map<String, Event[]> doExpandEvents(String collectionUid, String[] eventIds,
            long utcStartTime, long utcEndTime) throws RPCException{

        Map<String, Event[]> map = new HashMap<String, Event[]>();
        for (String eventId : eventIds) {
            ContentItem calItem = (ContentItem) contentService.findItemByUid(eventId);
            EventStamp eventStamp = EventStamp.getStamp(calItem);

            DateTime start = new DateTime(utcStartTime);
            start.setUtc(true);

            DateTime end = new DateTime(utcEndTime);
            end.setUtc(true);

            List<ContentItem> events = new ArrayList<ContentItem>();
            events.add(calItem);
            map.put(eventId,icalendarToCosmoConverter.createEventsFromCalendars(events,
                    start, end));

        }
        return map;

    }

    public String saveNewEventBreakRecurrence(String collectionUid, 
            Event event, String originalEventId, 
            CosmoDate originalEventEndDate) throws RPCException {
        
        return doSaveNewEventBreakRecurrence(collectionUid, event, 
                originalEventId, originalEventEndDate);
    }
    
    public String saveNewEventBreakRecurrence(String collectionUid, Event event, String originalEventId, CosmoDate originalEventEndDate, String ticket) throws RPCException {
        return doSaveNewEventBreakRecurrence(collectionUid, event, originalEventId, originalEventEndDate);
    }

    private String doSaveNewEventBreakRecurrence(String collectionUid, 
            Event event, String originalEventId, 
            CosmoDate originalEventEndDate) throws RPCException {
        
        CollectionItem calendarItem = getCalendarCollectionItem(collectionUid);

        //first save the new event
        ContentItem calendarEventItem = saveNewEvent(event, calendarItem);

        //get the old event's recurrence rule
        RecurrenceRule recurrenceRule = getRecurrenceRules(collectionUid,
                new String[] { originalEventId }).get(originalEventId);

        recurrenceRule.setEndDate(originalEventEndDate);
        saveRecurrenceRule(collectionUid, originalEventId, recurrenceRule);

        return calendarEventItem.getUid();
    }

    /**
     * Given a path relative to the current user's home directory, returns the
     * absolute path
     *
     * @param relativePath
     * @return
     */
    private String getAbsolutePath(String relativePath) {
        try {
            StringBuffer s = new StringBuffer("/");
            s.append(getUser().getUsername());
            if (relativePath != null){
                if (! relativePath.startsWith("/"))
                    s.append("/");
                s.append(relativePath);
            }
            return s.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getUsername() {
        try {
            return getUser().getUsername();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private User getUser() {
        try {
            return cosmoSecurityManager.getSecurityContext().getUser();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CollectionItem getCalendarCollectionItem(String collectionUid) {
        return (CollectionItem) contentService
                .findItemByUid(collectionUid);
    }
    private Iterator<String> availableNameIterator(VEvent vevent) {
        String  baseName = new StringBuffer(vevent.getUid()
                .getValue()).append(".ics").toString();
        int index = baseName.length() - 4;
        return availableNameIterator(baseName, index);
    }

    private Iterator<String> availableNameIterator(String baseName,
            final int appendIndex) {
        final char[] chars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
        final StringBuffer baseNameBuffer = new StringBuffer(baseName);

        return new Iterator<String>() {
            int count = 0;

            public boolean hasNext() {
                return count < 10;
            }

            public String next() {
                String next = null;
                if (count == 0) {
                    next = baseNameBuffer.toString();
                } else if (count == 1) {
                    next =  baseNameBuffer.insert(appendIndex, '0').toString();
                } else if (hasNext()) {
                    baseNameBuffer.setCharAt(appendIndex, chars[count-1]);
                    next =  baseNameBuffer.toString();
                } else {
                    throw new NoSuchElementException();
                }
                count++;
                return next;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }
    private NoteItem saveNewEvent(Event event, CollectionItem calendarItem) {
        NoteItem calendarEventItem;
        //make an empty iCalendar
        net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
        calendar.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        calendarEventItem = new NoteItem();
        calendarEventItem.setDisplayName(event.getTitle());
        VEvent vevent = cosmoToICalendarConverter.createVEvent(event);
        calendar.getComponents().add(vevent);
        cosmoToICalendarConverter.updateVTimeZones(calendar);

        EventStamp eventStamp = new EventStamp();
        eventStamp.setCalendar(calendar);
        calendarEventItem.addStamp(eventStamp);

        // set NoteItem attributes
        calendarEventItem.setIcalUid(eventStamp.getIcalUid());
        calendarEventItem.setBody(event.getDescription());
    
        Iterator<String> availableNameIterator = availableNameIterator(vevent);

        User owner = getUser();
        if (owner == null){
            owner = calendarItem.getOwner();
        }
        
        calendarEventItem.setOwner(owner);

        boolean added = false;
        do {
            String name = availableNameIterator.next();
            calendarEventItem.setName(name);
            if (calendarEventItem.getDisplayName() == null)
                calendarEventItem.setDisplayName(name);
            try{
                added = true;
                calendarEventItem = (NoteItem) contentService.createContent(calendarItem,
                            calendarEventItem);
            } catch (DuplicateItemNameException dupe){
                added = false;
            }
        } while (!added && availableNameIterator.hasNext());
        return calendarEventItem;
    }

}
