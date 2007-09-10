/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.calendar.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VJournal;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.Range;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.ExRule;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;

import org.osaf.cosmo.calendar.ICalendarUtils;
import org.osaf.cosmo.calendar.Instance;
import org.osaf.cosmo.calendar.InstanceList;

/**
 * This is a filter object that allows filtering a {@link Calendar} by
 * component type or property. It follows the model defined by the
 * CalDAV <calendar-data> XML element including support of the
 * no-value attribute. When no-value is used, a property is written
 * out up to the ':' in the stream, and then no more (i.e. the value
 * is skipped).
 *
 * Heavily based on code written by Cyrus Daboo.
 */
public class OutputFilter {

    private String componentName;
    private boolean allSubComponents;
    private HashMap subComponents;
    private boolean allProperties;
    private HashMap properties;
    private Period expand;
    private Period limit;
    private Period limitfb;

    public OutputFilter(String name) {
        componentName = name;
    }

    public void filter(Calendar calendar,
                       StringBuffer buffer) {
        // If expansion of recurrence is required what we have to do
        // is create a whole new calendar object with the new expanded
        // components in it and then write that one out.
        if (getExpand() != null)
            calendar = createExpanded(calendar);

        // If limit of recurrence set is required, we have to remove those
        // overriden components in recurring components that do not
        // overlap the given time period.  Create a new calendar in order
        // to preserve the original.
        else if (getLimit() != null)
            calendar = createLimitedRecurrence(calendar);

        buffer.append(Calendar.BEGIN).
            append(':').
            append(Calendar.VCALENDAR).
            append("\n");

        filterProperties(calendar.getProperties(), buffer);
        filterSubComponents(calendar.getComponents(), buffer);

        buffer.append(Calendar.END).
            append(':').
            append(Calendar.VCALENDAR).
            append("\n");
    }

    private Calendar createLimitedRecurrence(Calendar calendar) {
        // Create a new calendar with the same top-level properties as current
        Calendar newCal = new Calendar();
        newCal.getProperties().addAll(calendar.getProperties());
       
        InstanceList instances = new InstanceList();
        ComponentList overrides = new ComponentList();
        
        // Limit range
        Period period = getLimit();
        
        // Filter override components based on limit range
        for (Component comp : (List<Component>) calendar.getComponents()) {
            // Only care about VEVENT, VJOURNAL, VTODO
            if ((comp instanceof VEvent) ||
                (comp instanceof VJournal) ||
                (comp instanceof VToDo)) {
                // Add master component to result Calendar
                if (comp.getProperties().
                    getProperty(Property.RECURRENCE_ID) == null) {
                    newCal.getComponents().add(comp);
                    // seed the InstanceList with master component
                    instances.addComponent(comp, period.getStart(),
                                           period.getEnd());
                }
                // Keep track of overrides, we'll process later
                else
                    overrides.add(comp);
            } else {
                newCal.getComponents().add(comp);
            }
        }
        
        // Add override components to InstanceList.
        // Only add override if it changes anything about the InstanceList.
        for (Component comp : (List<Component>) overrides) {
            if (instances.addOverride(comp, period.getStart(),
                    period.getEnd()))
                newCal.getComponents().add(comp);
        }
        
        return newCal;
    }
    
    private Calendar createExpanded(Calendar calendar) {
        // Create a new calendar with the same top-level properties as this one
        Calendar newCal = new Calendar();
        newCal.getProperties().addAll(calendar.getProperties());

        // Now look at each component and determine whether expansion is
        // required
        InstanceList instances = new InstanceList();
        ComponentList overrides = new ComponentList();
        Component master = null;
        for (Component comp : (List<Component>) calendar.getComponents()) {
            if ((comp instanceof VEvent) ||
                (comp instanceof VJournal) ||
                (comp instanceof VToDo)) {
                // See if this is the master instance
                if (comp.getProperties().
                    getProperty(Property.RECURRENCE_ID) == null) {
                    master = comp;
                    instances.addComponent(comp, getExpand().getStart(),
                                           getExpand().getEnd());
                } else
                    overrides.add(comp);
            } else if (comp instanceof VTimeZone) {
                // Ignore VTIMEZONEs as we convert all date-time properties to
                // UTC
            } else {
                // Create new component and convert properties to UTC
                try {
                    Component newcomp = comp.copy();
                    componentToUTC(newcomp);
                    newCal.getComponents().add(newcomp);
                } catch (Exception e) {
                    throw new RuntimeException("Error copying component", e);
                }
            }
        }

        for (Component comp : (List<Component>) overrides)
            instances.addComponent(comp, getExpand().getStart(),
                    getExpand().getEnd());

        // Create a copy of the master with recurrence properties removed
        boolean isRecurring = false;
        Component masterCopy = null;
        try {
            masterCopy = master.copy();
        } catch (Exception e) {
            throw new RuntimeException("Error copying master component", e);
        }
        Iterator<Property> i = (Iterator<Property>)
            masterCopy.getProperties().iterator();
        while (i.hasNext()) {
            Property prop = i.next();
            if ((prop instanceof RRule) ||
                (prop instanceof RDate) ||
                (prop instanceof ExRule) ||
                (prop instanceof ExDate)) {
                i.remove();
                isRecurring = true;
            }
        }

        // Expand each instance within the requested range
        TreeSet<String> sortedKeys = new TreeSet<String>(instances.keySet());
        for (String ikey : sortedKeys) {
            Instance instance = (Instance) instances.get(ikey);

            // Make sure this instance is within the requested range
            // FIXME: Need to handle floating date/times.  Right now
            // floating times will use the server timezone.
            if ((getExpand().getStart().compareTo(instance.getEnd()) >= 0) ||
                (getExpand().getEnd().compareTo(instance.getStart()) <= 0))
                continue;
            
            // Create appropriate copy
            Component copy = null;
            try {
                copy = instance.getComp() == master ?
                    masterCopy.copy() :
                    instance.getComp().copy();
                componentToUTC(copy);
            } catch (Exception e) {
                throw new RuntimeException("Error copying component", e);
            }

            // Adjust the copy to match the actual instance info
            if (isRecurring) {
                // Add RECURRENCE-ID, replacing existing if present
                RecurrenceId rid = (RecurrenceId) copy.getProperties()
                    .getProperty(Property.RECURRENCE_ID);
                if (rid != null)
                    copy.getProperties().remove(rid);
                rid = new RecurrenceId(instance.getRid());
                copy.getProperties().add(rid);

                // Adjust DTSTART (in UTC)
                DtStart olddtstart = (DtStart)
                    copy.getProperties().getProperty(Property.DTSTART);
                if (olddtstart != null)
                    copy.getProperties().remove(olddtstart);
                DtStart newdtstart = new DtStart(instance.getStart());
                if ((newdtstart.getDate() instanceof DateTime) &&
                    (((DateTime)newdtstart.getDate()).getTimeZone() != null)) {
                    newdtstart.setUtc(true);
                }
                copy.getProperties().add(newdtstart);

                // If DTEND present, replace it (in UTC)
                DtEnd olddtend = (DtEnd)
                    copy.getProperties().getProperty(Property.DTEND);
                if (olddtend != null) {
                    copy.getProperties().remove(olddtend);
                    DtEnd newdtend = new DtEnd(instance.getEnd());
                    if ((newdtend.getDate() instanceof DateTime) &&
                        (((DateTime)newdtend.getDate()).getTimeZone() != null)) {
                        newdtend.setUtc(true);
                    }
                    copy.getProperties().add(newdtend);
                }
            }

            // Now have a valid expanded instance so add it
            newCal.getComponents().add(copy);
        }

        return newCal;
    }

    private void componentToUTC(Component comp) {
        // Do to each top-level property
        for (Property prop : (List<Property>) comp.getProperties()) {
            if (prop instanceof DateProperty) {
                DateProperty dprop = (DateProperty) prop;
                if ((dprop.getDate() instanceof DateTime) &&
                    (((DateTime) dprop.getDate()).getTimeZone() != null))
                    dprop.setUtc(true);
            }
        }

        // Do to each embedded component
        ComponentList subcomps = null;
        if (comp instanceof VEvent)
            subcomps = ((VEvent)comp).getAlarms();
        else if (comp instanceof VToDo)
            subcomps = ((VToDo)comp).getAlarms();

        if (subcomps != null) {
            for (Component subcomp : (List<Component>) subcomps)
                componentToUTC(subcomp);
        }
    }

    private void filterProperties(PropertyList properties,
                                  StringBuffer buffer) {
        if (isAllProperties()) {
            buffer.append(properties.toString());
            return;
        }

        if (! hasPropertyFilters())
            return;

        for (Property property : (List<Property>) properties) {
            PropertyMatch pm = testPropertyValue(property.getName());
            if (pm.isMatch()) {
                if (pm.isValueExcluded())
                    chompPropertyValue(property, buffer);
                else
                    buffer.append(property.toString());
            }
        }
    }

    private void chompPropertyValue(Property property,
                                    StringBuffer buffer) {
        buffer.append(property.getName()).
            append(property.getParameters()).
            append(':').
            append("\n");
    }

    private void filterSubComponents(ComponentList subComponents,
                                     StringBuffer buffer) {
        if (isAllSubComponents() && getLimit() != null) {
            buffer.append(subComponents.toString());
            return;
        }

        if (! (hasSubComponentFilters() || isAllSubComponents()))
            return;

        for (Component component : (List<Component>) subComponents) {
            if (getLimit() != null && component instanceof VEvent) {
                if (! includeOverride((VEvent) component))
                    continue;
            }

            if (isAllSubComponents())
                buffer.append(component.toString());
            else {
                OutputFilter subfilter = getSubComponentFilter(component);
                if (subfilter != null)
                    subfilter.filterSubComponent(component, buffer);
            }
        }
    }

    private boolean includeOverride(VEvent event) {
        // Policy: if event has a Recurrence-ID property then
        // include it if:
        //
        // a) If start/end are within limit range
        // b) else if r-id + duration is within the limit range
        // c) else if r-id is before limit start and
        // range=thisandfuture
        // d) else if r-id is after limit end and range=thisandprior
        //

        RecurrenceId rid = event.getRecurrenceId();
        if (rid == null)
            return true;

        Range range = (Range) rid.getParameter(Parameter.RANGE);
        DtStart dtstart = ((VEvent)event).getStartDate();
        DtEnd dtend = ((VEvent)event).getEndDate();
        DateTime start = new DateTime(dtstart.getDate());
        DateTime end = null;
        if (dtend != null) {
            end = new DateTime(dtend.getDate());
        } else {
            Dur duration = null;
            if (start instanceof DateTime) {
                // Its a timed event with no duration
                duration = new Dur(0, 0, 0, 0);
            } else {
                // Its an all day event so duration is one day
                duration = new Dur(1, 0, 0, 0);
            }
            end = (DateTime) org.osaf.cosmo.calendar.util.Dates.getInstance(duration.getTime(start), start);
        }

        Period p = new Period(start, end);
        if (! p.intersects(getLimit())) {
            Dur duration = new Dur(start, end);
            start = new DateTime(rid.getDate());
            end = (DateTime) org.osaf.cosmo.calendar.util.Dates.getInstance(duration.getTime(start), start);
            p = new Period(start, end);
            if (! p.intersects(getLimit())) {
                if (Range.THISANDFUTURE.equals(range)) {
                    if (start.compareTo(getLimit().getEnd()) >= 0)
                        return false;
                } else if (Range.THISANDPRIOR.equals(range)) {
                    if (start.compareTo(getLimit().getStart()) < 0)
                        return false;
                } else
                    return false;
            } 
        }

        return true;
    }

    private void filterSubComponent(Component subComponent,
                                    StringBuffer buffer) {
        buffer.append(Component.BEGIN).
            append(':').
            append(subComponent.getName()).
            append("\n");

        filterProperties(subComponent.getProperties(), buffer);
        filterSubComponents(ICalendarUtils.getSubComponents(subComponent), buffer);

        buffer.append(Component.END).
            append(':').
            append(subComponent.getName()).
            append("\n");
    }

    public boolean testComponent(Component comp) {
        return componentName.equalsIgnoreCase(comp.getName());
    }

    public boolean testSubComponent(Component subcomp) {
        if (allSubComponents)
            return true;

        if (subComponents == null)
            return false;

        if (subComponents.containsKey(subcomp.getName().toUpperCase()))
            return true;

        return false;
    }

    public PropertyMatch testPropertyValue(String name) {
        if (allProperties)
            return new PropertyMatch(true, false);

        if (properties == null)
            return new PropertyMatch(false, false);

        Boolean presult = (Boolean) properties.get(name.toUpperCase());
        if (presult == null)
            return new PropertyMatch(false, false);

        return new PropertyMatch(true, presult.booleanValue());
    }

    public String getComponentName() {
        return componentName;
    }

    public boolean isAllSubComponents() {
        return allSubComponents;
    }

    public void setAllSubComponents() {
        allSubComponents = true;
        subComponents = null;
    }

    public void addSubComponent(OutputFilter filter) {
        if (subComponents == null)
            subComponents = new HashMap();
        subComponents.put(filter.getComponentName().toUpperCase(), filter);
    }

    public boolean hasSubComponentFilters() {
        return subComponents != null;
    }

    public OutputFilter getSubComponentFilter(Component subcomp) {
        if (subComponents == null)
            return null;
        return (OutputFilter)
            subComponents.get(subcomp.getName().toUpperCase());
    }

    public boolean isAllProperties() {
        return allProperties;
    }

    public void setAllProperties() {
        allProperties = true;
        properties = null;
    }

    public void addProperty(String name,
                            boolean no_value) {
        if (properties == null)
            properties = new HashMap();
        properties.put(name.toUpperCase(), new Boolean(no_value));
    }

    public boolean hasPropertyFilters() {
        return properties != null;
    }

    public Period getExpand() {
        return expand;
    }

    public void setExpand(Period expand) {
        this.expand = expand;
    }

    public Period getLimit() {
        return limit;
    }

    public void setLimit(Period limit) {
        this.limit = limit;
    }

    public Period getLimitfb() {
        return limitfb;
    }

    public void setLimitfb(Period limitfb) {
        this.limitfb = limitfb;
    }

    public class PropertyMatch {
        private boolean match;
        private boolean valueExcluded;

        public PropertyMatch(boolean match,
                             boolean valueExcluded) {
            this.match = match;
            this.valueExcluded = valueExcluded;
        }

        public boolean isMatch() {
            return match;
        }

        public boolean isValueExcluded() {
            return valueExcluded;
        }
    }
}
