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

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import org.apache.jackrabbit.server.io.AbstractCommand;
import org.apache.jackrabbit.server.io.AbstractContext;
import org.apache.jackrabbit.server.io.ImportContext;
import org.apache.jackrabbit.webdav.DavException;

import org.apache.log4j.Logger;

import org.osaf.cosmo.UnsupportedFeatureException;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.dav.CosmoDavResponse;
import org.osaf.cosmo.icalendar.CosmoICalendarConstants;
import org.osaf.cosmo.icalendar.RecurrenceException;
import org.osaf.cosmo.jcr.CosmoJcrConstants;

import org.springframework.dao.DataAccessException;

/**
 * An import command for storing the calendar object attached to a
 * dav resource.
 */
public class StoreCalendarObjectCommand extends AbstractCommand {
    private static final Logger log =
        Logger.getLogger(StoreCalendarObjectCommand.class);
    private static final String BEAN_CALENDAR_DAO = "calendarDao";

    /**
     */
    public boolean execute(AbstractContext context)
        throws Exception {
        if (context instanceof ApplicationContextAwareImportContext) {
            return execute((ApplicationContextAwareImportContext) context);
        }
        else {
            return false;
        }
    }

    /**
     */
    public boolean execute(ApplicationContextAwareImportContext context)
        throws Exception {
        if (! context.getContentType().
            equals(CosmoICalendarConstants.CONTENT_TYPE)) {
            return false;
        }

        Node resourceNode = context.getNode();
        if (resourceNode == null ||
            ! resourceNode.isNodeType(CosmoJcrConstants.NT_DAV_RESOURCE)) {
            return false;
        }

        // get a handle to the resource content
        Node content = resourceNode.getNode(CosmoJcrConstants.NN_JCR_CONTENT);
        InputStream in =
            content.getProperty(CosmoJcrConstants.NP_JCR_DATA).getStream();

        try {
            // parse the resource
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(in);

            // store the resource in the repository
            CalendarDao dao = (CalendarDao) 
                context.getApplicationContext().
                getBean(BEAN_CALENDAR_DAO, CalendarDao.class);
            dao.storeCalendarObject(resourceNode, calendar);
        } catch (ParserException e) {
            // indicates an incorrectly-formatted resource
            throw new DavException(CosmoDavResponse.SC_FORBIDDEN,
                                   "Error parsing calendar resource: " +
                                   e.getMessage());
        } catch (UnsupportedFeatureException e) {
            // indicates that no supported component types were found
            // in the resource
            throw new DavException(CosmoDavResponse.SC_CONFLICT,
                                   e.getMessage());
        } catch (RecurrenceException e) {
            // indicates an incorrectly-constructed recurring event
            // resource
            throw new DavException(CosmoDavResponse.SC_FORBIDDEN,
                                   e.getMessage());
        } catch (Exception e) {
            if (e instanceof DataAccessException &&
                e.getCause() instanceof RepositoryException) {
                throw (RepositoryException) e.getCause();
            }
            throw e;
        }

        return false;
    }
}
