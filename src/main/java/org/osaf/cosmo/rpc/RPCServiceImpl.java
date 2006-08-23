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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.model.CalendarCollectionResource;
import org.osaf.cosmo.model.CalendarResource;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.rpc.model.Calendar;
import org.osaf.cosmo.rpc.model.Event;
import org.osaf.cosmo.rpc.model.ICalendarToCosmoConverter;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.service.HomeDirectoryService;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.util.ICalendarUtils;

public class RPCServiceImpl implements RPCService {
    private static final Log log =
        LogFactory.getLog(RPCServiceImpl.class);

    private UserService userService = null;
    private HomeDirectoryService homeDirectoryService = null;
    private CosmoSecurityManager cosmoSecurityManager = null;
    private CalendarBuilder calendarBuilder = new CalendarBuilder();
    private ICalendarToCosmoConverter icalendarToCosmoConverter = new ICalendarToCosmoConverter();
    
    public CosmoSecurityManager getCosmoSecurityManager() {
        return cosmoSecurityManager;
    }

    public void setCosmoSecurityManager(CosmoSecurityManager cosmoSecurityManager) {
        this.cosmoSecurityManager = cosmoSecurityManager;
    }

    public HomeDirectoryService getHomeDirectoryService() {
        return homeDirectoryService;
    }

    public void setHomeDirectoryService(HomeDirectoryService homeDirectoryService) {
        this.homeDirectoryService = homeDirectoryService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void createCalendar(String displayName, String path)
            throws RPCException {
        homeDirectoryService.createCalendarCollection(displayName, getAbsolutePath(path));
    }

    public Calendar[] getCalendars() throws RPCException {
        Set<CalendarCollectionResource> cols =
            homeDirectoryService.getCalendarCollectionResources(getAbsolutePath(null), true);
        List<Calendar> cals = new ArrayList<Calendar>();
        for (CalendarCollectionResource c : cols){
            Calendar calendar = new Calendar();
            calendar.setName(c.getDisplayName());
            calendar.setPath(c.getPath());
            cals.add(calendar);
        }
        //TODO sort these
        return (Calendar[]) cals.toArray(new Calendar[cals.size()]);
    }

    public Event getEvent(String calendarPath, String id) throws RPCException {
        CalendarResource calResource = homeDirectoryService.getCalendarResourceByEventId(
                getAbsolutePath(calendarPath), id);
        try {
            net.fortuna.ical4j.model.Calendar ical = extractCalendar(calResource);
            VEvent vevent = ICalendarUtils.getMasterEvent(ical, id);
            return icalendarToCosmoConverter.createEvent(vevent, ical);
        } catch (Exception e) {
            log.error("Problem getting event: userName: " + getUsername() 
                    + " calendarName: " + calendarPath 
                    + " id: " +id, e);
            throw new RPCException("Problem getting event", e);
        }   
    }

    public Event[] getEvents(String calendarPath, long utcStartTime,
            long utcEndTime) throws RPCException {
        
        
        Set<CalendarResource> calendarResources = homeDirectoryService
                .getCalendarResourcesInDateRange(getAbsolutePath(calendarPath),
                        utcStartTime, utcStartTime, null);
        Event[] events = null;
        try {
        Set<net.fortuna.ical4j.model.Calendar> calendars 
            = createCalendarSetFromCalendarResourceSet(calendarResources);
        DateTime beginDate = new DateTime();
        beginDate.setUtc(true);
        beginDate.setTime(utcStartTime);
        DateTime endDate = new DateTime();
        endDate.setUtc(true);
        endDate.setTime(utcEndTime);

        events = icalendarToCosmoConverter.createEventsFromCalendars(calendars,
                beginDate, endDate);
        } catch (Exception e) {
            log.error("Problem getting events: userName: " + getUsername() 
                    + " calendarName: " + calendarPath 
                    + " beginDate: " + utcStartTime
                    + " endDate: " + utcStartTime, e);
            throw new RPCException("Problem getting events", e);
        }
        return events;
    }

    public String getPreference(String preferenceName) throws RPCException {
       return userService.getPreference(getUsername(), preferenceName);
    }

    public String getTestString() {
        return "Scooby";
    }

    public String getVersion() {
        return CosmoConstants.PRODUCT_VERSION;
    }

    public void moveEvent(String sourceCalendar, String id,
            String destinationCalendar) throws RPCException {
        //TODO Not implemented yet
    }

    public void removeCalendar(String calendarPath) throws RPCException {
        String absolutePath = getAbsolutePath(calendarPath);
        homeDirectoryService.removeResource(absolutePath);
    }

    public void removeEvent(String calendarPath, String id) throws RPCException {
        CalendarResource calendarResource = homeDirectoryService
                .getCalendarResourceByEventId(getAbsolutePath(calendarPath), id);
        homeDirectoryService.removeResource(calendarResource.getPath());
    }

    public void removePreference(String preferenceName) throws RPCException {
        userService.removePreference(getUsername(), preferenceName);
    }

    public String saveEvent(String calendarPath, Event event)
            throws RPCException {
        return "!@#%$";
    }

    public void setPreference(String preferenceName, String value)
            throws RPCException {
        userService.setPreference(getUsername(),preferenceName);
    }
    
    /**
     * Given a path relative to the current user's home directory, returns the 
     * absolute path
     * @param relativePath
     * @return
     */
    private String getAbsolutePath(String relativePath) {
        try {
            StringBuffer s = new StringBuffer('/');
            s.append(cosmoSecurityManager.getSecurityContext()
                                    .getUser().getUsername());
            s.append('/');
            if (s != null){
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
    
    private net.fortuna.ical4j.model.Calendar extractCalendar(
            CalendarResource calendarResource) throws IOException,
            ParserException {
        net.fortuna.ical4j.model.Calendar ical = calendarBuilder
                .build(calendarResource.getContent());

        return ical;
    }
    
    private Set<net.fortuna.ical4j.model.Calendar> createCalendarSetFromCalendarResourceSet(
            Set<CalendarResource> calendarResources) throws IOException, ParserException {
        Set<net.fortuna.ical4j.model.Calendar> calendars = new HashSet<net.fortuna.ical4j.model.Calendar>();
        for (CalendarResource calendarResource : calendarResources){
            calendars.add(extractCalendar(calendarResource));
        }
        return calendars;
    }

}
