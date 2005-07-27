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
package org.osaf.cosmo.jackrabbit.io;

import java.io.InputStream;
import java.util.Iterator;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.server.io.AbstractCommand;
import org.apache.jackrabbit.server.io.AbstractContext;
import org.apache.jackrabbit.server.io.ImportContext;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.jcr.CosmoJcrConstants;
import org.osaf.cosmo.jcr.JCRUtils;

/**
 * A jcr-server import command that creates a node representing a
 * calendar object below the current node.
 *
 * The command only executes when the context's content type is
 * "text/calendar" and when the current node represents a CalDAV
 * calendar collection. The context's input stream is parsed into an
 * iCalendar object from which the new node's child nodes and
 * properties are set.
 */
public class CalendarResourceImportCommand extends AbstractCommand {
    private static final Log log =
        LogFactory.getLog(CalendarResourceImportCommand.class);

    /**
     */
    public boolean execute(AbstractContext context)
        throws Exception {
        if (context instanceof ImportContext) {
            return execute((ImportContext) context);
        } else {
            return false;
        }
    }

    /**
     */
    protected boolean execute(ImportContext context) throws Exception {
        if (context.getInputStream() == null) {
            // assume already consumed
            return false;
        }
        if (! canHandle(context)) {
            // ignore import
            return false;
        }

        // we only handle events at the moment, no todos or journals
        Calendar calendar = parseCalendar(context.getInputStream());
        boolean foundEvent = false;
        for (Iterator i=calendar.getComponents().iterator(); i.hasNext();) {
            Component component = (Component) i.next();
            if (component instanceof VEvent) {
                foundEvent = true;
                break;
            }
        }
        if (! foundEvent) {
            log.warn("No events found within calendar resource");
            return false;
        }

        importResource(context);

        // at this point section 4.5 of the spec guarantees us that
        // all the objects in the calendar are events with the same
        // uid (all part of a recurrence set) and probably some
        // timezones, one for each unique tzid in the events
        for (Iterator i=calendar.getComponents().iterator(); i.hasNext();) {
            Component component = (Component) i.next();
            if (component instanceof VEvent) {
                importEvent(context, (VEvent) component);
                continue;
            }
            if (component instanceof VTimeZone) {
                // XXX
            }
            // probably should return a BAD REQUEST error here
            throw new IllegalStateException("calendar resource contains unhandleable resource of type " + component.getClass());
        }

        // let later chain commands operate on the newly imported resource
        return false;
    }

    /**
     */
    protected boolean canHandle(ImportContext context)
        throws Exception {
        return (context.getContentType().
                equals(CosmoDavConstants.CT_ICALENDAR) &&
                context.getNode().
                isNodeType(CosmoJcrConstants.NT_CALDAV_COLLECTION));
    }

    /**
     */
    protected Calendar parseCalendar(InputStream in)
        throws Exception {
        CalendarBuilder builder = new CalendarBuilder();
        return builder.build(in);
    }

    /**
     */
    protected void importResource(ImportContext context)
        throws Exception {
        Node calendarCollectionNode = context.getNode();
        Node resourceNode =
            calendarCollectionNode.hasNode(context.getSystemId()) ?
            calendarCollectionNode.getNode(context.getSystemId()) :
            calendarCollectionNode.addNode(context.getSystemId(),
                                           CosmoJcrConstants.NT_CALDAV_RESOURCE);
        java.util.Calendar lastMod = java.util.Calendar.getInstance();
        if (context.getModificationTime() != 0) {
            lastMod.setTimeInMillis(context.getModificationTime());
        }
        resourceNode.setProperty(JCR_LASTMODIFIED, lastMod);
        context.setNode(resourceNode);
    }

    /**
     */
    protected void importEvent(ImportContext context, VEvent event)
        throws Exception {
        JCRUtils.setEventNode(event, context.getNode());
    }
}
