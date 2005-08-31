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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;

import org.apache.jackrabbit.server.io.AbstractCommand;
import org.apache.jackrabbit.server.io.AbstractContext;
import org.apache.jackrabbit.server.io.ExportContext;
import org.apache.jackrabbit.webdav.DavException;

import org.apache.log4j.Logger;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.icalendar.CosmoICalendarConstants;
import org.osaf.cosmo.jcr.CosmoJcrConstants;

/**
 * A command for exporting a view of the calendar objects contained
 * within a calendar collection.
 */
public class ExportCalendarCollectionCommand extends AbstractCommand {
    private static final Logger log =
        Logger.getLogger(ExportCalendarCollectionCommand.class);
    private static final String BEAN_CALENDAR_DAO = "calendarDao";

    /**
     */
    public boolean execute(AbstractContext context)
        throws Exception {
        if (context instanceof ApplicationContextAwareExportContext) {
            return execute((ApplicationContextAwareExportContext) context);
        }
        else {
            return false;
        }
    }

    /**
     */
    public boolean execute(ApplicationContextAwareExportContext context)
        throws Exception {
        Node resourceNode = context.getNode();
        if (resourceNode == null ||
            ! resourceNode.isNodeType(CosmoJcrConstants.NT_CALDAV_COLLECTION)) {
            return false;
        }

        // extract calendar components from the node
        CalendarDao dao = (CalendarDao) 
            context.getApplicationContext().
            getBean(BEAN_CALENDAR_DAO, CalendarDao.class);
        Calendar calendar = dao.getCalendarObject(resourceNode);

        // fill in the context

        File tmpfile =
            File.createTempFile("__cosmo",
                                CosmoICalendarConstants.FILE_EXTENSION);
        tmpfile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(tmpfile);

        // XXX: non-validating in production mode
        CalendarOutputter outputter = new CalendarOutputter();
        outputter.output(calendar, out);

        out.close();

        context.setInputStream(new FileInputStream(tmpfile));
        context.setContentLength(tmpfile.length());
        context.setModificationTime(tmpfile.lastModified());
        context.setContentType(CosmoICalendarConstants.CONTENT_TYPE +
                               "; charset=utf8");
        Property contentLanguage =
            resourceNode.getProperty(CosmoJcrConstants.NP_XML_LANG);
        context.setContentLanguage(contentLanguage.getString());
        java.util.Calendar creationTime =
            resourceNode.getProperty(CosmoJcrConstants.NP_JCR_CREATED).
            getDate();
        context.setCreationTime(creationTime.getTime().getTime());
        context.setETag("");

        return true;
    }
}
