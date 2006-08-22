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
import java.util.List;
import java.util.Set;

import net.fortuna.ical4j.data.CalendarBuilder;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.model.CalendarCollectionResource;
import org.osaf.cosmo.model.CalendarResource;
import org.osaf.cosmo.rpc.model.Calendar;
import org.osaf.cosmo.rpc.model.CosmoDate;
import org.osaf.cosmo.rpc.model.Event;
import org.osaf.cosmo.rpc.model.ICalendarToCosmoConverter;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.service.HomeDirectoryService;
import org.osaf.cosmo.service.UserService;

public class RPCServiceImpl implements RPCService {
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
        return (Calendar[]) cals.toArray(new Calendar[cals.size()]);
    }

    public Event getEvent(String calendarPath, String id) throws RPCException {
        CalendarResource calResource = getCalendarResourceByEventId(
                getAbsolutePath(calendarPath), id);
        net.fortuna.ical4j.model.Calendar ical = calendarBuilder
                .build(calResource.getContent());
        icalendarToCosmoConverter.createEvent(vevent, calendar)
        
    }

    public Event[] getEvents(String calendarPath, long utcStartTime,
            long utcEndTime) throws RPCException {
        Event event = new Event();
        event.setDescription("TESTEVENT");
        event.setTitle("TESTEVENT");
        
        CosmoDate start = new CosmoDate();
        start.setUtc(true);
        start.setDate(1);
        start.setMonth(9);
        start.setYear(2006);
        event.setStart(start);
        event.setId("123456");
        return new Event[]{event};
    }

    public String getPreference(String preferenceName) throws RPCException {
        return "TEST_PREF";
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

    }

    public void removeEvent(String calendarPath, String id) throws RPCException {

    }

    public void removePreference(String preferenceName) throws RPCException {

    }

    public String saveEvent(String calendarPath, Event event)
            throws RPCException {
        return "123455556";
    }

    public void setPreference(String preferenceName, String value)
            throws RPCException {
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
    

}
