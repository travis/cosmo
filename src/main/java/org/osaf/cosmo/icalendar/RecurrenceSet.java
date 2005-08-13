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
package org.osaf.cosmo.icalendar;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.ExRule;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple bean class that represents an ICalendar recurrence set,
 * which  comprises a master component and zero or more exception
 * components. All components in a recurrence set have the same uid.
 */
public class RecurrenceSet {
    private static final Log log = LogFactory.getLog(RecurrenceSet.class);

    /**
     */
    public static final Long KEY_NO_RECUR = new Long(0);

    private Component master;
    private HashMap components;
    private HashSet exceptions;
    private String uid;

    /**
     */
    public RecurrenceSet()
        throws RecurrenceException {
        components = new HashMap();
        exceptions = new HashSet();
    }

    /**
     */
    public void add(Collection components) {
        for (Iterator i=components.iterator(); i.hasNext();) {
            Component component = (Component) i.next();
            add(component);
        }
    }

    /**
     */
    public void add(Component component) {
        components.put(getComponentKey(component), component);

        RDate rdate = ICalendarUtils.getRDate(component);
        RRule rrule = ICalendarUtils.getRRule(component);
        ExDate exdate = ICalendarUtils.getExDate(component);
        ExRule exrule = ICalendarUtils.getExRule(component);
        log.debug("rdate: " + rdate);
        log.debug("rrule: " + rrule);
        log.debug("exdate: " + exdate);
        log.debug("exrule: " + exrule);

        if ((rdate == null && rrule == null &&
             exdate == null && exrule == null) || // no relevant props
            (rdate != null || rrule != null)) {   // has a recurrence prop
            log.debug("got master");
            master = component;
            uid = ICalendarUtils.getUid(master).getValue();
        }
        else {                                    // has an exception prop
            log.debug("got exception");
            exceptions.add(component);
        }
    }

    /**
     */
    public Component get(Date date) {
        return (Component) components.get(getComponentKey(date));
    }

    /**
     */
    public boolean contains(Date date) {
        return components.containsKey(getComponentKey(date));
    }

    /**
     */
    public Component getMaster()
        throws RecurrenceException {
        if (master == null) {
            throw new RecurrenceException("No master component");
        }
        return master;
    }

    /**
     */
    public Set getExceptions() {
        return exceptions;
    }

    /**
     */
    public String getUid() {
        return uid;
    }

    /**
     */
    public boolean isEmpty() {
        return components.isEmpty();
    }

    /**
     */
    public static Long getComponentKey(Component component) {
        RecurrenceId recurid = ICalendarUtils.getRecurrenceId(component);
        log.debug("recurid: " + recurid);
        if (recurid == null) {
            return KEY_NO_RECUR;
        }
        return new Long(recurid.getTime().getTime());
    }

    /**
     */
    public static Long getComponentKey(Date date) {
        return new Long(date.getTime());
    }
}
