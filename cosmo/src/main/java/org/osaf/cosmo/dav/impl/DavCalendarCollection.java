/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.osaf.cosmo.calendar.EntityConverter;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.LockedException;
import org.osaf.cosmo.dav.ProtectedPropertyModificationException;
import org.osaf.cosmo.dav.UnprocessableEntityException;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.dav.caldav.InvalidCalendarLocationException;
import org.osaf.cosmo.dav.caldav.MaxResourceSizeException;
import org.osaf.cosmo.dav.caldav.TimeZoneExtractor;
import org.osaf.cosmo.dav.caldav.UidConflictException;
import org.osaf.cosmo.dav.caldav.property.CalendarDescription;
import org.osaf.cosmo.dav.caldav.property.CalendarTimezone;
import org.osaf.cosmo.dav.caldav.property.MaxResourceSize;
import org.osaf.cosmo.dav.caldav.property.SupportedCalendarComponentSet;
import org.osaf.cosmo.dav.caldav.property.SupportedCalendarData;
import org.osaf.cosmo.dav.caldav.property.SupportedCollationSet;
import org.osaf.cosmo.dav.property.DavProperty;
import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionLockedException;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.DataSizeException;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.IcalUidInUseException;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.StampUtils;

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
public class DavCalendarCollection extends DavCollectionBase
    implements CaldavConstants, ICalendarConstants {
    private static final Log log =
        LogFactory.getLog(DavCalendarCollection.class);
    private static final Set<String> DEAD_PROPERTY_FILTER =
        new HashSet<String>();

    static {
        registerLiveProperty(CALENDARDESCRIPTION);
        registerLiveProperty(CALENDARTIMEZONE);
        registerLiveProperty(SUPPORTEDCALENDARCOMPONENTSET);
        registerLiveProperty(SUPPORTEDCALENDARDATA);
        registerLiveProperty(MAXRESOURCESIZE);
        
        DEAD_PROPERTY_FILTER.add(CalendarCollectionStamp.class.getName());
    }

    /** */
    public DavCalendarCollection(CollectionItem collection,
                                 DavResourceLocator locator,
                                 DavResourceFactory factory,
                                 EntityFactory entityFactory)
        throws DavException {
        super(collection, locator, factory, entityFactory);
    }

    /** */
    public DavCalendarCollection(DavResourceLocator locator,
                                 DavResourceFactory factory,
                                 EntityFactory entityFactory)
        throws DavException {
        this(entityFactory.createCollection(), locator, factory, entityFactory);
        getItem().addStamp(entityFactory.createCalendarCollectionStamp((CollectionItem) getItem()));
    }

    // Jackrabbit DavResource

    /** */
    public String getSupportedMethods() {
        // calendar collections not allowed inside calendar collections
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, COPY, DELETE, MOVE, MKTICKET, DELTICKET, MKCOL, REPORT";
    }

    /** */
    public void move(DavResource destination)
        throws org.apache.jackrabbit.webdav.DavException {
        validateDestination(destination);
        super.move(destination);
    }

    /** */
    public void copy(DavResource destination,
                     boolean shallow)
        throws org.apache.jackrabbit.webdav.DavException {
        validateDestination(destination);
        super.copy(destination, shallow);
    }

    // DavCollection

    public boolean isCalendarCollection() {
        return true;
    }

    // our methods

    /**
     * Returns the member resources in this calendar collection matching
     * the given filter.
     */
    public Set<DavCalendarResource> findMembers(CalendarFilter filter)
        throws DavException {
        Set<DavCalendarResource> members =
            new HashSet<DavCalendarResource>();

        CollectionItem collection = (CollectionItem) getItem();
        for (ContentItem memberItem :
             getCalendarQueryProcesor().filterQuery(collection, filter)) {
            DavResource resource = memberToResource(memberItem);
            if(resource!=null)
                members.add((DavCalendarResource) resource);
        }

        return members;
    }
    
    /**
     * Returns a VFREEBUSY component containing
     * the freebusy periods for the calendar collection for the
     * specified time range.
     * @param period time range for freebusy information
     * @return VFREEBUSY component containing FREEBUSY periods for
     *         specified timerange
     */
    public VFreeBusy generateFreeBusy(Period period) {

        VFreeBusy vfb = this.getCalendarQueryProcesor().freeBusyQuery(
                (CollectionItem) getItem(), period);
        
        return vfb;
    }

    /**
     * Returns the default timezone for this calendar collection, if
     * one has been set.
     */
    public VTimeZone getTimeZone() {
        Calendar obj = getCalendarCollectionStamp().getTimezoneCalendar();
        if (obj == null)
            return null;
        return (VTimeZone)
            obj.getComponents().getComponent(Component.VTIMEZONE);
    }

    protected Set<QName> getResourceTypes() {
        Set<QName> rt = super.getResourceTypes();
        rt.add(RESOURCE_TYPE_CALENDAR);
        return rt;
    }
    
    public CalendarCollectionStamp getCalendarCollectionStamp() {
        return StampUtils.getCalendarCollectionStamp(getItem());
    }


    /** */
    protected void populateItem(InputContext inputContext)
        throws DavException {
        super.populateItem(inputContext);

        CalendarCollectionStamp cc = getCalendarCollectionStamp();

        try {
            cc.setDescription(getItem().getName());
            // XXX: language should come from the input context
        } catch (DataSizeException e) {
            throw new MaxResourceSizeException(e.getMessage());
        }
    }

    /** */
    protected void loadLiveProperties(DavPropertySet properties) {
        super.loadLiveProperties(properties);

        CalendarCollectionStamp cc = getCalendarCollectionStamp();
        if (cc == null)
            return;

        if (cc.getDescription() != null)
            properties.add(new CalendarDescription(cc.getDescription(),
                                                   cc.getLanguage()));
        if (cc.getTimezoneCalendar() != null)
            properties.add(new CalendarTimezone(cc.getTimezoneCalendar().toString()));

        properties.add(new SupportedCalendarComponentSet());
        properties.add(new SupportedCollationSet());
        properties.add(new SupportedCalendarData());
        properties.add(new MaxResourceSize());
    }

    /** */
    protected void setLiveProperty(DavProperty property)
        throws DavException {
        super.setLiveProperty(property);

        CalendarCollectionStamp cc = getCalendarCollectionStamp();
        if (cc == null)
            return;

        DavPropertyName name = property.getName();
        if (property.getValue() == null)
            throw new UnprocessableEntityException("Property " + name + " requires a value");

        if (name.equals(SUPPORTEDCALENDARCOMPONENTSET) ||
            name.equals(SUPPORTEDCALENDARDATA) ||
            name.equals(MAXRESOURCESIZE))
            throw new ProtectedPropertyModificationException(name);

        if (name.equals(CALENDARDESCRIPTION)) {
            cc.setDescription(property.getValueText());
            cc.setLanguage(property.getLanguage());
            return;
        }

        if (name.equals(CALENDARTIMEZONE))
            cc.setTimezoneCalendar(TimeZoneExtractor.extract(property));
    }

    /** */
    protected void removeLiveProperty(DavPropertyName name)
        throws DavException {
        super.removeLiveProperty(name);

        CalendarCollectionStamp cc = getCalendarCollectionStamp();
        if (cc == null)
            return;

        if (name.equals(SUPPORTEDCALENDARCOMPONENTSET) ||
            name.equals(SUPPORTEDCALENDARDATA) ||
            name.equals(MAXRESOURCESIZE))
            throw new ProtectedPropertyModificationException(name);

        if (name.equals(CALENDARDESCRIPTION)) {
            cc.setDescription(null);
            cc.setLanguage(null);
            return;
        }

        if (name.equals(CALENDARTIMEZONE)) {
            cc.setTimezoneCalendar(null);
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
    protected void saveContent(DavItemContent member)
        throws DavException {
        if (! (member instanceof DavCalendarResource))
            throw new IllegalArgumentException("member not DavCalendarResource");

        ContentItem content = null;
        if (member instanceof DavEvent) {
            saveEvent(member);
        } else {
            try {
                super.saveContent(member);
            } catch (IcalUidInUseException e) {
                throw new UidConflictException(e);
            }
        }
    }

    private void saveEvent(DavItemContent member)
        throws DavException {
        
        ContentItem content = (ContentItem) member.getItem();
        EventStamp event = StampUtils.getEventStamp(content);
        EntityConverter converter = new EntityConverter(getEntityFactory());
        Set<ContentItem> toUpdate = new LinkedHashSet<ContentItem>();
        
        // convert icalendar representation to cosmo data model
        toUpdate.addAll(converter.convertEventCalendar(
                (NoteItem) content, event.getEventCalendar()));
        
        if (event.getCreationDate()!=null) {
            if (log.isDebugEnabled())
                log.debug("updating event " + member.getResourcePath());

            try {
                getContentService().updateContentItems(content.getParents(),
                        toUpdate);
            } catch (IcalUidInUseException e) {
                throw new UidConflictException(e);
            } catch (CollectionLockedException e) {
                throw new LockedException();
            }
        } else {
            if (log.isDebugEnabled())
                log.debug("creating event " + member.getResourcePath());

            try {
                getContentService().createContentItems(
                        (CollectionItem) getItem(), toUpdate);
            } catch (IcalUidInUseException e) {
                throw new UidConflictException(e);
            } catch (CollectionLockedException e) {
                throw new LockedException();
            }
        }

        member.setItem(content);
    }

    /** */
    protected void removeContent(DavItemContent member)
        throws DavException {
        if (! (member instanceof DavCalendarResource))
            throw new IllegalArgumentException("member not DavCalendarResource");

        ContentItem content = (ContentItem) member.getItem();
        CollectionItem parent = (CollectionItem) getItem();
        
        // XXX: what exceptions need to be caught?
        if (log.isDebugEnabled())
            log.debug("removing event " + member.getResourcePath());

        try {
            if(content instanceof NoteItem)
                getContentService().removeItemFromCollection(content, parent);
            else
                getContentService().removeContent(content);
        } catch (CollectionLockedException e) {
            throw new LockedException();
        }
    }

    private void validateDestination(DavResource destination)
        throws DavException {
        if (destination.getParent() instanceof DavCalendarCollection)
            throw new InvalidCalendarLocationException("Parent collection of destination must not be a calendar collection");
    }
}
