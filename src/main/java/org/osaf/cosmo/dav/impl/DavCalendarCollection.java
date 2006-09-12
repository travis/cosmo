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
package org.osaf.cosmo.dav.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.ValidationException;

import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.ResourceType;

import org.apache.log4j.Logger;

import org.osaf.cosmo.calendar.util.CalendarBuilderDispenser;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.dav.caldav.property.CalendarDescription;
import org.osaf.cosmo.dav.caldav.property.CalendarTimezone;
import org.osaf.cosmo.dav.caldav.property.SupportedCalendarComponentSet;
import org.osaf.cosmo.dav.caldav.property.SupportedCalendarData;
import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.CalendarEventItem;
import org.osaf.cosmo.model.CalendarCollectionItem;
import org.osaf.cosmo.model.DuplicateEventUidException;
import org.osaf.cosmo.model.ModelConversionException;
import org.osaf.cosmo.model.ModelValidationException;

/**
 * Extends <code>DavCollection</code> to adapt the Cosmo
 * <code>CalendarCollectionItem</code> to the DAV resource model.
 *
 * This class defines the following live properties:
 *
 * <ul>
 * <li><code>CALDAV:calendar-description</code></li>
 * <li><code>CALDAV:calendar-timezone</code></li>
 * <li><code>CALDAV:calendar-supported-calendar-component-set</code>
 * (protected)</li>
 * <li><code>CALDAV:supported-calendar-data</code> (protected)</li>
 * </ul>
 *
 * @see DavCollection
 * @see CalendarCollectionItem
 */
public class DavCalendarCollection extends DavCollection
    implements CaldavConstants, ICalendarConstants {
    private static final Logger log =
        Logger.getLogger(DavCalendarCollection.class);
    private static final int[] RESOURCE_TYPES;
    private static final Set DEAD_PROPERTY_FILTER = new HashSet();

    static {
        registerLiveProperty(CALENDARDESCRIPTION);
        registerLiveProperty(CALENDARTIMEZONE);
        registerLiveProperty(SUPPORTEDCALENDARCOMPONENTSET);
        registerLiveProperty(SUPPORTEDCALENDARDATA);

        int cc = ResourceType.registerResourceType(ELEMENT_CALDAV_CALENDAR,
                                              NAMESPACE_CALDAV);
        RESOURCE_TYPES = new int[] { ResourceType.COLLECTION, cc };

        DEAD_PROPERTY_FILTER.add(CalendarCollectionItem.ATTR_CALENDAR_UID);
        DEAD_PROPERTY_FILTER.add(CalendarCollectionItem.ATTR_CALENDAR_DESCRIPTION);
        DEAD_PROPERTY_FILTER.add(CalendarCollectionItem.ATTR_CALENDAR_LANGUAGE);
        DEAD_PROPERTY_FILTER.add(CalendarCollectionItem.ATTR_CALENDAR_TIMEZONE);
        DEAD_PROPERTY_FILTER.add(CalendarCollectionItem.ATTR_CALENDAR_SUPPORTED_COMPONENT_SET);
    }

    /** */
    public DavCalendarCollection(CalendarCollectionItem collection,
                                 DavResourceLocator locator,
                                 DavResourceFactory factory,
                                 DavSession session) {
        super(collection, locator, factory, session);
    }

    /** */
    public DavCalendarCollection(DavResourceLocator locator,
                                 DavResourceFactory factory,
                                 DavSession session) {
        this(new CalendarCollectionItem(), locator, factory, session);
    }

    // DavResource

    /** */
    public String getSupportedMethods() {
        // calendar collections not allowed inside calendar collections
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, COPY, DELETE, MOVE, MKTICKET, DELTICKET, MKCOL";
    }

    /** */
    public void spool(OutputContext outputContext)
        throws IOException {
        writeICalendar(outputContext);
    }

    // our methods

    /** */
    protected int[] getResourceTypes() {
        return RESOURCE_TYPES;
    }

    /** */
    protected void populateItem(InputContext inputContext) {
        super.populateItem(inputContext);

        CalendarCollectionItem cc = (CalendarCollectionItem) getItem();

        cc.setDescription(cc.getName());
        cc.setLanguage(Locale.getDefault().toString());
    }

    /** */
    protected void loadLiveProperties() {
        super.loadLiveProperties();

        CalendarCollectionItem cc = (CalendarCollectionItem) getItem();
        if (cc == null)
            return;

        DavPropertySet properties = getProperties();

        if (cc.getDescription() != null)
            properties.add(new CalendarDescription(cc.getDescription(),
                                                   cc.getLanguage()));
        if (cc.getTimezone() != null)
            properties.add(new CalendarTimezone(cc.getTimezone().toString()));

        properties.add(new SupportedCalendarComponentSet(cc.getSupportedComponents()));
        properties.add(new SupportedCalendarData());
    }

    /** */
    protected void setLiveProperty(DavProperty property) {
        super.setLiveProperty(property);

        CalendarCollectionItem cc = (CalendarCollectionItem) getItem();
        if (cc == null)
            return;

        DavPropertyName name = property.getName();
        if (property.getValue() == null)
            throw new ModelValidationException("null value for property " + name);
        String value = property.getValue().toString();

        if (name.equals(SUPPORTEDCALENDARDATA))
            throw new ModelValidationException("cannot set protected property " + name);

        if (name.equals(SUPPORTEDCALENDARCOMPONENTSET)) {
            // this property can only be initialized at creation time
            if (! exists()) {
                cc.setSupportedComponents(((SupportedCalendarComponentSet) property).getComponentTypes());
                return;
            }
            throw new ModelValidationException("cannot set protected property " + name);
        }

        if (name.equals(CALENDARDESCRIPTION)) {
            cc.setDescription(value);
            if (cc.getLanguage() == null)
                cc.setLanguage(Locale.getDefault().toString());
            return;
        }

        if (name.equals(CALENDARTIMEZONE)) {
            Calendar calendar = null;
            // validate the timezone
            try {
                CalendarBuilder builder =
                    CalendarBuilderDispenser.getCalendarBuilder();
                calendar = builder.build(new StringReader(value));
                calendar.validate(true);
            } catch (IOException e) {
                throw new ModelConversionException("unable to build calendar object from " + CALENDARTIMEZONE + " property value ", e);
            } catch (ParserException e) {
                throw new ModelConversionException("unable to parse " + CALENDARTIMEZONE + " property value: " + e.getMessage());
            } catch (ValidationException e) {
                throw new ModelValidationException("invalid " + CALENDARTIMEZONE + " property value: " + e.getMessage());
            }

            if (calendar.getComponents().size() > 1)
                throw new ModelValidationException(CALENDARTIMEZONE + " property must contain exactly one VTIMEZONE component - too many components enclosed");

            Component tz =
                calendar.getComponents().getComponent(Component.VTIMEZONE);
            if (tz == null)
                throw new ModelValidationException(CALENDARTIMEZONE + " must contain exactly one VTIMEZONE component - enclosed component not VTIMEZONE");

            cc.setTimezone(value);
        }
    }

    /** */
    protected void removeLiveProperty(DavPropertyName name) {
        super.removeLiveProperty(name);

        CalendarCollectionItem cc = (CalendarCollectionItem) getItem();
        if (cc == null)
            return;

        if (name.equals(SUPPORTEDCALENDARCOMPONENTSET) ||
            name.equals(SUPPORTEDCALENDARDATA)) {
            throw new ModelValidationException("cannot remove protected property " + name);
        }

        if (name.equals(CALENDARDESCRIPTION)) {
            cc.setDescription(null);
            cc.setLanguage(null);
            return;
        }

        if (name.equals(CALENDARTIMEZONE)) {
            cc.setTimezone(null);
            return;
        }
    }

    /** */
    protected Set getDeadPropertyFilter() {
        Set copy = new HashSet();
        copy.addAll(super.getDeadPropertyFilter());
        copy.addAll(DEAD_PROPERTY_FILTER);
        return copy;
    }

    /** */
    protected void saveFile(DavFile member)
        throws DavException {
        CalendarCollectionItem collection = (CalendarCollectionItem) getItem();

        if (! (member instanceof DavEvent))
            throw new IllegalArgumentException("member not DavEvent");
        CalendarEventItem event = (CalendarEventItem) member.getItem();

        // CALDAV:supported-calendar-data
        String contentType = event.getContentType();
        if (contentType == null)
            throw new DavException(DavServletResponse.SC_BAD_REQUEST,
                                   "No media type specified for calendar data");
        if (! contentType.equals(ICALENDAR_MEDIA_TYPE))
            throw new DavException(DavServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                                   "Content of type " + contentType + " not supported in calendar collection");

        // CALDAV:valid-calendar-data
        Calendar calendar = null;
        try {
            // forces the event's content to be parsed
            calendar = event.getCalendar();
            calendar.validate(true);
        } catch (ModelConversionException e) {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "Unable to interpret content as calendar object: " + e.getMessage());
        } catch (ValidationException e) {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "Invalid calendar object: " + e.getMessage());
        }

        // CALDAV:valid-calendar-object-resource
        if (hasMultipleComponentTypes(calendar))
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED, "Calendar object contains more than one type of component");
        if (calendar.getProperties().getProperty(Property.METHOD) != null)
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED, "Calendar object contains METHOD property");

        // CALDAV:supported-calendar-component
        if (! collection.supportsCalendar(calendar))
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED, "Calendar object does not contain at least one supported component");

        // XXX CALDAV:calendar-collection-location-ok

        // XXX CALDAV:max-resource-size

        // XXX CALDAV:min-date-time

        // XXX CALDAV:max-date-time

        // XXX CALDAV:max-instances

        // XXX CALDAV:max-attendees-per-instance

        // XXX: what exceptions need to be caught?
        if (event.getId() != -1) {
            if (log.isDebugEnabled())
                log.debug("updating event " + member.getResourcePath());

            try {
                event = getContentService().updateEvent(event);
            } catch (DuplicateEventUidException e) {
                throw new DavException(DavServletResponse.SC_CONFLICT, "Uid already in use");
            }
        } else {
            if (log.isDebugEnabled())
                log.debug("creating event " + member.getResourcePath());
            
            try {
                event = getContentService().addEvent(collection, event);
            } catch (DuplicateEventUidException e) {
                throw new DavException(DavServletResponse.SC_CONFLICT, "Uid already in use");
            }
        }

        member.setItem(event);
    }

    /** */
    protected void removeFile(DavFile member)
        throws DavException {
        if (! (member instanceof DavEvent))
            throw new IllegalArgumentException("member not DavEvent");
        CalendarEventItem event = (CalendarEventItem) member.getItem();

        // XXX: what exceptions need to be caught?
        if (log.isDebugEnabled())
            log.debug("removing event " + member.getResourcePath());
            
        getContentService().removeEvent(event);
    }

    private static boolean hasMultipleComponentTypes(Calendar calendar) {
        String found = null;
        for (Iterator<Component> i=calendar.getComponents().iterator();
             i.hasNext();) {
            Component component = i.next();
            if (component instanceof VTimeZone)
                continue;
            if (found == null) {
                found = component.getName();
                continue;
            }
            if (! found.equals(component.getName()))
                return true;
        }
        return false;
    }

    private void writeICalendar(OutputContext context)
        throws IOException {
        if (! exists())
            return;

        if (log.isDebugEnabled())
            log.debug("writing icalendar for " + getItem().getName());

        Calendar calendar = null;
        try {
            calendar = ((CalendarCollectionItem) getItem()).getCalendar();
        } catch (ParserException e) {
            throw new IllegalStateException("unable to parse member's icalendar content", e);
        }

        if (calendar.getComponents().isEmpty())
            return;

        context.setContentType(IOUtil.buildContentType("text/calendar",
                                                       "UTF-8"));

        // XXX content length unknown unless we write a temp file
        // modification time and etag are undefined for a collection

        if (! context.hasStream()) {
            return;
        }

        CalendarOutputter outputter = new CalendarOutputter();
        outputter.setValidating(false);
        try {
            outputter.output(calendar, context.getOutputStream());
        } catch (ValidationException e) {
            throw new IllegalStateException("unable to validate member's icalendar content", e);
        }
    }
}
