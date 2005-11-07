/*
 * Copyright 2005 Open Source Applications Foundation
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
package org.osaf.cosmo;

import java.security.Principal;
import java.util.HashSet;
import java.util.TimeZone;

import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.*;
import net.fortuna.ical4j.model.parameter.*;
import net.fortuna.ical4j.model.property.*;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.security.mock.MockAnonymousPrincipal;
import org.osaf.cosmo.security.mock.MockUserPrincipal;

/**
 */
public class TestHelper {
    static int apseq = 0;
    static int eseq = 0;
    static int rseq = 0;
    static int tseq = 0;
    static int useq = 0;

    public Calendar makeDummyCalendar() {
        Calendar cal =new Calendar();

        cal.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        cal.getProperties().add(Version.VERSION_2_0);

        return cal;
    }

    public Calendar makeDummyCalendarWithEvent() {
        Calendar cal = makeDummyCalendar();

        VEvent e1 = makeDummyEvent();
        cal.getComponents().add(e1);

        VTimeZone tz1 = TimeZoneRegistryFactory.getInstance().createRegistry().
            getTimeZone(TimeZone.getDefault().getID()).getVTimeZone();
        cal.getComponents().add(tz1);

        return cal;
    }

    public VEvent makeDummyEvent() {
        String serial = new Integer(++eseq).toString();
        String summary = "dummy" + serial;

        // tomorrow
        java.util.Calendar start = java.util.Calendar.getInstance();
        start.add(java.util.Calendar.DAY_OF_MONTH, 1);
        start.set(java.util.Calendar.HOUR_OF_DAY, 9);
        start.set(java.util.Calendar.MINUTE, 30);

        // 1 hour duration
        Dur duration = new Dur(0, 1, 0, 0);
 
        VEvent event = new VEvent(new Date(start.getTime()), duration, summary);
        event.getProperties().add(new Uid(serial));
 
        // add timezone information
        VTimeZone tz = TimeZoneRegistryFactory.getInstance().createRegistry().
            getTimeZone(TimeZone.getDefault().getID()).getVTimeZone();
        String tzValue =
            tz.getProperties().getProperty(Property.TZID).getValue();
        net.fortuna.ical4j.model.parameter.TzId tzParam =
            new net.fortuna.ical4j.model.parameter.TzId(tzValue);
        event.getProperties().getProperty(Property.DTSTART).
            getParameters().add(tzParam);

        // add an alarm for 5 minutes before the event with an xparam
        // on the description
        Dur trigger = new Dur(0, 0, -5, 0);
        VAlarm alarm = new VAlarm(trigger);
        alarm.getProperties().add(Action.DISPLAY);
        Description description = new Description("Meeting at 9:30am");
        XParameter xparam = new XParameter("X-COSMO-TEST-PARAM", "deadbeef");
        description.getParameters().add(xparam);
        alarm.getProperties().add(description);
        alarm.getProperties().add(new Description("Meeting at 9:30am"));
        event.getAlarms().add(alarm);

        // add an x-property with an x-param
        XProperty xprop = new XProperty("X-COSMO-TEST-PROP", "abc123");
        xprop.getParameters().add(xparam);
        event.getProperties().add(xprop);

        return event;
    }

    /**
     */
    public Ticket makeDummyTicket() {
        Ticket ticket = new Ticket();
        ticket.setTimeout(CosmoDavConstants.VALUE_INFINITE);
        ticket.setPrivileges(new HashSet());
        ticket.getPrivileges().add(CosmoDavConstants.PRIVILEGE_READ);
        return ticket;
    }

    /**
     */
    public Ticket makeDummyTicket(User user) {
        Ticket ticket = makeDummyTicket();
        ticket.setOwner(user.getUsername());
        ticket.setId(new Integer(++tseq).toString());
        return ticket;
    }

    /**
     */
    public User makeDummyUser(String username,
                              String password) {
        if (username == null) {
            throw new IllegalArgumentException("username required");
        }
        if (password == null) {
            throw new IllegalArgumentException("password required");
        }

        User user = new User();
        user.setUsername(username);
        user.setFirstName(username);
        user.setLastName(username);
        user.setEmail(username + "@localhost");
        user.setPassword(password);

        return user;
    }

    /**
     */
    public User makeDummyUser() {
        String serial = new Integer(++useq).toString();
        String username = "dummy" + serial;
        return makeDummyUser(username, username);
    }

    /**
     */
    public Principal makeDummyUserPrincipal() {
        return new MockUserPrincipal(makeDummyUser());
    }

    /**
     */
    public Principal makeDummyUserPrincipal(String name,
                                            String password) {
        return new MockUserPrincipal(makeDummyUser(name, password));
    }

    /**
     */
    public Principal makeDummyAnonymousPrincipal() {
        String serial = new Integer(++apseq).toString();
        return new MockAnonymousPrincipal("dummy" + serial);
    }

    /**
     */
    public Principal makeDummyRootPrincipal() {
        User user = makeDummyUser();
        user.setAdmin(Boolean.TRUE);
        return new MockUserPrincipal(user);
    }
}
