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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

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
import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.DuplicateItemNameException;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.rpc.model.Calendar;
import org.osaf.cosmo.rpc.model.CosmoDate;
import org.osaf.cosmo.rpc.model.CosmoToICalendarConverter;
import org.osaf.cosmo.rpc.model.Event;
import org.osaf.cosmo.rpc.model.ICalendarToCosmoConverter;
import org.osaf.cosmo.rpc.model.RecurrenceRule;
import org.osaf.cosmo.rpc.model.Subscription;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.server.ServiceLocatorFactory;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;

public class RPCServiceImpl implements RPCService, ICalendarConstants {
    private static final Log log =
        LogFactory.getLog(RPCServiceImpl.class);

    private final String[] READ_PERM = new String [] {Ticket.PRIVILEGE_READ};
    private final String[] WRITE_PERM = new String [] {Ticket.PRIVILEGE_WRITE};
    private final String[] READ_WRITE_PERM =
        new String [] {Ticket.PRIVILEGE_READ,Ticket.PRIVILEGE_WRITE};

    private UserService userService = null;
    private ContentService contentService = null;
    private CosmoSecurityManager cosmoSecurityManager = null;
    private ICalendarToCosmoConverter icalendarToCosmoConverter = new ICalendarToCosmoConverter();
    private CosmoToICalendarConverter cosmoToICalendarConverter = new CosmoToICalendarConverter();
    private ServiceLocatorFactory serviceLocatorFactory =  null;

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

    public ServiceLocatorFactory getServiceLocatorFactory() {
        return serviceLocatorFactory;
    }

    public void setServiceLocatorFactory(ServiceLocatorFactory serviceLocatorFactory) {
        this.serviceLocatorFactory = serviceLocatorFactory;
    }

    //methods for RPCService
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



    public Calendar[] getCalendars(HttpServletRequest request) throws RPCException {
        if (log.isDebugEnabled())
            log.debug("Getting calendars in home collection of " +
                      getUser().getUsername());
        User user = getUser();

        if (user == null){
            return new Calendar[]{};
        }
        // XXX no ContentService.findCalendars yet

        //first find all the calenders that the user owns
        HomeCollectionItem home = contentService.getRootItem(user);
        List<Calendar> cals = new ArrayList<Calendar>();
        for (Iterator<Item> i = home.getChildren().iterator(); i.hasNext();) {
            Item child = i.next();
            if (child.getStamp(CalendarCollectionStamp.class) != null) {
                Calendar calendar = createCalendarFromItem(request, child);
                cals.add(calendar);
            }
        }

        //TODO sort these
        return (Calendar[]) cals.toArray(new Calendar[cals.size()]);
    }

    public Calendar getCalendar(String collectionUid, String ticketKey,
            HttpServletRequest request) throws RPCException{

        CollectionItem collection = getCollectionItem(collectionUid);

        this.checkTicketProvidesPermissions(collection, ticketKey, READ_PERM);

        Calendar calendar = createCalendarFromItem(request, collection, ticketKey);
          
        return calendar;
    }

    public Calendar getCalendar(String collectionUid,
            HttpServletRequest request) throws RPCException{

        CollectionItem collection = getCollectionItem(collectionUid);

        this.checkCurrentUserOwnsCollection(collection);

        Calendar calendar = createCalendarFromItem(request, collection);

        return calendar;
    }

    public Event getEvent(String collectionUid, String eventUid)
    throws RPCException {

        CollectionItem collection =
            getCollectionItem(collectionUid);
            this.checkCurrentUserOwnsCollection(collection);

        return this.doGetEvent(collection, eventUid);
    }

    public Event getEvent(String collectionUid, String eventUid, String ticket)
        throws RPCException {

        CollectionItem collection =
            getCollectionItem(collectionUid);
            this.checkTicketProvidesPermissions(collection, ticket, READ_PERM);
        return doGetEvent(collection, eventUid);
    }

    public Event[] getEvents(String collectionUid, long utcStartTime,
            long utcEndTime) throws RPCException {

        CollectionItem collection = getCollectionItem(collectionUid);
        this.checkCurrentUserOwnsCollection(collection);


        return doGetEvents(collection, utcStartTime, utcEndTime);
    }
    
    public Event[] getEvents(String collectionUid, long utcStartTime,
            long utcEndTime, String ticket) throws RPCException {

        CollectionItem collection = getCollectionItem(collectionUid);
        this.checkTicketProvidesPermissions(collection, ticket, READ_PERM);

        return doGetEvents(collection, utcStartTime, utcEndTime);
    }

    public String getVersion() {
        return CosmoConstants.PRODUCT_VERSION;
    }

    public void removeCalendar(String collectionUid) throws RPCException {

        CollectionItem collection = (CollectionItem)
            contentService.findItemByUid(collectionUid);

        this.checkCurrentUserOwnsCollection(collection);

        if (log.isDebugEnabled())
            log.debug("Removing collection with uid " + collectionUid);

        contentService.removeItem(collection);

    }

    public void removeEvent(String collectionUid, String eventUid)
        throws RPCException {

        CollectionItem collection =
            getCollectionItem(collectionUid);
        try{
            this.checkCurrentUserOwnsCollection(collection);
        } catch (RPCException e){
            throw new RPCException("Could not remove event: " + e.getMessage());
        }

        doRemoveEvent(collection, eventUid);
    }

    public void removeEvent(String collectionUid, String eventUid, String ticket) throws RPCException {

        CollectionItem collection = getCollectionItem(collectionUid);
        this.checkTicketProvidesPermissions(collection, ticket, WRITE_PERM);

        doRemoveEvent(collection, eventUid);

    }

    public String saveEvent(String collectionUid, Event event)
        throws RPCException {

        CollectionItem collection = getCollectionItem(collectionUid);
        checkCurrentUserOwnsCollection(collection);

        return doSaveEvent(collection, event);
    }

    public String saveEvent(String collectionUid, Event event, String ticket) throws RPCException {

        CollectionItem collection = getCollectionItem(collectionUid);
        checkTicketProvidesPermissions(collection, ticket, WRITE_PERM);

        return doSaveEvent(collection, event);
    }

    public String getPreference(String key) throws RPCException {
        if (log.isDebugEnabled())
            log.debug("Getting preference " + key);
        return getUser().getPreference(key);
    }

    public void setPreference(String key, String value)
            throws RPCException {
        if (log.isDebugEnabled())
            log.debug("Setting preference " + key + " to " + value);
        User user = getUser();
        user.setPreference(key, value);
        userService.updateUser(user);
    }

    public Map<String, String> getPreferences() throws RPCException {
        HashMap<String, String> returnVal = new HashMap<String, String>();
        returnVal.putAll(getUser().getPreferences());
        return returnVal;
    }

    public void setPreferences(Map<String, String> preferences) throws RPCException {
        User user = getUser();
        user.setPreferences(preferences);
        userService.updateUser(user);
    }
    
    public void setMultiplePreferences(Map<String, String> preferences) throws RPCException {
        User user = getUser();
        user.setMultiplePreferences(preferences);
        userService.updateUser(user);
    }

    public void removePreference(String key) throws RPCException {
        if (log.isDebugEnabled())
            log.debug("Removing preference " + key);
        getUser().removePreference(key);
    }

    public Map<String, RecurrenceRule> getRecurrenceRules(String collectionUid,
            String[] eventUids) throws RPCException {

        CollectionItem collection = getCollectionItem(collectionUid);
        checkCurrentUserOwnsCollection(collection);

        return doGetRecurrenceRules(collection, eventUids);
    }
    
    public Map<String, RecurrenceRule> getRecurrenceRules(String collectionUid,
            String[] eventUids, String ticket) throws RPCException {

        CollectionItem collection = getCollectionItem(collectionUid);
        checkTicketProvidesPermissions(collection, ticket, READ_PERM);

        return doGetRecurrenceRules(collection, eventUids);
    }

    public void saveRecurrenceRule(String collectionUid, String eventUid,
            RecurrenceRule recurrenceRule) throws RPCException {

        CollectionItem collection = getCollectionItem(collectionUid);
        this.checkCurrentUserOwnsCollection(collection);

        doSaveRecurrenceRule(collection, eventUid, recurrenceRule);
    }

    public void saveRecurrenceRule(String collectionUid, String eventUid,
            RecurrenceRule recurrenceRule, String ticket) throws RPCException {

        CollectionItem collection = getCollectionItem(collectionUid);
        checkTicketProvidesPermissions(collection, ticket, WRITE_PERM);


        doSaveRecurrenceRule(collection, eventUid, recurrenceRule);

    }

    public Map<String, Event[]> expandEvents(String collectionUid,
            String[] eventUids, long utcStartTime,
            long utcEndTime) throws RPCException{

        CollectionItem collection = getCollectionItem(collectionUid);
        checkCurrentUserOwnsCollection(collection);

        return doExpandEvents(collection, eventUids, utcStartTime, utcEndTime);
    }

    public Map<String, Event[]> expandEvents(String collectionUid,
            String[] eventUids, long utcStartTime, long utcEndTime,
            String ticket) throws RPCException {

        CollectionItem collection = getCollectionItem(collectionUid);
        checkTicketProvidesPermissions(collection, ticket, READ_PERM);

        return doExpandEvents(collection, eventUids, utcStartTime, utcEndTime);
    }

    public String saveNewEventBreakRecurrence(String collectionUid,
            Event event, String originalEventUid,
            CosmoDate originalEventEndDate) throws RPCException {

        CollectionItem collection = getCollectionItem(collectionUid);
        checkCurrentUserOwnsCollection(collection);

        return doSaveNewEventBreakRecurrence(collection, event,
                originalEventUid, originalEventEndDate);
    }

    public String saveNewEventBreakRecurrence(String collectionUid, Event event,
            String originalEventUid, CosmoDate originalEventEndDate,
            String ticket) throws RPCException {

        CollectionItem collection = getCollectionItem(collectionUid);
        checkTicketProvidesPermissions(collection, ticket, READ_WRITE_PERM);

        return doSaveNewEventBreakRecurrence(collection, event,
                originalEventUid, originalEventEndDate);
    }

    public void saveSubscription(String collectionUid, String ticketKey, String displayName) throws RPCException{
        User user = getUser();
        if (user == null){
            throw new RPCException("You must be logged in to subscribe to a collection");
        }
        
        this.checkTicketProvidesPermissions(
                getCollectionItem(collectionUid), ticketKey, 
                this.READ_PERM);

        CollectionSubscription sub = null;

        //first see if that subscription exists
        sub = user.getSubscription(collectionUid, ticketKey);
        if (sub == null){
            //new subscription
            sub = new CollectionSubscription();
            sub.setCollectionUid(collectionUid);
            sub.setDisplayName(displayName);
            
            //check to see if that display name is in use
            //and if so get a new one
            Set<String> displayNames = getSubscriptionDisplayNames(user);
            int x = 1;
            while (displayNames.contains(sub.getDisplayName())){
                sub.setDisplayName(displayName + " (" + x + ")");
                x++;
            }
            
            sub.setOwner(user);
            sub.setTicketKey(ticketKey);
            user.addSubscription(sub);
        } else {
            //updating old subscription
            sub.setDisplayName(displayName);
        }

        userService.updateUser(user);
    }

    private Set<String> getSubscriptionDisplayNames(User user) {
        Set<CollectionSubscription> collectionSubscriptions = user
                .getCollectionSubscriptions();
        Set<String> displayNames = new HashSet<String>();
        for (CollectionSubscription sub : collectionSubscriptions){
            displayNames.add(sub.getDisplayName());
        }
        return displayNames;
    }

    public void deleteSubscription(String collectionUid, String ticketKey) throws RPCException{
        User user = getUser();
        if (user == null){
            throw new RPCException("You must be logged in to subscribe to a collection");
        }

        user.removeSubscription(collectionUid, ticketKey);
        userService.updateUser(user);
    }

    public Subscription[] getSubscriptions(HttpServletRequest request)
            throws RPCException {

        User user = getUser();
        if (user == null) {
            throw new RPCException(
                    "You must be logged in to get subscriptions");
        }

        List<Subscription> subscriptions = new ArrayList<Subscription>();
        Set<CollectionSubscription> collectionSubscriptions = user
                .getCollectionSubscriptions();
        for (CollectionSubscription colSub : collectionSubscriptions) {
            Subscription sub = createSubscription(colSub, request);
            subscriptions.add(sub);
        }
        return (Subscription[]) subscriptions
                .toArray(new Subscription[subscriptions.size()]);
    }

    public org.osaf.cosmo.rpc.model.Ticket getTicket(String ticketId,
            String collectionId) throws RPCException {
        org.osaf.cosmo.rpc.model.Ticket rpcTicket = new org.osaf.cosmo.rpc.model.Ticket();
        Ticket ticket = contentService.getTicket(collectionId, ticketId);
        rpcTicket.setPrivileges(new HashSet<String>(ticket.getPrivileges()));
        rpcTicket.setTicketKey(ticketId);
        return rpcTicket;
    }

    public Subscription getSubscription(String collectionUid, String ticketKey,
            HttpServletRequest request) throws RPCException {
        User user = getUser();
        if (user == null) {
            throw new RPCException(
                    "You must be logged in to subscribe to a collection");
        }
        
        checkTicketProvidesPermissions(
                getCollectionItem(collectionUid), ticketKey, READ_PERM);
        
        CollectionSubscription colsub = user.getSubscription(collectionUid, ticketKey);
        return createSubscription(colsub, request);
    }

    public void saveDisplayName(String collectionUid, String displayName)
            throws RPCException {
        User user = getUser();
        if (user == null) {
            throw new RPCException(
                    "You must be logged in to create a collection");
        }

        CollectionItem collection = getCollectionItem(collectionUid);

        this.checkCurrentUserOwnsCollection(collection);
        
        collection.setDisplayName(displayName);
        contentService.updateCollection(collection);
    }
    
    private Subscription createSubscription(
            CollectionSubscription collectionSubscription,
            HttpServletRequest request) throws RPCException {
        Subscription subscription = new Subscription();

        try {
            Calendar calendar = getCalendar(collectionSubscription
                    .getCollectionUid(), collectionSubscription.getTicketKey(),
                    request);
            subscription.setCalendar(calendar);
            org.osaf.cosmo.rpc.model.Ticket ticket = getTicket(collectionSubscription.getTicketKey(),
                    collectionSubscription.getCollectionUid());
            subscription.setTicket(ticket);
        } catch (ItemNotFoundException e){
            subscription.setCalendar(null);
            org.osaf.cosmo.rpc.model.Ticket ticket = new org.osaf.cosmo.rpc.model.Ticket();
            ticket.setTicketKey(collectionSubscription.getTicketKey());
            subscription.setTicket(ticket);
        }

        subscription.setDisplayName(collectionSubscription.getDisplayName());
        subscription.setUid(collectionSubscription.getCollectionUid());
        return subscription;
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
            User user = cosmoSecurityManager.getSecurityContext().getUser();
            if (user == null){
                return null;
            }
            String name = user.getUsername();
            return userService.getUser(name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CollectionItem getCollectionItem(String collectionUid)
        throws RPCException {
        CollectionItem collection = (CollectionItem) contentService
                .findItemByUid(collectionUid);
        if (collection == null){
            throw new ItemNotFoundException(collectionUid);
        }
        return collection;
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

    private void checkCurrentUserOwnsCollection(CollectionItem collection)
        throws RPCException{

        User user = getUser();
        if (user == null){
            throw new RPCException("No user logged in.");
        }
        
        if (!collection.getOwner().getUid().equals(user.getUid())) {
            throw new RPCException("You do not own the collection with uid "
                    + collection.getUid());
        }

    }

    private void checkTicketProvidesPermissions(CollectionItem collection,
            String ticketKey, String[] permissions) throws RPCException{

        Ticket ticket = null;

        for (Ticket thisTicket: collection.getTickets()){
            if (ticketKey.equals(thisTicket.getKey())){
                ticket = thisTicket;
                break;
            }
        }

        if (ticket == null){
            throw new RPCException("Ticket " + ticketKey +
                    " not found on collection with uid " +
                    collection.getUid());
        }

        if (ticket.getPrivileges().contains(Ticket.PRIVILEGE_FREEBUSY)
                && !ticket.getPrivileges().contains(Ticket.PRIVILEGE_READ)
                && !ticket.getPrivileges().contains(Ticket.PRIVILEGE_WRITE)){
            throw new RPCException(
                    "Support for freebusy tickets has not been implemented.");
        }

        if (ticket.hasTimedOut()){
            throw new RPCException("Ticket " + ticketKey +
                    " has expired.");
        }

        for (String permission: permissions){
            if (!ticket.getPrivileges().contains(permission)){
                throw new RPCException(
                        "You do not have " + permission + " permissions on " +
                        "the collection with uid " + collection.getUid());
            }
        }
    }

    private NoteItem saveNewEvent(Event event, CollectionItem calendarItem) {
        NoteItem calendarEventItem;
        //make an empty iCalendar
        net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
        calendar.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        User owner = getUser();
        if (owner == null){
            owner = calendarItem.getOwner();
        }

        calendarEventItem = new NoteItem();
        calendarEventItem.setOwner(owner);
        calendarEventItem.setDisplayName(event.getTitle());
        calendarEventItem.
            setClientCreationDate(java.util.Calendar.getInstance().getTime());
        calendarEventItem.
            setClientModifiedDate(calendarEventItem.getClientCreationDate());
        calendarEventItem.setContentType(ICALENDAR_MEDIA_TYPE);
        calendarEventItem.setContentEncoding("UTF-8");
        calendarEventItem.
            setLastModifiedBy(getUser() != null ? getUser().getEmail() : "");
        calendarEventItem.setTriageStatus(TriageStatus.createInitialized());
        calendarEventItem.setLastModification(ContentItem.Action.CREATED);
        calendarEventItem.setSent(Boolean.FALSE);
        calendarEventItem.setNeedsReply(Boolean.FALSE);
        // content length and content data are handled internally by
        // setting the calendar on EventStamp

        VEvent vevent = cosmoToICalendarConverter.createVEvent(event);
        calendar.getComponents().add(vevent);
        cosmoToICalendarConverter.updateVTimeZones(calendar);

        EventStamp eventStamp = new EventStamp();
        eventStamp.setCalendar(calendar);
        calendarEventItem.addStamp(eventStamp);

        Iterator<String> availableNameIterator = availableNameIterator(vevent);

        boolean added = false;
        do {
            String name = availableNameIterator.next();
            calendarEventItem.setName(name);
            if (calendarEventItem.getDisplayName() == null)
                calendarEventItem.setDisplayName(name);
            try{
                added = true;
                calendarEventItem = (NoteItem) contentService.createEvent(calendarItem, calendarEventItem, calendar);
            } catch (DuplicateItemNameException dupe){
                added = false;
            }
        } while (!added && availableNameIterator.hasNext());
        return calendarEventItem;
    }

    private Calendar createCalendarFromItem(HttpServletRequest request, Item item) {
        ServiceLocator serviceLocator = serviceLocatorFactory.createServiceLocator(request);

        return createCalendarFromItem(item, serviceLocator);
    }

    private Calendar createCalendarFromItem(HttpServletRequest request, Item item, String ticketKey){
        ServiceLocator serviceLocator = serviceLocatorFactory
                .createServiceLocator(request, ticketKey);

        return createCalendarFromItem(item, serviceLocator);
    }

    private Calendar createCalendarFromItem(Item item, ServiceLocator serviceLocator) {
        Calendar calendar = new Calendar();
        calendar.setName(item.getDisplayName());
        calendar.setUid(item.getUid());

        Map<String, String> protocolUrls = serviceLocator.getCollectionUrls((CollectionItem) item);
        calendar.setProtocolUrls(protocolUrls);
        return calendar;
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
    
    private String doSaveNewEventBreakRecurrence(CollectionItem collection,
            Event event, String originalEventUid,
            CosmoDate originalEventEndDate) throws RPCException {

        //first save the new event
        event.setId(null);
        ContentItem calendarEventItem = saveNewEvent(event, collection);

        //get the old event's recurrence rule
        RecurrenceRule recurrenceRule = doGetRecurrenceRules(collection,
                new String[] { originalEventUid }).get(originalEventUid);

        recurrenceRule.setEndDate(originalEventEndDate);
        doSaveRecurrenceRule(collection, originalEventUid, recurrenceRule);

        return calendarEventItem.getUid();
    }
    private Event doGetEvent(CollectionItem collection, String eventUid) throws RPCException{

        if (log.isDebugEnabled())
            log.debug("Getting event " + eventUid + " in calendar with uid " +
                      collection.getUid());
        ContentItem calItem = (ContentItem) contentService.findItemByUid(eventUid);
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
                    + " calendarUid: " + collection.getUid()
                    + " id: " +eventUid, e);
            throw new RPCException("Problem getting event", e);
        }
    }
    
    private Event[] doGetEvents(CollectionItem collection, long utcStartTime,
            long utcEndTime) throws RPCException {
        DateTime start = new DateTime(utcStartTime);
        start.setUtc(true);

        DateTime end = new DateTime(utcEndTime);
        end.setUtc(true);

        ComponentFilter eventFilter = new ComponentFilter(Component.VEVENT);
        eventFilter.setTimeRangeFilter(new TimeRangeFilter(start, end));

        ComponentFilter calFilter = new ComponentFilter(
                net.fortuna.ical4j.model.Calendar.VCALENDAR);
        calFilter.getComponentFilters().add(eventFilter);

        CalendarFilter filter = new CalendarFilter();
        filter.setFilter(calFilter);

        if (log.isDebugEnabled())
            log.debug("Getting events between " + start + " and " + end
                    + " in calendar with uid " + collection.getUid());

        // XXX: need ContentService.findEvents(path, filter)
        if (collection == null)
            throw new RPCException("Collection with uid " + collection.getUid()
                    + " does not exist");

        Set<ContentItem> calendarItems = null;
        try {
            calendarItems = contentService.findEvents(collection, filter);
        } catch (Exception e) {
            log.error("cannot find events for calendar with uid "
                    + collection.getUid(), e);
            throw new RPCException("Cannot find events for calendar with uid "
                    + collection.getUid() + ": " + e.getMessage(), e);
        }

        Event[] events = null;
        try {
            DateTime beginDate = new DateTime();
            beginDate.setUtc(true);
            beginDate.setTime(utcStartTime);
            DateTime endDate = new DateTime();
            endDate.setUtc(true);
            endDate.setTime(utcEndTime);

            events = icalendarToCosmoConverter.createEventsFromCalendars(
                    calendarItems, beginDate, endDate);
        } catch (Exception e) {
            log.error("Problem getting events: userName: " + getUsername()
                    + " calendarUid: " + collection.getUid() + " beginDate: "
                    + utcStartTime + " endDate: " + utcStartTime, e);
            throw new RPCException("Problem getting events", e);
        }
        return events;
    }
    private Map<String, Event[]> doExpandEvents(CollectionItem collection,
            String[] eventUids, long utcStartTime, long utcEndTime)
            throws RPCException {

        Map<String, Event[]> map = new HashMap<String, Event[]>();
        for (String eventUid : eventUids) {
            ContentItem calItem = (ContentItem) contentService
                    .findItemByUid(eventUid);
            EventStamp eventStamp = EventStamp.getStamp(calItem);

            DateTime start = new DateTime(utcStartTime);
            start.setUtc(true);

            DateTime end = new DateTime(utcEndTime);
            end.setUtc(true);

            List<ContentItem> events = new ArrayList<ContentItem>();
            events.add(calItem);
            map.put(eventUid, icalendarToCosmoConverter
                    .createEventsFromCalendars(events, start, end));

        }
        return map;

    }
    private void doRemoveEvent(CollectionItem collection, String eventUid)
            throws RPCException {

        if (log.isDebugEnabled())
            log.debug("Removing event " + eventUid + " from calendar with uid "
                    + collection.getUid());
        ContentItem calItem = (ContentItem) contentService
                .findItemByUid(eventUid);
        contentService.removeContent(calItem);
    }

    private String doSaveEvent(CollectionItem collection, Event event)
            throws RPCException {

        NoteItem calendarEventItem = null;

        // Check to see if this is a new event
        if (StringUtils.isEmpty(event.getId())) {
            calendarEventItem = saveNewEvent(event, collection);

        } else {
            calendarEventItem = (NoteItem) contentService
                    .findItemByUid(event.getId());
            calendarEventItem.setDisplayName(event.getTitle());

            User user = cosmoSecurityManager.getSecurityContext().getUser();
            calendarEventItem.
                setLastModifiedBy(user != null ? user.getEmail() : "");
            calendarEventItem.setLastModification(ContentItem.Action.EDITED);

            EventStamp eventStamp = EventStamp.getStamp(calendarEventItem);
            net.fortuna.ical4j.model.Calendar calendar = eventStamp
                    .getCalendar();
            cosmoToICalendarConverter.updateEvent(event, calendar);
            cosmoToICalendarConverter.updateVTimeZones(calendar);
            
            // update NoteItem attributes
            calendarEventItem.setIcalUid(eventStamp
                    .getIcalUid());
            calendarEventItem.setBody(event.getDescription());
            
            contentService.updateEvent(calendarEventItem, calendar);
        }

        return calendarEventItem.getUid();
    }

    private void doSaveRecurrenceRule(CollectionItem collection, String eventUid,
            RecurrenceRule recurrenceRule) throws RPCException {

        NoteItem calItem = (NoteItem) contentService.findItemByUid(eventUid);

        User user = cosmoSecurityManager.getSecurityContext().getUser();
        calItem.setLastModifiedBy(user != null ? user.getEmail() : "");
        calItem.setLastModification(ContentItem.Action.EDITED);

        EventStamp eventStamp = EventStamp.getStamp(calItem);
        net.fortuna.ical4j.model.Calendar calendar = eventStamp.getCalendar();

        Event event = icalendarToCosmoConverter.createEvent(eventUid, eventStamp
                .getMasterEvent(), calendar);
        event.setRecurrenceRule(recurrenceRule);
        cosmoToICalendarConverter.updateEvent(event, calendar);
        cosmoToICalendarConverter.updateVTimeZones(calendar);
        contentService.updateEvent(calItem, calendar);
    }
    private Map<String, RecurrenceRule> doGetRecurrenceRules(CollectionItem collection,
            String[] eventUids) throws RPCException {

        Map<String, RecurrenceRule> map = new HashMap<String, RecurrenceRule>();
        for (int x = 0; x < eventUids.length; x++) {
            String eventId = eventUids[x];
            ContentItem calItem = (ContentItem) contentService.findItemByUid(eventId);
            EventStamp eventStamp = EventStamp.getStamp(calItem);
            Event e = icalendarToCosmoConverter.createEvent(calItem.getUid(),
                    eventStamp.getMasterEvent(), eventStamp.getCalendar());
            map.put(eventId, e.getRecurrenceRule());
        }
        return map;
    }
}
