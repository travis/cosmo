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

import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.XProperty;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.model.Role;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;

/**
 */
public class TestHelper {
    static int apseq = 0;
    static int rseq = 0;
    static int useq = 0;

    private TestHelper() {
    }

    public static VEvent makeDummyEvent(String summary) {
        // tomorrow
        java.util.Calendar start = java.util.Calendar.getInstance();
        start.add(java.util.Calendar.DAY_OF_MONTH, 1);
        start.set(java.util.Calendar.HOUR_OF_DAY, 9);
        start.set(java.util.Calendar.MINUTE, 30);

        int duration = 1000 * 60 * 60;
 
        VEvent event = new VEvent(start.getTime(), duration, summary);
 
        // add timezone information..
        VTimeZone tz = VTimeZone.getDefault();
        TzId tzParam =
            new TzId(tz.getProperties().getProperty(Property.TZID).getValue());
        event.getProperties().getProperty(Property.DTSTART).
            getParameters().add(tzParam);

        // add random x-property
        event.getProperties().add(new XProperty("X-Cosmo-Test", "abc123"));

        return event;
    }

    /**
     */
    public static Role makeDummyRole() {
        String serial = new Integer(++rseq).toString();

        Role role = new Role();
        role.setName("dummy" + serial);

        return role;
    }

    /**
     */
    public static Ticket makeDummyTicket() {
        Ticket ticket = new Ticket();
        ticket.setTimeout(CosmoDavConstants.VALUE_INFINITY);
        ticket.setPrivileges(new HashSet());
        ticket.getPrivileges().add(CosmoDavConstants.PRIVILEGE_READ);
        return ticket;
    }

    /**
     */
    public static User makeDummyUser(String username,
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
    public static User makeDummyUser() {
        String serial = new Integer(++useq).toString();
        String username = "dummy" + serial;
        return makeDummyUser(username, username);
    }

    /**
     */
    public static Principal makeDummyUserPrincipal() {
        return new TestUserPrincipal(makeDummyUser());
    }

    /**
     */
    public static Principal makeDummyUserPrincipal(String name,
                                                   String password) {
        return new TestUserPrincipal(makeDummyUser(name, password));
    }

    /**
     */
    public static Principal makeDummyAnonymousPrincipal() {
        String serial = new Integer(++apseq).toString();
        return new TestAnonymousPrincipal("dummy" + serial);
    }

    /**
     */
    public static Principal makeDummyRootPrincipal() {
        User user = makeDummyUser();
        Role role = new Role();
        role.setName(CosmoSecurityManager.ROLE_ROOT);
        user.addRole(role);
        return new TestUserPrincipal(user);
    }
}
