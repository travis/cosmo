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

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.rpc.model.Calendar;
import org.osaf.cosmo.rpc.model.CosmoDate;
import org.osaf.cosmo.rpc.model.Event;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.service.HomeDirectoryService;
import org.osaf.cosmo.service.UserService;

public class RPCServiceImpl implements RPCService {
    private UserService userService = null;
    private HomeDirectoryService homeDirectoryService = null;
    private CosmoSecurityManager cosmoSecurityManager = null;
    
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
        // TODO Auto-generated method stub

    }

    public Calendar[] getCalendars() throws RPCException {
        Calendar calendar = new Calendar();
        calendar.setName("TESTNAME");
        calendar.setPath("TESTPATH");
        return new Calendar[]{calendar};
    }

    public Event getEvent(String calendarPath, String id) throws RPCException {
        Event event = new Event();
        event.setDescription("TESTEVENT");
        event.setTitle("TESTEVENT");
        
        CosmoDate start = new CosmoDate();
        start.setUtc(true);
        start.setDate(1);
        start.setMonth(8);
        start.setYear(1);
        event.setStart(start);
        event.setId("123456");
        return event;
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

}
