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

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.model.parameter.*;

/**
 * Utilities for working with iCalendar in Cosmo.
 */
public class ICalendarUtils {

    // convenience accessors for specific icalendar properties

    /**
     */
    public static Attach getAttach(Component component) {
        return (Attach)
            component.getProperties().getProperty(Property.ATTACH);
    }

    /**
     */
    public static Attendee getAttendee(Component component) {
        return (Attendee)
            component.getProperties().getProperty(Property.ATTENDEE);
    }

    /**
     */
    public static Categories getCategories(Component component) {
        return (Categories)
            component.getProperties().getProperty(Property.CATEGORIES);
    }

    /**
     */
    public static Clazz getClazz(Component component) {
        return (Clazz) component.getProperties().getProperty(Property.CLASS);
    }

    /**
     */
    public static Comment getComment(Component component) {
        return (Comment)
            component.getProperties().getProperty(Property.COMMENT);
    }

    /**
     */
    public static Contact getContact(Component component) {
        return (Contact)
            component.getProperties().getProperty(Property.CONTACT);
    }

    /**
     */
    public static Created getCreated(Component component) {
        return (Created)
            component.getProperties().getProperty(Property.CREATED);
    }

    /**
     */
    public static Description getDescription(Component component) {
        return (Description)
            component.getProperties().getProperty(Property.DESCRIPTION);
    }

    /**
     */
    public static DtEnd getDtEnd(Component component) {
        return (DtEnd) component.getProperties().getProperty(Property.DTEND);
    }

    /**
     */
    public static DtStamp getDtStamp(Component component) {
        return (DtStamp)
            component.getProperties().getProperty(Property.DTSTAMP);
    }

    /**
     */
    public static DtStart getDtStart(Component component) {
        return (DtStart)
            component.getProperties().getProperty(Property.DTSTART);
    }

    /**
     */
    public static Duration getDuration(Component component) {
        return (Duration)
            component.getProperties().getProperty(Property.DURATION);
    }

    /**
     */
    public static ExDate getExDate(Component component) {
        return (ExDate) component.getProperties().getProperty(Property.EXDATE);
    }

    /**
     */
    public static ExRule getExRule(Component component) {
        return (ExRule) component.getProperties().getProperty(Property.EXRULE);
    }

    /**
     */
    public static Geo getGeo(Component component) {
        return (Geo) component.getProperties().getProperty(Property.GEO);
    }

    /**
     */
    public static LastModified getLastModified(Component component) {
        return (LastModified)
            component.getProperties().getProperty(Property.LAST_MODIFIED);
    }

    /**
     */
    public static Location getLocation(Component component) {
        return (Location)
            component.getProperties().getProperty(Property.LOCATION);
    }

    /**
     */
    public static Organizer getOrganizer(Component component) {
        return (Organizer)
            component.getProperties().getProperty(Property.ORGANIZER);
    }

    /**
     */
    public static Priority getPriority(Component component) {
        return (Priority)
            component.getProperties().getProperty(Property.PRIORITY);
    }

    /**
     */
    public static RDate getRDate(Component component) {
        return (RDate) component.getProperties().getProperty(Property.RDATE);
    }

    /**
     */
    public static RecurrenceId getRecurrenceId(Component component) {
        return (RecurrenceId)
            component.getProperties().getProperty(Property.RECURRENCE_ID);
    }

    /**
     */
    public static RelatedTo getRelatedTo(Component component) {
        return (RelatedTo)
            component.getProperties().getProperty(Property.RELATED_TO);
    }

    /**
     */
    public static RequestStatus getRequestStatus(Component component) {
        return (RequestStatus)
            component.getProperties().getProperty(Property.REQUEST_STATUS);
    }

    /**
     */
    public static Resources getResources(Component component) {
        return (Resources)
            component.getProperties().getProperty(Property.RESOURCES);
    }

    /**
     */
    public static RRule getRRule(Component component) {
        return (RRule) component.getProperties().getProperty(Property.RRULE);
    }

    /**
     */
    public static Sequence getSequence(Component component) {
        return (Sequence)
            component.getProperties().getProperty(Property.SEQUENCE);
    }

    /**
     */
    public static Status getStatus(Component component) {
        return (Status) component.getProperties().getProperty(Property.STATUS);
    }

    /**
     */
    public static Summary getSummary(Component component) {
        return (Summary)
            component.getProperties().getProperty(Property.SUMMARY);
    }

    /**
     */
    public static Transp getTransp(Component component) {
        return (Transp) component.getProperties().getProperty(Property.TRANSP);
    }

    /**
     */
    public static Uid getUid(Component component) {
        return (Uid) component.getProperties().getProperty(Property.UID);
    }

    /**
     */
    public static Url getUrl(Component component) {
        return (Url) component.getProperties().getProperty(Property.URL);
    }

    // convenience accessors for specific icalendar parameters

    /**
     */
    public static AltRep getAltRep(Property property) {
        return (AltRep) property.getParameters().getParameter(Parameter.ALTREP);
    }

    /**
     */
    public static Cn getCn(Property property) {
        return (Cn) property.getParameters().getParameter(Parameter.CN);
    }

    /**
     */
    public static CuType getCuType(Property property) {
        return (CuType) property.getParameters().getParameter(Parameter.CUTYPE);
    }

    /**
     */
    public static DelegatedFrom getDelegatedFrom(Property property) {
        return (DelegatedFrom)
            property.getParameters().getParameter(Parameter.DELEGATED_FROM);
    }

    /**
     */
    public static DelegatedTo getDelegatedTo(Property property) {
        return (DelegatedTo)
            property.getParameters().getParameter(Parameter.DELEGATED_TO);
    }

    /**
     */
    public static Dir getDir(Property property) {
        return (Dir) property.getParameters().getParameter(Parameter.DIR);
    }

    /**
     */
    public static FmtType getFmtType(Property property) {
        return (FmtType)
            property.getParameters().getParameter(Parameter.FMTTYPE);
    }

    /**
     */
    public static Language getLanguage(Property property) {
        return (Language)
            property.getParameters().getParameter(Parameter.LANGUAGE);
    }

    /**
     */
    public static Member getMember(Property property) {
        return (Member) property.getParameters().getParameter(Parameter.MEMBER);
    }

    /**
     */
    public static PartStat getPartStat(Property property) {
        return (PartStat)
            property.getParameters().getParameter(Parameter.PARTSTAT);
    }

    /**
     */
    public static Range getRange(Property property) {
        return (Range) property.getParameters().getParameter(Parameter.RANGE);
    }

    /**
     */
    public static RelType getRelType(Property property) {
        return (RelType)
            property.getParameters().getParameter(Parameter.RELTYPE);
    }

    /**
     */
    public static Role getRole(Property property) {
        return (Role) property.getParameters().getParameter(Parameter.ROLE);
    }

    /**
     */
    public static Rsvp getRsvp(Property property) {
        return (Rsvp) property.getParameters().getParameter(Parameter.RSVP);
    }

    /**
     */
    public static SentBy getSentBy(Property property) {
        return (SentBy)
            property.getParameters().getParameter(Parameter.SENT_BY);
    }
}
