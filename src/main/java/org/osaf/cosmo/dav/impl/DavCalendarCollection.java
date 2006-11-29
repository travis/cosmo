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
package org.osaf.cosmo.dav.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.util.CalendarBuilderDispenser;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.dav.caldav.property.CalendarDescription;
import org.osaf.cosmo.dav.caldav.property.CalendarTimezone;
import org.osaf.cosmo.dav.caldav.property.MaxResourceSize;
import org.osaf.cosmo.dav.caldav.property.SupportedCalendarComponentSet;
import org.osaf.cosmo.dav.caldav.property.SupportedCalendarData;
import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.DataSizeException;
import org.osaf.cosmo.model.DuplicateEventUidException;
import org.osaf.cosmo.model.EventStamp;
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
 * <li><code>CALDAV:max-resource-size</code> (protected)</li>
 * </ul>
 *
 * @see DavCollection
 * @see CalendarCollectionItem
 */
public class DavCalendarCollection extends DavCollection
    implements CaldavConstants, ICalendarConstants {
    private static final Log log =
        LogFactory.getLog(DavCalendarCollection.class);
    private static final int[] RESOURCE_TYPES;
    private static final Set<String> DEAD_PROPERTY_FILTER =
        new HashSet<String>();

    static {
        registerLiveProperty(CALENDARDESCRIPTION);
        registerLiveProperty(CALENDARTIMEZONE);
        registerLiveProperty(SUPPORTEDCALENDARCOMPONENTSET);
        registerLiveProperty(SUPPORTEDCALENDARDATA);
        registerLiveProperty(MAXRESOURCESIZE);

        int cc = ResourceType.registerResourceType(ELEMENT_CALDAV_CALENDAR,
                                              NAMESPACE_CALDAV);
        RESOURCE_TYPES = new int[] { ResourceType.COLLECTION, cc };

        
        DEAD_PROPERTY_FILTER.add(CalendarCollectionStamp.class.getName());
    }

    /** */
    public DavCalendarCollection(CollectionItem collection,
                                 DavResourceLocator locator,
                                 DavResourceFactory factory,
                                 DavSession session) {
        super(collection, locator, factory, session);
    }

    /** */
    public DavCalendarCollection(DavResourceLocator locator,
                                 DavResourceFactory factory,
                                 DavSession session) {
        this(new CollectionItem(), locator, factory, session);
        getItem().addStamp(new CalendarCollectionStamp((CollectionItem) getItem()));
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

    /** */
    public void move(DavResource destination)
        throws DavException {
        validateDestination(destination);
        super.move(destination);
    }

    /** */
    public void copy(DavResource destination,
                     boolean shallow)
        throws DavException {
        validateDestination(destination);
        super.copy(destination, shallow);
    }

    // our methods

    /**
     * Returns the member resources in this calendar collection matching
     * the given filter.
     */
    public Set<DavCalendarResource> findMembers(CalendarFilter filter)
        throws DavException {
        // XXX what exceptions do we need to catch?

        Set<DavCalendarResource> members =
            new HashSet<DavCalendarResource>();
        CollectionItem collection = (CollectionItem) getItem();
        CalendarCollectionStamp calendar = getCalendarCollectionStamp();
        for (ContentItem memberItem :
                 getContentService().findEvents(collection, filter)) {
            String memberPath = getResourcePath() + "/" + memberItem.getName();
            DavResourceLocator memberLocator =
                getLocator().getFactory().
                createResourceLocator(getLocator().getPrefix(),
                                      getLocator().getWorkspacePath(),
                                      memberPath, false);
            DavEvent member = (DavEvent)
                ((StandardDavResourceFactory)getFactory()).
                createResource(memberLocator, getSession(), memberItem);
            members.add(member);
        }

        return members;
    }

    /**
     * Returns the default timezone for this calendar collection, if
     * one has been set.
     */
    public VTimeZone getTimeZone() {
       
        Calendar obj = getCalendarCollectionStamp().getTimezone();
        if (obj == null)
            return null;
        return (VTimeZone)
            obj.getComponents().getComponent(Component.VTIMEZONE);
    }

    /** */
    protected int[] getResourceTypes() {
        return RESOURCE_TYPES;
    }
    
    public CalendarCollectionStamp getCalendarCollectionStamp() {
        return CalendarCollectionStamp.getStamp(getItem());
    }

    /** */
    protected void populateItem(InputContext inputContext)
        throws DavException {
        super.populateItem(inputContext);

        CalendarCollectionStamp cc = getCalendarCollectionStamp();

        try {
            cc.setDescription(getItem().getName());
            // XXX: language should come from the input context
            cc.setLanguage(Locale.getDefault().toString());
        } catch (DataSizeException e) {
            throw new DavException(DavServletResponse.SC_FORBIDDEN, "Cannot store collection attribute: " + e.getMessage());
        }
    }

    /** */
    protected void loadLiveProperties() {
        super.loadLiveProperties();

        CalendarCollectionStamp cc = getCalendarCollectionStamp();
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
        properties.add(new MaxResourceSize());
    }

    /** */
    protected void setLiveProperty(DavProperty property) {
        super.setLiveProperty(property);

        CalendarCollectionStamp cc = getCalendarCollectionStamp();
        if (cc == null)
            return;

        DavPropertyName name = property.getName();
        if (property.getValue() == null)
            throw new ModelValidationException("null value for property " + name);
        String value = property.getValue().toString();

        if (name.equals(SUPPORTEDCALENDARDATA) ||
            name.equals(MAXRESOURCESIZE))
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

            cc.setTimezone(calendar);
        }
    }

    /** */
    protected void removeLiveProperty(DavPropertyName name) {
        super.removeLiveProperty(name);

        CalendarCollectionStamp cc = getCalendarCollectionStamp();
        if (cc == null)
            return;

        if (name.equals(SUPPORTEDCALENDARCOMPONENTSET) ||
            name.equals(SUPPORTEDCALENDARDATA) ||
            name.equals(MAXRESOURCESIZE)) {
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
    protected Set<String> getDeadPropertyFilter() {
        Set<String> copy = new HashSet<String>();
        copy.addAll(super.getDeadPropertyFilter());
        copy.addAll(DEAD_PROPERTY_FILTER);
        return copy;
    }

    /** */
    protected void saveFile(DavFile member)
        throws DavException {
        CollectionItem collection = (CollectionItem) getItem();
        CalendarCollectionStamp cc = getCalendarCollectionStamp();

        if (! (member instanceof DavEvent))
            throw new IllegalArgumentException("member not DavEvent");
        ContentItem content = (ContentItem) member.getItem();
        EventStamp event = EventStamp.getStamp(content);

        // CALDAV:supported-calendar-data
        String contentType = content.getContentType();
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
        if (hasMultipleComponentTypes(event.getCalendar()))
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED, "Calendar object contains more than one type of component");
        if (event.getCalendar().getProperties().getProperty(Property.METHOD) != null)
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED, "Calendar object contains METHOD property");

        // CALDAV:supported-calendar-component
        if (! cc.supportsCalendar(event.getCalendar()))
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED, "Calendar object does not contain at least one supported component");

        // XXX CALDAV:calendar-collection-location-ok

        // CALDAV:max-resource-size was already taken care of when
        // DavCollection.addMember called DavResourceBase.populateItem
        // on the event, though it returned a 409 rather than a 412

        // XXX CALDAV:min-date-time

        // XXX CALDAV:max-date-time

        // XXX CALDAV:max-instances

        // XXX CALDAV:max-attendees-per-instance

        if (event.getId() != -1) {
            if (log.isDebugEnabled())
                log.debug("updating event " + member.getResourcePath());

            try {
                content = getContentService().updateContent(content);
            } catch (DuplicateEventUidException e) {
                throw new DavException(DavServletResponse.SC_CONFLICT, "Uid already in use");
            }
        } else {
            if (log.isDebugEnabled())
                log.debug("creating event " + member.getResourcePath());

            try {
                content = getContentService().createContent(collection, content);
            } catch (DuplicateEventUidException e) {
                throw new DavException(DavServletResponse.SC_CONFLICT, "Uid already in use");
            }
        }

        member.setItem(content);
    }

    /** */
    protected void removeFile(DavFile member)
        throws DavException {
        if (! (member instanceof DavEvent))
            throw new IllegalArgumentException("member not DavEvent");

        ContentItem content = (ContentItem) member.getItem();
        
        // XXX: what exceptions need to be caught?
        if (log.isDebugEnabled())
            log.debug("removing event " + member.getResourcePath());
            
        getContentService().removeContent(content);
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

        CalendarCollectionStamp cc = getCalendarCollectionStamp();
        Calendar calendar = null;
        try {
            calendar = cc.getCalendar();
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

    private void validateDestination(DavResource destination)
        throws DavException {
        DavResource destinationCollection = destination.getCollection();
        if (! (destinationCollection instanceof DavCollection))
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED, "A calendar collection may only be created within a collection");
        if (destinationCollection instanceof DavCalendarCollection)
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED, "A calendar collection may not be created within a calendar collection");
    }
}
